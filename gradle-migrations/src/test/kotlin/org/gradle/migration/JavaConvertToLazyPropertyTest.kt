package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.Assertions
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.openrewrite.test.TypeValidation

class JavaConvertToLazyPropertyTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(JavaConvertToLazyPropertyTest::class.java.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10")
        // resulting code is not necessarily valid against the current classpath, and that is fine
        spec.afterTypeValidationOptions(TypeValidation.all().methodInvocations(false))
    }

    @Test
    fun convertToProperty() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;
            
            import org.gradle.api.*;
            import org.gradle.api.plugins.*;
            import org.gradle.api.tasks.*;
            import org.gradle.api.tasks.compile.*;
            
            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    SourceSet mainSourceSet = project.getExtensions()
                        .getByType(JavaPluginExtension.class)
                        .getSourceSets()
                        .getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                    project.getPlugins().withType(JavaPlugin.class, (javaPlugin) -> project.getTasks()
                            .named(mainSourceSet.getCompileJavaTaskName(), JavaCompile.class)
                            .configure((compileJava) -> compileJava.getOptions().setIncremental(true)));
                }
            }
                
                """.trimIndent(),
                """
            package com.yourorg;
            
            import org.gradle.api.*;
            import org.gradle.api.plugins.*;
            import org.gradle.api.tasks.*;
            import org.gradle.api.tasks.compile.*;
            
            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    SourceSet mainSourceSet = project.getExtensions()
                        .getByType(JavaPluginExtension.class)
                        .getSourceSets()
                        .getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                    project.getPlugins().withType(JavaPlugin.class, (javaPlugin) -> project.getTasks()
                            .named(mainSourceSet.getCompileJavaTaskName(), JavaCompile.class)
                            .configure((compileJava) -> compileJava.getOptions().getIncremental().set(true)));
                }
            }
                
                """.trimIndent()
            )
        )
    }

    @Test
    fun convertToPropertyDeclaredInSupertype() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;
            
            import org.gradle.api.*;
            import org.gradle.api.plugins.*;
            import org.gradle.api.tasks.*;
            import org.gradle.api.tasks.testing.*;
            
            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    project.getTasks().withType(Test.class, (test) -> test.setMaxHeapSize("1024M"));
                }
            }
                """.trimIndent(),
                """
            package com.yourorg;
            
            import org.gradle.api.*;
            import org.gradle.api.plugins.*;
            import org.gradle.api.tasks.*;
            import org.gradle.api.tasks.testing.*;
            
            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    project.getTasks().withType(Test.class, (test) -> test.getMaxHeapSize().set("1024M"));
                }
            }
                """.trimIndent()
            )
        )
    }


    @Test
    fun convertToFileCollection() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;
            
            import org.gradle.api.*;
            import org.gradle.api.plugins.*;
            import org.gradle.api.tasks.*;
            import org.gradle.api.tasks.testing.*;
            
            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    SourceSet intTestSourceSet = project.getExtensions()
                        .getByType(JavaPluginExtension.class)
                        .getSourceSets()
                        .getByName(SourceSet.TEST_SOURCE_SET_NAME);
                    project.getTasks().register("myTest", Test.class, (task) -> task.setTestClassesDirs(intTestSourceSet.getOutput().getClassesDirs()));
                }
            }
                
                """.trimIndent(),
                """
            package com.yourorg;
            
            import org.gradle.api.*;
            import org.gradle.api.plugins.*;
            import org.gradle.api.tasks.*;
            import org.gradle.api.tasks.testing.*;
            
            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    SourceSet intTestSourceSet = project.getExtensions()
                        .getByType(JavaPluginExtension.class)
                        .getSourceSets()
                        .getByName(SourceSet.TEST_SOURCE_SET_NAME);
                    project.getTasks().register("myTest", Test.class, (task) -> task.getTestClassesDirs().setFrom(intTestSourceSet.getOutput().getClassesDirs()));
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun migratesIntegerPropertySetter() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;

            import org.gradle.api.tasks.testing.Test;

            class Build {
                void cfg(Test t) {
                    t.setMaxParallelForks(4);
                }
            }
                """.trimIndent(),
                """
            package com.yourorg;

            import org.gradle.api.tasks.testing.Test;

            class Build {
                void cfg(Test t) {
                    t.getMaxParallelForks().set(4);
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun migratesBooleanPropertySetterFromExecSpec() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;

            import org.gradle.process.ExecSpec;

            class Build {
                void cfg(ExecSpec spec) {
                    spec.setIgnoreExitValue(true);
                }
            }
                """.trimIndent(),
                """
            package com.yourorg;

            import org.gradle.process.ExecSpec;

            class Build {
                void cfg(ExecSpec spec) {
                    spec.getIgnoreExitValue().set(true);
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun migratesStringPropertySetter() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;

            import org.gradle.api.tasks.compile.CompileOptions;

            class Build {
                void cfg(CompileOptions opts) {
                    opts.setEncoding("UTF-8");
                }
            }
                """.trimIndent(),
                """
            package com.yourorg;

            import org.gradle.api.tasks.compile.CompileOptions;

            class Build {
                void cfg(CompileOptions opts) {
                    opts.getEncoding().set("UTF-8");
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun doesNotMigrateUnrelatedSetter() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;

            class Build {
                private String name;
                public void setName(String name) { this.name = name; }
                void cfg() { setName("abc"); }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun migratesListPropertySetter() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;

            import java.util.List;
            import org.gradle.api.tasks.compile.CompileOptions;

            class Build {
                void cfg(CompileOptions opts, List<String> args) {
                    opts.setCompilerArgs(args);
                }
            }
                """.trimIndent(),
                """
            package com.yourorg;

            import java.util.List;
            import org.gradle.api.tasks.compile.CompileOptions;

            class Build {
                void cfg(CompileOptions opts, List<String> args) {
                    opts.getCompilerArgs().set(args);
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun migratesMapPropertySetter() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;

            import java.util.Map;
            import org.gradle.api.tasks.testing.Test;

            class Build {
                void cfg(Test t, Map<String, Object> sys) {
                    t.setSystemProperties(sys);
                }
            }
                """.trimIndent(),
                """
            package com.yourorg;

            import java.util.Map;
            import org.gradle.api.tasks.testing.Test;

            class Build {
                void cfg(Test t, Map<String, Object> sys) {
                    t.getSystemProperties().set(sys);
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun rewritesMultipleSetterKindsInOnePass() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;

            import java.util.List;
            import org.gradle.api.file.FileCollection;
            import org.gradle.api.tasks.testing.Test;

            class Build {
                void cfg(Test t, FileCollection cp, List<String> jvmArgs) {
                    t.setMaxParallelForks(4);
                    t.setClasspath(cp);
                    t.setJvmArgs(jvmArgs);
                }
            }
                """.trimIndent(),
                """
            package com.yourorg;

            import java.util.List;
            import org.gradle.api.file.FileCollection;
            import org.gradle.api.tasks.testing.Test;

            class Build {
                void cfg(Test t, FileCollection cp, List<String> jvmArgs) {
                    t.getMaxParallelForks().set(4);
                    t.getClasspath().setFrom(cp);
                    t.getJvmArgs().set(jvmArgs);
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun migratesClasspathSetterAsSetFrom() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;

            import org.gradle.api.file.FileCollection;
            import org.gradle.api.tasks.testing.Test;

            class Build {
                void cfg(Test t, FileCollection cp) {
                    t.setClasspath(cp);
                }
            }
                """.trimIndent(),
                """
            package com.yourorg;

            import org.gradle.api.file.FileCollection;
            import org.gradle.api.tasks.testing.Test;

            class Build {
                void cfg(Test t, FileCollection cp) {
                    t.getClasspath().setFrom(cp);
                }
            }
                """.trimIndent()
            )
        )
    }
}