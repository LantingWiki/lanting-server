### MySQL Timezone Issue

If you are running api/health but receiving error message like Cannot recognize "Malay TimeZone".
Simply go in Mysql command line and type following.

SET GLOBAL time_zone = '+8:00';

or change setting from my.ini in MySQL workbench.

### Spring Cache Redis 
用spring cache, 很好生效了. 但是不是存进redis, redis里没有key? 查了半天, 想还能存进哪个redis呢, 存了就删了?

debugger断到cache里面, 看到创建了concurrentmapcache. 好了, 知道是确实不放. 看了半天, 最后生疑, RedisCache怎么没有? 没有这个类. 仔细看了一遍, 结果没有spring-boot-starter-data-redis这个jar. 但是, build.gradle里面是有的?

尝试改错成 `implementation 'org.springframework.boot:spring-boot-starter-data-redis11111111'`, 还是能构建? 这是gradle同步出了问题. 改对回来重新同步, 好了, 显示download对应的jar. 再来就好了. 全自动. 所以前一次可能是没download成功, 以为成功了 (我点叉cancel). 不知道怎么就继续构建了. 然后依赖都是DI, 注入的, 所以没有explicit依赖, 所以还能跑成功, 跑出in-memory cache
