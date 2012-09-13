package org.jacoco.core.data;

import java.io.IOException;

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
		out.writeBooleanArray(data.getProbes());
		final DetailedProbeData d = (DetailedProbeData) data;
		out.writeVarInt(d.getCoveredBy().size());
		for (final String testMethod : d.getCoveredBy()) {
			out.writeUTF(testMethod);
		}
	}
}
