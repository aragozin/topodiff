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

import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.topodiff.graph.GraphView;
import org.topodiff.graph.Node;
import org.topodiff.graph.NodeType;
import org.topodiff.io.TripleReceiver;
import org.topodiff.algo.ToposortGraphProcessor;
import org.topodiff.util.Filters;
import org.topodiff.util.hash.MessageDigesters;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class DiffProcessorTest {

	private final static Set<Node> DEFAULT_PROPS_TO_FOLLOW = new HashSet<Node>();
	static {
		DEFAULT_PROPS_TO_FOLLOW.add(new Node(NodeType.NAMED, "arrow"));
	}

	static interface GraphSortingLaw {
		public void sort(GraphView model, TripleReceiver tripleWriter);
	}
	
	
	private final static GraphSortingLaw ALGO = new GraphSortingLaw() {
		public void sort(GraphView model, TripleReceiver tripleWriter) {
			MessageDigest hashAlgo = MessageDigesters.createSimpleHash32();
//			MessageDigest hashAlgo = MessageDigesters.createSHA1();
			
			ToposortGraphProcessor adapter = new ToposortGraphProcessor(tripleWriter, Filters.inList(DEFAULT_PROPS_TO_FOLLOW), hashAlgo);
			adapter.process(model);
		}

		public String getAlgorithmFingerPrint() {
			throw new UnsupportedOperationException("test only");
		}
	};
	

	@Test
	public void test1() {
		String[] source1 = {
				"_:a <x> _:b",
				"_:b <x> _:c",
				"_:c <x> _:a",
		};
		String[] source2 = {
				"_:a <x> _:b",
				"_:b <x> _:c",
				"_:c <x> _:a",
		};
		
		TestHelper.testDiffSummary(source1, source2, ALGO, 0);
	}

	@Test
	public void test2() {
		String[] source1 = {
				"_:a <arrow> _:b",
				"_:b <arrow> _:c",
				"_:c <arrow> _:a",
		};
		String[] source2 = {
				"_:a <arrow> _:b",
				"_:b <arrow> _:c",
				"_:c <arrow> _:a",
		};
		
		TestHelper.testDiffSummary(source1, source2, ALGO, 0);
	}
	
	@Test
	public void test3() {
		String[] source1 = {
				"_:a <arrow> _:b",
				"_:b <arrow> _:c",
				"_:c <arrow> _:a",
				"_:a <no> \"123\"",
		};
		String[] source2 = {
				"_:a <arrow> _:b",
				"_:b <arrow> _:c",
				"_:c <arrow> _:a",
				"_:b <no> \"123\"",
		};
		
		TestHelper.testDiffSummary(source1, source2, ALGO, 0);
	}

	@Test
	public void test4() {
		String[] source1 = {
				"_:a <arrow> _:b",
				"_:b <arrow> _:c",
				"_:c <arrow> _:a",
				"_:a <no> \"123\"",
				"_:c <no> \"123\"",
		};
		String[] source2 = {
				"_:a <arrow> _:b",
				"_:b <arrow> _:c",
				"_:c <arrow> _:a",
				"_:b <no> \"123\"",
				"_:d <no> \"123\"",
		};
		
		TestHelper.testDiffSummary(source1, source2, ALGO, 10);
	}

	@Test
	public void test5() {
		String[] source1 = {
				"_:a <arrow> _:b",
				"_:b <arrow> _:c",
				"_:c <arrow> _:a",
				"_:a <no> \"123\"",
		};
		String[] source2 = {
				"_:a <arrow> _:b",
				"_:b <arrow> _:c",
				"_:c <arrow> _:a",
				"_:b <no> \"345\"",
		};
		
		TestHelper.testDiffSummary(source1, source2, ALGO, 8);
	}

	@Test
	public void test6() {
		String[] source1 = {
				"_:a <arrow> _:b",
				"_:b <arrow> _:c",
				"<abc> <x> _:a",
		};
		String[] source2 = {
				"_:a <arrow> _:c",
				"_:b <arrow> _:a",
				"<abc> <x> _:b",
		};
		
		TestHelper.testDiffSummary(source1, source2, ALGO, 0);
	}

	@Test
	public void test7() {
		String[] source1 = {
				"_:a <arrow> _:b",
				"_:b <arrow> _:c",
				"_:c <arrow> \"nil\"",
				"<abc> <x> _:a",
		};
		String[] source2 = {
				"_:A <arrow> _:B",
				"_:B <arrow> _:C",
				"_:C <arrow> \"nil\"",
				"<123> <x> _:A",
		};
		
		TestHelper.testDiffSummary(source1, source2, ALGO, 2);
	}

	@Test
	public void test8() {
		String[] source1 = {
				"_:a <arrow> _:b",
				"_:b <arrow> _:c",
				"_:c <arrow> \"nil\"",
				"_:1 <arrow> _:2",
				"_:2 <arrow> _:3",
				"_:3 <arrow> \"nil\"",
				"<abc> <x> _:a",
				"<xyz> <x> _:1",
		};
		String[] source2 = {
				"_:A <arrow> _:B",
				"_:B <arrow> _:C",
				"_:C <arrow> \"nil\"",
				"_:1 <arrow> _:2",
				"_:2 <arrow> _:3",
				"_:3 <arrow> \"nil\"",
				"<abc> <x> _:1",
				"<xyz> <x> _:A",
		};
		
		TestHelper.testDiffSummary(source1, source2, ALGO, 0);
	}

	
}
