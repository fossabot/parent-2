package de.larssh.utils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.jooq.lambda.function.Consumer3;
import org.joor.Reflect;

import lombok.experimental.UtilityClass;

/**
 * This class contains helper methods for assertion.
 */
@UtilityClass
public class Assertions {
	/**
	 * Asserts that the methods {@link Object#equals(Object)} and
	 * {@link Object#hashCode()} of objects created by executing the {@code clazz}es
	 * constructor work as defined by {@code arguments}.
	 *
	 * <p>
	 * The constructor of {@code clazz} is called multiple times using different
	 * combinations of arguments. The resulting objects equality is checked using
	 * declarations of {@code arguments}.
	 * {@link Reflects#create(Reflect, Object...)} is used to construct instances of
	 * {@code clazz}.
	 *
	 * <p>
	 * {@code arguments} declares two kind of valid arguments for the class
	 * constructor and if changing the argument should change the objects equality.
	 *
	 * <p>
	 * In addition to argument checks {@link Object#equals(Object)} is checked
	 * against {@code null}, {@code true} and the object itself while the first two
	 * are expected to differ from the object itself.
	 *
	 * @param clazz     class to be instantiated based on given arguments
	 * @param arguments declaration of arguments and if changing arguments should
	 *                  change equality
	 */
	public static void assertEqualsAndHashCode(final Class<?> clazz, final AssertEqualsAndHashCodeArguments arguments) {
		final Reflect reflect = Reflect.on(clazz);
		assertEqualsAndHashCode(a -> Reflects.create(reflect, a).get(), arguments);
	}

	/**
	 * Asserts that the methods {@link Object#equals(Object)} and
	 * {@link Object#hashCode()} of objects created by executing {@code constructor}
	 * work as defined by {@code arguments}.
	 *
	 * <p>
	 * {@code constructor} is called multiple times using different combinations of
	 * arguments. The resulting objects equality is checked using declarations of
	 * {@code arguments}.
	 *
	 * <p>
	 * {@code arguments} declares two kind of valid arguments for the constructor
	 * and if changing the argument should change the objects equality.
	 *
	 * <p>
	 * In addition to argument checks {@link Object#equals(Object)} is checked
	 * against {@code null}, {@code true} and the object itself while the first two
	 * are expected to differ from the object itself.
	 *
	 * @param constructor function to construct object instances based on given
	 *                    arguments
	 * @param arguments   declaration of arguments and if changing arguments should
	 *                    change equality
	 */
	public static void assertEqualsAndHashCode(final Function<Object[], ?> constructor,
			final AssertEqualsAndHashCodeArguments arguments) {
		final Object object = constructor.apply(arguments.getOriginal().toArray());
		final int hashCode = object.hashCode();

		assertNotEquals(object, null, "equals null");
		assertNotEquals(object, true, "equals true");
		assertEquals(object, object, "equals this");

		final Object sameObject = constructor.apply(arguments.getOriginal().toArray());
		assertEquals(object, sameObject, "equals original arguments");
		assertEquals(hashCode, sameObject.hashCode(), "hashCode original arguments");

		IntStream.range(0, arguments.getOriginal().size()).forEach(index -> {
			final Consumer3<Object, Object, Supplier<String>> consumer = arguments.isExpectEquality(index)
					? org.junit.jupiter.api.Assertions::assertEquals
					: org.junit.jupiter.api.Assertions::assertNotEquals;
			final Object newObject = constructor.apply(arguments.getChangedArguments(index).toArray());

			// Assert equals
			final Supplier<String> equalsMessage = () -> String
					.format("%sequals changed argument %d", arguments.isExpectEquality(index) ? "" : "not ", index + 1);
			consumer.accept(object, newObject, equalsMessage);

			// Assert hashCode
			final Supplier<String> hashCodeMessage = () -> String.format("hashCode changed argument %d", index + 1);
			consumer.accept(hashCode, newObject.hashCode(), hashCodeMessage);
		});
	}
}
