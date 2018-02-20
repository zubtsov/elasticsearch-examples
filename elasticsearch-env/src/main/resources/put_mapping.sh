#!/usr/bin/env bash
curl -XPUT 'localhost:9200/outlook_mail' -H 'Content-Type: application/json' -d @mapping.json