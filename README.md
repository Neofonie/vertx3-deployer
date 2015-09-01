# Vertx 3 Deployer Module

One of the problems you encounter when you have a lot of verticles to deploy, 
is how to define dependencies between them, define an order, and organize the 
configuration. 

This project aims to solve those issues through a configuration file where you
can define the verticles and the dependencies between them. You can easily
create a configuration which will be passed to the verticle.

## Configuration

The configuration file a fairly easy. You define a collection called 
"verticles" which contains a list of verticles to be deployed. Each definition
of a verticle requires a name. This name acts as an ID for references.

There are only two fields related to the logic of the Deployer. You must
specify a name. The name is the class which must be available on the classpath. 

The seconds important field is "dependsOn". In the list "dependsOn"
you can add a list of IDs of verticles which need to be started BEFORE the 
start of this verticle. The "dependsOn" field is optional. When it is omitted
the verticle will start immediately. All the verticles on the same level
are started asynchronously. For example, when two verticles do not have a
"dependsOn" restriction, they will start at the same moment.

When one verticle does not start, the process is terminated.

```
{
   "verticles": [
         "mongodb": {
            "worker": true,
            "name": "de.neofonie.verticle.MongoVerticle",
            "instances": 1,
            "multithreaded": false
          },
          "import": {
            "dependsOn": ["mongodb"],
            "name": "de.neofonie.verticle.ImportVerticle"
          },
          "server": {
            "dependsOn": ["mongodb", "import"],
            "name": "de.neofonie.verticle.ServerVerticle"
          }
    ] 
}
```
All other fields you define are passed into the Verticle-core. They are not
sanity checked by the deployer.

## Configuration

You can pass a specific configuration to a verticle using the "config" field.


## global-config

Sometimes you will have the need to share configuration parameters across
more verticles. You can repeat this configuration in every verticle or you
can make use of a special construction in the JSON. This configuraton will
be available in all verticles.

{
  "global-config": {
    "service-name": "${service.name}",
    "drugs-import": {
        "pipeSize": 52428800,
        "saveBatchSize": 64,
        "maxSaveChunks": 5
    }
   "verticles": [
        // ....
    ] 
}

## The StartVerticle

For ease of use, we have constructed a StartVerticle which does the 
initialisation of the DeployerVerticle for you. The StartVerticle also takes
care for the destruction of the verticles when a SIGTERM/SIGINT signal is
received by the application. This is useful when your application is running
in a Docker container and needs to do a graceful shutdown. We have defined
an interval of 5 seconds in which the application can shut down gracefully
without being terminated. When you need more time to destruct the application
you are probably doing something wrong, hence we made it a fixed value. Of 
course, you can always write your own StartVerticle with your own logic inside.