/*
 * Copyright 2010-2011 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.excilys.ebi.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author <a href="mailto:dvilleneuve@excilys.com">Damien VILLENEUVE</a>
 * @version 1.0
 */
public class BitInputStream extends FilterInputStream {
	private int bitsLeft;
	private int bitsCountLeft;

	public BitInputStream(InputStream in) {
		super(in);
		clearBuffer();
	}

	/**
	 * <p>
	 * Reads the next byte of data from this input stream. The value byte is
	 * returned as an <code>int</code> in the range <code>0</code> to
	 * <code>255</code>. If no byte is available because the end of the stream;
	 * has been reached, the value <code>-1</code> is returned. This method
	 * blocks until input data is available, the end of the stream is detected,
	 * or an exception is thrown.
	 * </p>
	 * <p>
	 * Ignore and clear the current buffered byte. So some bits would be vanish
	 * with this method.
	 * </p>
	 * 
	 * @return the next byte of data, or <code>-1</code> if the end of the
	 *         stream is reached.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public int read() throws IOException {
		clearBuffer();
		return super.read();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Ignore and clear the current buffered byte. So some bits would be vanish
	 * with this method.
	 * </p>
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		clearBuffer();
		return super.read(b, off, len);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Ignore and clear the current buffered byte. So some bits would be vanish
	 * with this method.
	 * </p>
	 */
	@Override
	public long skip(long n) throws IOException {
		clearBuffer();
		return super.skip(n);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Ignore and clear the current buffered byte. So some bits would be vanish
	 * with this method.
	 * </p>
	 */
	@Override
	public synchronized void reset() throws IOException {
		clearBuffer();
		super.reset();
	}

	/**
	 * <p>
	 * Read the next n-bits on the stream.
	 * </p>
	 * 
	 * @param nbits
	 *            to read
	 * @return the next n bits of data, or <code>-1</code> if the end of the
	 *         stream is reached.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws IllegalArgumentException
	 *             if nbits is not between 0 and 32
	 */
	public int readBits(int nbits) throws IOException {
		if (nbits <= 0 || nbits > 32) {
			throw new IllegalArgumentException();
		}

		// Try to read the first bit. return -1 in case of EOF
		int bit = readBit();
		if (bit < 0) {
			return -1;
		}

		int res = bit;
		for (int i = 1; i < nbits; i++) {
			bit = readBit();
			if (bit < 0) {
				break;
			} else {
				res = res << 1 | bit;
			}
		}
		return res;
	}

	/**
	 * <p>
	 * Read the next n-bits on the stream and fill an array with it. The size of
	 * this array is nbits to the maximum, or the number of left bits on the
	 * stream if there is not enough bits to fill the array.
	 * </p>
	 * 
	 * @param nbits
	 *            to read
	 * @return an array filled with the next n bits of data, or an empty array
	 *         if the end of the stream is reached.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws IllegalArgumentException
	 *             if nbits is < 0
	 */
	public int[] readBitsToArray(int nbits) throws IOException {
		if (nbits <= 0) {
			throw new IllegalArgumentException();
		}

		// Try to read the first bit. return -1 in case of EOF
		int bit = readBit();
		if (bit < 0) {
			return new int[] {};
		}

		int[] res = new int[nbits];
		res[0] = bit;
		for (int i = 1; i < nbits; i++) {
			bit = readBit();
			if (bit < 0) {
				return Arrays.copyOf(res, i);
			} else {
				res[i] = bit;
			}
		}
		return res;
	}

	/**
	 * <p>
	 * Read the next byte if it's not already buffered and return the next bit
	 * of the stream. If the buffered byte is empty, reload an other one.
	 * </p>
	 * <p>
	 * <b>NB</b> : The next call to {@link #read()} will not read the end of
	 * this buffered byte.
	 * </p>
	 * 
	 * @return the next bit of data, or <code>-1</code> if the end of the stream
	 *         is reached.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public int readBit() throws IOException {
		if (bitsCountLeft == 0) {
			bitsLeft = super.read();
			bitsCountLeft = 8;

			if (bitsLeft < 0) {
				bitsCountLeft = 0;
				return -1;
			}
		}

		return (bitsLeft & mask(--bitsCountLeft)) >> bitsCountLeft;
	}

	/**
	 * Clear the buffered byte.
	 */
	private void clearBuffer() {
		bitsLeft = 0;
		bitsCountLeft = 0;
	}

	/**
	 * <p>
	 * Create a mask used to select a specidied bit in a number.
	 * </p>
	 * <p>
	 * For example : if <i>bitNumber</i> is 5, this method will return 16
	 * (000<b>1</b>0000)
	 * </p>
	 * 
	 * @param bitNumber
	 * @return an integer with only one 1 at the specified position
	 * @throws IllegalArgumentException
	 *             if bitNumber is not between 0 and 8
	 */
	private static byte mask(int bitNumber) {
		if (bitNumber < 0 || bitNumber > 8) {
			throw new IllegalArgumentException();
		}
		return (byte) (1 << bitNumber);
	}
}
