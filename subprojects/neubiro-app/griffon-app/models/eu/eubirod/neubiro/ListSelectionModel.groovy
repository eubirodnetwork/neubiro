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

import ca.odell.glazedlists.event.ListEvent
import ca.odell.glazedlists.event.ListEventListener
import griffon.core.artifact.GriffonModel
import griffon.metadata.ArtifactProviderFor

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */
@ArtifactProviderFor(GriffonModel)
class ListSelectionModel implements ListEventListener {
  // @see: http://griffon-user.3225736.n2.nabble.com/GlazedLists-Table-selection-td6622950.html
  def selection = []

  void listChanged(ListEvent eListEvent) {
    selection = eListEvent.sourceList
  }
}
