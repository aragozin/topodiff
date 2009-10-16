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
package org.topodiff.util.graph;

import java.util.Iterator;

import org.topodiff.graph.GraphView;
import org.topodiff.graph.Node;
import org.topodiff.graph.Triple;
import org.topodiff.util.Iterators;
import org.topodiff.util.Match;

/**
 * {@link GraphView} filter, using statement based filtering.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class FilteredGraphView implements GraphView {

	private final GraphView view;
	private final Match<Triple> filter;
	private final Match<Node> resourceFilter; 
	
	public FilteredGraphView(GraphView view, Match<Triple> filter) {
		this.view = view;
		this.filter = filter;
		this.resourceFilter = new ResFilter();
	}

	public Iterator<Node> listSubjects() {
		return Iterators.filter(view.listSubjects(), resourceFilter);
	}

	public Iterator<Triple> listStatements(Node res, Node prop, Node obj) {
		return Iterators.filter(view.listStatements(res, prop, obj), filter);
	}
	
	private class ResFilter implements Match<Node> {
		public boolean match(Node node) {
			return listStatements(node, null, null).hasNext() || listStatements(null, null, node).hasNext();
		}
	}
}
