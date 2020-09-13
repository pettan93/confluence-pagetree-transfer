package cz.kalas.knet.atlassian.utils.confluencePagetreeTransfer.model;

import com.google.gson.Gson;

import java.util.Arrays;

public class CreatePageDTO implements JsonDTO {

    private String id;
    private String type;
    private String title;
    private ancestorObject[] ancestors = null;
    private spaceObject space = new spaceObject();
    private bodyObject body = new bodyObject();
    private versionObject version = new versionObject();

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

    public ancestorObject[] getAncestors() {
        return ancestors;
    }

    public void setAncestors(String ancestorId) {
        ancestorObject ao = new ancestorObject(ancestorId);
        this.ancestors = new ancestorObject[]{ao};
    }

    public spaceObject getSpace() {
        return space;
    }

    public void setSpace(spaceObject space) {
        this.space = space;
    }

    public bodyObject getBody() {
        return body;
    }

    public void setBody(bodyObject body) {
        this.body = body;
    }

    public versionObject getVersion() {
        return version;
    }

    public void setVersion(versionObject version) {
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

    class ancestorObject {

        public ancestorObject(String id) {
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

    class spaceObject {

        public String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

    }

    class bodyObject {

        public storageObject getStorage() {
            return storage;
        }

        public void setStorage(storageObject storage) {
            this.storage = storage;
        }

        public storageObject storage = new storageObject();
    }

    class storageObject {

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

    class versionObject {

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String number;
    }


}
