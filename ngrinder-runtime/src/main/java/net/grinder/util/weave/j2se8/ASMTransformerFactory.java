// // Copyright (C) 2009 - 2012 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.util.weave.j2se8;

import net.grinder.util.Pair;
import net.grinder.util.weave.Weaver;
import net.grinder.util.weave.WeavingException;
import net.grinder.util.weave.j2se8.DCRWeaver.ClassFileTransformerFactory;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;


/**
 * {@link ClassFileTransformerFactory} implementation that uses ASM to
 * advise methods.
 *
 * @author Philip Aston
 */

public final class ASMTransformerFactory
	implements ClassFileTransformerFactory {

	private final String m_adviceClass;

	/**
	 * Constructor.
	 *
	 * <p>
	 * We can't add fields to the class due to DCR limitations, so we have to wire
	 * in the advice class using static methods.{@code adviceClass} should
	 * implement two methods with the following names and signatures.
	 * </p>
	 *
	 * <pre>
	 * public static void enter(Object reference,
	 *                          String location);
	 *
	 * public static void exit(Object reference,
	 *                         String location,
	 *                         boolean success);
	 * </pre>
	 *
	 *
	 * @param adviceClass
	 *          Class that provides the advice.
	 * @throws WeavingException
	 *           If {@code adviceClass} does not implement {@code enter} and
	 *           {@code exit} static methods.
	 */
	public ASMTransformerFactory(Class<?> adviceClass) throws WeavingException {

		try {
			final Method enterMethod =
				adviceClass.getMethod("enter", Object.class, String.class);

			if (!Modifier.isStatic(enterMethod.getModifiers())) {
				throw new WeavingException("Enter method is not static");
			}

			final Method exitMethod =
				adviceClass.getMethod("exit", Object.class, String.class, Boolean.TYPE);

			if (!Modifier.isStatic(exitMethod.getModifiers())) {
				throw new WeavingException("Exit method is not static");
			}
		}
		catch (Exception e) {
			throw new WeavingException(
				adviceClass.getName() + " does not expected enter and exit methods",
				e);
		}

		m_adviceClass = Type.getInternalName(adviceClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClassFileTransformer create(PointCutRegistry pointCutRegistry) {
		return new ASMTransformer(pointCutRegistry);
	}

	/**
	 * {@link ClassFileTransformer} that advise methods using ASM.
	 *
	 * @author Philip Aston
	 */
	private class ASMTransformer implements ClassFileTransformer {

		private final PointCutRegistry m_pointCutRegistry;

		/**
		 * Constructor.
		 *
		 * <p>
		 * Each method has at most one advice. If the class is re-transformed,
		 * perhaps with additional advised methods, we will be passed the original
		 * class byte code . We rely on a {@link PointCutRegistry} to remember which
		 * methods to advise.
		 * </p>
		 *
		 * @param pointCutRegistry
		 *          Remembers the methods to advice, and the location strings.
		 */
		public ASMTransformer(PointCutRegistry pointCutRegistry) {
			m_pointCutRegistry = pointCutRegistry;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override public byte[] transform(ClassLoader loader,
										  final String internalClassName,
										  Class<?> classBeingRedefined,
										  ProtectionDomain protectionDomain,
										  byte[] originalBytes)
			throws IllegalClassFormatException {

			// The PointCutRegistry provides us the constructors and methods to advise
			// organised by class. This allows us quickly to find the right methods,
			// and ignore classes that aren't to be advised. (Important, since we're
			// called for every class that is loaded).
			final Map<Constructor<?>, List<WeavingDetails>>
				constructorToWeavingDetails =
				m_pointCutRegistry.getConstructorPointCutsForClass(internalClassName);

			final Map<Method, List<WeavingDetails>> methodToWeavingDetails =
				m_pointCutRegistry.getMethodPointCutsForClass(internalClassName);

			final int size =
				(constructorToWeavingDetails != null ?
					constructorToWeavingDetails.size() : 0) +
					(methodToWeavingDetails != null ? methodToWeavingDetails.size() : 0);

			if (size == 0) {
				return null;
			}

			// Having found the right set of constructors methods, we transform the
			// key to a form that is easier for our ASM visitor to use.
			final Map<Pair<String, String>, List<WeavingDetails>>
				nameAndDescriptionToWeavingDetails =
				new HashMap<Pair<String, String>, List<WeavingDetails>>(size);

			if (constructorToWeavingDetails != null) {
				for (Entry<Constructor<?>, List<WeavingDetails>> entry :
					constructorToWeavingDetails.entrySet()) {

					final Constructor<?> c = entry.getKey();

					// The key will be unique, so we can set the value directly.
					nameAndDescriptionToWeavingDetails.put(
						Pair.of("<init>", Type.getConstructorDescriptor(c)),
						entry.getValue());
				}
			}

			if (methodToWeavingDetails != null) {
				for (Entry<Method, List<WeavingDetails>> entry :
					methodToWeavingDetails.entrySet()) {

					final Method m = entry.getKey();

					// The key will be unique, so we can set the value directly.
					nameAndDescriptionToWeavingDetails.put(
						Pair.of(m.getName(), Type.getMethodDescriptor(m)),
						entry.getValue());
				}
			}

			final ClassReader classReader = new ClassReader(originalBytes);

			final ClassWriter classWriter =
				new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);

			ClassVisitor visitorChain = classWriter;

			// Uncomment to see the generated code:
//      visitorChain =
//        new TraceClassVisitor(visitorChain, new PrintWriter(System.err));

			visitorChain = new AddAdviceClassAdapter(
				visitorChain,
				Type.getType("L" + internalClassName + ";"),
				nameAndDescriptionToWeavingDetails);

			// Uncomment to see the original code:
//      visitorChain =
//        new TraceClassVisitor(visitorChain, new PrintWriter(System.out));

			classReader.accept(visitorChain, 0);

			return classWriter.toByteArray();
		}
	}

	private final class AddAdviceClassAdapter extends ClassVisitor {

		private final Type m_internalClassType;
		private final Map<Pair<String, String>,
			List<WeavingDetails>> m_weavingDetails;

		private AddAdviceClassAdapter(
			ClassVisitor classVisitor,
			Type internalClassType,
			Map<Pair<String, String>, List<WeavingDetails>> weavingDetails) {

			super(Opcodes.ASM9, classVisitor);
			m_internalClassType = internalClassType;
			m_weavingDetails = weavingDetails;
		}

		@Override
		public void visit(int originalVersion,
						  int access,
						  String name,
						  String signature,
						  String superName,
						  String[] interfaces) {

			cv.visit(Math.max(originalVersion & 0xFFFF, Opcodes.V1_8),
				access,
				name,
				signature,
				superName,
				interfaces);
		}

		@Override
		public MethodVisitor visitMethod(final int access,
										 final String name,
										 final String desc,
										 final String signature,
										 final String[] exceptions) {

			final MethodVisitor defaultVisitor =
				cv.visitMethod(access, name, desc, signature, exceptions);

			final List<WeavingDetails> weavingDetails =
				m_weavingDetails.get(Pair.of(name, desc));

			if (weavingDetails != null) {
				assert defaultVisitor != null;

				return new AdviceMethodVisitor(defaultVisitor,
					m_internalClassType,
					access,
					name,
					weavingDetails);
			}

			return defaultVisitor;
		}
	}

	private Map<Weaver.TargetSource, TargetExtractor> m_extractors =
		new HashMap<Weaver.TargetSource, TargetExtractor>() { {
			put(Weaver.TargetSource.CLASS, new ClassTargetExtractor());
			put(Weaver.TargetSource.FIRST_PARAMETER,
				new LocalVariableTargetExtractor(0));
			put(Weaver.TargetSource.SECOND_PARAMETER,
				new LocalVariableTargetExtractor(1));
			put(Weaver.TargetSource.THIRD_PARAMETER,
				new LocalVariableTargetExtractor(2));
		} };

	private interface TargetExtractor {
		void extract(ContextMethodVisitor methodVisitor);
	}

	private static class LocalVariableTargetExtractor implements TargetExtractor {
		private final int m_variableNumber;

		public LocalVariableTargetExtractor(int variableNumber) {
			m_variableNumber = variableNumber;
		}

		public void extract(ContextMethodVisitor methodVisitor) {
			methodVisitor.visitVarInsn(Opcodes.ALOAD, m_variableNumber);
		}
	}

	private static class ClassTargetExtractor implements TargetExtractor {
		public void extract(ContextMethodVisitor methodVisitor) {
			methodVisitor.visitLdcInsn(methodVisitor.getInternalClassName());
		}
	}

	private interface ContextMethodVisitor {
		Type getInternalClassName();
		void visitVarInsn(int opcode, int var);
		void visitLdcInsn(Object cst);
	}

	/**
	 * <p>
	 * Generate our advice.
	 * </p>
	 * <p>
	 * Originally this was based on
	 * {@link org.objectweb.asm.commons.AdviceAdapter}. This had the following
	 * problems:
	 * </p>
	 * <ul>
	 * <li>
	 * 95% of {@code AdviceAdapter} code exists to call
	 * {@link org.objectweb.asm.commons.AdviceAdapter#onMethodEnter()} for a
	 * constructor after it has called {@code this()} or {@code super()}. This
	 * seems unnatural for our purposes, we really want to wrap our {@code
	 * TRYCATCHBLOCK} around the whole constructor.</li>
	 * <li>
	 * {@code AdviceAdapter} doesn't handle exceptions that propagate through the
	 * method, so we must add our own {@code TRYCATCHBLOCK} handling. We need to
	 * ignore add code before other adapters in the chain {@see
	 * #visitTryCatchBlock}), which the {@code onMethodEnter} callback doesn't let
	 * us do.</li>
	 * <li>
	 * The {@code AdviceAdapter} class Javadoc doesn't match the implementation -
	 * a smell.</li>
	 * <li>
	 * {@code AdviceAdapter} was the only reason we required the ASM {@code
	 * commons} jar.</li>
	 *
	 * </ul>
	 *
	 * @author Philip Aston
	 */
	private final class AdviceMethodVisitor
		extends MethodVisitor implements ContextMethodVisitor, Opcodes {

		private final Type m_internalClassType;
		private final List<WeavingDetails> m_weavingDetails;

		private final Label m_entryLabel = new Label();
		private final Label m_exceptionExitLabel = new Label();
		private boolean m_tryCatchBlockNeeded = true;
		private boolean m_entryCallNeeded = true;

		private AdviceMethodVisitor(MethodVisitor mv,
									Type internalClassType,
									int access,
									String name,
									List<WeavingDetails> weavingDetails) {
			super(Opcodes.ASM9, mv);

			m_internalClassType = internalClassType;
			m_weavingDetails = weavingDetails;
		}

		private void generateTryCatchBlock() {
			if (m_tryCatchBlockNeeded) {
				super.visitTryCatchBlock(m_entryLabel,
					m_exceptionExitLabel,
					m_exceptionExitLabel,
					null);

				m_tryCatchBlockNeeded = false;
			}
		}

		public Type getInternalClassName() {
			return m_internalClassType;
		}

		private void generateEntryCall() {
			if (m_entryCallNeeded) {
				m_entryCallNeeded = false;

				super.visitLabel(m_entryLabel);

				for (WeavingDetails weavingDetails : m_weavingDetails) {
					m_extractors.get(weavingDetails.getTargetSource()).extract(this);
					super.visitLdcInsn(weavingDetails.getLocation());

					super.visitMethodInsn(INVOKESTATIC,
						m_adviceClass,
						"enter",
						"(Ljava/lang/Object;Ljava/lang/String;)V");
				}
			}
		}

		private void generateEntryBlocks() {
			generateTryCatchBlock();
			generateEntryCall();
		}

		private void generateExitCall(boolean success) {
			// Iterate in reverse.
			final ListIterator<WeavingDetails> i =
				m_weavingDetails.listIterator(m_weavingDetails.size());

			while (i.hasPrevious()) {
				final WeavingDetails weavingDetails = i.previous();

				m_extractors.get(weavingDetails.getTargetSource()).extract(this);
				super.visitLdcInsn(weavingDetails.getLocation());

				super.visitInsn(success ? ICONST_1 : ICONST_0);

				super.visitMethodInsn(INVOKESTATIC,
					m_adviceClass,
					"exit",
					"(Ljava/lang/Object;Ljava/lang/String;Z)V");
			}
		}

		/**
		 * To nest well if another similar transformation has been done. we must
		 * ensure that any existing top level TryCatchBlock comes first. Otherwise
		 * our TryCatchBlock would have higher precedence, and other catch blocks
		 * would be skipped.
		 *
		 * <p>
		 * This is the reason for the delayed generateEntryBlock*() calls.
		 * Unfortunately, this considerably adds to the complexity of this adapter.
		 * </p>
		 */
		@Override public void visitTryCatchBlock(Label start,
												 Label end,
												 Label handler,
												 String type) {

			super.visitTryCatchBlock(start, end, handler, type);
			generateTryCatchBlock();
		}

		@Override public void visitLabel(Label label) {
			generateEntryBlocks();
			super.visitLabel(label);
		}

		@Override public void visitFrame(int type,
										 int nLocal,
										 Object[] local,
										 int nStack,
										 Object[] stack) {
			generateEntryBlocks();
			super.visitFrame(type, nLocal, local, nStack, stack);
		}

		public void visitInsn(int opcode) {
			generateEntryBlocks();

			switch (opcode) {
				case RETURN:
				case IRETURN:
				case FRETURN:
				case ARETURN:
				case LRETURN:
				case DRETURN:
					generateExitCall(true);
					break;

				default:
					break;
			}

			super.visitInsn(opcode);
		}

		public void visitIntInsn(int opcode, int operand) {
			generateEntryBlocks();
			super.visitIntInsn(opcode, operand);
		}

		public void visitVarInsn(int opcode, int var) {
			generateEntryBlocks();
			super.visitVarInsn(opcode, var);
		}

		public void visitTypeInsn(int opcode, String type) {
			generateEntryBlocks();
			super.visitTypeInsn(opcode, type);
		}

		public void visitFieldInsn(int opcode,
								   String owner,
								   String name,
								   String desc) {
			generateEntryBlocks();
			super.visitFieldInsn(opcode, owner, name, desc);
		}

		public void visitMethodInsn(int opcode,
									String owner,
									String name,
									String desc) {
			generateEntryBlocks();
			super.visitMethodInsn(opcode, owner, name, desc);
		}

		public void visitJumpInsn(int opcode, Label label) {
			generateEntryBlocks();
			super.visitJumpInsn(opcode, label);
		}

		public void visitLdcInsn(Object cst) {
			generateEntryBlocks();
			super.visitLdcInsn(cst);
		}

		public void visitIincInsn(int var, int increment) {
			generateEntryBlocks();
			super.visitIincInsn(var, increment);
		}

		public void visitTableSwitchInsn(int min,
										 int max,
										 Label dflt,
										 Label[] labels) {
			generateEntryBlocks();
			super.visitTableSwitchInsn(min, max, dflt, labels);
		}

		public void visitLookupSwitchInsn(Label dflt,
										  int[] keys,
										  Label[] labels) {
			generateEntryBlocks();
			super.visitLookupSwitchInsn(dflt, keys, labels);
		}

		public void visitMultiANewArrayInsn(String desc, int dims) {
			generateEntryBlocks();
			super.visitMultiANewArrayInsn(desc, dims);
		}

		@Override public void visitMaxs(int maxStack, int maxLocals) {
			super.visitLabel(m_exceptionExitLabel);
			generateExitCall(false);
			super.visitInsn(ATHROW);       // Re-throw.
			super.visitMaxs(maxStack, maxLocals);
		}
	}
}
