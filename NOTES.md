# Gradle & Java Development

## Goals:
Understand gradle build scripts and its language
Build a simple Java application with gradle
Document findings

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




