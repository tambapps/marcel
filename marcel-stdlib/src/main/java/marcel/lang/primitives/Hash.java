package marcel.lang.primitives;

public interface Hash {

	/** The initial default size of a hash table. */
	int DEFAULT_INITIAL_SIZE = 16;
	/** The default load factor of a hash table. */
	float DEFAULT_LOAD_FACTOR = .75f;
	/** The load factor for a (usually small) table that is meant to be particularly fast. */
	float FAST_LOAD_FACTOR = .5f;
	/** The load factor for a (usually very small) table that is meant to be extremely fast. */
	float VERY_FAST_LOAD_FACTOR = .25f;

	/** A generic hash strategy.
	 *
	 * <p>Note that the {@link #equals(Object,Object) equals()} method of a strategy must
	 * be able to handle {@code null}, too.
	 */

	interface Strategy<K> {

		/** Returns the hash code of the specified object with respect to this hash strategy.
		 *
		 * @param o an object (or {@code null}).
		 * @return the hash code of the given object with respect to this hash strategy.
		 */

		int hashCode(K o);

		/** Returns true if the given objects are equal with respect to this hash strategy.
		 *
		 * @param a an object (or {@code null}).
		 * @param b another object (or {@code null}).
		 * @return true if the two specified objects are equal with respect to this hash strategy.
		 */
		boolean equals(K a, K b);
	}

	/** The default growth factor of a hash table. */
	@Deprecated
	int DEFAULT_GROWTH_FACTOR = 16;
	/** The state of a free hash table entry. */
	@Deprecated
	byte FREE = 0;
	/** The state of a occupied hash table entry. */
	@Deprecated
	byte OCCUPIED = -1;
	/** The state of a hash table entry freed by a deletion. */
	@Deprecated
	byte REMOVED = 1;

	/** A list of primes to be used as table sizes. The <var>i</var>-th element is
	 *  the largest prime <var>p</var> smaller than 2<sup>(<var>i</var>+28)/16</sup>
	 * and such that <var>p</var>-2 is also prime (or 1, for the first few entries). */

	@Deprecated
	int PRIMES[] = { 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 5, 5, 5, 5, 5, 5, 7, 7, 7,
								  7, 7, 7, 7, 7, 7, 7, 7, 13, 13, 13, 13, 13, 13, 13, 13, 19, 19, 19, 19, 19,
								  19, 19, 19, 19, 19, 19, 19, 31, 31, 31, 31, 31, 31, 31, 43, 43, 43, 43, 43,
								  43, 43, 43, 61, 61, 61, 61, 61, 73, 73, 73, 73, 73, 73, 73, 103, 103, 109,
								  109, 109, 109, 109, 139, 139, 151, 151, 151, 151, 181, 181, 193, 199, 199,
								  199, 229, 241, 241, 241, 271, 283, 283, 313, 313, 313, 349, 349, 349, 349,
								  421, 433, 463, 463, 463, 523, 523, 571, 601, 619, 661, 661, 661, 661, 661,
								  823, 859, 883, 883, 883, 1021, 1063, 1093, 1153, 1153, 1231, 1321, 1321,
								  1429, 1489, 1489, 1621, 1699, 1789, 1873, 1951, 2029, 2131, 2143, 2311,
								  2383, 2383, 2593, 2731, 2803, 3001, 3121, 3259, 3391, 3583, 3673, 3919,
								  4093, 4273, 4423, 4651, 4801, 5023, 5281, 5521, 5743, 5881, 6301, 6571,
								  6871, 7129, 7489, 7759, 8089, 8539, 8863, 9283, 9721, 10141, 10531, 11071,
								  11551, 12073, 12613, 13009, 13759, 14323, 14869, 15649, 16363, 17029,
								  17839, 18541, 19471, 20233, 21193, 22159, 23059, 24181, 25171, 26263,
								  27541, 28753, 30013, 31321, 32719, 34213, 35731, 37309, 38923, 40639,
								  42463, 44281, 46309, 48313, 50461, 52711, 55051, 57529, 60091, 62299,
								  65521, 68281, 71413, 74611, 77713, 81373, 84979, 88663, 92671, 96739,
								  100801, 105529, 109849, 115021, 120079, 125509, 131011, 136861, 142873,
								  149251, 155863, 162751, 169891, 177433, 185071, 193381, 202129, 211063,
								  220021, 229981, 240349, 250969, 262111, 273643, 285841, 298411, 311713,
								  325543, 339841, 355009, 370663, 386989, 404269, 422113, 440809, 460081,
								  480463, 501829, 524221, 547399, 571603, 596929, 623353, 651019, 679909,
								  709741, 741343, 774133, 808441, 844201, 881539, 920743, 961531, 1004119,
								  1048573, 1094923, 1143283, 1193911, 1246963, 1302181, 1359733, 1420039,
								  1482853, 1548541, 1616899, 1688413, 1763431, 1841293, 1922773, 2008081,
								  2097133, 2189989, 2286883, 2388163, 2493853, 2604013, 2719669, 2840041,
								  2965603, 3097123, 3234241, 3377191, 3526933, 3682363, 3845983, 4016041,
								  4193803, 4379719, 4573873, 4776223, 4987891, 5208523, 5439223, 5680153,
								  5931313, 6194191, 6468463, 6754879, 7053331, 7366069, 7692343, 8032639,
								  8388451, 8759953, 9147661, 9552733, 9975193, 10417291, 10878619, 11360203,
								  11863153, 12387841, 12936529, 13509343, 14107801, 14732413, 15384673,
								  16065559, 16777141, 17519893, 18295633, 19105483, 19951231, 20834689,
								  21757291, 22720591, 23726449, 24776953, 25873963, 27018853, 28215619,
								  29464579, 30769093, 32131711, 33554011, 35039911, 36591211, 38211163,
								  39903121, 41669479, 43514521, 45441199, 47452879, 49553941, 51747991,
								  54039079, 56431513, 58930021, 61539091, 64263571, 67108669, 70079959,
								  73182409, 76422793, 79806229, 83339383, 87029053, 90881083, 94906249,
								  99108043, 103495879, 108077731, 112863013, 117860053, 123078019, 128526943,
								  134217439, 140159911, 146365159, 152845393, 159612601, 166679173,
								  174058849, 181765093, 189812341, 198216103, 206991601, 216156043,
								  225726379, 235720159, 246156271, 257054491, 268435009, 280319203,
								  292730833, 305691181, 319225021, 333358513, 348117151, 363529759,
								  379624279, 396432481, 413983771, 432312511, 451452613, 471440161,
								  492312523, 514109251, 536870839, 560640001, 585461743, 611382451,
								  638450569, 666717199, 696235363, 727060069, 759249643, 792864871,
								  827967631, 864625033, 902905501, 942880663, 984625531, 1028218189,
								  1073741719, 1121280091, 1170923713, 1222764841, 1276901371, 1333434301,
								  1392470281, 1454120779, 1518500173, 1585729993, 1655935399, 1729249999,
								  1805811253, 1885761133, 1969251079, 2056437379, 2147482951 };

