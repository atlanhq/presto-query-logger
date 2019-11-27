# presto-query-audit

Log your presto SQL queries to Elasticsearch

### Build

```bash
mvn clean install
```

`target/` folder will have a jar file named `QueryAuditEventListener-1.0.jar`

### Installing on presto

#### Dependencies list

```
HdrHistogram-2.1.9.jar                          joda-time-2.10.1.jar
QueryAuditEventListener-1.0.jar                 jopt-simple-5.0.2.jar
aggs-matrix-stats-client-6.7.0.jar              lang-mustache-client-6.7.0.jar
commons-codec-1.10.jar                          log4j-api-2.11.1.jar
commons-logging-1.1.3.jar                       lucene-analyzers-common-7.7.0.jar
compiler-0.9.3.jar                              lucene-backward-codecs-7.7.0.jar
elasticsearch-6.7.0.jar                         lucene-core-7.7.0.jar
elasticsearch-cli-6.7.0.jar                     lucene-grouping-7.7.0.jar
elasticsearch-core-6.7.0.jar                    lucene-highlighter-7.7.0.jar
elasticsearch-rest-client-6.7.0.jar             lucene-join-7.7.0.jar
elasticsearch-rest-high-level-client-6.7.0.jar  lucene-memory-7.7.0.jar
elasticsearch-secure-sm-6.7.0.jar               lucene-misc-7.7.0.jar
elasticsearch-x-content-6.7.0.jar               lucene-queries-7.7.0.jar
hppc-0.7.1.jar                                  lucene-queryparser-7.7.0.jar
httpasyncclient-4.1.2.jar                       lucene-sandbox-7.7.0.jar
httpclient-4.5.2.jar                            lucene-spatial-7.7.0.jar
httpcore-4.4.5.jar                              lucene-spatial-extras-7.7.0.jar
httpcore-nio-4.4.5.jar                          lucene-spatial3d-7.7.0.jar
jackson-core-2.8.11.jar                         lucene-suggest-7.7.0.jar
jackson-dataformat-cbor-2.8.11.jar              parent-join-client-6.7.0.jar
jackson-dataformat-smile-2.8.11.jar             rank-eval-client-6.7.0.jar
jackson-dataformat-yaml-2.8.11.jar              snakeyaml-1.17.jar
jna-4.5.1.jar                                   t-digest-3.2.jar
```

Put the built jar file with above dependencies into folder `/usr/lib/presto/lib/plugin/atlan-audit-logger-presto/` on each presto coordinator and worker.

#### Configs

Create file: `/etc/presto/event-listener.properties` with content

```
event-listener.name=atlan-audit-logger
es-host=dev-admin-search.atlan.com
es-port=443
```
