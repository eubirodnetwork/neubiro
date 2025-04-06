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

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.SortedList
import griffon.core.artifact.GriffonModel
import griffon.metadata.ArtifactProviderFor
import griffon.transform.Observable

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */
@ArtifactProviderFor(GriffonModel)
class AboutModel extends AbstractDialogModel {
  EventList authors = new SortedList(new BasicEventList(),
    { a, b -> a.name <=> b.name } as Comparator)

  @Observable
  String description

  protected String getDialogKey() { 'about' }

  protected String getDialogTitle() { 'About' }

  void mvcGroupInit(Map<String, Object> args) {
    super.mvcGroupInit(args)
    resizable = false

    description = application.messageSource.getMessage('application.description')

    List tmp = []
    tmp << [name: 'Stefano Gualdi <stefano.gualdi@gmail.com>', task: 'Software Architecture and Development', language: 'Java, Groovy']
    tmp << [name: 'Fabrizio Carinci <fabcarinci@gmail.com>', task: 'Statistical Analysis and Reporting', language: 'R']

    authors.addAll(tmp)
  }
}
