package org.qi4j.index.opensearch.cluster;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5Transport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.index.opensearch.OpenSearchClusterConfiguration;
import org.qi4j.index.opensearch.internal.AbstractOpenSearchSupport;

import java.util.ArrayList;
import java.util.List;

public class OpenSearchClusterSupport extends AbstractOpenSearchSupport
{

    @This
    private Configuration<OpenSearchClusterConfiguration> configuration;

    private ApacheHttpClient5Transport transport;

    @Override
    protected void activateOpenSearch()
        throws Exception
    {
        configuration.refresh();
        OpenSearchClusterConfiguration config = configuration.get();

        // Basic config
        String clusterName = config.clusterName().get() == null ? DEFAULT_CLUSTER_NAME : config.clusterName().get(); // not used by REST client
        index = config.index().get() == null ? DEFAULT_INDEX_NAME : config.index().get();
        indexNonAggregatedAssociations = config.indexNonAggregatedAssociations().get();

        // Nodes: switch to HTTP ports (default 9200)
        String nodesCsv = config.nodes().get();
        String[] nodes = nodesCsv == null || nodesCsv.isBlank()
            ? new String[]{"127.0.0.1:9200"}
            : nodesCsv.split(",");

//        // Parse timeouts (e.g. "5s", "500ms"). Map pingTimeout -> socket timeout; samplerInterval -> sniffer interval.
//        String pingTimeoutStr = config.pingTimeout().get() == null ? "5s" : config.pingTimeout().get();
//        String samplerIntervalStr = config.samplerInterval().get() == null ? "5s" : config.samplerInterval().get();
//        int socketTimeoutMs = (int) parseDurationMillis(pingTimeoutStr);
//        long connectTimeoutMs = 5_000; // sane default; adjust if you add a setting
//        int connectionRequestTimeoutMs = 5_000;
//
//        boolean clusterSniff = Boolean.TRUE.equals(config.clusterSniff().get());
//        boolean ignoreClusterName = Boolean.TRUE.equals(config.ignoreClusterName().get()); // Not applicable for REST; kept for compat

        // Build RestClient with multiple nodes and timeouts
        List<HttpHost> hosts = new ArrayList<>();
        for(String node : nodes)
        {
            String trimmed = node.trim();
            if(trimmed.isEmpty())
            {
                continue;
            }
            String[] hp = trimmed.split(":");
            String host = hp[0].trim();
            int port = hp.length > 1 ? Integer.parseInt(hp[1].trim()) : 9200;
            hosts.add(new HttpHost(host, port));
        }

        HttpHost[] array = hosts.toArray(new HttpHost[0]);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        JacksonJsonpMapper mapper = new JacksonJsonpMapper(objectMapper);
        transport = ApacheHttpClient5TransportBuilder
            .builder(array)
            .setMapper(mapper)
            .setHttpClientConfigCallback(hcb ->
                hcb.setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(Timeout.ofSeconds(5))
                    .setResponseTimeout(Timeout.ofSeconds(5))
                    .setConnectionRequestTimeout(Timeout.ofSeconds(5))
                    .build()
                    ))
                .build();
        client = new OpenSearchClient(transport);

        // Note: clusterName/ignoreClusterName are no-ops in REST mode. Left for config compatibility.
    }

    @Override
    protected void passivateOpenSearch()
        throws Exception
    {
        if(transport != null)
        {
            try
            {
                transport.close();
            }
            catch(Exception ignored)
            {
            }
            transport = null;
        }
    }
}
