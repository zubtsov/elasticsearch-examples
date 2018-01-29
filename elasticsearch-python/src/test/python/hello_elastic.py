import unittest
from elasticsearch import Elasticsearch


class TestElasticSearchCluster(unittest.TestCase):
    # by default we connect to localhost:9200
    es = Elasticsearch()

    def test_health(self):
        # datetimes will be serialized
        self.assertEquals(print(self.es.cluster.health()['status']), 'green')
