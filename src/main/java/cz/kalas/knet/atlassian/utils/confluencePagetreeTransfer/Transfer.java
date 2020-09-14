package cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer;

import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.exctractor.Confluence3Extractor;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.exctractor.ConfluenceExtractor;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.importer.Confluence7Importer;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.importer.ConfluenceImporter;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model.ConfluencePage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * Represent transfer of page (tree of pages) from Confluence 3.x.x to Confluence 4+
 */
public class Transfer {

    private final ConfluenceExtractor extractor;
    private final ConfluenceImporter importer;

    private final Integer sourcePageRoot;
    private final Integer targetPageRoot;

    public static void main(String[] args) {
        Properties properties = Transfer.loadProperties("config.properties");
        ConfluenceExtractor extractor = new Confluence3Extractor(properties);
        ConfluenceImporter importer = new Confluence7Importer(properties);

        var transfer = new Transfer(extractor, importer, 786446, 124585130);
        transfer.execute();
    }

    /**
     * @param extractor Confluence extractor
     * @param importer Confluence importer
     * @param sourcePageRoot Id of page from input system (tree starting with this page will be transferred)
     * @param targetPageRoot Id of existing page in output system (transferred pagetree will be descendant of this page)
     */
    public Transfer(ConfluenceExtractor extractor,
                    ConfluenceImporter importer,
                    Integer sourcePageRoot,
                    Integer targetPageRoot) {
        this.extractor = extractor;
        this.importer = importer;
        this.sourcePageRoot = sourcePageRoot;
        this.targetPageRoot = targetPageRoot;
    }

    /**
     * Executes transfer whole pagetree from input system (old version Confluence) to output system (newer version Confluence)
     */
    public void execute() {
        // lets parse whole page tree
        ConfluencePage pageTreeRoot = extractor.extractPageTree(sourcePageRoot);
        // lets create whole parsed page tree
        importer.createPageTree(pageTreeRoot, targetPageRoot);
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
