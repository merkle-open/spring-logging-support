# Spring Logging Support

System        | Status
--------------|------------------------------------------------        
CI master     | [![Build Status][travis-master]][travis-url]
CI develop    | [![Build Status][travis-develop]][travis-url]
Dependency    | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.namics.oss.spring.support.logging/spring-logging-support/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.namics.oss.spring.support.logging/spring-logging-support)

This modules aims to provide a universal mechanism to configure logging configuration and get access to logfiles.

## Usage

### Maven Dependency (Latest Version in `pom.xml`):

	<dependency>
		<groupId>com.namics.oss.spring.support.logging</groupId>
		<artifactId>spring-logging-support</artifactId>
		<version>2.0.0</version>
	</dependency>
	
### Requirements	

Java: JDK 8            	 

### Integration

The user interface can be integrated with the spring boot starter.

	<dependency>
		<groupId>com.namics.oss.spring.support.logging</groupId>
		<artifactId>spring-logging-support-starter</artifactId>
		<version>2.0.0</version>
	</dependency>
	
The default iFrame integration is

	<iframe class="frame" data-th-src="@{'/nx-log/logging.html'}"></iframe>

[travis-master]: https://travis-ci.org/namics/spring-logging-support.svg?branch=master
[travis-develop]: https://travis-ci.org/namics/spring-logging-support.svg?branch=develop
[travis-url]: https://travis-ci.org/namics/spring-logging-support

