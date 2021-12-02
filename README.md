# TimeLogger
TimeLogger is a Scala-based REST API which allows the user to manage simple projects.
Each project has its id, unique name, start time, end time (when it's different from 0L it means the projects has been deleted) and owner.
Smaller working units are represented as tasks (each task has its id, project id, opt. volume, opt. description, start time, working time -> says how long is the task going to last, and end time -> marks whether the task has been deleted when it's different from 0L).
To represent time I used the Long type.
The application cooperates with PostgreSQL database.
Used libraries: Akka-HTTP, Akka, Circe, Slick, Postgresql driver, FlywayDB, Akka-Testkit.


***Resources***

The application runs on files placed in directory `src/main/scala` and grouped in three main catalogues.

**core**

This is the folder where the main functionalities are declared such as database operations and data validations.

**http**

This is the folder where all REST endpoints works.

**utils**

This is the folder where all utilities are placed.



***Tests***

Each file has its own unit tests.