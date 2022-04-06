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
	@Produces(MediaType.TEXT_HTML)
	public String sayPlainTextHello() {
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
	public Response checkUser(UserDTO uo) {
		System.out.println(uo.getPassword());
		Optional<User> u = impl.checkLogin(uo.getEmail(), uo.getPassword());
		if (u.isPresent()) {
			System.out.println("----------\nUSUARIO YA CREADO\n---------");
			return Response.ok(UserDTOUtils.toDTO(u.get())).build();
		} else {
			Optional<User> ou = impl.register(uo.getEmail(), uo.getName(), uo.getPassword());
			System.out.println(uo.getEmail() + " - " + uo.getName() + " - " + uo.getPassword());
			
			JsonObject value = Json.createObjectBuilder().add("Nombre", uo.getName()).add("Mail", uo.getEmail())
					.add("Pass", uo.getPassword()).build();
			return Response.ok(value).build();
		}
	}

}
