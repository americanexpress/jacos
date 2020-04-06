## Java Connector for Salesforce (JaCoS)

![JaCoS](images/jacos.png) 

**Simple Java/Spring based SDK to enable seamless insert, update and upsert of files and records in the SalesForce in Bulk, Single Record and Multi Record Mode**. 

## Features

- Support Bulk upload of files of any size (even more than 150 MB) using Salesforce Bulk V2 API
- Customized chunking of records for Bulk mode operations
- Support for record by record insert, update and upsert
- Support for multi record insert, update and upsert for similar and dissimilar object
- Inbuilt and configurable Salesforce oAuth2 security support
- Auto-refresh-ability for oAuth2 access token supported
- Inbuilt and configurable proxy support to Salesforce servers
- Easy to configure and use


### Prerequisite
- Maven 3+
- Java 8+
- Salesforce oAuth2
- Spring framework.

### Get Started

1. To use JaCoS, Add the maven dependency in your project

```xml
<dependency>
    <groupId>com.americanexpress.jacos</groupId>
    <artifactId>Jacos-Core</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. Add the annotation '@EnableJacos' in your Spring Application Main class. For example:

```java
@SpringBootApplication
@EnableJacos
public class TestApp {
   private static final Logger logger = LoggerFactory.getLogger(TestApp.class);

```

3. In the application.yml  or application.properties, configure the following salesforce basic properties:

- instance_url : instance url of the salesforce server
- api_version: Salesforce API version. For example 'v42.0'. Please make sure that you are using version 42.0 or above, if you wish to use multi record operations
- chunkSizeInMB: Configure this if you are using bulk API. Leave it to the default value of 100, if you are not sure.

```yaml
jacos:
  sfdc:
     instance_url: 'https://example.my.salesforce.com'
     api_version: 'v42.0'
     chunkSizeInMB: 100
```

### To setup proxy to salesforce server, you can use the following  properties:

- proxyEnabled : true or false
- host: proxy host value
- port: proxy port value
- timeout: read time out /connection time out for the http connection to salesforce server
- user (optional) : proxy username
- password (optional) : proxy password

```yaml
jacos:
  sfdc:
      proxy:
        proxyEnabled: true
        host: proxy.host.com
        port: 8090
        timeout: 1000

```

### You can configure oAuth security for salesforce connection using as follows:

```yaml
jacos:
  sfdc:
    security:
      oauth2:
        client:
          access_token_uri: https://example.salesforce.com/services/oauth2/token
          client_id: client_id
          client_secret: client_secret
          grant_type: refresh_token
          refresh_token: token1
          content_type: 'application/x-www-form-urlencoded'
```

### JaCoS Currently supports 3 kinds of operation. They are as follows:

- Bulk Operation
- Part Operation
- MultiPart Operation

**Note: All header names in the input CSV should map to Salesforce API names.**

4. For Part and MultiPart Operations, you have the option to send custom REST headers while calling the SF APIs:    

```yaml
jacos:  
 sfdc:  
  requestHeaders:  
    exampleHeaderKey1: exampleHeaderValue1
    exampleHeaderKey2 : exampleHeaderValue2
```
### Bulk Operation

The bulk operation is based on [Salesforce Bulk V2 REST API](https://developer.salesforce.com/docs/atlas.en-us.api_bulk_v2.meta/api_bulk_v2/introduction_bulk_api_2.htm).

Create the bean of BulkApi2Operations and invoking the Salesforce APIs

```yaml
@Autowired
private BulkApi2Operations bulkApi2Operations;
```

BulkApi2Operations abstracts process of getting the access token from the refresh token and invoking the REST end points of Salesforce.

```yaml
OperationInfo operationInfo = new OperationInfo("<Object type like Lead/Contact/Account/..>", OperationEnum.Insert/OperationEnum.Update/..,"External Id mapping field");
```

- OperationInfo is the place holder/class which contains the information regarding the operations.
- External Id mapping field is the field used for performing Update/Upsert Operation on any Object. 
- Now you are done with all the prerequisites of invoking the Bulk Operation APIs. 
- All you need to do is just call the services offered by BulkApi2Operations.

```yaml
bulkApi2Operations.performSFOperation(operationInfo, "Content in csv format");
      [example]: Content in csv format
                 Ingest:
                        "Company_c,LastName_c,State\n" +
                        "March28TestComapany,March28TestName,Open\n"+
                        "March28TestComapany2,March28TestName2,Open\n";
                 Update:
                        "Id,Company,LastName,State\n" +
                        "sfdc_id,March28TestComapany_v2,March28TestName_v2,Open\n";
                 Delete:
                        "Id\n" +
                        "sfdc_id";

