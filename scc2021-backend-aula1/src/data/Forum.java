package data;

import java.util.Arrays;

public class Forum {

	private String _rid;
	private String id;
	private String owner_id;
	private ForumMessage[] messages;

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

	public String getOwner_id() {
		return owner_id;
	}

	public void setOwner_id(String owner_id) {
		this.owner_id = owner_id;
	}

	public ForumMessage[] getMessages() {
		return messages;
	}

	public void setMessages(ForumMessage[] messages) {
		this.messages = messages;
	}

	@Override
	public String toString() {
		StringBuilder forumMessages = new StringBuilder();
		for(int i = 0; i< messages.length; i++){
			forumMessages.append(messages[i].toString());
			if (i < messages.length -1){
				forumMessages.append(",");
			}
		}
		return "Forum [_rid=" + _rid + ", id=" + id + ", owner_id=" + owner_id + ", messages=" + "[" + forumMessages.toString() + "]"+ "]";
	}
}
