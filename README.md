# Borders Workflow Engine

Central workflow engine within Borders


Functional packaging constructs

#### REST API Controllers

Interface that is exposed over HTTP and define the resources for the engine

#### Application Service Code

Internal application code that uses the native Camunda API and does filtering
based on the user who is currently logged in.  


#### Core Resources
1. Tasks
2. Process Definitions
3. Notifications
4. Sessions
5. Process Instances


See swagger docs:

{env}://swagger-ui.html