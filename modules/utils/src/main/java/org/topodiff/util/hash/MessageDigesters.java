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
package org.topodiff.util.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class MessageDigesters {

	public static MessageDigest createSHA1() {
		try {
			return MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("No SHA1 algorithm available");
		}		
	}

	public static MessageDigest createSimpleHash32() {
		return new SimpleHash32Digester();
	}

	public static MessageDigest createSimpleHash64() {
		return new SimpleHash64Digester();
	}
	
	static class SimpleHash64Digester extends MessageDigest implements Cloneable {

		private long hashValue = 0;
		
		public SimpleHash64Digester() {
			super("simple-hash-64");
		}
		
		@Override
		protected byte[] engineDigest() {
			byte[] result = new byte[8];
			result[0] = (byte) (hashValue >> 56);
			result[1] = (byte) (hashValue >> 48);
			result[2] = (byte) (hashValue >> 40);
			result[3] = (byte) (hashValue >> 32);
			result[4] = (byte) (hashValue >> 24);
			result[5] = (byte) (hashValue >> 16);
			result[6] = (byte) (hashValue >> 8);
			result[7] = (byte) hashValue;
			return result;
		}

		@Override
		protected void engineReset() {
			hashValue = 0;
		}

		@Override
		protected void engineUpdate(byte input) {
			hashValue = 15 * hashValue + input;
		}

		@Override
		protected void engineUpdate(byte[] input, int offset, int len) {
			for(int i = offset; i != offset + len; ++i) {
				engineUpdate(input[i]);
			}
		}
	}
	
	static class SimpleHash32Digester extends MessageDigest implements Cloneable {

		private int hashValue = 0;
		
		public SimpleHash32Digester() {
			super("simple-hash-32");
		}
		
		@Override
		protected byte[] engineDigest() {
			byte[] result = new byte[4];
			result[0] = (byte) (hashValue >> 24);
			result[1] = (byte) (hashValue >> 16);
			result[2] = (byte) (hashValue >> 8);
			result[3] = (byte) hashValue;
			return result;
		}

		@Override
		protected void engineReset() {
			hashValue = 0;
		}

		@Override
		protected void engineUpdate(byte input) {
			hashValue = 15 * hashValue + input;
		}

		@Override
		protected void engineUpdate(byte[] input, int offset, int len) {
			for(int i = offset; i != offset + len; ++i) {
				engineUpdate(input[i]);
			}
		}
	}
}
