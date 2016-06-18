This is a fork of [Twitter/HBC](https://github.com/twitter/hbc) that supports streaming from a Gnip PowerTrack 2.0 stream.

## Introduction <a class='tall' id='introduction'>&nbsp;</a>

Twitter's [Hosebird client (HBC)](https://github.com/twitter/hbc) has been a popular streaming consumer app for many years. Originally built around the Twitter (public) Streaming API, it was extended a couple of years ago to work with Gnip PowerTrack streams. Since it was written to work with Twitter APIs, it natively supported OAuth authentication. The main trick with extending the library to work with Gnip streams was building in Basic Authentication. That [update was made back in 2014](https://blog.twitter.com/2014/drinking-from-the-enterprise-stream) and Gnip customers have been happily using HBC with PowerTrack v1 ever since.

This week we sat down to update the HBC to work with PowerTrack V2. PowerTrack 2.0 is hosted on Twitter servers, so going in it was assumed that the only updates required were related to changes to the realtime PowerTrack endpoint URLs. However, after those straightforward updates it was discovered that the HBC's attempts to use Basic Authentication with the Twitter servers was failing with 401 errors.

So after some experimentation it was discovered that explicitly adding a Authentication header to the connection request was what was needed to authenticate and start streaming data. 

Several HBC files were updated to make it compatiable with PowerTrack 2.0. The updated HBC library is available [HERE](https://github.com/jimmoffitt/hbc). See below for a tour of the code changes made.

## Code updates<a class='tall' id='code-updates'>&nbsp;</a>

The following HBC files were updated to work with PTv2 and to add an authentication request header:

#### PTv2 updates
+ [com.twitter.hbc.core.HttpConstants](#http-constants)
+ [com.twitter.hbc.core.endpoint.EnterpriseStreamingEndpoint_v2](#endpoint_v2)
+ [com.twitter.hbc.example.EnterpriseStream_v2](enterprise-stream-v2)

#### Adding an authentication request header:
+ [com.twitter.hbc.httpclient.ClientBase](#client-base)
+ [com.twitter.hbc.httpclient.auth.Authentication](#authentication)
  + [com.twitter.hbc.httpclient.auth.BasicAuth](#basic-auth)
  + [com.twitter.hbc.httpclient.auth.OAuth1](#oauth1)

Note that these updates were implemented to enable this library to stream from both versions of PowerTrack. If you only want to use HBC with PowerTrack 2.0, all the 'v2' details can be dropped, and the updates can be folded into the existing non-versioned name space. 

### HttpConstants class<a class='tall' id='http-constants'>&nbsp;</a>

#### com.twitter.hbc.core.HttpConstants 

In this file, we just added the root host domain for PowerTrack 2.0, https://gnip-stream.twitter.com.

```java

package com.twitter.hbc.core;

public class Constants {

  public static final String ENTERPRISE_STREAM_HOST = "https://stream.gnip.com";
  public static final String ENTERPRISE_STREAM_HOST_v2 = "https://gnip-stream.twitter.com";

}
```

### EnterpriseStreamingEndpoint_v2 class <a class='tall' id='endpoint_v2'>&nbsp;</a>

#### com.twitter.hbc.core.endpoint.EnterpriseStreamingEndpoint_v2

Here, we cloned the EnterpriseStreamingEndpoint class and created a new EnterpriseSteamingEndpoint_v2 class. 

The only significant change was in constructing the v2 BASE_PATH, using the same product, account name and stream label tokens, and arranging them in the v2 order:

```java
  private static final String BASE_PATH = "/stream/%s/accounts/%s/publishers/%s/%s.json"; //product, account_name, stream_label
```

The only other changes were updating the class constructors to reflect the new class name.

+ public EnterpriseStreamingEndpoint_v2(String account, String product, String label)
+ public EnterpriseStreamingEndpoint_v2(String account, String product, String label, int clientId) 
+ public EnterpriseStreamingEndpoint_v2(String account, String publisher, String product, String label, int clientId) 


### ClientBase class <a class='tall' id='client-base'>&nbsp;</a>

#### com.twitter.hbc.httpclient.ClientBase

This class contains the key update that enables basic authentication with PowerTrack v2. After creating a HTTP request object, new code was added to explicitly add a basic authentication header to the request. Adding this header requires Base64 encoding of the authentication username and password.

The Base64 encoding is implemented with this new import:

```
import sun.misc.BASE64Encoder;
```

Here is the code block that adds the header to the request:
   
```java
    auth.signRequest(request, postContent);

    //PTv2 update: Explicitly adding Authorization header with Base64 encoded username and password. 
    BASE64Encoder encoder = new BASE64Encoder();
    String authToken =  auth.getUsername() + ":" + auth.getPassword();
    String authValue = "Basic " + encoder.encode(authToken.getBytes());  
    request.addHeader("Authorization", authValue);

    Connection conn = new Connection(client, processor);
```


### Authentication interface <a class='tall' id='authentication'>&nbsp;</a>

#### com.twitter.hbc.httpclient.auth.Authentication


The Authentication call is an interface that gets implemented by both the ```BasicAuth``` and ```OAuth1``` classes. As a first attempt, getUsername and getPassword functions were added to the class interface. This is not ideal since both classes must implement the methods, and OAuth1 has no use of the username/password. For now the OAuth1 implements the 'get' methods by simply returing a ```null```, but a good next step would be to eliminate the (small) Authentication interface.     

```java
public interface Authentication {

  String getUsername();
  String getPassword();

}

```

### BasicAuth class <a class='tall' id='basic-auth'>&nbsp;</a>

#### com.twitter.hbc.httpclient.auth.BasicAuth

This class implements the Authentication class. Since we added the ```getUsername()``` and ```getPassword()``` methods to that interface, we implement the methods here.

```java
  public String getUsername() {
      return this.username;
  }

  public String getPassword() {
        return this.password;
  }

```

### OAuth1 class <a class='tall' id='oauth1'>&nbsp;</a>

#### com.twitter.hbc.httpclient.auth.OAuth1

This class implements the Authentication class. Since we added the ```getUsername()``` and ```getPassword()``` methods to that interface, we implement the methods here. This is not ideal since teh OAuth1 class has no use for the username and password, and the implemented methods simply return ```null```. A good next step would be to eliminate the (small) Authentication interface.    

```java
public String getUsername() {
      return null;
  }

  public String getPassword() {
     return null;
  }
```


### EnterpriseStream_v2 example client<a class='tall' id='enterprise-stream-v2'>&nbsp;</a>

#### com.twitter.hbc.example.EnterpriseStream_v2

To implement a HBC library that can stream from both versions of PowerTrack, this PTv2 example client is a clone of the PTv1 version (EnterpriseStreamExample.java). The updates here consist of:

+ Creating an ```endpoint``` object based on the RealTimeEnterpriseStreamingEndpoint_v2 class. 
+ When creating the ```client``` object, pass in the PTv2 host constant.
+ Uncommented the Main method.


```java
public class EnterpriseStream_v2 {

    RealTimeEnterpriseStreamingEndpoint_v2 endpoint = new RealTimeEnterpriseStreamingEndpoint_v2(account, product, label);

    // Create a new BasicClient. By default gzip is enabled.
    Client client = new ClientBuilder()
            .name("PowerTrackClient-01")
            .hosts(Constants.ENTERPRISE_STREAM_HOST_v2)
            .endpoint(endpoint)
            .authentication(auth)
            .processor(new LineStringProcessor(queue))
            .build();
  }

  public static void main(String[] args) {
    try {
      EnterpriseStream_v2.run(args[0], args[1], args[2], args[3], args[4]);
    } catch (InterruptedException e) {
      System.out.println(e);
    }
  }
}
```
