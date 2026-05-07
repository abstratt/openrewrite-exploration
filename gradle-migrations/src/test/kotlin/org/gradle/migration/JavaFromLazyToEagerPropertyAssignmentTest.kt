package org.gradle.migration

import org.junit.jupiter.api.Test
import org.openrewrite.java.Assertions
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.openrewrite.test.TypeValidation

class JavaFromLazyToEagerPropertyAssignmentTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            javaClass.getResourceAsStream("/META-INF/rewrite/rewrite.yml")!!,
            "org.gradle.migration.Gradle9to10",
        )
        spec.afterTypeValidationOptions(TypeValidation.all().methodInvocations(false))
    }

    @Test
    fun addsGetCallForDirectAssignment() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;

            import java.net.URI;
            import org.gradle.api.*;
            import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    project.getRepositories().withType(MavenArtifactRepository.class, (repository) -> {
                        URI u = repository.getUrl();
                    });
                }
            }

                """.trimIndent(),
                """
            package com.yourorg;

            import java.net.URI;
            import org.gradle.api.*;
            import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    project.getRepositories().withType(MavenArtifactRepository.class, (repository) -> {
                        URI u = repository.getUrl().get();
                    });
                }
            }

                """.trimIndent()
            )
        )
    }

    @Test
    fun addsGetCallForMethodArgument() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;

            import java.net.URI;
            import org.gradle.api.*;
            import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    project.getRepositories().withType(MavenArtifactRepository.class, (repository) -> {
                        consume(repository.getUrl());
                    });
                }

                private void consume(URI url) {
                }
            }

                """.trimIndent(),
                """
            package com.yourorg;

            import java.net.URI;
            import org.gradle.api.*;
            import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    project.getRepositories().withType(MavenArtifactRepository.class, (repository) -> {
                        consume(repository.getUrl().get());
                    });
                }

                private void consume(URI url) {
                }
            }

                """.trimIndent()
            )
        )
    }

    @Test
    fun addsGetCallBeforeChainedMethod() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;

            import org.gradle.api.*;
            import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    project.getRepositories().withType(MavenArtifactRepository.class, (repository) -> {
                        String s = repository.getUrl().toASCIIString();
                    });
                }
            }

                """.trimIndent(),
                """
            package com.yourorg;

            import org.gradle.api.*;
            import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    project.getRepositories().withType(MavenArtifactRepository.class, (repository) -> {
                        String s = repository.getUrl().get().toASCIIString();
                    });
                }
            }

                """.trimIndent()
            )
        )
    }

    @Test
    fun doesNotAddGetCallWhenAssigningToProvider() {
        rewriteRun(
            { spec -> spec.typeValidationOptions(TypeValidation.none()) },
            Assertions.java(
                """
            package com.yourorg;

            import java.net.URI;
            import org.gradle.api.*;
            import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
            import org.gradle.api.provider.Provider;

            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    project.getRepositories().withType(MavenArtifactRepository.class, (repository) -> {
                        Provider<URI> p = repository.getUrl();
                    });
                }
            }

                """.trimIndent()
            )
        )
    }

    @Test
    fun doesNotAddGetCallWhenPassingToProviderArgument() {
        rewriteRun(
            { spec -> spec.typeValidationOptions(TypeValidation.none()) },
            Assertions.java(
                """
            package com.yourorg;

            import java.net.URI;
            import org.gradle.api.*;
            import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
            import org.gradle.api.provider.Provider;

            public class ConfigurationPropertiesPlugin implements Plugin<Project> {
                @Override
                public void apply(Project project) {
                    project.getRepositories().withType(MavenArtifactRepository.class, (repository) -> {
                        consume(repository.getUrl());
                    });
                }

                private void consume(Provider<URI> url) {
                }
            }

                """.trimIndent()
            )
        )
    }

    @Test
    fun insertsGetOnChainedEagerMethodOfCataloguedProperty() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;

            import org.gradle.api.tasks.testing.Test;

            class Build {
                boolean cfg(Test t) {
                    return t.getMaxHeapSize().endsWith("g");
                }
            }
                """.trimIndent(),
                """
            package com.yourorg;

            import org.gradle.api.tasks.testing.Test;

            class Build {
                boolean cfg(Test t) {
                    return t.getMaxHeapSize().get().endsWith("g");
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun doesNotTouchUncatalogedProperty() {
        rewriteRun(
            Assertions.java(
                """
            package com.yourorg;

            import org.gradle.api.provider.Provider;

            class Build {
                boolean cfg(Provider<String> unrelated) {
                    return unrelated.toString().startsWith("x");
                }
            }
                """.trimIndent()
            )
        )
    }

    @Test
    fun doesNotAddGetCallWhenMethodReturnsProperty() {
        rewriteRun(
            { spec -> spec.typeValidationOptions(TypeValidation.none()) },
            Assertions.java(
                """
            package com.yourorg;

            import org.gradle.api.provider.Property;
            import org.gradle.api.tasks.testing.Test;

            class Build {
                Property<String> cfg(Test t) {
                    return t.getMaxHeapSize();
                }
            }
                """.trimIndent()
            )
        )
    }
}
