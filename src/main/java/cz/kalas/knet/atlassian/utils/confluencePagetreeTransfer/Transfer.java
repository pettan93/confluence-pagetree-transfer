package cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer;

import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model.ConfluencePage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class Transfer {

    private final Confluence3Extractor extractor;
    private final Confluence7Importer importer;

    public static void main(String[] args) {

        Properties properties = Transfer.loadProperties("config.properties");
        Confluence3Extractor extractor = new Confluence3Extractor(properties);
        Confluence7Importer importer = new Confluence7Importer(properties);

        var transfer = new Transfer(extractor, importer);
        transfer.transferPageTree(786446, 124585130);

    }

    public Transfer(Confluence3Extractor extractor, Confluence7Importer importer) {
        this.extractor = extractor;
        this.importer = importer;
    }

    public void transferPageTree(Integer sourcePageRoot, Integer targetPageRoot) {
        try {
            ConfluencePage pageTreeRoot = extractor.process(sourcePageRoot);

            importer.createPageTree(pageTreeRoot, targetPageRoot);

        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
            e.printStackTrace();
        }
    }


    public static Properties loadProperties(String propertiesName) {
        String resourceFilePath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource(propertiesName)).getPath();
        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(resourceFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return appProps;
    }


}
