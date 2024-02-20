# Input data storage

This service is created for the qanary-explanations-service. It reads the Qanary component's logs and writes them to a triplestore. 

## Usage
This service is used with the included Qanary-components repository as a submodule as its path is hard-coded yet. 
To use this tool it is also required to use an adjusted QanaryTripleStoreConnector file, which is mentioned [here](https://github.com/dschiese/Qanary/commit/0bfbad4f5b0466a2a674a918f51f8439d0179de1) since this includes the log-writing to files.


## Prequisteries
This service uses the Qanary-components-repository as a submodule and these components should be used when running a Qanary pipeline. Therefore this approach is a local one. 

## Functioning
The service finds the paths to the local components where the log files are stored. In a scheduled method these log files are parsed and the queries are stored in a triplestore. The data which is stored follows this pattern:

```sparql
?component ex:hasAnnotationType ?annotationType .
?annotationType ex:hasInputQuery ?inputQuery
```

Afterwards this data can be fetched to receive component, annotationType, and the query. Within the qanary-explanation-service this data can be used to create explanations for input data.
