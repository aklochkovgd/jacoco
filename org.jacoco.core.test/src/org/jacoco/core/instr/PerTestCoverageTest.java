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
package org.jacoco.core.instr;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.BitSet;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Instrumenter}.
 */
public class PerTestCoverageTest {

	/**
	 * The test target we want to see code coverage for.
	 */
	public static class TestTarget implements Runnable {

		public void run() {
			final int n = 7;
			final String status = isPrime(n) ? "prime" : "not prime";
			System.out.printf("%s is %s%n", Integer.valueOf(n), status);
		}

		private boolean isPrime(final int n) {
			for (int i = 2; i * i <= n; i++) {
				if ((n ^ i) == 0) {
					return false;
				}
			}
			return true;
		}

	}

	private SystemPropertiesRuntime runtime;

	private Instrumenter instrumenter;

	public static void updateCoverageStats(BitSet[] stats, int id) {
		System.out.println("Updating stats!");
		stats[id] = new BitSet();
		stats[id].set(0);
	}

	@Before
	public void setup() {
		System.setProperty("jacoco.updateStatementCoverageMethod",
				"org.jacoco.core.instr.PerTestCoverageTest#updateCoverageStats");
		runtime = new SystemPropertiesRuntime();
		instrumenter = new Instrumenter(runtime);
		runtime.startup();
	}

	@After
	public void teardown() {
		runtime.shutdown();
	}

	@Test
	public void testUpdateMethod() {
		String updateCoverageMethod = System
				.getProperty("jacoco.updateStatementCoverageMethod");
		final BitSet[] classCoverage = new BitSet[1];
		final int id = 0;
		final String[] methodParts = updateCoverageMethod.split("#");

		try {
			Class.forName(methodParts[0])
					.getDeclaredMethod(methodParts[1], BitSet[].class,
							Integer.TYPE)
					.invoke(null, classCoverage, Integer.valueOf(id));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		assertEquals(1, classCoverage[0].cardinality());
	}

	@Test
	public void testPerTestCoverage() throws Exception {
		// Create instrumented instance:
		final byte[] bytes = instrumenter.instrument(TargetLoader
				.getClassData(TestTarget.class));
		final TargetLoader loader = new TargetLoader(TestTarget.class, bytes);
		final Runnable targetInstance = (Runnable) loader.getTargetClass()
				.newInstance();
		targetInstance.run();

		final ExecutionDataStore executionData = new ExecutionDataStore();
		runtime.collect(executionData, null, false);

		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
		analyzer.analyzeClass(getTargetClass(TestTarget.class.getName()));

		// Let's dump some metrics and line coverage information:
		for (final IClassCoverage cc : coverageBuilder.getClasses()) {
			System.out.printf("Coverage of class %s%n", cc.getName());

			printCounter("instructions", cc.getInstructionCounter());
			printCounter("branches", cc.getBranchCounter());
			printCounter("lines", cc.getLineCounter());
			printCounter("methods", cc.getMethodCounter());
			printCounter("complexity", cc.getComplexityCounter());

			for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
				System.out.printf("Line %s: %s%n", Integer.valueOf(i),
						getColor(cc.getLine(i).getStatus()));
			}
		}
	}

	private InputStream getTargetClass(final String name) {
		final String resource = '/' + name.replace('.', '/') + ".class";
		return getClass().getResourceAsStream(resource);
	}

	private void printCounter(final String unit, final ICounter counter) {
		final Integer missed = Integer.valueOf(counter.getMissedCount());
		final Integer total = Integer.valueOf(counter.getTotalCount());
		System.out.printf("%s of %s %s missed%n", missed, total, unit);
	}

	private String getColor(final int status) {
		switch (status) {
		case ICounter.NOT_COVERED:
			return "red";
		case ICounter.PARTLY_COVERED:
			return "yellow";
		case ICounter.FULLY_COVERED:
			return "green";
		}
		return "";
	}

}
