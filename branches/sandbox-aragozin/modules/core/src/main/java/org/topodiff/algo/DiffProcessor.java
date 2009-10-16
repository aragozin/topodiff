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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topodiff.algo.TripleStreamParser.Mode;
import org.topodiff.graph.Node;
import org.topodiff.graph.Triple;
import org.topodiff.io.ModelDeltaReceiver;
import org.topodiff.util.Iterators;
import org.topodiff.util.Pair;


/**
 * Class for calculation delta between 2 streams of triples.
 * Disposable.
 *  
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class DiffProcessor {

	private final TripleStreamParser source1;
	private final TripleStreamParser source2;
	
	private final ModelDeltaReceiver handler;
	
	private final Map<String, List<IsoGroup>> isoGroupHashMap = new HashMap<String, List<IsoGroup>>();
	
	private final Map<String, IsoGroup> isoPool1 = new HashMap<String, IsoGroup>();
	private final Map<String, IsoGroup> isoPool2 = new HashMap<String, IsoGroup>();
	
	private final AnonDic anonDic = new AnonDic();	
	private int groupCounter = 0;

	public DiffProcessor(Iterator<Triple> reader1, Iterator<Triple> reader2, ModelDeltaReceiver handler) {
		this.source1 = new TripleStreamParser(reader1);
		this.source2 = new TripleStreamParser(reader2);
		this.handler = handler;
		
		Object model1 = source1;
		Object model2 = source2;

		anonDic.setSources(model1, model2);
	}
	
	public void process() {
		
		// stage 1
		source1.setMode(Mode.BLANKS);
		source2.setMode(Mode.BLANKS);
		
		processStage(true);

		// stage 2
		source1.setMode(Mode.NAMED);
		source2.setMode(Mode.NAMED);

		processStage(false);

		// stage 3
		source1.setMode(Mode.BLANKS);
		source2.setMode(Mode.BLANKS);
		
		processStage(false);
		
		
		for(List<IsoGroup> groups: isoGroupHashMap.values()) {
			for(IsoGroup group: groups) {
				scrapGroup(group);
			}
		}
	}

	private void processStage(boolean processIsoGroups) {
		while(source1.getCurrentSubject() != null || source2.getCurrentSubject() != null) {
			Node res1 = source1.getCurrentSubject();
			Node res2 = source2.getCurrentSubject();

			// head to head comparison of sorted triples
			int cmpResult = LexographicNodeComparator.INSTANCE.compare(res1, res2);
						
			// heads match each other
			if (cmpResult == 0) {
				if (processIsoGroups && source1.getCurrectIsomorphicHash() != null) {
					String hash = source1.getCurrectIsomorphicHash();
					Map<String, List<Triple>> g1 = readHashGroup(source1);
					Map<String, List<Triple>> g2 = readHashGroup(source2);
					
					List<IsoGroup> list = createGroups(g1);
					mergeGroups(list, g2);
					
					Iterator<IsoGroup> it = list.iterator();
					while(it.hasNext()) {
						IsoGroup group = it.next();
						if (group.sets1.size() == 1 && group.sets2.size() == 1) {
							// if both iso groups have only one subgraph, we can safely much subgraphs
							bindGroups(group.sets1.values().iterator().next(), group.sets2.values().iterator().next());
							it.remove();
						}
					}
					
					if (!list.isEmpty()) {
						isoGroupHashMap.put(hash, list);
						for(IsoGroup group: list) {
							addToPool(group);
						}
					}
				}
				else {
					List<Triple> g1 = source1.readSubject();
					List<Triple> g2 = source2.readSubject();
					
					match(g1, g2);
				}
			}
			else if (cmpResult < 0 ) {
				// res2 > res1
				List<Triple> g  = source1.readSubject();
				for(Triple t: g) {
					handler.tripleMatch(t, null);
				}
			}
			else {
				// res1 > res2
				List<Triple> g  = source2.readSubject();
				for(Triple t: g) {
					handler.tripleMatch(null, t);
				}
			}
		}
		
		// MAYBE: is this possible?
		while(source1.getCurrentSubject() != null) {
			reportDifferece(source1.readSubject(), 1);
		}

		while(source2.getCurrentSubject() != null) {
			reportDifferece(source2.readSubject(), 2);
		}
	}
	
	private void scrapGroup(IsoGroup group) {
		while(!group.sets1.isEmpty() && !group.sets2.isEmpty()) {
			Map.Entry<String, List<Triple>> e1 = group.sets1.entrySet().iterator().next();
			Map.Entry<String, List<Triple>> e2 = group.sets2.entrySet().iterator().next();
			
			bindGroups(e1.getKey(), e2.getKey());
		}
		
		while(!group.sets1.isEmpty()) {
			Map.Entry<String, List<Triple>> e1 = group.sets1.entrySet().iterator().next();
			group.sets1.remove(e1.getKey());
			isoPool1.remove(e1.getKey());
			
			reportDifferece(e1.getValue(), 1);
		}

		while(!group.sets2.isEmpty()) {
			Map.Entry<String, List<Triple>> e2 = group.sets2.entrySet().iterator().next();
			group.sets2.remove(e2.getKey());
			isoPool2.remove(e2.getKey());
			
			reportDifferece(e2.getValue(), 2);
		}
	}

	private void match(List<Triple> g1, List<Triple> g2) {
		g1 = new ArrayList<Triple>(g1);
		g2 = new ArrayList<Triple>(g2);

		Comparator<Triple> cmp = new TripleComparator(anonDic, anonDic, anonDic);
		
		while(g1.size() > 0 && g2.size() > 0) {
			
			Collections.sort(g1, cmp);
			Collections.sort(g2, cmp);
			
			stripMatching(g1, g2);
			
			if (!matchBlanks(g1, g2)) {
				break;
			}
		}
		
		if (!g1.isEmpty()) {
			reportDifferece(g1, 1);
		}
		if (!g2.isEmpty()) {
			reportDifferece(g2, 2);
		}
	}
	
	private void stripMatching(List<Triple> g1, List<Triple> g2) {
		Comparator<Triple> cmp = new TripleComparator(anonDic, anonDic, anonDic);

		Set<Triple> matched1 = new HashSet<Triple>();
		Set<Triple> matched2 = new HashSet<Triple>();
		
		int n1 = 0;
		int n2 = 0;
		while(n1 < g1.size() && n2 < g2.size()) {
			
			Triple t1 = g1.get(n1);
			Triple t2 = g2.get(n2);
			
			int res = cmp.compare(t1, t2);
			if (res == 0) {
				++n1;
				++n2;
				
				if (isDefined(t1)) {
					if (!isDefined(t2)) {
						throw new RuntimeException("Something wrong, go to bug hunting!");
					}
					matched1.add(t1);
					matched2.add(t2);
					reportMatch(t1, t2);
				}
			}
			else if (res < 0) {
				++n1;
				if (!canBeMatchedFurther(t1, 1)){
					matched1.add(t1);
					reportDifferece(t1, 1);
				}
			}
			else {
				++n2;
				if (!canBeMatchedFurther(t1, 1)){
					matched2.add(t2);
					reportDifferece(t2, 2);
				}
			}
		}
		
		g1.removeAll(matched1);
		g2.removeAll(matched2);
	}

	private boolean matchBlanks(List<Triple> g1, List<Triple> g2) {
		if (g1.isEmpty() || g2.isEmpty()) {
			return false;
		}
		
		if (tryMatchBlanks(g1, g2, false)) {
			return true;
		}

		if (tryMatchBlanks(g1, g2, true)) {
			return true;
		}
		
		return false;
	}
	
	private boolean tryMatchBlanks(List<Triple> g1, List<Triple> g2, boolean ambigousMatch) {
		MatchComparator matchComparator = new MatchComparator();
		TripleComparator tripleMatchComparator = new TripleComparator(matchComparator);
		
		Collections.sort(g1, tripleMatchComparator);
		Collections.sort(g2, tripleMatchComparator);
		
		for(Pair<List<Triple>, List<Triple>> delta: Iterators.sortedDelta(g1, g2, tripleMatchComparator)) {
			if (delta.a.size() == 1 && delta.b.size() == 1) {
				Triple t1 = delta.a.get(0);
				Triple t2 = delta.b.get(0);
				
				// matching triples
				bindTriples(t1, t2);
				return true;
			}
			else if (ambigousMatch && !delta.a.isEmpty() && delta.b.isEmpty()) {
				// try to match any thing
				
				Triple t1 = delta.a.get(0);
				Triple t2 = delta.b.get(0);
				
				bindTriples(t1, t2);
				return true;
			}
			
			// removing unmatchable triples
			if (delta.a.size() == 0) {
				reportDifferece(delta.b, 2);
				g2.removeAll(delta.b);
				return true;
			}

			if (delta.b.size() == 0) {
				reportDifferece(delta.a, 1);
				g1.removeAll(delta.a);
				return true;
			}
		}
		
		return false;
	}

	private void bindTriples(Triple t1, Triple t2) {
		if (t1.subject.isAnon() && (anonDic.isFree1(t1.subject) && anonDic.isFree2(t2.subject))) {
			bindNode(t1.subject, t2.subject);
		}
		if (t1.object.isAnon() && (anonDic.isFree1(t1.object) && anonDic.isFree2(t2.object))) {
			bindNode(t1.object, t2.object);
		}
	}

	private void bindNode(Node node1, Node node2) {
		if (DiffHelper.isDanglingNode(node1)) {
			if (!DiffHelper.isDanglingNode(node2)) {
				throw new RuntimeException("Bug is here!");
			}
			anonDic.map(node1, node2);
		}
		else {
			String ig1 = DiffHelper.getIsomorphicsGroupID(node1);
			String ig2 = DiffHelper.getIsomorphicsGroupID(node2);
			String nn1 = DiffHelper.getClusterNodeNo(node1);
			String nn2 = DiffHelper.getClusterNodeNo(node2);
			
			if (!nn1.equals(nn2)) {
				throw new RuntimeException("Yet another bug unleashed!");
			}
			
			bindGroups(ig1, ig2);
		}
	}

	/**
	 * @return <code>true</code> if it is still possible to match this triple
	 */
	private boolean canBeMatchedFurther(Triple triple, int source) {
		if (triple.subject.isAnon() && anonDic.isFree(triple.subject) 
				&& canBeMatchedFurther(triple.subject, source)) {
			return true;
		}
		if (triple.object.isAnon() && anonDic.isFree(triple.object) 
				&& canBeMatchedFurther(triple.object, source)) {
			return true;
		}

		return false;
	}
	
	private boolean canBeMatchedFurther(Node node, int source) {
		if (DiffHelper.isDanglingNode(node)) {
			return true;
		}
		
		IsoGroup is = (source == 1 ? isoPool1 : isoPool2).get(DiffHelper.getIsomorphicsGroupID(node));
		if (source == 1 && !is.sets2.isEmpty()) {
			return true;
		}
		else if (source == 2 && !is.sets1.isEmpty()) {
			return true;
		}
		
		// no possible matches exists
		return false;
	}
	
	/**
	 * @return <code>true</code> if triple does not contain unidentified b-nodes 
	 */
	private boolean isDefined(Triple triple) {
		return !(triple.subject.isAnon() && anonDic.isFree(triple.subject))
		&& !(triple.object.isAnon() && anonDic.isFree(triple.object));
	}

	private void reportMatch(Triple t1, Triple t2) {
		if (t1.subject.isAnon() && !anonDic.areMatching(t1.subject, t2.subject)) {
			throw new RuntimeException("Should match, something is broken!");
		}
		if (t1.object.isAnon() && !anonDic.areMatching(t1.object, t2.object)) {
			throw new RuntimeException("Should match, something is broken!");
		}

		handler.tripleMatch(t1, t2);
	}
	
	private void reportDifferece(Triple triple, int i) {
		if (i == 1) {
			handler.tripleMatch(triple, null);
		}
		else if (i == 2) {
			handler.tripleMatch(null, triple);
		}
		else {
			throw new IllegalArgumentException("Source id can be 1 or 2");
		}
	}

	private void reportDifferece(List<Triple> triples, int i) {
		for(Triple triple: triples) {
			if (i == 1) {
				handler.tripleMatch(triple, null);
			}
			else if (i == 2) {
				handler.tripleMatch(null, triple);
			}
			else {
				throw new IllegalArgumentException("Source id can be 1 or 2");
			}
		}
	}

	private List<IsoGroup> createGroups(Map<String, List<Triple>> g) {
		List<IsoGroup> groupList = new ArrayList<IsoGroup>();
		
		for(List<Triple> triples: g.values()) {
			putToGroupList(groupList, triples, 1);
		}
		
		return groupList;
	}
	
	private void mergeGroups(List<IsoGroup> groupList, Map<String, List<Triple>> g) {
		for(List<Triple> triples: g.values()) {
			putToGroupList(groupList, triples, 2);
		}
	}

	private void putToGroupList(List<IsoGroup> groupList, List<Triple> triples, int i) {
		String groupId = DiffHelper.getIsomorphicsGroupID(triples.get(0).subject);
		for(IsoGroup group: groupList) {
			List<Triple> groupTriples = group.getGraph();
			if (SortedTripleListComparator.INSTANCE.compare(groupTriples, triples) == 0) {				
				(i == 1 ? group.sets1 : group.sets2).put(groupId, triples);
				return;
			}
		}
		
		IsoGroup group = new IsoGroup(groupCounter++);
		(i == 1 ? group.sets1 : group.sets2).put(groupId, triples);
		groupList.add(group);
	}
	
	private void addToPool(IsoGroup group) {
		for(Map.Entry<String, List<Triple>> graph: group.sets1.entrySet()) {
			isoPool1.put(graph.getKey(), group);
		}
		for(Map.Entry<String, List<Triple>> graph: group.sets2.entrySet()) {
			isoPool2.put(graph.getKey(), group);
		}
	}
	
	private void bindGroups(String ig1, String ig2) {

		IsoGroup g1 = isoPool1.get(ig1);
		IsoGroup g2 = isoPool2.get(ig2);
		
		if (g1 == null || g1 != g2) {
			throw new RuntimeException("Yet another bug unleashed!");
		}
		
		isoPool1.remove(ig1);
		isoPool2.remove(ig2);
		List<Triple> list1 = g1.sets1.remove(ig1);
		List<Triple> list2 = g2.sets2.remove(ig2);
		
		bindGroups(list1, list2);
	}

	private void bindGroups(List<Triple> g1, List<Triple> g2) {
		if (g1.size() != g2.size()) {
			throw new RuntimeException("Something is fishy here");
		}
		
		String ig1 = DiffHelper.getIsomorphicsGroupID(g1.get(0).subject);
		String ig2 = DiffHelper.getIsomorphicsGroupID(g2.get(0).subject);
		
		for(int i = 0; i != g1.size(); ++i) {
			Node res1 = g1.get(i).subject;
			Node res2 = g2.get(i).subject;
			
			if (anonDic.isFree1(res1)) {				
				anonDic.map(res1, res2);
			}
			
			Node obj1 = g1.get(i).object;
			Node obj2 = g2.get(i).object;

			if (obj1.isAnon() && anonDic.isFree1(obj1)) {
				anonDic.map(obj1, obj2);
			}
			
			reportMatch(g1.get(i), g2.get(i));
		}
		
		isoPool1.remove(ig1);
		isoPool2.remove(ig2);
	}
	
	private Map<String, List<Triple>> readHashGroup(TripleStreamParser parser) {
		String hashId = parser.getCurrectIsomorphicHash();
		Map<String, List<Triple>> result = new HashMap<String, List<Triple>>();
		
		while(hashId.equals(parser.getCurrectIsomorphicHash())) {
			String groupId = parser.getCurrectIsomorphicGroup();
			List<Triple> triples = new ArrayList<Triple>();
			while(groupId.equals(parser.getCurrectIsomorphicGroup())) {
				triples.addAll(parser.readSubject());
			}
			result.put(groupId, triples);
		}
		
		return result;
	}
	

	private static class IsoGroup implements Comparable<IsoGroup>{
		
		public final Map<String, List<Triple>> sets1 = new HashMap<String, List<Triple>>();
		public final Map<String, List<Triple>> sets2 = new HashMap<String, List<Triple>>();
		private final int sortId;
		
		public IsoGroup(int sortId) {
			this.sortId = sortId;
		}

		public List<Triple> getGraph() {
			if (sets1.isEmpty()) {
				return sets2.values().iterator().next();
			}
			else {
				return sets1.values().iterator().next();
			}
		}

		public int compareTo(IsoGroup o) {
			return sortId - o.sortId;
		}
	}
	
	/**
	 * compare b-nodes by topohash value 
	 */
	private class MatchComparator implements Comparator<Node> {
		
		public int compare(Node o1, Node o2) {
			
			int result = anonDic.compare(o1, o2);
			
			if (result == 0 && o1.isAnon() && anonDic.isFree(o1)) {
				return compareByHash(o1, o2);
			}
			
			return result;
		}

		private int compareByHash(Node o1, Node o2) {
			String hash1 = DiffHelper.getIsomorphicsHash(o1);				
			String hash2 = DiffHelper.getIsomorphicsHash(o2);
			
			if (hash1 == null) {
				if (!DiffHelper.isDanglingNode(o1)) {
					throw new IllegalArgumentException("Input model1 does not seams to be sorted propertly! (Either dangling or hashed!)");
				}
				hash1 = "dangling";
			}
			if (hash2 == null) {
				if (!DiffHelper.isDanglingNode(o2)) {
					throw new IllegalArgumentException("Input model2 does not seams to be sorted propertly! (Either dangling or hashed!)");
				}
				hash2 = "dangling";
			}
			
			int res = hash1.compareTo(hash2);
			
			if (res != 0) {
				return res;
			}

			if ("dangling".equals(hash1)) {
				// two unmatched dangling nodes are always matchable
				return 0;
			}
			
			// we should include group index in match, otherwise we can match non isomorthic nodes
			
			String nn1 = DiffHelper.getClusterNodeNo(o1);
			String nn2 = DiffHelper.getClusterNodeNo(o2);
			nn1 = nn1 == null ? "" : nn1;
			nn2 = nn2 == null ? "" : nn2;
			
			res = nn1.compareTo(nn2);
			
			if (res != 0) {
				return res;
			}
			
			// now we must check what nodes actually belongs to isomorphic group (not just have same hash)
			String ig1 = DiffHelper.getIsomorphicsGroupID(o1);
			String ig2 = DiffHelper.getIsomorphicsGroupID(o2);
			
			IsoGroup group1 = isoPool1.get(ig1);
			IsoGroup group2 = isoPool2.get(ig2);
			if (group1 == null || group2 == null) {
				throw new IllegalArgumentException("Input format is incorrect! If node is unbinded it should have group!");
			}

			return group1.compareTo(group2);
		}
	}
	
	private static class AnonDic extends LexographicNodeComparator {
		private final Map<Node, Integer> sortId1 = new HashMap<Node, Integer>();
		private final Map<Node, Integer> sortId2 = new HashMap<Node, Integer>();
		private final Map<Node, Node> map1to2 = new HashMap<Node, Node>();
		private final Map<Node, Node> map2to1 = new HashMap<Node, Node>();
		
		private Object source1 = null;
		private Object source2 = null;

		public void setSources(Object source1, Object source2) {
			this.source1 = source1;
			this.source2 = source2;
		}
		
		public void map(Node r1, Node r2) {
			if (r1.scope != source1 || r2.scope != source2) {
				throw new RuntimeException("Arguments missplaced!");
			}
			
			Integer sortId = Integer.valueOf(map1to2.size());
			
			if (map1to2.put(r1, r2) != null) {
				throw new RuntimeException("Rebound " + r1);
			}
				 
			if (map2to1.put(r2, r1) != null) {
				throw new RuntimeException("Rebound " + r2);
			}
			
			sortId1.put(r1, sortId);
			sortId2.put(r2, sortId);
		}

		public boolean isFree(Node r) {
			return (source1 == r.scope && isFree1(r)) || (source2 == r.scope && isFree2(r));
		}

		public boolean isFree1(Node r1) {
			if (source1 != null && r1.scope != source1) {
				throw new RuntimeException("Assertion failed!");
			}
			return !map1to2.containsKey(r1);
		}

		public boolean isFree2(Node r2) {
			if (source2 != null && r2.scope != source2) {
				throw new RuntimeException("Assertion failed!");
			}
			return !map2to1.containsKey(r2);
		}
		
		public boolean areMatching(Node r1, Node r2) {
			if (source1 != null && (r1.scope != source1 || r2.scope != source2)) {
				throw new RuntimeException("Assertion failed!");
			}

			return r2.equals(map1to2.get(r1));
		}
		
		private Integer getSortId(Node node) {
			if (source1 == node.scope) {
				return sortId1.get(node);
			}
			else if (source2 == node.scope) {
				return sortId2.get(node);
			}

			return null; 
		}

		@Override
		protected int compareAnon(Node o1, Node o2) {
			Integer i1 = o1.isAnon() ? getSortId((Node) o1) : null;
			Integer i2 = o2.isAnon() ? getSortId((Node) o2) : null;

			// all unmatched nodes treated as equals and greater than any defined
			if (i1 == null) {
				i1 = Integer.valueOf(map1to2.size() + 1000); 
			}
			
			if (i2 == null) {
				i2 = Integer.valueOf(map1to2.size() + 1000);
			}
			
			return i1.compareTo(i2);
		}
	}
}
