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
package org.qi4j.index.opensearch;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.query.grammar.ComparisonPredicate;
import org.qi4j.api.query.grammar.ContainsAllPredicate;
import org.qi4j.api.query.grammar.ContainsPredicate;
import org.qi4j.api.query.grammar.Variable;


final class OpenSearchFinderSupport
{

    static Object resolveVariable(Object value, Map<String, Object> variables)
    {
        if(value == null)
        {
            return null;
        }
        if(value instanceof Variable)
        {
            Variable var = (Variable) value;
            Object realValue = variables.get(var.variableName());
            if(realValue == null)
            {
                throw new IllegalArgumentException("Variable " + var.variableName() + " not bound");
            }
            return realValue;
        }
        return value;
    }

    static FieldValue resolveFieldValue(Object value)
    {
        if(value instanceof Long v)      return FieldValue.of(v);
        if(value instanceof Integer v)   return FieldValue.of(v);
        if(value instanceof Short v)     return FieldValue.of(v);
        if(value instanceof Byte v)      return FieldValue.of(v);
        if(value instanceof Float v)     return FieldValue.of(v);
        if(value instanceof Double v)    return FieldValue.of(v);
        if(value instanceof Boolean v)   return FieldValue.of(v);
        if(value instanceof String v)    return FieldValue.of(v);

        // Big numbers
        if(value instanceof BigInteger v)
        {
            // Prefer exact integer semantics where possible
            if(v.bitLength() <= 63) {
                return FieldValue.of(v.longValue());
            }
            // Fallback (lossy) to double for very large values; consider mapping as scaled_float if needed
            return FieldValue.of(v.doubleValue());
        }
        if(value instanceof BigDecimal v)
        {
            // Fallback (lossy) to double; for exact precision, map field as scaled_float and scale before indexing/querying
            return FieldValue.of(v.doubleValue());
        }

        // Temporal types -> epoch millis (numeric range-friendly)
        if(value instanceof Date v)
        {
            return FieldValue.of(v.getTime());
        }
        if(value instanceof Instant v)
        {
            return FieldValue.of(v.toEpochMilli());
        }
        if(value instanceof LocalDateTime v)
        {
            long epochMillis = v.toInstant(ZoneOffset.UTC).toEpochMilli();
            return FieldValue.of(epochMillis);
        }
        if(value instanceof LocalDate v)
        {
            long epochMillis = v.toEpochDay();
            return FieldValue.of(epochMillis);
        }
        if( value instanceof StringIdentity v)
        {
            return FieldValue.of(v.toString());
        }
        if( value instanceof Enum<?> v)
        {
            return FieldValue.of(v.name());
        }
        throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
    }


    interface ComplexTypeSupport
    {

        Query.Builder comparison(ComparisonPredicate<?> spec, Map<String, Object> variables);

        Query.Builder contains(ContainsPredicate<?> spec, Map<String, Object> variables);

        Query.Builder containsAll(ContainsAllPredicate<?> spec, Map<String, Object> variables);

    }

    private OpenSearchFinderSupport()
    {
    }

}
