#For standalone deployment only +
#add following entrypoint to docker-compose.yml
#    entrypoint:
#      - docker-entrypoint.sh
#      - solr-precreate
#      - outlook_mail
#      - /opt/solr/server/solr/configsets/outlook_mail
FROM solr:7.2.1
COPY --chown=solr:solr outlook_mail/conf/* /opt/solr/server/solr/configsets/outlook_mail/conf/