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

package org.qi4j.runtime.service;

import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.util.VisitableHierarchy;
import org.qi4j.runtime.composite.CompositesModel;
import org.qi4j.runtime.composite.CompositesModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JAVADOC
 */
public class ServicesModel extends CompositesModel<ServiceModel>
    implements VisitableHierarchy<Object, Object>
{
    public ServicesModel( List<ServiceModel> serviceModels )
    {
        super(serviceModels);
    }

    public ServicesInstance newInstance( ModuleDescriptor module )
    {
        List<ServiceReference<?>> serviceReferences = stream()
                        .map(serviceModel -> new ServiceReferenceInstance(serviceModel, module))
                        .<ServiceReference<?>>map(ServiceReference.class::cast)
                        .collect(Collectors.toList());
        return new ServicesInstance( this, serviceReferences );
    }
}