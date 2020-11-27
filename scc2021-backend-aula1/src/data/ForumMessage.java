package data;

import java.time.LocalDateTime;
import java.util.Date;

public class ForumMessage {

    private String _rid;
    private String id;
    private String forumId;
    private LocalDateTime replyTime;
    private String sender;
    private String msg;
    private String reply;

    public String getForumId() {
        return forumId;
    }

    public void setForumId(String forumId) {
        this.forumId = forumId;
    }

    public LocalDateTime getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(LocalDateTime replyTime) {
        this.replyTime = replyTime;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) { this.reply = reply; }

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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ForumMessage [_rid=" + _rid + ", id=" + id + ", forumId=" + forumId + ", replyTime=" + replyTime
                + ", sender=" + sender + ", msg=" + msg + ", reply=" + reply + "]";
    }
}
