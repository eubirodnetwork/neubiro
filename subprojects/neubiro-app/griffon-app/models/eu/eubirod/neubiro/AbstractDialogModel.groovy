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

import griffon.transform.Observable
import griffon.util.GriffonNameUtils

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */

abstract class AbstractDialogModel {
  @Observable
  String title
  @Observable
  int width = 0
  @Observable
  int height = 0
  @Observable
  boolean resizable = true
  @Observable
  boolean modal = true

  protected abstract String getDialogKey()

  protected abstract String getDialogTitle()

  void mvcGroupInit(Map<String, Object> args) {
    title = GriffonNameUtils.capitalize(application.messageSource.getMessage('application.dialog.' + dialogKey + '.title'))
  }
}
