# DocumentStorage

The DocumentStorage module performs the upload of documents to the DKT platform. When one or more documents are uploaded to the HTTP endpoint, they will be stored in the file system and in the MySQL database. The MySQL database stores metadata about each document, e.g. a state that marks if the document has been processed, if errors occured during processing, and other states. A number of worker threads uses this database table as a processing queue and processes the documents one after another. Each document is converted to NIF and then send to a pipeline. The processing pipeline is configurable and executes a number of e-Services one after another. The results are then stored in the triple store. This image shows this process:

![Document upload components](/images/document-upload-components.png?raw=true "Document upload components")


## API endpoints

### Create collection

API endpoint: http://dev.digitale-kuratierung.de/api/document-storage/collections/{collection-name}
HTTP method: POST
Parameters:
* URL parameter collection-name: The name of the collection

CURL example:

```
curl -X POST "http://dev.digitale-kuratierung.de/api/document-storage/collections/my-collection"
```

### Add documents to the Document Storage

API endpoint: http://dev.digitale-kuratierung.de/api/document-storage/collections/{collection-name}/documents
HTTP method: POST
Parameters:
* URL parameter collection-name: The name of the collection
* URL parameter pipeline (optional): The ID of a custom pipeline that is used to process this document. When the parameter is not set then the default processing pipeline will be used. See the [pipelining API](full.html#!/Pipelining/post_pipelining_templates) for information how to create a pipeline.

CURL examples:

#### Add an HTML file to the collection

```
curl -X POST -d '<p>Welcome to Berlin!</p>' "http://dev.digitale-kuratierung.de/api/document-storage/collections/my-collection/documents?fileName=my-file.html"
```

#### Add a Zip file

```
curl -X POST -H "Content-Type: application/zip" "http://dev.digitale-kuratierung.de/api/document-storage/collections/my-collection/documents?fileName=file2.zip"
```

### Retrieve all documents from a collection

API endpoint: http://dev.digitale-kuratierung.de/api/document-storage/collections/{collection-name}/documents
Request method: GET
Parameters: 
* URL parameter collection-name: The name of the collection

```
curl -X GET "http://dev.digitale-kuratierung.de/api/document-storage/collections/my-collection/documents"
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


API endpoint: http://dev.digitale-kuratierung.de/api/document-storage/collections/{collection-name}/status
Request method: GET
Parameters: 
* URL parameter collection-name: The name of the collection

```
curl -X GET "http://dev.digitale-kuratierung.de/api/document-storage/collections/my-collection/status"
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

### Delete a collection

Delete a collection. This will delete the collection from the database, delete all its file from the server and also delete the data from the triple store.

API endpoint: http://dev.digitale-kuratierung.de/api/document-storage/collections/{collection-name}
Request method: DELETE
Parameters:
* URL parameter collection-name: The name of the collection

### Get all collections

API endpoint: http://dev.digitale-kuratierung.de/api/document-storage/collections
Request method: GET
Parameters: none

```
curl -X GET "http://dev.digitale-kuratierung.de/api/document-storage/collections"
```

Example Output:

```
["mendelsohn-archive", "example-collection"]
```


## Configuration

**pipeline.json**

The system will look for a file `pipeline.json` in the classpath. This pipeline defines the enrichment pipeline that will be used to process all documents after they have been uploaded to a document collection. The pipeline can be parametrized using the parameters:

* $base-url$ Changes the base url of all pipeline requests. It can be configured either using the configuration parameter `dkt.storage.pipeline.base-url` (see below).
* $collection-name$ Is the name of the document collection and can be used to denote other storage names.

**dkt.storage.pipeline.base-url**

This is a standard Java configuration parameter which can be set e.g. in the `application.properties` file. It changes the $base-url$ parameter of the pipelines. The parameter is optional, if it is not configured, $base-url$ will be configured to `http://localhost:xy`, with xy being the port the server listens on, e.g. `http://localhost:8080`.

**dkt.storage.data-dir**

This is a standard Java configuration parameter which can be set e.g. in the `application.properties` file. It specifies the location which is used to store the upload files. The default value is "documents/".

**dkt.storage.virtuoso-username**

Specify the user name that has write access to the Virtuoso triple store.

**dkt.storage.virtuoso-password**

Specify the password of the user for write access to the Virtuoso triple store.

**dkt.storage.virtuoso-crud-endpoint**

Specify the API endpoint for write access to the Virtuoso triple store. E.g. `http://example.com:8890/sparql-graph-crud-auth`
