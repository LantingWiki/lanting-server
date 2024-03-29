package wiki.lanting.services;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import wiki.lanting.common.LantingResponse;
import wiki.lanting.controllers.ArchiveController;
import wiki.lanting.mappers.ArchiveMapper;
import wiki.lanting.mappers.LikeArticleMapper;
import wiki.lanting.mappers.SearchKeywordMapper;
import wiki.lanting.mappers.UserMapper;
import wiki.lanting.models.*;

import javax.annotation.PostConstruct;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wang.boyang
 */

@Slf4j
@Service
public class ArchiveService {

    public static final String USER_SERVICE_KAFKA_TOPIC = "wiki-lanting-services-UserService";
    public static final String REDIS_KEY_PENDING_CREATE = "wiki.lanting.services.UserService.userPendingCreate";

    @Value("${lanting.constants.singlepagepath}")
    String SINGPLE_PAGE_PATH;

    @Value("${lanting.constants.webdriverpath}")
    String WEB_DRIVER_PATH;

    final KafkaTemplate<String, String> template;
    final RedisTemplate<String, String> redisTemplate;
    final JdbcTemplate jdbcTemplate;
    final UserMapper userMapper;
    final ArchiveMapper archiveMapper;
    final LikeArticleMapper likeArticleMapper;
    final SearchKeywordMapper searchKeywordMapper;

    CompiledArchivesEntity compiledArchivesEntity = null;
    boolean isArchving = false;

    public ArchiveService(JdbcTemplate jdbcTemplate, UserMapper userMapper, RedisTemplate<String, String> redisTemplate, LikeArticleMapper likeArticleMapper, SearchKeywordMapper searchKeywordMapper, @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") KafkaTemplate<String, String> template, ArchiveMapper archiveMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.template = template;
        this.likeArticleMapper = likeArticleMapper;
        this.searchKeywordMapper = searchKeywordMapper;
        this.archiveMapper = archiveMapper;
    }

    @PostConstruct
    public void initializer() {
        initLikes();
        this.compiledArchivesEntity = initCompiledArchives();
    }

    public void initLikes() {
        log.info("This will be printed; LOG has already been injected");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date dateBeforeDB = new Date();
        log.info("Time mark before access DB {}", dateFormat.format(dateBeforeDB));
        List<LikeArticleEntity> likeArticleEntities = likeArticleMapper.selectList(null);
        Date dateAfterDB = new Date();
        log.info("Time mark after access DB {}", dateFormat.format(dateAfterDB));
        Map<String, String> likesMap = getLikeMap(likeArticleEntities);
        Date dateBeforeLoop = new Date();
        log.info("Time mark after calc loop {}", dateFormat.format(dateBeforeLoop));
        redisTemplate.opsForValue().multiSet(likesMap);
        Date dateAfterLoop = new Date();
        log.info("Time mark after redis loop {}", dateFormat.format(dateAfterLoop));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaManualAckListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(new HashMap<>(8) {{
            this.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            this.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            this.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        }}));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @KafkaListener(topics = USER_SERVICE_KAFKA_TOPIC, containerFactory = "kafkaManualAckListenerContainerFactory", groupId = "lanting-group")
    public void listen(ConsumerRecord<String, String> cr, Acknowledgment ack) throws JsonProcessingException {
        log.info("consumerRecord: {}", cr.toString());
        @SuppressWarnings("Convert2Diamond")
        List<UserEntity> userEntities = new ObjectMapper().readValue(cr.value(),
                new TypeReference<List<UserEntity>>() {
                });

        int toCreateUserCount = userEntities.size();
        log.info("total users to create {}", toCreateUserCount);
        for (UserEntity userEntity : userEntities) {
            this.createUser(userEntity);
            setPendingCreation(-1);
        }
        ack.acknowledge();
    }

    /**
     * 使用JDBC:
     * List<UserEntity> result = jdbcTemplate.query("select * from abe.users", (rs, rowNum) -> {
     * log.info("in row mapper: {} {}", rs, rowNum);
     * UserEntity userEntity = new UserEntity();
     * userEntity.id = rs.getLong(1);
     * return userEntity;
     * });
     * return result.size() > 0 ? result.get(0) : null;
     * <p>
     * 使用RedisTemplate
     * Integer test1 = redisTemplate.opsForValue().append("test1", "111");
     * log.error("test1 {}", test1);
     */
    public UserEntity readUser(long id) {
        UserEntity userEntity = userMapper.selectById(id);
        log.info("in readUser, id: {}, user: {}", id, userEntity);
        return userEntity;
    }

