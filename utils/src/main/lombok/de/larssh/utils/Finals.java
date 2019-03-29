package de.larssh.utils;

import java.util.function.Supplier;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.experimental.UtilityClass;

/**
 * This class contains helper methods for final fields.
 */
@UtilityClass
public class Finals {
	/**
	 * Returns {@code value} unchanged. This prevents compilers from inlining
	 * constants (static final fields). Depending classes referring to the constant
	 * do not need to be recompiled if the constant value changes.
	 *
	 * <p>
	 * Inlining affects primitive data types and strings.
	 *
	 * <p>
	 * <b>Usage example:</b>
	 * <pre>public static final String CONSTANT_FIELD = constant("constant value");</pre>
	 *
	 * @param <T>   return type
	 * @param value value
	 * @return value
	 */
	public static <T> T constant(final T value) {
		return value;
	}

	/**
	 * Returns a new {@link Supplier} that calculates its return value at most one
	 * time. Therefore it can be used to create lazy fields.
	 *
	 * <p>
	 * The used implementation synchronizes threads while calling {@code supplier}.
	 *
	 * @param <T>      return type
	 * @param supplier value supplier
	 * @return value
	 */
	public static <T> Supplier<T> lazy(final Supplier<T> supplier) {
		return new CachedSupplier<>(supplier);
	}

	/**
	 * {@link Supplier} implementation proxying another supplier while calculating
	 * its return value at most one time.
	 *
	 * <p>
	 * Threads are synchronized upon calling the wrapped supplier.
	 *
	 * @param <T> return type
	 */
	@RequiredArgsConstructor
	private static class CachedSupplier<T> implements Supplier<T> {
		/**
		 * Wrapped supplier
		 *
		 * @return wrapped supplier
		 */
		Supplier<T> supplier;

		/**
		 * Cached value or empty optional
		 *
		 * @return cached value or empty optional
		 */
		@NonFinal
		@Nullable
		T value = null;

		/** {@inheritDoc} */
		@Override
		public synchronized T get() {
			if (value == null) {
				value = supplier.get();
			}
			return Nullables.orElseThrow(value);
		}
	}
}
