package es.um.sisdist.videofaces.backend.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
//		impl.deleteUsers();

		ClientConfig config = new ClientConfig();
//		config.property(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		ClientBuilder.newBuilder().register(JacksonFeature.class);
		Client client = ClientBuilder.newClient(config);
		WebTarget service = client.target(getBaseURI());
		// Fluent interfaces

		// Registrar usuario
//		String uid = registerUser(service);

		// Mandar un vídeo

		// Comprobar si el vídeo está subido
//		if (checkVideo(service, "0")) {
//			System.out.println("Se ha procesado");
//		} else {
//			System.out.println("Se está procesando");
//		}

		// Consulta de las caras
		getFaces(service, "0", "0");

	}

	private static void getFaces(WebTarget service, String uid, String vid) {
		Response faces = service.path("users/" + uid + "/video/" + vid).request().get();
		byte[] b = faces.readEntity(byte[].class);
//		System.out.println(s);
		String s = stringFromBytes(b);
		String[] trozos = s.split("\",\"");
		for (int i = 0; i < trozos.length; i++) {
			byte[] bytes = trozos[i].getBytes();
			try {
				FileOutputStream fos = new FileOutputStream("photos/f" + i + ".jpg");
				fos.write(bytes);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String stringFromBytes(byte byteData[]) {
		char charData[] = new char[byteData.length];
		for (int i = 0; i < charData.length; i++) {
			charData[i] = (char) (((int) byteData[i]) & 0xFF);
		}
		return new String(charData);
	}

	private static boolean checkVideo(WebTarget service, String vid) {
		Response checkVideo = service.path("users/" + vid + "/getVideo").request().get();
		if (checkVideo.readEntity(Map.class).get("status").equals("PROCESSED")) {
			return true;
		}
		return false;

	}

	private static String registerUser(WebTarget service) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("email", "juan@um.es");
		data.put("name", "juan");
		data.put("password", "1234");

		String register = service.path("register").request().post(Entity.json(data), String.class);

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

//    private static URI getBaseURI()
//    {
//        return UriBuilder.fromUri(
//                "http://localhost:8080/es.um.sisdist.RestTest").build();
//    }

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/Service").build();
	}
}