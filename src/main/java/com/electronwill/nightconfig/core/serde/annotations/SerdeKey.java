package com.electronwill.nightconfig.core.serde.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets the key to use when serializing/deserializing an element, instead of its
 * Java name.
 * <p>
 * Example:
 *
 * <pre>
 * <code>
 * class MyObject {
 *    {@code @SerdeKey("uuid")}
 *    String objectUniqueId;
 *
 *    String withoutAnnotation;
 * }
 * </code>
 * </pre>
 * <p>
 * Serialization to json:
 *
 * <pre>
 * <code>
 * {
 *    "uuid" : "…",
 *    "withoutAnnotation": "…"
 * }
 * </code>
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerdeKey {
    String value();
}
