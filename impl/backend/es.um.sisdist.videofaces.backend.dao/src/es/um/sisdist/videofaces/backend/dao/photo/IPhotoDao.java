package es.um.sisdist.videofaces.backend.dao.photo;

import java.util.List;
import java.util.Optional;

import es.um.sisdist.videofaces.backend.dao.models.Photo;
import es.um.sisdist.videofaces.backend.dao.models.Video;


public interface IPhotoDao {
	
	public Optional<Photo> getPhotoById(String id);
	
	public Optional<Photo> addPhoto(String vid, byte[] photo);
	
	public List<Photo> getPhotosFromVideo(String vid);
	
	public void printPhotos();
	
	public void deletePhotos();
}
