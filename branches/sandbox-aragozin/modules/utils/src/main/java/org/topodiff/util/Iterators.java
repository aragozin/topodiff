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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;



/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com
 */
public class Iterators {

	/**
	 * Add all objects from {@link Iterator} to {@link Collection} container.
	 */
	public static <T> void addAll(Collection<T> collection, Iterator<T> values) {
		while(values.hasNext()) {
			collection.add(values.next());
		}
	}

	/**
	 * Add all objects from {@link Iterable} to {@link Collection} container.
	 */
	public static <T> void addAll(Collection<T> collection, Iterable<T> values) {
		addAll(collection, values.iterator());
	}
	
	/**
	 * Wraps {@link Iterator} into {@link Iterable} interface, useful in 'for' loops.
	 */
	public static <T> Iterable<T> from(final Iterator<T> it) {
		return new Iterable<T>() {
			boolean ready = true;
			public Iterator<T> iterator() {
				if (!ready) {
					throw new IllegalStateException("Cannot call iterator() more than once");
				}
				ready = true;
				return it;
			}

			@Override
			public String toString() {
				return it.toString();
			}
		};
	}
	
	/**
	 * Equivalent to {@link #lines(text, new String[0)}.
	 */
	public static Iterable<String> lines(final String text) {
		return lines(text, new String[0]);
	}

	/**
	 * Creates a line iterator aware about escaping. <br />
	 * (For empty text, 0 lines will be returned)
	 * 
	 * @param text
	 *            text to iterate
	 * @param continuationChar
	 *            escape sequence that preserve next line end char as part of
	 *            string. (e.g. \ in java strings), may be <code>null</code>
	 * @param nlEscape
	 *            escape sequence to replace by \n in result string (e.g \n in
	 *            java strings), may be <code>null</code>
	 */
	public static Iterable<String> lines(final String text, final String[] nlEscapes) {
		return new Iterable<String>() {
			
			public Iterator<String> iterator() {
				return new LineIterator(text, nlEscapes);
			}
			
			@Override
			public String toString() {
				return text;
			}
		};
	}
	
	private static class LineIterator implements Iterator<String> {

		private final String text;
		private final String[] nlEscapes;
		private StringBuilder buf = new StringBuilder();
		private int pos = 0;
		
		public LineIterator(String text, String[] nlEscapes) {
			this.text = text;
			this.nlEscapes = nlEscapes;
		}

		public boolean hasNext() {
			return pos < text.length();
		}

		public String next() {
			if (hasNext()) {
				return readLine();
			}
			else {
				throw new NoSuchElementException();
			}
		}

		private String readLine() {
			while(pos < text.length()) {
				if (eatEscape()) {
					buf.append('\n');
				}
				else {
					char ch = text.charAt(pos);
					++pos;
					if (ch == '\n' || ch == '\r') {
						char nextCh = pos < text.length() ? text.charAt(pos) : ' ';
						if (nextCh != ch && (nextCh == '\n' || nextCh == '\r')) {
							++pos; // \n\r or \r\n
						}
						break;
					}
					else {
						buf.append(ch);
					}
				}
			}
			String line = buf.toString();
			buf.setLength(0);
			return line;
		}

		private boolean eatEscape() {
			char ch = text.charAt(pos);
			for(String escape: nlEscapes) {
				if (escape.charAt(0) == ch && pos + escape.length() < text.length()) {
					String subs = text.substring(pos, pos + escape.length());
					if (escape.equals(subs)) {
						pos += escape.length();
						return true;
					}
				}
			}
			return false;
		}

		public void remove() {
			throw new IllegalArgumentException();
		}
	}
	
	public static <A, B> Iterator<B> morph(final Iterator<A> it, final Morph<A, B> morph) {
		return new Iterator<B>() {

			public boolean hasNext() {
				return it.hasNext();
			}

			public B next() {
				return morph.morph(it.next());
			}

			public void remove() {
				it.remove();
			}
		};
	}
	
	public static <A, B> Iterable<B> morph(final Iterable<A> ia, final Morph<A, B> morph) {
		return new Iterable<B>(){
			public Iterator<B> iterator() {
				return morph(ia.iterator(), morph);
			}
		};
	}
	
