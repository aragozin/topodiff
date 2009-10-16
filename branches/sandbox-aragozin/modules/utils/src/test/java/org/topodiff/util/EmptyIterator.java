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

import org.junit.Ignore;

@Ignore
public class EmptyIterator<T> implements Iterator<T> {

    public boolean hasNext() {
        return false;
    }

    public T next() {
        throw new NoSuchElementException("Iterator is empty.");
    }

    public void remove() {
        throw new NoSuchElementException("Iterator is empty.");
    }
}
