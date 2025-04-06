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

import groovy.util.logging.Slf4j
import org.yaml.snakeyaml.Yaml

import javax.swing.JFileChooser
import javax.swing.JFrame
import java.nio.file.Path
import java.nio.file.Paths

import static griffon.util.GriffonNameUtils.isBlank

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */

@Slf4j
class NeubiroConfigDefault implements NeubiroConfig {

  private Map config = [:]

  @Override
  void loadIfNeeded() {
    if (config.isEmpty()) {
      load()
    }
  }

  @Override
  void load() {
    this.config = [:]

    try {
      File basePath = getHomeDirectory()
      File f = new File(basePath, CONFIG_FILENAME)
      if (f.exists()) {
        log.debug "Reading external configuration from ${f.absoluteFile}"
        f.withReader { r ->
          Yaml yaml = new Yaml()
          this.config = yaml.load(r)
        }
      }
    }
    catch (Exception e) {
      log.error("Wrong config file format", e)
    }
  }

  @Override
  void save() {
    // Save config preferences
    Yaml yaml = new Yaml()

    File basePath = getHomeDirectory()
    new File(basePath, CONFIG_FILENAME).withWriter { w ->
      yaml.dump(this.config, w)
    }
  }

  @Override
  void set(String key, String value) {
    this.config[key] = value
  }

  @Override
  String get(String key, String defaultValue) {
    this.config[key] ?: defaultValue
  }

  @Override
  String get(String key) {
    this.config[key]
  }

  @Override
  File selectFileOrDir(JFrame startingWindow, String locationPrefs, int selectionMode = JFileChooser.FILES_ONLY, String name = null, fileFilter = null) {
    if (isBlank(name)) {
      name = 'Open'
    }

    File location = new File(get(locationPrefs, '.'))

    JFileChooser fc = new JFileChooser(location)
    fc.fileSelectionMode = selectionMode
    fc.acceptAllFileFilterUsed = false
    fc.dialogTitle = name

    if (fileFilter) {
      fc.setFileFilter(fileFilter)
    }

    if (fc.showOpenDialog(startingWindow) == JFileChooser.APPROVE_OPTION) {
      this.config[locationPrefs] = fc.currentDirectory.path
      return fc.selectedFile
    }
    return null
  }

  @Override
  File getHomeDirectory() {
    String userDir = System.getProperty('user.home')
    Path currentRelativePath = Paths.get(userDir, 'neubiro')
    File wd = currentRelativePath.toAbsolutePath().toFile()
    if (!wd.exists()) {
      wd.mkdirs()
    }
    return wd
  }
}
