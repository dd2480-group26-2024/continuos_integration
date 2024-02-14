# Continuous Integration Server for Maven Projects

## How to run
### Tests 
To run compile and run the tests, use the command `mvn test` at the root of the repository.
If Maven home is not automatically found, `MAVEN_HOME` must be added to environment variables. The directory to be used can be found using `mvn -v`. 
### Run the server
To run the server, first use the command `mvn package`. This will generate a new .jar file under `./target`. To run the server, execute `java -jar target/build-jar-with-dependencies.jar <GITHUB_TOKEN>`. The token must be associated with the permission of editing commit statuses, and must have access to repositories the server will handle.  

Webhooks must then be configured to send push events to the CI server.

## How it works
### Compilation step
After receiving a request from GitHub, the server clone the repository and runs `mvn clean compile`. 

This feature is tested using a Maven archetype project, and a Maven archetype projet with an additional error syntax.
### Testing step
After compilation, the CI server will run `mvn test` to execute the tests.

This feature is tested using a maven archetype project with a basic JUnit test.
### Status notification
The CI server will notify the build status in two ways: 

An email will be sent with the build information and status.

The status of GitHub's commit will be set accordingly to the outcome of `mvn test`.

## Build History
Link to the build history: https://htmlpreview.github.io/?https://github.com/dd2480-group26-2024/continuous_integration/blob/40/build_history/index.html






