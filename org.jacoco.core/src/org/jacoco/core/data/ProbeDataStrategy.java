/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors All
 * rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jacoco.core.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class ProbeDataStrategy<T extends ProbeData> {

	public abstract Class<T> getProbeDataClass();

	public abstract ProbeData newProbeData(int dataLength);

	public abstract ProbeData readData(CompactDataInput in) throws IOException;

	public abstract void writeData(CompactDataOutput out, ProbeData data)
			throws IOException;

	private static final String DEFAULT_STRATEGY = "default";
	private static final Map<String, Class<? extends ProbeDataStrategy>> STRATEGIES = new HashMap<String, Class<? extends ProbeDataStrategy>>();
	static {
		STRATEGIES.put("default", BooleanProbeDataStrategy.class);
		STRATEGIES.put("detailed", DetailedProbeDataStrategy.class);
	}

	public final static ProbeDataStrategy<? extends ProbeData> INSTANCE = createInstance();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ProbeDataStrategy<? extends ProbeData> createInstance() {
		String strategy = System.getProperty("jacoco.probeDataStrategy",
				DEFAULT_STRATEGY);
		if (!STRATEGIES.containsKey(strategy)) {
			strategy = DEFAULT_STRATEGY;
		}
		final Class strategyClass = STRATEGIES.get(strategy);
		System.out.println("Using strategy " + strategyClass.getName());
		try {
			return (ProbeDataStrategy) strategyClass.newInstance();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

}
