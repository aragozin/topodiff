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


/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class LexographicNodeComparator implements Comparator<Node> {

	public static final LexographicNodeComparator INSTANCE = new LexographicNodeComparator();
	
	protected final static int CLASS_URI = 1;
	protected final static int CLASS_LITERAL = 2;
	protected final static int CLASS_ANNON = 3;
	protected final static int CLASS_NULL = 4; // null should be last, this allow to handle EOF as null in DiffProcessor
	
	public int compare(Node o1, Node o2) {
		int c1 = getNodeClass(o1);
		int c2 = getNodeClass(o2);
		
		int res = compareClasses(c1, c2);
		if (res != 0) {
			return res;
		}
		
		switch (c1) {
			case CLASS_NULL:
				return 0;
				
			case CLASS_ANNON:
				return compareAnon(o1, o2);

			case CLASS_URI:
				return compareUri(o1, o2);
			
			case CLASS_LITERAL:
				return compareLiteral(o1, o2);
				
			default:
				return compareOther(c1, o1, o2);
		}
	}

	protected int compareClasses(int c1, int c2) {
		return c1 - c2;
	}

	protected int compareOther(int nodeType, Node o1, Node o2) {
		throw new RuntimeException("Should never happen");
	}

	protected int compareLiteral(Node o1, Node o2) {
		String l1 = o1.getLexicalForm();
		String l2 = o2.getLexicalForm();
		return l1.compareTo(l2);
	}

	protected int compareUri(Node o1, Node o2) {
		String u1 = o1.getLexicalForm();
		String u2 = o2.getLexicalForm();
		return u1.compareTo(u2);
	}

	protected int compareAnon(Node o1, Node o2) {
		String i1 = o1.getLexicalForm();
		String i2 = o2.getLexicalForm();
		return i1.compareTo(i2);
	}
	
	protected int getNodeClass(Node n) {
		if (n == null) {
			return CLASS_NULL;
		}
		else if (n.isAnon()) {
			return CLASS_ANNON;
		}
		else if (n.isLiteral()) {
			return CLASS_LITERAL;
		}
		else if (n.isResource()) {
			return CLASS_URI;
		}
		else {
			throw new RuntimeException("Unclassified node " + n);
		}
	}
}
