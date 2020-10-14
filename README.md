# lanting-server

### spring (Framework) [x]

### gradle (Build) [x]

### mysql (Database) [x]

### mybatis/mybatis-plus (ORM) [x]

### redis (Cache) [x]

### kafka (Messaging)

zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties & kafka-server-start /usr/local/etc/kafka/server.properties

### dubbo (RPC)

### logback (Logging)

### swagger (API doc) [x]

### exception handling

## deployment

### docker/supervisor/pm2/forever (daemonize)

rsync -arvz --progress ./build/libs/lanting-server-0.0.1-SNAPSHOT.jar root@lanting.wiki:/data/server-apps/lanting-server/
