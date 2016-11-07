# artist-api

User guide

How to run using maven:
mvn spring-boot:run

Build a war file (will be placed in target/):
```
mvn package
```

Deploy package:
Copy .war-file to your tomcat-installation/webapps folder.

Run tests:
```
mvn test
```

How to use the webservice:
1. If web service is run with maven:
Direct your browser to the adress and port that the tomcat server is listening on and request mashup-api/?mbid=<MusicBrainz id>
Example: GET http://localhost:8080/mashup-api?mbid=5700dcd4-c139-4f31-aa3e-6382b9af9032
2. If web service is deployed on a tomcat server a valid request will look like this:
GET http://localhost:8080/MusicAPI-0.0.1-SNAPSHOT/mashup-api/?mbid=5700dcd4-c139-4f31-aa3e-6382b9af9032

Developed using:
Java 8, Open JDK
Maven v.3.3.9
SpringBoot v1.4.1.RELEASE
Tomcat v. 8
