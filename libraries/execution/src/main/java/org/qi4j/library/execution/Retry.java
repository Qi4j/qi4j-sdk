package org.qi4j.library.execution;

import org.qi4j.api.unitofwork.concern.UnitOfWorkConcern;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * &#64;Retry is a method annotation to automatically call the method again if an exception was thrown.
 * <p>
 * By default, the method will be called twice if any {@link java.lang.Throwable} is thrown. By setting
 * the value, one can increase that number, and the {@link #on()} and {@link #unless()} parameters can
 * be used to select which Throwable (incl its subtypes) the retry will happen on.
 * </p>
 * <p>
 *     This can only be applied to idempotent methods, and keeping in mind the ordering of Concerns may
 *     be very significant. E.g. If the {@link RetryConcern} is "around" the
 *     {@link UnitOfWorkConcern} then depending on the parameters on
 *     the {@link UnitOfWorkPropagation} will determine if the
 *     method is still idempotent or not, in particular
 *     {@link UnitOfWorkPropagation.Propagation#REQUIRES_NEW}. Furthermore,
 *     {@link UnitOfWorkPropagation} has its own Retry mechanism independent
 *     of this one.
 * </p>
 */
@Retention( RUNTIME )
@Target( METHOD )
@Inherited
@Documented
public @interface Retry
{
    /**
     * Number of times that the method should be called.
     * <p>
     *     This number must be 1 or greater, otherwise an {@link IllegalArgumentException} is thrown.
     * </p>
     */
    int value() default 2;

    /**
     * List of Throwables that should trigger the Retry operation.
     * <p>
     * Default: All Throwables.
     * </p>
     */
    Class<? extends Throwable>[] on() default { Throwable.class };

    /**
     * List of Throwables that should NOT trigger the Retry operation, even if they are subclasses found in the on() value
     * <p>
     * Default: none.
     * </p>
     */
    Class<? extends Throwable>[] unless() default {};

    /**
     * Slowing down of retries.
     * <p>
     *     If the backoff is greater than 0 (default), there will be a successive backoff of retrying the call,
     *     and starting with backoff() milliseconds, the sleep time between tries will double for each try.
     */
    int backoff() default 0;
}
