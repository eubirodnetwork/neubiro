/*
 * Copyright 2014-2025 Stefano Gualdi, AGENAS.
 *
 * Licensed under the European Union Public Licence (EUPL), Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.eubirod.neubiro

import com.github.rcaller.rstuff.FailurePolicy
import com.github.rcaller.rstuff.RCallerOptions
import com.github.rcaller.rstuff.RProcessStartUpOptions
import com.github.rcaller.util.Globals
import com.sun.jna.platform.win32.Advapi32Util
import eu.eubirod.neubiro.config.NeubiroConfig
import griffon.core.GriffonApplication
import griffon.core.artifact.GriffonService
import griffon.metadata.ArtifactProviderFor
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import eu.eubirod.neubiro.stat.DataPreparationException
import eu.eubirod.neubiro.stat.Indicator
import eu.eubirod.neubiro.stat.IndicatorsHolder
import eu.eubirod.neubiro.utils.NeubiroUtils

import org.apache.commons.lang.time.StopWatch
import org.h2.tools.Server
import org.yaml.snakeyaml.Yaml
import com.github.rcaller.rstuff.RCaller
import com.github.rcaller.rstuff.RCode
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject
import java.nio.file.Path
import java.nio.file.Paths
import java.security.CodeSource
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE
import static griffon.util.GriffonApplicationUtils.*

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */
@Slf4j
@ArtifactProviderFor(GriffonService)
class StatEngineService {

  @Inject
  GriffonApplication app

  @Inject
  DatabaseService databaseService

  @Inject
  NeubiroConfig neubiroConfig

  String startupPath
  String rScriptPath
  String h2JarFilePath

  @PostConstruct
  void serviceInit() {
    Path currentRelativePath = Paths.get("")
    startupPath = currentRelativePath.toAbsolutePath().toString()
    rScriptPath = getRscriptPathForCurrentPlatform()
    h2JarFilePath = getH2Jarfile()

    log.debug "Startup path set to: ${startupPath}"
    log.debug "R script path set to: ${rScriptPath}"
    log.debug "H2 jar file is: ${h2JarFilePath}"

    log.debug "Service inited"
  }

  @PreDestroy
  void serviceDestroy() {
    log.debug "Service destroyed"
  }

  Boolean isRInstalled() {
    File rscript = new File(rScriptPath)
    rscript.exists()
  }

  void loadIndicators(File indicatorsDir, String language) {
    IndicatorsHolder indicatorsHolder = IndicatorsHolder.instance
    indicatorsHolder.clear()
    indicatorsHolder.parse(indicatorsDir, language)
  }

  Boolean runIndicators(List indicators, Map runningParams, File indicatorsDir, File workDir, File firstWorkDir, File libsDir) {
    IndicatorsHolder indicatorsHolder = IndicatorsHolder.instance

    StopWatch timer = new StopWatch()
    timer.start()

    def runList = indicatorsHolder.getExecutionList(indicators)

    app.eventRouter.publishEvent('RunIndicatorsStarted', [runList.size()])

    // Execute each indicator in the right order
    def result = true
    for (i in runList) {
      app.eventRouter.publishEvent('RunIndicatorsProgress', [i])

      def ind = indicatorsHolder.get(i)
      result = runIndicator(ind, runningParams, indicatorsDir, workDir, firstWorkDir, libsDir)
      if (!result) {
        log.error "Indicator ${i} failed!"
        break
      }
    }

    if (result) {
      app.eventRouter.publishEvent('RunIndicatorsFinished')
    }

    timer.stop()

    log("Overall execution time is ${timer.toString()}", 'complete')

    return result
  }

