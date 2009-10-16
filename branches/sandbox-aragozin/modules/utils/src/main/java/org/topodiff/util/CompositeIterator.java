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
 * The composite of two given iterators.
 *
 * @author Alexander Kuznetsov
 * @author Max Gorbunov
 * @param <T>   {@inheritDoc}
 * @since 1.0
 */
public class CompositeIterator<T> implements Iterator<T> {
    /**
     * The current iterator. May be equal to <code>last</code>.
     */
    private Iterator<T> current;
    /**
     * The second iterator.
     */
    private Iterator<T> last;

    /**
     * Constructs new composition of <code>first</code> and <code>last</code> iterators.
     *
     * @param first the first iterator
     * @param last  the second iterator
     */
    public CompositeIterator(Iterator<T> first, Iterator<T> last) {
        this.current = first;
        this.last = last;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    public boolean hasNext() {
        if (!current.hasNext()) {
            current = last;
        }
        return current.hasNext();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return current.next();
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
        current.remove();
    }
}
