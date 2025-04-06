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

import groovy.util.logging.Slf4j
import org.codehaus.griffon.runtime.swing.DefaultSwingWindowDisplayHandler

import javax.annotation.Nonnull
import java.awt.Window

import static griffon.swing.support.SwingUtils.centerOnScreen

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */
@Slf4j
class CenteringWindowDisplayHandler extends DefaultSwingWindowDisplayHandler {
  @Override
  void show(@Nonnull String name, @Nonnull Window window) {
    if (name == 'mainWindow') {
      centerOnScreen(window)
    }
    super.show(name, window)
  }
}
