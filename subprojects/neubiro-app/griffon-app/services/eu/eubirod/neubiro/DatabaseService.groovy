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

import com.google.util.io.base.UnicodeReader
import eu.eubirod.neubiro.config.NeubiroConfig
import eu.eubirod.neubiro.db.ImportEnhancerFunctions
import griffon.core.GriffonApplication
import griffon.core.artifact.GriffonService
import griffon.metadata.ArtifactProviderFor
import groovy.sql.Sql
import groovy.time.TimeCategory
import groovy.util.logging.Slf4j
import eu.eubirod.neubiro.db.CSVFile
import eu.eubirod.neubiro.db.MappingsHolder
import eu.eubirod.neubiro.db.LookupsHelper
import eu.eubirod.neubiro.db.SpecsFile
import eu.eubirod.neubiro.db.ImportEnhancerCategory
import eu.eubirod.neubiro.stat.IndicatorsHolder
import org.apache.commons.lang.time.StopWatch
import org.h2.tools.Server

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.ResultSet

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */
@Slf4j
@ArtifactProviderFor(GriffonService)
class DatabaseService {
  static String DATABASE_DRIVER = 'org.h2.Driver'
  static String DATABASE_NAME = 'neubiro-tcp-db'
  static String DATABASE_PORT = '4040'
  static String DATABASE_USERNAME = ''
  static String DATABASE_PASSWORD = ''

  @Inject
  GriffonApplication app

  @Inject
  NeubiroConfig config

  Server localServer = null
  String connectionURL

  @PostConstruct
  void serviceInit() {
    def args = ["-tcpPort", DATABASE_PORT, "-tcpAllowOthers", "-ifNotExists"] as String[]
    localServer = Server.createTcpServer(args)
    localServer.start()

    File wd = config.getHomeDirectory()
    connectionURL = "jdbc:h2:tcp://localhost:${DATABASE_PORT}/file://${new File(wd, DATABASE_NAME).absolutePath}"

    log.debug "Local db url: ${connectionURL}"

    log.debug "Service inited"
  }

  @PreDestroy
  void serviceDestroy() {
    if (localServer) {
      localServer.stop()
    }

    log.debug "Service destroyed"
  }

  Sql connect() {
    Sql.newInstance(connectionURL, DATABASE_USERNAME, DATABASE_PASSWORD, DATABASE_DRIVER)
  }

  void loadSpecs(File specsFile) {
    MappingsHolder mappingsHolder = MappingsHolder.instance
    mappingsHolder.clear()
    mappingsHolder.parse(specsFile)
  }

  def importAll(File file, Map context, Map lookupsFiles) {
    MappingsHolder mappingsHolder = MappingsHolder.instance

    LookupsHelper.instance.cleanup()

    StopWatch timer = new StopWatch()
    timer.start()

    def filesToImport = []

    lookupsFiles.each { k, v ->
      filesToImport << [
        specs  : mappingsHolder.get(k),
        file   : new File(v),
        context: [:]
      ]
    }

    if (file) {
      // Import THE UNIQUE master file after mappings files
      String masterSpecId = mappingsHolder.getMasterSpecs()[0].id
      filesToImport << [
        specs  : mappingsHolder.get(masterSpecId),
        file   : file,
        context: context
      ]
    }

    // Import all
    filesToImport.each { d ->
      importFile(d)
    }

    timer.stop()

    log("Overall import time is ${timer.toString()}", 'complete')
  }

