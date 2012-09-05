package org.jacoco.examples;

/**
 * The test target we want to see code coverage for.
 */
public class TestTarget1 implements Runnable {

	public void run() {
		final int n = 7;
		final String status = isPrime(n) ? "prime" : "not prime";
		System.out.printf("%s is %s%n", Integer.valueOf(n), status);
	}

	public boolean isPrime(final int n) {
		if (n < 2) {
			return false;
		}
		if (n == 2 || n == 3) {
			return true;
		}
		if (n % 2 == 0 || n % 3 == 0) {
			return false;
		}
		final long sqrtN = (long) Math.sqrt(n) + 1;
		for (long i = 6L; i <= sqrtN; i += 6) {
			if (n % (i - 1) == 0 || n % (i + 1) == 0) {
				return false;
			}
		}
		return true;
	}

}