  Boolean runIndicator(Indicator indicator, Map runningParams, File indicatorsDir, File workDir, File firstWorkDir, File libsDir) {
    Boolean result = false

    StopWatch timer = new StopWatch()
    timer.start()

    // Prepare base directory for R libraries
    if (!libsDir.exists()) {
      libsDir.mkdirs()
    }

    // Prepare output/working directory for R script
    if (!workDir.exists()) {
      workDir.mkdirs()
    }

    def outputDir = new File(workDir, indicator.id)
    if (!outputDir.exists()) {
      outputDir.mkdirs()
    } else {
      // Cleanup directory
      outputDir.deleteDir()
      outputDir.mkdirs()
    }

    log("Running indicator ${indicator.id}", 'info')

    if (indicator.hasDataToPrepare() && runningParams.engine_type == 'local') {
      app.eventRouter.publishEvent('RunIndicatorsPhase', [indicator.id, 1])

      log("Data preparation start")

      if (firstWorkDir && !(runningParams.selector[0] && runningParams.selector[1])) {
        // Fast data preparation (copy input data from the first indicator)
        indicator.fastPrepareData(firstWorkDir, outputDir)

        log("Fast prepare data end in ${timer.toString()}")
      } else {
        // Select data as requested in specs
        Sql sql = databaseService.connect()
        try {
          indicator.prepareData(sql, runningParams.selector, outputDir)
        }
        catch (Exception e) {
          app.eventRouter.publishEvent('RunIndicatorError', [indicator.id])
          log("Data preparation error", "error")
          throw new DataPreparationException(app.messageSource.getMessage('application.message.indicators.error.dataPreparation'), e)
        }
        finally {
          sql.close()
        }

        log("Prepare data end in ${timer.toString()}")
      }
    } else {
      log("No data to prepare for indicator")
    }

    app.eventRouter.publishEvent('RunIndicatorsPhase', [indicator.id, 2])

    log.debug "Run R start"

    // Run R
    File scriptDir = new File(indicator.path)
    File scriptFile = new File(scriptDir, NeubiroConfig.INDICATOR_SOURCE_FILE)
    if (scriptFile.exists()) {
      try {
        execScript(scriptFile, runningParams, indicatorsDir, outputDir, libsDir)

        // Check if zip specifications are present and if present create the zip
        Yaml yaml = new Yaml()
        outputDir.eachFile { f ->
          if (f.name.endsWith(".zip.yml")) {
            def zipDesc = yaml.load(f.text)
            createZipFile(zipDesc, outputDir)
            if (zipDesc.cleanup) {
              f.delete()
            }
          }
        }

        // Check expected output
        result = indicator.outputFiles ? indicator.outputFiles.any { new File(outputDir, it).exists() } : true

        if (log.isDebugEnabled() && !result) {
          log.error "Expected output not found (${indicator.outputFiles})"
        }
      }
      catch (Exception e) {
        log.error e.message, e
        result = false
      }
    } else {
      log.debug "No code to run"
      result = true
    }

    if (result) {
      app.eventRouter.publishEvent('RunIndicatorsPhase', [indicator.id, 3])
    } else {
      log("Execution error", "error")
      app.eventRouter.publishEvent('RunIndicatorError', [indicator.id])
    }

    log.debug "Run R end"

    timer.stop()

    log("Done with indicator ${indicator.id} in ${timer.toString()}")

    return result
  }