	public static <A> Iterator<A> filter(final Iterator<A> it, final Match<A> filter) {
		return new Iterator<A>() {

			boolean exhausted = false; 
			A next;
			
			{
				fetchNext();
			}
			
			
			public boolean hasNext() {
				return !exhausted;
			}

			public A next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				A result = next;
				fetchNext();
				return result;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			private void fetchNext() {
				while(it.hasNext()) {
					next = it.next();
					if (filter.match(next)) {
						return;
					}
				}
				next = null;
				exhausted = true;
			}
		};
	}
	
	public static <A> Iterable<A> filter(final Iterable<A> ia, final Match<A> filter) {
		return new Iterable<A>(){
			public Iterator<A> iterator() {
				return filter(ia.iterator(), filter);
			}
		};
	}
	
	public static <A> LookAheadIterator<A> lookAhead(Iterator<A> it) {
		return new LookAheadIteratorImpl<A>(it);
	}
	
	private static class LookAheadIteratorImpl<T> implements LookAheadIterator<T> {
		private final Iterator<T> nested;
		private T next;
		private boolean hasNext;
		
		public LookAheadIteratorImpl(Iterator<T> nested) {
			this.nested = nested;
			fetchNext();
		}

		public T peekNext() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return next;
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
		
		private void fetchNext() {
			hasNext = nested.hasNext();
			next = hasNext ? nested.next() : null;
		}
	}
	
	public static <T> Iterator<List<T>> groupped(Iterator<T> it) {
		return new EqualsGrouppingIterator<T>(it);
	}

	public static <T> Iterable<List<T>> groupped(final Iterable<T> it) {
		return new Iterable<List<T>>() {
			public Iterator<List<T>> iterator() {
				return groupped(it.iterator());
			}
		};
	}

	public static <T> Iterator<List<T>> groupped(Iterator<T> it, Comparator<T> order) {
		return new ComparatorGrouppingIterator<T>(it, order);
	}

	public static <T> Iterable<List<T>> groupped(final Iterable<T> it, final Comparator<T> order) {
		return new Iterable<List<T>>() {
			public Iterator<List<T>> iterator() {
				return groupped(it.iterator(), order);
			}
		};
	}
	
	private static abstract class AbstractGrouppingIterator<T> extends AbstractPrefetchIterator<List<T>> {
	
		private final LookAheadIterator<T> nested;

		protected AbstractGrouppingIterator(Iterator<T> nested) {
			this.nested = lookAhead(nested);
			// sub clasess should call fetchNext() in constructors
		}
		
		protected void fetchNext() {
			if (nested.hasNext()) {
				hasNext = true;
				next = new ArrayList<T>();
				T first = nested.next();
				next.add(first);
				while(nested.hasNext()) {
					if (areEqual(first, nested.peekNext())) {
						next.add(nested.next());
					}
					else {
						break;
					}
				}
			}
			else {
				next = null;
				hasNext = false;
			}
		}
		
		protected abstract boolean areEqual(T t1, T t2);
	}
	
	private static class ComparatorGrouppingIterator<T> extends AbstractGrouppingIterator<T> {
		
		private final Comparator<? super T> order;

		public ComparatorGrouppingIterator(Iterator<T> nested, Comparator<? super T> order) {
			super(nested);
			this.order = order;
			
			fetchNext();
		}
		
		protected boolean areEqual(T t1, T t2) {
			return order.compare(t1, t2) == 0;
		}
	}	

	private static class EqualsGrouppingIterator<T> extends AbstractGrouppingIterator<T> {
		
		public EqualsGrouppingIterator(Iterator<T> nested) {
			super(nested);
			
			fetchNext();
		}
		
		protected boolean areEqual(T t1, T t2) {
			return (t1 == null && t2 == null) || (t1 != null && t1.equals(t2));
		}
	}

	/**
	 * Important! algorithm assumes sorted collections on input. 
	 * 
	 * @param t1 iterator over first sorted collection
	 * @param t2 iterator over second sorted collection
	 * @return iterator over diff between collections
	 */	
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<?>> Iterator<Pair<List<T>, List<T>>> sortedDelta(Iterator<T> t1, Iterator<T> t2) {
		return sortedDelta(t1, t2, (Comparator)Comparators.NATURAL_ORDER);
	}