    public ArchiveController.LikeRequestBody likeArticle(ArchiveController.LikeRequestBody likeRequestBody, String clientAddress) {
        LikeArticleEntity likeArticleEntity = new LikeArticleEntity();
        likeArticleEntity.articleId = likeRequestBody.articleId;
        likeArticleEntity.isLike = likeRequestBody.like;
        likeArticleEntity.clientId = clientAddress;
        likeArticleEntity.createdAt = System.currentTimeMillis();

        likeArticleMapper.insert(likeArticleEntity);
        long delta;
        if (likeArticleEntity.isLike) {
            delta = 1;
        } else {
            delta = -1;
        }
        redisTemplate.opsForValue().increment("lantingLikes-" + likeArticleEntity.articleId, delta);
        String result = redisTemplate.opsForValue().get("lantingLikes-" + likeArticleEntity.articleId);
        log.info("after like the value is {}", result);
        return likeRequestBody;
    }

    public UserEntity createUser(UserEntity userEntity) {
        int insert = userMapper.insert(userEntity);
        log.info("in createUser, user: {}", insert);
        return userEntity;
    }

    public int updateUser(UserEntity userEntity) {
        int result = userMapper.updateById(userEntity);
        log.info("in updateUser, result: {}, user: {}", result, userEntity);
        return result;
    }

    public int deleteUser(long id) {
        int result = userMapper.deleteById(id);
        log.info("in updateUser, result: {}, userid: {}", result, id);
        return result;
    }

    public int countUser() {
        int result = userMapper.selectCount(null);
        log.info("in countUser, result: {}", result);
        return result;
    }

    @Cacheable(value = "wiki.lanting.services.UserService.searchUser", key = "#userEntity.nickname")
    public List<UserEntity> searchUser(UserEntity userEntity) {
        List<UserEntity> results = userMapper.selectByMap(Map.of("nickname", userEntity.nickname));
        log.info("in searchUser, results: {}, nickname: {}", results, userEntity.nickname);
        return results;
    }

    public Boolean massCreateUser(List<UserEntity> userEntities) throws JsonProcessingException {
        // send a message to Kafka
        ListenableFuture<SendResult<String, String>> send = this.template.send(
                USER_SERVICE_KAFKA_TOPIC, new ObjectMapper().writeValueAsString(userEntities));
        try {
            SendResult<String, String> sendResult = send.get();
            log.info("in massCreateUser, sent: {}, metadata: {}",
                    sendResult.getProducerRecord(), sendResult.getRecordMetadata());
            setPendingCreation(userEntities.size());
            return true;
        } catch (ExecutionException | InterruptedException e) {
            log.error("in massCreateUser", e);
            return false;
        }
    }

    public int checkPendingCreation() {
        String result = redisTemplate.opsForValue().get(REDIS_KEY_PENDING_CREATE);
        log.info("remained users to create: {}", result);

        if (result != null) {
            return Integer.parseInt(result);
        } else {
            return 0;
        }
    }


    public void setPendingCreation(long delta) {
        // atomic operation
        if (delta > 0) {
            redisTemplate.opsForValue().increment(REDIS_KEY_PENDING_CREATE, delta);
        } else if (delta < 0) {
            redisTemplate.opsForValue().decrement(REDIS_KEY_PENDING_CREATE, -delta);
        }
    }


    public Map<Long, Integer> readLikeArticle(long articleId) {
        Map<Long, Integer> likesResultMap = new HashMap<>();
        if (articleId != -1) {
            String lantingKey = "lantingLikes-" + articleId;
            String result = redisTemplate.opsForValue().get(lantingKey);
            if (result != null) {
                likesResultMap.put(articleId, Integer.valueOf(result));
            }
        } else {
            Set<String> redisKeys = redisTemplate.keys("lantingLikes-*");
            if (redisKeys != null) {
                List<String> keysList = new ArrayList<>(redisKeys);
                List<String> valueList = redisTemplate.opsForValue().multiGet(redisKeys);
                if (valueList != null) {
                    for (int i = 0; i < keysList.size(); i++) {
                        String Key = keysList.get(i).substring(13);
                        likesResultMap.put(Long.valueOf(Key), Integer.valueOf(valueList.get(i)));
                    }
                }
            }

        }
        return likesResultMap;
    }

