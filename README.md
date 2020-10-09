# lanting-server

### spring (Framework)

### gradle (Build)

### mysql (Database)

### mybatis/mybatis-plus (ORM)

### redis (Cache)

### kafka (Messaging)

### dubbo (RPC)

### logback (Logging)

### swagger (API doc)

### exception handling

### docker/supervisor/pm2/forever (daemonize)

### MySQL Timezone Issue

If you are running api/health but receiving error message like Cannot recognize "Malay TimeZone".
Simply go in Mysql command line and type following.

SET GLOBAL time_zone = '+8:00';

or change setting from my.ini in MySQL workbench.