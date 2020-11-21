package data;


public class Forum {

	private String _rid;
	private String id;
	private String ownerId;
	private String[] messageIds;

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

	public String[] getMessageIds() {
		return messageIds;
	}

	public void setMessages(String[] messagesIds) {
		this.messageIds = messagesIds;
	}

	@Override
	public String toString() {
		StringBuilder forumMessages = new StringBuilder();
		for(int i = 0; i< messageIds.length; i++){
			forumMessages.append("messageId="+ messageIds[i]);
			if (i < messageIds.length -1){
				forumMessages.append(",");
			}
		}
		return "Forum [_rid=" + _rid + ", id=" + id + ", owner_id=" + ownerId + ", messages=" + "[" + forumMessages.toString() + "]"+ "]";
	}
}
