package es.um.sisdist.videofaces.backend.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.videofaces.backend.dao.models.User;
import es.um.sisdist.videofaces.backend.dao.models.Video;
import es.um.sisdist.videofaces.models.UserDTO;
import es.um.sisdist.videofaces.models.UserDTOUtils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// POJO, no interface no extends

@Path("/users")
public class UsersEndpoint
{
    private AppLogicImpl impl = AppLogicImpl.getInstance();
    
    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUserInfo(@PathParam("username") String username)
    {
    	return UserDTOUtils.toDTO(impl.getUserByEmail(username).orElse(null));    	
    }
    
    @POST
	@Path("/{id}/{filename}/video")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addVideo(@PathParam("id") String userid, @PathParam("filename") String filename, InputStream inputStream) {
		System.out.println("Video recibido: " + filename);
		try {
			
			byte[] videodata = inputStream.readAllBytes();
			if(userid == null) {
				System.out.println("DSFAV");
			}
			else {
				System.out.println(userid);
			}
			
			Optional<Video> v = impl.saveVideo(userid, filename, videodata);
			if(v == null || !v.isPresent()) {
				System.out.println("Una desgracia");
			}
			else {
				System.out.println("Todo fresco: " + v.get().getFilename());
			}
			
			return Response.status(Response.Status.OK).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}
    
    @GET
    @Path("/{id}/consultvideos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVideosWithUserId(@PathParam("id") String userid) {
    	System.out.println("Videos solicitados");
    	
    	String json = "[";
    	List<Video> videos = impl.getVideosFromUser(userid);
    	for(Video vi : videos) {
    		json = json + "{\"filename\": \"" + vi.getFilename() + "\",\"id\": \"" + vi.getId() + "\"},";    		
		}
    	json = json.substring(0, json.length() - 1) + "]";
    	
		System.out.println("Vídeos: " + json);
    	
    	return Response.ok(json).build();
    }
    
}
