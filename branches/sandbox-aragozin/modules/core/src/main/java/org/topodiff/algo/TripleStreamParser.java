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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.topodiff.graph.Node;
import org.topodiff.graph.Triple;


/**
 * Utility class used by {@link DiffProcessor}
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com
 */
class TripleStreamParser {

	public enum Mode {
		BLANKS,
		NAMED,
	}
	
	private final Iterator<Triple> reader;
	private final Object scopeOverride;
	private Triple nextLine;
	
	private Mode mode = Mode.BLANKS;
	
	public TripleStreamParser(Iterator<Triple> reader) {
		this.reader = reader;
		this.nextLine = reader.hasNext() ? reader.next() : null;
		scopeOverride = this;
		if (nextLine != null) {
			this.nextLine = reown(nextLine);
		}
	}

	private Triple reown(Triple triple) {
		if (scopeOverride == null) {
			return triple;
		}
		else {
			Node subject = new Node(triple.subject, scopeOverride); 
			Node predicate = new Node(triple.predicate, scopeOverride); 
			Node object = new Node(triple.object, scopeOverride); 
			return new Triple(subject, predicate, object);
		}
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	
	public String getCurrectIsomorphicHash() {
		Node res = getCurrentSubject();
		return res == null ? null : DiffHelper.getIsomorphicsHash(res);		
	}

	public String getCurrectIsomorphicGroup() {
		Node res = getCurrentSubject();
		return res == null ? null : DiffHelper.getIsomorphicsGroupID(res);		
	}
	
	public Node getCurrentSubject() {
		if (isBlocked() || nextLine == null) {
			return null;
		}
		else {
			return nextLine.subject;
		}
	}
	
	private boolean isBlocked() {
		if (nextLine == null || mode == null) {
			return true;
		}
		switch (mode) {
			case BLANKS:
				return nextLine.subject.isUri();
			case NAMED:
				return nextLine.subject.isAnon();
			default:
				return true;
		}
	}

	public List<Triple> readSubject() {
		if (nextLine == null) {
			throw new IllegalStateException("EOF");
		}
		
		Node res = getCurrentSubject();
		
		List<Triple> list = new ArrayList<Triple>();
		list.add(nextLine);
		Node prevSubject = nextLine.subject;
		nextLine = null;
		
		while(reader.hasNext()) {
			nextLine = reown(reader.next());
			if (isGreater(prevSubject, nextLine.subject)) {
				mode = null;
			}
			
			if (res.equals(nextLine.subject)) {
				list.add(nextLine);
				prevSubject = nextLine.subject;
				nextLine = null;
			}
			else {
				break;
			}
		}
		
		return list;
	}

	private boolean isGreater(Node prevSubject, Node subject) {
		return LexographicNodeComparator.INSTANCE.compare(prevSubject, subject) > 0;
	}
}
