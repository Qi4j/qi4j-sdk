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
package org.qi4j.tools.model.descriptor;

import javax.json.JsonObjectBuilder;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.util.Visitable;
import org.qi4j.api.util.Visitor;

/**
 * Transient Detail Descriptor.
 */
public class TransientDetailDescriptor
    extends CompositeDetailDescriptor<TransientDescriptor>
    implements Visitable<TransientDetailDescriptor>
{
    TransientDetailDescriptor( TransientDescriptor aDescriptor )
    {
        super( aDescriptor );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( Visitor<? super TransientDetailDescriptor, ThrowableType> visitor )
        throws ThrowableType
    {
        return visitor.visit( this );
    }

    public JsonObjectBuilder toJson()
    {
        JsonObjectBuilder builder = super.toJson();
        return builder;
    }
}
