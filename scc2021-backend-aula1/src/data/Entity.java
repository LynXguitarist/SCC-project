package data;

import java.util.Arrays;

public class Entity {
	private String _rid;
	private String id;
	private String name;
	private String description;
	private String[] mediaIds; // photo
	private String[] calendarIds;
	private int numberOfLikes;
	private String forumId;
	private boolean listed;
	private boolean liked;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String[] getMediaIds() {
		return mediaIds;
	}

	public void setMediaIds(String[] mediaIds) {
		this.mediaIds = mediaIds;
	}

	public String[] getCalendarIds() {
		return calendarIds;
	}

	public void setCalendarIds(String[] calendarIds) {
		this.calendarIds = calendarIds;
	}

	public boolean isListed() {
		return listed;
	}

	public void setListed(boolean listed) {
		this.listed = listed;
	}

	public int getNumberOfLikes() {
		return numberOfLikes;
	}

	public void setNumberOfLikes(int numberOfLikes) {
		this.numberOfLikes = numberOfLikes;
	}

	public String getForumId() {
		return forumId;
	}

	public void setForumId(String forumId) {
		this.forumId = forumId;
	}

	public boolean isLiked() {
		return liked;
	}

	public void setLiked(boolean liked) {
		this.liked = liked;
	}

	@Override
	public String toString() {
		return "Entity [_rid=" + _rid + ", id=" + id + ", name=" + name + ", description=" + description + ", mediaID="
				+ Arrays.toString(mediaIds) + ", calendarId=" + Arrays.toString(calendarIds) + ", listed=" + listed
				+ "]";
	}

}
