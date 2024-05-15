package onlydust.com.marketplace.api.postgres.adapter;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.elements.GivenClassesConjunction;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchUnitTest {

    @Test
    void arch() {
        final var jc = new ClassFileImporter().importPackages("onlydust.com.marketplace.api.postgres");

        final var readWritePackagesRule = noClasses()
                .that().resideInAPackage("..entity.read..")
                .should().dependOnClassesThat()
                .resideInAPackage("..entity.write..");

        final var readEntityNaming = readEntityClasses()
                .should().haveSimpleNameEndingWith("ViewEntity")
                .orShould().haveSimpleNameEndingWith("QueryEntity");

        final var readEntitiesAreImmutable = readEntityClasses()
                .should().beAnnotatedWith(Immutable.class);

        final var viewEntitiesAreLinkedToTables = readEntityClasses()
                .and().haveSimpleNameEndingWith("ViewEntity")
                .should().beAnnotatedWith(Table.class);

        final var queryEntitiesAreNotLinkedToTables = noQueryEntityClasses()
                .should().beAnnotatedWith(Table.class)
                .orShould().beAnnotatedWith(org.hibernate.annotations.Table.class);

        final var queryEntitiesAreNotUsedInRelationships = noReadEntityClasses()
                .should().dependOnClassesThat()
                .haveSimpleNameEndingWith("QueryEntity");

        final var queryEntitiesAreNotUsedInWriteEntities = noWriteEntityClasses()
                .should().dependOnClassesThat()
                .haveSimpleNameEndingWith("QueryEntity");

        final var writeEntityNaming = noWriteEntityClasses()
                .should().haveSimpleNameEndingWith("ViewEntity")
                .orShould().haveSimpleNameEndingWith("QueryEntity");


        readWritePackagesRule.check(jc);
        readEntityNaming.check(jc);
        readEntitiesAreImmutable.check(jc);
        viewEntitiesAreLinkedToTables.check(jc);
        writeEntityNaming.check(jc);
        queryEntitiesAreNotLinkedToTables.check(jc);
        queryEntitiesAreNotUsedInRelationships.check(jc);
        queryEntitiesAreNotUsedInWriteEntities.check(jc);
    }

    private static GivenClassesConjunction noQueryEntityClasses() {
        return noClasses()
                .that().resideInAPackage("..entity.read..")
                .and().areAnnotatedWith(Entity.class)
                .and().haveSimpleNameEndingWith("QueryEntity");
    }

    private static GivenClassesConjunction noWriteEntityClasses() {
        return noClasses()
                .that().resideInAPackage("..entity.write..")
                .and().areAnnotatedWith(Entity.class);
    }

    private static GivenClassesConjunction readEntityClasses() {
        return classes()
                .that().resideInAPackage("..entity.read..")
                .and().areAnnotatedWith(Entity.class);
    }

    private static GivenClassesConjunction noReadEntityClasses() {
        return noClasses()
                .that().resideInAPackage("..entity.read..")
                .and().areAnnotatedWith(Entity.class);
    }
}
