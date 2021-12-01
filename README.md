# TimeLogger
TimeLogger is a Scala-based REST API which allows the user to manage simple projects.
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

***Code***

**core**

*auth*

*projects*

*tasks*

**http**

*routes*

**utils**

*database*

*responses*

`TimeLogger`


***Tests***

