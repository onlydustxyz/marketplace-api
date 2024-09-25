package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.CountryReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface CountryReadRepository extends Repository<CountryReadEntity, String> {
    @Query(value = """
            SELECT DISTINCT kyc.country as iso3
            FROM accounting.kyc
            WHERE kyc.country IS NOT NULL
            ORDER BY kyc.country
            """, nativeQuery = true)
    List<CountryReadEntity> findAllContributorCountries();
}
