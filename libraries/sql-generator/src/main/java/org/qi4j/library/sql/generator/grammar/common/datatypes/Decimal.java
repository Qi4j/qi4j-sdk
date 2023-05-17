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
 * <p>
 * This class represents the {@code DECIMAL} data type, sometimes abbreviated as {@code DEC}. The site
 * http://intelligent-enterprise.informationweek.com/000626/celko.jhtml explains difference between {@code NUMERIC} and
 * {@code DECIMAL}:
 * </p>
 * <p>
 * The difference between DECIMAL(s,p) and NUMERIC(s,p) is subtle in the SQL-92 Standard -- DECIMAL(s,p) must be exactly
 * as precise as declared, while NUMERIC(s,p) must be at least as precise as declared.
 * </p>
 *
 */
public interface Decimal
    extends SQLDataType, ParametrizableDataType
{

    /**
     * Returns the precision (first integer) for this {@code DECIMAL}.
     *
     * @return The precision for this {@code DECIMAL}.
     */
    Integer getPrecision();

    /**
     * Returns the scale (second integer) for this {@code DECIMAL}.
     *
     * @return The precision for this {@code DECIMAL}.
     */
    Integer getScale();
}
