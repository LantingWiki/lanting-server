package wiki.lanting.services;


import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

@Slf4j
@Service
public class WechatService {

    public HashMap<String,String> lastEdited = new HashMap<>();
    public static class NewsDetails {
        public long id;
        public String url;
        public String title;
        public String author;
        public String publisher;
        public String date;
        public String chapter;
        public String tag;
        public String remarks;
    }
    public HashMap<String,NewsDetails> localCache = new HashMap<>();

    public String initService(String xmlInput) throws IOException, SAXException, ParserConfigurationException {
        
        log.info("the input is {}", xmlInput);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlInput)));
        Element rootElement = document.getDocumentElement();
        Node mainNode=  document.getFirstChild();
        NodeList nlist = mainNode.getChildNodes();
        HashMap<String,String> Result = new HashMap<>();

        log.info("the message is {}",rootElement.getAttributeNodeNS("Content","Content"));

        for (int i = 0 ; i < nlist.getLength() ; i++) {
            Node child = nlist.item(i);
            Result.put(child.getNodeName(), child.getTextContent());
            log.info("the {} child is {},{}",i,child.getNodeName(),child.getTextContent());
            // process the child node here
        }

        String toUserName=String.format("<![CDATA[%s]]>",Result.get("FromUserName"));
        String fromUserName=String.format("<![CDATA[%s]]>",Result.get("ToUserName"));
        String Content=String.format("<![CDATA[我是一个复读机： %s]]>",Result.get("Content"));
        String outPut = "<xml>" +
                "<ToUserName>"+
                toUserName +
                "</ToUserName>" +
                "<FromUserName>" +
                fromUserName +
                "</FromUserName>" +
                "<CreateTime>1602773245</CreateTime>" +
                "<MsgType>" +
                "<![CDATA[text]]>" +
                "</MsgType>" +
                "<Content>" +
                Content +
                "</Content>" +
                "</xml>";
        log.info("last editted is {}",lastEdited);
        lastEdited.put(Result.get("FromUserName"), Result.get("Content"));
        return outPut;
    }
}
