package cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.importer;

import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model.ConfluencePage;
import cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model.CreatePageDTO;

/**
 *
 */
public interface ConfluenceImporter {

    /**
     * Creates one page in remote system
     *
     * @param page         Page to create
     * @param parentPageId New page will be created as descendant of page with this id
     * @return Newly created page DTO
     */
    CreatePageDTO createPage(ConfluencePage page, Integer parentPageId);

    /**
     * Creates tree page hierarchy in remote system
     *
     * @param sourceRootPage Page to create which is also root page and can have descendant pages
     * @param parentPageId   Id of page in remote system which will be ancestor of page tree
     */
    void createPageTree(ConfluencePage sourceRootPage, Integer parentPageId);


}
