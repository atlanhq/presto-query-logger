# Presto Query Logger

A presto plugin to push incoming SQL queries into Elasticsearch

## Table of Contents

- Compatibility
- Build and Run
- Contribute

### Compatibility

 [Presto](https://prestosql.io)>=v329

Elasticsearch: 6.7.4

### Build and Run

```bash
mvn clean install
```

Follow [presto-plugins guide](https://prestosql.io/docs/current/develop/spi-overview.html) for deploying the plugin into your presto cluster. To compile the plugin for other presto versions change the presto version in `pom.xml` and compile. See [metrics_sample](https://github.com/atlanhq/presto-query-logger/blob/master/metrics_sample.json) for details of what is being pushed in Elasticsearch. 

### Contribute

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request
