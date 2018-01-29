package com.zubtsov.elasticsearchsample1;

import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestElasticSearchCluster {

    private static TransportClient client;
    private static ClusterAdminClient adminClient;

    @BeforeClass
    public static void setUpTransportClient() throws UnknownHostException {
        client = new PreBuiltTransportClient(Settings.builder().put("cluster.name", "zubtsov-es-cluster").build());
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

        adminClient = client.admin().cluster();
    }

    @Test
    public void testClusterHealth() {
        ClusterHealthStatus actual = adminClient.prepareHealth().get().getStatus();
        Assert.assertEquals("Cluster health isn't green!",
                ClusterHealthStatus.GREEN,
                actual);
    }
}
