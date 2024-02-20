Start `<elasticsearch>/bin/elasticsearch`
and `<kibana>/bin/kibana`.

Open kibana at `http://localhost:5601` or possibly `http://192.168.1.111:5601`.

To submit the schema, wrap it in

```json
PUT habitus

PUT habitus/_mapping
{
  "add schema here"
}
```

To get a list of indices, run
```json
GET _cat/indices
```

To add data, wrap it in

```json
PUT habitus/_doc/<id>
{
  "add document here"
}

```
Skip the <id> to have one generated, but change it to `POST`.
```json
POST habitus/_doc/
{
  "add document here"
}
```

To query for all records, use

```json
GET habitus/_search

```

Delete a document with
```json
DELETE habitus/_doc/<id>
```

Remove everything with
```json
DELETE habitus
```

See other files like `index.json`, `schema.json`, `search.json` for more details.