    private Map<String, String> getLikeMap(List<LikeArticleEntity> likeArticleEntities) {
        Map<String, String> likesMap = new HashMap<>();
        likeArticleEntities.forEach(e -> {
            String curLikes = likesMap.get(String.valueOf(e.articleId));
            int curLikesInt;
            if (curLikes == null) {
                curLikesInt = 0;
            } else {
                curLikesInt = Integer.parseInt(curLikes);
            }
            String lantingKey = "lantingLikes-" + e.articleId;
            likesMap.put(lantingKey, e.isLike ? String.valueOf(curLikesInt + 1) : String.valueOf(curLikesInt - 1));
        });
        return likesMap;
    }

    public ArchiveBasicInfoEntity tributeArchiveInfo(String link) throws IOException {
        ArchiveBasicInfoEntity archiveBasicInfoEntity = new ArchiveBasicInfoEntity();
        Matcher matcher;
        String regex;
        Document doc = Jsoup.connect(link).get();

//        BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/wang.boyang/Projects/mine/lanting-server/src/main/resources/example-xpost.html"));
//        writer.write(doc.html());
//        writer.close();

        Elements title = doc.select(".rich_media_title");
        if (title.isEmpty()) {
            title = doc.select("title");
        }
        archiveBasicInfoEntity.title = title.first().text().strip();

        Elements xPost = doc.select(".original_primary_card_tips");
        if (xPost.isEmpty()) {
            Element publisherNode = doc.select("#js_name").first();
            if (publisherNode != null) {
                archiveBasicInfoEntity.publisher = publisherNode.text().strip();
            }
            Element authorNode = doc.select("span.rich_media_meta.rich_media_meta_text").first();
            if (authorNode == null) {
                archiveBasicInfoEntity.author = archiveBasicInfoEntity.publisher;
            } else {
                archiveBasicInfoEntity.author = authorNode.text();
            }
        } else {
            archiveBasicInfoEntity.publisher = doc.select(".original_primary_nickname").text();
            archiveBasicInfoEntity.author = doc.select("meta[name=author]").attr("content");
        }
        if (archiveBasicInfoEntity.author != null) {
            archiveBasicInfoEntity.author = archiveBasicInfoEntity.author.replaceAll("[ /]", ", ");
        }

        String dt = null;
        regex = ",i=\"(\\d\\d\\d\\d-\\d\\d-\\d\\d)\";";
        matcher = Pattern.compile(regex).matcher(doc.html());
        if (matcher.find()) {
            dt = matcher.group(1).substring(0, 7);
        }
        archiveBasicInfoEntity.date = dt;
        log.info("in tributeArchiveInfo, archiveBasicInfoEntity: {}", archiveBasicInfoEntity);
        return archiveBasicInfoEntity;
    }

    public String generateTempTributeScriptContent(ArchiveTributeInfoEntity archiveTributeInfoEntity) {
        StringBuilder res = new StringBuilder("#!/bin/bash").append(System.lineSeparator())
                .append("cd " + SINGPLE_PAGE_PATH).append(System.lineSeparator())
                .append("cd ../../../").append(System.lineSeparator())
                .append("git pull --rebase").append(System.lineSeparator())
                .append("cd " + SINGPLE_PAGE_PATH).append(System.lineSeparator())
                .append(String.format(
                        "node single-file.js --noopen=true --web-driver-executable-path='%s' --articleinfo='%s' %s",
                        WEB_DRIVER_PATH,
                        JSON.toJSONString(archiveTributeInfoEntity),
                        archiveTributeInfoEntity.link))
                .append(System.lineSeparator())
                .append("cd ../../../").append(System.lineSeparator())
                .append("sleep 3").append(System.lineSeparator())
                .append("git status").append(System.lineSeparator())
                .append("yarn archives").append(System.lineSeparator())
                .append("git push origin master").append(System.lineSeparator());
        return res.toString();
    }

    public LantingResponse<Boolean> tributeArchiveSave(ArchiveTributeInfoEntity archiveTributeInfoEntity) {
        if (isArchving) {
            return new LantingResponse<Boolean>().fail().code("Previous archive hasn't finished").data(false);
        }
        isArchving = true;

        try {
            File tempScript = File.createTempFile("lanting", null);
            Writer streamWriter = new OutputStreamWriter(new FileOutputStream(
                    tempScript));
            PrintWriter printWriter = new PrintWriter(streamWriter);
            String generatedTempScript = generateTempTributeScriptContent(archiveTributeInfoEntity);
            log.info("generatedTempScript {}", generatedTempScript);
            printWriter.print(generatedTempScript);
            printWriter.close();

            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            log.error("intributeArchiveSave", e);
            return new LantingResponse<Boolean>().error().code(e.getMessage()).data(false);
        } finally {
            isArchving = false;
        }
        return new LantingResponse<Boolean>().data(true);
    }

