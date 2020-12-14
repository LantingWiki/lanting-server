rsync -arvz --progress ./build/libs/lanting-server-0.0.1-SNAPSHOT.jar root@lanting.wiki:/data/server-apps/lanting-server/

java -jar -DWEB_DRIVER_PATH=/data/server-apps/chromewebdriver -DSINGLE_PAGE_PATH=/data/server-apps/lanting-github/server/clipper/cli -DWECHAT_TOKEN=XXX -DSPRING_DATASOURCE_PASSWORD=XXX lanting-server-0.0.1-SNAPSHOT.jar
