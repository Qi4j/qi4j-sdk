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

package org.qi4j.envisage.school.domain.school.assembly;

import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.envisage.school.domain.school.School;
import org.qi4j.envisage.school.domain.school.Student;
import org.qi4j.envisage.school.domain.school.Subject;
import org.qi4j.envisage.school.domain.school.School;
import org.qi4j.envisage.school.domain.school.Subject;

@Mixins( StudentEntity.StudentMixin.class )
public interface StudentEntity
    extends Student, HasIdentity
{
    class StudentMixin
        implements Student
    {
        @This
        private StudentState state;

        @Override
        public School school()
        {
            return state.school().get();
        }

        @Override
        public Iterable<Subject> subjects()
        {
            return state.subjects();
        }
    }

    interface StudentState
    {
        ManyAssociation<Subject> subjects();

        Association<School> school();

        Property<Identity> schoolId();
    }

}
