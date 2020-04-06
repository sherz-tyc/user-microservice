## Introduction
The primary objective of this Microservice is to perform basic CRUD
operations to the User repository. It also provides a relatively 
advanced users search by criteria via an endpoint.
Furthermore, this User Microservice sends out (produce) event 
messages as notifications via Kafka whenever there is a CREATE, 
DELETE or UPDATE operation performed on User repository. Other 
microservices can subscribe to the Kafka and be notified of any
changes to the repository.
Spring Actuator is employed to provide basic monitoring of the
microservice. One can extend the features of this actuator to
provide improved monitoring. 

## Designs/Tools
The following is a list of tools/framework that I have used to 
develop this REST-ful Java application:
- Spring Boot
- Maven
- Rest-Assured Integration Test suite
- Kafka
- Spring JPA
- H2 Database
- JUnit / Mockito
- Spring Actuator

## Building the project
1. Clone/Pull via Git or download zip file of this project (and un-zip)
to a directory;

2. Using command line tool, navigate to the root directory where this
README.md file and, specifically, the pom.xml file is located;
4. Run the following maven command via the command line:

``mvn clean install -DskipTests``

*This command will skip tests
*Check the output logs on the command line console to see if there are
any errors.

## Running the application
Before running the actual application, It is essential to run the
Zookeeper and Kafka servers respectively (in that order) first, as the
microservice depends on the above 2 servers to send and receive event
messages. The following are commands to run the 2 respective servers.
Assuming that Kafka (bundled with Zookeeper) is installed on your 
Windows machine, navigate to the root directory of kafka installation:

``.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties``

``.\bin\windows\kafka-server-start.bat .\config\server.properties``

You may run the application by running the following maven command via 
the command line:

``mvn spring-boot:run``

*Check the output logs on the command line console to see if there are
are any errors.

## Accessing the API documentation
Once the application is running, you can navigate to the following URL
from your browser of choice:

``http://localhost:8080/swagger-ui.html``

From the SwaggerUI interface you can test the application endpoints
via OpenAPI by clicking "Try it out" buttons for the respective
endpoints.

## Run Tests Only
To run tests only, run the following command on command line:

``mvn clean verify``

* Please note that the tests may appear to stall mid-way through, and
the stall will last up to 20 seconds, this is a known bug involving
Kafka that if consumers are created before producers, it causes the
consumer to stall and wait. I have tried several solutions to this
problem but with no luck, I shall fix this once a solution has been
found.

## Building Docker image
A Dockerfile is included in this Spring-Boot application for the
pupose of building a Docker image. In order to perform such action,
Docker must be installed on the machine. Please refer to the Docker
website for instructions on installing Docker the right way.


Thank you for taking an interest in this project.