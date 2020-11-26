package data;

import java.time.LocalDateTime;
import java.util.Arrays;

public class Entity {
	private String _rid;
	private String id;
	private String name;
	private String description;
	private String[] mediaIds; // photo
	private int numberOfLikes;
	private boolean isDeleted;
	// date of the deletion of the ENtity
	private LocalDateTime deletionDate;

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

	public int getNumberOfLikes() {
		return numberOfLikes;
	}

	/**
	 * inc can be 1(if it was liked) or -1(if lost a like)
	 * 
	 * @param inc
	 */
	public void setNumberOfLikes(int inc) {
		this.numberOfLikes += inc;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public LocalDateTime getDeletionDate() {
		return deletionDate;
	}

	public void setDeletionDate(LocalDateTime deletionDate) {
		this.deletionDate = deletionDate;
	}

	@Override
	public String toString() {
		return "Entity [_rid=" + _rid + ", id=" + id + ", name=" + name + ", description=" + description + ", mediaID="
				+ Arrays.toString(mediaIds) + ", number of likes=" + numberOfLikes + ", isDeleted=" + isDeleted
				+ ", deletionDate=" + deletionDate + "]";
	}

}
