/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.BitSet;

import org.junit.Test;

/**
 * Unit tests for {@link ExecutionData}.
 */
public class ExecutionDataTest {

	@Test
	public void testCreateEmpty() {
		final ExecutionData e = new ExecutionData(5, "Example", 3);
		assertEquals(5, e.getId());
		assertEquals("Example", e.getName());
		assertEquals(3, e.getData().length);
		assertNull(e.getData()[0]);
		assertNull(e.getData()[1]);
		assertNull(e.getData()[2]);
	}

	@Test
	public void testGetters() {
		final BitSet[] data = new BitSet[0];
		final ExecutionData e = new ExecutionData(5, "Example", data);
		assertEquals(5, e.getId());
		assertEquals("Example", e.getName());
		assertSame(data, e.getData());
	}

	@Test
	public void testReset() {
		final ExecutionData e = new ExecutionData(5, "Example", new BitSet[] {
				createBitSet(1), null, createBitSet(2) });
		e.reset();
		assertNull(e.getData()[0]);
		assertNull(e.getData()[1]);
		assertNull(e.getData()[2]);
	}

	@Test
	public void testMerge() {
		final ExecutionData a = new ExecutionData(5, "Example", new BitSet[] {
				null, createBitSet(1), null, createBitSet(2) });
		final ExecutionData b = new ExecutionData(5, "Example", new BitSet[] {
				null, null, createBitSet(3), createBitSet(4) });
		a.merge(b);

		// b is merged into a:
		assertNull(a.getData()[0]);
		assertEquals(1, a.getData()[1].cardinality());
		assertEquals(1, a.getData()[2].cardinality());
		assertEquals(2, a.getData()[3].cardinality());

		// b must not be modified:
		assertNull(b.getData()[0]);
		assertNull(b.getData()[1]);
		assertEquals(1, b.getData()[2].cardinality());
		assertEquals(1, b.getData()[3].cardinality());
	}

	@Test
	public void testAssertCompatibility() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new BitSet[] { createBitSet(1) });
		a.assertCompatibility(5, "Example", 1);
	}

	@Test(expected = IllegalStateException.class)
	public void testAssertCompatibilityNegative1() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new BitSet[] { createBitSet(1) });
		a.assertCompatibility(55, "Example", 1);
	}

	@Test(expected = IllegalStateException.class)
	public void testAssertCompatibilityNegative2() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new BitSet[] { createBitSet(1) });
		a.assertCompatibility(5, "Exxxample", 1);
	}

	@Test(expected = IllegalStateException.class)
	public void testAssertCompatibilityNegative3() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new BitSet[] { createBitSet(1) });
		a.assertCompatibility(5, "Example", 3);
	}

	@Test
	public void testToString() {
		final ExecutionData a = new ExecutionData(Long.MAX_VALUE, "Example",
				new BitSet[] { createBitSet(1) });
		assertEquals("ExecutionData [name=Example, id=7fffffffffffffff]",
				a.toString());
	}

	private BitSet createBitSet(int bitToSet) {
		BitSet bs = new BitSet();
		bs.set(bitToSet);
		return bs;
	}

}
