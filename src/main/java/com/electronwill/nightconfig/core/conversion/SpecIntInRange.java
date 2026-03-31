package com.electronwill.nightconfig.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the value of a field must be in a certain range (inclusive).
 *
 * @author TheElectronWill
 * @deprecated Use the new package {@link com.electronwill.nightconfig.core.serde} with {@code serde.annotations}.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SpecIntInRange {
    /**
     * @return the minimum possible value, inclusive
     */
    int min();

    /**
     * @return the maximum possible value, inclusive
     */
    int max();
}