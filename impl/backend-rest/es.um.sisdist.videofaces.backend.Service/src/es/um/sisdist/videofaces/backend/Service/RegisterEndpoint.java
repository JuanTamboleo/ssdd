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

@Path("/register")
public class RegisterEndpoint {

	private AppLogicImpl impl = AppLogicImpl.getInstance();
	
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
