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
package org.qi4j.tutorials.composites.tutorial7;

import org.qi4j.api.injection.scope.This;

/**
 * This is the implementation of the HelloWorld
 * behaviour interface.
 * <p>
 * It uses a @This DependencyModel Injection
 * annotation to access the state of the Composite. The field
 * will be automatically injected when the Composite
 * is instantiated. Injections of resources or references
 * can be provided either to fields, constructor parameters or method parameters.
 * </p>
 */
public class HelloWorldBehaviourMixin
    implements HelloWorldBehaviour
{
    @This
    HelloWorldState state;

    @Override
    public String say()
    {
        return state.getPhrase() + " " + state.getName();
    }
}