  def execScript(File script, Map runningParams, File baseDir, File workDir, File libsDir) {
    String pScript = NeubiroUtils.convertToPlatformSpecificPath(script.absolutePath)
    String pBaseDir = NeubiroUtils.convertToPlatformSpecificPath(baseDir.absolutePath)
    String pWorkDir = NeubiroUtils.convertToPlatformSpecificPath(workDir.absolutePath)
    String pLibsDir = NeubiroUtils.convertToPlatformSpecificPath(libsDir.absolutePath)
    String pH2JarFile = NeubiroUtils.convertToPlatformSpecificPath(h2JarFilePath)
    String startupDir = NeubiroUtils.convertToPlatformSpecificPath(startupPath)

    RCode code = RCode.create()

    // Prepare env for R code
    code.addRCode("rm(list=ls(all=TRUE))")
    code.addRCode("baseDir <- \"${pBaseDir}\"")
    code.addRCode("workDir <- \"${pWorkDir}\"")
    code.addRCode("libsDir <- \"${pLibsDir}\"")
    code.addRCode("h2JarFile <- \"${pH2JarFile}\"")
    code.addRCode("startupDir <- \"${startupDir}\"")

    // Add custom libraries path for R
    code.addRCode(".libPaths(c(\"${pLibsDir}\", .libPaths()))")

    // Define global parameters for R code
    def tmp
    runningParams.each { k, v ->
      if (v instanceof Collection) {
        if (!v.empty) {
          tmp = "${k} <- c(${v.collect { "\"${it}\"" }.join(",")})"
        } else {
          tmp = "${k} <- \"\""
        }
      } else {
        tmp = "${k} <- \"${v}\""
      }
      code.addRCode(tmp)
    }

    // Set workDir
    code.addRCode("setwd(workDir)")

    // Open logging
    code.addRCode("con <- file(\"indicator.log\")")
    code.addRCode("sink(con, append=FALSE)")
    code.addRCode("sink(con, append=FALSE, type=\"message\")")

    // Load indicator source code
    code.addRCode("source(\"${pScript}\")")

    // Close logging
    code.addRCode("sink()")
    code.addRCode("sink(type=\"message\")")

    // Init Rcaller and set executable path for Rscript
    RProcessStartUpOptions rStartupOptions = RProcessStartUpOptions.create()
    rStartupOptions.setVanilla(true)
    // rStartupOptions.setNoSave(true)
    // rStartupOptions.setNoEnviron(true)
    // rStartupOptions.setNoRestore(true)
    RCallerOptions rOptions = RCallerOptions.create(rScriptPath, Globals.R_current, FailurePolicy.RETRY_5, Long.MAX_VALUE, 100, rStartupOptions)
    RCaller rcaller = RCaller.create(rOptions)

    log.debug "R script path set to: ${rOptions.getrScriptExecutable()}"

    // Init code
    rcaller.setRCode(code)

    log.debug "${rcaller.getRCode()}"

    // Excecute R code
    rcaller.runOnly()

    // Cleanup temp files
    rcaller.deleteTempFiles()
  }

  private String getH2Jarfile() {
    CodeSource codeSource = Server.class.getProtectionDomain().getCodeSource()
    File jarFile = new File(codeSource.getLocation().toURI().getPath())
    jarFile.absolutePath
  }

  private String getRscriptPathForCurrentPlatform() {
    def rScript = null
    def rpath = neubiroConfig.get("rpath")

    if (!rpath) {
      if (isWindows) {
        log.debug "Found WINDOWS"
        String installPath = Advapi32Util.registryGetStringValue(HKEY_LOCAL_MACHINE, "SOFTWARE\\R-core\\R", "InstallPath")
        rScript = "${installPath}${File.separator}bin${File.separator}${NeubiroConfig.R_WINDOWS_SCRIPT}"
        rScript = NeubiroUtils.convertToPlatformSpecificPath(rScript)
      } else if (isLinux) {
        log.debug "Found LINUX"
        rScript = "/usr/bin/${NeubiroConfig.R_UNIX_SCRIPT}"
      } else if (isMacOSX) {
        log.debug "Found OSX ${getOsVersion()}"
        if (NeubiroUtils.versionCompare(getOsVersion(), "10.10.5") == 1) {
          rScript = "/usr/local/bin/${NeubiroConfig.R_UNIX_SCRIPT}"
        } else {
          rScript = "/usr/bin/${NeubiroConfig.R_UNIX_SCRIPT}"
        }
      } else {
        log.debug "No OS Found"
        // Unsupported platform
        rScript = null
      }
    } else {
      log.debug "Get R script path from config file!"
      rScript = NeubiroUtils.convertToPlatformSpecificPath(rpath)
    }

    return rScript
  }

  private createZipFile(zipDesc, outputDir) {
    def zipFilename = zipDesc.file
    def cleanup = zipDesc.cleanup
    def files = zipDesc.files

    if (files.size > 0) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream()
      ZipOutputStream zipFile = new ZipOutputStream(baos)
      File curFile
      def filename
      def renamedFilename
      def cleanupList = []
      files.each { f ->
        def parts = f.split(/\|/)
        if (parts.size() > 1) {
          filename = parts[0]
          renamedFilename = parts[1]
        } else {
          filename = f
          renamedFilename = f
        }

        curFile = new File(outputDir, filename)
        if (curFile.exists()) {
          zipFile.putNextEntry(new ZipEntry(renamedFilename))
          curFile.withInputStream { i -> zipFile << i }
          zipFile.closeEntry()

          if (cleanup) {
            cleanupList << curFile
          }
        } else {
          log("Missing file ${curFile.name} when creating ${zipFilename}", "error")
        }
      }
      zipFile.finish()

      OutputStream outputStream = new FileOutputStream(new File(outputDir, zipFilename))
      baos.writeTo(outputStream)
      outputStream.flush()
      outputStream.close()

      cleanupList.each { f ->
        f.delete()
      }
    }
  }

  private void log(String msg, String type = "", boolean consoleOnly = false) {
    app.eventRouter.publishEvent('WriteLog', [msg, type])
    if (!consoleOnly) {
      log.debug msg
    }
  }
}
