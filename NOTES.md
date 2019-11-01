# Gradle & Java Development

## Goals:
* Understand gradle build scripts and its language
* Build a simple Java application with gradle
* Document findings

## Groovy
Groovy is an object oriented programming language with features that allow quick development of custom  domain specific languages.

### Methods:
``` groovy
def mymethod(param1, param2, param3) {
	// do something with params
}
```

### Return:
You can omit the return keyword on the last statement in a method.

```groovy
def mymethod(param1, param2, param3) {
	return 'result'
}
```

becomes

```groovy
def mymethod(param1, param2, param3) {
	'result'
}
```

### Method Calling:
https://mrhaki.blogspot.com/2009/10/groovy-goodness-optional-parenthesis.html
http://www.groovy-lang.org/style-guide.html#_omitting_parentheses
You can omit parentheses when calling methods:
```groovy
mymethod('hello')
```

becomes

```groovy
mymethod 'hello'
```

### Maps: http://docs.groovy-lang.org/latest/html/documentation/core-syntax.html#_maps
```groovy
def colors = [red: '#FF0000', green: '#00FF00', blue: '#0000FF']
```

### Closures: https://groovy-lang.org/closures.html
```groovy
def myclosure = {x -> x * x}
```
Closures are basically anonymous methods.
by default they have a single argument called ‘it’
the value of the last statement in the closure is returned by the closure
closures can have delegates meaning that “unbound” method calls inside the closure are resolved by looking for said method on the delegate https://groovy-lang.org/closures.html#_delegation_strategy

## Gradle

Gradle is a very general build system. It builds on top of the groovy language.


### Task Syntax
https://stackoverflow.com/questions/27584463/understanding-the-groovy-syntax-in-a-gradle-task-definition
Gradle builds on top of groovy. To make things a bit more confusing there is a compilation step that takes the build.gradle and transforms it. Thats why things like:

```groovy
task hello {
	println 'hello'
}
```

actually mean:

```groovy
task('hello', { println 'hello' })
```

### Gradle Phases
In Gradle there is the distinction between configuration and execution phase. In the configuration phase a build script is executed and configures a taskGraph. Each node in the graph is actually a task object that depends on other tasks.

In the execution phase a specified task is executed together with its dependencies.

```groovy
task distribution {
    doLast {
        println "We build the zip with version=$version"
    }
}

task release {
    dependsOn 'distribution'
    doLast {
        println 'We release now'
    }
}
```

### Task Types
There are many types of pre built gradle tasks and you can define your own types. To specify a new task of a specific type you can use the type argument. The example shows a common task type called Copy that enables copying of files

```groovy
task copyReportsForArchiving(type: Copy) {
    from "$buildDir/reports/my-report.pdf", "src/docs/manual.pdf"
    into "$buildDir/toArchive"
}
```

Another interesting task type is Zip.

### Custom Task Types
Custom types of tasks can define inputs, outputs and other properties that enable the build system to efficiently determine what needs to be built when.

https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:task_inputs_outputs

For example using the method decorator @OutputDirectory the task class can define where the outputs are written to. This can be used as the input for another task for example:

```groovy
task packageClasses(type: Zip) {
    archiveAppendix = "classes"
    destinationDirectory = file(archivesDirPath)

    from compileJava
}

```

Here from compileJava uses the outputs of the compileJava task as input to the ZIp task.

### Compiling Java
The most basic configuration for a Java project is the following: 
```groovy
plugins {
    id 'java'
}
```

This will add the java plugin to the build script. That plugin adds a lot of default tasks. If you adhere to the default project structure this will be enough to build your Java project. See https://docs.gradle.org/current/userguide/java_plugin.html for details.

The java plugin is a general plugin that builds jar files. To make this actually executable easily you can use the application plugin (which includes the java plugin) https://docs.gradle.org/current/userguide/application_plugin.html

```groovy
plugins {
    id 'application'
}
```

```groovy
application {
    mainClassName = 'HelloWorld'
}
```

### Java Dependencies
Most external dependencies are hosted on a maven repository. There is one central repository for that called maven central. You can make use of this repository by adding it to your build.gradle

```groovy
repositories {
    mavenCentral()
}
```

To know how to include a dependency now go the repository site for example: https://mvnrepository.com/artifact/org.nanohttpd/nanohttpd/2.3.0 and look at the gradle tab. It will provide you with the code that you need to include a dependency.

For example this is what the build gradle looks for using nanohttpd:

```groovy
plugins {
    id 'application'
}

repositories {
    mavenCentral()
}

application {
    mainClassName = 'HelloWorld'
}

dependencies {
    compile group: 'org.nanohttpd', name: 'nanohttpd', version: '2.3.1'
}
```

## Spring Framework

The Spring framework seems to be the most used framework for building web services in Java. We will try to build a simple REST service that lets us get a greeting and go from there.

https://spring.io/guides/gs/rest-service/ provides a good starting tutorial.

### Resource

Spring automatically converts Java objects into JSON using the Jackson JSON library. So we only need to define a class that represents our resource and marshalling will be handled by Spring and Jackson. Next we need to define a controller that serves objects of the resource. So we end up with the following two files:

```java
package learn_java_rest.rest_resources;

public class Greeting {
    private final long id;
    private final String content;

    public Greeting(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
```

```java
package learn_java_rest.rest_controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import learn_java_rest.rest_resources.Greeting;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(value = "/greeting", method = RequestMethod.GET)
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, name));
    }
}
```

To run this application now we first need to have an entry point. This is done through our main application class. 

```java
package learn_java_rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class APIServer {
    public static void main(String[] args) {
        SpringApplication.run(APIServer.class, args);
    }
}
```

Note that the dependencies that we import here need to be specified in our build.gradle like follows. Although the tutorial talks about using the springboot plugin I wanted to understand every line of the build.gradle so I tried to keep it as minimal as possible. This is what I ended up with:

```groovy
plugins {
    id 'application'
}

repositories {
    mavenCentral()
}

application {
    mainClassName = 'learn_java_rest.APIServer'
}

dependencies {
    compile group: 'org.springframework', name: 'spring-context', version: '5.2.0.RELEASE'
    compile group: 'org.springframework', name: 'spring-core', version: '5.2.0.RELEASE'
    compile group: 'org.springframework.boot', name: 'spring-boot', version: '2.2.0.RELEASE'
    compile group: 'org.springframework.boot', name: 'spring-boot-autoconfigure', version: '2.2.0.RELEASE'
    compile group: 'org.springframework', name: 'spring-web', version: '5.2.0.RELEASE'
    compile group: 'org.springframework.boot', name: 'spring-boot-starter-web', version: '2.2.0.RELEASE'
}

```
(I found these dependencies by first including only dependencies that where imported and then following the error messages. For example `spring-boot-starter-web` needs to be there otherwise the application will not be served but only built.)

To run this application now we use `gradle`:
```bash
./gradlew -q run
```

If everything is setup correctly this will build and serve the application under http://localhost:8080 .


