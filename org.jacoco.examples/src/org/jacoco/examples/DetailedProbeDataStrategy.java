package org.jacoco.examples;

import java.io.IOException;

import org.jacoco.core.data.CompactDataInput;
import org.jacoco.core.data.CompactDataOutput;
import org.jacoco.core.data.ProbeData;
import org.jacoco.core.data.ProbeDataStrategy;

public class DetailedProbeDataStrategy extends
		ProbeDataStrategy<DetailedProbeData> {

	@Override
	public Class<DetailedProbeData> getProbeDataClass() {
		return DetailedProbeData.class;
	}

	@Override
	public ProbeData newProbeData(final int dataLength) {
		return new DetailedProbeData(dataLength);
	}

	@Override
	public ProbeData readData(final CompactDataInput in) throws IOException {
		final DetailedProbeData data = new DetailedProbeData(
				in.readBooleanArray());
		final int coveredByLen = in.readVarInt();
		for (int i = 0; i < coveredByLen; i++) {
			data.addCoveredBy(in.readUTF());
		}
		return data;
	}

	@Override
	public void writeData(final CompactDataOutput out, final ProbeData data)
			throws IOException {
		// TODO Auto-generated method stub

	}

}
