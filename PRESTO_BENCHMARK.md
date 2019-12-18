# Presto benchmarks how to

## Setup

Setup benchto service, follow instructions in the [docs](https://github.com/prestodb/benchto/tree/master/benchto-service)

Create a configuration file named `application-presto-devenv.yaml` (format=application-<profile/env name your created above>.yaml) just outside of `presto-benchto-benchmarks` diectory (exists inside presto repo root). Put correct benchto service host and port and presto urls.

```yaml
benchmark-service:
  url: http://localhost:8080

data-sources:
  presto:
    url: jdbc:presto://192.168.35.234:8080
    username: airflow
    driver-class-name: com.facebook.presto.jdbc.PrestoDriver

environment:
  name: PRESTO-DEVENV

presto:
  url: http://192.168.35.234:8080

benchmark:
  feature:
    presto:
      metrics.collection.enabled: true

macros:
  sleep-4s:
    command: echo "Sleeping for 4s" && sleep 4
```

Instructions on how to run benchmarks are available at https://github.com/prestodb/presto/blob/master/presto-benchto-benchmarks

Make sure to keep use overrides file to run the benchmarks on just on medium for starting.

## Generate data

Use the following script to generate SQL commands. (this is updated version of what's given in presto)


```python
#!/usr/bin/env python

schemas = [
    # (new_schema, source_schema)
    ('tpch_10gb_orc', 'tpch.sf10'),
    ('tpch_100gb_orc', 'tpch.sf100'),
    ('tpch_1tb_orc', 'tpch.sf1000'),
    ('tpch_10tb_orc', 'tpch.sf10000'),
    ('tpch_30gb_orc', 'tpch.sf30'),
    ('tpch_300gb_orc', 'tpch.sf300'),
    ('tpch_30tb_orc', 'tpch.sf30000'),
    ('tpch_3tb_orc', 'tpch.sf3000')
]

tables = [
    'customer',
    'lineitem',
    'nation',
    'orders',
    'part',
    'partsupp',
    'region',
    'supplier',
]

for (new_schema, source_schema) in schemas:

    if new_schema.endswith('_orc'):
        format = 'ORC'
    elif new_schema.endswith('_text'):
        format = 'TEXTFILE'
    else:
        raise ValueError(new_schema)

    print('CREATE SCHEMA IF NOT EXISTS hive.%s;' % (new_schema,))
    for table in tables:
        print('CREATE TABLE IF NOT EXISTS "hive"."%s"."%s" WITH (format = \'%s\') AS SELECT * FROM %s."%s";' % \
              (new_schema, table, format, source_schema, table))
```

Change source schema in the script based on your presto distribution. 

To see available schemas and catalogs: `show catalogs`

If you are using other presto distribution than startburst make sure to change https://github.com/prestodb/presto/blob/master/presto-benchto-benchmarks/src/main/resources/benchmarks/presto/*.yaml for different benchmarks, Schemas might be different. Put the correct ones based on the data you generated. 

## Analysing benchmarks

// TODO
