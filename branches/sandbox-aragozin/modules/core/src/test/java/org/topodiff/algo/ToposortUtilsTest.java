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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.topodiff.graph.GraphView;
import org.topodiff.graph.Node;
import org.topodiff.graph.NodeType;
import org.topodiff.graph.Triple;
import org.topodiff.algo.LexographicNodeComparator;
import org.topodiff.algo.ToposortUtils;
import org.topodiff.graph.simple.SimpleGraphView;
import org.topodiff.util.Iterators;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com
 */
public class ToposortUtilsTest{
	
	public void assertEquals(List<Triple> list1, List<Triple> list2) {
		ToposortUtils toolkit = new ToposortUtils();
		Assert.assertEquals(toolkit.getDigest(list1), toolkit.getDigest(list2));
	}
	
	public List<List<Triple>> getAllCombinations(Set<Triple> triples) {
		List<List<Triple>> result = new ArrayList<List<Triple>>();
		result.add(new ArrayList<Triple>());
		
		for(int i = 0; i != triples.size(); ++i) {
			List<List<Triple>> nextResult = new ArrayList<List<Triple>>();
			for(List<Triple> preface: result) {
				for(Triple triple: triples) {
					if (!preface.contains(triple)) {
						List<Triple> newItem = new ArrayList<Triple>(preface.size() + 1);
						newItem.addAll(preface);
						newItem.add(triple);
						
						nextResult.add(newItem);
					}
				}
			}
			result = nextResult;
		}
		
		return result;
	}
	
	public void testSortIsomorphism(GraphView model) {
		List<Triple> triples = toTriples(model);
		TripleComparator cmp = new TripleComparator(LexographicNodeComparator.INSTANCE,	LexographicNodeComparator.INSTANCE, LexographicNodeComparator.INSTANCE);
		
		Collections.sort(triples, cmp);
		
		List<List<Triple>> testCases = getAllCombinations(new HashSet<Triple>(triples));

		ToposortUtils toolkit = new ToposortUtils();
		List<Triple> sortedTriples = toolkit.sort(triples);
		
		for(List<Triple> testCase: testCases) {
			List<Triple> sortedCase = toolkit.sort(testCase);
			
			try {
				assertEquals(sortedTriples, sortedCase);
			}
			catch(AssertionError error) {
				System.out.println("Failed: \n" + toolkit.getDigest(testCase));
				System.out.println("Sorted failed: \n" + toolkit.getDigest(sortedCase));
				System.out.println("Sorted: \n" + toolkit.getDigest(sortedTriples));
				System.out.println("Sorted source: \n" + toolkit.getDigest(triples));
				throw error;
			}
		}
	}

	private List<Triple> toTriples(GraphView model) {
		List<Triple> triples = new ArrayList<Triple>();
		Iterators.addAll(triples, model.listStatements(null, null, null));
		return triples;
	}

	private Node makeAnon(String id) {
		return new Node(NodeType.BLANK, id);
	}

	private Node makeNamed(String iri) {
		return new Node(NodeType.NAMED, iri);
	}

	private Node makeLit(String text) {
		return new Node(NodeType.LITERAL, text);
	}
	
	private void addTriple(SimpleGraphView graph, Node subj, Node pred, Node obj) {
		graph.add(new Triple(subj, pred, obj));
	}
	
	@Test
	public void abcTest() {
		Node a = makeAnon("A");
		Node b = makeAnon("B");
		Node c = makeAnon("C");
		Node arrow = makeNamed("arrow");
		
		SimpleGraphView graph = new SimpleGraphView();
		addTriple(graph, a, arrow, b);
		addTriple(graph, b, arrow, c);
		addTriple(graph, c, arrow, a);
		
		testSortIsomorphism(graph);
	}

	
	@Test
	public void abcTestDoubleLink() {
		Node a = makeAnon("A");
		Node b = makeAnon("B");
		Node c = makeAnon("C");
		Node arrow = makeNamed("arrow");

		SimpleGraphView graph = new SimpleGraphView();
		addTriple(graph, a, arrow, b);
		addTriple(graph, b, arrow, a);
		addTriple(graph, b, arrow, c);
		addTriple(graph, c, arrow, b);
		addTriple(graph, c, arrow, a);
		addTriple(graph, a, arrow, c);
		
		testSortIsomorphism(graph);
	}
	
//	@Test
//	public void buterflySubTest() {
//		Node b0 = makeAnon("b0");
//		Node b1 = makeAnon("b1");
//		Node b2 = makeAnon("b2");
//		Node b3 = makeAnon("b3");
//		Node b4 = makeAnon("b4");
//		Node arrow = makeNamed("arrow");
//		
//		List<Triple> triples = new ArrayList<Triple>();
//		
//		triples.add(new Triple(b0, arrow, b1));
//		triples.add(new Triple(b2, arrow, b3));
//		triples.add(new Triple(b2, arrow, b0));
//		triples.add(new Triple(b3, arrow, b4));
//		triples.add(new Triple(b1, arrow, b2));
//		triples.add(new Triple(b4, arrow, b2));
//
//		List<Triple> sorted = new ArrayList<Triple>();
//		
//		sorted.add(new Triple(b0, arrow, b1));
//		sorted.add(new Triple(b1, arrow, b2));
//		sorted.add(new Triple(b2, arrow, b0));
//		sorted.add(new Triple(b2, arrow, b3));
//		sorted.add(new Triple(b3, arrow, b4));
//		sorted.add(new Triple(b4, arrow, b2));
//	
//		ToposortUtils toolkit = new ToposortUtils();
//		// toolkit.getDigest(toolkit.sort(triples))
//		assertEquals(toolkit.sort(triples), sorted);
//	}
//	
	@Test
	public void buterflyTest() {
		Node a = makeAnon("A");
		Node b = makeAnon("B");
		Node c = makeAnon("C");
		Node d = makeAnon("D");
		Node e = makeAnon("A");
		Node arrow = makeNamed("arrow");
		
		SimpleGraphView graph = new SimpleGraphView();
		addTriple(graph, a, arrow, b);
		addTriple(graph, b, arrow, c);
		addTriple(graph, c, arrow, a);
		addTriple(graph, b, arrow, d);
		addTriple(graph, d, arrow, e);
		addTriple(graph, e, arrow, b);
		
		testSortIsomorphism(graph);
	}

