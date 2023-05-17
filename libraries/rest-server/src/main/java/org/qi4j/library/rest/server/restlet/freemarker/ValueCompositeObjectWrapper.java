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
package org.qi4j.library.rest.server.restlet.freemarker;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;
import org.qi4j.api.value.ValueComposite;

/**
 * ObjectWrapper implementation that adds support for ValueComposites, exposing Property state.
 */
public class ValueCompositeObjectWrapper
    extends DefaultObjectWrapper
{
    public ValueCompositeObjectWrapper( Version freemarkerVersion )
    {
        super( freemarkerVersion );
    }

    @Override
    public TemplateModel wrap( Object obj )
        throws TemplateModelException
    {
        if( obj instanceof ValueComposite )
        {
            return new ValueCompositeTemplateModel( (ValueComposite) obj, this );
        }
        else
        {
            return super.wrap( obj );
        }
    }
}
