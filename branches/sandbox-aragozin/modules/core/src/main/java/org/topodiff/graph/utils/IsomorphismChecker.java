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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.topodiff.graph.GraphView;
import org.topodiff.graph.Node;
import org.topodiff.graph.Triple;
import org.topodiff.util.Iterators;

class IsomorphismChecker {

	private GraphView graph1;
	private GraphView graph2;
	
	public IsomorphismChecker(GraphView graph1, GraphView graph2) {
		this.graph1 = graph1;
		this.graph2 = graph2;
	}
	
	public boolean areIsoMorphic() {
		BMapping bmapping = new BMapping(null);
		
		return match(graph1.listStatements(null, null, null), bmapping);
	}
	
	private boolean match(Iterator<Triple> stIt, BMapping bmapping) {
		for(Triple triple: Iterators.from(stIt)) {
			if (!matchTriple(bmapping, triple)) {
				return false;
			}
		}
		return true;
	}

	private boolean matchTriple(BMapping bmapping, Triple triple) {
		List<Triple> matches = new ArrayList<Triple>(); 
		Triple mtriple = bmapping.map(triple);
		Iterators.addAll(matches, graph2.listStatements(mtriple.subject, mtriple.predicate, mtriple.object));
		if (matches.size() == 0) {
			return false;
		}
		else if (matches.size() == 1) {
			Triple ntriple = matches.get(0);
			bmapping.match(triple, ntriple);
			return true;
		}
		else {
			for(Triple ntriple: matches) {
				BMapping child = new BMapping(bmapping);
				child.match(triple, ntriple);
				if (mtriple.subject == null) {
					if (!recursiveCheck(triple.subject, child)) {
						continue;
					}
				}
				if (mtriple.object == null) {
					if (!recursiveCheck(triple.object, child)) {
						continue;
					}
				}
				bmapping.join(child);
				return true;
			}
			return false;
		}
	}

	private boolean recursiveCheck(Node node, BMapping bmapping) {
		for(Triple triple: Iterators.from(graph1.listStatements(node, null, null))) {
			if (!matchTriple(bmapping, triple)) {
				return false;
			}
		}
		for(Triple triple: Iterators.from(graph1.listStatements(null, null, node))) {
			if (!matchTriple(bmapping, triple)) {
				return false;
			}
		}		
		return true;
	}

	private static class BMapping {
		final BMapping parent;
		final Map<Node, Node> nodes = new HashMap<Node, Node>();
		
		public BMapping(BMapping parent) {
			this.parent = parent;
		}
		
		public Triple map(Triple triple) {
			Node subject = triple.subject.isAnon() ? map(triple.subject) :  triple.subject;
			Node predicate = triple.predicate.isAnon() ? map(triple.predicate) :  triple.predicate;
			Node object = triple.object.isAnon() ? map(triple.object) :  triple.object;
			
			return new Triple(subject, predicate, object);
		}
		
		private Node map(Node node) {
			if (node.isAnon()) {
				if (nodes.containsKey(node)) {
					return nodes.get(node);
				}
				else if (parent != null) {
					return parent.map(node);
				}
				else {
					return null;
				}
			}
			else {
				return node;
			}
		}
		
		public void match(Triple t1, Triple t2) {
			if (t1.subject.isAnon()) {
				nodes.put(t1.subject, t2.subject);
			}
			if (t1.predicate.isAnon()) {
				nodes.put(t1.predicate, t2.predicate);
			}
			if (t1.object.isAnon()) {
				nodes.put(t1.object, t2.object);
			}
		}
		
		public void join(BMapping other) {
			nodes.putAll(other.nodes);
		}
	}
}
