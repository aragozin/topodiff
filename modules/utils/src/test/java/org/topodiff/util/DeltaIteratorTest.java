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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.topodiff.util.Iterators;
import org.topodiff.util.Morph;
import org.topodiff.util.Pair;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com
 */
public class DeltaIteratorTest {

	@SuppressWarnings("unused")
	private static final Morph<List<String>, String[]> LIST_TO_ARRAY = new Morph<List<String>, String[]>() {
		public String[] morph(List<String> a) {
			return (String[])a.toArray(new String[a.size()]);
		}
	};
	
	@SuppressWarnings("unused")
	private static final Comparator<String> FIRST_LETTER_COMPARATOR = new Comparator<String>() {
		public int compare(String o1, String o2) {
			String s1 = o1.substring(0, 1);
			String s2 = o2.substring(0, 1);
			return s1.compareTo(s2);
		}
	};

	@Test
	public void deltaTest0() {
		String[] stream1 = {}; 
		String[] stream2 = {};
		String[][][] delta = {};
		
		checkDelta(stream1, stream2, delta);
	}

	@Test
	public void deltaTest1() {
		String[] stream1 = {"A"}; 
		String[] stream2 = {"A"};
		String[][][] delta = {{{"A"},{"A"}}};
		
		checkDelta(stream1, stream2, delta);
	}

	@Test
	public void deltaTest2() {
		String[] stream1 = {"B"}; 
		String[] stream2 = {"C"};
		String[][][] delta = {{{"B"},{}}, {{}, {"C"}}};
		
		checkDelta(stream1, stream2, delta);
	}

	@Test
	public void deltaTest3() {
		String[] stream1 = {"A", "B"}; 
		String[] stream2 = {"B", "C"};
		String[][][] delta = {{{"A"},{}}, {{"B"}, {"B"}},{{}, {"C"}}};
		
		checkDelta(stream1, stream2, delta);
	}

	@Test
	public void deltaTest4() {
		String[] stream1 = {"A", "B", "B"}; 
		String[] stream2 = {"B", "C"};
		String[][][] delta = {{{"A"},{}}, {{"B", "B"}, {"B"}},{{}, {"C"}}};
		
		checkDelta(stream1, stream2, delta);
	}

	@Test
	public void deltaTest5() {
		String[] stream1 = {"A", "B", "B"}; 
		String[] stream2 = {"B", "C", "C"};
		String[][][] delta = {{{"A"},{}}, {{"B", "B"}, {"B"}},{{}, {"C", "C"}}};
		
		checkDelta(stream1, stream2, delta);
	}

	@Test
	public void deltaTest6() {
		String[] stream1 = {"A", "A", "A"}; 
		String[] stream2 = {};
		String[][][] delta = {{{"A", "A", "A"},{}}};
		
		checkDelta(stream1, stream2, delta);
	}

	@Test
	public void deltaTest7() {
		String[] stream1 = {};
		String[] stream2 = {"A", "A", "A"}; 
		String[][][] delta = {{{}, {"A", "A", "A"}}};
		
		checkDelta(stream1, stream2, delta);
	}
	
	@Test
	public void deltaTest8() {
		String[] stream1 = {"A", "B", "D"}; 
		String[] stream2 = {"A", "C", "D"};
		String[][][] delta = {{{"A"},{"A"}}, {{"B"}, {}},{{}, {"C"}}, {{"D"}, {"D"}}};
		
		checkDelta(stream1, stream2, delta);
	}
	
	private void checkDelta(String[] stream1, String[] stream2, String[][][] delta) {
		
		Iterable<Pair<List<String>, List<String>>> di = Iterators.sortedDelta(Arrays.asList(stream1), Arrays.asList(stream2));

		StringBuffer buf = new StringBuffer();
		for(Pair<List<String>, List<String>> pair: di) {
			buf.append(Arrays.toString(pair.a.toArray()));
			buf.append(" -- ");
			buf.append(Arrays.toString(pair.b.toArray()));
			buf.append('\n');
		}
		
		Assert.assertEquals(print(delta), buf.toString());
	}

	private String print(String[][][] delta) {
		StringBuffer buf = new StringBuffer();
		for(String[][] group: delta) {
			String[] a = group[0];
			String[] b = group[1];
			buf.append(Arrays.toString(a));
			buf.append(" -- ");
			buf.append(Arrays.toString(b));
			buf.append('\n');
		}
		
		return buf.toString();
	}		
}
