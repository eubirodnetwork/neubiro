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
package eu.eubirod.neubiro.utils

import groovy.util.logging.Slf4j
import static griffon.util.GriffonApplicationUtils.isWindows

@Slf4j
final class NeubiroUtils {

  static Map splitFilename(String fileName) {
    def idx = fileName.lastIndexOf(".")
    def name = fileName
    def ext = ""
    if (idx > 0) {
      name = fileName[0..idx - 1]
      if (fileName.length() > idx + 1) {
        ext = fileName[idx + 1..-1]
      }
    }
    return [name: name, ext: ext]
  }

  static String convertToPlatformSpecificPath(String path) {
    if (isWindows) {
      return path.replaceAll("\\\\", "\\\\\\\\");
    }
    return path
  }

  // @see: http://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
  static Integer versionCompare(String str1, String str2) {
    String[] vals1 = str1.split("\\.");
    String[] vals2 = str2.split("\\.");
    int i = 0;
    // set index to first non-equal ordinal or length of shortest version string
    while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
      i++;
    }
    // compare first non-equal ordinal number
    if (i < vals1.length && i < vals2.length) {
      int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
      return Integer.signum(diff);
    }
    // the strings are equal or one string is a substring of the other
    // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
    else {
      return Integer.signum(vals1.length - vals2.length);
    }
  }
}
