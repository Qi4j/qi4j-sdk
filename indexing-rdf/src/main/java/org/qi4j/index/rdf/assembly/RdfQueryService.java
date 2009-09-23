/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.index.rdf.assembly;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.index.rdf.internal.RdfEntityFinderMixin;
import org.qi4j.index.rdf.internal.RdfEntityIndexerMixin;
import org.qi4j.index.rdf.internal.RdfNamedEntityFinderMixin;
import org.qi4j.index.rdf.internal.TupleQueryExecutorMixin;
import org.qi4j.index.rdf.internal.RdfExportMixin;
import org.qi4j.index.rdf.RdfExport;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.entitystore.StateChangeListener;

/**
 * JAVADOC Add JavaDoc
 */
@Mixins( { RdfEntityIndexerMixin.class, RdfEntityFinderMixin.class, RdfNamedEntityFinderMixin.class, RdfExportMixin.class, TupleQueryExecutorMixin.class } )
public interface RdfQueryService
    extends StateChangeListener, EntityFinder, NamedEntityFinder, RdfExport, ServiceComposite
{
}
