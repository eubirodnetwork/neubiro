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
import eu.eubirod.neubiro.config.NeubiroConfigDefault
import griffon.core.event.EventHandler
import griffon.core.injection.Module
import griffon.inject.DependsOn
import griffon.swing.SwingWindowDisplayHandler
import org.codehaus.griffon.runtime.core.injection.AbstractModule
import static griffon.util.AnnotationUtils.named
import org.kordamp.jipsy.ServiceProviderFor

@DependsOn('swing')
@ServiceProviderFor(Module)
class ApplicationModule extends AbstractModule {
  @Override
  protected void doConfigure() {
    bind(NeubiroConfig)
      .to(NeubiroConfigDefault)
      .asSingleton()

    bind(EventHandler)
      .to(ApplicationEventHandler)
      .asSingleton()

    bind(SwingWindowDisplayHandler)
      .withClassifier(named('defaultWindowDisplayHandler'))
      .to(CenteringWindowDisplayHandler)
      .asSingleton()
  }
}
