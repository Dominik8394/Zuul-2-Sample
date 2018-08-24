# Zuul-2-Sample

Zuul 2 Example based on the sample project provided by Netflix. 

## Compatibility

Zuul 2 is not compatibel with Spring-Cloud-framework. It's completely standalone and requires a Setup of a 
Netty server. 


## Filters

Zuul 2 filters are written in Groovy unlike Spring-Cloud-Netflix Zuul which are written in Java.
Since Groovy Syntax does not differ very much from Java Syntax there is not a lot of change. In order to
load the filters properly by the FilterFileManager you need to set the following properties in 
the application.properties file

        ## Loading Filters
        zuul.filters.root=<src/main/groovy/com/netflix/zuul/sample/filters>
        zuul.filters.locations=<${zuul.filters.root}/inbound, ${zuul.filters.root}/endpoint, ${zuul.filters.root}/outbound>
        zuul.filters.packages=<>

Reminder: Locations of filters may vary depending on the actual location.

## Routing

Routing paths can be defined in application.properties file. Routing to backend services is 
done by Netflix Ribbon. Services are then 'registered' with their respective name and port.

        Example with localhost:
        <!-- Do these for each subsequent backend service you would like to work with --->
        <clientname>.ribbon.listOfServers=localhost:<port>
        <clientname>.ribbon.client.NIWSServerListClassName=com.netflix.loadbalancer.ConfigurationBasedServerList
   
## Ports

Zuul runs under port :8080. This can be changed with

        zuul.server.port.main=<port>

In this example we use three different mock services to illustrate the functionality of Zuul 2.x.
Each service runs under the following ports

        users-service = localhost:8081
        images-service = localhost:8082
        comments-service = localhost:8083
        

## Service Discovery 

For sake of simplicity of this example the service discovery eureka is disabled. In respective
application.properties file 

        eureka.shouldFetchRegistry=false
        eureka.validateInstanceId=false

# Start the Application

## Start Zuul 2 Proxy

You can run the Zuul application assuming you navigated to the root project.
Zuul-Sample-Project just type in the following command

       ## For windows leave out the './' in order to execute the command
       ./gradlew run
        

## Start Users-/Images-/Comments-service

Navigate to the respective services' root folder and type 

        ## For windows leave out the './' in order to execute the command
        ./gradlew bootRun
        
 
