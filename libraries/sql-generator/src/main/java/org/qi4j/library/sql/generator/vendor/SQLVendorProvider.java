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
package org.qi4j.library.sql.generator.vendor;

import java.io.IOException;
import java.util.Iterator;
import java.util.ServiceLoader;
import org.slf4j.LoggerFactory;

/**
 * This class provides easy way of acquiring vendors for specific databases.
 *
 *
 */
public class SQLVendorProvider
{
    /**
     * <p>
     * Creates a new vendor. If one passes {@link SQLVendor} as a parameter, it will return the default vendor-neutral
     * implementation.
     * </p>
     * <p>
     * Invoking this statement is equivalent to calling {@code new ServiceLoader().firstProvider( vendorClass); }.
     *
     * @param <VendorType> The type of the vendor.
     * @param vendorClass  The class of the vendor.
     * @return The vendor of a given class.
     * @throws IOException If {@link ServiceLoader} throws {@link IOException}.
     * @see ServiceLoader
     */
    public static <VendorType extends SQLVendor> VendorType createVendor( Class<VendorType> vendorClass )
        throws IOException
    {
        LoggerFactory.getLogger( SQLVendorProvider.class ).info( "Trying to load implementation for " + vendorClass.getName() );
        ServiceLoader<VendorType> load = ServiceLoader.load( vendorClass );
        Iterator<VendorType> vendorTypeIterator = load.iterator();
        if( vendorTypeIterator.hasNext() )
        {
            return vendorTypeIterator.next();
        }
        throw new InternalError( "ServiceLoader of SQLVendor implementations is not finding the META-INF/services" );
    }
}
