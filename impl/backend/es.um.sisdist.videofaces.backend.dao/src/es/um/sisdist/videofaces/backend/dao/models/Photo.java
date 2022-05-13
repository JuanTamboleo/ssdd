package es.um.sisdist.videofaces.backend.dao.models;

public class Photo {
	String id;
	String vid;

	public Photo() {

	}

	public Photo(String id, String vid) {
		super();
		this.id = id;
		this.vid = vid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVid() {
		return vid;
	}

	public void setVid(String vid) {
		this.vid = vid;
	}

}
