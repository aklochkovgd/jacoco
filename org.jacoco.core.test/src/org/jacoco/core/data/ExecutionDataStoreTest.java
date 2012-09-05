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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ExecutionDataStore}.
 */
public class ExecutionDataStoreTest implements IExecutionDataVisitor {

	private ExecutionDataStore store;

	private Map<Long, ExecutionData> dataOutput;

	@Before
	public void setup() {
		store = new ExecutionDataStore();
		dataOutput = new HashMap<Long, ExecutionData>();
	}

	@Test
	public void testEmpty() {
		assertNull(store.get(123));
		store.accept(this);
		assertEquals(Collections.emptyMap(), dataOutput);
	}

	@Test
	public void testPut() {
		final boolean[] probes = new boolean[] { false, false, true };
		BooleanProbeData src = new BooleanProbeData(probes);
		store.put(new ExecutionData(1000, "Sample", src));
		final ExecutionData data = store.get(1000);
		assertSame(src, data.getData());
		store.accept(this);
		assertEquals(Collections.singletonMap(Long.valueOf(1000), data),
				dataOutput);
	}

	@Test
	public void testGetContents() {
		final BooleanProbeData probes = new BooleanProbeData(0);
		final ExecutionData a = new ExecutionData(1000, "A", probes);
		store.put(a);
		final ExecutionData aa = new ExecutionData(1000, "A", probes);
		store.put(aa);
		final ExecutionData b = new ExecutionData(1001, "B", probes);
		store.put(b);
		final Set<ExecutionData> actual = new HashSet<ExecutionData>(
				store.getContents());
		final Set<ExecutionData> expected = new HashSet<ExecutionData>(
				Arrays.asList(a, b));
		assertEquals(expected, actual);
	}

	@Test
	public void testGetWithoutCreate() {
		final ExecutionData data = new ExecutionData(1000, "Sample",
				new BooleanProbeData(0));
		store.put(data);
		assertSame(data, store.get(1000));
	}

	@Test
	public void testGetWithCreate() {
		final Long id = Long.valueOf(1000);
		final ExecutionData data = store.get(id, "Sample", 3);
		assertEquals(1000, data.getId());
		assertEquals("Sample", data.getName());
		assertEquals(3, data.getData().getLength());
		assertFalse(data.getData().isCovered(0));
		assertFalse(data.getData().isCovered(1));
		assertFalse(data.getData().isCovered(2));
		assertSame(data, store.get(id, "Sample", 3));
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNegative1() {
		final boolean[] probes = new boolean[] { false, false, true };
		store.put(new ExecutionData(1000, "Sample",
				new BooleanProbeData(probes)));
		store.get(Long.valueOf(1000), "Other", 3);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNegative2() {
		final boolean[] probes = new boolean[] { false, false, true };
		store.put(new ExecutionData(1000, "Sample",
				new BooleanProbeData(probes)));
		store.get(Long.valueOf(1000), "Sample", 4);
	}

	@Test(expected = IllegalStateException.class)
	public void testPutNegative() {
		final BooleanProbeData data = new BooleanProbeData(0);
		store.put(new ExecutionData(1000, "Sample1", data));
		store.put(new ExecutionData(1000, "Sample2", data));
	}

	@Test
	public void testMerge() {
		final boolean[] probes1 = new boolean[] { false, true, false, true };
		store.visitClassExecution(new ExecutionData(1000, "Sample",
				new BooleanProbeData(probes1)));
		final boolean[] probes2 = new boolean[] { false, true, true, false };
		store.visitClassExecution(new ExecutionData(1000, "Sample",
				new BooleanProbeData(probes2)));

		final ProbeData result = store.get(1000).getData();
		assertFalse(result.isCovered(0));
		assertTrue(result.isCovered(1));
		assertTrue(result.isCovered(2));
		assertTrue(result.isCovered(3));
	}

	@Test(expected = IllegalStateException.class)
	public void testMergeNegative() {
		final boolean[] probes1 = new boolean[] { false, false };
		store.visitClassExecution(new ExecutionData(1000, "Sample",
				new BooleanProbeData(probes1)));
		final boolean[] probes2 = new boolean[] { false, false, false };
		store.visitClassExecution(new ExecutionData(1000, "Sample",
				new BooleanProbeData(probes2)));
	}

	@Test
	public void testReset() throws InstantiationException,
			IllegalAccessException {
		final boolean[] probes1 = new boolean[] { true, true, false };
		store.put(new ExecutionData(1000, "Sample", new BooleanProbeData(
				probes1)));
		store.reset();
		final ProbeData data = store.get(1000).getData();
		assertNotNull(data);
		assertFalse(data.isCovered(0));
		assertFalse(data.isCovered(1));
		assertFalse(data.isCovered(2));
	}

	// === IExecutionDataOutput ===

	public void visitClassExecution(final ExecutionData data) {
		dataOutput.put(Long.valueOf(data.getId()), data);
	}

}
