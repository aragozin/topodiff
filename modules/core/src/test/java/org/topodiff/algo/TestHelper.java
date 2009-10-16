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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Ignore;
import org.topodiff.algo.DiffProcessorTest.GraphSortingLaw;
import org.topodiff.diff.ModelDiffAlgo.DiffOption;
import org.topodiff.graph.GraphView;
import org.topodiff.graph.Node;
import org.topodiff.graph.Triple;
import org.topodiff.io.ModelDeltaReceiver;
import org.topodiff.algo.DiffProcessor;
import org.topodiff.algo.LexographicNodeComparator;
import org.topodiff.graph.simple.SimpleGraphView;
import org.topodiff.graph.utils.GraphUtils;
import org.topodiff.util.Iterators;
import org.topodiff.util.Pair;


/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@Ignore
class TestHelper {
	
	public static void sort(List<Triple> triples) {
		TripleComparator cmp = new TripleComparator(LexographicNodeComparator.INSTANCE);
		Collections.sort(triples, cmp);
	}
	
	/**
	 * for test usage only
	 */
	public static Triple triple(String text) {
		if (!text.trim().endsWith(".")) {
			text += " .";
		}
		return parseTripleList(text).get(0);
	}
	
	public static List<Triple> parseTripleList(String[] text) {
		List<Triple> result = new ArrayList<Triple>();
		for(String line: text) {
			result.add(triple(line));
		}
		return result;
	}

	public static List<Triple> parseTripleList(String text) {
		TestNTripleFormatReader reader = new TestNTripleFormatReader(new StringReader(text));
		List<Triple> result = new ArrayList<Triple>();
		Iterators.addAll(result, reader);
		return result;
	}
	
	public static DeltaHandler createDeltaHandler() {
		return new DeltaHandler();
	}
	
	public static class DeltaHandler implements ModelDeltaReceiver, Iterable<Pair<Triple, Triple>> {
		
		public int aSize;
		public int bSize;
		public int dSize;
		
		private final List<Pair<Triple, Triple>> pairs = new ArrayList<Pair<Triple,Triple>>();
		private final EnumSet<DiffOption> mode;

		public DeltaHandler() {
			this(EnumSet.allOf(DiffOption.class));
		}
		
		public DeltaHandler(EnumSet<DiffOption> mode) {
			this.mode = mode;
		}

		public Iterator<Pair<Triple, Triple>> iterator() {
			return pairs.iterator();
		}

		public void tripleMatch(Triple tripleA, Triple tripleB) {
			if (tripleA != null) {
				aSize++;
			}
			if (tripleA != null) {
				bSize++;
			}
			if (tripleA == null || tripleB == null) {
				dSize++;
			}

			if ( (tripleA != null && tripleB != null && mode.contains(DiffOption.RETAINED))
					|| (tripleA != null && tripleB == null && mode.contains(DiffOption.REMOVED))
					|| (tripleA == null && tripleB != null && mode.contains(DiffOption.ADDED)) ) {
				pairs.add(Pair.make(tripleA, tripleB));
			}
		}

		public void done() {
			// do nothing
		}
	}

	public static ListTripleWriter sortModel(GraphView graph, GraphSortingLaw sortAlgo) {
		ListTripleWriter triples = new ListTripleWriter();
		sortAlgo.sort(graph, triples);
		SimpleGraphView newGraph = new SimpleGraphView(triples);
		Assert.assertTrue(GraphUtils.areIsomorthic(graph, newGraph));
		
		return triples;
	}
	
	public static DeltaHandler diff(String[] source1, String[] source2, GraphSortingLaw sortAlgo) {
		SimpleGraphView model1 = new SimpleGraphView(parseTripleList(source1));
		SimpleGraphView model2 = new SimpleGraphView(parseTripleList(source2));
		
		ListTripleWriter triples1 = sortModel(model1, sortAlgo);
		ListTripleWriter triples2 = sortModel(model2, sortAlgo);
		
		DeltaHandler handler = createDeltaHandler();
		DiffProcessor dp = new DiffProcessor(triples1.iterator(), triples2.iterator(), handler);
		dp.process();
		
		return handler;
	}

