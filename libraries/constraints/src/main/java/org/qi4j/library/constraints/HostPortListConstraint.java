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
package org.qi4j.library.constraints;

import org.apache.commons.validator.routines.UrlValidator;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.HostPortList;
import org.qi4j.library.constraints.annotation.HostPortList;

import static java.util.Arrays.stream;

/**
 * Implement @HostPortList constraint.
 */
public class HostPortListConstraint
    implements Constraint<HostPortList, String>
{
    private static final UrlValidator VALIDATOR = new UrlValidator( new String[]{ "http" }, UrlValidator.NO_FRAGMENTS );

    @Override
    public boolean isValid( HostPortList annotation, String value )
    {
        return stream( value.split( "[ ,]+" ) )
            .map( this::handleLocalHost )
            .map( this::prefixProtocol )
            .allMatch( VALIDATOR::isValid );
    }

    private String handleLocalHost( String entry )
    {
        if( entry.startsWith( "localhost" ) )
        {
            return "localhost.my" + entry.substring( 9 );
        }
        return entry;
    }

    private String prefixProtocol( String value )
    {
        return "http://" + value;
    }
}
