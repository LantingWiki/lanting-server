package wiki.lanting.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


@Slf4j
@SpringBootTest
public class WechatServiceTest {
    @Autowired
    WechatService WechatService;

    @BeforeAll
    static void setUp() {

    }

    @AfterAll
    static void tearDown() {

    }

    @Test
    void parseMessageTest() throws ParserConfigurationException, SAXException, IOException {

        String inputXML ="<xml>" +
                            "<ToUserName>"+
                                "<![CDATA[gh_7519e0f64995]]>"+
                            "</ToUserName>" +
                            "<FromUserName>" +
                                "<![CDATA[oKwJrt2gqPpy4w8onzULp597JfPY]]>" +
                            "</FromUserName>" +
                            "<CreateTime>1602773245</CreateTime>" +
                            "<MsgType>" +
                                "<![CDATA[text]]>" +
                            "</MsgType>" +
                            "<Content>" +
                                "<![CDATA[test123]]>" +
                            "</Content>" +
                            "<MsgId>22946197590553876</MsgId>" +
                            "<Encrypt>" +
                                "<![CDATA[3NXE9C0f85lxTisPgi4QukIDJK3F32yTJzlEChy+rJbBetHgPV5Sy53f1nCQ/1AepBzODamx4ZONksYqkgfSs3lWswlyMKFx6uInYqmivK/h+whNxWM/ll70R7/wwoC3mrGK4FKqanPNOGJHYu/nk/KtTgM7PLElol+yi7HITI5QRxy2bvvFtBkrLeedIr5RNW4Q7eAj186nUCXxgP1uAOwwipQDeE+jdmwrrxi3je2n6oLmRSpHqwrvGf8UXkRPv3pGOQymkRiXWDiIsp2JqvyhQo50d9xLKy6tz+BKdfXMbO3Wtwucu5zvS4uD1AbYV8Q0MJq26EPlrwILRzyJkxkiFlVoNZkJvCipaFu2e4v/bAt0HdBc2XJ9yOyPogr6oItgp4FHxsBxb4KnkAo3UEZITTFCqRxs0KjQ9NC/JjM=]]>" +
                            "</Encrypt>" +
                        "</xml>";
        String result = WechatService.initService(inputXML);
        log.info("test result is {}",result);
        //    sample xml

    }
}