	// hash common


	/** 2<sup>32</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2. */
	static final int INT_PHI = 0x9E3779B9;
	/** The reciprocal of {@link #INT_PHI} modulo 2<sup>32</sup>. */
	static final int INV_INT_PHI = 0x144cbc89;
	/** 2<sup>64</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2. */
	static final long LONG_PHI = 0x9E3779B97F4A7C15L;
	/** The reciprocal of {@link #LONG_PHI} modulo 2<sup>64</sup>. */
	static final long INV_LONG_PHI = 0xf1de83e19937733dL;

	/** Avalanches the bits of an integer by applying the finalisation step of MurmurHash3.
	 *
	 * <p>This method implements the finalisation step of Austin Appleby's <a href="http://code.google.com/p/smhasher/">MurmurHash3</a>.
	 * Its purpose is to avalanche the bits of the argument to within 0.25% bias.
	 *
	 * @param x an integer.
	 * @return a hash value with good avalanching properties.
	 */
	public static int murmurHash3(int x) {
		x ^= x >>> 16;
		x *= 0x85ebca6b;
		x ^= x >>> 13;
		x *= 0xc2b2ae35;
		x ^= x >>> 16;
		return x;
	}


	/** Avalanches the bits of a long integer by applying the finalisation step of MurmurHash3.
	 *
	 * <p>This method implements the finalisation step of Austin Appleby's <a href="http://code.google.com/p/smhasher/">MurmurHash3</a>.
	 * Its purpose is to avalanche the bits of the argument to within 0.25% bias.
	 *
	 * @param x a long integer.
	 * @return a hash value with good avalanching properties.
	 */
	public static long murmurHash3(long x) {
		x ^= x >>> 33;
		x *= 0xff51afd7ed558ccdL;
		x ^= x >>> 33;
		x *= 0xc4ceb9fe1a85ec53L;
		x ^= x >>> 33;
		return x;
	}

	/** Quickly mixes the bits of an integer.
	 *
	 * <p>This method mixes the bits of the argument by multiplying by the golden ratio and
	 * xorshifting the result. It is borrowed from <a href="https://github.com/leventov/Koloboke">Koloboke</a>, and
	 * it has slightly worse behaviour than {@link #murmurHash3(int)} (in open-addressing hash tables the average number of probes
	 * is slightly larger), but it's much faster.
	 *
	 * @param x an integer.
	 * @return a hash value obtained by mixing the bits of {@code x}.
	 * @see #invMix(int)
	 */
	public static int mix(final int x) {
		final int h = x * INT_PHI;
		return h ^ (h >>> 16);
	}

