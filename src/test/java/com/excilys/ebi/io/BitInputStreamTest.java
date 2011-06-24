package com.excilys.ebi.io;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BitInputStreamTest {
	private BitInputStream bis;
	private FileInputStream fis;

	@Before
	public void setUp() throws Exception {
		try {
			fis = new FileInputStream(new File("src/test/resources/test.txt"));
			bis = new BitInputStream(fis);
		} catch (Exception e) {
			throw e;
		}
	}

	@After
	public void tearDown() throws Exception {
		try {
			bis.close();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
				throw e;
			}
		}

	}

	@Test(expected = IllegalArgumentException.class)
	public void testReadBits_negative() throws IOException {
		bis.readBits(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReadBits_tooHigh() throws IOException {
		bis.readBits(33);
	}

	@Test
	public void testReadBits_EOF() throws IOException {
		bis.skip(4);

		int bit = bis.readBits(1);
		assertTrue(bit == -1);
	}

	@Test
	public void testReadBits_3bits() throws IOException {
		int[] bits = new int[11];
		for (int i = 0; i < bits.length; i++) {
			bits[i] = bis.readBits(3);
		}

		// Text of the file is "test", so expected stream are :
		// 011 101 000 110 010 101 110 011 011 101 00
		// So : 3 5 0 6 2 5 6 3 3 5 0
		int[] expectedValues = new int[] { 3, 5, 0, 6, 2, 5, 6, 3, 3, 5, 0 };
		for (int i = 0; i < expectedValues.length; i++) {
			assertTrue(expectedValues[i] == bits[i]);
		}
	}

	@Test
	public void testReadBits_8bits() throws IOException {
		int[] bits = new int[32];
		for (int i = 0; i < bits.length; i++) {
			bits[i] = bis.readBits(8);
		}

		// Text of the file is "test", so expected values are :
		// 01110100 01100101 01110011 01110100
		// So : 116 101 115 116
		int[] expectedValues = new int[] { 116, 101, 115, 116 };
		for (int i = 0; i < expectedValues.length; i++) {
			assertTrue(expectedValues[i] == bits[i]);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReadBitsToArray_negative() throws IOException {
		bis.readBitsToArray(-1);
	}

	@Test
	public void testReadBitsToArray_EOF() throws IOException {
		bis.skip(4);

		int[] bit = bis.readBitsToArray(1);
		assertTrue(bit.length == 0);
	}

	@Test
	public void testReadBitsToArray_8bits() throws IOException {
		int[] bits = bis.readBitsToArray(8);

		assertTrue(bits.length == 8);

		// Text of the file is "test", so expected values are :
		// 01110100 01100101 01110011 01110100
		String expectedValues = "01110100";
		for (int i = 0; i < expectedValues.length(); i++) {
			int expectedBit = Integer.valueOf(expectedValues.substring(i, i + 1));
			assertTrue(expectedBit == bits[i]);
		}
	}

	@Test
	public void testReadBitsToArray_11bits() throws IOException {
		int[] bits = bis.readBitsToArray(11);

		assertTrue(bits.length == 11);

		// Text of the file is "test", so expected values are :
		// 01110100 01100101 01110011 01110100
		String expectedValues = "01110100011";
		for (int i = 0; i < expectedValues.length(); i++) {
			int expectedBit = Integer.valueOf(expectedValues.substring(i, i + 1));
			assertTrue(expectedBit == bits[i]);
		}
	}

	@Test
	public void testReadBitsToArray_51bits() throws IOException {
		int[] bits = bis.readBitsToArray(51);

		assertTrue(bits.length == 32);

		// Text of the file is "test", so expected values are :
		// 01110100 01100101 01110011 01110100
		String expectedValues = "01110100011001010111001101110100";
		for (int i = 0; i < expectedValues.length(); i++) {
			int expectedBit = Integer.valueOf(expectedValues.substring(i, i + 1));
			assertTrue(expectedBit == bits[i]);
		}
	}

	@Test
	public void testReadBit_FullStream() throws IOException {
		int[] bits = new int[32];
		for (int i = 0; i < bits.length; i++) {
			bits[i] = bis.readBit();
		}

		// Text of the file is "test", so expected binary stream is :
		// 01110100 01100101 01110011 01110100
		String expectedStream = "01110100011001010111001101110100";
		for (int i = 0; i < expectedStream.length(); i++) {
			int expectedBit = Integer.valueOf(expectedStream.substring(i, i + 1));
			assertTrue(expectedBit == bits[i]);
		}
	}

	@Test
	public void testReadBit_EOF() throws IOException {
		bis.skip(4);

		int bit = bis.readBit();
		assertTrue(bit == -1);
	}
	
	@Test
	public void testRead_AfterRead4Bits() throws IOException {
		bis.readBits(4);
		int nextByte = bis.read();
		
		// Text of the file is "test", so expected byte is :
		// e = 01100101 = 101
		assertTrue(nextByte == 101);
	}
	
	@Test
	public void testRead_AfterRead8Bits() throws IOException {
		bis.readBits(4);
		int nextByte = bis.read();
		
		// Text of the file is "test", so expected byte is :
		// e = 01100101 = 101
		assertTrue(nextByte == 101);
	}

	@Test
	public void testReset_EOFThenReset() throws IOException {
		if (bis.markSupported()) {
			bis.skip(4);
			bis.reset();

			testReadBit_FullStream();
		}
	}
}
