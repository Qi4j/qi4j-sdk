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
package org.qi4j.index.solr;

import java.io.File;
import java.lang.reflect.Field;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.library.fileconfig.FileConfiguration;

import static org.qi4j.api.util.AccessibleObjects.accessible;

@Mixins( EmbeddedSolrService.Mixin.class )
@Activators( EmbeddedSolrService.Activator.class )
public interface EmbeddedSolrService
{
   SolrServer solrServer();

   SolrCore solrCore();

    void activateSolr()
            throws Exception;

    void passivateSolr()
            throws Exception;

    class Activator extends ActivatorAdapter<ServiceReference<EmbeddedSolrService>>
    {

        @Override
        public void afterActivation( ServiceReference<EmbeddedSolrService> activated )
                throws Exception
        {
            activated.get().activateSolr();
        }

        @Override
        public void beforePassivation( ServiceReference<EmbeddedSolrService> passivating )
                throws Exception
        {
            passivating.get().passivateSolr();
        }

    }

   abstract class Mixin
         implements EmbeddedSolrService
   {
      @Service
      FileConfiguration fileConfig;
      public CoreContainer coreContainer;
      public EmbeddedSolrServer server;

      @Uses
      ServiceDescriptor descriptor;

      private SolrCore core;

      @Override
      public void activateSolr() throws Exception
      {
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

         try
         {
            File directory = new File( fileConfig.dataDirectory(), descriptor.identity().toString() );
            directory.mkdirs();

            System.setProperty( "solr.solr.home", directory.getAbsolutePath() );

            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            coreContainer = initializer.initialize();
            server = new EmbeddedSolrServer( coreContainer, "" );
            core = coreContainer.getCore( "" );
         } finally
         {
            Thread.currentThread().setContextClassLoader( oldCl );
         }
      }

      @Override
      public void passivateSolr() throws Exception
      {
         core.closeSearcher();
         coreContainer.shutdown();

         // Clear instance fields for GC purposes
         Field instanceField = SolrCore.class.getDeclaredField( "instance" );
         accessible( instanceField ).set( null, null );

         SolrConfig.config = null;
      }

       @Override
      public SolrServer solrServer()
      {
         return server;
      }

       @Override
      public SolrCore solrCore()
      {
         return core;
      }
   }
}
