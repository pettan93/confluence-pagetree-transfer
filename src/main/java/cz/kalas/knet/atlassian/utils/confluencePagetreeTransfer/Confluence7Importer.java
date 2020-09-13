package cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer;

import com.google.gson.Gson;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model.ConfluencePage;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model.CreatePageDTO;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Confluence7Importer {

    private final Properties properties;

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
    }

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

    public Integer createPageInTargetSystem(ConfluencePage page, Integer parentPageId) {
        return Integer.valueOf(createPage(page, parentPageId).getId());

    }

    public CreatePageDTO createPage(ConfluencePage p, Integer parentId) {
        System.out.println("creating page with source id " + p.sourcePageId + " to " + properties.getProperty("output.url"));

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


        } catch (Exception e) {
            e.printStackTrace();

            try {
                String error = IOUtils.toString(Objects.requireNonNull(conn).getErrorStream(), StandardCharsets.UTF_8);
                System.out.println(error);
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return new Gson().fromJson(resultJsonString, CreatePageDTO.class);
    }


}
