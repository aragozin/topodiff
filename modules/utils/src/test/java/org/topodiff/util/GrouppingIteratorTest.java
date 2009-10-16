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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.topodiff.util.Iterators;
import org.topodiff.util.Morph;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com
 */
public class GrouppingIteratorTest {

	private static final Morph<List<String>, String[]> LIST_TO_ARRAY = new Morph<List<String>, String[]>() {
		public String[] morph(List<String> a) {
			return (String[])a.toArray(new String[a.size()]);
		}
	};
	
	private static final Comparator<String> FIRST_LETTER_COMPARATOR = new Comparator<String>() {
		public int compare(String o1, String o2) {
			String s1 = o1.substring(0, 1);
			String s2 = o2.substring(0, 1);
			return s1.compareTo(s2);
		}
	};

	@Test
	public void groupping0() {
		
		String[] array = {};
		String[][] garray = {};
		
		checkGroupping(array, garray);
	}

	@Test
	public void groupping0_1() {
		
		String[] array = {"A"};
		String[][] garray = {{"A"}};
		
		checkGroupping(array, garray);
	}

	@Test
	public void groupping1() {
		
		String[] array = {"A", "A", "B", "C", "C"};
		String[][] garray = {{"A", "A"}, {"B"}, {"C", "C"}};
		
		checkGroupping(array, garray);
	}

	@Test
	public void groupping2() {
		
		String[] array = {"A", "A1", "B", "C", "C"};
		String[][] garray = {{"A"}, {"A1"}, {"B"}, {"C", "C"}};
		
		checkGroupping(array, garray);
	}

	@Test
	public void groupping3() {
		
		String[] array = {"A", "A1", "B", "C", "C1"};
		String[][] garray = {{"A"}, {"A1"}, {"B"}, {"C"}, {"C1"}};
		
		checkGroupping(array, garray);
	}

	@Test
	public void groupping4() {
		
		String[] array = {"A", "A", "A"};
		String[][] garray = {{"A", "A", "A"}};
		
		checkGroupping(array, garray);
	}

	@Test
	public void comparatorGroupping1() {
		
		String[] array = {"A1", "A2", "A3"};
		String[][] garray = {{"A1", "A2", "A3"}};
		
		checkComparatorGroupping(array, garray, FIRST_LETTER_COMPARATOR);
	}

	@Test
	public void comparatorGroupping2() {
		
		String[] array = {"B", "A1", "A2", "A3"};
		String[][] garray = {{"B"}, {"A1", "A2", "A3"}};
		
		checkComparatorGroupping(array, garray, FIRST_LETTER_COMPARATOR);
	}

	@Test
	public void comparatorGroupping3() {
		
		String[] array = {"B", "A1", "A2", "A3", "C"};
		String[][] garray = {{"B"}, {"A1", "A2", "A3"}, {"C"}};
		
		checkComparatorGroupping(array, garray, FIRST_LETTER_COMPARATOR);
	}

	@Test
	public void comparatorGroupping4() {
		
		String[] array = {"A1", "A2", "A3", "C"};
		String[][] garray = {{"A1", "A2", "A3"}, {"C"}};
		
		checkComparatorGroupping(array, garray, FIRST_LETTER_COMPARATOR);
	}

	private void checkGroupping(String[] array, String[][] garray) {
		Iterator<String[]> gi = Iterators.morph(Iterators.groupped(Arrays.asList(array)), LIST_TO_ARRAY).iterator();
		List<String[]> glist = new ArrayList<String[]>();
		Iterators.addAll(glist, gi);
		
		String list1 = print(Arrays.asList(garray));
		String list2 = print(glist);
		
		Assert.assertEquals(list1, list2);
	}

	private void checkComparatorGroupping(String[] array, String[][] garray, Comparator<String> comparator) {
		Iterator<String[]> gi = Iterators.morph(Iterators.groupped(Arrays.asList(array), comparator), LIST_TO_ARRAY).iterator();
		List<String[]> glist = new ArrayList<String[]>();
		Iterators.addAll(glist, gi);
		
		String list1 = print(Arrays.asList(garray));
		String list2 = print(glist);
		
		Assert.assertEquals(list1, list2);
	}

	private String print(List<String[]> list) {
		StringBuffer buf = new StringBuffer();
		for(String[] group: list) {
			buf.append(Arrays.toString(group));
			buf.append('\n');
		}
		
		return buf.toString();
	}		
}
