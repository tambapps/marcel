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
package marcel.lang.primitives.iterators;
import java.util.Iterator;
import java.util.PrimitiveIterator;

/** A type-specific {@link Iterator}; provides an additional method to avoid (un)boxing, and
	* the possibility to skip elements.
	*
	* @see Iterator
	*/
public interface LongIterator extends Iterator<Long>, PrimitiveIterator.OfLong {
	/**
	 * Returns the next element as a primitive type.
	 *
	 * @return the next element in the iteration.
	 * @see Iterator#next()
	 */
	long nextLong();

	@Override
	default Long next() {
	 return nextLong();
	}

}
