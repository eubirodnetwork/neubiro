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

import griffon.core.artifact.GriffonService
import griffon.metadata.ArtifactProviderFor
import groovy.io.FileType
import groovy.util.logging.Slf4j
import eu.eubirod.neubiro.transfer.WrongCredentialsException
import org.apache.commons.lang.time.StopWatch
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.yaml.snakeyaml.Yaml

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */
@Slf4j
@ArtifactProviderFor(GriffonService)
class TransferService {
  final static String DESCRIPTOR_FILENAME = "descriptor.yml"
  final static String TRANSFER_FILENAME_PREFIX = "tracciato_record_oracle"
  final static String TRANSFER_FILENAME_SUFFIX = ".zip"

  List collectFiles(File workDir) {
    List result = []

    if (workDir.exists()) {
      def counter = 1
      workDir.eachFileRecurse(FileType.FILES) { file ->
        if (file.name.endsWith(".zip")) {
          def descriptor = getDescriptor(file)
          if (descriptor?.type == 'oracle') {
            result << [
              selected  : false,
              id        : counter,
              name      : file.name,
              path      : file.path - workDir.path - file.name,
              date      : new Date(file.lastModified()).format('dd/MM/yyyy HH:mm:ss'),
              file      : file,
              descriptor: descriptor
            ]
            counter++
          }
        }
      }
    }

    return result
  }

  boolean transferFiles(Map serverParams, List filesList) {
    boolean result = false

    FTPClient ftp = new FTPClient()
    ftp.connect(serverParams.server)
    int reply = ftp.getReplyCode()
    if (FTPReply.isPositiveCompletion(reply)) {
      if (ftp.login(serverParams.username, serverParams.password)) {
        ftp.setFileType(FTP.BINARY_FILE_TYPE)
        if (serverParams.activeFtp) {
          ftp.enterLocalActiveMode();
        } else {
          ftp.enterLocalPassiveMode();
        }

        StopWatch timer = new StopWatch()
        timer.start()

        app.event('transferFilesStarted', [filesList.size()])

        log("Start file transfer", "info")

        def transferFilename
        filesList.each { item ->
          if (item.selected) {
            log("Transferring file ${item.file.name}")

            app.event('transferFilesProgress', [item.file.name])

            transferFilename = "${TRANSFER_FILENAME_PREFIX}_${item.descriptor.operator}_${item.descriptor.year}${TRANSFER_FILENAME_SUFFIX}"

            item.file.withInputStream { is ->
              ftp.storeFile(transferFilename, is)
            }

            log("File ${item.file.name} transferred as ${transferFilename}!")
          }
        }

        ftp.logout()

        result = true

        log("File transfer complete")

        timer.stop()
        log("Overall transfer time is ${timer.toString()}", 'complete')

        app.event('transferFilesFinished')
      } else {
        ftp.disconnect()
        throw new WrongCredentialsException("wrong credentials")
      }
    }

    ftp.disconnect()

    return result
  }

  private Map getDescriptor(File file) {
    Map result = [:]

    def zipFile = new java.util.zip.ZipFile(file)

    def f = zipFile.entries().find { it.name == DESCRIPTOR_FILENAME }
    if (f) {
      def content = zipFile.getInputStream(f).text
      Yaml parser = new Yaml()
      result = parser.load(content)
    }

    return result
  }

  private void log(String msg, String type = "", boolean consoleOnly = false) {
    app.event('WriteLog', [msg, type])
    if (!consoleOnly) {
      log.debug msg
    }
  }
}
