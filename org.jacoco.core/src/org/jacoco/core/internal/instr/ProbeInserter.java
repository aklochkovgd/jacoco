/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import static org.objectweb.asm.Opcodes.DUP;

import java.util.BitSet;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;

/**
 * Internal utility to add probes into the control flow of a method. The code
 * for a probe simply sets a certain slot of a boolean array to true. In
 * addition the probe array has to be retrieved at the beginning of the method
 * and stored in a local variable.
 */
class ProbeInserter extends MethodAdapter implements IProbeInserter {

	private final IProbeArrayStrategy arrayStrategy;

	/** Position of the inserted variable. */
	private final int variable;

	/** Index the inserted variable. */
	private final int variableIdx;

	/** Indicated whether the probe variable has already been inserted. */
	private boolean inserted;

	/** Maximum stack usage of the code to access the probe array. */
	private int accessorStackSize;

	/** Labels and line numbers preceding the first real instruction. */
	private final InsnList prolog;

	private String updateStatementCoverageClass;
	private String updateStatementCoverageMethod;

	/**
	 * Creates a new {@link ProbeInserter}.
	 * 
	 * @param access
	 *            access flags of the adapted method.
	 * @param desc
	 *            the method's descriptor
	 * @param mv
	 *            the method visitor to which this adapter delegates calls
	 * @param arrayStrategy
	 *            callback to create the code that retrieves the reference to
	 *            the probe array
	 */
	ProbeInserter(final int access, final String desc, final MethodVisitor mv,
			final IProbeArrayStrategy arrayStrategy) {
		super(mv);
		this.arrayStrategy = arrayStrategy;
		int idx = (Opcodes.ACC_STATIC & access) == 0 ? 1 : 0;
		int pos = idx;
		for (final Type t : Type.getArgumentTypes(desc)) {
			idx++;
			pos += t.getSize();
		}
		variableIdx = idx;
		variable = pos;
		inserted = false;
		prolog = new InsnList();

		try {
			final String methodName = System
					.getProperty("jacoco.updateStatementCoverageMethod");
			if (methodName != null) {
				final String[] parts = methodName.split("#");
				updateStatementCoverageClass = parts[0];
				updateStatementCoverageMethod = parts[1];
			}
		} catch (final Exception e) {
			updateStatementCoverageClass = null;
			updateStatementCoverageMethod = null;
		}
	}

	public void insertProbe(final int id) {

		checkLoad();

		if (updateStatementCoverageClass == null) {
			mv.visitVarInsn(Opcodes.ALOAD, variable);

			// Stack[0]: [Z

			InstrSupport.push(mv, id);

			// Stack[1]: I
			// Stack[0]: [Z

			mv.visitTypeInsn(Opcodes.NEW, InstrSupport.LINE_DATA_CLASS);
			mv.visitInsn(DUP);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					InstrSupport.LINE_DATA_CLASS, "<init>", "()V");
			// mv.visitVarInsn(Opcodes.ASTORE, variable + 1);
			// mv.visitVarInsn(Opcodes.ALOAD, variable + 1);
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
					InstrSupport.LINE_DATA_CLASS, "set", "(I)V");
			// mv.visitVarInsn(Opcodes.ALOAD, variable + 1);

			// Stack[2]: I
			// Stack[1]: I
			// Stack[0]: [Z

