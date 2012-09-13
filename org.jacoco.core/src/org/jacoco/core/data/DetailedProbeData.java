package org.jacoco.core.data;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

public class DetailedProbeData extends BooleanProbeData {

	private static final Class[] TEST_ANNOTATIONS = { Test.class, Before.class,
			BeforeClass.class, After.class, AfterClass.class };

	private final Set<String> coveredBy = new HashSet<String>();

	public DetailedProbeData(final int dataLength) {
		super(dataLength);
	}

	public DetailedProbeData(final boolean[] data) {
		super(data);
	}

	@Override
	public void merge(final ProbeData other) {
		super.merge(other);
		coveredBy.addAll(((DetailedProbeData) other).coveredBy);
	}

	@Override
	public void reset() {
		super.reset();
		coveredBy.clear();
	}

	@Override
	public void setCovered(final int probeId) {
		super.setCovered(probeId);
		coveredBy.add(findCurrentTestMethod());
	}

	public void addCoveredBy(final String testMethod) {
		this.coveredBy.add(testMethod);
	}

	public Set<String> getCoveredBy() {
		return coveredBy;
	}

	private static String findCurrentTestMethod() {
		Method method = null;
		for (final StackTraceElement e : Thread.currentThread().getStackTrace()) {
			final Method m = getMethod(e);
			if (m == null) {
				continue;
			}
			if (isTestMethod(m)) {
				method = m;
				// System.out.println("Test method:"
				// + m.getDeclaringClass().getName() + "#" + m.getName());
				break;
			}
		}

		if (method == null) {
			return null;
		}

		return getMethodName(method);
	}

	private static String getMethodName(final Method method) {
		return method.getDeclaringClass().getName() + "#" + method.getName();
	}

	private static boolean isTestMethod(final Method method) {
		for (final Class annotationClass : TEST_ANNOTATIONS) {
			if (method.isAnnotationPresent(annotationClass)) {
				return true;
			}
		}
		return false;
	}

	private static Method getMethod(final StackTraceElement stackTraceElement) {
		final String stackTraceClassName = stackTraceElement.getClassName();
		final String stackTraceMethodName = stackTraceElement.getMethodName();
		final int stackTraceLineNumber = stackTraceElement.getLineNumber();
		Class<?> stackTraceClass;
		try {
			stackTraceClass = Class.forName(stackTraceClassName);

			if (stackTraceMethodName.equals("<init>")) {
				return null;
			}

			// I am only using AtomicReference as a container to dump a String
			// into,
			// feel free to ignore it for now
			final AtomicReference<String> methodDescriptorReference = new AtomicReference<String>();

			final String classFileResourceName = "/"
					+ stackTraceClassName.replaceAll("\\.", "/") + ".class";
			final InputStream classFileStream = stackTraceClass
					.getResourceAsStream(classFileResourceName);

			if (classFileStream == null) {
				// throw new RuntimeException(
				// "Could not acquire the class file containing for the calling class");
				return null;
			}

			try {
				final ClassReader classReader = new ClassReader(classFileStream);
				classReader.accept(new EmptyVisitor() {
					@Override
					public MethodVisitor visitMethod(final int access,
							final String name, final String desc,
							final String signature, final String[] exceptions) {
						if (!name.equals(stackTraceMethodName)) {
							return null;
						}

						return new EmptyVisitor() {
							@Override
							public void visitLineNumber(final int line,
									final Label start) {
								if (line == stackTraceLineNumber) {
									methodDescriptorReference.set(desc);
								}
							}
						};
					}
				}, 0);
			} finally {
				classFileStream.close();
			}

			final String methodDescriptor = methodDescriptorReference.get();

			if (methodDescriptor == null) {
				// throw new RuntimeException("Could not find line "
				// + stackTraceLineNumber);
				return null;
			}

			for (final Method method : stackTraceClass.getMethods()) {
				if (stackTraceMethodName.equals(method.getName())
						&& methodDescriptor.equals(Type
								.getMethodDescriptor(method))) {
					return method;
				}
			}

		} catch (final Exception e) {
		}

		return null;
		// throw new RuntimeException("Could not find the calling method");
	}

}
