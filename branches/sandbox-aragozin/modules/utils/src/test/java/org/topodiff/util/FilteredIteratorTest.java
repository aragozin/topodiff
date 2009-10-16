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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;
import org.topodiff.util.FilteredIterator;
import org.topodiff.util.Match;

public class FilteredIteratorTest {

    @Test
    public void testFilteredIterator() {
        ArrayList<Integer> first = new ArrayList<Integer>();
        first.add(1);
        first.add(2);
        first.add(3);
        first.add(4);
        first.add(5);
        first.add(6);

        FilteredIterator<Integer> filteredIterator = new FilteredIterator<Integer>(first.iterator(), new IntegerFilter());
        while (filteredIterator.hasNext()) {
            assertTrue(filteredIterator.next() > 3);
        }
    }

    @Test
    public void testFilteredIteratorWithNull1() {
        ArrayList<Integer> first = new ArrayList<Integer>();
        first.add(1);
        first.add(2);
        first.add(3);
        first.add(4);
        first.add(5);
        first.add(6);
        first.add(null);

        FilteredIterator<Integer> filteredIterator = new FilteredIterator<Integer>(first.iterator(), new NullFilter<Integer>());
        while (filteredIterator.hasNext()) {
            assertEquals(null, filteredIterator.next());
        }
    }

    @Test
    public void testFilteredIteratorWithNull2() {
        ArrayList<Integer> first = new ArrayList<Integer>();
        first.add(null);
        first.add(1);
        first.add(2);
        first.add(3);
        first.add(4);
        first.add(5);
        first.add(6);


        FilteredIterator<Integer> filteredIterator = new FilteredIterator<Integer>(first.iterator(), new NullFilter<Integer>());
        while (filteredIterator.hasNext()) {
            assertEquals(null, filteredIterator.next());
        }
    }

    @Test
    public void testFilteredIteratorWithNull3() {
        ArrayList<Integer> first = new ArrayList<Integer>();
        first.add(1);
        first.add(2);
        first.add(null);
        first.add(3);
        first.add(4);
        first.add(5);
        first.add(6);


        FilteredIterator<Integer> filteredIterator = new FilteredIterator<Integer>(first.iterator(), new NullFilter<Integer>());
        while (filteredIterator.hasNext()) {
            assertEquals(null, filteredIterator.next());
        }
    }

    @Test
    public void testFilteredIteratorWithNull4() {
        ArrayList<Integer> first = new ArrayList<Integer>();
        first.add(1);
        first.add(2);
        first.add(3);
        first.add(4);
        first.add(5);
        first.add(6);
        first.add(null);

        FilteredIterator<Integer> filteredIterator = new FilteredIterator<Integer>(first.iterator(), new NotNullFilter<Integer>());
        while (filteredIterator.hasNext()) {
            assertTrue(null != filteredIterator.next());
        }
    }

    private class IntegerFilter implements Match<Integer> {

        public boolean match(Integer integer) {
            return (integer > 3);
        }
    }

    private class NullFilter<T> implements Match<T> {

        public boolean match(T object) {
            return (object == null);
        }
    }

    private class NotNullFilter<T> implements Match<T> {

        public boolean match(T object) {
            return (object != null);
        }
    }
}
