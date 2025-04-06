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
package eu.eubirod.neubiro.db

import groovy.sql.Sql
import groovy.util.logging.*

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */

@Slf4j
@Singleton
class LookupsHelper {
  private Sql sql
  private Map cache = [:]

  void setDb(Sql db) {
    this.sql = db
  }

  void cleanup() {
    // Clean cache
    cache.clear()
  }

  String get(String value, String table, String lookupField, String resultField) {
    def result = null
    def key = "${table}-${lookupField}-${value}"
    if (cache.containsKey(key)) {
      result = cache[key]
    } else {
      def stmt = "select ${resultField} from ${table} where ${lookupField} = ?"
      def row = sql.firstRow(stmt, value)
      result = row ? "${row[resultField]}" : null
      cache[key] = result
    }
    result
  }

  List get(String value, String table, String lookupField, List<String> resultFields) {
    def result = null
    def key = "${table}-${lookupField}-${value}"
    if (cache.containsKey(key)) {
      result = cache[key]
    } else {
      def stmt = "select ${resultFields.join(',')} from ${table} where ${lookupField} = ?"
      def rows = sql.rows(stmt, value)
      result = rows
      cache[key] = result
    }
    result
  }

}
