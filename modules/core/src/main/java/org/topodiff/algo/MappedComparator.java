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
import java.util.Map;

import org.topodiff.graph.Node;

/**
 * Blank nodes a sorted according to their ID, if ID is not defined, Integer.MAX_VALUE is used (to push such nodes to the bottom of list). 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
class MappedComparator extends LexographicNodeComparator {

	private final Map<Node, Integer> anonMapping1;
	private final Map<Node, Integer> anonMapping2;
	
	public MappedComparator(Map<Node, Integer> anonMapping) {
		this.anonMapping1 = anonMapping;
		this.anonMapping2 = anonMapping;
	}

	public MappedComparator(Map<Node, Integer> anonMapping1, Map<Node, Integer> anonMapping2) {
		this.anonMapping1 = anonMapping1;
		this.anonMapping2 = anonMapping2;
	}

	@Override
	protected int compareAnon(Node o1, Node o2) {
		Integer id1 = anonMapping1.get(o1);
		Integer id2 = anonMapping2.get(o2);
		int int1 = id1 == null ? Integer.MAX_VALUE : id1.intValue();
		int int2 = id2 == null ? Integer.MAX_VALUE : id2.intValue();
		// sure, (int1 - int2) will mostly work, but this way below will protect me fromg arithmetic overflow
		return int1 > int2 ? 1 : int1 < int2 ? -1 : 0 ; 
	}
}