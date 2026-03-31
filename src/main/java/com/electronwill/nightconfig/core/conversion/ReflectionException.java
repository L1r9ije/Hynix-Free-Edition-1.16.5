package com.electronwill.nightconfig.core.conversion;

/**
 * Thrown when an important reflective operation fails.
 *
 * @author TheElectronWill
 * @deprecated Use the new package
 * {@link com.electronwill.nightconfig.core.serde} with
 * {@code serde.annotations}.
 */
@Deprecated
public class ReflectionException extends RuntimeException {
    public ReflectionException(String message) {
        super(message);
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
