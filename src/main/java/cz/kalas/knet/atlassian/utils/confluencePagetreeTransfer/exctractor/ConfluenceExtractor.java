package cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.exctractor;

import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model.ConfluencePage;

public interface ConfluenceExtractor {

    /**
     * Extracts tree page hierarchy from remote system
     *
     * @param pageId Id of root page
     * @return Page which is root of tree
     */
    ConfluencePage extractPageTree(Integer pageId);
}
