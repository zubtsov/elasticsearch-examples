import org.elasticsearch.client.ClusterAdminClient
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.cluster.health.ClusterHealthStatus
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.TransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class ElasticSearchClusterTests {
//    It seems like groovy is no longer supported (since Feb 2016)
//    GNode node = nodeBuilder().node();
//    GClient client = node.client();

    private static TransportClient client
    private static ClusterAdminClient adminClient

    @BeforeClass
    static void setUpTransportClient() throws UnknownHostException {
        client = new PreBuiltTransportClient(Settings.builder().put("cluster.name", "zubtsov-es-cluster").build())
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300))

        adminClient = client.admin().cluster()
    }

    @Test
    void testClusterHealth() {
        ClusterHealthStatus actual = adminClient.prepareHealth().get().getStatus()
        Assert.assertEquals("Cluster health isn't green!",
                ClusterHealthStatus.GREEN,
                actual)
    }
}