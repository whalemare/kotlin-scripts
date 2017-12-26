#### Kotlin Scripts

#### Quick Start

All you need to get going is a JDK, Kotlin installed and a unix-ish environment.

```shell
$ cd kotlin-scripts/
$ ./gradlew check copyToLib
$ export PATH=$PATH:`pwd`/kotlin
$ helloworld.kt
$ vi `which helloworld.kt` # Make some changes
$ helloworld.kt # See the results (takes a few seconds to compile)
$ helloworld.kt # Runs in around 100ms now that it is pre-compiled
```

Now open the cloned project in IntelliJ and edit and run the scripts within the IDE.

#### How it works

* The project itself is just a standard gradle project. It uses the [gradle wrapper](http://www.gradle.org/docs/current/userguide/gradle_wrapper.html) to bootstrap, automatically 
  installing gradle, downloading libraries and compiling.
* Kotlin natively supports scripting to a limited extent, so it is valid to use `#!` [(shebang)](http://en.wikipedia.org/wiki/Shebang_(Unix)) directives at the top of kotlin source files.
* The kickstarter exploits this to call [kotlin-script.sh](/kotlin/kotlin-script.sh) to launch scripts. The script runner checks to see if the script being run has been modified and if so, forces recompilation via gradle. Otherwise, the script is launched immediately.

#### Limitations
* Scripts must end with the `.kt` suffix as IntelliJ uses extensions to detect file types.
* Scripts must be declared in their own unique package or the `main` method will conflict with other scripts.
* The gradle koltin plugin seems to require that kotlin sources live in a directory called `kotlin`

#### License
This project is licensed under a MIT license.