	/**
	 * Important! algorithm assumes sorted collections on input. 
	 * 
	 * @param t1 iterator over first sorted collection
	 * @param t2 iterator over second sorted collection
	 * @param order
	 * @return iterator over diff between collections
	 */
 	public static <T> Iterator<Pair<List<T>, List<T>>> sortedDelta(Iterator<T> t1, Iterator<T> t2, Comparator<T> order) {
 		return new SortedDeltaIterator<T>(t1, t2, order);
 	}

 	/**
 	 * Important! algorithm assumes sorted collections on input.
 	 *  
 	 * @param t1 iterable over first sorted collection
 	 * @param t2 iterable over second sorted collection
 	 * @return iterable over diff between collections
 	 */
 	@SuppressWarnings("unchecked")
	public static <T extends Comparable<?>> Iterable<Pair<List<T>, List<T>>> sortedDelta(Iterable<T> t1, Iterable<T> t2) {
		return sortedDelta(t1, t2, (Comparator)Comparators.NATURAL_ORDER);
 	}

 	/**
 	 * Important! algorithm assumes sorted collections on input.
 	 *  
 	 * @param t1 iterable over first sorted collection
 	 * @param t2 iterable over second sorted collection
 	 * @param order
 	 * @return iterable over diff between collections
 	 */
 	public static <T> Iterable<Pair<List<T>, List<T>>> sortedDelta(final Iterable<T> t1, final Iterable<T> t2, final Comparator<T> order) {
 		return new Iterable<Pair<List<T>,List<T>>>() {
			public Iterator<Pair<List<T>, List<T>>> iterator() {
				return sortedDelta(t1.iterator(), t2.iterator(), order);
			}
		};
 	}
	
	private static class SortedDeltaIterator<T> extends AbstractPrefetchIterator<Pair<List<T>, List<T>>> {
		private final LookAheadIterator<List<T>> it1;
		private final LookAheadIterator<List<T>> it2;
		private final Comparator<T> order;
		
		public SortedDeltaIterator(Iterator<T> it1, Iterator<T> it2, Comparator<T> order) {
			this.it1 = lookAhead(groupped(it1, order));
			this.it2 = lookAhead(groupped(it2, order));
			this.order = order;
			if (order == null) {
				throw new IllegalArgumentException("Should provide valid comparator");
			}
			
			fetchNext();
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void fetchNext() {
			if (it1.hasNext() || it2.hasNext()) {
				hasNext = true;
				
				if (!it1.hasNext()) {
					next = new Pair<List<T>, List<T>>(Collections.EMPTY_LIST, it2.next());
					return;
				}
				if (!it2.hasNext()) {
					next = new Pair<List<T>, List<T>>(it1.next(), Collections.EMPTY_LIST);
					return;
				}
				
				T h1 = it1.peekNext().get(0);
				T h2 = it2.peekNext().get(0);
				
				int res = order.compare(h1, h2);
				if (res < 0) {
					next = new Pair<List<T>, List<T>>(it1.next(), Collections.EMPTY_LIST);
				}
				else if (res > 0) {
					next = new Pair<List<T>, List<T>>(Collections.EMPTY_LIST, it2.next());
				}
				else {
					next = new Pair<List<T>, List<T>>(it1.next(), it2.next());
				}
			}
			else {
				next = null;
				hasNext = false;
			}
		}
	}
	
	public static <T> Iterator<T> sortedUnique(Iterator<T> it) {
		return new SortedUniqueIterator<T>(it, null);
	}

	public static <T> Iterable<T> sortedUnique(final Iterable<T> it) {
		return new Iterable<T>() {			
			@Override
			public Iterator<T> iterator() {
				return sortedUnique(it.iterator());
			}
		};
	}

	public static <T> Iterator<T> sortedUnique(Iterator<T> it, Comparator<T> comparator) {
		return new SortedUniqueIterator<T>(it, comparator);
	}
	
	public static <T> Iterable<T> sortedUnique(final Iterable<T> it, final Comparator<T> comparator) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return sortedUnique(it.iterator(), comparator);
			}
		};
	}
}
