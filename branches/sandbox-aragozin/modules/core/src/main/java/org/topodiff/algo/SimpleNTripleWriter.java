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
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.topodiff.graph.Node;
import org.topodiff.graph.Triple;
import org.topodiff.io.TripleReceiver;

/**
 * This writer will automatically number b-nodes.
 * It doesn't do any escaping (result is unparsable), but it is not need to calculate a hash
 * @author ARagozin
 */
class SimpleNTripleWriter implements TripleReceiver {

	private final Map<Node, Integer> anonMap = new HashMap<Node, Integer>();
	private final Writer writer;
	
	public SimpleNTripleWriter(Writer writer) {
		this.writer = writer;
	}
	
	public void receive(Triple stmt) {
		try {
			writerNode(stmt.subject);
			writer.append(" ");
			writerNode(stmt.predicate);
			writer.append(" ");
			writerNode(stmt.object);
			writer.append("\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void done() {
		try {
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writerNode(Node node) throws IOException {
		switch(node.nodeType) {
		case NAMED:
			writer.append('<').append(node.getLexicalForm()).append('>');
			break;
		case LITERAL:
			writer.append('"').append(node.getLexicalForm()).append('"');
			break;
		case BLANK:
			writeBNode(node);
			break;
		}
	}
	
	private void writeBNode(Node node) throws IOException {
		Integer id = anonMap.get(node);
		if (id == null) {
			anonMap.put(node, id = Integer.valueOf(anonMap.size()));
		}
		writer.append("_:b").append(String.valueOf(id));
	}
}
