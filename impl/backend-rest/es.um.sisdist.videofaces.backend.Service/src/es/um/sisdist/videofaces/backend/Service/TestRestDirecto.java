package es.um.sisdist.videofaces.backend.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.core.util.JacksonFeature;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.videofaces.backend.dao.models.User;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

public class TestRestDirecto {

	static AppLogicImpl impl = AppLogicImpl.getInstance();
	
	public static void main(String[] args) {
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

		// Mandar un v�deo
		sendVideo(service, uid);

		// Comprobar si el v�deo est� subido
		checkVideo(service, "0", uid);

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
			
			java.util.TimeZone tc = java.util.TimeZone.getTimeZone("UTC");
	        var df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	        df.setTimeZone(tc);
			String date = df.format(new java.util.Date());	        
	        
	        String token = impl.getUserById(uid).get().getTOKEN();
	        
	        String auth_token = "http://localhost:8080/rest/users/auth/" + uid + "/classroom.mp4/video" + date + token;
	        	        
	        String md5 = User.md5pass(auth_token);
	        System.out.println(md5);

	        
			Response video = service.path("users/auth/" + uid + "/classroom.mp4/video").request().header("user", uid).header("date", date).header("auth-token", md5).post(Entity.entity(inputStream, MediaType.MULTIPART_FORM_DATA));
			System.out.println("\nStatus del env�o:" + video.getStatus());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void checkVideo(WebTarget service, String vid, String uid) {
		
		java.util.TimeZone tc = java.util.TimeZone.getTimeZone("UTC");
        var df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tc);
		String date = df.format(new java.util.Date());	        
        
        String token = impl.getUserById(uid).get().getTOKEN();
        
        String auth_token = "http://localhost:8080/rest/users/auth/" + uid + "/video/" + vid + date + token;
        	        
        String md5 = User.md5pass(auth_token);
        System.out.println(md5);
		
		Response checkVideo = service.path("/users/auth/" + uid + "/video/" + vid).request().header("user", uid).header("date", date).header("auth-token", md5).get();
		while (checkVideo.getStatus() != 200) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("\nSe ha procesado");
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/rest").build();
	}
	
}

