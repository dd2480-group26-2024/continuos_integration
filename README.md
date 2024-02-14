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
Link to the build history: https://htmlpreview.github.io/?https://github.com/dd2480-group26-2024/continuous_integration/blob/main/build_history/index.html

Locally, the build history is saved under `./build_history` and is preserved after server shutdown.

## Documentation 
Open the index.html file located in doc/

## Statement of Contribution
`Marcus Jakobsson:` I implemented and documented the function updateGitHubStatus and compileMavenProject, and also created tests for each function.Participated in group programming for combining everyone's parts.

`Toto Roomi:` I implemented the first iteration of runTests() , made a test for it and a Maven test project, added the documentation to the right folder /doc. Participated in group programming for combining everyone's parts.

`Rémi Grasset:` Setup the basic maven project. Worked on extracting data from GitHub's request and writing the build history (processRequestDatan, saveToBuildHistory) and the tests for those features. Participated in group programming for combining everyone's parts.

`Kerem Robin Yurt:` I implemented sendEmailNotification and set up tests for it, documented that method, runTests and a explanation line for cloneAndCheckout. Took initiative for the SEMAT part and discussed it with the group and then wrote it. Participated in group programming for combining everyone's parts.

`Anton Bölenius:` Wrote the cloneAndCheckout function with test, fixed deleting of directory, took initiative in issue planning and delegation, participated in group programming for combining everyone's parts.

## SEMAT
The team started the assignment with a group meeting where we discussed how we should divide the work. The outcomes of these assignments was looked as a possible learning to understand how CI systems work and how one could be developed. The team worked individually but used Discord to communicate and also to ask about the process. With regular meetings the team discussed and planned the next steps in the assignment, regular meetings lead to the team growth. Mechanism that were used to grow the team was to have the meetings and also being able to help out when it was needed. The team set up regular deadlines to make sure that everyone is committed to the assignment. With the first meeting, the goals of the assignment were clear and understood by everybody. Each individual in the team had the understanding of their role and what to do. With common deadlines, everyone knew when their part of work had to be done. Regular meetings were held in Discord (virtually). To get a perfect fit, we assigned the different parts of the assignment after competences, and that way the work was balanced. The team split up the assignment in different parts and assigned each part to a group member. The communication in the group was clear. The work was done individually, but when it was time to set up the different parts so that the assignment worked,  the group worked together. Tools as Live Share was used to find bugs and make small changes. When adaptions had to be made, the group identified the problems and implemented solutions. With continuous deadlines, the work was done in time. The work was efficient and done in time by the group. The group worked effectively and where done in time.
