/*
	* Copyright (C) 2002-2022 Sebastiano Vigna
	*
	* Licensed under the Apache License, Version 2.0 (the "License");
	* you may not use this file except in compliance with the License.
	* You may obtain a copy of the License at
	*
	*     http://www.apache.org/licenses/LICENSE-2.0
	*
	* Unless required by applicable law or agreed to in writing, software
	* distributed under the License is distributed on an "AS IS" BASIS,
	* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	* See the License for the specific language governing permissions and
	* limitations under the License.
	*/
package marcel.lang.primitives.spliterators;

import marcel.lang.util.function.FloatConsumer;

import java.util.Spliterator;
import java.util.function.Consumer;

/** A type-specific {@link Spliterator}; provides an additional methods to avoid (un)boxing, and
	* the possibility to skip elements.
	*
	* @author C. Sean Young &lt;csyoung@google.com&gt;
	* @see Spliterator
	* @since 8.5.0
	*/
public interface FloatSpliterator extends Spliterator.OfPrimitive<Float, FloatConsumer, FloatSpliterator> {

	/** Skips the given number of elements.
	 *
	 * <p>The effect of this call is exactly the same as that of calling {@link #tryAdvance} for
	 * {@code n} times (possibly stopping if {@link #tryAdvance} returns false).
	 * The action called will do nothing; elements will be discarded.
	 *
	 * @implSpec This default implementation is linear in n. It is expected concrete implementations
	 * that are capable of it will override it to run lower time, but be prepared for linear time.
	 *
	 * @param n the number of elements to skip.
	 * @return the number of elements actually skipped.
	 * @see Spliterator#tryAdvance
	 */
	default long skip(final long n) {
	 if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
	 long i = n;
	 while(i-- != 0 && tryAdvance((float unused) -> {})) {} // No loop body; logic all happens in conditional
	 return n - i - 1;
	}
	/**
	  * {@inheritDoc}
	  *
	  * @apiNote Note that this specification strengthens the one given in {@link Spliterator#trySplit()}.
	  */
	@Override
  FloatSpliterator trySplit();

	default boolean tryAdvance(Consumer<? super Float> action) {
		if (action instanceof FloatConsumer) {
			return tryAdvance((FloatConsumer) action);
		}
		else {
			return tryAdvance((FloatConsumer) action::accept);
		}
	}
}
