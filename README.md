# Money-transfer-RESTful-API

## Technology
|Tech stack|
|-------|
|Java 8|
|Java JAX-RS|
|JUnit 5|
|Google/gson|
|Maven|

## How to run

<p>mvn package assembly:single</p>
<p>java -jar target\moneytransfer-0.0.1-SNAPSHOT-jar-with-dependencies.jar</p>

## API services

| HTTP          | Path          |Action|
| ------------- |:-------------|:---------------|
| GET           | http://localhost:8080/accounts | Get all accounts
| GET           | http://localhost:8080/accounts/{accountId}      | Get account corresponding to {accountId}|
| GET           | http://localhost:8080/accounts/{accountId}/username| Get username corresponding to {accountId}|
| GET           | http://localhost:8080/accounts/{accountId}/balance| Get balance corresponding to {accountId}|
| POST          | http://localhost:8080/accounts| Create an account|
| PUT           | http://localhost:8080/accounts/{accountId}/withdraw/{amount}| Withdraw {amount} from account corresponding to {accountId}|
| PUT           | http://localhost:8080/accounts/{accountId}/deposit/{amount}| Deposit {amount} from account corresponding to {accountId}|
| DELETE        | http://localhost:8080/accounts/{accountId}| Delete account corresponding to {accountId}|
| GET           | http://localhost:8080/transfers| Get all transfers|
GET             | http://localhost:8080/transfers/{transferId}| Get transfer corresponding to {accountId}|
| GET           | http://localhost:8080/transfers/getTransfersByAccountId/{accountId}| Getall transfers corresponding to {accountId}|
|POST           | http://localhost:8080/transfers| Create a transfer|

## Pre-loaded accounts
<p>http://localhost:8080/accounts/0</p>
<p>http://localhost:8080/accounts/1</p>
<p>http://localhost:8080/accounts/2</p>
<p>http://localhost:8080/accounts/3</p>
