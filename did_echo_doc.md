# Echo DID

## Decentralized Identifiers

DID - это новый тип идентификации, который позволяет проводить ее децентрализованно.

Conventional identity management systems are based on centralized authorities such as corporate directory services, certificate authorities, or domain name registries. From the standpoint of cryptographic trust verification, each of these centralized authorities serves as its own root of trust. To make identity management work across these systems requires implementing federated identity management.

The emergence of distributed ledger technology (DLT) and blockchain technology provides the opportunity for fully decentralized identity management. In a decentralized identity system, entities (that is, discrete identifiable units such as, but not limited to, people, organizations, and things) are free to use any shared root of trust. Globally distributed ledgers, decentralized P2P networks, or other systems with similar capabilities, provide the means for managing a root of trust without introducing a centralized authority or a single point of failure. In combination, DLTs and decentralized identity management systems enable any entity to create and manage their own identifiers on any number of distributed, independent roots of trust.

Более подробно про DID можно прочитать в [официальной документации](https://www.w3.org/TR/did-core/).

## DID in ECHO network

DID Document формируется из объекта протокола ECHO.
Для взаимодействия с DID Document разработано три типа операций: create, update, delete.

На данный момент DID в ECHO может иметь три типа:

1. DID для аккаунта.
2. DID для ассета.
3. DID для контракта.

Все добавляемые ключи будут проверены на их соответствие кодировке `Base58` и должны относиться к алгоритму `Ed25519`.

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

### Operation validation

Мы можем использовать DID одного из трех типов, описанных выше.
Для этого их необходимо провалидировать, только владелец аккаунта/ассета/контракта будет иметь возможность создавать DID для данных объектов.

### DID API

Так же в ECHO существует DID API, которое возвращает готовый DID document, заполненные в соотвествии со стандартом, ссылка на который есть выше.

### `"publicKey"` and `"authentication"` fields of DID documents

В протоколе ECHO невозможно помещать в `"publicKey"` ключи, которые не будут использованы в `"authentication"`.

## DID implementation

### Operations

#### `did_create_operation`

Операция для создания объекта DID.

* registrar - аккаунт создающий DID
* essence - object id требующее подтверждение с помощью DID
* public_keys - set из pubkeys `Ed25519` в кодировке `Base58` (может быть пустым)

#### `did_update_operation`

Операция для обновления объекта DID.

* registrar - аккаунт создающий DID
* did_identifier - method specific identifier
* pub_keys_to_delete - set из pubkeys `Ed25519` в кодировке `Base58` для удаления (может быть пустым)
* pub_keys_to_add - set из pubkeys `Ed25519` в кодировке `Base58` для добавления (может быть пустым)

#### `did_delete_operation`

Операция для удаления объекта DID.

* registrar - аккаунт создающий DID
* did_identifier - method specific identifier

### Operation validation

Есть два этапа валидации операции:

1. validate

>Visitor, который проверит, что ключи находятся в нужной кодировке.

2. do_evaluate

> Метод, который проверяет, что(один из трех вариантов):

> 2.1. Аккаунт, отправивший операцию является аккаунтом для которого создается DID.

> 2.2. Аккаунт, отправивший операцию является аккаунтом, выпустившим ассет.

> 2.3. Аккаунт, отправивший операцию является аккаунтом-владельцем контракта.

### DID object

`did_object` - объект, содержажий информацию о DID Document:

* public_keys - pubkeys `Ed25519` в кодировке `Base58` (может быть пустым)
* essence - object id владющий DID

### DID API

Для получения документа DID существуют API метод, под названием `get_did_object`. Он принимает string с DID Document ID и возвращает соотвествующий объект, если подобного объекта не существует, то вернется пустой DID document.

Пример использования, через curl:

```
curl --data '{"jsonrpc": "2.0", "params": ["did", "get_did_object", ["1.25.0"]], "method": "call", "id": 9}' http://localhost:8090/rpc
```

## Примеры использования DID

DID может быть использован для подтверждения того, что аккаунт закреплен за определенным человеком и это подтверждено каким-либо root of trust.

DID может быть использован для подтверждения того, что ассет выпущен дествительно данным владельцем или обаладает определенными качествами.

DID может быть использован для подтверждения того, что контракт прошел аудит и получил сертефикат. Это позволит пользователям, получить, более простой способ удостовериться, что контракт дествительно безопасен. 