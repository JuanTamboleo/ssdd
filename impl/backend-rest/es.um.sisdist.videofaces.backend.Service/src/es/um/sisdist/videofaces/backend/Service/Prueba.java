package es.um.sisdist.videofaces.backend.Service;

import java.util.Optional;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.videofaces.backend.dao.models.User;
import es.um.sisdist.videofaces.models.UserDTO;
import es.um.sisdist.videofaces.models.UserDTOUtils;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/prueba")
public class Prueba {

	private AppLogicImpl impl = AppLogicImpl.getInstance();
	
	
	@GET
	@Path("/users")
	@Produces(MediaType.TEXT_HTML)
	public String getUserus() {
//		impl.deleteUsers();
		System.out.println("---------------------");
		impl.printUsers();
		System.out.println("---------------------");
		return "Hello AAAAA";
	}
	
	@GET
	@Path("/videos")
	@Produces(MediaType.TEXT_HTML)
	public String getVideos() {
//		impl.deleteUsers();
		System.out.println("---------------------");
		impl.printVideos();
		System.out.println("---------------------");
		return "Hello AAAAA";
	}
	
	@GET
	@Path("/photos")
	@Produces(MediaType.TEXT_HTML)
	public String getPhotos() {
//		impl.deleteUsers();
		System.out.println("---------------------");
		impl.printPhotos();
		System.out.println("---------------------");
		return "Hello AAAAA";
	}

//	@POST
//	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response postPrueba() {
//		JsonObject value = Json.createObjectBuilder()
//				.add("Nombre", "Ramón")
//				.build();
//		return Response.ok(value).build();
//	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerUser(UserDTO uo) {
		Optional<User> u = impl.checkLogin(uo.getEmail(), uo.getPassword());
		if (u.isPresent()) {
			System.out.println("----------\nUSUARIO YA CREADO\n---------");
			return Response.status(Status.FORBIDDEN).build();
		} else {
			Optional<User> ou = impl.register(uo.getEmail(), uo.getName(), uo.getPassword());
			System.out.println(ou.get().getEmail() + " - " + ou.get().getName() + " - " + ou.get().getPassword_hash()
					+ " - " + ou.get().getId());
			JsonObject value = Json.createObjectBuilder().add("id", ou.get().getId()).add("name", ou.get().getName())
					.add("email", ou.get().getEmail()).add("password", ou.get().getPassword_hash()).build();
			return Response.ok(value).status(Status.CREATED).build();
		}
	}

}
