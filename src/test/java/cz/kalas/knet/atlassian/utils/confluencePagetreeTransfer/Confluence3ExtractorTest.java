package cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer;

import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.exctractor.Confluence3Extractor;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.exctractor.ConfluenceExtractor;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model.ConfluencePage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class Confluence3ExtractorTest {

    Properties testProperties;

    @Before
    public void setup() {
        testProperties = Transfer.loadProperties("config-knet.properties");
    }

    /**
     * Test is tied to my testing Confluence instance
     */
    @Test
    public void extractPageTreeTest() {
        ConfluenceExtractor extractor = new Confluence3Extractor(testProperties);
        ConfluencePage rootPage = extractor.extractPageTree(10781309);

        Assert.assertNotNull(rootPage);
        Assert.assertEquals(10781309, (int) rootPage.sourcePageId);
    }
}