	public static void testDiffSummary(String[] source1, String[] source2, GraphSortingLaw sortAlgo, int deltaSize) {
		DeltaHandler handler = diff(source1, source2, sortAlgo);
		@SuppressWarnings("unused")
		String result = print(handler);
		Assert.assertEquals(source1.length, handler.aSize);
		Assert.assertEquals(source2.length, handler.bSize);
		Assert.assertEquals(deltaSize, handler.dSize);
	}

//	public static void testDiffSummary(String[] source1, String[] source2, ModelSortAlgo sortAlgo, int deltaSize) {
//		Model model1 = new ModelLoader().loadModel(parseTripleList(source1).iterator());
//		Model model2 = new ModelLoader().loadModel(parseTripleList(source2).iterator());
//		
//		ListTripleWriter triples1 = new ListTripleWriter();
//		ListTripleWriter triples2 = new ListTripleWriter();
//		
//		sortAlgo.writeSorted(new DefaultGraphView(model1), triples1); 
//		sortAlgo.writeSorted(new DefaultGraphView(model2), triples2); 
//		
//		List<Pair<Triple, Triple>> delta = parseTripleDelta(diff);
//		
//		DeltaHandler handler = createDeltaHandler();
//		DiffProcessor dp = new DiffProcessor(triples1.iterator(), triples2.iterator(), handler);
//		dp.process();
//		
//		String result1 = print(delta);
//		String result2 = print(handler);
//		
//		Assert.assertEquals(result1, result2);
//	}
	
//	private static List<Pair<Triple, Triple>> parseTripleDelta(String[][] diff) {
//		List<Pair<Triple, Triple>> result = new ArrayList<Pair<Triple,Triple>>();
//		for(String[] line: diff) {
//			if (line.length == 1) {
//				Triple triple = triple(line[0]);
//				result.add(Pair.make(triple, triple));
//			}
//			else {
//				Triple a = null;
//				Triple b = null;
//				
//				if (line[0] != null) {
//					a = triple(line[0]);
//				}
//				if (line[1] != null) {
//					b = triple(line[1]);
//				}
//				
//				result.add(Pair.make(a, b));
//				
//			}
//		}
//		
//		return result;
//	}

	private static String print(Iterable<Pair<Triple, Triple>> delta) {
		
		class Printer {
			
			StringBuffer buf = new StringBuffer();
			Map<Node, String> anonMap1 = new HashMap<Node, String>();
			Map<Node, String> anonMap2 = new HashMap<Node, String>();
			int bcount = 0;
			
			public void print(Iterable<Pair<Triple, Triple>> delta) {
				for(Pair<Triple, Triple> line: delta) {
					if (line.a != null && line.b != null) {
						match(line.a.subject, line.b.subject);
						match(line.a.object, line.b.object);
						buf.append("AB: ");
						print1(line.a);
						buf.append('\n');

					}
					else if (line.a == null) {
						register2(line.b.subject);
						register2(line.b.object);
						buf.append("B:  ");
						print2(line.b);
						buf.append('\n');

					}
					else {
						register1(line.a.subject);
						register1(line.a.object);
						buf.append("A:  ");
						print1(line.a);
						buf.append('\n');
					}
				}
			}
			
			private void match(Node nodeA, Node nodeB) {
				if (nodeA.isAnon()) {
					String name = anonMap1.get(nodeA);
					if (name == null) {
						name = anonMap2.get(nodeB);
					}
					if (name == null) {
						name = String.valueOf(bcount++);
					}
					
					anonMap1.put(nodeA, name);
					anonMap1.put(nodeB, name);
				}				
			}

			private void register1(Node object) {
				if (object.isAnon() && !anonMap1.containsKey(object)) {
					String name = String.valueOf(bcount++);
					anonMap1.put(object, name);
				}
			}

			private void register2(Node object) {
				if (object.isAnon() && !anonMap2.containsKey(object)) {
					String name = String.valueOf(bcount++);
					anonMap2.put(object, name);
				}
			}
			
			private void print1(Triple a) {
				buf.append(print1(a.subject));
				buf.append(" ");
				buf.append(print1(a.predicate));
				buf.append(" ");
				buf.append(print1(a.object));				
				buf.append(".");
			}

			private String print1(Node node) {
				if (node.isAnon()) {
					String name = anonMap1.get(node);
					return "_:" + name;
				}
				else if (node.isLiteral()) {
					return "\"" + node.getLexicalForm() + "\"";
				}
				else {
					return "<" + node.getLexicalForm() + ">";
				}
			}

			private void print2(Triple a) {
				buf.append(print2(a.subject));
				buf.append(" ");
				buf.append(print2(a.predicate));
				buf.append(" ");
				buf.append(print2(a.object));				
				buf.append(".");
			}
			
			private String print2(Node node) {
				if (node.isAnon()) {
					String name = anonMap2.get(node);
					return "_:" + name;
				}
				else if (node.isLiteral()) {
					return "\"" + node.getLexicalForm() + "\"";
				}
				else {
					return "<" + node.getLexicalForm() + ">";
				}
			}
			
			public String toString() {
				return buf.toString();
			}
		}
		
		Printer printer = new Printer();
		printer.print(delta);
		
		return printer.toString();
	}
	
//	private static List<Triple> reown(List<Triple> list) {
//		Model model = ModelFactory.createDefaultModel();
//		for(int i = 0; i != list.size(); ++i) {
//			list.set(i, TripleHelper.reown(model, list.get(i)));
//		}
//		return list;
//	}
}
