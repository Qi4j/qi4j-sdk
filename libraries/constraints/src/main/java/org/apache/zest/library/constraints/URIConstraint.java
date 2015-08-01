/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.zest.library.constraints;

import java.net.URISyntaxException;
import org.apache.zest.api.constraint.Constraint;
import org.apache.zest.library.constraints.annotation.URI;

public class URIConstraint
    implements Constraint<URI, String>
{

    private static final long serialVersionUID = 1L;

    @Override
    @SuppressWarnings( "ResultOfObjectAllocationIgnored" )
    public boolean isValid( URI annotation, String value )
    {
        try
        {
            new java.net.URI( value );
            return true;
        }
        catch( URISyntaxException ignored )
        {
            return false;
        }
    }

}