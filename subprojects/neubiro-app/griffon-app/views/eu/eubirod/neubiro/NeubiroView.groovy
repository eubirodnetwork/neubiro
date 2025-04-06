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
import eu.eubirod.neubiro.utils.AlphanumComparator

import javax.swing.BoxLayout
import java.awt.BorderLayout
import javax.swing.SwingConstants
import java.awt.GridBagConstraints

import static griffon.util.GriffonApplicationUtils.isMacOSX

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */

@ArtifactProviderFor(GriffonView)
class NeubiroView {
  FactoryBuilderSupport builder
  NeubiroModel model

  void initUI() {
    builder.with {

      build(NeubiroActions)

      def fileChooserWindow = fileChooser()

      AlphanumComparator alfaComparator = new AlphanumComparator()

      application(id: 'mainWindow',
        title: 'NeuBiro',
        preferredSize: [800, 600],
        pack: true,

        //location: [50,50],
        locationByPlatform: true,

        iconImage: imageIcon('/neubiro-icon-48x48.png').image,
        iconImages: [
          imageIcon('/neubiro-icon-48x48.png').image,
          imageIcon('/neubiro-icon-32x32.png').image,
          imageIcon('/neubiro-icon-16x16.png').image
        ]) {

        build(NeubiroMenuBar)

        migLayout(layoutConstraints: 'fill')

        // Content area
        tabbedPane(constraints: 'center, grow') {

          build(NeubiroConfigView)
          build(NeubiroImportView)
          build(NeubiroIndicatorsView)
          build(NeubiroTransferView)

          // Log pane
          panel(name: application.messageSource.getMessage('application.tab.log.name'), constraints: BorderLayout.CENTER) {
            borderLayout()
            panel(border: titledBorder(title: "Log", constraints: CENTER)) {
              boxLayout(axis: BoxLayout.Y_AXIS)
              scrollPane() {
                textPane(id: 'logArea', editable: false)
              }
            }

            hbox(constraints: SOUTH) {
              button(clearLogAction)
            }
          }
        }

        // Status bar
        vbox(constraints: 'south, grow') {
          separator()
          panel {
            gridBagLayout()
            label(id: 'status', text: bind { model.status },
              constraints: gbc(weightx: 1.0,
                anchor: GridBagConstraints.WEST,
                fill: GridBagConstraints.HORIZONTAL,
                insets: [1, 3, 1, 3])
            )
            separator(orientation: SwingConstants.VERTICAL, constraints: gbc(fill: GridBagConstraints.VERTICAL))
            progressBar id: 'progressBar', value: bind { model.current }, indeterminate: false,
              minimum: bind { model.minProgressBar }, maximum: bind { model.maxProgressBar }
            constraints:
            gbc(insets: [1, 3, 1, 3])
          }
        }
      }
    }
  }
}
