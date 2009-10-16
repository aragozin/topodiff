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

package org.topodiff.graph;

/**
 * Very simple implementation of RDF node.
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class Node {

	public final NodeType nodeType;
	public final String lexicalForm;
	public final Object scope; // required to distinguish b-nodes from different models

	public Node(NodeType nodeType, String lexicalForm) {
		this(nodeType, lexicalForm, null);
	}

	public Node(NodeType nodeType, String lexicalForm, Object scope) {
		this.nodeType = nodeType;
		this.lexicalForm = lexicalForm;
		this.scope = scope;
		if (nodeType == null || lexicalForm == null) {
			throw new IllegalArgumentException("Null is not allowed");
		}
	}
	
	public Node(Node node, Object scope) {
		this(node.nodeType, node.lexicalForm, scope);
	}
	
	public boolean isAnon() {
		return nodeType == NodeType.BLANK;
	}

	public boolean isUri() {
		return nodeType == NodeType.NAMED;
	}
	
	public boolean isResource() {
		return nodeType != NodeType.LITERAL;
	}

	public boolean isLiteral() {
		return nodeType == NodeType.LITERAL;
	}

	public String getLexicalForm() {
		return lexicalForm;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((lexicalForm == null) ? 0 : lexicalForm.hashCode());
		result = prime * result
				+ ((nodeType == null) ? 0 : nodeType.hashCode());		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (lexicalForm == null) {
			if (other.lexicalForm != null)
				return false;
		} else if (!lexicalForm.equals(other.lexicalForm))
			return false;
		if (nodeType == null) {
			if (other.nodeType != null)
				return false;
		} else if (!nodeType.equals(other.nodeType))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return lexicalForm;
	}
}
