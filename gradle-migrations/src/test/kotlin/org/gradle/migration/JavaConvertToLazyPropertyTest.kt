package org.gradle.migration

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.compile.CompileOptions
import org.junit.jupiter.api.Test
import org.openrewrite.java.Assertions
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.openrewrite.test.TypeValidation

class JavaConvertToLazyPropertyTest : RewriteTest {
    override fun defaults(spec: RecipeSpec): Unit {
        spec.recipe(JavaConvertToLazyPropertyTest::class.java.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle8to9")
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
                    project.getPlugins().withType(JavaPlugin.class, (javaPlugin) -> {
                        project.getTasks()
                            .named(mainSourceSet.getCompileJavaTaskName(), JavaCompile.class)
                            .configure((compileJava) -> compileJava.getOptions().setIncremental(true));
                    });
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
                    project.getPlugins().withType(JavaPlugin.class, (javaPlugin) -> {
                        project.getTasks()
                            .named(mainSourceSet.getCompileJavaTaskName(), JavaCompile.class)
                            .configure((compileJava) ->  compileJava.getOptions().getIncremental().set(true));
                    });
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
                    project.getTasks().withType(Test.class, (test) -> {
                        test.setMaxHeapSize("1024M");
                    });
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
                    project.getTasks().withType(Test.class, (test) -> {
                        
                        test.getMaxHeapSize().set("1024M");
                    });
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
                    project.getTasks().register("myTest", Test.class, (task) -> {
                        task.setTestClassesDirs(intTestSourceSet.getOutput().getClassesDirs());
                    });
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
                    project.getTasks().register("myTest", Test.class, (task) -> {
                        
                        task.getTestClassesDirs().setFrom(intTestSourceSet.getOutput().getClassesDirs());
                    });
                }
            }
                """.trimIndent()
            )
        )
    }
}