package es.um.sisdist.videofaces.backend.dao.photo;

import java.util.Optional;

import es.um.sisdist.videofaces.backend.dao.models.Photo;


public interface IPhotoDao {
	
	public Optional<Photo> getPhotoById(String id);
	
	public Optional<Photo> addPhoto(String vid, byte[] photo);
}
