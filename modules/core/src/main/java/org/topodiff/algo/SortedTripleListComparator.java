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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.topodiff.graph.Node;
import org.topodiff.graph.Triple;

/**
 * Comparator for comparing 2 lists of triples. 
 * Result is meaningful only if lists are topologically sorted before.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
class SortedTripleListComparator implements Comparator<List<Triple>> {

	public static final SortedTripleListComparator INSTANCE = new SortedTripleListComparator();
	
	public int compare(List<Triple> o1, List<Triple> o2) {
		Map<Node, Integer> anonMap1 = new HashMap<Node, Integer>();
		Map<Node, Integer> anonMap2 = new HashMap<Node, Integer>();
		
		MappedComparator nodeCmp = new MappedComparator(anonMap1, anonMap2);
		TripleComparator tripleCmp = new TripleComparator(nodeCmp, nodeCmp, nodeCmp);
		
		int n = 0;
		while(n < o1.size() && n < o2.size()) {
			Triple t1 = o1.get(n);
			Triple t2 = o2.get(n);
			++n;
			
			int res = tripleCmp.compare(t1, t2);
			
			if (res != 0) {
				return res;
			}
			
			if (t1.subject.isAnon() && !anonMap1.containsKey(t1.subject)) {
				if (!t2.subject.isAnon()) {
					throw new RuntimeException("Caught a bug! Go and fix it ;)");
				}
				
				anonMap1.put(t1.subject, Integer.valueOf(anonMap1.size()));
				anonMap2.put(t2.subject, Integer.valueOf(anonMap2.size()));
				
				if (anonMap1.size() != anonMap2.size()) {
					throw new RuntimeException("Caught a bug! Go and fix it ;)");
				}
			}

			if (t1.object.isAnon() && !anonMap1.containsKey(t1.object)) {
				if (!t2.object.isAnon()) {
					throw new RuntimeException("Caught a bug! Go and fix it ;)");
				}
				
				anonMap1.put(t1.object, Integer.valueOf(anonMap1.size()));
				anonMap2.put(t2.object, Integer.valueOf(anonMap2.size()));
				
				if (anonMap1.size() != anonMap2.size()) {
					throw new RuntimeException("Caught a bug! Go and fix it ;)");
				}
			}
		}
		
		return o1.size() - o2.size();
	}
}