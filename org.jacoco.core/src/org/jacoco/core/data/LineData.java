package org.jacoco.core.data;

import java.util.Arrays;

public final class LineData {

	private static final int INITIAL_CAPACITY = 8;

	private short[] tests;
	private int size = 0;

	public LineData() {
	}

	public LineData(final short test) {
		addTest(test);
	}

	LineData(final short[] tests) {
		ensureCapacity(tests.length);
		size = tests.length;
		System.arraycopy(tests, 0, this.tests, 0, size);
	}

	public boolean isEmpty() {
		return tests == null;
	}

	public void addTest(final short test) {
		if (tests == null) {
			tests = new short[INITIAL_CAPACITY];
		}
		ensureCapacity(size + 1);
		tests[size++] = test;
	}

	public short[] getTests() {
		return tests;
	}

	public int getSize() {
		return size;
	}

	private void ensureCapacity(final int capacity) {
		if (tests == null || capacity > tests.length) {
			int newsize;
			if (tests != null) {
				newsize = tests.length;
			} else {
				newsize = INITIAL_CAPACITY;
			}
			while (newsize < capacity) {
				newsize *= 2;
			}
			final short[] t = tests;
			tests = new short[newsize];
			if (t != null) {
				System.arraycopy(t, 0, tests, 0, t.length);
			}
		}
	}

	public void merge(final LineData other) {
		if (other.isEmpty()) {
			return;
		}
		final short[] data = tests;
		final int oldsize = size;
		tests = new short[tests.length];
		size = 0;
		int i = 0, j = 0;
		while (i < oldsize && j < other.size) {
			if (data[i] < other.tests[j]) {
				addTest(data[i++]);
			} else {
				addTest(other.tests[j]);
				if (data[i] == other.tests[j]) {
					i++;
				}
				j++;
			}
		}
		for (; i < oldsize; i++) {
			addTest(data[i]);
		}
		for (; j < other.size; j++) {
			addTest(other.tests[j]);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + size;
		result = prime * result + Arrays.hashCode(tests);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final LineData other = (LineData) obj;
		if (size != other.size) {
			return false;
		}
		if (!Arrays.equals(tests, other.tests)) {
			return false;
		}
		return true;
	}

}
