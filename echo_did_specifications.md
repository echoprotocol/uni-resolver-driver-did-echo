did:echo method
=================
19th March 2020

Anton Shkindzer <<a.shkinder@pixelplex.io>>,
Anzhy Cherniavsky <<a.chernyavski@pixelplex.io>>

[Decentralized Identifiers](https://w3c-ccg.github.io/did-spec/) are designed to be compatible with any distributed ledger or network (called the target system).
We propose a new DID method that allows special objects in ECHO network to be treated as valid DIDs.

## DID Method Name

The namestring that shall identify this DID method is: `echo`

A DID that uses this method MUST begin with the following prefix: `did:echo`. Per the DID specification, this string MUST be in lowercase. The remainder of the DID, after the prefix, is specified below.

## Method Specific Identifier

The method specific identifier is composed of network type and id of object at ECHO network.

Network type can be '0' for mainnet, '1' for testnet and '2' for devnet. Next goes triplet of numbers devided by points: id of object at ECHO network.

> Note: if it's not one of this networks, value will be '255'.

```
echo-did = "did:echo:" echo-specific-idstring
echo-specific-idstring = [ echo-network  ":" ] echo-object-id
echo-network  = "0" / "1" / "2" / "255"
echo-object-id  = 1*DIGIT "." 1*DIGIT "." 1*DIGIT
```

### Example

Example `echo` DIDs:

 * `did:echo:0.1.25.2`
 * `did:echo:1.1.25.1`
 * `did:echo:2.1.25.0`

## DID Document

### Example

```json
{
  "@context": "https://w3id.org/did/v1",
  "id": "did:echo:0.1.25.0",
  "publicKey": [
    {
      "id": "did:echo:0.1.25.0",
      "type": "Ed25519VerificationKey2018",
      "publicKeyBase58": "6vyGhBpTjtbJCCyyCP21Tm3fAkWLrSCBaVG3ULrisRek#key-1"
    },
    {
      "id": "did:echo:0.1.25.0",
      "type": "Ed25519VerificationKey2018",
      "publicKeyBase58": "DaQencDTLD5u6LGk9JNaMoJBh6sAkGchMnZPjtJXdvG1#key-2"
    }
  ],
  "authentication": [
    {
      "type": "Ed25519SignatureAuthentication2018",
      "publicKey": "did:echo:0.1.25.0#key-1"
    },
    {
      "type": "Ed25519SignatureAuthentication2018",
      "publicKey": "did:echo:0.1.25.0#key-2"
    }
  ]
}
```

## Type of keys

did:echo uses `Ed25519SignatureAuthentication2018` keys encoded in `Base58`.

## CRUD Operation Definitions

Each DID Document is stored within an object in the ECHO network. A transaction needs to sent in order to create an object. Operations are components of the protocol transaction. For more information, please read the  [ECHO documentation](https://docs.echo.org/api-reference/echo-operations).

There are several methods to build a transaction: 

 1. via ECHO wallet - `echo_wallet`.
 2. via ECHO libraries: [echopy-lib](https://github.com/echoprotocol/echopy-lib), [echojs-lib](https://github.com/echoprotocol/echojs-lib) and others.
 3. build JSON transactions using third-party tools and via [Network broadcast API: `broadcast_transaction`](https://docs.echo.org/api-reference/echo-node-api/network-broadcast-api#broadcast_transaction-trx).

We will describe the first method as it is the most demonstrative. Other methods are described in the documentation to libraries.

Here's the JSON for the transaction:
```json
{
  "ref_block_num": 0,
  "ref_block_prefix": 0,
  "expiration": "<time>",
  "operations": [
    <operation>
  ],
  "extensions": [],
  "signatures": [
    "<signature>"
  ],
  "signed_with_echorand_key": false
}
```

All `<>` fields need to be filled out. We describe this in the points below.

To launch the `echo_wallet` please first read the [documentation](https://docs.echo.org/how-to/use-cli-wallet).

### Create (Register)

Follow these steps:

1. `begin_builder_transaction`

This returns `TRXID`, which is used to simultaneously build multiple transactions.

Next we get the operation prototype:

2. `get_prototype_operation did_create_operation`

```json
[
  66,{
    "fee": {
      "amount": 0,
      "asset_id": "1.3.0"
    },
    "registrar": "1.2.0",
    "essence": "0.0.0",
    "public_keys": []
  }
]
```

After filling out the fields, we add the operation to the transaction:

3. `add_operation_to_builder_transaction TRXID [66,{"fee":{"amount":0,"asset_id":"1.3.0"},"registrar":"1.2.26","essence":"1.2.26","public_keys":["DaQencDTLD5u6LGk9JNaMoJBh6sAkGchMnZPjtJXdvG2"]}]`

Next we set the transaction fee:

4. `set_fees_on_builder_transaction TRXID ECHO`

We sign it and send to the network:

5. `sign_builder_transaction TRXID true`
```json
{
  "ref_block_num": 65,
  "ref_block_prefix": 1164919582,
  "expiration": "2020-03-18T15:04:05",
  "operations": [[
      66,{
        "fee": {
          "amount": 0,
          "asset_id": "1.3.0"
        },
        "registrar": "1.2.26",
        "essence": "1.2.26",
        "public_keys": [
          "DaQencDTLD5u6LGk9JNaMoJBh6sAkGchMnZPjtJXdvG2"
        ]
      }
    ]
  ],
  "extensions": [],
  "signatures": [
    "d8a6f2b5aef58c3021473405c66c1da1913351db94b47838de2e3f0463c0e341093e86fc64e7dfc0a973ee22064c2df652234950143c501cd274cf8251466607"
  ],
  "signed_with_echorand_key": false
}
```

### Read (Resolve)

DID Document can be retrieved using JSON-RPC:

1. Via echo universal resolver method.
2. Via direct query, for example, using curl.

curl Example:

```
curl --data '{"jsonrpc": "2.0", "params": ["did", "get_did_object", ["1.25.0"]], "method": "call", "id": 1}' http://localhost:8090/rpc
```

Response:

```
{"id":9,"jsonrpc":"2.0","result":"{\"@context\":\"https://w3id.org/did/v1\",\"id\":\"did:echo:0.1.25.0\",\"publicKey\":[{\"id\":\"did:echo:0.1.25.0\",\"type\":\"Ed25519VerificationKey2018\",\"publicKeyBase58\":\"6XS3BMVnEHAzo1PhHWt9vndrZn2P27tCbU9WdqCM8sJu#key-1\"},{\"id\":\"did:echo:0.1.25.0\",\"type\":\"Ed25519VerificationKey2018\",\"publicKeyBase58\":\"DaQencDTLD5u6LGk9JNaMoJBh6sAkGchMnZPjtJXdvG2#key-2\"}],\"authentication\":[{\"type\":\"Ed25519SignatureAuthentication2018\",\"publicKey\":\"did:echo:0.1.25.0#key-1\"},{\"type\":\"Ed25519SignatureAuthentication2018\",\"publicKey\":\"did:echo:0.1.25.0#key-2\"}]}"}
```

### Update

Follow these steps:

1. `begin_builder_transaction`

This returns `TRXID`, which is used to simultaneously build multiple transactions.

Get the operation prototype:

2. `get_prototype_operation did_update_operation`

```json
[
  67,{
    "fee": {
      "amount": 0,
      "asset_id": "1.3.0"
    },
    "registrar": "1.2.0",
    "did_identifier": "1.25.0",
    "pub_keys_to_delete": [],
    "pub_keys_to_add": []
  }
]
```

After filling out the fields, we add the operation to the transaction:

3. `add_operation_to_builder_transaction TRXID [67,{"fee":{"amount":0,"asset_id":"1.3.0"},"registrar":"1.2.26","did_identifier":"1.25.0","pub_keys_to_delete":["6XS3BMVnEHAzo1PhHWt9vndrZn2P27tCbU9WdqCM8sJu"],"pub_keys_to_add":[]}]`

Next we set the transaction fee:

4. `set_fees_on_builder_transaction TRXID ECHO`

We sign it and send to the network:

5. `sign_builder_transaction TRXID true`
```json
{
  "ref_block_num": 66,
  "ref_block_prefix": 1754558868,
  "expiration": "2020-03-19T11:22:07",
  "operations": [[
      67,{
        "fee": {
          "amount": 0,
          "asset_id": "1.3.0"
        },
        "registrar": "1.2.26",
        "did_identifier": "1.25.0",
        "pub_keys_to_delete": [
          "6XS3BMVnEHAzo1PhHWt9vndrZn2P27tCbU9WdqCM8sJu"
        ],
        "pub_keys_to_add": []
      }
    ]
  ],
  "extensions": [],
  "signatures": [
    "fc95d9d788cb502444be76f6416635577368d0d5e2dff8c079f87509a4c49f9bcf9418323ea02420c0dd81b2d12750249b186db54ae5250e47363d67707b3602"
  ],
  "signed_with_echorand_key": false
}
```

### Delete (Revoke)

Follow these steps:

1. `begin_builder_transaction`

This returns `TRXID`, which is used to simultaneously build multiple transactions.

Get the operation prototype:

2. `get_prototype_operation did_delete_operation`

```json
[
  68,{
    "fee": {
      "amount": 0,
      "asset_id": "1.3.0"
    },
    "registrar": "1.2.0",
    "did_identifier": "1.25.0"
  }
]
```

After filling out the fields, we add the operation to the transaction:

3. `add_operation_to_builder_transaction TRXID [68,{"fee":{"amount":0,"asset_id":"1.3.0"},"registrar":"1.2.26","did_identifier":"0.1.25.0"}]`

Next we set the transaction fee:

4. `set_fees_on_builder_transaction TRXID ECHO`

We sign it and send to the network:

5. `sign_builder_transaction TRXID true`
```json
{
  "ref_block_num": 67,
  "ref_block_prefix": 4132514528,
  "expiration": "2020-03-19T11:26:52",
  "operations": [[
      68,{
        "fee": {
          "amount": 0,
          "asset_id": "1.3.0"
        },
        "registrar": "1.2.26",
        "did_identifier": "1.25.0"
      }
    ]
  ],
  "extensions": [],
  "signatures": [
    "499f93cd32653de3a4a1ea3601e67c4f0e50c5a91de899467470cd9e5f68ffacac3e32531533f6e55c0c71f98a6c74ab650bf3644b0369526d1e874594286e0e"
  ],
  "signed_with_echorand_key": false

```

 > Note: You can preview the transaction before signing it using `preview_builder_transaction`.

 ## Security Considerations

TODO

## Privacy Considerations

TODO

## Performance Considerations

TODO

References
----------

 **[1]** https://w3c-ccg.github.io/did-spec/

 **[2]** https://docs.echo.org/
 