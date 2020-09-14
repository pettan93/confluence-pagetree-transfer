package cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model;

import com.google.gson.Gson;

import java.util.List;

/**
 * Represents page in input system
 * It is created by parsing xml from Confluence API
 */
public class ConfluencePage implements JsonDTO {

    public Integer sourcePageId;

    public String title;

    public String wikiContent;

    public List<ConfluencePage> children;

    public ConfluencePage() {
    }


    @Override
    public String toString() {
        return "Confluence3Page{" +
                "sourcePageId=" + sourcePageId +
                ", title='" + title + '\'' +
                ", wikiContent='" + (wikiContent != null ? wikiContent.length() : "") + '\'' +
                ", children=" + children +
                '}';
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

}
