# Tolam Markets Exchange Microservices

## Setup

Install OpenJDK 17 from https://adoptium.net/.

A valid Docker environment is required for running in development mode. 

## Usage

### Configuration

The Exchange Service has several configuration options available. There are some required
credentials that must be provided for third party integrations to work.

Configuration can be provided as a `local.yml` file in the root directory _OR_ as environment
variables. Note that when using environment variables, the format changes to upper snake case,
i.e. `hem.hedera.operator-id` becomes `HEM_HEDERA_OPERATOR_ID`.

An example file has been provided at `local.yml.example`.

Credentials and configuration to use specifically during integration tests can be added
in `local-integration.yml`. `local.yml` _will not_ be used for tests.

The following configuration keys are available:

| Key                                 | Description                                                                         | Required                   | Default Value  |
|-------------------------------------|-------------------------------------------------------------------------------------|----------------------------|----------------|
| `hem.hedera.operator-id`            | Account to use as the "system account"                                              | Yes                        ||
| `hem.hedera.private-key`            | Private key corresponding to the "system account"                                   | Yes                        ||
| `hem.hedera.offsets-contract-id`    | ID of the Smart Contract to utilize for listing / purchasing                        | Yes                        ||
| `hem.lworks.api-key`                | API Key for integration with LedgerWorks                                            | Yes                        ||
| `hem.txn.timeout.listing.created`   | Seconds to wait before expiring a listing transaction in the `CREATED` state        | No                         | 30             |
| `hem.txn.timeout.listing.approved`  | Seconds to wait before expiring a listing transaction in the `APPROVED` state       | No                         | 30             |
| `hem.txn.timeout.purchase.created`  | Seconds to wait before expiring a purchase transaction in the `CREATED` state       | No                         | 30             |
| `hem.txn.timeout.purchase.approved` | Seconds to wait before expiring a purchase transaction in the `APPROVED` state      | No                         | 30             |
| `hem.txn.minimum-finality`          | Seconds before the `consensus_timestamp` on a transaction is considered final       | No                         | 5              |
| `hem.armm.url`                      | URL for an ARMM instance to retrieve prices from                                    | No                         | localhost:8081 |
| `hem.armm.fallback`                 | Enable placeholder fallback for ARMM prices¹                                        | No                         | false          |
| `pubsub.emulator.host`              | Hostname and port for a Google Pub/Sub emulator                                     | Only when running as a jar ||
| `micronaut.server.cors.enabled`     | Allow cross-origin requests. This should be set to `true` when working with the UI. | No                         | false          | 

¹ The fallback for the ARMM returns a randomized price range for an offset

#### MainNet configuration

To switch to MainNet, add the environment variable `MICRONAUT_ENVIRONMENTS=mainnet`.

### Running

Running in development environment:
```
./gradlew :offset-service:run
```

Please note that this command requires Docker unless you configure `pubsub.emulator.host`.

### Building

Building is _not required_ for running in development mode.

Building a jar for deployment:
```
./gradlew :offset-service:build
```

To build a jar without running tests _(not recommended)_, use the following command instead:
```
./gradlew :offset-service:assemble
```

The resulting jar file is available at `service/offset/build/libs/offset-service-0.1.0-SNAPSHOT-all.jar`.

This jar can be run using the following command:
```
java -jar service/offset/build/libs/offset-service-0.1.0-SNAPSHOT-all.jar
```
This will load configuration from your environment variables. 

If you wish to use your `local.yml` instead, add `-Dmicronaut.config.files=local.yml`, as shown below.
```
java -Dmicronaut.config.files=local.yml -jar service/offset/build/libs/offset-service-0.1.0-SNAPSHOT-all.jar
```

### Testing

To run all tests:
```
./gradlew check
```

To run only unit tests:
```
./gradlew test
```

To run only integration tests:
```
./gradlew integrationTest
```

Test reports will be available under `service/offset/build/reports/tests`.

### Use with User Interface

To run with the UI follow the setup instructions for Tolam Markets UI. UI is designed to run
alongside Microservices. Ensure the same Smart Contract ID is specified for both the UI and
Microservices. Smart Contracts may be generated using scripts available in the Smart Contracts
repository.

## License

Copyright 2022 Tolam Earth

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
