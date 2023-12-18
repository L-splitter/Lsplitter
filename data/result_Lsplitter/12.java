/**
 * This method checks if the actual class is the same as the expected class.
 * If they are the same, it returns null.
 *
 * @param actual   The actual class.
 * @param expected The expected class.
 * @return null if the actual class is the same as the expected class, otherwise it returns the actual class.
 */
private static Class<?> checkIfClassesAreEqual(Class<?> actual, Class<?> expected) {
    if (actual == expected) {
        return null;
    }
    return actual;
}
/**
 * This method checks if the actual class is a def.class and returns the appropriate PainlessCast.
 *
 * @param actual   The actual class.
 * @param expected The expected class.
 * @param explicit The explicit flag.
 * @return The appropriate PainlessCast if the actual class is a def.class, otherwise it returns null.
 */
private static PainlessCast checkIfActualIsDef(Class<?> actual, Class<?> expected, boolean explicit) {
    if (actual == def.class) {
        return PainlessCast.originalTypetoTargetType(def.class, expected, explicit);
    }
    return null;
}
/**
 * This method checks if the actual class is a String.class and returns the appropriate PainlessCast.
 *
 * @param actual   The actual class.
 * @param expected The expected class.
 * @param explicit The explicit flag.
 * @return The appropriate PainlessCast if the actual class is a String.class, otherwise it returns null.
 */
private static PainlessCast checkIfActualIsString(Class<?> actual, Class<?> expected, boolean explicit) {
    if (actual == String.class && expected == char.class && explicit) {
        return PainlessCast.originalTypetoTargetType(String.class, char.class, true);
    }
    return null;
}
// Similar methods can be created for other classes like boolean.class, byte.class, short.class, etc.
/**
 * This method checks if the actual class can be cast to the expected class.
 * If they can be cast, it returns the appropriate PainlessCast.
 * If they cannot be cast, it throws a ClassCastException.
 *
 * @param location The location.
 * @param actual   The actual class.
 * @param expected The expected class.
 * @param explicit The explicit flag.
 * @return The appropriate PainlessCast if the actual class can be cast to the expected class.
 * @throws ClassCastException if the actual class cannot be cast to the expected class.
 */
private static PainlessCast checkIfClassesCanBeCast(Location location, Class<?> actual, Class<?> expected, boolean explicit) {
    if ((actual == def.class && expected != void.class)
        || (actual != void.class && expected == def.class)
        || expected.isAssignableFrom(actual)
        || (actual.isAssignableFrom(expected) && explicit)) {
        return PainlessCast.originalTypetoTargetType(actual, expected, explicit);
    } else {
        throw location.createError(
            new ClassCastException(
                "Cannot cast from "
                    + "["
                    + PainlessLookupUtility.typeToCanonicalTypeName(actual)
                    + "] to "
                    + "["
                    + PainlessLookupUtility.typeToCanonicalTypeName(expected)
                    + "]."
            )
        );
    }
}
public static PainlessCast getLegalCast(Location location, Class<?> actual, Class<?> expected, boolean explicit, boolean internal) {
    Objects.requireNonNull(actual);
    Objects.requireNonNull(expected);
    actual = checkIfClassesAreEqual(actual, expected);
    if (actual == null) {
        return null;
    }
    PainlessCast painlessCast = checkIfActualIsDef(actual, expected, explicit);
    if (painlessCast != null) {
        return painlessCast;
    }
    painlessCast = checkIfActualIsString(actual, expected, explicit);
    if (painlessCast != null) {
        return painlessCast;
    }
    // Similar checks can be done for other classes like boolean.class, byte.class, short.class, etc.
    return checkIfClassesCanBeCast(location, actual, expected, explicit);
}
