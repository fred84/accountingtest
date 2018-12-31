REQUIREMENTS

- java 11

USAGE

- download file from release page
- `java -Dmicronaut.server.port=8080 -jar accountingtest-1.0.0-all.jar`
- `curl -XPOST http://localhost:8080/create`
- `curl -XPOST http://localhost:8080/create`
- `curl -XPOST http://localhost:8080/adjustment -H 'Content-Type: application/json' -d '{"accountId": 1, "amount": 1, "description": "abc"}'`
- `curl -XPOST http://localhost:8080/transfer -H 'Content-Type: application/json' -d '{"fromId": 1, "toId": 2, "amount": 1, "description": "def"}'`
- `curl http://localhost:8080/1`
- `curl http://localhost:8080/2`
- `curl http://localhost:8080/1/history`

AVAILABLE API METHODS

- create account
- transfer from account to account
- adjust account balance
- view account balance changes

IMPLEMENTATION

- account balances are updated using CAS.
- as a result balance history are eventually consistent.

SIMPLIFICATIONS

- Account only have numeric identifier. I assume that it is known by third-party systems
- Currency is omitted 
- Account balance should be non-negative (no overdrafts, etc)
- Account balance value could be adjusted. This is emulation of transfer from external system
- Timezones are not supported
- Both account creation and transfer are not idemponent. External identifiers could be introduced for real system
- No filtering supported for balance change history
- Balance addition is always successful. In future if it could be rejected we may implement Saga pattern
- Scale and rounding issues for account balance are omitted
- Api error messages are not very consistent

