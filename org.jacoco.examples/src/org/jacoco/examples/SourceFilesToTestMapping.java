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
package org.jacoco.examples;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.DetailedProbeData;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ProbeData;
import org.jacoco.core.data.ProbeDataStrategy;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.junit.Test;

/**
 * Collects "test methods to source files" mapping using a custom
 * {@link ProbeData}.
 * 
 * @see ProbeData
 * @see ProbeDataStrategy
 */
public class SourceFilesToTestMapping {

	private final SystemPropertiesRuntime runtime = new SystemPropertiesRuntime();

	private final Instrumenter instrumenter = new Instrumenter(runtime);

	private Class<?> instrumentedClass;

	public SourceFilesToTestMapping() {
		System.setProperty("jacoco.probeDataStrategy",
				"org.jacoco.examples.DetailedProbeDataStrategy");
		runtime.startup();
	}

	public void instrumentClass(final Class targetClass) throws Exception {
		final byte[] bytes = instrumenter.instrument(TargetLoader
				.getClassData(targetClass));
		final TargetLoader loader = new TargetLoader(targetClass, bytes);
		this.instrumentedClass = loader.getTargetClass();
	}

	private void printReport() throws Exception {
		final ExecutionDataStore executionData = new ExecutionDataStore();
		runtime.collect(executionData, null, false);

		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
		analyzer.analyzeClass(getTargetClass(TestTarget1.class.getName()));

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

		for (final ExecutionData d : executionData.getContents()) {
			final Set<String> tests = ((DetailedProbeData) d.getData())
					.getCoveredBy();
			final StringBuilder b = new StringBuilder();
			for (final Iterator<String> it = tests.iterator(); it.hasNext();) {
				b.append(it.next());
				if (it.hasNext()) {
					b.append(", ");
				}
			}
			System.out.println(String.format("Class %s is covered by tests %s",
					d.getName(), b));
		}
	}

	public static void main(final String[] args) throws Exception {
		final SourceFilesToTestMapping c = new SourceFilesToTestMapping();

		c.instrumentClass(TestTarget1.class);
		c.test1();
		c.test2();

		c.instrumentClass(TestTarget2.class);
		c.test2();

		c.printReport();

		c.runtime.shutdown();
	}

	@Test
	public void test1() throws Exception {
		final Runnable targetInstance = (Runnable) instrumentedClass
				.newInstance();
		targetInstance.run();
		test2();
	}

	@Test
	public void test2() throws Exception {
		final Object targetInstance = instrumentedClass.newInstance();
		final Method m = instrumentedClass.getMethod("isPrime", int.class);
		m.invoke(targetInstance, 5);
		m.invoke(targetInstance, 8);
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
