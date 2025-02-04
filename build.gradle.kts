import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.github.gradle.node.npm.task.NpmTask
import io.smallrye.openapi.api.OpenApiConfig
import java.util.Locale

/*
 * SPDX-FileCopyrightText: 2023 Lifely
 * SPDX-License-Identifier: EUPL-1.2+
 */

plugins {
    java
    kotlin("jvm") version "1.9.23"
    war
    jacoco

    id("org.jsonschema2pojo") version "1.2.1"
    id("org.openapi.generator") version "7.4.0"
    id("com.github.node-gradle.node") version "7.0.2"
    id("org.barfuin.gradle.taskinfo") version "2.2.0"
    id("io.smallrye.openapi") version "3.10.0"
    id("org.hidetake.swagger.generator") version "2.19.2"
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
    id("com.bmuschko.docker-remote-api") version "9.4.0"
    id("com.diffplug.spotless") version "6.25.0"
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "net.atos.common-ground"
description = "Zaakafhandelcomponent"

// make sure the Java version is supported by WildFly
// and update our base Docker image and JDK versions in our GitHubs workflows accordingly
val javaVersion = JavaVersion.VERSION_21

val zacDockerImage by extra {
    if (project.hasProperty("zacDockerImage")) {
        project.property("zacDockerImage").toString()
    } else {
        "ghcr.io/infonl/zaakafhandelcomponent:dev"
    }
}

val versionNumber by extra {
    if (project.hasProperty("versionNumber")) {
        project.property("versionNumber").toString()
    } else {
        "dev"
    }
}

val branchName by extra {
    if (project.hasProperty("branchName")) {
        project.property("branchName").toString()
    } else {
        "localdev"
    }
}

val commitHash by extra {
    if (project.hasProperty("commitHash")) {
        project.property("commitHash").toString()
    } else {
        "localdev"
    }
}

// create custom configuration for extra dependencies that are required in the generated WAR
val warLib by configurations.creating {
    extendsFrom(configurations["compileOnly"])
}

// create custom configuration for the JaCoCo agent JAR used to generate code coverage of our integration tests
// see: https://blog.akquinet.de/2018/09/06/test-coverage-for-containerized-java-apps/
val jacocoAgentJarForItest by configurations.creating {
    isTransitive = false
}

sourceSets {
    // create custom integration test source set
    create("itest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.apache.commons:commons-text:1.11.0")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("commons-io:commons-io:2.15.1")
    implementation("com.opencsv:opencsv:5.9")
    implementation("org.flowable:flowable-engine:7.0.1")
    implementation("org.flowable:flowable-cdi:7.0.1")
    implementation("org.flowable:flowable-cmmn-engine:7.0.1")
    implementation("org.flowable:flowable-cmmn-cdi:7.0.1")
    implementation("org.flowable:flowable-cmmn-engine-configurator:7.0.1")
    implementation("org.slf4j:slf4j-jdk14:2.0.12")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("javax.cache:cache-api:1.1.1")
    implementation("com.google.guava:guava:33.1.0-jre")
    implementation("com.mailjet:mailjet-client:5.2.5")
    implementation("com.itextpdf:kernel:8.0.3")
    implementation("com.itextpdf:layout:8.0.3")
    implementation("com.itextpdf:io:8.0.3")
    implementation("com.itextpdf:html2pdf:5.0.3")
    implementation("org.flywaydb:flyway-core:10.10.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.10.0")
    implementation("org.apache.solr:solr-solrj:9.5.0")
    implementation("nl.info.webdav:webdav-servlet:1.2.42")
    implementation("net.sourceforge.htmlcleaner:htmlcleaner:2.29")
    implementation("com.unboundid:unboundid-ldapsdk:7.0.0")

    swaggerUI("org.webjars:swagger-ui:5.12.0")

    // enable detekt formatting rules. see: https://detekt.dev/docs/rules/formatting/
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.5")

    runtimeOnly("org.infinispan:infinispan-jcache:15.0.0.Final")
    runtimeOnly("org.infinispan:infinispan-cdi-embedded:15.0.0.Final")

    // declare dependencies that are required in the generated WAR; see war section below
    // simply marking them as 'compileOnly' or 'implementation' does not work
    warLib("org.apache.httpcomponents:httpclient:4.5.14")
    warLib("org.reactivestreams:reactive-streams:1.0.4")
    // WildFly does already include the Jakarta Mail API lib so not sure why, but we need to
    // include it in the WAR or else ZAC will fail to be deployed
    warLib("jakarta.mail:jakarta.mail-api:2.1.3")

    // dependencies provided by Wildfly
    // update these versions when upgrading WildFly
    // you can find most of these dependencies in the WildFly pom.xml file
    // of the WidFly version you are using on https://github.com/wildfly/wildfly
    // for others you need to check the 'modules' directory of your local WildFly installtion
    providedCompile("jakarta.platform:jakarta.jakartaee-api:10.0.0")
    providedCompile("org.eclipse.microprofile.rest.client:microprofile-rest-client-api:3.0.1")
    providedCompile("org.eclipse.microprofile.config:microprofile-config-api:3.1")
    providedCompile("org.eclipse.microprofile.health:microprofile-health-api:4.0.1")
    providedCompile("org.eclipse.microprofile.fault-tolerance:microprofile-fault-tolerance-api:4.0.2")
    providedCompile("org.jboss.resteasy:resteasy-multipart-provider:6.2.7.Final")
    providedCompile("org.wildfly.security:wildfly-elytron-http-oidc:2.2.3.Final")
    providedCompile("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    // ~dependencies provided by Wildfly

    // yasson is required for using a JSONB context in our unit tests
    // where we do not have the WildFly runtime environment available
    testImplementation("org.eclipse:yasson:3.0.3")
    testImplementation("io.kotest:kotest-runner-junit5:5.8.1")
    testImplementation("io.mockk:mockk:1.13.10")

    // integration test dependencies
    "itestImplementation"("org.testcontainers:testcontainers:1.19.7")
    "itestImplementation"("org.testcontainers:mockserver:1.19.7")
    "itestImplementation"("org.testcontainers:postgresql:1.19.7")
    "itestImplementation"("org.json:json:20240303")
    "itestImplementation"("io.kotest:kotest-runner-junit5:5.8.1")
    "itestImplementation"("io.kotest:kotest-assertions-json:5.8.1")
    "itestImplementation"("org.slf4j:slf4j-simple:2.0.12")
    "itestImplementation"("io.github.oshai:kotlin-logging-jvm:6.0.3")
    "itestImplementation"("com.squareup.okhttp3:okhttp:4.12.0")
    "itestImplementation"("com.squareup.okhttp3:okhttp-urlconnection:4.12.0")
    "itestImplementation"("org.awaitility:awaitility-kotlin:4.2.1")
    "itestImplementation"("org.mock-server:mockserver-client-java:5.15.0")

    jacocoAgentJarForItest("org.jacoco:org.jacoco.agent:0.8.11:runtime")
}

tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektApply") {
    description = "Apply detekt fixes."
    autoCorrect = true
    ignoreFailures = true
}
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    config.setFrom("$rootDir/config/detekt.yml")
    setSource(files("src/main/kotlin", "src/test/kotlin", "src/itest/kotlin"))
    // our Detekt configuration build builds upon the default configuration
    buildUponDefaultConfig = true
}