			mv.visitInsn(Opcodes.AASTORE);
		} else {
			final Label l0 = new Label();
			final Label l1 = new Label();
			final Label l2 = new Label();
			final Label l3 = new Label();
			mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");

			mv.visitLabel(l0);

			mv.visitLdcInsn(updateStatementCoverageClass);
			// Stack[0]: Ljava.lang.String;

			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class",
					"forName", "(Ljava/lang/String;)Ljava/lang/Class;");
			// Stack[0]: Ljava.lang.Class;

			mv.visitLdcInsn(updateStatementCoverageMethod);
			// Stack[1]: Ljava.lang.String;
			// Stack[0]: Ljava.lang.Class;

			mv.visitInsn(Opcodes.ICONST_2);
			// Stack[2]: I
			// Stack[1]: Ljava.lang.String;
			// Stack[0]: Ljava.lang.Class;

			mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class");
			// Stack[3]: [java.lang.Class
			// Stack[1]: Ljava.lang.String;
			// Stack[0]: Ljava.lang.Class;

			mv.visitInsn(Opcodes.DUP);
			// Stack[4]: [java.lang.Class
			// Stack[3]: [java.lang.Class
			// Stack[1]: Ljava.lang.String;
			// Stack[0]: Ljava.lang.Class;

			mv.visitInsn(Opcodes.ICONST_0);
			// Stack[5]: I
			// Stack[4]: [java.lang.Class
			// Stack[3]: [java.lang.Class
			// Stack[1]: Ljava.lang.String;
			// Stack[0]: Ljava.lang.Class;

			mv.visitLdcInsn(Type.getType(BitSet[].class));
			// Stack[6]: Ljava.lang.Class
			// Stack[5]: I
			// Stack[4]: [java.lang.Class
			// Stack[3]: [java.lang.Class
			// Stack[1]: Ljava.lang.String;
			// Stack[0]: Ljava.lang.Class;

			mv.visitInsn(Opcodes.AASTORE);
			// Stack[3]: [java.lang.Class
			// Stack[1]: Ljava.lang.String;
			// Stack[0]: Ljava.lang.Class;

			mv.visitInsn(Opcodes.DUP);
			// Stack[4]: [java.lang.Class
			// Stack[3]: [java.lang.Class
			// Stack[1]: Ljava.lang.String;
			// Stack[0]: Ljava.lang.Class;

			mv.visitInsn(Opcodes.ICONST_1);
			// Stack[5]: I
			// Stack[4]: [java.lang.Class
			// Stack[3]: [java.lang.Class
			// Stack[1]: Ljava.lang.String;
			// Stack[0]: Ljava.lang.Class;

			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Integer", "TYPE",
					"Ljava/lang/Class;");
			// Stack[6]: Ljava.lang.Class
			// Stack[5]: I
			// Stack[4]: [java.lang.Class
			// Stack[3]: [java.lang.Class
			// Stack[1]: Ljava.lang.String;
			// Stack[0]: Ljava.lang.Class;

			mv.visitInsn(Opcodes.AASTORE);
			// Stack[3]: [java.lang.Class
			// Stack[1]: Ljava.lang.String;
			// Stack[0]: Ljava.lang.Class;

			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
					"getDeclaredMethod",
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			// Stack[1]: Ljava.lang.reflect.Method;

			mv.visitInsn(Opcodes.ACONST_NULL);
			// Stack[1]: null
			// Stack[1]: Ljava.lang.reflect.Method;

			mv.visitInsn(Opcodes.ICONST_2);
			// Stack[1]: I
			// Stack[1]: null
			// Stack[1]: Ljava.lang.reflect.Method;

			mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: null
			// Stack[1]: Ljava.lang.reflect.Method;

			mv.visitInsn(Opcodes.DUP);
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: null
			// Stack[1]: Ljava.lang.reflect.Method;

			mv.visitInsn(Opcodes.ICONST_0);
			// Stack[1]: I
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: null
			// Stack[1]: Ljava.lang.reflect.Method;

			mv.visitVarInsn(Opcodes.ALOAD, variable);
			// Stack[1]: [Ljava/util/BitSet;
			// Stack[1]: I
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: null
			// Stack[1]: Ljava.lang.reflect.Method;

			mv.visitInsn(Opcodes.AASTORE);
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: null
			// Stack[1]: Ljava.lang.reflect.Method;

			mv.visitInsn(Opcodes.DUP);
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: null
			// Stack[1]: Ljava.lang.reflect.Method;

			mv.visitInsn(Opcodes.ICONST_1);
			// Stack[1]: I
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: null
			// Stack[1]: Ljava.lang.reflect.Method;

			InstrSupport.push(mv, id);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer",
					"valueOf", "(I)Ljava/lang/Integer;");
			// Stack[1]: I
			// Stack[1]: I
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: null
			// Stack[1]: Ljava.lang.reflect.Method;

			mv.visitInsn(Opcodes.AASTORE); // store test id (Integer) into
											// the
											// array of parameters for
											// Method.invoke
			// Stack[1]: [Ljava/lang/Object;
			// Stack[1]: null
			// Stack[1]: Ljava.lang.reflect.Method;

			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
					"java/lang/reflect/Method", "invoke",
					"(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
			mv.visitInsn(Opcodes.POP);

			mv.visitLabel(l1);
			mv.visitJumpInsn(Opcodes.GOTO, l3);

			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_FULL, 2, new Object[] {
					updateStatementCoverageClass.replace('.', '/'),
					"[Ljava/util/BitSet;" }, 1,
					new Object[] { "java/lang/Exception" });
			mv.visitVarInsn(Opcodes.ASTORE, variable + 1);
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
			mv.visitInsn(Opcodes.DUP);
			mv.visitVarInsn(Opcodes.ALOAD, variable + 1);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					"java/lang/RuntimeException", "<init>",
					"(Ljava/lang/Throwable;)V");
			mv.visitInsn(Opcodes.ATHROW);

			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_SAME, 0, new Object[0], 0, new Object[0]);
		}
	}

	private void checkLoad() {
		if (!inserted) {
			accessorStackSize = arrayStrategy.storeInstance(mv, variable);
			prolog.accept(mv);
			inserted = true;
		}
	}

	@Override
	public final void visitLabel(final Label label) {
		if (!inserted) {
			prolog.add(new LabelNode(label));
		} else {
			mv.visitLabel(label);
		}
	}

	@Override
	public void visitLineNumber(final int line, final Label start) {
		if (!inserted) {
			prolog.add(new LineNumberNode(line, new LabelNode(start)));
		} else {
			mv.visitLineNumber(line, start);
		}
	}

	@Override
	public final void visitVarInsn(final int opcode, final int var) {
		checkLoad();
		mv.visitVarInsn(opcode, map(var));
	}

	@Override
	public final void visitIincInsn(final int var, final int increment) {
		checkLoad();
		mv.visitIincInsn(map(var), increment);
	}

	@Override
	public final void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
		checkLoad();
		mv.visitLocalVariable(name, desc, signature, start, end, map(index));
	}

	@Override
	public final void visitInsn(final int opcode) {
		checkLoad();
		mv.visitInsn(opcode);
	}

	@Override
	public final void visitIntInsn(final int opcode, final int operand) {
		checkLoad();
		mv.visitIntInsn(opcode, operand);
	}

	@Override
	public final void visitTypeInsn(final int opcode, final String type) {
		checkLoad();
		mv.visitTypeInsn(opcode, type);
	}

	@Override
	public final void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
		checkLoad();
		mv.visitFieldInsn(opcode, owner, name, desc);
	}

	@Override
	public final void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc) {
		checkLoad();
		mv.visitMethodInsn(opcode, owner, name, desc);
	}

	@Override
	public final void visitJumpInsn(final int opcode, final Label label) {
		checkLoad();
		mv.visitJumpInsn(opcode, label);
	}

	@Override
	public final void visitLdcInsn(final Object cst) {
		checkLoad();
		mv.visitLdcInsn(cst);
	}

	@Override
	public final void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label[] labels) {
		checkLoad();
		mv.visitTableSwitchInsn(min, max, dflt, labels);
	}

	@Override
	public final void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		checkLoad();
		mv.visitLookupSwitchInsn(dflt, keys, labels);
	}

	@Override
	public final void visitMultiANewArrayInsn(final String desc, final int dims) {
		checkLoad();
		mv.visitMultiANewArrayInsn(desc, dims);
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
		// Max stack size of the probe code is 3 which can add to the
		// original stack size depending on the probe locations. The accessor
		// stack size is an absolute maximum, as the accessor code is inserted
		// at the very beginning of each method when the stack size is empty.
		if (updateStatementCoverageClass == null) {
			final int increasedStack = Math
					.max(maxStack + 6, accessorStackSize);
			mv.visitMaxs(increasedStack, maxLocals + 1);
		} else {
			final int increasedStack = Math
					.max(maxStack + 6, accessorStackSize);
			mv.visitMaxs(increasedStack, maxLocals + 2);
		}
	}

	private int map(final int var) {
		if (var < variable) {
			return var;
		} else {
			return var + 1;
		}
	}

	@Override
	public final void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {

		if (type != Opcodes.F_NEW) { // uncompressed frame
			throw new IllegalArgumentException(
					"ClassReader.accept() should be called with EXPAND_FRAMES flag");
		}

		if (inserted) {
			final int n = Math.max(nLocal, variableIdx) + 1;
			final Object[] newLocal = new Object[n];
			for (int i = 0; i < n; i++) {
				if (i < variableIdx) {
					// For dead code it is possible to specify less locals than
					// we have method parameters.
					newLocal[i] = i < nLocal ? local[i] : Opcodes.TOP;
					continue;
				}
				if (i > variableIdx) {
					newLocal[i] = local[i - 1];
					continue;
				}
				newLocal[i] = InstrSupport.DATAFIELD_DESC;
			}
			mv.visitFrame(type, n, newLocal, nStack, stack);
		} else {
			mv.visitFrame(type, nLocal, local, nStack, stack);
		}

		checkLoad();
	}

}
