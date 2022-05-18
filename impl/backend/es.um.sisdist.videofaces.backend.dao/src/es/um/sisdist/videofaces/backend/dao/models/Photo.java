package es.um.sisdist.videofaces.backend.dao.models;

import java.sql.SQLException;


public class Photo {
	String id;
	String vid;
	byte[] data;

	public Photo(String id, String vid, java.sql.Blob blob) {
		super();
		this.id = id;
		this.vid = vid;
		try {
			this.data = blob.getBytes(1, (int) blob.length());
		} catch (SQLException e) {
			e.printStackTrace();
		}
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

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	
	
}
