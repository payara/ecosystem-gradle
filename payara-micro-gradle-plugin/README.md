# Payara Micro Gradle Plugin

## Summary
Payara Micro Gradle Plugin that incorporates payara-micro with the produced artifact. It requires JDK 1.8+.
 
### Latest version available: 1.0.3-SNAPSHOT

## Installing

To use it, simply add the following script to your build.gradle:

```groovy
plugins {
  id "fish.payara.micro-gradle-plugin" version "1.0.3-SNAPSHOT"
}
```

For more information, [click here](https://plugins.gradle.org/plugin/fish.payara.micro-gradle-plugin).

## Configuration sample

```groovy
payaraMicro {
    payaraVersion = '5.192'
    deployWar = false
    useUberJar = true
    daemon = false
    commandLineOptions = [port: 2468]
    javaCommandLineOptions = [Dtest: 'test123', ea:true] 
}
```

## Plugin Tasks

### microBundle
This task bundles the attached project's artifact into uber jar with specified configurations.

- __javaPath__ (optional | default: "java"): Absolute path to the ```java``` executable.
- __payaraVersion__ (optional |  default: 5.192): By default ```microBundle``` task fetches payara-micro with version 5.192.

## microStart
This task start payara-micro with specified configurations.

- __useUberJar__ (optional | default: false): Use created uber-jar that resides in ```build\libs``` folder. The name of the jar artifact will be resolved automatically by evaluating its final name, artifact id and version. This configuration has the higher precedence (in given order) compared to ```payaraMicroAbsolutePath``` and ```payaraVersion```.
- __daemon__ (optional | default: false): Starts payara-micro in separate JVM process and continues with the gradle build.
- __immediateExit__ (optional | default: false): If payara-micro is executed in ```daemon``` mode, the executor thread will wait for the ready message before shutting down its process. By setting ```immediateExit``` to ```true``` you can skip this and instantly interrupt the executor thread. 
- __javaPath__ (optional | default: "java"): Absolute path to the ```java``` executable.
- __payaraMicroAbsolutePath__ (optional): Absolute path to payara-micro executable.
- __payaraVersion__ (optional): default: 5.192): The payara-micro version that will be used with ```microStart``` task.
- __deployWar__ (optional | default: false): If the attached project is of type WAR, it will automatically be deployed to payara-micro if ```deployWar``` is set to ```true```. 
- __javaCommandLineOptions__ (optional): Defines a list of command line options that will be passed to ```java``` executable. Command line options can either be defined as key-value pairs or just as list of values. key-value pairs will be formatted as ``key=value``.
- __commandLineOptions__ (optional): Defines a list of command line options that will be passed onto payara-micro. Command line options can either be defined as key,value pairs or just as list of keys or values separately.


## microStop
This task stops payara-micro with specified configurations. By default this goal tries to find out currently executing payara-micro instance based on project GAV. 

- __processId__ (optional): Process id of the running payara-micro instance.
- __useUberJar__ (optional | default: false): Use created uber-jar that resides in ```build\libs``` folder. The name of the jar artifact will be resolved automatically by evaluating its final name.   
