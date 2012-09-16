/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jacoco.core.data;

import java.util.Arrays;

public class BooleanProbeData implements ProbeData {

	private final boolean[] data;

	public BooleanProbeData(final int dataLength) {
		this.data = new boolean[dataLength];
	}

	public BooleanProbeData(final boolean[] data) {
		this.data = data;
	}

	public void merge(final ProbeData other) {
		final BooleanProbeData o = (BooleanProbeData) other;
		for (int i = 0; i < data.length; i++) {
			data[i] = data[i] || o.data[i];
		}
	}

	public void reset() {
		Arrays.fill(data, false);
	}

	public int getLength() {
		return data.length;
	}

	public boolean isCovered(final int probeId) {
		return data[probeId];
	}

	public int getHitCount() {
		return data.length;
	}

	public boolean[] getProbes() {
		return data;
	}

	public void setCovered(final int probeId) {
		data[probeId] = true;
	}

}
