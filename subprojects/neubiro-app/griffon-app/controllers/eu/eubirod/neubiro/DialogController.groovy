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

import griffon.core.artifact.GriffonController
import griffon.metadata.ArtifactProviderFor
import griffon.transform.Threading
import groovy.util.logging.Slf4j
import java.awt.Window

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */
@Slf4j
@ArtifactProviderFor(GriffonController)
class DialogController {
  def model
  def builder
  protected dialog

  @Threading(Threading.Policy.INSIDE_UITHREAD_SYNC)
  void show(Window window = null) {
    window = window ?: Window.windows.find { it.focused }
    if (!dialog || dialog.owner != window) {
      dialog = builder.dialog(
        owner: window,
        title: model.title,
        resizable: model.resizable,
        modal: model.modal) {
        container(builder.content)
      }
      if (model.width > 0 && model.height > 0) {
        dialog.preferredSize = [model.width, model.height]
      }
      dialog.pack()
    }
    int x = window.x + (window.width - dialog.width) / 2
    int y = window.y + (window.height - dialog.height) / 2
    dialog.setLocation(x, y)
    dialog.visible = true
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_SYNC)
  void hide(evt) {
    dialog?.visible = false
    dialog?.dispose()
    dialog = null
  }
}
