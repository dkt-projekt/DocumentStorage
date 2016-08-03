# DocumentStorage

The DocumentStorage module performs the storage and management of documents and collections of documents. For the moment, the collections will be handled in NIF format documents.

##Creating a Collection

The NER endpoint bundles several different approaches. 
The first one is based on NER models which are trained using previously annotated data to extract certain features. These features are then used to select candidate entities. The second one is a simple dictionary approach. A dictionary is uploaded and in new input, every instance of a (group of) word(s) that can be found in the dictionary is annotated as being an entity. 
The third one is a temporal analyzer module. This is based on a regular expression grammar to detect temporal expressions in input and annotate them in the output NIF.
Which approach is used depends on the analysis parameter (described below) and the model specified.
The system is setup so that the output of one component can directly be used as input for the next component, since NIF is the interchange format that is used throughout the whole project.

### Endpoint

Using the POST http method:

http://api.digitale-kuratierung.de/api/e-documentstorage/collection/{collectionName}

### Input
In addition to the collectionName, that is a path variable, the following parameters have to be set to create a collection:

`private`: The language of the input text. For now, only German (`de`) and English (`en`) are supported.  
  
`user`: is the creator of the collection and has CRUD permissions.
  
`users`: is a comma separated list of user names that have CRUD permissions in the collection.

### Output
A String containing the URI assigned to the collection.

Example cURL post for creating a collection:  
`curl -X POST "http://api.digitale-kuratierung.de/api/e-documentstorage/collection/testcollection?private=true&user=dkt-projekt&users=pebo01,ansr01"`


##Getting an overview of the collection

### Endpoint

Using the GET http method.

http://api.digitale-kuratierung.de/api/e-documentstorage/collection/{collectionName}/overview

### Input
`collectionName`: name of the collection for which an overview is requested.

`user`: the user identifier that is tryiung to access the collection overview. If the user has not right access, the request will fail.

### Output
A JSON string containing the overview of the specified colleciton.

Examle cURL:
`curl "http://dev.digitale-kuratierung.de/api/e-documentstorage/collection/testcollection/overview?user=dkt-projekt"`

## Get the content of a collection

### Endpoint

Using the GET http method.

http://api.digitale-kuratierung.de/api/e-documentstorage/collection/{collectionName}

### Input
`collectionName`: name of the collection whose content is being requested.

`user`: the user identifier that is trying to access the collection content. If the user has not right access, the request will fail.

### Output
the content of the collection (in NIF format). This content also includes all the documents contained in the colleciton.

Examle cURL:
`curl "http://api.digitale-kuratierung.de/api/e-documentstorage/collection/testcollection?user=dkt-projekt"`

## Delete a collection

### Endpoint

Using the DELETE http method.

http://api.digitale-kuratierung.de/api/e-documentstorage/collection/{collectionName}

### Input
`collectionName`: name of the collection that will be deleted.

`user`: the user identifier that is tryiung to access the collection overview. If the user has not right access, the request will fail.

### Output
the content of the collection (in NIF format). This content also includes all the documents contained in the colleciton.

Examle cURL delete:
`curl -X DELETE "http://api.digitale-kuratierung.de/api/e-documentstorage/collection/testcollection?user=dkt-projekt"`

## Add a document to a collection

### Endpoint

Using the POST http method.

http://api.digitale-kuratierung.de/api/e-documentstorage/collection/{collectionName}/document/{documentName}

### Input
`collectionName`: name of the collection where the document will be added.

`documentName`: name of the document that will be added.

`user`: the user identifier that is tryiung to access the collection overview. If the user has not right access, the request will fail.

`input/postBody/fileInput`: the content of the file that will be added. 

TODO MORE

### Output
A string containing the assigned URI to the document.

Examle cURL delete:
`curl -X POST "http://api.digitale-kuratierung.de/api/e-documentstorage/collection/testcollection/document/testdocument?user=dkt-projekt&input=Text to be included as the text of the document"`

## Get content of a document

### Endpoint

Using the GET http method.

http://api.digitale-kuratierung.de/api/e-documentstorage/collection/{collectionName}/document/{documentName}

### Input
`collectionName`: name of the collection of the document.

`documentName`: name of the document.

`user`: the user identifier that is trying to access the document content. If the user has not right access, the request will fail.

`contentType`: defines the type of content that will be returned. There are two options: `nif` will return the NIF representation of the document and `other` will return an URL where the original document can be accessed.

### Output
A string containing the assigned URI to the document.

Examle cURL delete:
`curl "http://api.digitale-kuratierung.de/api/e-documentstorage/collection/testcollection/document/testdocument?user=dkt-projekt&contentType=nif"`

## Delete a document

### Endpoint

Using the DELETE http method.

http://api.digitale-kuratierung.de/api/e-documentstorage/collection/{collectionName}/document/{documentName}

### Input
`collectionName`: name of the collection of the document.

`documentName`: name of the document.

`user`: the user identifier that is trying to delete the document. If the user has not right access, the request will fail.

### Output
The NIF content of the deleted document.

Examle cURL delete:
`curl -X DELETE "http://api.digitale-kuratierung.de/api/e-documentstorage/collection/testcollection/document/testdocument?user=dkt-projekt"`

