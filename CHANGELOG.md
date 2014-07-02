== 2.2.0
* Add support for Gnip's PowerTrack streaming APIs.
* Twitter4j - Fix user/message extraction for sitestreams (https://github.com/twitter/hbc/pull/118)

== 2.1.0

* Replace configuration of ConnectionManager with configuration of SchemeRegistry to fix https://github.com/twitter/hbc/issues/113

== 2.0.2

* Allow for configuration of http ConnectionManager
* Corrected code example in README

== 2.0.1

* Stop using deprecated guava method `Closeables.closeQuietly` https://github.com/twitter/hbc/pull/104
* Fix some static analysis warnings

== 2.0.0

* Upgrade JOAuth to 6.0.2 from 4.0.1.
* Upgrade Guava dependency to 14.0.1.
* Removed twitter4j 2.x support. `hbc-twitter4j` module is now based on twitter4j 4.0.1.
Removed the `hbc-twitter4j-v3` module as part of this.
* Removed transitive Scala dependency that was brought in via the previous versions of JOAuth.

== 1.3.0 (Feb 14, 2013)

* Initial public release.
