package es.um.sisdist.videofaces.backend.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.google.protobuf.ByteString;
import com.google.type.Date;
import com.google.type.TimeZone;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.videofaces.backend.dao.models.Photo;
import es.um.sisdist.videofaces.backend.dao.models.User;
import es.um.sisdist.videofaces.backend.dao.models.Video;
import es.um.sisdist.videofaces.backend.dao.models.Video.PROCESS_STATUS;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

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
		return value;
	}

	@POST
	@Path("/{id}/{filename}/video")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addVideo(@PathParam("id") String userid, @PathParam("filename") String filename,
			InputStream inputStream) {
		try {
			byte[] videodata = inputStream.readAllBytes();
			Optional<Video> v = impl.saveVideo(userid, filename, videodata);

			// GRPC
			impl.variosIDs(v.get().getId(), new ByteArrayInputStream(videodata));

			// Respuesta
			return Response.status(Response.Status.CREATED).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@POST
	@Path("/auth/{id}/{filename}/video")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVideo(@Context UriInfo uriInfo, @Context HttpHeaders httpheaders, @PathParam("id") String uid,
			@PathParam("filename") String filename, InputStream inputStream) {
		if (validateUser(httpheaders, uriInfo, uid)) {
			try {
				byte[] videodata = inputStream.readAllBytes();
				Optional<Video> v = impl.saveVideo(uid, filename, videodata);

				// GRPC
				impl.variosIDs(v.get().getId(), new ByteArrayInputStream(videodata));

				// Respuesta
				return Response.status(Response.Status.ACCEPTED)
						.location(new URI("http://localhost:8080/Service/auth/" + uid + "video/" + v.get().getId()))
						.build();
			} catch (Exception e) {
				e.printStackTrace();
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
		}
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}

	@GET
	@Path("/auth/{id}/video/{vid}")
	public Response videoAccess(@Context UriInfo uriInfo, @Context HttpHeaders httpheaders, @PathParam("id") String uid,
			@PathParam("vid") String vid) {
		if (validateUser(httpheaders, uriInfo, uid)) {
			if (impl.getVideoById(vid).get().getPstatus().equals(PROCESS_STATUS.PROCESSED)) {
				return Response.ok().entity("Est� procesado").type(MediaType.TEXT_PLAIN).build();
			} else {
				return Response.status(Response.Status.NO_CONTENT).entity("No est� procesado")
						.type(MediaType.TEXT_PLAIN).build();
			}
		}
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}

	private boolean validateUser(HttpHeaders httpheaders, UriInfo uriInfo, String uid) {
		if ((httpheaders.getRequestHeaders().get("user") != null) && (httpheaders.getRequestHeaders().get("date") != null)) {
			String md5Request = User
					.md5pass(uriInfo.getAbsolutePath() + httpheaders.getRequestHeaders().get("date").get(0)
							+ impl.getUserById(httpheaders.getRequestHeaders().get("user").get(0)).get().getTOKEN());

			if (httpheaders.getRequestHeaders().get("auth-token").toString().equals("[" + md5Request + "]")) {
				return true;
			}
		}
		return false;
	}

	@GET
	@Path("/{id}/video/{vid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadPhotos(@PathParam("id") String uid, @PathParam("vid") String vid) {
		var photos = impl.getPhotosFromVideos(vid);
		byte[] concat = new byte[0];
		Optional<Video> video = impl.getVideoById(vid);
		if (video.get().getPstatus().equals(PROCESS_STATUS.PROCESSED)) {
			for (Photo p : photos) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					baos.write(concat);
					baos.write(p.getData());
					baos.write("\",\"".getBytes());
					concat = baos.toByteArray();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		return Response.ok(concat).build();
	}

	@GET
	@Path("/{id}/consultvideos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVideosWithUserId(@PathParam("id") String userid) {

		String json = "[";
		List<Video> videos = impl.getVideosFromUser(userid);
		for (Video vi : videos) {
			json = json + "{\"filename\": \"" + vi.getFilename() + "\",\"id\": \"" + vi.getId() + "\"},";
		}
		if (json.length() > 1) {
			json = json.substring(0, json.length() - 1);
		}

		json = json + "]";

		return Response.ok(json).build();
	}

	@GET
	@Path("/{vid}/getVideo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVideoWithId(@PathParam("vid") String vid) {
		Optional<Video> video = impl.getVideoById(vid);
		String json = "{\"filename\": \"" + video.get().getFilename() + "\",\"id\": \"" + video.get().getId()
				+ "\",\"date\": \"" + video.get().getDate() + "\",\"status\": \"" + video.get().getPstatus() + "\"}";
		return Response.ok(json).build();
	}

	@GET
	@Path("/{id}/removeVideo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeVideoWithId(@PathParam("id") String id) {
		Optional<Video> video = impl.getVideoById(id);
		if (video.get().getPstatus().equals(PROCESS_STATUS.PROCESSED)) {
			impl.removeVideo(id);
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

}
