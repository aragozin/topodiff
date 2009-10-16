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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topodiff.graph.Node;
import org.topodiff.graph.Triple;
import org.topodiff.util.text.Encodings;

/**
 * This class implements deterministic sorting of triple-set.
 * Deterministic means that sorting of isomorphic models will yield equivalent list of triples 
 * (after renaming of b-nodes according to other deterministic algorithm also implemented here).  
 *  
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ToposortUtils {

	public static final ToposortUtils INSTANCE = new ToposortUtils();
	
	/**
	 * Deterministic topological sort.
	 * @param triples remains unchanged
	 * @return sorted triples
	 */
	public List<Triple> sort(Collection<Triple> triples) {
		Map<Node, Integer> anonRemap = new HashMap<Node, Integer>();
		return sort(triples, anonRemap);
	}

	private List<Triple> sort(Collection<Triple> triples, Map<Node, Integer> anonRemap) {
		
		List<Triple> result = new ArrayList<Triple>(triples);
		if (triples.size() <= 1) {
			return result;
		}
		
		int n = 0;
		
		while(n < result.size()) {
		
			Comparator<Node> nodeCmp = new MappedComparator(new HashMap<Node, Integer>(anonRemap));
			Comparator<Triple> tripleComparator = new TripleComparator(nodeCmp, nodeCmp, nodeCmp);

			// TODO buble, may be not?
			Collections.sort(result.subList(n, result.size()), tripleComparator);
			
			while(n < result.size()) {
				// search for rows with unnamed blanks (blank rows for short)
				
				Triple triple = result.get(n);
				boolean blank = (triple.subject.isAnon() && !anonRemap.containsKey(triple.subject))
						|| (triple.object.isAnon() && !anonRemap.containsKey(triple.object));
				
				if (!blank || n == result.size() - 1) {
					// please note, last blank row may remain unmapped but it is OK
					++n;
					continue;
				}
				
				// we've got a blank row, now if next row equals to this (using comparator ignoring unmapped blanks)
				// we should fall to subgraphs sorting approach
				
				Triple nextRow = result.get(n + 1);
				if (tripleComparator.compare(triple, nextRow) == 0) {
					
					// subgraphs sorting
					List<Triple> equSet = new ArrayList<Triple>();
					equSet.add(triple);
					equSet.add(nextRow);
					int nn = n + 2;
					while(nn < result.size()) {
						Triple row = result.get(nn++);
						if (tripleComparator.compare(triple, row) == 0) {
							equSet.add(row);
						}
						else {
							break;
						}
					}
					
					// Need to find "min" triple, we will compare triples by remaining graphs attached to them
					Triple winner = null;
					List<Triple> minSubgraph = null;
					
					for(Triple row: equSet) {
						Set<Triple> subtriples = new HashSet<Triple>();
						subtriples.add(row);
						buildClosure(subtriples, result.subList(n, result.size()));
						
						Map<Node, Integer> childRemap = new HashMap<Node, Integer>(anonRemap);
						numberBlanks(row, childRemap);
						
						List<Triple> subgraph = sort(subtriples, childRemap);
						
						if (minSubgraph == null || SortedTripleListComparator.INSTANCE.compare(subgraph, minSubgraph) < 0) {
							winner = row;
							minSubgraph = subgraph;
						}
					}
					
					triple = winner;

					// and renumber it
					numberBlanks(triple, anonRemap);

					// and fallback to sorting
					break;
				}
				else {
					numberBlanks(triple, anonRemap);
					break;				
				}
			}
		}
		
		return result;
	}

	private void numberBlanks(Triple triple, Map<Node, Integer> anonRemap) {
		if (triple.subject.isAnon() && !anonRemap.containsKey(triple.subject)) {
			anonRemap.put(triple.subject, Integer.valueOf(anonRemap.size()));
		}
		if (triple.object.isAnon() && !anonRemap.containsKey(triple.object)) {
			anonRemap.put((Node) triple.object, Integer.valueOf(anonRemap.size()));
		}
	}
	
	private void buildClosure(Set<Triple> subset, List<Triple> tripleScope) {
		
		while(true) {
			Set<Node> blanks = new HashSet<Node>();
			for(Triple triple: subset) {
				if (triple.subject.isAnon()) {
					blanks.add(triple.subject);
				}
				if (triple.object.isAnon()) {
					blanks.add((Node) triple.object);
				}
			}
			
			boolean modified = false;
			for(Triple triple: tripleScope) {
				if (subset.contains(triple)) {
					continue;
				}
				else if (blanks.contains(triple.subject) || blanks.contains(triple.object)) {
					subset.add(triple);
					modified = true;
				}
			}
			
			if (!modified) {
				break;
			}
		}
	}

	/**
	 * Deterministic bnode numbering (renaming).
	 * @param triples sorted list of triples
	 * 
	 * XXX it is not used in code now, but I will leave it as example
	 */
	public Map<Node, Integer> renameBlanks(List<Triple> triples) {
		Map<Node, Integer> map = new HashMap<Node, Integer>();
		
		for(Triple triple: triples) {
			if (triple.subject.isAnon()) {
				if (!map.containsKey(triple.subject)) {
					map.put(triple.subject, Integer.valueOf(map.size()));
				}
			}
			if (triple.object.isAnon()) {
				if (!map.containsKey(triple.object)) {
					map.put((Node) triple.object, Integer.valueOf(map.size()));
				}
			}
		}
		
		return map;
	}
	
	/**
	 * @param triples should be topologically sorted to yield meaningful result
	 * @return string representing this topology (isomorphic graphs will have equal string)
	 */
	public String getDigest(List<Triple> triples) {
		StringWriter writer = new StringWriter();
		SimpleNTripleWriter tripleWriter = new SimpleNTripleWriter(writer);
		for(Triple triple: triples) {
			tripleWriter.receive(triple);
		}
		tripleWriter.done();
		
		return writer.toString();
	}
	
	public byte[] getDigestHash(List<Triple> triples, MessageDigest digest) {
		try {
			OutputStream nullStream = new OutputStream() {
				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					// do nothing
				}

				@Override
				public void write(byte[] b) throws IOException {
					// do nothing
				}

				@Override
				public void write(int b) throws IOException {
					// do nothing
				}
			};
			
			digest.reset();
			DigestOutputStream dos = new DigestOutputStream(nullStream, digest);
			
			OutputStreamWriter writer = new OutputStreamWriter(dos, Encodings.UTF8);
			SimpleNTripleWriter tripleWriter = new SimpleNTripleWriter(writer);
			for(Triple triple: triples) {
				tripleWriter.receive(triple);
			}
			tripleWriter.done();
			writer.close();
			
			byte[] result = digest.digest();
			digest.reset();
			
			return result;
		} catch (IOException e) {
			throw new RuntimeException("How it is possible?", e);
		}
	}
}
