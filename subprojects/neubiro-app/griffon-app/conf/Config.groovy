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
application {
  title = 'NeuBiro'
  version = '1.5.1'
  startupGroups = ['neubiro']
  autoShutdown = true
}

mvcGroups {
  // MVC Group for "neubiro"
  'neubiro' {
    model = 'eu.eubirod.neubiro.NeubiroModel'
    view = 'eu.eubirod.neubiro.NeubiroView'
    controller = 'eu.eubirod.neubiro.NeubiroController'
  }

  // MVC Group for "credits"
  'about' {
    model = 'eu.eubirod.neubiro.AboutModel'
    view = 'eu.eubirod.neubiro.AboutView'
    controller = 'eu.eubirod.neubiro.DialogController'
  }

  // MVC Group for "preferences"
  'preferences' {
    model = 'eu.eubirod.neubiro.PreferencesModel'
    view = 'eu.eubirod.neubiro.PreferencesView'
    controller = 'eu.eubirod.neubiro.DialogController'
  }
}
