package wiki.lanting.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Value("${lanting.secrets.wechat_token}")
    final String WECHAT_TOKEN = "";

    static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    @GetMapping("/echo")
    public String echo(@RequestParam String signature,
                       @RequestParam String timestamp,
                       @RequestParam String nonce,
                       @RequestParam String echostr) throws NoSuchAlgorithmException {
        List<String> tmpArr = new ArrayList<>();
        tmpArr.add(WECHAT_TOKEN);
        tmpArr.add(timestamp);
        tmpArr.add(nonce);
        Collections.sort(tmpArr);
        String tmpStr = String.join("", tmpArr);
        tmpStr = sha1(tmpStr);
        if (tmpStr.equals(signature)) {
            return echostr;
        } else {
            log.error("Wechat signature doesn't match, theirs: {}, ours: {}", signature, tmpStr);
            return "";
        }
    }
}
