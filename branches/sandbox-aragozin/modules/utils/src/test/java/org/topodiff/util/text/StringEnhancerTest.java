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
package org.topodiff.util.text;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.topodiff.util.text.Strings;

public class StringEnhancerTest {

    @Test
    public void testEmptyListShouldJoinToEmptyString() {
        Assert.assertEquals(Strings.join(new ArrayList<String>(), ","), "");
    }

    @Test
    public void testListWithSingleElementEmptyShouldJoinToItsStringRepresentation() {
        List<String> list = new ArrayList<String>();
        list.add("a");
        assertEquals("a", Strings.join(list, ","));
    }

    @Test
    public void testNonEmptyShouldJoinToProperStringWithSeparators() {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        assertEquals("a,b", Strings.join(list, ","));
    }

}
