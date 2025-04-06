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

import griffon.core.artifact.GriffonView
import griffon.metadata.ArtifactProviderFor
import griffon.util.GriffonNameUtils
import groovy.util.logging.Slf4j
import java.awt.Font

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */
@Slf4j
@ArtifactProviderFor(GriffonView)
class AboutView {
  FactoryBuilderSupport builder
  AboutModel model
  def content

  void initUI() {
    content = builder.with {
      //def rowConstraints = "center, span 1, wrap".toString()
      def rowConstraints = "center, wrap".toString()

      panel(id: 'content') {
        migLayout layoutConstraints: 'fill'

        label(icon: imageIcon('/neubiro-icon-128x128.png'), constraints: rowConstraints)

        label(GriffonNameUtils.capitalize(application.messageSource.getMessage('application.title')) +
          ' ' + application.configuration['application.version'],
          font: current.font.deriveFont(Font.BOLD),
          constraints: rowConstraints)

        label(text: bind { model.description }, constraints: rowConstraints)

        scrollPane(preferredSize: [420, 80], constraints: rowConstraints) {
          table {
            tableFormat = defaultTableFormat(columnNames: ['Name', 'Task', 'Language'])
            eventTableModel(source: model.authors, format: tableFormat)
            installTableComparatorChooser(source: model.authors)
          }
        }

        keyStrokeAction(component: current,
          keyStroke: 'ESCAPE',
          condition: 'in focused window',
          action: hideAction)
      }
    }
  }
}
