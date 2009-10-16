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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;

import org.junit.Ignore;
import org.topodiff.graph.Node;
import org.topodiff.graph.NodeType;
import org.topodiff.graph.Triple;
import org.topodiff.io.TripleIterator;

/**
 * Copy-paste of {@code NTripleFormatReader} for tests  
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@Ignore
class TestNTripleFormatReader implements TripleIterator {

	private BufferedReader reader;
	
	private Node subj;
	private Node verb;
	private Node obj;
	
	public TestNTripleFormatReader(Reader reader) {
		if (reader instanceof BufferedReader) {
			this.reader = (BufferedReader) reader;
		}
		else {
			this.reader = new BufferedReader(reader);
		}
	}

	public boolean hasNext() {
		if (subj == null) {
			fetchNextStatement();
		}		
		return subj != null;
	}

	public Triple next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		Triple stmt = new Triple(subj, verb, obj);
		fetchNextStatement();
		return stmt;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

    private void fetchNextStatement() {
		try {
		
			while(reader != null) {
				String line = reader.readLine();
	
				if (line == null) {
					subj = null;
					verb = null;
					obj = null;
					reader.close();
					reader = null;
					return;
				}
				else if (line.trim().length() == 0 || line.trim().charAt(0) == '#') {
					continue;
				}
				
				StringStream ss = new StringStream(line);
				
				try {
					skipWhiteSpace(ss);
					subj = readResource(ss);
					skipWhiteSpace(ss);
					verb = readProperty(ss); 
					skipWhiteSpace(ss);
					obj = readNode(ss);
					skipWhiteSpace(ss);
					expect(ss, ".");
					
					break;
				}
				catch(IllegalArgumentException e) {
					System.err.println("Failed to parse line: " + line);
					throw e;
				}
			}
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Node readProperty(StringStream ss) {
		Node res = readResource(ss);
		return res;
	}

	private Node asBNode(String name) {
		return new Node(NodeType.BLANK, name, this);
	}

	private Node asResource(String uri) {
		return new Node(NodeType.NAMED, uri, this);
	}

	
    private Node readResource(StringStream ss)  {
        char inChar = ss.readChar();

        if (inChar == '_') { // anon resource
            expect(ss, ":");
            String name = readName(ss);
            if (name == null) {
                syntaxError("expected bNode label");
                return null;
            }
            return asBNode(name);
        } else if (inChar == '<') { // uri
            String uri = readURI(ss);
            inChar = ss.readChar();
            if (inChar != '>') {
                syntaxError("expected '>'");
                return null;
            }
            return asResource(uri);
        } else {
            syntaxError("unexpected input");
            return null;
        }
    }

	private Node readNode(StringStream ss)  {
        skipWhiteSpace(ss);
        switch (ss.nextChar()) {
            case '"' :
                return readLiteral(ss, false);
            case 'x' :
                return readLiteral(ss, true);
            case '<' :
            case '_' :
                return readResource(ss);
            default :
                syntaxError("unexpected input");
                return null;
        }
    }

    private Node readLiteral(StringStream ss, boolean wellFormed)  {

        StringBuffer lit = new StringBuffer();

        if (wellFormed) {
            expect(ss, "xml");
        }

        expect(ss, "\"");

        while (true) {
            char inChar = ss.readChar();
            if (inChar == '\\') {
                char c = ss.readChar();
                if (ss.eol()) {
                    return null;
                }
                if (c == 'n') {
                    inChar = '\n';
                } else if (c == 'r') {
                    inChar = '\r';
                } else if (c == 't') {
                    inChar = '\t';
                } else if (c == '\\' || c == '"') {
                    inChar = c;
                } else if (c == 'u') {
                    inChar = readUnicode4Escape(ss);
                } else {
                    syntaxError("illegal escape sequence '" + c + "'");
                    return null;
                }
            } else if (inChar == '"') {
                String lang;
                if ('@' == ss.nextChar()) {
                    expect(ss, "@");
                   lang = readLang(ss);
                } else if ('-' == ss.nextChar()) {
                    expect(ss, "-");
                    lang = readLang(ss);
                } else {
                    lang = "";
                }
                if (wellFormed) {
                    return new Node(NodeType.LITERAL, lit.toString(), this);
                } else if ('^' == ss.nextChar()) {
                    String datatypeURI = null;
                    expect(ss,"^^<");
                    datatypeURI = readURI(ss);
                    expect(ss, ">");
                    
                    return new Node(NodeType.LITERAL, lit.toString() + "^^<" + datatypeURI + ">", this);
                } else {
                    return new Node(NodeType.LITERAL, lit.toString() + "@" + lang, this);
                }
            }
            lit = lit.append(inChar);
        }
    }

    private char readUnicode4Escape(StringStream ss) {
        char buf[] =
            new char[] {
                ss.readChar(),
                ss.readChar(),
                ss.readChar(),
                ss.readChar()};
        try {
            return (char) Integer.parseInt(new String(buf), 16);
        } catch (NumberFormatException e) {
            syntaxError("bad unicode escape sequence");
            return 0;
        }
    }
    
    private String readLang(StringStream ss) {
        StringBuffer lang = new StringBuffer(15);


        while (true) {
            char inChar = ss.nextChar();
            if (Character.isWhitespace(inChar) || inChar == '.' || inChar == '^')
                return lang.toString();
            lang = lang.append(ss.readChar());
        }
    }
    
    private String readURI(StringStream ss) {
        StringBuffer uri = new StringBuffer();

        while (ss.nextChar() != '>') {
            char inChar = ss.readChar();

            if (inChar == '\\') {
                expect(ss, "u");
                inChar = readUnicode4Escape(ss);
            }
            uri = uri.append(inChar);
        }
        return uri.toString();
    }
    
    private String readName(StringStream ss) {
        StringBuffer name = new StringBuffer();

        while (!Character.isWhitespace(ss.nextChar())) {
            name = name.append(ss.readChar());
        }
        return name.toString();
    }
    
    private void skipWhiteSpace(StringStream ss) {
	    while (Character.isWhitespace(ss.nextChar()) || ss.nextChar() == '#') {
	        char inChar = ss.readChar();
	        if (ss.eol()) {
	            return;
	        }
	        if (inChar == '#') {
	            while (inChar != '\n') {
	                inChar = ss.readChar();
	                if (ss.eol()) {
	                    return;
	                }
	            }
	        }
	    }
	}

	private void expect(StringStream ss, String str) {
        for (int i = 0; i < str.length(); i++) {
            char want = str.charAt(i);

            char inChar = ss.readChar();

            if (inChar != want) {
                syntaxError("expected \"" + str + "\"");
            }
        }
    }
    
    private void syntaxError(String string) {
		throw new IllegalArgumentException(string);
	}

	private static class StringStream {

        private final String text;
        private int charIndex;

        public StringStream(String text) {
        	this.text = text;        	
        }

        public char readChar() {
        	if (charIndex >= text.length()) {
        		throw new IllegalArgumentException("Premature end of line '" + text  + "'");
        	}
        	else {
        		return text.charAt(charIndex++);
        	}
        }

        public char nextChar() {
        	if (charIndex >= text.length()) {
        		return '\000';
        	}
        	else {
        		return text.charAt(charIndex);
        	}
        }

        public boolean eol() {
            return charIndex >= text.length();
        }
    }    
}
