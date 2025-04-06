/*
 * Copyright 2014 Stefano Gualdi.
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

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */

@Grab('com.xlson.groovycsv:groovycsv:1.0')
import com.xlson.groovycsv.CsvParser

// DEBUG
def types = [:]
// DEBUG

def typesMapping = [
    data      : [type: 'varchar', defaultSize: 10],
    date      : [type: 'varchar', defaultSize: 10],
    stringa   : [type: 'varchar', defaultSize: 255],
    varchar   : [type: 'varchar', defaultSize: 255],
    'char'    : [type: 'varchar', defaultSize: 255],
    intero    : [type: 'smallint', defaultSize: 0],
    'smallint': [type: 'smallint', defaultSize: 0],
    'integer' : [type: 'smallint', defaultSize: 0],
    'int'     : [type: 'smallint', defaultSize: 0],
    'byte'    : [type: 'smallint', defaultSize: 0],
    'float'   : [type: 'decimal', defaultSize: 0],
    'decimal' : [type: 'decimal', defaultSize: 0],
    binaria   : [type: 'boolean', defaultSize: 0]
]

def useAlias = true

def csv = new File('data/tracciato-biro-new.csv').withReader('UTF-8') {
  def data = CsvParser.parseCsv(it)

  new File("data/biro-new${useAlias ? '-aliased' : ''}.specs").withWriter('UTF-8') { w ->

    w.writeLine("fields {")

    data.each { l ->

      // DEBUG
      types[l.TYPE?.trim()] = 0
      // DEBUG

      def size = l?.SIZE
      def type = l.TYPE?.trim()

      def tt = typesMapping[type]
      if (tt) {
        type = tt.type
        if (!size) {
          size = tt.defaultSize
        }
      } else {
        println "ERROR: no type for field ${l.FIELD}"
      }

      def label = l.LABEL?.trim()
      def range = l.RANGE?.trim()
      // def note = l.NOTE?.trim()

      def ref = l.REF?.trim()
      def alias = l.ALIAS?.trim()

      w.writeLine("\t// BIRO REF: ${ref} - ${l.DESC}")
      if (range) {
        w.writeLine("\t// RANGE: ${range}")
      }
      /*
      if (note) {
          w.writeLine("\t// NOTE: ${note}")
      }
      */

      def fieldName
      if (alias && useAlias) {
        fieldName = l.ALIAS.trim()
      } else {
        fieldName = l.FIELD.trim().toUpperCase()
      }

      w.writeLine("\t'${fieldName}' {")
      w.writeLine("\t\ttype = \"${type}\"")
      if (size != 0) {
        w.writeLine("\t\tsize = ${size}")
      }
      if (label) {
        w.writeLine("\t\tlabel = \"${label}\"")
      }
      w.writeLine("\t}")
      w.writeLine("")
    }

    w.writeLine("}")
  }
}

// DEBUG
println types.keySet()
// DEBUG
