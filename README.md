es-index-notification [![Build Status](https://travis-ci.org/twxkit/es-index-notification.png?branch=master)](https://travis-ci.org/twxkit/es-index-notification)
=====================

Index JSON into ES with goodness of callback notification.

By default, elasticsearch does indexing of a document in background once the request is submitted. If you want to be notified once the background indexing process is complete, the default elasticsearch does not have a mechanism to do it. This pluing gives you that functionality for you.

Index any JSON document with a `callback-url` in the request header. Once the indexing is done, this plugin sends a status of index process to the `callback-url` specified both in success and failure scenarios.


Installation
=====================

Use `ES_HOME/bin/plugin` to install this plugin.

```
Example:
    (es_home)$ ./bin/plugin -i es-index-notification -u https://github.com/twxkit/es-index-notification/releases/download/v1.3/es-index-notification-1.3.zip
```

Usage
======

POST/PUT any JSON document just like you would do with elasticsearch. Only the URL changes.

#### Example 1 (Index a document with auto generated id)

```bash
curl -H"Content-Type: application/json" -H"callback-url: http://myapp/index/notification" -XPOST http://host:port/es-index-notification/{index}/{type} -d'
{
    "name":"Java 8 Cookbook",
    "description":"Explains all new cool java 8 features with examples"
}'
```



#### Example 2 (Index a document with user specified id)

```bash
curl -H"Content-Type: application/json" -H"callback-url: http://myapp/index/notification" -XPUT http://host:port/es-index-notification/{index}/{type}/{id} -d'
{
    "name":"Java 8 Cookbook",
    "description":"Explains all new cool java 8 features with examples"
}'
```
    
In both the above examples, replace the params with in braces `{ }` and a callback would be made to the sepcified url via `callback-url`

