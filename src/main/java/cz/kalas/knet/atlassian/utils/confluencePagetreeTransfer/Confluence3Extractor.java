package cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer;

import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model.ConfluencePage;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

public class Confluence3Extractor {

    private final Properties properties;

    public static int processedPageCounter = 0;

    private final DocumentBuilderFactory builderFactory;
    private final DocumentBuilder builder;

    public Confluence3Extractor(Properties properties) {
        this.properties = properties;
        builderFactory = DocumentBuilderFactory.newInstance();
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new IllegalStateException("Class cant be instantiated!");
        }
    }

    public ConfluencePage process(Integer rootPageId) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        return parse(queryPageContent(rootPageId));
    }


    private ConfluencePage parse(String xml) throws ParserConfigurationException, XPathExpressionException, IOException, SAXException {
        ConfluencePage page = new ConfluencePage();


        Document xmlDocument = builder.parse(new InputSource(new StringReader(xml)));
        XPath xPath = XPathFactory.newInstance().newXPath();

        page.sourcePageId = Integer.parseInt((String) xPath.compile("/content/@id").evaluate(xmlDocument, XPathConstants.STRING));
        page.title = (String) xPath.compile("/content/title").evaluate(xmlDocument, XPathConstants.STRING);
        page.wikiContent = (String) xPath.compile("/content/body").evaluate(xmlDocument, XPathConstants.STRING);

        int childrenSize = Integer.parseInt((String) xPath.compile("/content/children/@size").evaluate(xmlDocument, XPathConstants.STRING));

        if (childrenSize > 0) {
            NodeList children = (NodeList) xPath.compile("/content/children/child::node()").evaluate(xmlDocument, XPathConstants.NODESET);

            List<ConfluencePage> childrenPages = new ArrayList<>();

            for (int i = 0; i < childrenSize; i++) {

                Node childNode = children.item(i);


                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) childNode;
                    Integer childPageId = Integer.parseInt(element.getAttribute("id"));
                    childrenPages.add(process(childPageId));
                }
                page.children = childrenPages;

            }
        }

        processedPageCounter++;
        return page;

    }


    private String queryPageContent(Integer pageId) {
        System.out.println("requesting page " + pageId + " from " + properties.getProperty("input.url"));
        String content = null;
        try {
            URL url = new URL(
                    properties.getProperty("input.url") +
                    "/rest/prototype/1/content/" + pageId + "?expand=children");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Basic " + Base64
                    .getEncoder()
                    .encodeToString(
                            (properties.getProperty("input.username") + ":" + properties.getProperty("input.pass"))
                                    .getBytes())
            );

            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            content = IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