jacoco {
    toolVersion = "0.8.11"
}

java {
    java.sourceCompatibility = javaVersion
    java.targetCompatibility = javaVersion

    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion.majorVersion)
    }

    // add our generated client code to the main source set
    sourceSets["main"].java.srcDir("$rootDir/src/generated/java")
}

jsonSchema2Pojo {
    // generates Java model files for the "productaanvraag" JSON schema(s)
    setSource(files("$rootDir/src/main/resources/json-schema"))
    targetDirectory = file("$rootDir/src/generated/java")
    setFileExtensions(".schema.json")
    targetPackage = "net.atos.zac.aanvraag.model.generated"
    setAnnotationStyle("JSONB2")
    dateType = "java.time.LocalDate"
    dateTimeType = "java.time.ZonedDateTime"
    timeType = "java.time.LocalTime"
    includeHashcodeAndEquals = false
    includeToString = false
    initializeCollections = false
    includeAdditionalProperties = false
}

node {
    download.set(true)
    version.set("20.11.1")
    distBaseUrl.set("https://nodejs.org/dist")
    nodeProjectDir.set(file("$rootDir/src/main/app"))
    if (System.getenv("CI") != null) {
        npmInstallCommand.set("ci")
    } else {
        npmInstallCommand.set("install")
    }
}

