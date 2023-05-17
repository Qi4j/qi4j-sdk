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
package org.qi4j.library.sql.generator.implementation.grammar.common.datatypes;

import org.qi4j.library.sql.generator.grammar.common.datatypes.SQLDataType;
import org.qi4j.library.sql.generator.grammar.common.datatypes.UserDefinedType;
import org.qi4j.library.sql.generator.implementation.TypeableImpl;

/**
 *
 */
public class UserDefinedTypeImpl extends TypeableImpl<SQLDataType, UserDefinedType>
    implements UserDefinedType
{

    private final String _text;

    public UserDefinedTypeImpl( String textContent )
    {
        this( UserDefinedType.class, textContent );
    }

    protected UserDefinedTypeImpl( Class<? extends UserDefinedType> realImplementingType, String textContent )
    {
        super( realImplementingType );

        this._text = textContent;
    }

    @Override
    protected boolean doesEqual( UserDefinedType another )
    {
        return bothNullOrEquals( this._text, another.getTextualRepresentation() );
    }

    public String getTextualRepresentation()
    {
        return this._text;
    }
}
