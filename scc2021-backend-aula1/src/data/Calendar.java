package data;

import java.util.List;

public class Calendar {

	private String _rid;
	private String id;
	private String name;
	private String description;
	private String ownerId;

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

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	@Override
	public String toString() {
		return "Calendar [_rid=" + _rid + ", id=" + id + ", name=" + name + ", description=" + description
				+ ", ownerId=" + ownerId + "]";
	}

}
