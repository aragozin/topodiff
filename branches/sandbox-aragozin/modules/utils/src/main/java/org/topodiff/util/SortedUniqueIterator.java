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
package org.topodiff.util;

import java.util.Comparator;
import java.util.Iterator;

class SortedUniqueIterator<T> extends AbstractPrefetchIterator<T> {

	private LookAheadIterator<T> lookAhead;
	private Comparator<T> comparator;
	
	public SortedUniqueIterator(Iterator<T> it, Comparator<T> comparator) {
		lookAhead = (LookAheadIterator<T>) (it instanceof LookAheadIterator ? it : Iterators.lookAhead(it));
		this.comparator = comparator;
		fetchNext();
	}

	@Override
	protected void fetchNext() {
		hasNext = lookAhead.hasNext();
		if (hasNext) {
			next = lookAhead.next();
			while(lookAhead.hasNext() && equals(next, lookAhead.peekNext())) {
				lookAhead.next();
			}
		}
	}

	private boolean equals(T next, T peekNext) {
		if (comparator != null) {
			comparator.compare(next, peekNext);
		}
		return (next != null && next.equals(peekNext)) || (next == null && peekNext == null);
	}
}
