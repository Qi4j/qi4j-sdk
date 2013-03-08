/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.api.association;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.StateDescriptor;

/**
 * Associations State Descriptor.
 */
public interface AssociationStateDescriptor extends StateDescriptor
{
    AssociationDescriptor getAssociationByName( String name )
        throws IllegalArgumentException;

    AssociationDescriptor getAssociationByQualifiedName( QualifiedName name )
        throws IllegalArgumentException;

    AssociationDescriptor getManyAssociationByName( String name )
        throws IllegalArgumentException;

    AssociationDescriptor getManyAssociationByQualifiedName( QualifiedName name )
        throws IllegalArgumentException;

    Iterable<? extends AssociationDescriptor> associations();

    Iterable<? extends AssociationDescriptor> manyAssociations();
}