smallryeOpenApi {
    infoTitle.set("Zaakafhandelcomponent backend API")
    schemaFilename.set("META-INF/openapi/openapi")
    operationIdStrategy.set(OpenApiConfig.OperationIdStrategy.METHOD)
    outputFileTypeFilter.set("YAML")
}

swaggerSources {
    register("zaakafhandelcomponent") {
        setInputFile(file("$rootDir/build/generated/openapi/META-INF/openapi/openapi.yaml"))
    }
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    format("misc") {
        target("*.gradle", ".gitattributes", ".gitignore", ".containerignore", ".dockerignore")

        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    java {
        targetExclude("src/generated/**", "build/generated/**")

        removeUnusedImports()
        importOrderFile("config/importOrder.txt")

        formatAnnotations()

        // Latest supported version:
        // https://github.com/diffplug/spotless/tree/main/lib-extra/src/main/resources/com/diffplug/spotless/extra/eclipse_wtp_formatter
        eclipse("4.21").configFile("config/zac.xml")

        licenseHeaderFile("config/licenseHeader.txt")
            .onlyIfContentMatches("FileCopyrightText: 2[0-9-]+ Lifely").updateYearWithLatest(true)
    }
    format("e2e") {
        target("src/e2e/**/*.js", "src/e2e/**/*.ts")
        targetExclude("src/e2e/node_modules/**")

        prettier(mapOf("prettier" to "3.2.5", "prettier-plugin-organize-imports" to "3.2.4"))
            .config(mapOf("parser" to "typescript", "plugins" to arrayOf("prettier-plugin-organize-imports")))
    }
    gherkin {
        target("src/e2e/**/*.feature")
        targetExclude("src/e2e/node_modules/**")

        gherkinUtils()
    }
    format("app") {
        target("src/main/app/**/*.js", "src/main/app/**/*.ts")
        targetExclude(
            "src/main/app/node_modules/**",
            "src/main/app/dist/**",
            "src/main/app/.angular/**"
        )
        targetExclude(
            "src/main/app/node_modules/**",
            "src/main/app/src/generated/**",
            "src/main/app/coverage/**",
            "src/main/app/dist/**",
            "src/main/app/.angular/**"
        )

        prettier(mapOf("prettier" to "3.2.5", "prettier-plugin-organize-imports" to "3.2.4"))
            .config(mapOf("parser" to "typescript", "plugins" to arrayOf("prettier-plugin-organize-imports")))
    }
    format("json") {
        target("src/**/*.json")
        targetExclude(
                "src/e2e/node_modules/**",
                "src/main/app/node_modules/**",
                "src/main/app/dist/**",
                "src/main/app/.angular/**",
                "src/**/package-lock.json",
                "src/main/app/coverage/**.json"
        )

        prettier(mapOf("prettier" to "3.2.5")).config(mapOf("parser" to "json"))
    }
}
tasks.getByName("spotlessApply").finalizedBy(listOf("detektApply"))

// run npm install task after generating the Java clients because they
// share the same output folder (= $rootDir)
tasks.getByName("npmInstall").setMustRunAfter(listOf("generateJavaClients"))
tasks.getByName("generateSwaggerUIZaakafhandelcomponent").setDependsOn(listOf("generateOpenApiSpec"))

tasks.getByName("compileItestKotlin") {
    dependsOn("copyJacocoAgentForItest")
    mustRunAfter("buildDockerImage")
}

tasks.war {
    dependsOn("npmRunBuild")

    // add built frontend resources to WAR archive
    from("src/main/app/dist/zaakafhandelcomponent")

    // explicitly add our 'warLib' 'transitive' dependencies that are required in the generated WAR
    classpath(files(configurations["warLib"]))
}

tasks {
    clean {
        dependsOn("mavenClean")

        delete("$rootDir/src/main/app/dist")
        delete("$rootDir/src/main/app/reports")
        delete("$rootDir/src/generated")
        delete("$rootDir/src/e2e/reports")
    }

    build {
        dependsOn("generateWildflyBootableJar")
    }

    test {
        dependsOn("npmRunTest")
    }

    compileJava {
        dependsOn("generateJavaClients")
    }

    jacocoTestReport {
        dependsOn(test)

        reports {
            xml.required = true
            html.required = false
        }
    }

    withType<JacocoReport> {
        // exclude Java client code that was auto generated at build time
        afterEvaluate {
            classDirectories.setFrom(
                classDirectories.files.map {
                    fileTree(it).matching {
                        exclude("**/generated/**")
                    }
                }
            )
        }
    }

    processResources {
        // exclude resources that we do not need in the build artefacts
        exclude("api-specs/**")
        exclude("wildfly/**")
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<Javadoc> {
        options.encoding = "UTF-8"
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask> {
        generatorName.set("java")
        outputDir.set("$rootDir/src/generated/java")
        generateApiTests.set(false)
        generateApiDocumentation.set(false)
        generateModelTests.set(false)
        generateModelDocumentation.set(false)
        globalProperties.set(
            mapOf(
                // generate model files only (note that an empty string indicates: generate all)
                "modelDocs" to "false",
                "apis" to "false",
                "models" to ""
            )
        )
        configOptions.set(
            mapOf(
                "library" to "microprofile",
                "microprofileRestClientVersion" to "3.0",
                "sourceFolder" to "",
                "dateLibrary" to "java8",
                "disallowAdditionalPropertiesIfNotPresent" to "false",
                "openApiNullable" to "false",
                "useJakartaEe" to "true"
            )
        )
        // Specify custom Mustache template dir as temporary workaround for the issue where OpenAPI Generator 7.2.0
        // fails to generate import statements for @JsonbCreator annotations.
        // Maybe this workaround can be removed when we migrate to OpenAPI Generator 7.3.0.
        templateDir.set("$rootDir/src/main/resources/openapi-generator-templates")
    }

    register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateKvkZoekenClient") {
        inputSpec.set("$rootDir/src/main/resources/api-specs/kvk/zoeken-openapi.yaml")
        modelPackage.set("net.atos.client.kvk.zoeken.model.generated")
    }

    register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateKvkBasisProfielClient") {
        inputSpec.set("$rootDir/src/main/resources/api-specs/kvk/basisprofiel-openapi.yaml")
        modelPackage.set("net.atos.client.kvk.basisprofiel.model.generated")
    }

    register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateKvkVestigingsProfielClient") {
        inputSpec.set("$rootDir/src/main/resources/api-specs/kvk/vestigingsprofiel-openapi.yaml")
        modelPackage.set("net.atos.client.kvk.vestigingsprofiel.model.generated")
    }

    register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateBrpClient") {
        inputSpec.set("$rootDir/src/main/resources/api-specs/brp/openapi.yaml")
        modelPackage.set("net.atos.client.brp.model.generated")
    }

    register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateVrlClient") {
        inputSpec.set("$rootDir/src/main/resources/api-specs/vrl/openapi.yaml")
        modelPackage.set("net.atos.client.vrl.model.generated")
    }

    register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateBagClient") {
        inputSpec.set("$rootDir/src/main/resources/api-specs/bag/openapi.yaml")
        modelPackage.set("net.atos.client.bag.model.generated")
        // we use a different date library for this client
        configOptions.set(
            mapOf(
                "library" to "microprofile",
                "microprofileRestClientVersion" to "3.0",
                "sourceFolder" to "",
                "dateLibrary" to "java8-localdatetime",
                "disallowAdditionalPropertiesIfNotPresent" to "false",
                "openApiNullable" to "false",
                "useJakartaEe" to "true"
            )
        )
    }

    register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateKlantenClient") {
        // this task was not enabled in the original Maven build either;
        // these model files were added to the code base manually instead
        isEnabled = false

        inputSpec.set("$rootDir/src/main/resources/api-specs/klanten/openapi.yaml")
        modelPackage.set("net.atos.client.klanten.model.generated")
    }

    register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateContactMomentenClient") {
        inputSpec.set("$rootDir/src/main/resources/api-specs/contactmomenten/openapi.yaml")
        modelPackage.set("net.atos.client.contactmomenten.model.generated")
    }

    register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateZgwBrcClient") {
        inputSpec.set("$rootDir/src/main/resources/api-specs/zgw/brc-openapi.yaml")
        modelPackage.set("net.atos.client.zgw.brc.model.generated")
    }

    register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateZgwDrcClient") {
        inputSpec.set("$rootDir/src/main/resources/api-specs/zgw/drc-openapi.yaml")
        modelPackage.set("net.atos.client.zgw.drc.model.generated")
    }

    register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateZrcDrcClient") {
        inputSpec.set("$rootDir/src/main/resources/api-specs/zgw/zrc-openapi.yaml")
        modelPackage.set("net.atos.client.zgw.zrc.model.generated")
    }

    register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateZtcDrcClient") {
        inputSpec.set("$rootDir/src/main/resources/api-specs/zgw/ztc-openapi.yaml")
        modelPackage.set("net.atos.client.zgw.ztc.model.generated")
    }

    register("generateJavaClients") {
        dependsOn(
            generateJsonSchema2Pojo,
            "generateKvkZoekenClient",
            "generateKvkBasisProfielClient",
            "generateKvkVestigingsProfielClient",
            "generateBrpClient",
            "generateVrlClient",
            "generateBagClient",
            "generateKlantenClient",
            "generateContactMomentenClient",
            "generateZgwBrcClient",
            "generateZgwDrcClient",
            "generateZrcDrcClient",
            "generateZtcDrcClient"
        )
    }

    register<NpmTask>("npmRunBuild") {
        dependsOn("npmInstall")
        dependsOn("generateOpenApiSpec")
        npmCommand.set(listOf("run", "build"))

        // avoid running this task when there are no changes in the input or output files
        // see: https://github.com/node-gradle/gradle-node-plugin/blob/master/docs/faq.md
        inputs.files(fileTree("src/main/app/node_modules"))
        inputs.files(fileTree("src/main/app/src"))
        inputs.file("src/main/app/package.json")
        inputs.file("src/main/app/package-lock.json")
        outputs.dir("src/main/app/dist/zaakafhandelcomponent")
    }

    register<NpmTask>("npmRunTest") {
        dependsOn("npmRunBuild")

        npmCommand.set(listOf("run", "test:report"))
        // avoid running this task when there are no changes in the input or output files
        // see: https://github.com/node-gradle/gradle-node-plugin/blob/master/docs/faq.md
        inputs.files(fileTree("src/main/app/node_modules"))
        inputs.files(fileTree("src/main/app/src"))
        inputs.file("src/main/app/package.json")
        inputs.file("src/main/app/package-lock.json")

        // the Jest junit reporter generates file: src/main/app/reports/report.xml
        outputs.dir("src/main/app/reports")
    }

    register<DockerBuildImage>("buildDockerImage") {
        dependsOn("generateWildflyBootableJar")

        inputDir.set(file("."))
        buildArgs.set(
            mapOf(
                "versionNumber" to versionNumber,
                "branchName" to branchName,
                "commitHash" to commitHash
            )
        )
        dockerFile.set(file("Dockerfile"))
        images.add(zacDockerImage)
    }

    register<Copy>("copyJacocoAgentForItest") {
        description = "Copies and renames the JaCoCo agent runtime JAR file for instrumentation during the integration tests"
        from(configurations.getByName("jacocoAgentJarForItest"))
        // simply rename the JaCoCo agent runtime JAR file name to strip away the version number
        rename {
            "org.jacoco.agent-runtime.jar"
        }
        into("$rootDir/build/jacoco/itest/jacoco-agent")
    }

    register<Test>("itest") {
        inputs.files(project.tasks.findByPath("compileItestKotlin")!!.outputs.files)

        testClassesDirs = sourceSets["itest"].output.classesDirs
        classpath = sourceSets["itest"].runtimeClasspath

        systemProperty("zacDockerImage", zacDockerImage)
    }

    register<JacocoReport>("jacocoIntegrationTestReport") {
        dependsOn("itest")

        description = "Generates code coverage report for the integration tests"
        executionData.setFrom("$rootDir/build/jacoco/itest/jacoco-report/jacoco-it.exec")
        // tell JaCoCo to report on our code base
        sourceSets(sourceSets["main"])
        reports {
            xml.required = true
            html.required = false
        }
    }

    register<Exec>("generateWildflyBootableJar") {
        dependsOn("war")
        if (System.getProperty("os.name").lowercase(Locale.ROOT).contains("windows")) {
            commandLine("./mvnw.cmd", "wildfly-jar:package")
        } else {
            commandLine("./mvnw", "wildfly-jar:package")
        }
    }

    register<Exec>("mavenClean") {
        if (System.getProperty("os.name").lowercase(Locale.ROOT).contains("windows")) {
            commandLine("./mvnw.cmd", "clean")
        } else {
            commandLine("./mvnw", "clean")
        }
    }
}
