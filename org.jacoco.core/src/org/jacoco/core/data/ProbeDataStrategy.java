/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors All
 * rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jacoco.core.data;

import java.io.IOException;

public abstract class ProbeDataStrategy<T extends ProbeData> {

	public abstract Class<T> getProbeDataClass();

	public abstract ProbeData newProbeData(int dataLength);

	public abstract ProbeData readData(CompactDataInput in) throws IOException;

	public abstract void writeData(CompactDataOutput out, ProbeData data)
			throws IOException;

	private static final String DEFAULT_STRATEGY_CLASS = BooleanProbeDataStrategy.class
			.getName();

	public final static ProbeDataStrategy<? extends ProbeData> INSTANCE = createInstance();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ProbeDataStrategy<? extends ProbeData> createInstance() {
		final String strategyClass = System.getProperty(
				"jacoco.probeDataStrategy", DEFAULT_STRATEGY_CLASS);
		System.out.println("DDD Using probe strategy" + strategyClass);
		Class clazz;
		try {
			clazz = Class.forName(strategyClass);
			return (ProbeDataStrategy) clazz.newInstance();
		} catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
