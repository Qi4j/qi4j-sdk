package org.qi4j.index.opensearch;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.google.common.collect.Lists;

import java.time.Duration;

public class OpenSearchContainer extends GenericContainer<OpenSearchContainer>
{
    public OpenSearchContainer(String dockerImageName)
    {
        super(dockerImageName);
        setExposedPorts(Lists.newArrayList(9200,9600));
        addEnv("discovery.type", "single-node");
        addEnv("DISABLE_SECURITY_PLUGIN", "true");
        setWaitStrategy(Wait.forLogMessage(".*Cluster health status changed from .YELLOW. to .GREEN.*", 1).withStartupTimeout(Duration.ofSeconds(90)));
        start();
    }


}
