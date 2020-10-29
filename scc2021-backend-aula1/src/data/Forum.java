package data;


public class Forum {

	private String _rid;
	private String id;
	private String ownerId;
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

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
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
		return "Forum [_rid=" + _rid + ", id=" + id + ", owner_id=" + ownerId + ", messages=" + "[" + forumMessages.toString() + "]"+ "]";
	}
}
