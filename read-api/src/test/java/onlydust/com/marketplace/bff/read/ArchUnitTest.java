package onlydust.com.marketplace.bff.read;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchUnitTest {

    @Test
    void arch() {
        final var jc = new ClassFileImporter().importPackages("onlydust.com.marketplace");

        final var readWritePackagesRule = noClasses()
                .that().resideInAPackage("..bff.read.entities..")
                .should().dependOnClassesThat()
                .resideInAPackage("..postgres.entity.write..");

        readWritePackagesRule.check(jc);
    }
}
