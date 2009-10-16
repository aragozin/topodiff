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
package org.topodiff.graph.simple;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.topodiff.graph.GraphView;
import org.topodiff.graph.Node;
import org.topodiff.graph.NodeType;
import org.topodiff.graph.Triple;
import org.topodiff.algo.LexographicNodeComparator;
import org.topodiff.util.Iterators;
import org.topodiff.util.Morph;

public class SimpleGraphView implements GraphView {
	
	private static final Node LOWER_BOUND = new Node(NodeType.LITERAL, "LOWER") {
		public String toString() {
			return "LOWER";
		}
	};
	private static final Node UPPER_BOUND = new Node(NodeType.LITERAL, "UPPER") {
		public String toString() {
			return "UPPER";
		}
	};
	private static final NodeComparator BOUND_COMPARATOR = new NodeComparator();
	private static final Comparator<Triple> SUBJECT_COMPARATOR = new Comparator<Triple>() {
		public int compare(Triple o1, Triple o2) {
			return BOUND_COMPARATOR.compare(o1.subject, o2.subject);
		}
	};
	private static final Comparator<Triple> PREDICATE_COMPARATOR = new Comparator<Triple>() {
		public int compare(Triple o1, Triple o2) {
			return BOUND_COMPARATOR.compare(o1.predicate, o2.predicate);
		}
	};
	private static final Comparator<Triple> OBJECT_COMPARATOR = new Comparator<Triple>() {
		public int compare(Triple o1, Triple o2) {
			return BOUND_COMPARATOR.compare(o1.object, o2.object);
		}
	};
	
	private static final Comparator<Triple> SPO_COMPARATOR = new TripledComparator(
			SUBJECT_COMPARATOR,
			PREDICATE_COMPARATOR,
			OBJECT_COMPARATOR
			);
	private static final Comparator<Triple> OSP_COMPARATOR = new TripledComparator(
			OBJECT_COMPARATOR,
			SUBJECT_COMPARATOR,
			PREDICATE_COMPARATOR
	);
	private static final Comparator<Triple> POS_COMPARATOR = new TripledComparator(
			PREDICATE_COMPARATOR,
			OBJECT_COMPARATOR,
			SUBJECT_COMPARATOR
	);
	
	private SortedMap<Triple, Triple> spo;
	private SortedMap<Triple, Triple> osp;
	private SortedMap<Triple, Triple> pos;
	
	private Map<String, SortedMap<Triple, Triple>> indexMap = new HashMap<String, SortedMap<Triple, Triple>>();
	
	public SimpleGraphView() {
		spo = new TreeMap<Triple, Triple>(SPO_COMPARATOR);
		osp = new TreeMap<Triple, Triple>(OSP_COMPARATOR);
		pos = new TreeMap<Triple, Triple>(POS_COMPARATOR);
		
		indexMap.put("___", spo);
		indexMap.put("S__", spo);
		indexMap.put("__O", osp);
		indexMap.put("_P_", pos);
		indexMap.put("SP_", spo);
		indexMap.put("S_O", osp);
		indexMap.put("_PO", pos);
		indexMap.put("SPO", spo);
	}
	
	public SimpleGraphView(Collection<Triple> triples) {
		this();
		addAll(triples);
	}
	

	public void addAll(Collection<Triple> triples) {
		for(Triple triple: triples) {
			add(triple);
		}
	}

	public void add(Triple triple) {
		if (triple.subject == null || triple.predicate == null || triple.object == null) {
			throw new IllegalArgumentException("Triple is not fully defined");
		}
		spo.put(triple, triple);
		osp.put(triple, triple);
		pos.put(triple, triple);
	}

	public Iterator<Node> listSubjects() {
		
		Morph<Triple, Node> getSubject = new Morph<Triple, Node>() {
			public Node morph(Triple a) {
				return a.subject;
			}
		};
		
		Iterator<Node> subIterator = Iterators.morph(spo.keySet().iterator(), getSubject);
		subIterator = Iterators.sortedUnique(subIterator);
		
		return subIterator;
	}
	
	
	public Iterator<Triple> listStatements(Node subj, Node pred, Node obj) {
		char[] key = new char[3];
		key[0] = subj == null ? '_' : 'S';
		key[1] = pred == null ? '_' : 'P';
		key[2] = obj == null ? '_' : 'O';
		
		Triple lower = new Triple(subj == null ? LOWER_BOUND : subj, pred == null ? LOWER_BOUND : pred, obj == null ?  LOWER_BOUND : obj);
		Triple upper = new Triple(subj == null ? UPPER_BOUND : subj, pred == null ? UPPER_BOUND : pred, obj == null ?  UPPER_BOUND : obj);
		
		SortedMap<Triple, Triple> index = indexMap.get(new String(key));
		if (lower.equals(upper)) {
			return Collections.singleton(lower).iterator();
		}
		else {
			return index.subMap(lower, upper).values().iterator();
		}
	}

	private static class NodeComparator extends LexographicNodeComparator {
		
		@Override
		protected int compareOther(int nodeType, Node o1, Node o2) {
			return 0;
		}

		@Override
		protected int getNodeClass(Node n) {
			if (n == LOWER_BOUND) {
				return -1;
			}
			else if (n == UPPER_BOUND) {
				return CLASS_NULL + 10;
			}
			else {
				return super.getNodeClass(n);
			}
		}
	}
	
	private static class TripledComparator implements Comparator<Triple> {

		private Comparator<Triple> c1;
		private Comparator<Triple> c2;
		private Comparator<Triple> c3;
		
		public TripledComparator(Comparator<Triple> c1, Comparator<Triple> c2, Comparator<Triple> c3) {
			this.c1 = c1;
			this.c2 = c2;
			this.c3 = c3;
		}

		public int compare(Triple o1, Triple o2) {
			int res = 0;
			res = c1.compare(o1, o2);
			if (res != 0) {
				return res;
			}
			res = c2.compare(o1, o2);
			if (res != 0) {
				return res;
			}
			res = c3.compare(o1, o2);
			return res;
		}
	}
}
