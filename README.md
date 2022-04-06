# Hosebird Client (hbc) [![status: retired](https://opensource.twitter.dev/status/retired.svg)](https://opensource.twitter.dev/status/#retired)
 
A Java HTTP client for consuming Twitter's standard [Streaming API](https://developer.twitter.com/en/docs/tweets/filter-realtime/overview)

!! As of March 2022, this code is [no longer maintained or supported](https://github.com/twitter/hbc/issues/210). 

!! Note that, as of August 16th, 2018, the [user streams and site streams features have been retired](https://twittercommunity.com/t/reminder-site-streams-user-streams-and-legacy-dm-endpoints-will-be-sunset-august-16th/109482), so code that attempts to connect to these endpoints will no longer work.

## Features
* GZip support
* OAuth support
* Partitioning support
* Automatic reconnections with appropriate backfill counts
* Access to raw bytes payload
* Proper backoffs/retry schemes
* Relevant statistics/events

## Getting Started

The Hosebird client is broken down into two modules: hbc-core and hbc-twitter4j. The hbc-core module uses a message queue, which the consumer can poll for the raw String messages, while the hbc-twitter4j module uses the [twitter4j](http://twitter4j.org) listeners and data model on top of the message queue to provide a parsing layer.

The latest hbc artifacts are published to maven central. Bringing hbc into your project should be as simple as adding the following to your maven pom.xml file:

```xml
  <dependencies>
    <dependency>
      <groupId>com.twitter</groupId>
      <artifactId>hbc-core</artifactId> <!-- or hbc-twitter4j -->
      <version>2.2.0</version> <!-- or whatever the latest version is -->
    </dependency>
  </dependencies>
```

### Quickstart

Declaring the connection information:
```java
/** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(1000);

/** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
// Optional: set up some followings and track terms
List<Long> followings = Lists.newArrayList(1234L, 566788L);
List<String> terms = Lists.newArrayList("twitter", "api");
hosebirdEndpoint.followings(followings);
hosebirdEndpoint.trackTerms(terms);

// These secrets should be read from a config file
Authentication hosebirdAuth = new OAuth1("consumerKey", "consumerSecret", "token", "secret");
```

Creating a client:
```java
ClientBuilder builder = new ClientBuilder()
  .name("Hosebird-Client-01")                              // optional: mainly for the logs
  .hosts(hosebirdHosts)
  .authentication(hosebirdAuth)
  .endpoint(hosebirdEndpoint)
  .processor(new StringDelimitedProcessor(msgQueue))
  .eventMessageQueue(eventQueue);                          // optional: use this if you want to process client events

Client hosebirdClient = builder.build();
// Attempts to establish a connection.
hosebirdClient.connect();
```

Now, msgQueue and eventQueue will now start being filled with messages/events. Read from these queues however you like.
```java
// on a different thread, or multiple different threads....
while (!hosebirdClient.isDone()) {
  String msg = msgQueue.take();
  something(msg);
  profit();
}
```

You can close a connection with

```java
hosebirdClient.stop();
```

### Quick Start Example

To run the sample stream example:

```
mvn install && mvn exec:java -pl hbc-example -Dconsumer.key=XYZ -Dconsumer.secret=SECRET -Daccess.token=ABC -Daccess.token.secret=ABCSECRET
```

You can find these values on http://developer.twitter.com and navigating to one of your applications then to the Keys and Tokens tab.
The Consumer API key and secrets values on that page correspond to hbc's `-Dconsumer.*` properties.

Alternatively you can set those properties in hbc-examples/pom.xml

## The Details

### Authentication:

Declaring OAuth1 credentials in the client:

```java
new OAuth1("consumerKey", "consumerSecret", "token", "tokenSecret")
```

Be sure not to pass your tokens as strings directly into the initializers. They should be read from a configuration file that isn't checked in with your code or something similar. Safety first.

### Specifying an endpoint

Declare a StreamingEndpoint to connect to. These classes reside in the package com.twitter.hbc.core.endpoint, and correspond to all of our endpoints. By default, the HTTP parameter "delimited=length" is set for all of our StreamingEndpoints for compatibility with our processor (next section). If you are using our StringDelimitedProcessor this parameter must be set. For a list of available public endpoints and the http parameters we support, see [Twitter's Streaming API docs](https://developer.twitter.com/en/docs/tweets/filter-realtime/overview).

#### Filter streams:

```java
StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
// Optional: set up some followings and track terms
List<Long> followings = Lists.newArrayList(1234L, 566788L);
List<String> terms = Lists.newArrayList("twitter", "api");
endpoint.followings(followings);
endpoint.trackTerms(terms);
```

#### Setting up a Processor:

The hosebird client uses the notion of a "processor" which processes the stream and put individual messages into the provided BlockingQueue. We provide a StringDelimitedProcessor class which should be used in conjunction with the StreamingEndpoints provided. The processor takes as its parameter a BlockingQueue, which the client will put String messages into as it streams them.

Setting up a StringDelimitedProcessor is as easy as:

```java
new StringDelimitedProcessor(msgQueue);
```

### The hbc-twitter4j module

The hbc-twitter4j module uses the twitter4j listeners and models. To use it, create a normal Client object like before using the ClientBuilder, then depending on which type of stream you are reading from, create an appropriate Twitter4jClient. The Twitter4jClient wraps around the Client it is passed, and calls the callback methods in the twitter4j listeners whenever it retrieves a message from the message queue. The actual work of polling from the message queue, parsing, and executing the callback method is done by forking threads from an executor service that the client is passed.

If connecting to a status stream (filter, firehose, sample), use Twitter4jStatusClient:

```java
// client is our Client object
// msgQueue is our BlockingQueue<String> of messages that the handlers will receive from
// listeners is a List<StatusListener> of the t4j StatusListeners
// executorService
Twitter4jClient t4jClient = new Twitter4jStatusClient(client, msgQueue, listeners, executorService);
t4jClient.connect();

// Call this once for every thread you want to spin off for processing the raw messages.
// This should be called at least once.
t4jClient.process(); // required to start processing the messages
t4jClient.process(); // optional: another Runnable is submitted to the executorService to process the msgQueue
t4jClient.process(); // optional
```

#### Using Handlers, a Twitter4j listener add-on

All Twitter4jClients support Handlers, which extend their respective Twitter4j listeners: StatusStreamHandler extends StatusesListener. These handlers have extra callback menthods that may be helpful for parsing messages that the Twitter4j listeners do not yet support

## Building / Testing

To build locally (you must use java 1.7 for compiling, though we produce 1.6 compatible classes):

```
mvn compile
```
To run tests:

```
mvn test
```

## Authors:
* Steven Liu
* Kevin Oliver

## License
Copyright 2013 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0

