FROM docker.elastic.co/logstash/logstash-oss:6.1.3
RUN rm -f /usr/share/logstash/pipeline/logstash.conf
COPY --chown=logstash:logstash logstash.conf /usr/share/logstash/pipeline/
COPY --chown=logstash:logstash mail.csv /usr/share/logstash/
#TODO: install solr_http plugin
#RUN ./bin/logstash-plugin install solr_http
ENV LOG_LEVEL "info"