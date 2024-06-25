package onlydust.com.marketplace.api.read;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import jakarta.persistence.Entity;
import org.hibernate.annotations.Immutable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchUnitTest {

    @Test
    void arch() {
        final var jc = new ClassFileImporter().importPackages("onlydust.com.marketplace");

        List.of(
                noClasses()
                        .that().resideInAPackage("..marketplace.api.read.entities..")
                        .should().dependOnClassesThat()
                        .resideInAPackage("..postgres.entity.write.."),
                noClasses()
                        .that().areAnnotatedWith(Entity.class)
                        .should().haveModifier(JavaModifier.FINAL),
                classes()
                        .that().areAnnotatedWith(Entity.class)
                        .should().haveSimpleNameEndingWith("Entity"),
                classes()
                        .that().resideInAPackage("..marketplace.api.read.entities..")
                        .and().areAnnotatedWith(Entity.class)
                        .should().beAnnotatedWith(Immutable.class),
                classes()
                        .that().haveSimpleNameEndingWith("ViewEntity")
                        .and().areAnnotatedWith(Entity.class)
                        .should().beAnnotatedWith(Immutable.class),
                classes()
                        .that().haveSimpleNameEndingWith("QueryEntity")
                        .and().areAnnotatedWith(Entity.class)
                        .should().beAnnotatedWith(Immutable.class)
        ).forEach(rule -> rule.check(jc));
    }
}
