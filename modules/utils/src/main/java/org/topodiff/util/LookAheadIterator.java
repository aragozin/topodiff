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

/**
 * Implements {@link ModelDifferentiator} interface, using provided {@link ModelArchive} and {@link ModelDiffAlgo}.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com
 */
public interface LookAheadIterator<T> extends Iterator<T> {

	public T peekNext();
	
}
