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
curl -X POST -d '<p>Welcome to Berlin!</p>' "http://localhost:8080/document-storage/my-collection?fileName=my-file.html"
```

#### Add a Zip file


### Retrieve document collections



## Configuration

**pipeline.json**

The system will look for a file `pipeline.json` in the classpath. This pipeline defines the enrichment pipeline that will be used to process all documents after they have been uploaded to a document collection. The pipeline can be parametrized using the parameters:

* $base-url$ Changes the base url of all pipeline requests. It can be configured either using the configuration parameter `dkt.storage.pipeline.base-url` (see below).
* $collection-name$ Is the name of the document collection and can be used to denote other storage names.

**dkt.storage.pipeline.base-url**

This is a standard Java configuration parameter which can be set e.g. in the `application.properties` file. It changes the $base-url$ parameter of the pipelines. The parameter is optional, if it is not configured, $base-url$ will be configured to `http://localhost:xy`, with xy being the port the server listens on, e.g. `http://localhost:8080`.
