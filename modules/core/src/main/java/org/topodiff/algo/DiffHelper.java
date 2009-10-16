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
package org.topodiff.algo;


import org.topodiff.graph.Node;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com
 */
class DiffHelper {

	/**
	 * Assumed format of label {@code b_<hash>x<graphNo>_<nodeNo>}
	 * MAYBE: use regex?
	 * 
	 * @return {@code <hash>} part of label or null, if format is different
	 */
	public static String getIsomorphicsHash(Node res) {
		if (res.isAnon()) {
			String id = res.getLexicalForm();
			int p = id.indexOf("b_");
			if (p < 0) {
				return null;
			}
			int e = id.indexOf('x', p);
			if (e < 0) {
				return null;
			}
			return id.substring(p + 2, e);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Assumed format of label {@code b_<hash>x<graphNo>_<nodeNo>}
	 * MAYBE: use regex?
	 * 
	 * @return {@code <hash>x<Id>_<graphNo>} part of label or null, if format is different
	 */
	public static String getIsomorphicsGroupID(Node res) {
		if (res.isAnon()) {
			String id = res.getLexicalForm();
			int p = id.indexOf("b_");
			if (p < 0) {
				return null;
			}
			int e = id.lastIndexOf('_');
			if (e < 0) {
				return null;
			}
			return id.substring(p + 2, e);
		}
		else {
			return null;
		}
	}

	/**
	 * Assumed format of label {@code b_<hash>x<graphNo>_<nodeNo>}
	 * MAYBE: use regex?
	 * 
	 * @return {@code <nodeNo>} part of label or null, if format is different
	 */
	public static String getClusterNodeNo(Node res) {
		if (res.isAnon()) {
			String id = res.getLexicalForm();
			int e = id.lastIndexOf('_');
			if (e < 0) {
				return null;
			}
			return id.substring(e + 1);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Assumed format of label {@code dangling_<nodeNo> }.
	 * @param res
	 * @return
	 */
	public static boolean isDanglingNode(Node res) {
		if (res.isAnon() && res.getLexicalForm().startsWith("dangling_")) {
			return true;
		}
		else {
			return false;
		}
	}
}
