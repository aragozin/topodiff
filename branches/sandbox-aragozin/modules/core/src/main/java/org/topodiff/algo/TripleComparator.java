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

import org.topodiff.graph.Node;
import org.topodiff.graph.Triple;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
class TripleComparator implements Comparator<Triple> {
	
	public final Comparator<? super Node> subjectComparator;
	public final Comparator<? super Node> predicateComparator;
	public final Comparator<? super Node> objectComparator;
	
	public TripleComparator(Comparator<? super Node> nodeComparator) {
		this(nodeComparator, nodeComparator, nodeComparator);
	}

	public TripleComparator(Comparator<? super Node> subjectComparator, Comparator<? super Node> predicateComparator,	Comparator<? super Node> objectComparator) {
		this.subjectComparator = subjectComparator;
		this.predicateComparator = predicateComparator;
		this.objectComparator = objectComparator;
	}

	public int compare(Triple n1, Triple n2) {
		
		if (subjectComparator != null) {
			Node s1 = n1.subject;
			Node s2 = n2.subject;
			
			int res = subjectComparator.compare(s1, s2);
			
			if (res != 0) {
				return res;
			}
		}
		
		if (predicateComparator != null) {
			Node p1 = n1.predicate;
			Node p2 = n2.predicate;
			
			int res = predicateComparator.compare(p1, p2);
			
			if (res != 0) {
				return res;
			}
		}
		
		if (objectComparator != null) {
			Node o1 = n1.object; 
			Node o2 = n2.object;
			
			int res = objectComparator.compare(o1, o2);
			
			if (res != 0) {
				return res;
			}
		}
		
		return 0;
	}
}