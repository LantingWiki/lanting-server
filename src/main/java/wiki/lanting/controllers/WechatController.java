package wiki.lanting.controllers;

import ch.qos.logback.core.subst.Token;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author wang.boyang
 */
@Slf4j
@RestController
@RequestMapping("/api/wechat")
public class WechatController {

    static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    @PostMapping("/echo")
    public String echo(@RequestBody WechatController.wechatRequestBody requestBody) throws NoSuchAlgorithmException {
        //TODO
        List<String> tmpArr  = new ArrayList<>();
        String token = System.getenv("WECHAT_TOKEN");
        tmpArr.add(token);
        tmpArr.add(requestBody.timestamp);
        tmpArr.add(requestBody.nonce);
        Collections.sort(tmpArr);
        String tmpStr  = String.join("", tmpArr);
        tmpStr = sha1(tmpStr);
        if (tmpStr.equals(requestBody.signature)){
            return requestBody.echostr;
        }else{
            return "";
        }

    }

    @Data
    private static class wechatRequestBody {
        public String signature;
        public String timestamp;
        public String nonce;
        public String echostr;
    }
}
