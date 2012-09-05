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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

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
		final LineData[] probes = new LineData[] { null, null,
				new LineData((short) 1) };
		store.put(new ExecutionData(1000, "Sample", probes));
		final ExecutionData data = store.get(1000);
		assertSame(probes, data.getData());
		store.accept(this);
		assertEquals(Collections.singletonMap(Long.valueOf(1000), data),
				dataOutput);
	}

	@Test
	public void testGetContents() {
		final LineData[] probes = new LineData[] {};
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
				new LineData[] {});
		store.put(data);
		assertSame(data, store.get(1000));
	}

	@Test
	public void testGetWithCreate() {
		final Long id = Long.valueOf(1000);
		final ExecutionData data = store.get(id, "Sample", 3);
		assertEquals(1000, data.getId());
		assertEquals("Sample", data.getName());
		assertEquals(3, data.getData().length);
		assertNull(data.getData()[0]);
		assertNull(data.getData()[1]);
		assertNull(data.getData()[2]);
		assertSame(data, store.get(id, "Sample", 3));
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNegative1() {
		final LineData[] data = new LineData[] { null, null,
				new LineData((short) 1) };
		store.put(new ExecutionData(1000, "Sample", data));
		store.get(Long.valueOf(1000), "Other", 3);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNegative2() {
		final LineData[] data = new LineData[] { null, null,
				new LineData((short) 1) };
		store.put(new ExecutionData(1000, "Sample", data));
		store.get(Long.valueOf(1000), "Sample", 4);
	}

	@Test(expected = IllegalStateException.class)
	public void testPutNegative() {
		final LineData[] data = new LineData[0];
		store.put(new ExecutionData(1000, "Sample1", data));
		store.put(new ExecutionData(1000, "Sample2", data));
	}

	@Test
	public void testMerge() {
		final LineData[] data1 = new LineData[] { null,
				new LineData((short) 1), null, new LineData((short) 3) };
		store.visitClassExecution(new ExecutionData(1000, "Sample", data1));
		final LineData[] data2 = new LineData[] { null,
				new LineData((short) 2), new LineData((short) 4), null };
		store.visitClassExecution(new ExecutionData(1000, "Sample", data2));

		final LineData[] result = store.get(1000).getData();
		assertNull(result[0]);
		assertEquals(2, result[1].getSize());
		assertEquals((short) 1, result[1].getTests()[0]);
		assertEquals((short) 2, result[1].getTests()[1]);
		assertEquals(1, result[2].getSize());
		assertEquals((short) 4, result[2].getTests()[0]);
		assertEquals(1, result[3].getSize());
		assertEquals((short) 3, result[3].getTests()[0]);
	}

	@Test(expected = IllegalStateException.class)
	public void testMergeNegative() {
		final LineData[] data1 = new LineData[] { null, null };
		store.visitClassExecution(new ExecutionData(1000, "Sample", data1));
		final LineData[] data2 = new LineData[] { null, null, null };
		store.visitClassExecution(new ExecutionData(1000, "Sample", data2));
	}

	@Test
	public void testReset() throws InstantiationException,
			IllegalAccessException {
		final LineData[] data1 = new LineData[] { new LineData((short) 1),
				new LineData((short) 2), null };
		store.put(new ExecutionData(1000, "Sample", data1));
		store.reset();
		final LineData[] data2 = store.get(1000).getData();
		assertNotNull(data2);
		assertNull(data2[0]);
		assertNull(data2[1]);
		assertNull(data2[2]);
	}

	// === IExecutionDataOutput ===

	public void visitClassExecution(final ExecutionData data) {
		dataOutput.put(Long.valueOf(data.getId()), data);
	}

}
