import unittest
from elasticsearch import Elasticsearch

class TestElasticSearchCluster(unittest.TestCase):
    # by default we connect to localhost:9200
    es = Elasticsearch()

    def test_health(self):
        print('Testing Elasticsearch cluster state...')
        self.assertEqual('green', self.es.cluster.health()['status'], 'Cluster state isn\'t green!')
