# Web3 integration

This module is responsible for integrating with EVM-compatible blockchains using the JSON-RPC protocol.

## Dependencies

* [web3j](https://docs.web3j.io/)
* [starknet-jvm](https://github.com/software-mansion/starknet-jvm)

## Usage

### Generate java wrappers for smart contracts

In order to check for contract validity and to interact with the smart contracts, we need to generate Java wrappers for the smart contracts.

To do so, first install web3j cli tool following the instructions [here](https://docs.web3j.io/4.8.7/command_line_tools/).

Then download the smart contract ABI from etherscan and save it in the `src/main/resources/abi` directory.

Finally, run the following command:

```shell
web3j generate solidity -a src/main/resources/contract.abi -b src/main/resources/contract.bin -o src/main/java -p onlydust.com.marketplace.api.infura.contracts
```
