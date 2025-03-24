# Building the migration plugin

```
gradle :gradle-migrations:build
```

# Running migrations against spring-boot


### Get spring-boot
```
git clone https://github.com/spring-projects/spring-boot.git
```

### Run the migrations against `buildSrc`

```
cd spring-boot
gradle :buildSrc:rewriteRun  -I <LOCAL-PATH-TO-THIS-REPO>/gradle-migrations/apply-migrations.gradle.kts --no-build-cache --no-daemon -Dorg.gradle.jvmargs="-Xmx16g" -x :buildSrc:test
```

We apply an init script, and exclude tasks that we don't want to run in the current repo.

### Verifying the changes made

Examine the changes made:

```
git diff
```

Now build with the target release. In the case of some of the recipes in this project, 
the required target release contains the changes from the `provider-api-migration/public-api-changes` branch from the Gradle repo.

```
<LOCAL-PATH-TO-PROVIDER-API-GRADLE>/bin/gradle build -x test -x :buildSrc:checkFormatMain
```