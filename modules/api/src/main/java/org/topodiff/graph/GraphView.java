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

import java.util.Iterator;

/**
 * Read only interface to access RDF graph, using this interface simplifies model filtering, and may help to avoid
 * creation of needless Jena {@link Model} instances.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface GraphView {

	public Iterator<Node> listSubjects();
	
	public Iterator<Triple> listStatements(Node res, Node prop, Node obj);
	
}
