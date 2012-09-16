package org.jacoco.core.data;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import junit.framework.TestCase;

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

	private static final Pattern[] TEST_CLASSES = { Pattern.compile("Test.*"),
			Pattern.compile(".*TestCase"), Pattern.compile(".*Test") };

	private static final Map<String, Set<String>> testMethodsCache = new HashMap<String, Set<String>>();
	private static final Set<String> nonTestMethodsCache = new HashSet<String>();

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
		final Map<String, StackTraceElement> cachedTestClasses = new HashMap<String, StackTraceElement>();
		for (final StackTraceElement e : Thread.currentThread().getStackTrace()) {
			final Set<String> cacheEntry = testMethodsCache.get(e
					.getClassName());
			if (cacheEntry != null) {
				if (cacheEntry.contains(e.getMethodName())) {
					return getMethodName(e.getClassName(), e.getMethodName());
				} else {
					cachedTestClasses.put(e.getClassName(), e);
				}
			}
		}

		for (final StackTraceElement e : cachedTestClasses.values()) {
			final Set<String> cacheEntry = testMethodsCache.get(e
					.getClassName());
			if (isTestMethod(e)) {
				cacheEntry.add(e.getMethodName());
				return getMethodName(e.getClassName(), e.getMethodName());
			}
		}

		for (final StackTraceElement e : Thread.currentThread().getStackTrace()) {
			if (cachedTestClasses.containsKey(e.getClassName())
					|| !isTestClass(e.getClassName())) {
				continue;
			}

			if (isTestMethod(e)) {
				testMethodsCache.get(e.getClassName()).add(e.getMethodName());
				return getMethodName(e.getClassName(), e.getMethodName());
			}
		}

		return null;
	}

	private static boolean isTestClass(final String className) {
		for (final Pattern p : TEST_CLASSES) {
			if (p.matcher(className).matches()) {
				testMethodsCache.put(className, new HashSet<String>());
				return true;
			}
		}
		return false;
	}

	private static String getMethodName(final String className,
			final String methodName) {
		return className + "#" + methodName;
	}

	private static boolean isTestMethod(final StackTraceElement e) {
		final String methodName = getMethodName(e.getClassName(),
				e.getMethodName());
		if (nonTestMethodsCache.contains(methodName)) {
			return false;
		}

		if (isJunit3TestMethod(e)) {
			return true;
		}

		final Method m = getMethod(e);
		if (m != null) {
			for (final Class annotationClass : TEST_ANNOTATIONS) {
				if (m.isAnnotationPresent(annotationClass)) {
					return true;
				}
			}
		}

		nonTestMethodsCache.add(methodName);
		return false;
	}

	private static boolean isJunit3TestMethod(final StackTraceElement e) {
		try {
			final Class clazz = Class.forName(e.getClassName());
			return TestCase.class.isAssignableFrom(clazz);
		} catch (final ClassNotFoundException e1) {
			return false;
		}
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
