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

import static org.junit.Assert.*;
import org.junit.Test;
import org.topodiff.util.CompositeIterator;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class CompositeIteratorTest {

    @Test
    public void testHasNext() {
        CompositeIterator<String> iterator = new CompositeIterator<String>(
                new EmptyIterator<String>(),
                new EmptyIterator<String>());

        assertFalse(iterator.hasNext());
    }

    @Test
    public void testNext() {
        CompositeIterator<String> iterator =
                new CompositeIterator<String>(new EmptyIterator<String>(),
                        new EmptyIterator<String>());
        try {
            iterator.next();
            assertTrue(false);
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }
    }

    //TODO: check remove non-empty
    @Test
    public void testRemoveEmpty() {
        CompositeIterator<String> iterator =
                new CompositeIterator<String>(new EmptyIterator<String>(),
                        new EmptyIterator<String>());
        try {
            iterator.remove();
        } catch (NoSuchElementException e) {
            if (!e.getClass().equals(NoSuchElementException.class)) {
                fail("Should throw NoSuchElementException");
            }
            return;
        }
        fail("Should throw exception");
    }

    @Test
    public void testRemoveNonEmpty1() {
        ArrayList<Integer> first = new ArrayList<Integer>();
        first.add(1);
        first.add(2);
        first.add(3);
        ArrayList<Integer> second = new ArrayList<Integer>();
        second.add(4);
        second.add(5);
        second.add(6);

        CompositeIterator<Integer> iterator = new CompositeIterator<Integer>(
                first.iterator(),
                second.iterator());

        iterator.next();
        iterator.next();
        iterator.next();
        iterator.remove();
        Integer i = 4;
        while (i <= 6) {
            Integer j = iterator.next();
            assertEquals(i, j);
            i++;
        }
        assertFalse("Unexpected elements after the end of iteration.", iterator.hasNext());

        assertEquals(first.size(), 2);
        assertEquals(second.size(), 3);
        assertEquals(first.get(0), new Integer(1));
        assertEquals(first.get(1), new Integer(2));
    }

    @Test
    public void testRemoveNonEmpty2() {
        ArrayList<Integer> first = new ArrayList<Integer>();
        first.add(1);
        first.add(2);
        first.add(3);
        ArrayList<Integer> second = new ArrayList<Integer>();
        second.add(4);
        second.add(5);
        second.add(6);

        CompositeIterator<Integer> iterator = new CompositeIterator<Integer>(
                first.iterator(),
                second.iterator());

        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();
        iterator.remove();
        Integer i = 6;
        while (i <= 6) {
            Integer j = iterator.next();
            assertEquals(i, j);
            i++;
        }
        assertFalse("Unexpected elements after the end of iteration.", iterator.hasNext());

        assertEquals(first.size(), 3);
        assertEquals(second.size(), 2);
        assertEquals(second.get(0), new Integer(4));
        assertEquals(second.get(1), new Integer(6));
    }

    @Test
    public void testIterate() {
        ArrayList<Integer> first = new ArrayList<Integer>();
        first.add(1);
        first.add(2);
        first.add(3);
        ArrayList<Integer> second = new ArrayList<Integer>();
        second.add(4);
        second.add(5);
        second.add(6);

        CompositeIterator<Integer> iterator = new CompositeIterator<Integer>(
                first.iterator(),
                second.iterator());
        Integer i = 0;
        while (iterator.hasNext()) {
            i++;
            assertEquals(i, iterator.next());
        }
        assertEquals(i, new Integer(6));
    }
}
