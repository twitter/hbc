/* Copyright 2014 Twitter, Inc. */
package twitter4j;

import twitter4j.conf.Configuration;

/**
 * This saddens me, but we need a way to get at the JSONObject to Model
 * classes in twitter4j. All of them are package protected, so this was the best
 * we could do.
 */
public class PublicObjectFactory extends JSONImplFactory {

  public PublicObjectFactory(Configuration conf) {
    super(conf);
  }

  public DirectMessage newDirectMessage(JSONObject json) throws TwitterException {
    return new DirectMessageJSONImpl(json);
  }

}
