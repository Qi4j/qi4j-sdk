/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.lib.swing.binding;

import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.property.Property;

import javax.swing.*;
import java.util.Set;

public interface SwingAdapter
{
    Set<Capabilities> canHandle();

    void fromSwingToProperty( JComponent component, Property<?> property );

    void fromPropertyToSwing( JComponent component, Property<?> property );

    void fromSwingToAssociation( JComponent component, Association<?> property );

    void fromAssociationToSwing( JComponent component, Association<?> property );

    void fromSwingToSetAssociation( JComponent component, SetAssociation<?> property );

    void fromSetAssociationToSwing( JComponent component, SetAssociation<?> property );

    void fromSwingToListAssociation( JComponent component, ListAssociation<?> property );

    void fromListAssociationToSwing( JComponent component, ListAssociation<?> property );

    public class Capabilities
    {
        public Class<? extends JComponent> component;
        public Class<?> type;
        public boolean property;
        public boolean association;
        public boolean listAssociation;
        public boolean setAssociation;

        public Capabilities( Class<? extends JComponent> component, Class<?> type,
                             boolean property, boolean association, boolean setAssociation,
                             boolean listAssociation )
        {
            this.component = component;
            this.type = type;
            this.property = property;
            this.association = association;
            this.listAssociation = listAssociation;
            this.setAssociation = setAssociation;
        }
    }
}
