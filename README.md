# Transit - Java Spring Boot

## 빌드 환경 및 빌드

### Java 17

```bash
$ java -version
java -version                
openjdk version "17.0.8" 2023-07-18
OpenJDK Runtime Environment Homebrew (build 17.0.8+0)
OpenJDK 64-Bit Server VM Homebrew (build 17.0.8+0, mixed mode, sharing)
```

### Gradle 8

```bash
$ gradle -version

------------------------------------------------------------
Gradle 8.2.1
------------------------------------------------------------

Build time:   2023-07-10 12:12:35 UTC
Revision:     a38ec64d3c4612da9083cc506a1ccb212afeecaa

Kotlin:       1.8.20
Groovy:       3.0.17
Ant:          Apache Ant(TM) version 1.10.13 compiled on January 4 2023
JVM:          20.0.1 (Homebrew 20.0.1)
OS:           Mac OS X 13.5.1 aarch64
```

### Build

```bash
$ pwd
/a/b/c/java-app

$ gradle build

$ ls ./app/build/lib/
```


## Vault 환경

### Transit 활성화

```bash
$ vault secrets enable transit
```

### Trasit 키 생성

```bash
$ vault write -f transit/keys/ds-poc type=aes256-gcm96

Key                       Value
---                       -----
allow_plaintext_backup    false
auto_rotate_period        0s
deletion_allowed          false
derived                   false
exportable                false
imported_key              false
keys                      map[1:1694166532]
latest_version            1
min_available_version     0
min_decryption_version    1
min_encryption_version    0
name                      ds-poc
supports_decryption       true
supports_derivation       true
supports_encryption       true
supports_signing          false
type                      aes256-gcm96
```

### Transit 생성된 키 확인

```bash
$ vault list transit/keys

Keys
----
ds-poc
```

### Transit 암호화 테스트

```bash
$ vault write transit/encrypt/ds-poc plaintext=$(echo "My Data" | base64)

Key            Value
---            -----
ciphertext     vault:v1:ZwX8OwN9/1EOticsdacRUZY5cONc/aH8bt1StS8JG/pNQSsP
key_version    1
```

### Transit 복호화 테스트

```bash
$ vault write transit/decrypt/ds-poc ciphertext='vault:v1:ZwX8OwN9/1EOticsdacRUZY5cONc/aH8bt1StS8JG/pNQSsP'

Key          Value
---          -----
plaintext    TXkgRGF0YQo=

$ echo TXkgRGF0YQo= | base64 -d

My Data
```

## MySQL 8.0.31

- version : 8.0.31
- port : 3306
- user/pw : admin/password

### mysql client (macos - option)

```bash
$ brew install mysql-client
$ echo 'export PATH="/opt/homebrew/opt/mysql-client/bin:$PATH"' >> ~/.zshrc
```

### mysql connect

```bash
$ export MYSQL_HOST=127.0.0.1
$ export MYSQL_PORT=3306
$ mysql -h $MYSQL_HOST -p $MYSQL_PORT -u admin -ppassword

# if docker
$ docker exec -it mysql mysql -u root -ppassword

mysql: [Warning] Using a password on the command line interface can be insecure.
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 10
Server version: 8.0.31 MySQL Community Server - GPL

Copyright (c) 2000, 2022, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql>
```

### mysql create table example

```sql
mysql> CREATE DATABASE VaultData;

Query OK, 1 row affected (0.00 sec)
```

### mysql user create

```sql
mysql> CREATE USER app@'%' IDENTIFIED BY 'password';

mysql> GRANT ALL PRIVILEGES
    ON VaultData.*
    TO app@'%';

mysql> FLUSH PRIVILEGES;
```

### mysql table example

```sql

mysql> USE VaultData;

Database changed

mysql> create table vault_data (
  id int unsigned auto_increment not null,
  data varchar(32) not null,
  date_created timestamp default now(),
  primary key (id)
);

Query OK, 0 rows affected (0.02 sec)
```

### mysql insert test

```sql
mysql> INSERT INTO vault_data (data) VALUES ('1st test data');

Query OK, 1 row affected (0.02 sec)

mysql> SELECT * FROM vault_data;

+----+---------------+---------------------+
| id | data          | date_created        |
+----+---------------+---------------------+
|  1 | 1st test data | 2023-09-08 10:25:25 |
+----+---------------+---------------------+
1 row in set (0.00 sec)
```


## Java Run

### 환경변수 구성
```bash
export MYSQL_HOST=127.0.0.1
export MYSQL_PORT=3306
export MYSQL_DB_NAME=VaultData
export MYSQL_USERNAME=app
export MYSQL_USERPW=password
export VAULT_HOST=127.0.0.1
export VAULT_PORT=8200
export VAULT_SCHEME=http
export VAULT_TOKEN=root
export VAULT_TRANSIT_KEY_NAME=ds-poc
```

### 실행
```bash
java -jar demo-0.1.0.jar
```

![](./images/screenshot_main.png)
