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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class Filters {

	public static <A> Match<A> or(final Collection<Match<A>> filters) {
		Match<A>[] array = AnyType.cast(new Match<?>[filters.size()]);
		filters.toArray(array);
		return or(array);
	}

	public static <A> Match<A> or(final Match<A> ... filters) {
		if (filters.length == 0) {
			throw new IllegalArgumentException("OR with zero arguments is undefined");
		}
		
		Match<A> orFilter = new Match<A>() {

			public boolean match(A object) {
				for(Match<A> filter: filters) {
					if (filter.match(object)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public String toString() {
				return "OR" + Arrays.toString(filters);
			}
		};
		
		return orFilter;
	}
	
	public static <A> Match<A> inList(Collection<A> values) {
		final Set<A> set = new HashSet<A>(values);
		return new Match<A>() {
			public boolean match(A object) {
				return set.contains(object);
			}

			@Override
			public String toString() {
				return "inList" + set.toString();
			}
		};
	}	
}
