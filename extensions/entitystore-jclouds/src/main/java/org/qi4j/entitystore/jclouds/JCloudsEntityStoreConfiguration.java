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

package org.qi4j.entitystore.jclouds;

import java.util.Map;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;

/**
 * Configuration of JCloudsMapEntityStore service.
 */
public interface JCloudsEntityStoreConfiguration
{
    // START SNIPPET: config
    /**
     * Name of the JClouds provider to use. Defaults to 'transient'.
     */
    @Optional Property<String> provider();
    @UseDefaults Property<String> identifier();
    @UseDefaults Property<String> credential();
    /**
     * Use this to fine tune your provider implementation according to JClouds documentation.
     */
    @UseDefaults Property<Map<String, String>> properties();
    /**
     * Name of the JClouds container to use. Defaults to 'qi4j-entities'.
     */
    @Optional Property<String> container();
    /**
     * Endpoint for the BlobStore provider.
     */
    @Optional Property<String> endpoint();
    // END SNIPPET: config

}