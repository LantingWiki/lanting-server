package wiki.lanting.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;
import wiki.lanting.common.LantingResponse;
import wiki.lanting.services.WechatService;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author boyang.wang
 */
@Slf4j
@RestController
@RequestMapping("/api/wechat")
public class WechatController {

    final WechatService WechatService;

    public WechatController(WechatService WechatService) {
        this.WechatService = WechatService;
    }

    @Value("${lanting.secrets.wechat-token}")
    String wechatToken = "";

    static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

//    @GetMapping("/echo")
    public String echo(@RequestParam String signature,
                       @RequestParam String timestamp,
                       @RequestParam String nonce,
                       @RequestParam String echostr) throws NoSuchAlgorithmException {
        List<String> tmpArr = new ArrayList<>();
        tmpArr.add(wechatToken);
        tmpArr.add(timestamp);
        tmpArr.add(nonce);
        Collections.sort(tmpArr);
        String tmpStr = String.join("", tmpArr);
        tmpStr = sha1(tmpStr);
        if (tmpStr.equals(signature)) {
            return echostr;
        } else {
            log.error("Wechat signature doesn't match, theirs: {}, ours: {}, timestamp: {}, nonce: {}, token: {}", signature, tmpStr, timestamp, nonce, wechatToken);
            return "";
        }
    }

    @PostMapping(path = {"/message/receive", "/echo"}, consumes = {MediaType.TEXT_XML_VALUE})
    public LantingResponse<String> readPlainMsg(@RequestBody String requestBody) throws ParserConfigurationException, SAXException, IOException {
        log.info("received xml: {}", requestBody);
        String result = WechatService.initService(requestBody);
        log.info("Processed result {}", result);
        return new LantingResponse<String>().data(result);
    }



}
