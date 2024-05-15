package onlydust.com.marketplace.api.postgres.adapter;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.elements.GivenClassesConjunction;
import jakarta.persistence.Entity;
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

        final var writeEntityNaming = noWriteEntityClasses()
                .should().haveSimpleNameEndingWith("ViewEntity")
                .orShould().haveSimpleNameEndingWith("QueryEntity");


        readWritePackagesRule.check(jc);
        readEntityNaming.check(jc);
        readEntitiesAreImmutable.check(jc);
        writeEntityNaming.check(jc);
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
}
