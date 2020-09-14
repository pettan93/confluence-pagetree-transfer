package cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.exctractor;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gets pages from Confluence 3.5.7 using REST API in XML format and converts them to objects
 * Can be used recursively (along with page-children page relationships)
 * <p>
 * Confluence old prototype REST API docs
 * https://docs.atlassian.com/atlassian-confluence/REST/3.5.7/
 */
public class Confluence3Extractor implements ConfluenceExtractor {

    private static final Logger LOGGER = Logger.getLogger(Confluence3Extractor.class.getName());

    public static int processedPageCounter = 0;

    private final Properties properties;

    private final DocumentBuilder builder;

    private final XPath xPath;

    public Confluence3Extractor(Properties properties) {
        this.properties = properties;
        xPath = XPathFactory.newInstance().newXPath();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to instantiate xml document builder!");
        }
    }

    public ConfluencePage extractPageTree(Integer pageId) {
        try {
            return parsePageFromXml(
                    queryPageContent(pageId)
            );
        } catch (XPathExpressionException | IOException | SAXException e) {
            e.printStackTrace();
            throw new IllegalStateException("Parsing page tree failed on page with id " + pageId);
        }
    }


    /**
     * Parses page from xml, recursively query (http api call) and parses pages children
     *
     * @param pageXml page data in XML format (see Confluence API docs)
     * @return Page model, tree structure if page has children
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     */
    private ConfluencePage parsePageFromXml(String pageXml)
            throws XPathExpressionException, IOException, SAXException {
        Document xmlDocument = builder.parse(new InputSource(new StringReader(pageXml)));

        ConfluencePage page = new ConfluencePage();
        page.sourcePageId = Integer.parseInt((String) xPath.compile("/content/@id")
                .evaluate(xmlDocument, XPathConstants.STRING));
        page.title = (String) xPath.compile("/content/title")
                .evaluate(xmlDocument, XPathConstants.STRING);
        page.wikiContent = (String) xPath.compile("/content/body")
                .evaluate(xmlDocument, XPathConstants.STRING);

        int childrenSize = Integer.parseInt((String) xPath.compile("/content/children/@size")
                .evaluate(xmlDocument, XPathConstants.STRING));

        if (childrenSize > 0) {
            NodeList children = (NodeList) xPath.compile("/content/children/child::node()")
                    .evaluate(xmlDocument, XPathConstants.NODESET);

            List<ConfluencePage> childPages = new ArrayList<>();
            for (int i = 0; i < childrenSize; i++) {
                Node childNode = children.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) childNode;
                    Integer childPageId = Integer.parseInt(element.getAttribute("id"));
                    childPages.add(
                            extractPageTree(childPageId) // recursive call here
                    );
                }
                page.children = childPages;
            }
        }
        processedPageCounter++;
        return page;
    }


    /**
     * Queries page description and content from Confluence API
     * REST API Docs: https://docs.atlassian.com/atlassian-confluence/REST/3.5.7/#id1629318
     *
     * @param pageId Queried page id
     * @return page data in XML format (see Confluence API docs)
     */
    private String queryPageContent(Integer pageId) {
        LOGGER.log(Level.INFO, "requesting page " + pageId + " from " + properties.getProperty("input.url"));
        String responseContent = null;
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

            responseContent = IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseContent;
    }
}
