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
package org.qi4j.entitystore.hazelcast;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;

/**
 * Configuration of HazelcastEntityStoreService.
 */
// START SNIPPET: config
public interface HazelcastEntityStoreConfiguration
{

    /**
     * The location of the Hazelcast configuration XML file.
     * If the property is not a URL, it will be tried as a path name on the classpath.
     *
     * @return The location of the Hazelcast configuration XML file. Defaults to hazelcast-default.xml
     */
    @Optional
    Property<String> configXmlLocation();

    /**
     * The name of the Hazelcast map that is the key-value entity store.
     *
     * @return the name of Hazelcast map containing the entities. Defaults to "qi4j:entitystore:data"
     */
    @Optional
    Property<String> mapName();

}
// END SNIPPET: config
