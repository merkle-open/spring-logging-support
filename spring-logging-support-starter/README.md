# spring-logging-support-starter

Spring-Logging-Support module can be configured using Auto-Configuration. This document provides a basic overview on how to utilize the spring-logging-support-starter. Detailed information on how to work with the starter may be observed in the spring-logging-support-samples-starter project.

## Step 1: Add the required dependencies

Add the dependency for the module itself (i.e. spring-logging-support) and the corresponding starter module (i.e. spring-logging-support-starter) which is responsible for the auto-configuration of the module.

    <dependency>
        <groupId>com.namics.oss</groupId>
        <artifactId>spring-logging-support-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.namics.oss</groupId>
        <artifactId>spring-logging-support</artifactId>
        <version>1.0.0</version>
    </dependency>

### Configuration of the web interface
The starter allows you to override the default settings for servlet-name and servlet-mapping.

    # Optional properties for spring-logging-support
    com.namics.oss.spring.support.logging.web.servlet-name=loggingServlet
	com.namics.oss.spring.support.logging.web.servlet-mapping=/nx-log/*
   
Add the iFrame in a view of the application.

	<iframe class="frame" data-th-src="@{'/nx-log/logging.html'}"></iframe>
