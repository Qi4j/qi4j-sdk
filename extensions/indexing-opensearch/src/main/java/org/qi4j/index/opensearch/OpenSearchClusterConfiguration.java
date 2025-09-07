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
package org.qi4j.index.opensearch;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;

// START SNIPPET: config
public interface OpenSearchClusterConfiguration
    extends OpenSearchIndexingConfiguration
{

    /**
     * Comma-separated list of nodes host:port.
     * Defaults to '127.0.0.1:9200'.
     */
    @Optional
    Property<String> nodes();
// END SNIPPET: config

//    /**
//     * Allows client to sniff the rest of the cluster, and add those into its list of machines to use.
//     * In this case, note that the ip addresses used will be the ones that the other nodes were started
//     * with (the “publish” address).
//     * Defaults to FALSE.
//     */
//// TODO: Cluster sniffing is not supported yet.
//    @UseDefaults
//    Property<Boolean> clusterSniff();
//
//    /**
//     * Set to true to ignore cluster name validation of connected nodes.
//     * Defaults to FALSE.
//     */
//// TODO: Cluster sniffing is not supported yet.
//    @UseDefaults
//    Property<Boolean> ignoreClusterName();
//
//    /**
//     * The time to wait for a ping response from a node.
//     * Defaults to 5s.
//     */
//// TODO: Cluster sniffing is not supported yet.
//    @Optional
//    Property<String> pingTimeout();
//
//    /**
//     * How often to sample / ping the nodes listed and connected.
//     * Defaults to 5s.
//     */
//// TODO: Cluster sniffing is not supported yet.
//    @Optional
//    Property<String> samplerInterval();

// START SNIPPET: config
}
// END SNIPPET: config
