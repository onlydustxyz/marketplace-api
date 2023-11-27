package onlydust.com.marketplace.api.bootstrap.it;

import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAppInstallationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAuthorizedRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubAppInstallationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubAuthorizedRepoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;

@ActiveProfiles("hasura_auth")
public class MeGetGithubOrganizationsApiIT extends AbstractMarketplaceApiIT {
    private static final String ONLYDUST_ACCOUNT_JSON = """
            {
              "id": 44300036,
              "organization": {
                "id": 98735558,
                "login": "onlydustxyz",
                "avatarUrl": "https://avatars.githubusercontent.com/u/98735558?v=4",
                "htmlUrl": "https://github.com/onlydustxyz",
                "name": "OnlyDust",
                "repos": [
                  {
                    "id": 457709036,
                    "owner": "onlydustxyz",
                    "name": "draconis",
                    "description": "The best ally to your Web3 contributor journey. Become a DeWorker now.",
                    "htmlUrl": "https://github.com/onlydustxyz/draconis",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 460925920,
                    "owner": "onlydustxyz",
                    "name": "metadata-cost-exploration",
                    "description": "Experiments about metadata management for bounty operations",
                    "htmlUrl": "https://github.com/onlydustxyz/metadata-cost-exploration",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 463078636,
                    "owner": "onlydustxyz",
                    "name": "template-rust-project",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/template-rust-project",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 463453877,
                    "owner": "onlydustxyz",
                    "name": "dev-misc",
                    "description": "Dev Miscellaneous",
                    "htmlUrl": "https://github.com/onlydustxyz/dev-misc",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 465363775,
                    "owner": "onlydustxyz",
                    "name": "draconis-ui",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/draconis-ui",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 465778306,
                    "owner": "onlydustxyz",
                    "name": "ethereum-event-fetcher",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/ethereum-event-fetcher",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 472337439,
                    "owner": "onlydustxyz",
                    "name": "the-merge-nft-frontend",
                    "description": "TheMerge NFT Claim UI",
                    "htmlUrl": "https://github.com/onlydustxyz/the-merge-nft-frontend",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 478066258,
                    "owner": "onlydustxyz",
                    "name": "starknet-react",
                    "description": "A collection of React providers and hooks for StarkNet",
                    "htmlUrl": "https://github.com/onlydustxyz/starknet-react",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 478070593,
                    "owner": "onlydustxyz",
                    "name": "startknet-101-solver",
                    "description": "Testing the frontend interaction with Starknet, solving the https://github.com/l-henri/starknet-cairo-101 challenges automatically",
                    "htmlUrl": "https://github.com/onlydustxyz/startknet-101-solver",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 478148207,
                    "owner": "onlydustxyz",
                    "name": "starknet-modular-arch",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/starknet-modular-arch",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 479033681,
                    "owner": "onlydustxyz",
                    "name": "draconis-starknet",
                    "description": "The best ally to your Web3 contributor journey. Become a DeWorker now.",
                    "htmlUrl": "https://github.com/onlydustxyz/draconis-starknet",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 480281806,
                    "owner": "onlydustxyz",
                    "name": "cairo-contracts",
                    "description": "OpenZeppelin Contracts written in Cairo for StarkNet, a decentralized ZK Rollup",
                    "htmlUrl": "https://github.com/onlydustxyz/cairo-contracts",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 480776993,
                    "owner": "onlydustxyz",
                    "name": "starklings",
                    "description": "An interactive tutorial to get you up and running with Starknet",
                    "htmlUrl": "https://github.com/onlydustxyz/starklings",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 481932781,
                    "owner": "onlydustxyz",
                    "name": "starkonquest",
                    "description": "An educational game to learn Cairo where you implement ship AIs that fight to catch as much dust as possible!",
                    "htmlUrl": "https://github.com/onlydustxyz/starkonquest",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 483330552,
                    "owner": "onlydustxyz",
                    "name": "starknet-onboarding-ui",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/starknet-onboarding-ui",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 484056846,
                    "owner": "onlydustxyz",
                    "name": "starkonquest-ui",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/starkonquest-ui",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 486660536,
                    "owner": "onlydustxyz",
                    "name": "generator-starknet",
                    "description": "This is a development platform to quickly generate, develop & deploy smart contract based applications on StarkNet.",
                    "htmlUrl": "https://github.com/onlydustxyz/generator-starknet",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 490231565,
                    "owner": "onlydustxyz",
                    "name": "github-contribution-labels",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/github-contribution-labels",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 490985437,
                    "owner": "onlydustxyz",
                    "name": "protostar-vs-nile",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/protostar-vs-nile",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 491431479,
                    "owner": "onlydustxyz",
                    "name": "uraeus",
                    "description": "Command line utilities to check StarkNet contracts written in Cairo.",
                    "htmlUrl": "https://github.com/onlydustxyz/uraeus",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 492805046,
                    "owner": "onlydustxyz",
                    "name": "development-guidelines",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/development-guidelines",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 493552406,
                    "owner": "onlydustxyz",
                    "name": "protostar-vscode-test-adapter",
                    "description": "vscode extension to view protostar tests in the Test Explorer.",
                    "htmlUrl": "https://github.com/onlydustxyz/protostar-vscode-test-adapter",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 493584169,
                    "owner": "onlydustxyz",
                    "name": "project-kickstarter",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/project-kickstarter",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 493591124,
                    "owner": "onlydustxyz",
                    "name": "kaaper",
                    "description": "Documentation generator for Cairo projects.",
                    "htmlUrl": "https://github.com/onlydustxyz/kaaper",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 493602649,
                    "owner": "onlydustxyz",
                    "name": "serpopard",
                    "description": "Code coverage tool for Cairo contracts.",
                    "htmlUrl": "https://github.com/onlydustxyz/serpopard",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 493605381,
                    "owner": "onlydustxyz",
                    "name": "heka",
                    "description": "Heka is a tool measuring the Contribution friendliness of a Github repository.",
                    "htmlUrl": "https://github.com/onlydustxyz/heka",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 493795808,
                    "owner": "onlydustxyz",
                    "name": "cairo-streams",
                    "description": "Array stream library written in pure Cairo",
                    "htmlUrl": "https://github.com/onlydustxyz/cairo-streams",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 494087007,
                    "owner": "onlydustxyz",
                    "name": "devops",
                    "description": "Repo of Infra as Code & tooling Devops",
                    "htmlUrl": "https://github.com/onlydustxyz/devops",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 494452167,
                    "owner": "onlydustxyz",
                    "name": "heka-rest-api",
                    "description": "Heka backend REST API",
                    "htmlUrl": "https://github.com/onlydustxyz/heka-rest-api",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 499061005,
                    "owner": "onlydustxyz",
                    "name": "marketplace-starknet",
                    "description": "Death Note starknet contracts to manage contribution badges",
                    "htmlUrl": "https://github.com/onlydustxyz/marketplace-starknet",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 499502122,
                    "owner": "onlydustxyz",
                    "name": "marketplace-signup",
                    "description": "Handles GitHub users signup / profile creation",
                    "htmlUrl": "https://github.com/onlydustxyz/marketplace-signup",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 501233690,
                    "owner": "onlydustxyz",
                    "name": "marketplace-frontend-old",
                    "description": "OnlyDust contribution marketplace frontend",
                    "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend-old",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 505834957,
                    "owner": "onlydustxyz",
                    "name": "onlydust-rs",
                    "description": "Only Dust Rust librairies",
                    "htmlUrl": "https://github.com/onlydustxyz/onlydust-rs",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 505906397,
                    "owner": "onlydustxyz",
                    "name": "starklings-exercises-validator",
                    "description": "A Python microservice to validate starklings exercises",
                    "htmlUrl": "https://github.com/onlydustxyz/starklings-exercises-validator",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 505906938,
                    "owner": "onlydustxyz",
                    "name": "starklings-backend",
                    "description": "A miscroservice to mint staklings users badges",
                    "htmlUrl": "https://github.com/onlydustxyz/starklings-backend",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 507994565,
                    "owner": "onlydustxyz",
                    "name": "starklings-diploma",
                    "description": "Starklings certification Soulbound NFT",
                    "htmlUrl": "https://github.com/onlydustxyz/starklings-diploma",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 508219470,
                    "owner": "onlydustxyz",
                    "name": "cicd",
                    "description": "Shared GitHub workflows",
                    "htmlUrl": "https://github.com/onlydustxyz/cicd",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 510308165,
                    "owner": "onlydustxyz",
                    "name": "starknet-node",
                    "description": "Script to create a starknet node",
                    "htmlUrl": "https://github.com/onlydustxyz/starknet-node",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 510654652,
                    "owner": "onlydustxyz",
                    "name": "contributions-platform-e2e-test",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/contributions-platform-e2e-test",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 511478575,
                    "owner": "onlydustxyz",
                    "name": "starknet-accounts",
                    "description": "Provides custom implementations of StarkNet accounts",
                    "htmlUrl": "https://github.com/onlydustxyz/starknet-accounts",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 523762227,
                    "owner": "onlydustxyz",
                    "name": "imhotep",
                    "description": "Cairo EVM bytecode interpreter",
                    "htmlUrl": "https://github.com/onlydustxyz/imhotep",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 530493897,
                    "owner": "onlydustxyz",
                    "name": "apibara",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/apibara",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 533216561,
                    "owner": "onlydustxyz",
                    "name": "backoffice-wallet-link",
                    "description": "Wallet link for Retool, used in the lead contributor back-office",
                    "htmlUrl": "https://github.com/onlydustxyz/backoffice-wallet-link",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 540776771,
                    "owner": "onlydustxyz",
                    "name": "heroku-buildpack-rust",
                    "description": "A buildpack for Rust applications on Heroku, with full support for Rustup, cargo and build caching.",
                    "htmlUrl": "https://github.com/onlydustxyz/heroku-buildpack-rust",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 557956292,
                    "owner": "onlydustxyz",
                    "name": "pathfinder",
                    "description": "A Starknet full node written in Rust",
                    "htmlUrl": "https://github.com/onlydustxyz/pathfinder",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 559561894,
                    "owner": "onlydustxyz",
                    "name": "cairo-foundry-demo",
                    "description": "Demo project for the cairo-foundry test runner",
                    "htmlUrl": "https://github.com/onlydustxyz/cairo-foundry-demo",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 560488797,
                    "owner": "onlydustxyz",
                    "name": "ctf",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/ctf",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 560545617,
                    "owner": "onlydustxyz",
                    "name": "account-obstr",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/account-obstr",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 563958883,
                    "owner": "onlydustxyz",
                    "name": "update-hasura-metadata-buildpack",
                    "description": "An heroku buildpack to deploy hasura on target environment",
                    "htmlUrl": "https://github.com/onlydustxyz/update-hasura-metadata-buildpack",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 564249238,
                    "owner": "onlydustxyz",
                    "name": "marketplace-frontend-v2",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend-v2",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 566371874,
                    "owner": "onlydustxyz",
                    "name": "hasura-auth",
                    "description": "Authentication for Hasura.",
                    "htmlUrl": "https://github.com/onlydustxyz/hasura-auth",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 566840633,
                    "owner": "onlydustxyz",
                    "name": "graphql-engine-heroku",
                    "description": "Blazing fast, instant realtime GraphQL APIs on Postgres with fine grained access control, also trigger webhooks on database events.",
                    "htmlUrl": "https://github.com/onlydustxyz/graphql-engine-heroku",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 572060328,
                    "owner": "onlydustxyz",
                    "name": "google-scripts",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/google-scripts",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 593218280,
                    "owner": "onlydustxyz",
                    "name": "octocrab",
                    "description": "A modern, extensible GitHub API Client for Rust.",
                    "htmlUrl": "https://github.com/onlydustxyz/octocrab",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 593701982,
                    "owner": "onlydustxyz",
                    "name": "gateway",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/gateway",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 616908135,
                    "owner": "onlydustxyz",
                    "name": "checkers-tendermint",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/checkers-tendermint",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 618506515,
                    "owner": "onlydustxyz",
                    "name": "wait-on-action",
                    "description": "A GitHub Action variant of the wait-on package (npmjs.com/package/wait-on)",
                    "htmlUrl": "https://github.com/onlydustxyz/wait-on-action",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 652694393,
                    "owner": "onlydustxyz",
                    "name": "juniper",
                    "description": "GraphQL server library for Rust",
                    "htmlUrl": "https://github.com/onlydustxyz/juniper",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 653632145,
                    "owner": "onlydustxyz",
                    "name": "github-cache",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/github-cache",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 655629139,
                    "owner": "onlydustxyz",
                    "name": "marketplace-provisionning",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/marketplace-provisionning",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 661599656,
                    "owner": "onlydustxyz",
                    "name": "madara-test",
                    "description": "\\uD83E\\uDD77\\uD83E\\uDE78 Madara is a ⚡ blazing fast ⚡ Starknet sequencer, based on substrate, powered by Rust \\uD83E\\uDD80",
                    "htmlUrl": "https://github.com/onlydustxyz/madara-test",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 661662838,
                    "owner": "onlydustxyz",
                    "name": "graphql-engine",
                    "description": "Blazing fast, instant realtime GraphQL APIs on your DB with fine grained access control, also trigger webhooks on database events.",
                    "htmlUrl": "https://github.com/onlydustxyz/graphql-engine",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 663102799,
                    "owner": "onlydustxyz",
                    "name": "od-rust-template",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/od-rust-template",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 672836289,
                    "owner": "onlydustxyz",
                    "name": "aws-infra-test",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/aws-infra-test",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 682131651,
                    "owner": "onlydustxyz",
                    "name": "metadata-image-generator",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/metadata-image-generator",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 688074915,
                    "owner": "onlydustxyz",
                    "name": "heroku-buildpack-rust-monorepo",
                    "description": "A buildpack for Rust applications on Heroku, with full support for Rustup, cargo and build caching.",
                    "htmlUrl": "https://github.com/onlydustxyz/heroku-buildpack-rust-monorepo",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 693559321,
                    "owner": "onlydustxyz",
                    "name": "cmc",
                    "description": "Library for CoinMarketCap API",
                    "htmlUrl": "https://github.com/onlydustxyz/cmc",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 695033641,
                    "owner": "onlydustxyz",
                    "name": "marketplace-backend",
                    "description": "Contributions marketplace backend services",
                    "htmlUrl": "https://github.com/onlydustxyz/marketplace-backend",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 698096830,
                    "owner": "onlydustxyz",
                    "name": "marketplace-api",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/marketplace-api",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 699283256,
                    "owner": "onlydustxyz",
                    "name": "marketplace-indexer",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/marketplace-indexer",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  },
                  {
                    "id": 703009655,
                    "owner": "onlydustxyz",
                    "name": "hackathon-frontend",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/hackathon-frontend",
                    "stars": null,
                    "forkCount": null,
                    "hasIssues": null,
                    "isIncludedInProject": null,
                    "isAuthorizedInGithubApp": null
                  }
                ],
                "installed": true,
                "isCurrentUserAdmin": null,
                "isPersonal": null,
                "installationId": null
              }
            }
                        
            """;

