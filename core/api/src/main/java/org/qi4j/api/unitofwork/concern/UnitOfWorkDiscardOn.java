/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.qi4j.api.unitofwork.concern;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to denote the unit of work discard policy.
 * <p>
 * By default, discard is applied on any method that has {@link UnitOfWorkPropagation} and any exception is thrown.
 * </p>
 * <p>
 * Apply {@code UnitOfWorkDiscardOn} to override the default settings.
 * </p>
 * <p>
 * Usage example:
 * </p>
 * <pre>
 * <code>
 *
 * &#64;Concerns( UnitOfWorkConcern.class )
 * public class MyBusinessServiceMixin implements BusinessService
 * {
 *   &#64;Structure UnitOfWorkFactory uowf;
 *
 *   &#64;UnitOfWorkDiscardOn( MyBusinessException.class )
 *   public void myBusinessMethod()
 *   {
 *     // Must invoke current unit of work.
 *     UnitOfWork uow = uowf.currentUnitOfWork();
 *
 *     // Perform business logic
 *   }
 * }
 * </code>
 * </pre>
 *
 * <p>
 * The unit of work will be discarded iff {@code MyBusinessException} exceptions or its subclass is thrown from within
 * {@code myBusinessMethod} method.
 * </p>
 */
@Retention( RUNTIME )
@Target( METHOD )
@Inherited
@Documented
public @interface UnitOfWorkDiscardOn
{
    Class<? extends Throwable>[] value() default { Throwable.class };
}
