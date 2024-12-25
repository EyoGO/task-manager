# Task Manager
**Yurii Hentash**

**Programming Language**: Java 17+  
**Framework**: Spring  

Before launching, a new database named `taskmanager` must be created in PostgreSQL (in H2, this happens automatically). If necessary, modify PostgreSQL details in the `application.yml` file. To run the application, use the command:  
`java -jar taskManager.jar`.  

After launching, requests from the task specification can be executed on the resource:  
[http://localhost:8080/api/v1/tasks](http://localhost:8080/api/v1/tasks).

A REST service was created with methods corresponding to the requirements for the `Task` entity (`UUID id, Status status, String description`). DDL for both databases is generated automatically based on the annotations of the `Task` entity to simplify the task.

Generated documentation can be viewed in **Swagger**.  
The application has **controller**, **service**, and **repository** layers. Of these, `TaskService` is covered by unit tests at **52%**. The `POST`, `PUT`, and `PATCH` methods include validation of incoming DTOs.

Service methods include small informational logs and logs for database switching/synchronization (output to the console and a file).

As an additional business rule, a duplicate check was added during creation – a `Task` must be unique by status and description (though logically, uniqueness by description alone would suffice).

The most challenging part of the task was adding the backup database. For this, two `DataSource` configurations and corresponding repositories were created for each database. A `@Scheduled` method was implemented for switching between databases. It periodically queries the database to ensure it is operational (though it would be better to add such logic to error handling – if the database fails and a request is sent, we would encounter an error, and after a few attempts, switching to the backup database could occur).  

In case of failure, the program starts working with the repository of the backup database. The `@Scheduled` method continues to ping the primary database to switch back when it becomes available.

It is crucial to maintain consistent data in both databases. Therefore, when switching databases, a data synchronization mechanism is triggered (this mechanism also runs periodically, but during switching, it is forced to execute). For this, the program maintains two ordered lists of create/update/delete events (`List`).  

In normal operation, the program writes data only to one database and adds an event to the list for the other database. Then, the `@Scheduled` method checks for events in the lists and executes the corresponding requests on the second database, clearing the list afterward. Thanks to this approach, everything that happens to the primary database also happens to the backup database in the same sequence.  

(This is a simplified version of asynchronous writing for a test task. In a real system, instead of in-memory lists, a messaging service like Kafka or RabbitMQ would be used.)
