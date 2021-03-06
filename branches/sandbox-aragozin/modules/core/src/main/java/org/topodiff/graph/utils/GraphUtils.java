/**
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.topodiff.graph.utils;

import org.topodiff.graph.GraphView;

public class GraphUtils {
	public static boolean areIsomorthic(GraphView graph1, GraphView graph2) {
		IsomorphismChecker checker = new IsomorphismChecker(graph1, graph2);
		return checker.areIsoMorphic();
	}
}