  private importFile(Map importDef) {
    SpecsFile specs = importDef.specs

    File file = importDef.file
    String tableName = specs.id

    log("Import started for ${tableName}", 'info')

    StopWatch timer = new StopWatch()
    timer.start()

    Map context = importDef.context
    context.func = ImportEnhancerFunctions.class

    // Elenco dei campi richesti per l'import
    def fieldsToImport = specs.getFieldsMap()

    def noFieldsToRead = specs.getFieldsNo()

    def csvImportOptions = [
      separatorChar: specs?.separatorChar,
      quoteChar    : specs?.quoteChar,
      escapeChar   : specs?.escapeChar,
      skipLines    : 1
    ]

    // Definizione del file csv da importare
    def csvFile = new CSVFile(file, csvImportOptions)

    // Mappatura ed indicizzazione dei campi presenti nel file csv
    def csvFieldsMap = csvFile.getColumnsMap(fieldsToImport.collect { it.name })

    // DDL di creazione tabella ed indici
    def createTableStmt = specs.createTableDDL()
    def createIndexesStmt = specs.createIndexesDDL()

    Sql sql = connect()

    // FIXME: ugly!!!
    LookupsHelper.instance.setDb(sql)

    sql.execute(createTableStmt)
    if (createIndexesStmt) {
      sql.execute(createIndexesStmt)
    }

    def fileSize = file.size()

    app.eventRouter.publishEvent('ImportStarted', [fileSize])

    def vars
    def values
    def varPlaceholder

    def record
    def recordMeta

    def missingLogged = [:]
    def recNo = 1
    def lineNo = 2
    def counter = 0

    new UnicodeReader(file).toCsvReader(csvImportOptions).eachLineCsv { String[] tokens ->
      vars = []
      values = []
      varPlaceholder = []

      record = [:]
      recordMeta = [:]
      fieldsToImport.each { f ->
        def fname = f.name.trim()

        // Import only not empty fields. We want NULL in the db to recognize missing values!
        def v
        if (f?.calculated) {
          try {
            use(ImportEnhancerCategory) {
              v = f.value(record, context)
            }
          }
          catch (Exception e) {
            v = null
            log("Cannot calculate field ${fname}", 'error')
            log.error("Cannot calculate field ${fname}", e)
          }
        } else {
          def fidx = csvFieldsMap[fname]
          if (fidx == null) {
            if (!missingLogged[fname]) {
              // To prevent too much logging tracks missing fields only once
              log("Missing field ${fname} in CSV file.", 'error')
              missingLogged[fname] = true
            }
            v = null
          } else {
            v = convertValueToSpecType(tokens[fidx], f)
            // If present run the validity check for field
            if (f?.valid) {
              try {
                if (!f.valid(v)) {
                  v = null
                }
              } catch (Exception e) {
                log("Cannot run field's validity check for field ${fname} at line ${lineNo}", 'error')
                log.error("Cannot run field's validity check for field ${fname} at line ${lineNo}", e)
              }
            }
          }
        }

        recordMeta[fname] = [
          persist  : f.persist,
          name     : f.nameTo ? f.nameTo.trim() : fname,
          mandatory: f.mandatory
        ]

        record[fname] = v
      }

      // If present run the recordCheck closure
      def discard = false
      if (specs?.recordCheck) {
        def checkResult
        try {
          checkResult = specs.recordCheck(record)
          if (checkResult?.action?.toLowerCase() == "save") {
            // If present use the data returned by the recordCheck closure
            if (checkResult?.record) {
              record = checkResult.record
            }
          } else {
            // The record must be discared, log it
            log("Record discarded by recordCheck due to ${checkResult?.message} at line ${lineNo}", 'error')
            discard = true
          }
        } catch (Exception e) {
          log("Cannot run recordCheck due to ${e.getMessage()} at line ${lineNo}", 'error')
          log.error("Cannot run recordCheck at line ${lineNo}", e)
        }
      }

      if (!discard) {
        // Check for empty mandatory fields
        def errors = []
        record.each { fname, value ->
          if (recordMeta[fname].mandatory && isEmptyOrNull(value)) {
            errors << "Field ${fname} is mandatory but has no value. Record not imported at line ${lineNo}".toString()
          }
        }

        // If there are missing mandatory fields discard the record
        if (errors.size() == 0) {
          // Compose SQL elements
          record.each { fname, value ->
            if ((value != null) && recordMeta[fname].persist) {
              vars << "\"" + recordMeta[fname].name + "\""
              values << value
              varPlaceholder << "?"
              // println recordMeta[fname].name  + " -> " + value
            }
          }

          // println ">>>>>>>>>>>>>>>>>>>>>>> " + vars
          // println ">>>>>>>>>>>>>>>>>>>>>>> " + values

          try {
            sql.execute("INSERT INTO \"${tableName}\" (${vars.join(',')}) VALUES(${varPlaceholder.join(',')})", values)
            recNo++
          } catch (Exception e) {
            log("Invalid CSV line skipped. Cannot import record due to ${e.getMessage()} at line ${lineNo}", 'error')
            log.error("Cannot import record at line ${lineNo}", e)
            throw new Exception(e)
          }
        } else {
          errors.each { String msg ->
            log(msg, 'error')
          }
        }
      }

      app.eventRouter.publishEvent('ImportProgressBarUpdate', [recNo, counter])

      counter += tokens.join(" ").size()
      lineNo++
    }

    app.eventRouter.publishEvent('ImportFinished', [recNo - 1])

    sql.close()

    timer.stop()

    log("Import completed for ${tableName} - ${recNo - 1} records in ${timer.toString()}")

    if (specs.locf) {
      log("LOCF process started for ${tableName}", 'info')

      timer.reset()
      timer.start()

      // DDL di creazione tabella ed indici per tabella LOCF
      def masterLocfTableName = specs.locf.table
      def createLocfTableStmt = specs.createTableDDL(masterLocfTableName)
      def createLocfIndexesStmt = specs.createIndexesDDL(masterLocfTableName)

      sql = connect()

      // Create the LOCF target table
      sql.execute(createLocfTableStmt)
      if (createLocfIndexesStmt) {
        sql.execute(createLocfIndexesStmt)
      }

      // Crea istruzione select con il group by che utilizza le chiavi definite e oder by medesimo
      String masterTable = specs.id
      String masterOrder = specs.locf.order.collect {"\"${it}\""}.join(",")
      String sqlLocf = "SELECT * from \"${masterTable}\" ORDER BY ${masterOrder}"
      String sqlLocfCount = "SELECT COUNT(*) as cnt from \"${masterTable}\""

      def masterTableCount = sql.firstRow(sqlLocfCount)?.cnt

      app.eventRouter.publishEvent('LocfStarted', [masterTableCount])

      List<String> keyDefs = specs.locf.keys.collect { it.trim() }
      List<String> excludedFields = (keyDefs + specs.locf.exclude).collect { it.trim() }
      String key = null
      String lastKey = null
      def curRow = null
      def prevRow = null
      Long idx = 0
      sql.eachRow(sqlLocf) { row ->
        curRow = convertRowToMap(row)

        key = keyDefs.collect { curRow[it] }.join("-")
        if (key != lastKey) {
          lastKey = key
          if (prevRow != null) {
            saveLocfRecord(prevRow, masterLocfTableName, sql)
          }
        } else {
          curRow?.keySet()?.each { k ->
            if (!(k in excludedFields)) {
              if (!curRow[k] && prevRow[k]) {
                curRow[k] = prevRow[k]
              }
            }
          }
        }
        prevRow = curRow

        // Check if we are on the last record
        if (idx == masterTableCount - 1) {
          saveLocfRecord(prevRow, masterLocfTableName, sql)
        }

        idx++

        app.eventRouter.publishEvent('LocfProgressBarUpdate', [recNo])
      }

      app.eventRouter.publishEvent('LocfFinished', [idx])

      sql.close()
      timer.stop()

      log("LOCF process completed for ${tableName} in ${timer.toString()}")
    }
  }

