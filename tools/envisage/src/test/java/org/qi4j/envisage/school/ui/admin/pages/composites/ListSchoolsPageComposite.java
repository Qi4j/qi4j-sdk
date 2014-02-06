/*
 * Copyright (c) 2008, Edward Yakop. All Rights Reserved.
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
package org.qi4j.envisage.school.ui.admin.pages.composites;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.envisage.school.domain.school.School;
import org.qi4j.envisage.school.domain.school.SchoolRepository;
import org.qi4j.envisage.school.ui.admin.pages.mixins.Page;

@Mixins( ListSchoolsPageComposite.ListSchoolsPageMixin.class )
public interface ListSchoolsPageComposite
    extends Page, TransientComposite
{

    class ListSchoolsPageMixin
        implements Page
    {

        @Service
        Iterable<ServiceComposite> services;
        @Service
        SchoolRepository schools;

        @Override
        public String generateHtml()
        {
            String html = "<ul>";
            for( School school : schools.findAll() )
            {
                html += "<li>" + school.name() + "</li>";
            }
            html += "</ul>";

            return html;
        }
    }

}
