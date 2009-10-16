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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com
 */
abstract class AbstractPrefetchIterator<T> implements Iterator<T> {
	
	protected T next;
	protected boolean hasNext;

	public AbstractPrefetchIterator() {
		// sub claseses should call fetchNext() in constructors
	}
	
	public boolean hasNext() {
		return hasNext;
	}

	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		T result = next;
		fetchNext();
		return result;
	}



	public void remove() {
		throw new UnsupportedOperationException();
	}

	protected abstract void fetchNext(); 
}
