package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Storage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ERC20Entity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ERC20Repository;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.util.List;

@AllArgsConstructor
public class PostgresERC20Adapter implements ERC20Storage {
    private final ERC20Repository repository;

    @Override
    public void save(ERC20 erc20) {
        repository.save(ERC20Entity.of(erc20));
    }

    @Override
    public Boolean exists(Blockchain blockchain, ContractAddress address) {
        return repository.existsById(ERC20Entity.PrimaryKey.of(blockchain, address));
    }

    @Override
    public List<ERC20> all() {
        return repository.findAll().stream().map(ERC20Entity::toDomain).toList();
    }
}
