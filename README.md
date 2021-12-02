# TimeLogger
TimeLogger is a Scala-based REST API which allows the user to manage simple projects.
Each project has its id, unique name, start time, end time (when it's different from 0L it means the projects has been deleted) and owner.
Smaller working units are represented as tasks (each task has its id, project id, opt. volume, opt. description, start time, working time -> says how long is the task going to last, and end time -> marks whether the task has been deleted when it's different from 0L).
To represent time I used the Long type.
The application cooperates with PostgreSQL database.
Used libraries: Akka-HTTP, Akka, Circe, Slick, Postgresql driver, FlywayDB, Akka-Testkit.

***Database Tables***
Database uses three tables -> auth, projects and tasks.

Auth table contains users' nicknames and ids (they are used in order to get authentication token, which then is used to check whether a user is or isn't allowed to do something).

Projects table contains all the data about the projects.

Tasks table contains all the data about the tasks

***Resources***

The application runs on files placed in directory `src/main/scala` and grouped in three main catalogues.

**core**

This is the folder where the main functionalities are declared such as database operations and data validations.

Projects are required to have unique names and tasks within one project cannot overlap.
**http**

This is the folder where all REST endpoints works.

`auth\signIn`
* post -> returns an auth token if correct user's nickname has been provided

`auth\singUp`
* post -> returns an auth token and adds a new user to the database

`/projects/single_project`
* get -> return a project with all its tasks if a user is project's owner
* delete -> marks project as deleted if a user is project's owner
* post -> updates an existing project if a user is project's owner
* put -> adds new project to the database

`/projects/all_projects`
* get -> return all user's projects with their tasks which may be sorted or filtered is several ways (returns only specific projects, returns only deleted/undeleted, return only those which took part within specific time boundaries, are sorted by starting time, are sorted by task that start the latest/earliest)

`/tasks`
* put -> adds new task to the database if the project has been deleted
* delete -> marks task as deleted if a user is its project's owner
* post -> updates an existing task if a user is its project's owner

**utils**

This is the folder where all utilities are placed.

Responses types:
* default -> Ok
* NameOccupiedResponse -> Conflict
* NoResourceResponse -> NoContent
* NotAuthorisedResponse -> Unauthorized
* TimeConflictResponse -> Conflict

***Tests***

Each file has its own unit tests.