package onlydust.com.marketplace.api.postgres.adapter;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchUnitTest {

    @Test
    void arch() {
        final var jc = new ClassFileImporter().importPackages("onlydust.com.marketplace.api.postgres");

        final var noWriteEntityInReadEntityRule = noClasses()
                .that().resideInAPackage("..entity.read..")
                .should().dependOnClassesThat()
                .resideInAPackage("..entity.write..");

        noWriteEntityInReadEntityRule.check(jc);
    }
}
