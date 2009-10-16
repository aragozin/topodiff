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
package org.topodiff.io.sort;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topodiff.graph.GraphView;
import org.topodiff.graph.Node;
import org.topodiff.graph.NodeType;
import org.topodiff.io.TripleReceiver;
import org.topodiff.algo.ToposortGraphProcessor;
import org.topodiff.util.Filters;
import org.topodiff.util.Match;
import org.topodiff.util.hash.MessageDigesters;
import org.topodiff.util.text.Strings;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ToposortSortMethod implements GraphSortMethod {
	
	private final static Set<Node> DEFAULT_PROPS_TO_FOLLOW = new HashSet<Node>();
	static {
		DEFAULT_PROPS_TO_FOLLOW.add(new Node(NodeType.NAMED, "http://www.w3.org/TR/rdf-schema/#first"));
		DEFAULT_PROPS_TO_FOLLOW.add(new Node(NodeType.NAMED, "http://www.w3.org/TR/rdf-schema/#rest"));
	}
	
	private final MessageDigest hashAlgo = MessageDigesters.createSHA1();
	private final Match<Node> propFilter = Filters.inList(DEFAULT_PROPS_TO_FOLLOW);
	
	public String getAlgorithmFingerPrint() {
		List<String> props = new ArrayList<String>();
		
		props.add("algorithm=TOPOSORT");
		props.add("class=" + ToposortGraphProcessor.class.getName());
		props.add("bCluster.filter=" + propFilter.toString());
		
		Collections.sort(props);
		
		return Strings.join(props, "\n");
	}

	public void sort(GraphView model, TripleReceiver tripleWriter) {
		ToposortGraphProcessor adapter = new ToposortGraphProcessor(tripleWriter, propFilter, hashAlgo);
		adapter.process(model);
	}
}
