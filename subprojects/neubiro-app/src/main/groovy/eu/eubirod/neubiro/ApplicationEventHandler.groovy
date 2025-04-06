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

import eu.eubirod.neubiro.config.NeubiroConfig
import griffon.core.GriffonApplication
import griffon.core.event.EventHandler
import groovy.util.logging.Slf4j

import javax.inject.Inject

@Slf4j
class ApplicationEventHandler implements EventHandler {

  @Inject
  NeubiroConfig neubiroConfig

  void onBootstrapStart(GriffonApplication application) {
    String[] args = application.getStartupArgs()
    if (args.length > 0) {
      application.setLocaleAsString(args[0])
    } else {
      neubiroConfig.loadIfNeeded()
      application.setLocaleAsString(neubiroConfig.get("mainLanguage", NeubiroConfig.DEFAULT_MAIN_LANGUAGE))
    }
  }
}
