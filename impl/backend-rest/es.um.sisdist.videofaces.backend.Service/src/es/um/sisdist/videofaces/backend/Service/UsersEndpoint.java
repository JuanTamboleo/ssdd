package es.um.sisdist.videofaces.backend.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.google.protobuf.ByteString;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.videofaces.backend.dao.models.User;
import es.um.sisdist.videofaces.backend.dao.models.Video;
import es.um.sisdist.videofaces.models.UserDTO;
import es.um.sisdist.videofaces.models.UserDTOUtils;
import jakarta.json.Json;
import jakarta.json.JsonObject;
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
public class UsersEndpoint {
	private AppLogicImpl impl = AppLogicImpl.getInstance();

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getUserInfo(@PathParam("id") String userid) {
		UserDTO uo = UserDTOUtils.toDTO(impl.getUserById(userid).orElse(null));
		JsonObject value = Json.createObjectBuilder().add("id", uo.getId()).add("email", uo.getEmail())
				.add("password", uo.getPassword()).add("name", uo.getName()).add("TOKEN", uo.getTOKEN())
				.add("visits", Integer.toString(uo.getVisits()))
				.add("videos", Integer.toString(impl.checkVideos(userid))).build();
		System.out.println(value);
		return value;
	}

//	@POST
//	@Path("/{id}/video")
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response video(@PathParam("id") String userid, InputStream inputStream) {
//		System.out.println("Se recive el vídeo");
//		try {
////			String path = "C:\\Users\\jtamb\\Desktop\\Trabajos\\Cuarto\\SSDD\\a"+userid+".mp4";
////			File file = new File(path);
////			Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
//			System.out.println("Completado almacenaje");
//			System.out.println("Mandando a Grpc");
////			System.out.println("Tamaño del inputStream " + inputStream.readAllBytes().length);
////			byte[] bytes = new byte[1024];
////			while ( (bytes = inputStream.readNBytes(1024)).length != 0) {
////				System.out.println("Buenas noches: " + bytes);
////			}
//			impl.variosIDs(userid, inputStream);
//			return Response.status(Response.Status.CREATED)
//					.header("Location", "/users/" + userid + "/video/" + new Random().nextInt(10)).build();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return Response.status(Response.Status.BAD_REQUEST).build();
//		}
//	}

	@POST
	@Path("/{id}/video")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addVideo(@PathParam("id") String userid, InputStream inputStream) {
		System.out.println("Video recibido");
		try {
			byte[] videodata = inputStream.readAllBytes();
			if (userid == null) {
				System.out.println("DSFAV");
			} else {
				System.out.println(userid);
			}

			Optional<Video> v = impl.saveVideo(userid, "Prueba", videodata);
			if (v == null || !v.isPresent()) {
				System.out.println("Una desgracia");
			} else {
				System.out.println("Todo fresco: " + v.get().getFilename());
			}
			// GRPC

			impl.variosIDs(userid, new ByteArrayInputStream(videodata));

			// Respuesta
			return Response.status(Response.Status.CREATED)
					.header("Location", "/users/" + userid + "/video/" + v.get().getId()).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path("/{id}/video/{vid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVideo(@PathParam("id") String uid, @PathParam("vid") String vid) {

		return null;
	}

	@GET
	@Path("/{id}/consultvideos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVideosWithUserId(@PathParam("id") String userid) {
		System.out.println("Videos solicitados");

		String json = "[";
		List<Video> videos = impl.getVideosFromUser(userid);
		for (Video vi : videos) {
			json = json + "{\"filename\": \"" + vi.getFilename() + "\",\"id\": \"" + vi.getId() + "\"},";
		}
		json = json.substring(0, json.length() - 1) + "]";

		System.out.println("Vídeos: " + json);

		return Response.ok(json).build();
	}

}
