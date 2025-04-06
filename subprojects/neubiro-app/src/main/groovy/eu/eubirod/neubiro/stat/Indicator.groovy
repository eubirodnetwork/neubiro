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
package eu.eubirod.neubiro.stat

import groovy.sql.Sql
import org.apache.commons.io.FileUtils
import au.com.bytecode.opencsv.CSVWriter

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */
class Indicator {
  String id
  String description

  Boolean hidden
  Boolean excludeReport

  List<String> dependsOn

  List<IndicatorInput> input = []

  List<String> outputFiles

  String path

  void addInput(IndicatorInput inputDef) {
    input << inputDef
  }

  void prepareData(Sql connection, List whereData, File outputDir) {
    File outputFile
    String sqlStmt
    input?.each { inputDef ->
      // A DDL statement take precedence over simple sql query
      if (inputDef.sqlDdl) {
        connection.executeUpdate(inputDef.sqlDdl)
      } else {
        outputFile = new File(outputDir, inputDef.file)
        sqlStmt = createSqlSelect(inputDef, whereData)
        writeCSV(connection, sqlStmt, outputFile)
      }
    }
  }

  void fastPrepareData(File sourceDir, File outputDir) {
    File sourceIndicatorDir = new File(sourceDir, id)
    File fileToCopy
    input?.each { inputDef ->
      fileToCopy = new File(sourceIndicatorDir, inputDef.file)
      FileUtils.copyFileToDirectory(fileToCopy, outputDir)
    }
  }

  private void writeCSV(Sql connection, String sqlStmt, File outputFile) {
    outputFile.withWriter { writer ->
      CSVWriter csvWriter = new CSVWriter(writer)

      boolean writeHeader = true
      connection.eachRow(sqlStmt) { row ->
        def tmpRow = row.toRowResult()

        if (writeHeader) {
          // Write header
          csvWriter.writeNext(tmpRow.keySet() as String[])
          writeHeader = false
        }

        csvWriter.writeNext(tmpRow.values() as String[])
      }
    }
  }

  private String createSqlSelect(IndicatorInput inputDef, List whereData) {

    // Check if the data must be prepared on a subset of the table
    String tableName = inputDef.table?.trim()?.toLowerCase()
    String finalTableName = tableName
    String variableAssociatedTable = ""
    String var
    if (whereData) {
      var = whereData[0]
      if (var) {
        String value = whereData[1]
        Variable variable = IndicatorsHolder.instance.getVariable(var)
        variableAssociatedTable = variable.table?.trim()?.toLowerCase()
        String variableType = variable.type?.trim()?.toLowerCase()
        finalTableName = "(SELECT * FROM ${tableName} WHERE ${var} = ${variableType == 'string' ? '\'' + value + '\'' : value})"
      }
    }

    def sqlStmt = "" << ""
    if (inputDef.sql) {
      String sqlText = inputDef.sql.toString()
      // Update the query only if the select unit variable references the same table
      if ((variableAssociatedTable == tableName) && var) {
        def matcher = (sqlText =~ /(?i)(FROM|JOIN)\s+(\S+)/)
        if (matcher.hasGroup()) {
          def op = matcher[0][1]
          def tbl = matcher[0][2]
          if (tbl?.toLowerCase() == tableName?.toLowerCase()) {
            sqlText = sqlText.replaceAll("(?i)${tbl}", finalTableName)
          }
        }
      }
      sqlStmt << sqlText
    } else {
      // Export data
      sqlStmt << "SELECT"
      sqlStmt << " "
      sqlStmt << inputDef.fields.join(',')
      sqlStmt << " "
      sqlStmt << "FROM " << finalTableName

      if (inputDef.criteria) {
        sqlStmt << " "
        sqlStmt << "WHERE"
        sqlStmt << " "
        sqlStmt << inputDef.criteria.join(' AND ')
      }

      if (inputDef.groups) {
        sqlStmt << " "
        sqlStmt << "GROUP BY"
        sqlStmt << " "
        sqlStmt << inputDef.groups.join(',')
      }

      if (inputDef.order) {
        sqlStmt << " "
        sqlStmt << "ORDER BY"
        sqlStmt << " "
        sqlStmt << inputDef.order.join(',')
      }

      sqlStmt << ";"
    }

    return sqlStmt.toString()
  }

  boolean hasDataToPrepare() {
    input ? input?.any { it?.hasDataToPrepare() ?: false } : false
  }
}
