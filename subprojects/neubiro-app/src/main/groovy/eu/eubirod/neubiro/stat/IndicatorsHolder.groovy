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

import eu.eubirod.neubiro.config.NeubiroConfig
import groovy.io.FileType
import groovy.util.logging.*

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */

@Slf4j
@Singleton
class IndicatorsHolder {

  static List<String> DESCRIPTOR_INPUT_FIELDS = IndicatorInput.declaredFields.name.findAll {
    !it.startsWith('$') && !it.startsWith('_') && it != 'metaClass'
  }

  List<Variable> variables = []
  List<Indicator> indicators = []

  void parse(String indicatorsDirname) {
    parse(new File(indicatorsDirname))
  }

  void parse(File indicatorsDir, String currentLocale = 'it') {
    if (indicatorsDir.exists()) {
      def selectUnitSpecsFile = new File(indicatorsDir, NeubiroConfig.INDICATORS_SELECTUNIT_FILE)
      if (selectUnitSpecsFile.exists()) {
        def vars = new ConfigSlurper().parse(selectUnitSpecsFile.toURL())
        vars?.variables?.each { k, v ->
          Variable var = new Variable(
            name: k,
            table: v?.table,
            label: v.containsKey('label') ? v?.label : k,
            type: v.containsKey('type') ? v?.type : 'string',
            mandatory: v.containsKey('mandatory') ? v?.mandatory : false
          )
          add(var)
        }
      }

      indicatorsDir.eachFile(FileType.DIRECTORIES) { dir ->
        def descriptorFile = new File(dir, NeubiroConfig.INDICATOR_SPECS_FILE)
        if (descriptorFile.exists()) {
          def i = parseDescriptor(dir, descriptorFile, currentLocale)
          if (i) {
            add(i)
          }
        }
      }
    }
  }

  private Indicator parseDescriptor(File path, File descriptorFile, String locale) {
    def descriptor = new ConfigSlurper().parse(descriptorFile.toURL())

    def i = new Indicator(
      id: descriptor.indicator.id,
      description: descriptor.indicator["description_${locale}"] ?: descriptor.indicator.description,
      dependsOn: descriptor?.indicator?.dependsOn ?: null,
      hidden: descriptor?.indicator?.hidden ?: false,
      excludeReport: descriptor?.indicator?.excludeReport ?: false,
      outputFiles: descriptor?.indicator?.output?.files ?: null,
      path: path.absolutePath
    )

    def inputDef
    def descriptorInput = descriptor?.indicator?.input
    if (descriptorInput) {
      // Check type of the input definition
      if (DESCRIPTOR_INPUT_FIELDS.intersect(descriptorInput.keySet()).size() != 0) {
        // Single dataset
        inputDef = parseDescriptorInput(descriptorInput)
        i.addInput(inputDef)
      } else {
        // Multiple datasets
        descriptorInput.each { k, v ->
          inputDef = parseDescriptorInput(v)
          i.addInput(inputDef)
        }
      }
    }

    return i
  }

  private IndicatorInput parseDescriptorInput(ConfigObject descriptorInputDef) {
    def inputDef = new IndicatorInput(
      table: descriptorInputDef?.table ?: null,
      sql: descriptorInputDef?.sql ?: null,
      sqlDdl: descriptorInputDef?.sqlDdl ?: null,
      fields: descriptorInputDef?.fields ?: null,
      groups: descriptorInputDef?.groups ?: null,
      order: descriptorInputDef?.order ?: null,
      criteria: descriptorInputDef?.criteria ?: null,
      file: descriptorInputDef?.file ?: 'input.csv'
    )
    return inputDef
  }

  /**
   Generates the list of indicators to execute with all the dependencies resolved
   */
  Map getIndicatorsToRun(List indicatorsList) {
    def selectedIndicators = indicatorsList.findAll { it.selected }

    def result = [:]
    selectedIndicators.each { i ->
      // Get the indicator's dependencies
      def tmpDeps = i.obj.dependsOn ?: []

      // Add it
      result[i.id] = tmpDeps

      // Prepare the list of the dependent indicators for the recursive call
      def dependentIndicators = tmpDeps.collect([]) { [id: it, obj: get(it), selected: true] }

      // Recursive call to resolve the dependencies of the dependencies!
      def resolvedDependencies = getIndicatorsToRun(dependentIndicators)

      // Add the resolved dependencies to the result
      resolvedDependencies.each { k, v ->
        result[k] = v
      }
    }

    return result
  }

  List<String> getExecutionList(List selectedIndicators) {
    def indicatorsToRun = getIndicatorsToRun(selectedIndicators)

    // Calculate the right execution order
    def resolver = new DependencyResolver()
    resolver.addIndicators(indicatorsToRun)

    return resolver.resolve()
  }

  Indicator get(String id) {
    indicators.find { it.id == id }
  }

  Variable getVariable(String name) {
    variables.find { it.name == name }
  }

  void clear() {
    indicators.clear()
    variables.clear()
  }

  List<Indicator> getVisibleIndicators() {
    indicators.findAll { !it.hidden }
  }

  List<Indicator> getVisibleIndicatorsForUI() {
    def list = []
    visibleIndicators.each { i ->
      list << [
        id           : i.id,
        description  : i.description,
        obj          : i,
        selected     : false,
        hidden       : i.hidden,
        excludeReport: i.excludeReport
      ]
    }
    list
  }

  void add(Indicator i) {
    indicators << i
  }

  List<Variable> getVariables() {
    variables
  }

  List<Variable> getVariablesForUI() {
    def list = []
    variables.each { v ->
      list << [
        name     : v.name,
        label    : v.label,
        type     : v.type,
        mandatory: v.mandatory
      ]
    }
    list
  }

  void add(Variable v) {
    variables << v
  }
}