  private isEmptyOrNull(value) {
    if (value == null) {
      true
    } else {
      if (value instanceof String || value instanceof GString) {
        value?.trim() == ""
      }
    }
  }

  private Map convertRowToMap(row) {
    def curRow = [:]
    row.toRowResult().keySet().each { column ->
      curRow[column] = row[column]
    }
    curRow
  }

  private saveLocfRecord(record, tableName, sql) {
    def vars = []
    def values = []
    def varPlaceholder = []
    record.each { fname, value ->
      if (value != null) {
        vars << "\"" + fname + "\""
        values << record[fname]
        varPlaceholder << "?"
      }
    }

    try {
      sql.execute("INSERT INTO \"${tableName}\" (${vars.join(',')}) VALUES(${varPlaceholder.join(',')})", values)
    } catch (Exception e) {
      log.error("Cannot save LOCF record", e)
      throw new Exception(e)
    }
  }

  private convertValueToSpecType(value, field) {
    def result
    if (value instanceof String) {
      String type = field?.type?.toLowerCase() ?: 'varchar'
      try {
        switch (type) {
          case "varchar":
            int size = field?.size ?: 255
            if (value.size() <= size) {
              result = value
            } else {
              result = null
            }
            break
          case "decimal":
            result = value.toDouble()
            break
          case "int":
          case "smallint":
            result = value.toInteger()
            break
          case "boolean":
            result = value.toBoolean()
            break
          case "date":
            result = Date.parse(field.format, value)
            break
        }
      } catch (Exception e) {
        // If not parsable return null
        result = null
      }
    } else {
      result = value
    }

    result
  }

  List collectUniqueValuesFor(String varName) {
    List result = []

    IndicatorsHolder indicators = IndicatorsHolder.instance

    Sql sql = connect()

    String masterTableName = indicators.getVariable(varName).table

    String stmt = "select distinct ${varName} from ${masterTableName} order by ${varName}".toString()
    sql.eachRow(stmt) { row ->
      def tmpRow = row.toRowResult()
      result << tmpRow[varName]
    }

    sql.close()

    return result
  }

  Map collectStats() {
    Map<String, Long> stats = [:]

    Sql sql = connect()

    // Collect all tables' names
    def md = sql.connection.metaData
    ResultSet rs = md.getTables(null, null, "%", ["TABLE"] as String[]);
    while (rs.next()) {
      stats[rs.getString(3)] = 0
    }

    // Count records for each table
    stats.keySet().each { k ->
      String countSql = "SELECT COUNT(*) AS numberOfRows FROM \"${k}\"".toString()
      def countRows = sql.firstRow(countSql)
      stats[k] = countRows.numberOfRows
    }

    sql.close()

    return stats
  }

  private void log(String msg, String type = "", boolean consoleOnly = false) {
    app.eventRouter.publishEvent('WriteLog', [msg, type])
    if (!consoleOnly) {
      log.debug msg
    }
  }
}
