package data;

import java.util.Arrays;

public class ForumMessage {

    private String _rid;
    private String id;
    private String message;
    private String reply;

    public String get_rid() {
        return _rid;
    }

    public void set_rid(String _rid) {
        this._rid = _rid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    @Override
    public String toString() {
        return "ForumMessage: [_rid=" + _rid + ", id=" + id + ", message=" + message + ", reply=" + reply + "]";
    }
}
