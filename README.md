# Running the sample migration

The code in the sample project, as is, does not compile.

In order to run OpenRewrite, we disable Kotlin compilation for that project.

Also, lots of weirdnesses (with changes not being picked up) while using the Gradle daemon, so we use single-use daemons.

```
gradle -x sample-project:compileKotlin rewriteRun --no-daemon
```