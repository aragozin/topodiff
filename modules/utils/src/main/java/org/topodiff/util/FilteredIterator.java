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


public class FilteredIterator<T> implements Iterator<T> {
    /**
     * The underlying iterator.
     */
    private Iterator<T> iterator;
    /**
     * The filtering object.
     */
    private Match<T> filter;
    /**
     * The next element in the underlying <code>iterator</code>, if any.
     */
    private T nextElement;
    /**
     * Contains <code>true</code> if there is the next element.
     */
    private boolean hasNext;

    /**
     * Constructs new <code>FilteredIterator</code> object
     * to filter <code>iterator</code> with given <code>filter</code>.
     *
     * @param iterator the iterator to filter
     * @param filter   the {@link Match}
     */
    public FilteredIterator(Iterator<T> iterator, Match<T> filter) {
        this.iterator = iterator;
        this.filter = filter;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    public boolean hasNext() {
        if (hasNext) {
            return true;
        }

        while (iterator.hasNext()) {
            T t = iterator.next();
            if (filter.match(t)) {
                hasNext = true;
                nextElement = t;
                return true;
            }
        }

        return false;
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
        hasNext = false;
        return nextElement;
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
        iterator.remove();
    }
}
