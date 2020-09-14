package cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer;

import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.importer.Confluence7Importer;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.importer.ConfluenceImporter;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model.ConfluencePage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class Confluence7ImporterTest {

    Properties testProperties;

    @Before
    public void setup() {
        testProperties = Transfer.loadProperties("config-knet.properties");
    }

    /**
     * Test actually creates page in Confluence instance as it is in properties file!
     * Not isolated, not stateless
     */
    @Test
    public void createPageTest() {
        ConfluenceImporter importer = new Confluence7Importer(testProperties);

        var page = new ConfluencePage();
        page.sourcePageId = 999;
        page.title = "test" + System.currentTimeMillis();
        page.wikiContent = "foo";

        var importResult = importer.createPage(page, 72715266);

        Assert.assertNotNull(importResult);
        Assert.assertNotNull(importResult.getId());
    }

}