    @Autowired
    GithubAppInstallationRepository githubAppInstallationRepository;
    @Autowired
    HasuraUserHelper hasuraUserHelper;
    @Autowired
    GithubAuthorizedRepoRepository githubAuthorizedRepoRepository;

    @Test
    void should_get_github_account_from_installation_id() {
        final String jwt = hasuraUserHelper.authenticatePierre().jwt();
        client.get()
                .uri(getApiURI(GITHUB_INSTALLATIONS_GET + "/44300036"))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(ONLYDUST_ACCOUNT_JSON);
    }

    @Test
    void should_return_404_when_not_found() {
        final String jwt = hasuraUserHelper.authenticatePierre().jwt();
        client.get()
                .uri(getApiURI(GITHUB_INSTALLATIONS_GET + "/0"))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Installation 0 not found");
    }

    @Test
    void should_return_user_organizations() {
        // Given
        final GithubAppInstallationEntity githubAppInstallationEntity = new GithubAppInstallationEntity();
        githubAppInstallationEntity.setId(123456L);
        githubAppInstallationRepository.delete(githubAppInstallationEntity);
        final String githubPAT = faker.rickAndMorty().character() + faker.random().nextLong();
        final HasuraUserHelper.AuthenticatedUser pierre = hasuraUserHelper.authenticatePierre(githubPAT);
        final String jwt = pierre.jwt();
        githubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/user/orgs?per_page=100&page=1"))
                .withHeader("Authorization", WireMock.equalTo("Bearer " + githubPAT))
                .willReturn(okJson("""
                                [
                                    {
                                        "login": "Barbicane-fr",
                                        "id": 58205251,
                                        "node_id": "MDEyOk9yZ2FuaXphdGlvbjU4MjA1MjUx",
                                        "url": "https://api.github.com/orgs/Barbicane-fr",
                                        "repos_url": "https://api.github.com/orgs/Barbicane-fr/repos",
                                        "events_url": "https://api.github.com/orgs/Barbicane-fr/events",
                                        "hooks_url": "https://api.github.com/orgs/Barbicane-fr/hooks",
                                        "issues_url": "https://api.github.com/orgs/Barbicane-fr/issues",
                                        "members_url": "https://api.github.com/orgs/Barbicane-fr/members{/member}",
                                        "public_members_url": "https://api.github.com/orgs/Barbicane-fr/public_members{/member}",
                                        "avatar_url": "https://avatars.githubusercontent.com/u/58205251?v=4",
                                        "description": ""
                                    },
                                    {
                                        "login": "onlydustxyz",
                                        "id": 98735558,
                                        "node_id": "O_kgDOBeKVxg",
                                        "url": "https://api.github.com/orgs/onlydustxyz",
                                        "repos_url": "https://api.github.com/orgs/onlydustxyz/repos",
                                        "events_url": "https://api.github.com/orgs/onlydustxyz/events",
                                        "hooks_url": "https://api.github.com/orgs/onlydustxyz/hooks",
                                        "issues_url": "https://api.github.com/orgs/onlydustxyz/issues",
                                        "members_url": "https://api.github.com/orgs/onlydustxyz/members{/member}",
                                        "public_members_url": "https://api.github.com/orgs/onlydustxyz/public_members{/member}",
                                        "avatar_url": "https://avatars.githubusercontent.com/u/98735558?v=4",
                                        "description": ""
                                    },
                                    {
                                        "login": "symeo-io",
                                        "id": 105865802,
                                        "node_id": "O_kgDOBk9iSg",
                                        "url": "https://api.github.com/orgs/symeo-io",
                                        "repos_url": "https://api.github.com/orgs/symeo-io/repos",
                                        "events_url": "https://api.github.com/orgs/symeo-io/events",
                                        "hooks_url": "https://api.github.com/orgs/symeo-io/hooks",
                                        "issues_url": "https://api.github.com/orgs/symeo-io/issues",
                                        "members_url": "https://api.github.com/orgs/symeo-io/members{/member}",
                                        "public_members_url": "https://api.github.com/orgs/symeo-io/public_members{/member}",
                                        "avatar_url": "https://avatars.githubusercontent.com/u/105865802?v=4",
                                        "description": ""
                                    }
                                ]
                        """)));
        githubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/user/orgs?per_page=100&page=1"))
                .withHeader("Authorization", WireMock.equalTo("Bearer " + githubPAT))
                .willReturn(okJson("""
                                [
                                    {
                                        "login": "onlydustxyz",
                                        "id": 98735558,
                                        "node_id": "O_kgDOBeKVxg",
                                        "url": "https://api.github.com/orgs/onlydustxyz",
                                        "repos_url": "https://api.github.com/orgs/onlydustxyz/repos",
                                        "events_url": "https://api.github.com/orgs/onlydustxyz/events",
                                        "hooks_url": "https://api.github.com/orgs/onlydustxyz/hooks",
                                        "issues_url": "https://api.github.com/orgs/onlydustxyz/issues",
                                        "members_url": "https://api.github.com/orgs/onlydustxyz/members{/member}",
                                        "public_members_url": "https://api.github.com/orgs/onlydustxyz/public_members{/member}",
                                        "avatar_url": "https://avatars.githubusercontent.com/u/98735558?v=4",
                                        "description": ""
                                    },
                                    {
                                        "login": "symeo-io",
                                        "id": 105865802,
                                        "node_id": "O_kgDOBk9iSg",
                                        "url": "https://api.github.com/orgs/symeo-io",
                                        "repos_url": "https://api.github.com/orgs/symeo-io/repos",
                                        "events_url": "https://api.github.com/orgs/symeo-io/events",
                                        "hooks_url": "https://api.github.com/orgs/symeo-io/hooks",
                                        "issues_url": "https://api.github.com/orgs/symeo-io/issues",
                                        "members_url": "https://api.github.com/orgs/symeo-io/members{/member}",
                                        "public_members_url": "https://api.github.com/orgs/symeo-io/public_members{/member}",
                                        "avatar_url": "https://avatars.githubusercontent.com/u/105865802?v=4",
                                        "description": ""
                                    }
                                ]
                        """)));
        githubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(String.format("/orgs/%s/memberships/%s", "symeo" +
                                                                                                               "-io",
                        "PierreOucif")))
                .withHeader("Authorization", WireMock.equalTo("Bearer " + githubPAT))
                .willReturn(okJson("""
                                {
                                    "url": "https://api.github.com/orgs/symeo-io/memberships/PierreOucif",
                                    "state": "active",
                                    "role": "admin",
                                    "organization_url": "https://api.github.com/orgs/symeo-io",
                                    "user": {
                                        "login": "PierreOucif",
                                        "id": 16590657,
                                        "node_id": "MDQ6VXNlcjE2NTkwNjU3",
                                        "avatar_url": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                        "gravatar_id": "",
                                        "url": "https://api.github.com/users/PierreOucif",
                                        "html_url": "https://github.com/PierreOucif",
                                        "followers_url": "https://api.github.com/users/PierreOucif/followers",
                                        "following_url": "https://api.github.com/users/PierreOucif/following{/other_user}",
                                        "gists_url": "https://api.github.com/users/PierreOucif/gists{/gist_id}",
                                        "starred_url": "https://api.github.com/users/PierreOucif/starred{/owner}{/repo}",
                                        "subscriptions_url": "https://api.github.com/users/PierreOucif/subscriptions",
                                        "organizations_url": "https://api.github.com/users/PierreOucif/orgs",
                                        "repos_url": "https://api.github.com/users/PierreOucif/repos",
                                        "events_url": "https://api.github.com/users/PierreOucif/events{/privacy}",
                                        "received_events_url": "https://api.github.com/users/PierreOucif/received_events",
                                        "type": "User",
                                        "site_admin": false
                                    },
                                    "organization": {
                                        "login": "symeo-io",
                                        "id": 105865802,
                                        "node_id": "O_kgDOBk9iSg",
                                        "url": "https://api.github.com/orgs/symeo-io",
                                        "repos_url": "https://api.github.com/orgs/symeo-io/repos",
                                        "events_url": "https://api.github.com/orgs/symeo-io/events",
                                        "hooks_url": "https://api.github.com/orgs/symeo-io/hooks",
                                        "issues_url": "https://api.github.com/orgs/symeo-io/issues",
                                        "members_url": "https://api.github.com/orgs/symeo-io/members{/member}",
                                        "public_members_url": "https://api.github.com/orgs/symeo-io/public_members{/member}",
                                        "avatar_url": "https://avatars.githubusercontent.com/u/105865802?v=4",
                                        "description": ""
                                    }
                                }
                        """)));
        githubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(String.format("/orgs/%s/memberships/%s",
                        "onlydustxyz", "PierreOucif")))
                .withHeader("Authorization", WireMock.equalTo("Bearer " + githubPAT))
                .willReturn(okJson("""
                                {
                                     "url": "https://api.github.com/orgs/onlydustxyz/memberships/PierreOucif",
                                     "state": "active",
                                     "role": "member",
                                     "organization_url": "https://api.github.com/orgs/onlydustxyz",
                                     "user": {
                                         "login": "PierreOucif",
                                         "id": 16590657,
                                         "node_id": "MDQ6VXNlcjE2NTkwNjU3",
                                         "avatar_url": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                         "gravatar_id": "",
                                         "url": "https://api.github.com/users/PierreOucif",
                                         "html_url": "https://github.com/PierreOucif",
                                         "followers_url": "https://api.github.com/users/PierreOucif/followers",
                                         "following_url": "https://api.github.com/users/PierreOucif/following{/other_user}",
                                         "gists_url": "https://api.github.com/users/PierreOucif/gists{/gist_id}",
                                         "starred_url": "https://api.github.com/users/PierreOucif/starred{/owner}{/repo}",
                                         "subscriptions_url": "https://api.github.com/users/PierreOucif/subscriptions",
                                         "organizations_url": "https://api.github.com/users/PierreOucif/orgs",
                                         "repos_url": "https://api.github.com/users/PierreOucif/repos",
                                         "events_url": "https://api.github.com/users/PierreOucif/events{/privacy}",
                                         "received_events_url": "https://api.github.com/users/PierreOucif/received_events",
                                         "type": "User",
                                         "site_admin": false
                                     },
                                     "organization": {
                                         "login": "onlydustxyz",
                                         "id": 98735558,
                                         "node_id": "O_kgDOBeKVxg",
                                         "url": "https://api.github.com/orgs/onlydustxyz",
                                         "repos_url": "https://api.github.com/orgs/onlydustxyz/repos",
                                         "events_url": "https://api.github.com/orgs/onlydustxyz/events",
                                         "hooks_url": "https://api.github.com/orgs/onlydustxyz/hooks",
                                         "issues_url": "https://api.github.com/orgs/onlydustxyz/issues",
                                         "members_url": "https://api.github.com/orgs/onlydustxyz/members{/member}",
                                         "public_members_url": "https://api.github.com/orgs/onlydustxyz/public_members{/member}",
                                         "avatar_url": "https://avatars.githubusercontent.com/u/98735558?v=4",
                                         "description": ""
                                     }
                                 }
                        """)));


        githubAuthorizedRepoRepository.deleteAll(List.of(
                new GithubAuthorizedRepoEntity(GithubAuthorizedRepoEntity.Id.builder()
                        .installationId(44300036L)
                        .repoId(498695724L)
                        .build()),
                new GithubAuthorizedRepoEntity(GithubAuthorizedRepoEntity.Id.builder()
                        .installationId(44300036L)
                        .repoId(470103674L)
                        .build()),
                new GithubAuthorizedRepoEntity(GithubAuthorizedRepoEntity.Id.builder()
                        .installationId(44300036L)
                        .repoId(470901963L)
                        .build()),
                new GithubAuthorizedRepoEntity(GithubAuthorizedRepoEntity.Id.builder()
                        .installationId(44300036L)
                        .repoId(471286458L)
                        .build()),
                new GithubAuthorizedRepoEntity(GithubAuthorizedRepoEntity.Id.builder()
                        .installationId(44300036L)
                        .repoId(476359452L)
                        .build())
        ));

        // When
        client.get()
                .uri(getApiURI(ME_GET_ORGANIZATIONS))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        [
                          {
                            "id": 98735558,
                            "login": "onlydustxyz",
                            "avatarUrl": "https://avatars.githubusercontent.com/u/98735558?v=4",
                            "htmlUrl": "https://github.com/onlydustxyz",
                            "name": "OnlyDust",
                            "repos": [
                              {
                                "id": 457709036,
                                "owner": "onlydustxyz",
                                "name": "draconis",
                                "description": "The best ally to your Web3 contributor journey. Become a DeWorker now.",
                                "htmlUrl": "https://github.com/onlydustxyz/draconis",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 460925920,
                                "owner": "onlydustxyz",
                                "name": "metadata-cost-exploration",
                                "description": "Experiments about metadata management for bounty operations",
                                "htmlUrl": "https://github.com/onlydustxyz/metadata-cost-exploration",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 463078636,
                                "owner": "onlydustxyz",
                                "name": "template-rust-project",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/template-rust-project",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 463453877,
                                "owner": "onlydustxyz",
                                "name": "dev-misc",
                                "description": "Dev Miscellaneous",
                                "htmlUrl": "https://github.com/onlydustxyz/dev-misc",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 465363775,
                                "owner": "onlydustxyz",
                                "name": "draconis-ui",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/draconis-ui",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 465778306,
                                "owner": "onlydustxyz",
                                "name": "ethereum-event-fetcher",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/ethereum-event-fetcher",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 472337439,
                                "owner": "onlydustxyz",
                                "name": "the-merge-nft-frontend",
                                "description": "TheMerge NFT Claim UI",
                                "htmlUrl": "https://github.com/onlydustxyz/the-merge-nft-frontend",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 478066258,
                                "owner": "onlydustxyz",
                                "name": "starknet-react",
                                "description": "A collection of React providers and hooks for StarkNet",
                                "htmlUrl": "https://github.com/onlydustxyz/starknet-react",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 478070593,
                                "owner": "onlydustxyz",
                                "name": "startknet-101-solver",
                                "description": "Testing the frontend interaction with Starknet, solving the https://github.com/l-henri/starknet-cairo-101 challenges automatically",
                                "htmlUrl": "https://github.com/onlydustxyz/startknet-101-solver",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 478148207,
                                "owner": "onlydustxyz",
                                "name": "starknet-modular-arch",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/starknet-modular-arch",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 479033681,
                                "owner": "onlydustxyz",
                                "name": "draconis-starknet",
                                "description": "The best ally to your Web3 contributor journey. Become a DeWorker now.",
                                "htmlUrl": "https://github.com/onlydustxyz/draconis-starknet",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 480281806,
                                "owner": "onlydustxyz",
                                "name": "cairo-contracts",
                                "description": "OpenZeppelin Contracts written in Cairo for StarkNet, a decentralized ZK Rollup",
                                "htmlUrl": "https://github.com/onlydustxyz/cairo-contracts",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "description": "An interactive tutorial to get you up and running with Starknet",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 481932781,
                                "owner": "onlydustxyz",
                                "name": "starkonquest",
                                "description": "An educational game to learn Cairo where you implement ship AIs that fight to catch as much dust as possible!",
                                "htmlUrl": "https://github.com/onlydustxyz/starkonquest",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 483330552,
                                "owner": "onlydustxyz",
                                "name": "starknet-onboarding-ui",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/starknet-onboarding-ui",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 484056846,
                                "owner": "onlydustxyz",
                                "name": "starkonquest-ui",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/starkonquest-ui",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 486660536,
                                "owner": "onlydustxyz",
                                "name": "generator-starknet",
                                "description": "This is a development platform to quickly generate, develop & deploy smart contract based applications on StarkNet.",
                                "htmlUrl": "https://github.com/onlydustxyz/generator-starknet",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 490231565,
                                "owner": "onlydustxyz",
                                "name": "github-contribution-labels",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/github-contribution-labels",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 490985437,
                                "owner": "onlydustxyz",
                                "name": "protostar-vs-nile",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/protostar-vs-nile",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 491431479,
                                "owner": "onlydustxyz",
                                "name": "uraeus",
                                "description": "Command line utilities to check StarkNet contracts written in Cairo.",
                                "htmlUrl": "https://github.com/onlydustxyz/uraeus",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 492805046,
                                "owner": "onlydustxyz",
                                "name": "development-guidelines",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/development-guidelines",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 493552406,
                                "owner": "onlydustxyz",
                                "name": "protostar-vscode-test-adapter",
                                "description": "vscode extension to view protostar tests in the Test Explorer.",
                                "htmlUrl": "https://github.com/onlydustxyz/protostar-vscode-test-adapter",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 493584169,
                                "owner": "onlydustxyz",
                                "name": "project-kickstarter",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/project-kickstarter",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 493591124,
                                "owner": "onlydustxyz",
                                "name": "kaaper",
                                "description": "Documentation generator for Cairo projects.",
                                "htmlUrl": "https://github.com/onlydustxyz/kaaper",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 493602649,
                                "owner": "onlydustxyz",
                                "name": "serpopard",
                                "description": "Code coverage tool for Cairo contracts.",
                                "htmlUrl": "https://github.com/onlydustxyz/serpopard",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 493605381,
                                "owner": "onlydustxyz",
                                "name": "heka",
                                "description": "Heka is a tool measuring the Contribution friendliness of a Github repository.",
                                "htmlUrl": "https://github.com/onlydustxyz/heka",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 493795808,
                                "owner": "onlydustxyz",
                                "name": "cairo-streams",
                                "description": "Array stream library written in pure Cairo",
                                "htmlUrl": "https://github.com/onlydustxyz/cairo-streams",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 494087007,
                                "owner": "onlydustxyz",
                                "name": "devops",
                                "description": "Repo of Infra as Code & tooling Devops",
                                "htmlUrl": "https://github.com/onlydustxyz/devops",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 494452167,
                                "owner": "onlydustxyz",
                                "name": "heka-rest-api",
                                "description": "Heka backend REST API",
                                "htmlUrl": "https://github.com/onlydustxyz/heka-rest-api",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 499061005,
                                "owner": "onlydustxyz",
                                "name": "marketplace-starknet",
                                "description": "Death Note starknet contracts to manage contribution badges",
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-starknet",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 499502122,
                                "owner": "onlydustxyz",
                                "name": "marketplace-signup",
                                "description": "Handles GitHub users signup / profile creation",
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-signup",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 501233690,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend-old",
                                "description": "OnlyDust contribution marketplace frontend",
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend-old",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 505834957,
                                "owner": "onlydustxyz",
                                "name": "onlydust-rs",
                                "description": "Only Dust Rust librairies",
                                "htmlUrl": "https://github.com/onlydustxyz/onlydust-rs",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 505906397,
                                "owner": "onlydustxyz",
                                "name": "starklings-exercises-validator",
                                "description": "A Python microservice to validate starklings exercises",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings-exercises-validator",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 505906938,
                                "owner": "onlydustxyz",
                                "name": "starklings-backend",
                                "description": "A miscroservice to mint staklings users badges",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings-backend",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 507994565,
                                "owner": "onlydustxyz",
                                "name": "starklings-diploma",
                                "description": "Starklings certification Soulbound NFT",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings-diploma",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 508219470,
                                "owner": "onlydustxyz",
                                "name": "cicd",
                                "description": "Shared GitHub workflows",
                                "htmlUrl": "https://github.com/onlydustxyz/cicd",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 510308165,
                                "owner": "onlydustxyz",
                                "name": "starknet-node",
                                "description": "Script to create a starknet node",
                                "htmlUrl": "https://github.com/onlydustxyz/starknet-node",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 510654652,
                                "owner": "onlydustxyz",
                                "name": "contributions-platform-e2e-test",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/contributions-platform-e2e-test",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 511478575,
                                "owner": "onlydustxyz",
                                "name": "starknet-accounts",
                                "description": "Provides custom implementations of StarkNet accounts",
                                "htmlUrl": "https://github.com/onlydustxyz/starknet-accounts",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 523762227,
                                "owner": "onlydustxyz",
                                "name": "imhotep",
                                "description": "Cairo EVM bytecode interpreter",
                                "htmlUrl": "https://github.com/onlydustxyz/imhotep",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 530493897,
                                "owner": "onlydustxyz",
                                "name": "apibara",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/apibara",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 533216561,
                                "owner": "onlydustxyz",
                                "name": "backoffice-wallet-link",
                                "description": "Wallet link for Retool, used in the lead contributor back-office",
                                "htmlUrl": "https://github.com/onlydustxyz/backoffice-wallet-link",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 540776771,
                                "owner": "onlydustxyz",
                                "name": "heroku-buildpack-rust",
                                "description": "A buildpack for Rust applications on Heroku, with full support for Rustup, cargo and build caching.",
                                "htmlUrl": "https://github.com/onlydustxyz/heroku-buildpack-rust",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 557956292,
                                "owner": "onlydustxyz",
                                "name": "pathfinder",
                                "description": "A Starknet full node written in Rust",
                                "htmlUrl": "https://github.com/onlydustxyz/pathfinder",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 559561894,
                                "owner": "onlydustxyz",
                                "name": "cairo-foundry-demo",
                                "description": "Demo project for the cairo-foundry test runner",
                                "htmlUrl": "https://github.com/onlydustxyz/cairo-foundry-demo",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 560488797,
                                "owner": "onlydustxyz",
                                "name": "ctf",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/ctf",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 560545617,
                                "owner": "onlydustxyz",
                                "name": "account-obstr",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/account-obstr",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 563958883,
                                "owner": "onlydustxyz",
                                "name": "update-hasura-metadata-buildpack",
                                "description": "An heroku buildpack to deploy hasura on target environment",
                                "htmlUrl": "https://github.com/onlydustxyz/update-hasura-metadata-buildpack",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 564249238,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend-v2",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend-v2",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 566371874,
                                "owner": "onlydustxyz",
                                "name": "hasura-auth",
                                "description": "Authentication for Hasura.",
                                "htmlUrl": "https://github.com/onlydustxyz/hasura-auth",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 566840633,
                                "owner": "onlydustxyz",
                                "name": "graphql-engine-heroku",
                                "description": "Blazing fast, instant realtime GraphQL APIs on Postgres with fine grained access control, also trigger webhooks on database events.",
                                "htmlUrl": "https://github.com/onlydustxyz/graphql-engine-heroku",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 572060328,
                                "owner": "onlydustxyz",
                                "name": "google-scripts",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/google-scripts",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 593218280,
                                "owner": "onlydustxyz",
                                "name": "octocrab",
                                "description": "A modern, extensible GitHub API Client for Rust.",
                                "htmlUrl": "https://github.com/onlydustxyz/octocrab",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 593701982,
                                "owner": "onlydustxyz",
                                "name": "gateway",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/gateway",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 616908135,
                                "owner": "onlydustxyz",
                                "name": "checkers-tendermint",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/checkers-tendermint",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 618506515,
                                "owner": "onlydustxyz",
                                "name": "wait-on-action",
                                "description": "A GitHub Action variant of the wait-on package (npmjs.com/package/wait-on)",
                                "htmlUrl": "https://github.com/onlydustxyz/wait-on-action",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 652694393,
                                "owner": "onlydustxyz",
                                "name": "juniper",
                                "description": "GraphQL server library for Rust",
                                "htmlUrl": "https://github.com/onlydustxyz/juniper",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 653632145,
                                "owner": "onlydustxyz",
                                "name": "github-cache",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/github-cache",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 655629139,
                                "owner": "onlydustxyz",
                                "name": "marketplace-provisionning",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-provisionning",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 661599656,
                                "owner": "onlydustxyz",
                                "name": "madara-test",
                                "description": "\\uD83E\\uDD77\\uD83E\\uDE78 Madara is a ⚡ blazing fast ⚡ Starknet sequencer, based on substrate, powered by Rust \\uD83E\\uDD80",
                                "htmlUrl": "https://github.com/onlydustxyz/madara-test",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 661662838,
                                "owner": "onlydustxyz",
                                "name": "graphql-engine",
                                "description": "Blazing fast, instant realtime GraphQL APIs on your DB with fine grained access control, also trigger webhooks on database events.",
                                "htmlUrl": "https://github.com/onlydustxyz/graphql-engine",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 663102799,
                                "owner": "onlydustxyz",
                                "name": "od-rust-template",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/od-rust-template",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 672836289,
                                "owner": "onlydustxyz",
                                "name": "aws-infra-test",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/aws-infra-test",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 682131651,
                                "owner": "onlydustxyz",
                                "name": "metadata-image-generator",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/metadata-image-generator",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 688074915,
                                "owner": "onlydustxyz",
                                "name": "heroku-buildpack-rust-monorepo",
                                "description": "A buildpack for Rust applications on Heroku, with full support for Rustup, cargo and build caching.",
                                "htmlUrl": "https://github.com/onlydustxyz/heroku-buildpack-rust-monorepo",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 693559321,
                                "owner": "onlydustxyz",
                                "name": "cmc",
                                "description": "Library for CoinMarketCap API",
                                "htmlUrl": "https://github.com/onlydustxyz/cmc",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 695033641,
                                "owner": "onlydustxyz",
                                "name": "marketplace-backend",
                                "description": "Contributions marketplace backend services",
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-backend",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 698096830,
                                "owner": "onlydustxyz",
                                "name": "marketplace-api",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-api",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 699283256,
                                "owner": "onlydustxyz",
                                "name": "marketplace-indexer",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-indexer",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 703009655,
                                "owner": "onlydustxyz",
                                "name": "hackathon-frontend",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/hackathon-frontend",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              }
                            ],
                            "installed": true,
                            "isCurrentUserAdmin": false,
                            "isPersonal": false,
                            "installationId": 44300036
                          },
                          {
                            "id": 105865802,
                            "login": "symeo-io",
                            "avatarUrl": "https://avatars.githubusercontent.com/u/105865802?v=4",
                            "htmlUrl": "https://github.com/symeo-io",
                            "name": "Symeo.io",
                            "repos": [
                              {
                                "id": 494083544,
                                "owner": "symeo-io",
                                "name": "stack-analyser-poc",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/stack-analyser-poc",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 495382833,
                                "owner": "symeo-io",
                                "name": "symeo-monolithic-backend",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-monolithic-backend",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 498463832,
                                "owner": "symeo-io",
                                "name": "symeo-python-technical-test-dorian-frances",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-python-technical-test-dorian-frances",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 499396818,
                                "owner": "symeo-io",
                                "name": "delivery-poc",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/delivery-poc",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 510711359,
                                "owner": "symeo-io",
                                "name": "lily-audit",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/lily-audit",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 512630813,
                                "owner": "symeo-io",
                                "name": "symeo-webapp-v1",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-webapp-v1",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 516812947,
                                "owner": "symeo-io",
                                "name": "symeo-db-dump-loader",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-db-dump-loader",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 530105527,
                                "owner": "symeo-io",
                                "name": "symeo.io",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo.io",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 530538652,
                                "owner": "symeo-io",
                                "name": "symeo-lambda-data-collection-cronjob",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-lambda-data-collection-cronjob",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 540301049,
                                "owner": "symeo-io",
                                "name": "symeo-quality-api",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-quality-api",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 557133009,
                                "owner": "symeo-io",
                                "name": "symeo-ci-script",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-ci-script",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 561198487,
                                "owner": "symeo-io",
                                "name": "delicorner-tech-test-template",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/delicorner-tech-test-template",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 567681570,
                                "owner": "symeo-io",
                                "name": "symeo-common-lib",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-common-lib",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 570129100,
                                "owner": "symeo-io",
                                "name": "symeo-audit-webapp",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-audit-webapp",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 570150480,
                                "owner": "symeo-io",
                                "name": "symeo-audit-api",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-audit-api",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 577411575,
                                "owner": "symeo-io",
                                "name": "symeo-vcs-authentication-adapter",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-vcs-authentication-adapter",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 577691776,
                                "owner": "symeo-io",
                                "name": "symeo-report-api",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-report-api",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 589590744,
                                "owner": "symeo-io",
                                "name": "gpt3-eval",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/gpt3-eval",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 589670498,
                                "owner": "symeo-io",
                                "name": "method-cyclomatic-complexity-plugin",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/method-cyclomatic-complexity-plugin",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 593240835,
                                "owner": "symeo-io",
                                "name": "symeo-api",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-api",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 593536214,
                                "owner": "symeo-io",
                                "name": "symeo-js",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-js",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 595202901,
                                "owner": "symeo-io",
                                "name": "symeo-python",
                                "description": "The Symeo SDK made for interacting with your Symeo secrets and configuration from python applications",
                                "htmlUrl": "https://github.com/symeo-io/symeo-python",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 595278857,
                                "owner": "symeo-io",
                                "name": "symeo-webapp",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-webapp",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 601143461,
                                "owner": "symeo-io",
                                "name": "symeo-python-sdk-test",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-python-sdk-test",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 601720367,
                                "owner": "symeo-io",
                                "name": "symeo-php",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-php",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 602899574,
                                "owner": "symeo-io",
                                "name": "docs.symeo.io",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/docs.symeo.io",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 604510922,
                                "owner": "symeo-io",
                                "name": "symeo-js-sdk-test",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-js-sdk-test",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 610721954,
                                "owner": "symeo-io",
                                "name": "symeo-sdk-it",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-sdk-it",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 616883136,
                                "owner": "symeo-io",
                                "name": "symeo-demo",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-demo",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 624779785,
                                "owner": "symeo-io",
                                "name": "symeo-cli",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-cli",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 628875479,
                                "owner": "symeo-io",
                                "name": "symeo-product-demo-js",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-product-demo-js",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 630968030,
                                "owner": "symeo-io",
                                "name": "symeo-rate-limiting-worker",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-rate-limiting-worker",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 636240099,
                                "owner": "symeo-io",
                                "name": "symeo-js-template",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-js-template",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 640857793,
                                "owner": "symeo-io",
                                "name": "symeo-php-template",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-php-template",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 641970419,
                                "owner": "symeo-io",
                                "name": "symeo-python-template",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-python-template",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 649551461,
                                "owner": "symeo-io",
                                "name": "symeo-reactjs-template",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-reactjs-template",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 656103826,
                                "owner": "symeo-io",
                                "name": "symeo-cloud-function-example",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-cloud-function-example",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 656112347,
                                "owner": "symeo-io",
                                "name": "symeo-lambda-example",
                                "description": null,
                                "htmlUrl": "https://github.com/symeo-io/symeo-lambda-example",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              }
                            ],
                            "installed": true,
                            "isCurrentUserAdmin": true,
                            "isPersonal": false,
                            "installationId": 44300041
                          },
                          {
                            "id": 16590657,
                            "login": "PierreOucif",
                            "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                            "htmlUrl": "https://github.com/PierreOucif",
                            "name": "Ilysse",
                            "repos": [
                              {
                                "id": 49207056,
                                "owner": "PierreOucif",
                                "name": "Exercice_part_2",
                                "description": "Share a repository on GitHub",
                                "htmlUrl": "https://github.com/PierreOucif/Exercice_part_2",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 49336771,
                                "owner": "PierreOucif",
                                "name": "3d-scanning-image-processing",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/3d-scanning-image-processing",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 49336884,
                                "owner": "PierreOucif",
                                "name": "Character-recognition",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/Character-recognition",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 49443035,
                                "owner": "PierreOucif",
                                "name": "Machine-Vision",
                                "description": "ME6406 Machine Vision homeworks done during the fall 2015 semestre at Georgia Tech",
                                "htmlUrl": "https://github.com/PierreOucif/Machine-Vision",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 49765829,
                                "owner": "PierreOucif",
                                "name": "3D-scanning-first-software",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/3D-scanning-first-software",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 56065996,
                                "owner": "PierreOucif",
                                "name": "testJenkins",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/testJenkins",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 56069528,
                                "owner": "PierreOucif",
                                "name": "jenkins",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/jenkins",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 60153388,
                                "owner": "PierreOucif",
                                "name": "angularMaterialPractice",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/angularMaterialPractice",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 60348436,
                                "owner": "PierreOucif",
                                "name": "initRepoAngularJS",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/initRepoAngularJS",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 72745480,
                                "owner": "PierreOucif",
                                "name": "profileSegmentation",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/profileSegmentation",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 72746454,
                                "owner": "PierreOucif",
                                "name": "profile-segmentation",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/profile-segmentation",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 82152320,
                                "owner": "PierreOucif",
                                "name": "image-processing-tools",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/image-processing-tools",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 82683154,
                                "owner": "PierreOucif",
                                "name": "test",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/test",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 162179942,
                                "owner": "PierreOucif",
                                "name": "Hystrix",
                                "description": "Hystrix is a latency and fault tolerance library designed to isolate points of access to remote systems, services and 3rd party libraries, stop cascading failure and enable resilience in complex distributed systems where failure is inevitable.",
                                "htmlUrl": "https://github.com/PierreOucif/Hystrix",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 321580026,
                                "owner": "PierreOucif",
                                "name": "test_backmarket",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/test_backmarket",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 356362720,
                                "owner": "PierreOucif",
                                "name": "poc-lld",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/poc-lld",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 361101648,
                                "owner": "PierreOucif",
                                "name": "armis-reference-api-kata",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/armis-reference-api-kata",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 378055100,
                                "owner": "PierreOucif",
                                "name": "demo-python-setup-and-tooling",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/demo-python-setup-and-tooling",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 410545036,
                                "owner": "PierreOucif",
                                "name": "card-backend",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/card-backend",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 482760118,
                                "owner": "PierreOucif",
                                "name": "stack-analyser",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/stack-analyser",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 486539812,
                                "owner": "PierreOucif",
                                "name": "ef-resources",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/ef-resources",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 502866874,
                                "owner": "PierreOucif",
                                "name": "python-technical-test",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/python-technical-test",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 608749086,
                                "owner": "PierreOucif",
                                "name": "basic-repo",
                                "description": "Basic Python project to layout",
                                "htmlUrl": "https://github.com/PierreOucif/basic-repo",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 661589618,
                                "owner": "PierreOucif",
                                "name": "carbonfact-java",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/carbonfact-java",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 661592692,
                                "owner": "PierreOucif",
                                "name": "carbonfact-python",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/carbonfact-python",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              },
                              {
                                "id": 697620016,
                                "owner": "PierreOucif",
                                "name": "marketplace-api",
                                "description": null,
                                "htmlUrl": "https://github.com/PierreOucif/marketplace-api",
                                "stars": null,
                                "forkCount": null,
                                "hasIssues": null,
                                "isIncludedInProject": null,
                                "isAuthorizedInGithubApp": true
                              }
                            ],
                            "installed": true,
                            "isCurrentUserAdmin": true,
                            "isPersonal": true,
                            "installationId": 44300050
                          }
                        ]
                                                
                        """);
    }
}
