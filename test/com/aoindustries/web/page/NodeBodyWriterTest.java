package com.aoindustries.web.page;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author orion
 */
public class NodeBodyWriterTest {

	private static final String TEST_BODY_PREFIX = "<TestNode>Test body <";
	private static final String TEST_ELEMENT_BODY = "<TestElement />";
	private static final String TEST_BODY_SUFFIX =
		"<" + NodeBodyWriter.MARKER_PREFIX + "ffffffffffffffff" + NodeBodyWriter.MARKER_SUFFIX
		+ "</TestNode>"
		+ NodeBodyWriter.MARKER_PREFIX + "ffffffff";

	private static final String TEST_EXPECTED_RESULT = TEST_BODY_PREFIX + TEST_ELEMENT_BODY + TEST_BODY_SUFFIX;

	private static Node testNode;
	private static String testNodeBody;

	private static final ElementContext nullElementContext = new ElementContext() {
		@Override
		public void include(String resource, Writer out) throws IOException {
			// Do nothing
		}
	};

	@BeforeClass
	public static void setUpClass() throws IOException {
		testNode = new Node() {
			@Override
			public String getLabel() {
				return "Test Node";
			}
			@Override
			public String getListItemCssClass() {
				return "test_item";
			}
		};
		Long elementKey = testNode.addChildElement(
			new Element() {
				@Override
				public String getLabel() {
					return "Test Element";
				}
				@Override
				protected String getDefaultIdPrefix() {
					return "test";
				}
				@Override
				public String getListItemCssClass() {
					return "test_element";
				}
			},
			(out, context) -> out.write(TEST_ELEMENT_BODY)
		);
		StringBuilder SB = new StringBuilder();
		SB.append(TEST_BODY_PREFIX);
		NodeBodyWriter.writeElementMarker(elementKey, SB);
		SB.append(TEST_BODY_SUFFIX);
		testNodeBody = SB.toString();
	}

	@AfterClass
	public static void tearDownClass() {
		testNode = null;
	}

	@Test
	public void testWriteElementMarker() throws Exception {
		//System.out.println(testNodeBody);
		//System.out.flush();
		final char[] testNodeBodyChars = testNodeBody.toCharArray();
		final int testNodeBodyLen = testNodeBody.length();
		for(int writeLen = 1; writeLen <= testNodeBodyLen; writeLen++) {
			for(int off = 0; off < writeLen; off++) {
				StringWriter out = new StringWriter(TEST_EXPECTED_RESULT.length());
				try {
					try (NodeBodyWriter writer = new NodeBodyWriter(testNode, out, nullElementContext)) {
						writer.write(testNodeBodyChars, 0, off);
						for(int pos = off; pos < testNodeBodyLen; pos += writeLen) {
							int end = pos + writeLen;
							if(end > testNodeBodyLen) end = testNodeBodyLen;
							int len = end - pos;
							assertTrue(len >= 0);
							assertTrue((pos + len) <= testNodeBodyLen);
							writer.write(testNodeBodyChars, pos, len);
						}
					}
				} finally {
					out.close();
				}
				assertEquals(TEST_EXPECTED_RESULT, out.toString());
			}
		}
	}
}
