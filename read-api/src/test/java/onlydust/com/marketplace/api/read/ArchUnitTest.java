package onlydust.com.marketplace.api.read;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import jakarta.persistence.Entity;
import org.hibernate.annotations.Immutable;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchUnitTest {

    @Test
    void arch() {
        final var jc = new ClassFileImporter().importPackages("onlydust.com.marketplace");

        final var readWritePackagesRule = noClasses()
                .that().resideInAPackage("..marketplace.api.read.entities..")
                .should().dependOnClassesThat()
                .resideInAPackage("..postgres.entity.write..");

        final var noFinalEntityRule = noClasses()
                .that().areAnnotatedWith(Entity.class)
                .should().haveModifier(JavaModifier.FINAL);

        final var onlyImmutableReadEntities = classes()
                .that().resideInAPackage("..marketplace.api.read.entities..")
                .and().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith(Immutable.class);

        readWritePackagesRule.check(jc);
        noFinalEntityRule.check(jc);
        onlyImmutableReadEntities.check(jc);
    }
}
