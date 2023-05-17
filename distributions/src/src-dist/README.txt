
Welcome to the world of Qi4j
   - Composite Oriented Programming on the Java platform.


This Qi4j Source Distribution contains everything you need to
create Qi4j applications.


Qi4j started in 2007, and is still in heavy development under the umbrella
of the Qi4j project at the Apache Software Foundation. We would
like developers around the world to participate in the advancement of this
cool, new and challenging technology. We are especially interested in
people willing to help improve the SDK, samples, tutorials, documentation
and other supporting material.

Please see https://qi4j.github.com for more information.


Licensing
---------
All Qi4j code is licensed subject to the Apache License.

Third-Party Dependencies may be licensed under other terms. The only
required dependencies are SLF4J (MIT Licence) and ASM (BSD Licence).

Finally, Qi4j TestSupport depends on JUnit 4.x and its dependencies, which
is also not included in the SDK itself, as it is present among most Java
developers.


Dependencies not included
-------------------------
The source distribution contains Qi4j sources only to keep the download
size small. The Gradle build automatically downloads needed dependencies.
If you need to go offline type `./gradlew goOffline` to ensure all needed
dependencies are cached by Gradle.

If you prefer to use a dependency management system, go to:
https://qi4j.github.com/latest/howto-depend-on-qi4j.html


Building Qi4j
---------------------
To build Qi4j from sources you only need to have a valid Java JDK >= 8
installation and any version of Gradle.

This distribution embeds the exact version of Gradle needed to build Qi4j.
Here is how to bootstrap that version:

    gradle -b gradle/wrapper-install/build.gradle install

This will deploy the Gradle Wrapper and create gradlew and gradlew.bat script
files that you should use from now on.

Once you have the wrapper installed you can invoke the build system.

Here is how to run a full build with checks:

    ./gradlew check assemble

Running gradlew for the first time will download the required Gradle
distribution. You can run ./gradlew tasks to list available tasks.

Also, if Docker engine is present on your system, a large amount of time
will be spent in testing the many Entity Store implementations, as it
will spin up the many external systems for the tests (on at a time). If
Docker is not running, and those external systems are not available locally,
then those tests will be skipped automatically.

Read the Qi4j Build System tutorial for more details:
https://qi4j.github.com/latest/build-system.html


Thank you for trying out Qi4j and Composite Oriented Programming.


-- Qi4j Team

