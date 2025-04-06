/*
 * Copyright 2014-2025 Stefano Gualdi, EUBIROD network.
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

package eu.eubirod.neubiro.config

import eu.eubirod.neubiro.utils.ExtensionFileFilter

import javax.swing.JFrame

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */

interface NeubiroConfig {

  static final String CONFIG_FILENAME = 'neubiro.yml'

  static final String DEFAULT_OPERATOR_NAME = ''
  static final String DEFAULT_YEAR = ''
  static final String DEFAULT_MAIN_LANGUAGE = 'en'
  static final String DEFAULT_ENGINE_TYPE = 'local'
  static final String DEFAULT_LANGUAGE = 'en'
  static final String DEFAULT_REFERENCE_TYPE = 'none'
  static final String DEFAULT_SERVER = ''
  static final String DEFAULT_USERNAME = ''
  static final String DEFAULT_INDICATORS_DIR = ''
  static final String DEFAULT_WORK_DIR = ''
  static final String DEFAULT_CSV_FILE = ''
  static final String DEFAULT_SPECS_FILE = ''

  static final ExtensionFileFilter ALL_FILES_FILTER = new ExtensionFileFilter("All files", "")
  static final ExtensionFileFilter CSV_FILES_FILTER = new ExtensionFileFilter("CSV files", "csv")
  static final ExtensionFileFilter SPECS_FILES_FILTER = new ExtensionFileFilter("SPECS files", "specs")
  static final ExtensionFileFilter ZIP_FILES_FILTER = new ExtensionFileFilter("ZIP files", "zip")

  static final String INDICATOR_SPECS_FILE = 'indicator.specs'
  static final String INDICATOR_SOURCE_FILE = 'indicator.r'
  static final String INDICATORS_SELECTUNIT_FILE = 'selectUnit.specs'

  static final String R_UNIX_SCRIPT = 'Rscript'
  static final String R_WINDOWS_SCRIPT = 'Rscript.exe'

  void loadIfNeeded()
  void load()
  void save()

  void set(String key, String value)
  String get(String key)
  String get(String key, String defaultValue)

  File selectFileOrDir(JFrame startingWindow, String locationPrefs, int selectionMode, String name, fileFilter)

  File getHomeDirectory()
}
