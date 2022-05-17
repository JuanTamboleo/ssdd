package es.um.sisdist.videofaces.backend.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.tools.file.FileUtil;
import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.core.util.JacksonFeature;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.videofaces.backend.dao.models.Video;
import jakarta.json.Json;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;

public class TestClient {
	public static void main(String[] args) {
		AppLogicImpl impl = AppLogicImpl.getInstance();
		impl.deletePhotos();
		impl.deleteVideos();
		impl.deleteUsers();

		ClientConfig config = new ClientConfig();
//		config.property(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		ClientBuilder.newBuilder().register(JacksonFeature.class);
		Client client = ClientBuilder.newClient(config);
		WebTarget service = client.target(getBaseURI());
		// Fluent interfaces

		// Registrar usuario
		String uid = registerUser(service);

		// Mandar un vídeo
		sendVideo(service, uid);

		// Comprobar si el vídeo está subido
		checkVideo(service, "0");

		// Consulta de las caras
		getFaces(service, "0", "0");

	}

	private static String registerUser(WebTarget service) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("email", "juan@um.es");
		data.put("name", "juan");
		data.put("password", "1234");

		String register = service.path("register").request().post(Entity.json(data), String.class);
		System.out.println("Registrado usuario con:");

		String[] trozos = register.split("\"string\":\"");
		String uid = register.substring(register.indexOf(trozos[1]),
				register.indexOf(trozos[1]) + trozos[1].indexOf("\""));
		System.out.println("ID = " + uid);
		System.out.println("Name = " + register.substring(register.indexOf(trozos[2]),
				register.indexOf(trozos[2]) + trozos[2].indexOf("\"")));
		System.out.println("Email = " + register.substring(register.indexOf(trozos[3]),
				register.indexOf(trozos[3]) + trozos[3].indexOf("\"")));
		return uid;
	}

	private static void sendVideo(WebTarget service, String uid) {
		File file = new File("video/classroom.mp4");
		try {
			InputStream inputStream = new FileInputStream(file);
			Response video = service.path("users/" + uid + "/classroom.mp4/video").request()
					.post(Entity.entity(inputStream, MediaType.MULTIPART_FORM_DATA));
			System.out.println("\nStatus del envío:" + video.getStatus());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void checkVideo(WebTarget service, String vid) {
		Response checkVideo = service.path("users/" + vid + "/getVideo").request().get();
		while (checkVideo.readEntity(Map.class).get("status").equals("PROCESSING")) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("\nSe ha procesado");
	}

	private static void getFaces(WebTarget service, String uid, String vid) {
		Response faces = service.path("users/" + uid + "/video/" + vid).request().get();
		byte[] b = faces.readEntity(byte[].class);

		int aux = 0;
		int contador = 0;
		for (int i = 2; i < b.length; i++) {
			if (b[i - 2] == '"' && b[i - 1] == ',' && b[i] == '"') {
				byte[] baux = Arrays.copyOfRange(b, aux, i - 3);
				try {
					FileOutputStream fos = new FileOutputStream("photos/f" + contador + ".jpg");
					fos.write(baux);
				} catch (Exception e) {
					e.printStackTrace();
				}
				contador++;
				aux = i + 1;
			}
		}
		System.out.println("\nFotos recibidas, están en la carpetas photos");
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/rest").build();
	}

}