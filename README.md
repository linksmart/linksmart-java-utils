[![GitHub tag (latest release)](https://img.shields.io/github/tag/linksmart/linksmart-java-utils.svg?label=release)](https://github.com/linksmart/linksmart-java-utils/tags)
[![Build Status](https://travis-ci.com/linksmart/linksmart-java-utils.svg?branch=master)](https://travis-ci.com/linksmart/linksmart-java-utils)


LinkSmart Java Utility 
======================

# MQTT connection library 

The MQTT library manage the connections and configurations to MQTT broker(s). The library creates single real/network connection for a given connection-profile or broker, and share this connection between different internal/logical clients. This avoids the necesity of creating multiple TCP sockets for to the same broker or connection-profile. E.g., if there are three listeners and one publisher in a given program using the same broker and same connection-profile, if the commponents do not share the client they will create four TCP connections to the broker. Morover, sharing the connection requires to address distribution of the messages and avoiding pub sub deadlocks. 

## Usage 

```java
  package foo.bar;

  import eu.linksmart.services.utils.mqtt.broker.StaticBroker;

  public class Application {

      public static void main(String[] args) throws Exception {

              String brokerAlias = ""; // generic name of the broker (see configuration library)
              String will = null; // programatically given will message 
              String willTopic = null; // programatically given will topic 

              // connecting 
              StaticBroker connection = new StaticBroker(brokerAlias, will, willTopic);

              // subscription 
              connection.addListener("#", message -> {
                  System.out.println("Topic: " + message.getTopic() + " message: " + message.toString());
              });

              // publish
              connection.publish("topic", "message");

              // waiting for the message to arrive before disconnecting (avoids race condition)
              Thread.sleep(100);

              // disconnect from broker
              connection.disconnect();

              // realising resources
              connection.destroy();

      }
  }

```

## Dependency 

LinkSmart maven repo:

```xml
  <repository>
    <id>linksmart</id>
    <url>https://nexus.linksmart.eu/repository/public/</url>
  </repository>
```
Maven dependency:
```xml
  <dependency>
    <groupId>eu.linksmart.commons</groupId>
    <artifactId>utils</artifactId>
    <version>${linksmart.commons.version}</version>
  </dependency>
```

