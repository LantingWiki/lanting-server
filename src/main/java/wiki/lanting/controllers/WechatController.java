package wiki.lanting.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wang.boyang
 */
@Slf4j
@RestController
@RequestMapping("/api/wechat")
public class WechatController {

    @PostMapping("/echo")
    public String echo() {
        //TODO
        return "";
    }
}
