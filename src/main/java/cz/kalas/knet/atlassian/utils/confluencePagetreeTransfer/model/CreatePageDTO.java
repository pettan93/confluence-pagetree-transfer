package cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model;

import com.google.gson.Gson;

import java.util.Arrays;

/**
 * Data payload for Confluence API. This serialized to JSON is input for creating page using Confluence REST API
 *
 * https://docs.atlassian.com/ConfluenceServer/rest/7.7.3/ - Confluence newest REST API reference
 * https://developer.atlassian.com/server/confluence/confluence-rest-api-examples/
 *
 */
public class CreatePageDTO implements JsonDTO {

    private String id;
    private String type;
    private String title;
    private AncestorObject[] ancestors = null;
    private SpaceObject space = new SpaceObject();
    private BodyObject body = new BodyObject();
    private VersionObject version = new VersionObject();

   public void setSpaceKey(String key) {
        this.space.setKey(key);
    }

    public void setBodyValue(String value) {
        this.body.getStorage().setValue(value);
    }

    public void setBodyRepresentation(String value) {
        this.body.getStorage().setRepresentation(value);
    }

    public void setVersionNumber(String value) {
        this.version.setNumber(value);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AncestorObject[] getAncestors() {
        return ancestors;
    }

    public void setAncestors(String ancestorId) {
        AncestorObject ao = new AncestorObject(ancestorId);
        this.ancestors = new AncestorObject[]{ao};
    }

    public SpaceObject getSpace() {
        return space;
    }

    public void setSpace(SpaceObject space) {
        this.space = space;
    }

    public BodyObject getBody() {
        return body;
    }

    public void setBody(BodyObject body) {
        this.body = body;
    }

    public VersionObject getVersion() {
        return version;
    }

    public void setVersion(VersionObject version) {
        this.version = version;
    }

    @Override
    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return "CreatePageDTO{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", ancestors=" + Arrays.toString(ancestors) +
                ", space=" + space +
                ", body=" + body +
                ", version=" + version +
                '}';
    }

    static class AncestorObject {

        public AncestorObject(String id) {
            this.id = id;
        }

        public String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    static class SpaceObject {

        public String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

    }

    static class BodyObject {

        public StorageObject getStorage() {
            return storage;
        }

        public void setStorage(StorageObject storage) {
            this.storage = storage;
        }

        public StorageObject storage = new StorageObject();
    }

    static class StorageObject {

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getRepresentation() {
            return representation;
        }

        public void setRepresentation(String representation) {
            this.representation = representation;
        }

        public String value;
        public String representation;
    }

    static class VersionObject {

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String number;
    }


}
