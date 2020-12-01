// Copyright (C) 2009 Philip Aston
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


/**
 * Something that remembers all the point cuts.
 *
 * @author Philip Aston
 */
interface PointCutRegistry {
  /**
   * Return the registered constructor point cuts for a class.
   *
   * @param internalClassName
   *          The name of the class, in internal form. For example, {@code
   *          java/util/List}. Passed through to
   *          {@link ClassFileTransformer#transform}.
   * @return A map of constructors to weaving details. Each method in a class
   *         has at most one location string for a given {@link TargetSource}
   *         type.
   */
  Map<Constructor<?>, List<WeavingDetails>> getConstructorPointCutsForClass(
    String internalClassName);

  /**
   * Return the registered method point cuts for a class.
   *
   * @param internalClassName
   *          The name of the class, in internal form. For example, {@code
   *          java/util/List}. Passed through to
   *          {@link ClassFileTransformer#transform}.
   * @return A map of methods to location strings. Each method in a class has
   *         at most one location string for a given {@link TargetSource}
   *         type.
   */
  Map<Method, List<WeavingDetails>> getMethodPointCutsForClass(
    String internalClassName);
}
