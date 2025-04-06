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
package eu.eubirod.neubiro.stat

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */

/**
 // Define indicators
 def indicators = [
 'sample': [],
 '1.0': ['2.2'],
 '1.1': [],
 '1.2': [],
 '1.3': ['1.1'],
 '2.1': ['1.1'],
 '2.2': ['2.1'],
 '2.3': ['1.2', '1.0']
 ]

 // resolve execution order
 def resolver = new DependencyResolver(indicators)
 println resolver.resolve()

 */
class DependencyResolver {

  Map<String, Node> nodes = [:]

  DependencyResolver(Map<String, List<String>> indicators) {
    addIndicators(indicators)
  }

  void addIndicator(String id) {
    nodes[id] = new Node(id: id)
  }

  void addDependency(String id, List<String> dependencies) {
    if (dependencies) {
      dependencies.each { n ->
        nodes[id].addEdge(nodes[n])
      }
    }
  }

  void addIndicators(Map<String, List<String>> indicators) {
    indicators.each { k, v ->
      addIndicator(k)
    }
    indicators.each { k, v ->
      if (v) {
        v.each { n ->
          addDependency(k, v)
        }
      }
    }
  }

  List<String> resolve() {
    List<Node> resolution = []
    nodes.each { k, v ->
      resolution << resolve(nodes[k])
    }
    resolution.flatten().unique().id
  }

  List<Node> resolve(Node node) {
    def resolved = []
    resolveImpl(node, resolved, [])

    resolved
  }

  void resolveImpl(Node node, List<Node> resolved, List<Node> unresolved) {
    unresolved << node
    for (edge in node.edges) {
      if (!(edge in resolved)) {
        if (edge in unresolved) {
          throw new CircularDependencyException("Circular dependency error: ${node.toString()} -> ${edge.toString()}")
        }
        resolveImpl(edge, resolved, unresolved)
      }
    }
    resolved << node
    unresolved.remove(node)
  }

  class Node {
    String id
    List<Node> edges = []

    def addEdge(Node node) {
      edges << node
    }

    String toString() {
      "${id}"
    }
  }
}
