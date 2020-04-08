# Universal resolver driver: did:echo

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for Echo **did:echo** identifiers.

## Specifications
[ECHO DID specifications](echo_did_specifications.md)

## Example DIDs

```
did:echo:0.1.2.1
did:echo:1.1.2.0
```

## Build and Run

```
docker build -f ./docker/Dockerfile . -t driver-did-echo
docker run --network host -p 8080:8080 driver-did-echo
curl -X GET http://localhost:8080/1.0/identifiers/did:echo:0.1.25.0
```

## URL of node

You should set your own URL of nodes by setting up env variables:

* uniresolver_driver_did_echo_mainnet_rpc_url for mainnet url
* uniresolver_driver_did_echo_testnet_rpc_url for testnet url
* uniresolver_driver_did_echo_devnet_rpc_url for devnet url

## Maintainers

- Anton Shkindzer [@pixelplex](https://github.com/nikonok)
- Anzhy Cherniavsky [@pixelplex](https://github.com/anzhy-chernyavski)
