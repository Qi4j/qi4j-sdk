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
package org.qi4j.library.sql.generator.grammar.common.datatypes;

/**
 * This class represents {@code TIME} data type.
 *
 */
public interface SQLTime
    extends SQLDataType, ParametrizableDataType
{

    /**
     * Returns the precision for this {@code TIME}. May be {@code null}.
     *
     * @return The precision for this {@code TIME}.
     */
    Integer getPrecision();

    /**
     * Returns whether the {@code TIME} should be with time zone. May be {@code null} if no choice specified.
     *
     * @return true if the time includes the timezone.
     */
    Boolean isWithTimeZone();
}
