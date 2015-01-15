package org.qi4j.index.elasticsearch.extension.spatial;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.query.Query;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.elasticsearch.ElasticSearchConfiguration;
import org.qi4j.index.elasticsearch.assembly.ESClusterIndexQueryAssembler;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.library.spatial.assembly.TGeometryAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.indexing.AbstractSpatialRegressionTest;
import org.qi4j.test.util.DelTreeAfter;

import java.io.File;

import static org.qi4j.test.util.Assume.assumeNoIbmJdk;

/**
 * Created by jj on 21.12.14.
 */
public class ElasticSearchSpatialRegressionQueryClusterTest
        extends AbstractSpatialRegressionTest
{
    private static final File DATA_DIR = new File( "build/tmp/es-spatial-query-test" );
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter( DATA_DIR );

    @BeforeClass
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }

    protected boolean isExpressionSupported(Query<?> expression)
    {
        return true;
    }


    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        super.assemble( module );

        // Geometry support
        new TGeometryAssembler().assemble(module);



        // Config module
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );

        config.values(SpatialConfiguration.Configuration.class,
                SpatialConfiguration.FinderConfiguration.class,
                SpatialConfiguration.IndexerConfiguration.class,
                SpatialConfiguration.IndexingMethod.class,
                SpatialConfiguration.ProjectionSupport.class).
                visibleIn(Visibility.application);

        // Index/Query
        new ESClusterIndexQueryAssembler().
                withConfig(config, Visibility.layer).
                identifiedBy("ElasticSearchConfigurationVariant1").
                assemble(module);
        ElasticSearchConfiguration esConfig = config.forMixin(ElasticSearchConfiguration.class).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set(Boolean.TRUE);


        // FileConfig
        FileConfigurationOverride override = new FileConfigurationOverride().
                withData(new File(DATA_DIR, "qi4j-data")).
                withLog(new File(DATA_DIR, "qi4j-logs")).
                withTemporary(new File(DATA_DIR, "qi4j-temp"));
        module.services(FileConfigurationService.class).
                setMetaInfo(override);


        config.services(FileConfigurationService.class)
                // .identifiedBy("ElasticSearchConfigurationVariant1")
                .setMetaInfo(override)
                .visibleIn(Visibility.application);
    }
}