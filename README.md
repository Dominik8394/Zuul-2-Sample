# Zuul-2-Sample

Zuul 2 Example based on the sample project provided by Netflix. 

## Filters

Zuul Filters are written in Groovy instead of Java. Filters seem to be integrated properly.

## Routing

Routing is yet not possible with files such like it was done in Zuul 1.x. Therefor routing happens in the application.properties file
with assistance of Netflix Ribbon. It is still unclear how to specify URL routes since previous attempts have not worked accordingly.

## Start the Application

You can run the application with the following command assuming you navigated to the root project.

        gradlew run
 