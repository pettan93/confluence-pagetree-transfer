package cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.importer;

import com.google.gson.Gson;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.exctractor.ConfluenceExtractor;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model.ConfluencePage;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model.CreatePageDTO;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Import pages to Confluence 7.7.3 using REST API using JSON format payload
 * Can be used recursively (along with page-children page relationships)
 * <p>
 * Confluence REST API docs
 * https://docs.atlassian.com/ConfluenceServer/rest/7.7.3/
 * Probably will work with any Confluence prior from version 4.x.x
 */
public class Confluence7Importer implements ConfluenceImporter {

    private static final Logger LOGGER = Logger.getLogger(Confluence7Importer.class.getName());

    private final Properties properties;

    /**
     * List of ids (input system) of pages, that we want skip
     */
    private final List<Integer> skipPages;

    public Confluence7Importer(Properties properties) {
        this.properties = properties;
        this.skipPages = Arrays.stream(((String) properties.get("output.skipPages"))
                .split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public void createPageTree(ConfluencePage sourceRootPage, Integer parentPageId) {
        if (skipPages.contains(sourceRootPage.sourcePageId)) return;
        modifySourcePageContent(sourceRootPage);
        Integer createdPageId = createPageInTargetSystem(sourceRootPage, parentPageId);
        if (sourceRootPage.children != null && !sourceRootPage.children.isEmpty()) {
            for (ConfluencePage child : sourceRootPage.children) {
                createPageTree(child, createdPageId);
            }
        }
        LOGGER.log(Level.INFO, "creating page tree completed, root page (input system id " +
                sourceRootPage + ") output system id :  " + createdPageId);
    }

    /**
     * Well, besides converting of newlines to html line break, there are some nontransferable illegal characters and
     * problems with converting old wiki markup to new Confluence XHTML-based format..
     * Lets ad-hoc fix all there problems here
     */
    private void modifySourcePageContent(ConfluencePage sourceRootPage) {
        sourceRootPage.wikiContent =
                sourceRootPage.wikiContent
                        .replaceAll("nbsp\\.", "nbsp;")
                        .replaceAll("&nbsp;", " ")
                        .replaceAll("&", "")
                        .replaceAll("<", "")
                        .replaceAll(">", "")
                        .replaceAll("\\*", "")
                        .replaceAll("\\n", "<br/>")
                        .replaceAll("\\r\\n", "")
                        .replaceAll("\\\\", "");
    }

    private Integer createPageInTargetSystem(ConfluencePage page, Integer parentPageId) {
        return Integer.valueOf(
                createPage(page, parentPageId).getId()
        );
    }

    public CreatePageDTO createPage(ConfluencePage p, Integer parentId) {
        LOGGER.log(Level.INFO, "creating page with source id " +
                p.sourcePageId + " to " + properties.getProperty("output.url"));

        HttpURLConnection conn = null;
        String resultJsonString = "";
        try {
            CreatePageDTO pageModel = new CreatePageDTO();
            pageModel.setType("page");
            pageModel.setTitle(p.title);
            pageModel.setSpaceKey(properties.getProperty("output.spacekey"));
            pageModel.setAncestors(String.valueOf(parentId));
            pageModel.setBodyValue(p.wikiContent);
            pageModel.setBodyRepresentation("storage");
            // hard-coded version 1 as we create new page with first content version
            pageModel.setVersionNumber(String.valueOf(1));

            // HTTP REST POST
            URL url = new URL(properties.getProperty("output.url") + "/rest/api/content/");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Basic " + Base64
                    .getEncoder()
                    .encodeToString(
                            (properties.getProperty("output.username") + ":" + properties.getProperty("output.pass"))
                                    .getBytes())
            );

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            OutputStream stream = conn.getOutputStream();

            stream.write(pageModel.toJson().getBytes(StandardCharsets.UTF_8));
            stream.close();

            resultJsonString = IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8);
            return new Gson().fromJson(resultJsonString, CreatePageDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                String error = IOUtils.toString(Objects.requireNonNull(conn).getErrorStream(), StandardCharsets.UTF_8);
                LOGGER.log(Level.INFO, error);
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            throw new IllegalStateException("Creating page with original id " + p.sourcePageId + " failed");
        }
    }


}
