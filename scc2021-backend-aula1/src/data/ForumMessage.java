package data;

import java.util.Arrays;
import java.util.Date;

public class  ForumMessage {

    private String _rid;
    private String id;
    private String entityId;
    private Date creationTime;
    private String fromWho;
    private String msg;
    private String replyToId;

    public String getEntityId() {
        return entityId;
    }
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
    public Date getCreationTime() {
        return creationTime;
    }
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
    public String getReplyToId() {
        return replyToId;
    }
    public void setReplyToId(String replyToId) {
        this.replyToId = replyToId;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public String get_rid() {
        return _rid;
    }
    public void set_rid(String _rid) {
        this._rid = _rid;
    }
    public String getFromWho() {
        return fromWho;
    }
    public void setFromWho(String fromWho) {
        this.fromWho = fromWho;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    @Override
    public String toString() {
        return "ForumMessage [_rid=" + _rid + ", id=" + id + ", entityId=" + entityId + ", creationTime=" + creationTime
                + ", fromWho=" + fromWho + ", msg=" + msg + ", replyToId=" + replyToId + "]";
    }
}
