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
package org.topodiff.algo;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.topodiff.graph.GraphView;
import org.topodiff.graph.Node;
import org.topodiff.graph.NodeType;
import org.topodiff.graph.Triple;
import org.topodiff.io.TripleReceiver;
import org.topodiff.util.Match;
import org.topodiff.util.text.Encodings;

/**
 * This class writes sorted presentation of model, 
 * b-nodes are sorted by deterministic topological rule, to facilitate
 * delta calculation.
 *  
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ToposortGraphProcessor {

	private final TripleReceiver tripleWriter;
	private final ToposortUtils toolkit = ToposortUtils.INSTANCE;
	private final MessageDigest hashAlgo;
	private final Match<Node> propFollowFilter; 
	
	private final Map<Node, String> bnodeMap = new HashMap<Node, String>();
	private final Map<String, List<Subgraph>> isoSets = new HashMap<String, List<Subgraph>>();
	
	private final Set<Triple> thirdKindStatements = new HashSet<Triple>();
	
	private int danglingNodeCounter = 0;
	private boolean closed = false;
	
	public ToposortGraphProcessor(TripleReceiver tripleWriter, Match<Node> propFollowFilter, MessageDigest hashAlgo) {
		this.tripleWriter = tripleWriter;
		this.propFollowFilter = propFollowFilter;
		this.hashAlgo = hashAlgo;
	}
	
	public void process(GraphView model) {

		if (closed) {
			throw new IllegalArgumentException();
		}
		
		prepareBlanks(model);
		
		writeBlanks(model);
		
		writeNamed(model);
		
		writeThirdKind(model);
		
		closed = true;		
	}

	private void writeBlanks(GraphView model) {
		Map<String, Node> map = new TreeMap<String, Node>();
		for(Map.Entry<Node, String> entry: bnodeMap.entrySet()) {
			Node node = entry.getKey();
			String id = entry.getValue();
			map.put(id, node);
		}
		writerNodes(model, map.values(), propFollowFilter);
	}

	private void writeNamed(GraphView model) {
		Map<String, Node> map = new TreeMap<String, Node>();
		Iterator<Node> rI = model.listSubjects();
		while(rI.hasNext()) {
			Node res = rI.next();
			if (!res.isAnon()) {
				map.put(res.getLexicalForm(), res);
			}
		}				
		writerNodes(model, map.values(), null);
	}

	private void writeThirdKind(GraphView model) {
		List<Triple> triples = new ArrayList<Triple>(thirdKindStatements);
		Collections.sort(triples, new TripleComparator());
		
		for(Triple stmt: triples) {
			writeTriple(model, stmt);
		}
	}

	private void writerNodes(GraphView model, Collection<Node> values, Match<Node> propFollowFilter) {
		Comparator<Triple> cmp = new TripleComparator();
		
		for(Node subject: values) {
			Iterator<Triple> it = model.listStatements(subject, null, null);
			List<Triple> troples = new ArrayList<Triple>();
			while(it.hasNext()) {
				Triple stmt = it.next();
				if (!thirdKindStatements.contains(stmt)) {
					troples.add(stmt);
				}
			}
			Collections.sort(troples, cmp);
			
			for(Triple stmt: troples) {
				writeTriple(model, stmt);
			}
		}	
	}
	
	private void prepareBlanks(GraphView model) {
		Set<Node> blanks = new HashSet<Node>();
		
		Iterator<Node> iter = model.listSubjects();
		while(iter.hasNext()) {
			Node res = iter.next();
			if (res.isAnon()) {
				blanks.add(res);
			}
		}
		
		while(!blanks.isEmpty()) {
			Node res = blanks.iterator().next();
			
			Set<Node> todo = new HashSet<Node>();
			todo.add(res);
			Set<Node> done = new HashSet<Node>();
			Set<Triple> subgraph = new HashSet<Triple>();
			
			collectSubgraph(model, todo, done, subgraph);
			blanks.removeAll(done);
			
			if (subgraph.size() > 0) {
				addSubgraph(toolkit.sort(subgraph));
			}
			else {
				// we cannot find any topological identification for this node it will be written as b-node
			}
		}

		// renaming
		List<String> hashes = new ArrayList<String>(isoSets.keySet());
		Collections.sort(hashes);
		
		for(String hash: hashes) {
			List<Subgraph> isoList = isoSets.get(hash);

			for(int isGraphNo = 0; isGraphNo != isoList.size(); ++isGraphNo) {
				Subgraph graph = isoList.get(isGraphNo);
				numberBlanks(graph.triples, hash, isGraphNo);
			}
		}
		
		isoSets.clear();
	}
	
	private void numberBlanks(List<Triple> triples, String hash, int isGraphNo) {
		int n = 0;
		for(Triple triple: triples) {
			if (triple.subject.isAnon() && !bnodeMap.containsKey(triple.subject)) {
				String id = "b_" + hash + "x" + isGraphNo +"_n" + (n++);
				bnodeMap.put(triple.subject, id);
			}
			if (triple.object.isAnon() && !bnodeMap.containsKey(triple.object)) {
				String id = "b_" + hash + "x" + isGraphNo +"_n" + (n++);
				bnodeMap.put(triple.object, id);
			}
		}
	}

	private void writeTriple(GraphView model, Triple stmt) {
		Node s = stmt.subject;
		Node p = stmt.predicate;
		Node o = stmt.object;
		
		if (s.isAnon()) {
			String id = bnodeMap.get(s);
			if (id == null) {
				id = mapDanglingNode(s);
			}
			
			s = new Node(NodeType.BLANK, id, null);
		}
		
		if (o.isAnon()) {
			String id = bnodeMap.get(o);
			if (id == null) {
				id = mapDanglingNode(o);
			}
			
			o = new Node(NodeType.BLANK, id, null);
		}
		
		tripleWriter.receive(new Triple(s, p, o));
	}
	
	
	private void addSubgraph(List<Triple> triples) {
		String hash;
		synchronized(hashAlgo) {
			hash = Encodings.bytesToHex(toolkit.getDigestHash(triples, hashAlgo));
		}
		
		List<Subgraph> sets = isoSets.get(hash);
		if (sets == null) {
			isoSets.put(hash, sets = new ArrayList<Subgraph>());
		}
		
		sets.add(new Subgraph(triples));
	}

	private void collectSubgraph(GraphView model, Set<Node> todo, Set<Node> done, Set<Triple> subgraph) {
		while(!todo.isEmpty()) {
			Iterator<Node> it = todo.iterator();
			Node res = it.next();
			it.remove();
			done.add(res);
			
			Iterator<Triple> stIt = model.listStatements(res, null, null);
			while(stIt.hasNext()) {
				Triple stmt = stIt.next();
				Node object = stmt.object;
				if (object.isAnon() && !done.contains(object)) {
					if (propFollowFilter == null || propFollowFilter.match(stmt.predicate)) {
						todo.add(object);
						subgraph.add(stmt);
					}
					else {
						thirdKindStatements.add(stmt);
					}
				}
				else {
					subgraph.add(stmt);
				}
			}

			stIt = model.listStatements(null, null, res);
			while(stIt.hasNext()) {
				Triple stmt = stIt.next();
				Node subj = stmt.subject;
				if (subj.isAnon() && !done.contains(subj)) {
					if (propFollowFilter != null && propFollowFilter.match(stmt.predicate)) {
						todo.add(subj);
						subgraph.add(stmt);
					}
				}
			}
		}
	}
	
	/**
	 * If blank node does not participate as subject in model, it will not be named
	 * during rename phase. Such nodes are named ad hoc.
	 */
	private String mapDanglingNode(Node node) {
		String id = "dangling_" + (danglingNodeCounter++);
		bnodeMap.put(node, id);
		return id;
	}
	
	private int compare(Triple t1, Triple t2) {
		return compare(t1.subject, t1.predicate, t1.object, 
				t2.subject, t2.predicate, t2.object);
	}

	private int compare(Node s1, Node p1, Node o1, Node s2, Node p2, Node o2) {

		int res = compareNodes(s1, s2);
		
		if (res != 0) {
			return res;
		}
		
		res = compareNodes(p1, p2);
		
		if (res != 0) {
			return res;
		}
		
		return compareNodes(o1, o2);
	}
	
	private int compareNodes(Node o1, Node o2) {
		NodeType t1 = o1.nodeType;
		NodeType t2 = o2.nodeType;
		
		if (t1 != t2) {
			return t1.compareTo(t2);
		}
		
		switch (t1) {
				
			case BLANK:
				String i1 = bnodeMap.get(o1);
				if (i1 == null) {
					i1 = mapDanglingNode(o1);
				}
				String i2 = bnodeMap.get(o2);
				if (i2 == null) {
					i2 = mapDanglingNode(o2);
				}
				
				return i1.compareTo(i2);

			case NAMED:
			case LITERAL:
				String s1 = o1.getLexicalForm();
				String s2 = o2.getLexicalForm();
				return s1.compareTo(s2);
				
			default:
				return 0;
		}
	}
	
	private final class TripleComparator implements	Comparator<Triple> {
		public int compare(Triple o1, Triple o2) {
			return ToposortGraphProcessor.this.compare(o1, o2);
		}
	}

	private static class Subgraph {
		
		public final List<Triple> triples;
	
		public Subgraph(List<Triple> triples) {
			this.triples = triples;
		}
	}
}