	@Test
	public void buterflyTest2() {
		Node a = makeAnon("A");
		Node b = makeAnon("B");
		Node c = makeAnon("C");
		Node d = makeAnon("D");
		Node e = makeAnon("A");
		Node x = makeNamed("x");
		Node arrow = makeNamed("arrow");
		
		SimpleGraphView graph = new SimpleGraphView();
		addTriple(graph, a, arrow, b);
		addTriple(graph, b, arrow, c);
		addTriple(graph, c, arrow, a);
		addTriple(graph, b, arrow, d);
		addTriple(graph, d, arrow, e);
		addTriple(graph, e, arrow, b);
		addTriple(graph, a, arrow, x);
		
		testSortIsomorphism(graph);
	}

	@Test
	public void buterflyTest3() {
		Node a = makeAnon("A");
		Node b = makeAnon("B");
		Node c = makeAnon("C");
		Node d = makeAnon("D");
		Node e = makeAnon("A");
		Node x = makeNamed("x");
		Node arrow = makeNamed("arrow");
		
		SimpleGraphView graph = new SimpleGraphView();
		addTriple(graph, a, arrow, b);
		addTriple(graph, b, arrow, c);
		addTriple(graph, c, arrow, a);
		addTriple(graph, b, arrow, d);
		addTriple(graph, d, arrow, e);
		addTriple(graph, e, arrow, b);
		addTriple(graph, c, arrow, x);
		
		testSortIsomorphism(graph);
	}
	
	@Test
	public void abcxTest_sub1() {
		Node a = makeAnon("A");
		Node b = makeAnon("B");
		Node c = makeAnon("C");
		Node x = makeLit("x");
		Node arrow = makeNamed("arrow");
		Node no = makeNamed("no");
		
		SimpleGraphView graph = new SimpleGraphView();
		//addTriple(graph, a, arrow, b);
		addTriple(graph, b, arrow, c);
		addTriple(graph, c, arrow, a);
		addTriple(graph, a, no, x);
		
		testSortIsomorphism(graph);
	}

	@Test
	public void abcxTest_sub2() {
		Node a = makeAnon("A");
		Node b = makeAnon("B");
		Node c = makeAnon("C");
		Node x = makeLit("x");
		Node arrow = makeNamed("arrow");
		Node no = makeNamed("no");
		
		SimpleGraphView graph = new SimpleGraphView();
		addTriple(graph, a, arrow, b);
		//addTriple(graph, b, arrow, c);
		addTriple(graph, c, arrow, a);
		addTriple(graph, a, no, x);
		
		testSortIsomorphism(graph);
	}

	@Test
	public void abcxTest_sub3() {
		Node a = makeAnon("A");
		Node b = makeAnon("B");
		Node c = makeAnon("C");
		Node x = makeLit("x");
		Node arrow = makeNamed("arrow");
		Node no = makeNamed("no");
		
		SimpleGraphView graph = new SimpleGraphView();
		addTriple(graph, a, arrow, b);
		addTriple(graph, b, arrow, c);
		//addTriple(graph, c, arrow, a);
		addTriple(graph, a, no, x);
		
		testSortIsomorphism(graph);
	}

	@Test
	public void abcxTest1() {
		Node a = makeAnon("A");
		Node b = makeAnon("B");
		Node c = makeAnon("C");
		Node x = makeLit("x");
		Node arrow = makeNamed("arrow");
		Node no = makeNamed("no");
		
		SimpleGraphView graph = new SimpleGraphView();
		addTriple(graph, a, arrow, b);
		addTriple(graph, b, arrow, c);
		addTriple(graph, c, arrow, a);
		addTriple(graph, a, no, x);
		
		testSortIsomorphism(graph);
	}
	
}