  bulkApi2Operations.performSFOperationFromFile(operationInfo, "csv file path");
      [example]: File Content in csv format
                 Ingest:
                        Company,LastName,State
                        April3TestComapany,April3TestName,Open
                        April3TestComapany2,April3TestName2,Open
                 * Note: Other operations are similar
```

### Part Operation

The part operation is based on [Salesforce sObject REST API](https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/resources_sobject_basic_info.htm)

For uploading data into Salesforce org record by record, use Part Operation API. To use Part Operation API, First autowire the PartApi2Operations object like

```yaml
@Autowired
private PartApi2Operations partApi2Operations;
Form an  OperationInfo object using OperationInfo constructor like the following:

OperationInfo operationInfo = new OperationInfo(objectType, OperationEnum.valueOf(operationType.toUpperCase()), "<content as a json string>");

```

The OperationInfo object contains various metadata info for some salesforce operation. The constructor of operation Info is as follows:

```yaml
public OperationInfo(String objectType, OperationEnum operationType, String externalIdField)
```
- where objectType is the name of the object you are sending for insert / upsert / update. For example - Lead or Account
- operationType is the enum denoting various operations which can have the values INSERT or UPDATE or UPSERT.
- externalIdField is the id value for an update operation , null for an insert operation and <fieldname>:<fieldvalue> for an upsert operation.
- To call the API to send a record to the salesforce org using Part API, invoke:

```yaml
response = partApi2Operations.performSFOperation(operationInfo, jsonBody);

```

### Multi Part Operation

The multi-part operation is based on [Salesforce sObject Collection REST API](https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/resources_composite_sobjects_collections.htm)

- For uploading data into Salesforce org record by record, use Part Operation API. 
- First autowire the PartApi2Operations object like:

```yaml
@Autowired
private PartApi2Operations partApi2Operations;
Form an  OperationInfo object using OperationInfo constructor like the following:

OperationInfo operationInfo = new OperationInfo(null, OperationEnum.MULTIINSERT, null);
```

- The OperationInfo object contains various metadata info for some salesforce operation. The constructor of operation Info is as follows:

```yaml
public OperationInfo(String objectType, OperationEnum operationType, String externalIdField)
```

- where objectType can be null for multi-record operations
- operationType is the enum denoting various operations which can have the values MULTIINSERT or MULTIUPDATE .
- externalIdField can be null for multi-record operations.

To call the API to send a record to the salesforce org using Part API , use:

```yaml
response = partApi2Operations.performSFOperation(operationInfo, jsonBody);
```
### Multi Part Operation using file

- This operation read data from <code>inputFile</code> and perform the part operation using salesforce composite API. 
- It splits input file into multiple chunks then each send it to SF using the composite API. 
- The number of records per chunk is decided based on the SF API limit (200) and can be changed using the CompositeUpdateReqFileSerializer while init.
- Each SF operation status is written in outputFilePath with schema "ID,STATUS,ERROR"

```yaml
* @param operationInfo   contains the operation details. Supported types are MULTIINSERT or MULTIUPDATE.
 * @param inputFile       input file to process
 * @param outputFilePath  Path to write each operation output
 * @throws PartApiException
 *
 
partApi2Operations.performSFOperation( operationInfo,  inputFile, outputFilePath)


```
Note: All header names in the input CSV should map to Salesforce API names.

### Custom REST API Operation

The Custom REST API operation is based on [Exposing Salesforce Apex Classes as REST Web Services](https://developer.salesforce.com/docs/atlas.en-us.apexcode.meta/apexcode/apex_rest.htm)

- To call any custom REST api developed and exposed through Salesforce 

```java
@Autowired
private PartApi2Operations partApi2Operations;
//Form an  OperationInfo object using OperationInfo constructor like the following:

String metaDataJson = "{ \"method\": \"GET\"}";

OperationInfo operationInfo = new OperationInfo(restUri, OperationEnum.CUSTOMRESTAPI, metaDataJson);
```

- The OperationInfo object contains various metadata info for some salesforce operation. The constructor of operation Info is as follows:

```java
public OperationInfo(String restUri, OperationEnum operationType, String metaDataJson)
```

- where restUri is the relative REST URI path of the Custom REST API exposed through Salesforce
- operationType is the enum denoting operation which should have the value CUSTOMRESTAPI .
- metaDataJson should contain a json string which currently supports a mandatory field called method. The value of the method should be the HTTP method of the custom REST API.

To call the API to send a record to the salesforce org using Custom REST API , use:

```java
response = partApi2Operations.performSFOperation(operationInfo, jsonBody);
```

### Contributing

We welcome Your interest in the American Express Open Source Community on Github. Any Contributor to
any Open Source Project managed by the American Express Open Source Community must accept and sign
an Agreement indicating agreement to the terms below. Except for the rights granted in this 
Agreement to American Express and to recipients of software distributed by American Express, You
reserve all right, title, and interest, if any, in and to Your Contributions. Please
[fill out the Agreement](https://cla-assistant.io/americanexpress/jacos).

### License
Any contributions made under this project will be governed by the
[Apache License 2.0](./LICENSE.txt).


### Code of Conduct
This project adheres to the [American Express Community Guidelines](./CODE_OF_CONDUCT.md). 
By participating, you are expected to honor these guidelines.
