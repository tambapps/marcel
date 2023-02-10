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
package marcel.lang.primitives.iterable;

import marcel.lang.primitives.iterators.LongIterator;

import java.util.function.Consumer;

/** A type-specific {@link Iterable} that strengthens that specification of {@link #iterator()} and {@link #forEach(Consumer)}.
	*
	* @see Iterable
	*/
public interface LongIterable extends Iterable<Long> {
	/** Returns a type-specific iterator.
	 *
	 * @apiNote Note that this specification strengthens the one given in {@link Iterable#iterator()}.
	 *
	 * @return a type-specific iterator.
	 * @see Iterable#iterator()
	 */
	@Override
	LongIterator iterator();

	/**
	 * Performs the given action for each element of this type-specific {@link java.lang.Iterable}
	 * until all elements have been processed or the action throws an
	 * exception.
	 *
	 * @param action the action to be performed for each element.
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 * @since 8.0.0
	 * @apiNote Implementing classes should generally override this method, and take the default
	 *   implementation of the other overloads which will delegate to this method (after proper
	 *   conversions).
	 */
	default void forEach(final java.util.function.LongConsumer action) {
		iterator().forEachRemaining(action);
	}
}