	/** The inverse of {@link #mix(int)}. This method is mainly useful to create unit tests.
	 *
	 * @param x an integer.
	 * @return a value that passed through {@link #mix(int)} would give {@code x}.
	 */
	public static int invMix(final int x) {
		return (x ^ x >>> 16) * INV_INT_PHI;
	}

	/** Quickly mixes the bits of a long integer.
	 *
	 * <p>This method mixes the bits of the argument by multiplying by the golden ratio and
	 * xorshifting twice the result. It is borrowed from <a href="https://github.com/leventov/Koloboke">Koloboke</a>, and
	 * it has slightly worse behaviour than {@link #murmurHash3(long)} (in open-addressing hash tables the average number of probes
	 * is slightly larger), but it's much faster.
	 *
	 * @param x a long integer.
	 * @return a hash value obtained by mixing the bits of {@code x}.
	 */
	public static long mix(final long x) {
		long h = x * LONG_PHI;
		h ^= h >>> 32;
		return h ^ (h >>> 16);
	}

	/** The inverse of {@link #mix(long)}. This method is mainly useful to create unit tests.
	 *
	 * @param x a long integer.
	 * @return a value that passed through {@link #mix(long)} would give {@code x}.
	 */
	public static long invMix(long x) {
		x ^= x >>> 32;
		x ^= x >>> 16;
		return (x ^ x >>> 32) * INV_LONG_PHI;
	}


	/** Returns the hash code that would be returned by {@link Float#hashCode()}.
	 *
	 * @param f a float.
	 * @return the same code as {@link Float#hashCode() new Float(f).hashCode()}.
	 */

	public static int float2int(final float f) {
		return Float.floatToRawIntBits(f);
	}

	/** Returns the hash code that would be returned by {@link Double#hashCode()}.
	 *
	 * @param d a double.
	 * @return the same code as {@link Double#hashCode() new Double(f).hashCode()}.
	 */

	public static int double2int(final double d) {
		final long l = Double.doubleToRawLongBits(d);
		return (int)(l ^ (l >>> 32));
	}

	/** Returns the hash code that would be returned by {@link Long#hashCode()}.
	 *
	 * @param l a long.
	 * @return the same code as {@link Long#hashCode() new Long(f).hashCode()}.
	 */
	public static int long2int(final long l) {
		return (int)(l ^ (l >>> 32));
	}

	/** Returns the least power of two greater than or equal to the specified value.
	 *
	 * <p>Note that this function will return 1 when the argument is 0.
	 *
	 * @param x an integer smaller than or equal to 2<sup>30</sup>.
	 * @return the least power of two greater than or equal to the specified value.
	 */
	public static int nextPowerOfTwo(int x) {
		return 1 << (32 - Integer.numberOfLeadingZeros(x - 1));
	}

	/** Returns the least power of two greater than or equal to the specified value.
	 *
	 * <p>Note that this function will return 1 when the argument is 0.
	 *
	 * @param x a long integer smaller than or equal to 2<sup>62</sup>.
	 * @return the least power of two greater than or equal to the specified value.
	 */
	public static long nextPowerOfTwo(long x) {
		return 1L << (64 - Long.numberOfLeadingZeros(x - 1));
	}


	/** Returns the maximum number of entries that can be filled before rehashing.
	 *
	 * @param n the size of the backing array.
	 * @param f the load factor.
	 * @return the maximum number of entries before rehashing.
	 */
	public static int maxFill(final int n, final float f) {
		/* We must guarantee that there is always at least
		 * one free entry (even with pathological load factors). */
		return Math.min((int)Math.ceil(n * f), n - 1);
	}

	/** Returns the maximum number of entries that can be filled before rehashing.
	 *
	 * @param n the size of the backing array.
	 * @param f the load factor.
	 * @return the maximum number of entries before rehashing.
	 */
	public static long maxFill(final long n, final float f) {
		/* We must guarantee that there is always at least
		 * one free entry (even with pathological load factors). */
		return Math.min((long)Math.ceil(n * f), n - 1);
	}

	/** Returns the least power of two smaller than or equal to 2<sup>30</sup> and larger than or equal to {@code Math.ceil(expected / f)}.
	 *
	 * @param expected the expected number of elements in a hash table.
	 * @param f the load factor.
	 * @return the minimum possible size for a backing array.
	 * @throws IllegalArgumentException if the necessary size is larger than 2<sup>30</sup>.
	 */
	public static int arraySize(final int expected, final float f) {
		final long s = Math.max(2, nextPowerOfTwo((long)Math.ceil(expected / f)));
		if (s > (1 << 30)) throw new IllegalArgumentException("Too large (" + expected + " expected elements with load factor " + f + ")");
		return (int)s;
	}
}