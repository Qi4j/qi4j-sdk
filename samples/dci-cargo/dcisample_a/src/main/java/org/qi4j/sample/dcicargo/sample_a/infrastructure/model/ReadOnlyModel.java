/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_a.infrastructure.model;

import org.apache.wicket.model.IModel;
import org.qi4j.api.Qi4j;
import org.qi4j.api.structure.Module;
import org.qi4j.sample.dcicargo.sample_a.infrastructure.conversion.EntityToDTOService;

/**
 * Abstract base model for Wicket model objects taking Qi4j objects.
 */
public abstract class ReadOnlyModel<T>
    implements IModel<T>
{
    private static final long serialVersionUID = 1L;

    static protected EntityToDTOService valueConverter;
    static protected Qi4j qi4j;
    static protected Module module;

    /**
     * This default implementation of setObject unconditionally throws an
     * UnsupportedOperationException. Since the method is final, any subclass is effectively a
     * read-only model.
     *
     * @param object The object to set into the model
     *
     * @throws UnsupportedOperationException
     */
    public final void setObject( final T object )
    {
        throw new UnsupportedOperationException( "Model " + getClass() +
                                                 " does not support setObject(Object)" );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "Model:classname=[" );
        sb.append( getClass().getName() ).append( "]" );
        return sb.toString();
    }

    public static void prepareModelBaseClass( Module m,
                                              Qi4j api,
                                              EntityToDTOService entityToDTO
    )
    {
        module = m;
        qi4j = api;
        valueConverter = entityToDTO;
    }
}