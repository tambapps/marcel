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
package marcel.util.primitives.iterators;
import marcel.util.primitives.collections.LongCollection;

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

	default int unwrap(final long array[]) {
		return unwrap(array, 0, array.length);
	}

	default int unwrap(final long array[], int offset, final int max) {
		if (max < 0) throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		if (offset < 0 || offset + max > array.length) throw new IllegalArgumentException();
		int j = max;
		while(j-- != 0 && hasNext()) array[offset++] = nextLong();
		return max - j - 1;
	}

	default int skip(final int n) {
		if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
		int i = n;
		while(i-- != 0 && hasNext()) nextLong();
		return n - i - 1;
	}

	default long unwrap(final LongCollection c) {
		long n = 0;
		while(hasNext()) {
			c.add(nextLong());
			n++;
		}
		return n;
	}
}