    public int searchKeywordCreate(String keyword) {
        keyword = keyword.strip();
        List<SearchKeywordEntity> found = searchKeywordMapper.selectByMap(Map.of("keyword", keyword));
        int result;
        if (found.size() == 0) {
            SearchKeywordEntity searchKeywordEntity = new SearchKeywordEntity();
            searchKeywordEntity.createdAt = System.currentTimeMillis();
            searchKeywordEntity.updatedAt = searchKeywordEntity.createdAt;
            searchKeywordEntity.keyword = keyword;
            searchKeywordEntity.searchCount = 1;
            result = searchKeywordMapper.insert(searchKeywordEntity);
        } else {
            SearchKeywordEntity searchKeywordEntity = found.get(0);
            searchKeywordEntity.searchCount += 1;
            searchKeywordEntity.updatedAt = System.currentTimeMillis();
            result = searchKeywordMapper.updateById(searchKeywordEntity);
        }
        return result;
    }

    public List<SearchKeywordEntity> searchKeywordRead() {
        return searchKeywordMapper.selectList(null);
    }

    public CompiledArchivesEntity archivesRead() {
        return this.compiledArchivesEntity;
    }

    public CompiledArchivesEntity initCompiledArchives() {
        CompiledArchivesEntity compiledArchivesEntity = new CompiledArchivesEntity();

        List<ArchiveEntity> archiveEntities = archiveMapper.selectList(null);
        List<Map<String, Object>> origs = jdbcTemplate.queryForList("SELECT * FROM archive_origs");
        List<Map<String, Object>> authors = jdbcTemplate.queryForList("SELECT * FROM archive_authors");
        List<Map<String, Object>> tags = jdbcTemplate.queryForList("SELECT * FROM archive_tags");
        List<Map<String, Object>> publishers = jdbcTemplate.queryForList("SELECT * FROM archive_publishers");
        List<Map<String, Object>> remarksList = jdbcTemplate.queryForList("SELECT * FROM archive_remarks");

        for (ArchiveEntity archiveEntity : archiveEntities) {
            FullArchiveEntity fullArchiveEntity = new FullArchiveEntity(archiveEntity);
            compiledArchivesEntity.archives.put(archiveEntity.id, fullArchiveEntity);
            fullArchiveEntity.date = fullArchiveEntity.publishYear + "-" + String.format("%02d", fullArchiveEntity.publishMonth);
        }
        for (Map<String, Object> publisher : publishers) {
            Long id = (Long) publisher.get("archive_id");
            FullArchiveEntity fullArchiveEntity = compiledArchivesEntity.archives.get(id);
            fullArchiveEntity.publisher = (String) publisher.get("publisher");
        }
        for (Map<String, Object> remarks : remarksList) {
            Long id = (Long) remarks.get("archive_id");
            FullArchiveEntity fullArchiveEntity = compiledArchivesEntity.archives.get(id);
            fullArchiveEntity.remarks = (String) remarks.get("remarks");
        }
        for (Map<String, Object> orig : origs) {
            Long id = (Long) orig.get("archive_id");
            FullArchiveEntity fullArchiveEntity = compiledArchivesEntity.archives.get(id);
            fullArchiveEntity.origs.add((String) orig.get("orig"));
        }
        for (Map<String, Object> author : authors) {
            Long id = (Long) author.get("archive_id");
            FullArchiveEntity fullArchiveEntity = compiledArchivesEntity.archives.get(id);
            fullArchiveEntity.author.add((String) author.get("author"));
        }
        for (Map<String, Object> tag : tags) {
            Long id = (Long) tag.get("archive_id");
            FullArchiveEntity fullArchiveEntity = compiledArchivesEntity.archives.get(id);
            fullArchiveEntity.tag.add((String) tag.get("tag"));
        }

        log.info("inited compiledArchivesEntity {}",
                compiledArchivesEntity.archives.keySet().size());
        return compiledArchivesEntity;
    }
}
