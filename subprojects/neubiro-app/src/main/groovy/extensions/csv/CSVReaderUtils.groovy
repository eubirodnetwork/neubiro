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
package extensions.csv

/*
 * Copyright 2010-2013 Les Hazlewood
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */

import au.com.bytecode.opencsv.CSVParser
import au.com.bytecode.opencsv.CSVReader
import com.google.util.io.base.UnicodeReader

import java.nio.charset.Charset

/**
 * Utility class for adding CSV parsing capability to core Java/Groovy classes (String, File, InputStream, Reader).
 *
 * @since 0.1
 * @author Les Hazlewood
 */
class CSVReaderUtils {
  static void eachLineCsv(CSVReader csvReader, Closure c) {
    try {
      String[] tokens = csvReader.readNext()
      while (tokens) {
        c(tokens);
        tokens = csvReader.readNext();
      }
    } finally {
      csvReader.close()
    }
  }

  static void eachLineCsv(File file, Closure c) {
    eachLineCsv(toCsvReader(file, null), c);
  }

  static void eachLineCsv(InputStream is, Closure c) {
    eachLineCsv(toCsvReader(is, null), c);
  }

  static void eachLineCsv(Reader r, Closure c) {
    eachLineCsv(toCsvReader(r, null), c);
  }

  static void eachLineCsv(String csv, Closure c) {
    eachLineCsv(toCsvReader(csv, null), c);
  }

  static CSVReader toCsvReader(File file, Map settingsMap) {
    return toCsvReader(new FileReader(file), settingsMap)
  }

  static CSVReader toCsvReader(InputStream is, Map settingsMap) {
    Charset charset = settingsMap?.get('charset') as Charset
    Reader reader;
    if (charset) {
      reader = new UnicodeReader(is, charset.toString())
    } else {
      reader = new UnicodeReader(is, "UTF-8")
    }
    return toCsvReader(reader, settingsMap);
  }

  static CSVReader toCsvReader(Reader r, Map settingsMap) {
    char separatorChar = (settingsMap?.get('separatorChar') ?: CSVParser.DEFAULT_SEPARATOR) as char
    char quoteChar = (settingsMap?.get('quoteChar') ?: CSVParser.DEFAULT_QUOTE_CHARACTER) as char
    char escapeChar = (settingsMap?.get('escapeChar') ?: CSVParser.DEFAULT_ESCAPE_CHARACTER) as char
    int skipLines = Math.max(0, (settingsMap?.get('skipLines') ?: 0) as int)

    boolean strictQuotes = CSVParser.DEFAULT_STRICT_QUOTES
    def mapValue = settingsMap?.get('strictQuotes')
    if (mapValue != null) {
      strictQuotes = mapValue as boolean
    }

    boolean ignoreLeadingWhiteSpace = CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE
    mapValue = settingsMap?.get('ignoreLeadingWhiteSpace')
    if (mapValue != null) {
      ignoreLeadingWhiteSpace = mapValue as boolean
    }

    return new CSVReader(r, separatorChar, quoteChar, escapeChar, skipLines, strictQuotes, ignoreLeadingWhiteSpace)
  }

  static CSVReader toCsvReader(String s, Map settingsMap) {
    return toCsvReader(new StringReader(s), settingsMap)
  }
}
