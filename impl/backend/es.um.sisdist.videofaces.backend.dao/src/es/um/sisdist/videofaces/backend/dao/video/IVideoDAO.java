package es.um.sisdist.videofaces.backend.dao.video;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import es.um.sisdist.videofaces.backend.dao.models.Video;

public interface IVideoDAO
{
    public Optional<Video> getVideoById(String id);

    // Get stream of video data
    public InputStream getStreamForVideo(String id);

    public Video.PROCESS_STATUS getVideoStatus(String id);
    
    public void changeVideoStatus(String id);
    
    public Optional<Video> addVideo(String userid, String filename, byte[] videodata);
    
    public List<Video> getVideosFromUser(String userid);
    
    public void removeVideoWithId(String id);
    
    public void printVideos();
    
    public void deleteVideos();
}
