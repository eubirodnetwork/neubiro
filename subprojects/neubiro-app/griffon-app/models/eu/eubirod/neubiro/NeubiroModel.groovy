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

import ca.odell.glazedlists.*
import griffon.core.artifact.GriffonModel
import griffon.metadata.ArtifactProviderFor
import griffon.transform.Observable
import griffon.transform.PropertyListener
import eu.eubirod.neubiro.db.MappingsHolder

import static griffon.util.GriffonNameUtils.isBlank

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */

@Observable
@PropertyListener('enabler')
@ArtifactProviderFor(GriffonModel)
class NeubiroModel {
  // Config
  String operatorName
  String year
  String mainLanguage

  // Indicators options
  String referenceType
  String engineType
  String language
  String selectUnitVariable
  String selectUnitValue
  String highlightsVariable
  String highlightsValue

  // Files
  String csvFile
  String specsFile
  String indicatorsDir
  String workDir

  // Transfer
  String server
  String username
  String password
  boolean activeFtp

  // Dyn files
  Map lookupsFiles = [:]

  // Database status
  Map dbStats = [:]

  // Processing statuses
  boolean importing = false
  boolean running = false
  boolean transferring = false
  boolean canImport = false
  boolean canImportLookups = false
  boolean canRun = false
  boolean canRefresh = false
  boolean canRefreshFiles = false
  boolean canTransferFiles = false
  boolean canClearLog = false
  boolean canShowExternalFiles = false
  boolean canShowInputFiles = false

  // Progress bar
  long minProgressBar = 0
  long maxProgressBar = 0
  long current = 0
  String status = ""

  Map getVariables() {
    def result = [:]
    variablesList.each { v ->
      def type = v.type.toLowerCase()
      def value
      if (type == "string") {
        value = v.value
      } else if (type == "integer") {
        value = Integer.parseInt(v.value)
      } else if (type == "decimal") {
        value = Double.parseDouble(v.value)
      } else {
        value = v.value
      }
      result[v.name] = value
    }
    return result
  }

  boolean areMandatoryVariablesFilled() {
    def invalidCount = 0
    variablesList.each { itm ->
      if (itm.mandatory && isBlank(itm.value)) {
        invalidCount++
      }
    }

    return (invalidCount == 0)
  }

  boolean areLookupsTablesFilled() {
    def invalidCount = 0

    def lookups = MappingsHolder.instance.getLookupsSpecs()
    lookups.each { itm ->
      if (itm.mandatory && (!dbStats[itm.id] || (dbStats[itm.id] == 0)) && isBlank(lookupsFiles[itm.id])) {
        invalidCount++
      }
    }

    return (invalidCount == 0)
  }

  private enabler = { e ->
    canImport = !isBlank(csvFile) && !isBlank(specsFile) && !importing
    def hasLookups = MappingsHolder.instance.getLookupsSpecs().size() > 0
    canImportLookups = !isBlank(specsFile) && !importing && hasLookups && areLookupsTablesFilled()
    canRun = !isBlank(workDir) && !isBlank(indicatorsDir) && !isBlank(language) && !isBlank(referenceType) && !isBlank(engineType) && !running && !importing
    canRefresh = !isBlank(indicatorsDir) && !running && !importing
    canRefreshFiles = !isBlank(workDir) && !running && !importing && !transferring
    canTransferFiles = !isBlank(workDir) && !isBlank(server) && !isBlank(username) && !isBlank(password) && !running && !importing && !transferring
    canClearLog = !running && !importing && !transferring
    canShowExternalFiles = !isBlank(referenceType) && referenceType == '_external_'
    canShowInputFiles = !isBlank(engineType) && engineType == 'central'
  }

  // Lookup tables
  EventList tablesList = new SortedList(
    new BasicEventList(),
    { a, b -> a.id <=> b.id } as Comparator
  )

  // Context variables
  EventList variablesList = new SortedList(
    new BasicEventList(),
    { a, b -> a.name <=> b.name } as Comparator
  )

  // Indicators
  EventList indicatorsList = new SortedList(
    new BasicEventList(),
    { a, b -> a.id <=> b.id } as Comparator
  )

  // Reference files
  ListSelectionModel referenceFilesListSelection = new ListSelectionModel()
  EventList referenceFilesList = new SortedList(
    new BasicEventList(),
    { a, b -> a.name <=> b.name } as Comparator
  )

  // Input files
  ListSelectionModel inputFilesListSelection = new ListSelectionModel()
  EventList inputFilesList = new SortedList(
    new BasicEventList(),
    { a, b -> a.name <=> b.name } as Comparator
  )

  // Files to transfer
  EventList filesList = new SortedList(
    new BasicEventList(),
    { a, b -> a.name <=> b.name } as Comparator
  )

  // Select unit variables
  EventList selectUnitVariableList = new BasicEventList()
}
