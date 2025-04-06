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

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */

class ImportEnhancerCategory {
  static String lookup(String self, String table, String lookupField, String resultField) {
    LookupsHelper.instance.get(self, table, lookupField, resultField)
  }

  static List lookup(String self, String table, String lookupField, List<String> resultFields) {
    LookupsHelper.instance.get(self, table, lookupField, resultFields)
  }

  static int ageInDaysFrom(Date self, Date toDate) {
    ImportEnhancerFunctions.ageInDaysFrom(self, toDate)
  }

  static int ageInYearsFrom(Date self, Date toDate) {
    ImportEnhancerFunctions.ageInYearsFrom(self, toDate)
  }
}
