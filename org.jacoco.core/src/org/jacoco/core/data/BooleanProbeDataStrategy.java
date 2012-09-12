package org.jacoco.core.data;

/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
import java.io.IOException;

public class BooleanProbeDataStrategy extends
		ProbeDataStrategy<BooleanProbeData> {

	@Override
	public Class<BooleanProbeData> getProbeDataClass() {
		return BooleanProbeData.class;
	}

	@Override
	public ProbeData newProbeData(final int dataLength) {
		return new BooleanProbeData(dataLength);
	}

	@Override
	public ProbeData readData(final CompactDataInput in) throws IOException {
		return new BooleanProbeData(in.readBooleanArray());
	}

	@Override
	public void writeData(final CompactDataOutput out, final ProbeData data)
			throws IOException {
		final boolean[] d = ((BooleanProbeData) data).getData();
		System.out.println("DDD writing " + d.length + " probes");
		out.writeBooleanArray(d);
	}
}
