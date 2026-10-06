package com.andresyfr.franchise.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.andresyfr.franchise",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class CleanArchitectureTest {

    @ArchTest
    static final ArchRule domainMustNotDependOnOuterLayers = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application..", "..infrastructure..", "org.springframework..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule applicationMustNotDependOnInfrastructure = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule restEntrypointsMustNotAccessPersistence = noClasses()
            .that().resideInAPackage("..infrastructure.entrypoint..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure.persistence..")
            .allowEmptyShould(true);
}