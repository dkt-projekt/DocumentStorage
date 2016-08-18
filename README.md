# DocumentStorage

The DocumentStorage module performs the upload of documents to the DKT platform. Each uploaded document will also processed by a series of Natural Language Processing services, the results will be stored in the e-Sesame triple store. The document storage organize the documents into separate collections.

## API endpoints

### Add documents to the Document Storage

API endpoint: http://dev.digitale-kuratierung.de/api/document-storage/{collection-name}
HTTP method: POST
Parameters:
* URL parameter collection-name: The name of the collection

CURL examples:

#### Add an HTML file to the collection

```
curl -X POST -d '<p>Welcome to Berlin!</p>' "http://dev.digitale-kuratierung.de/api/document-storage/my-collection?fileName=my-file.html"
```

#### Add a Zip file

```
curl -X POST -H "Content-Type: application/zip" "http://dev.digitale-kuratierung.de/api/document-storage/my-collection?fileName=file2.zip"
```

### Retrieve all documents from a collection

API endpoint: http://dev.digitale-kuratierung.de/api/{collection-name}/my-collection/documents
Request method: GET
Parameters: 
* URL parameter collection-name: The name of the collection

```
curl -X GET "http://dev.digitale-kuratierung.de/api/document-storage/my-collection/documents"
```

Example Output:

```
[
  {
    "id": 1,
    "filename": "00.xhtml",
    "path": "00.xhtml",
    "status": "PROCESSED",
    "errorMessage": null,
    "documentUri": "http://digitale-kuratierung.de/ns/00.xhtml#char=0,11",
    "uploadTime": 1471529514000,
    "lastUpdate": 1471529516000,
    "collection": {
      "name": "my-collection",
      "documents": [],
      "creationTime": 1471529514000
    }
  },
  {
    "id": 2,
    "filename": "01.xhtml",
    "path": "01.xhtml",
    "status": "ERROR",
    "errorMessage": "{\n  \"exception\": \"eu.freme.common.exception.ExternalServiceFailedException\",\n  \"path\": \"/e-sesame/storeData\",\n  \"message\": \"SAIL is already locked by: 6242@v35731.1blu.de in /opt/storage/sesameStorage/my-collection\",\n  \"error\": \"Bad Gateway\",\n  \"status\": 502,\n  \"timestamp\": 1471529619168\n}",
    "documentUri": "http://digitale-kuratierung.de/ns/01.xhtml#char=0,11",
    "uploadTime": 1471529514000,
    "lastUpdate": 1471529514000,
    "collection": {
      "name": "my-collection",
      "documents": [],
      "creationTime": 1471529514000
    }
  }
]
```

### Retrieve aggregated status information from a collection


API endpoint: http://dev.digitale-kuratierung.de/api/{collection-name}/my-collection/status
Request method: GET
Parameters: 
* URL parameter collection-name: The name of the collection

```
curl -X GET -H "http://dev.digitale-kuratierung.de/api/document-storage/my-collection/status"
```

Example Output:

```
{
  "counts": {
    "PROCESSED": 14,
    "ERROR": 2,
    "CURRENTLY_PROCESSING": 0,
    "NOT_PROCESSED": 0
  },
  "finished": true
}
```

## Configuration

**pipeline.json**

The system will look for a file `pipeline.json` in the classpath. This pipeline defines the enrichment pipeline that will be used to process all documents after they have been uploaded to a document collection. The pipeline can be parametrized using the parameters:

* $base-url$ Changes the base url of all pipeline requests. It can be configured either using the configuration parameter `dkt.storage.pipeline.base-url` (see below).
* $collection-name$ Is the name of the document collection and can be used to denote other storage names.

**dkt.storage.pipeline.base-url**

This is a standard Java configuration parameter which can be set e.g. in the `application.properties` file. It changes the $base-url$ parameter of the pipelines. The parameter is optional, if it is not configured, $base-url$ will be configured to `http://localhost:xy`, with xy being the port the server listens on, e.g. `http://localhost:8080`.
