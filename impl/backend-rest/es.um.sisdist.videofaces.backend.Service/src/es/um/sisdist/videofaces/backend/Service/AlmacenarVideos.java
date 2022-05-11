package es.um.sisdist.videofaces.backend.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.persistence.tools.file.FileUtil;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/sendvideos")
public class AlmacenarVideos {
	
	private AppLogicImpl impl = AppLogicImpl.getInstance();
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayPlainTextHello() {
		try {
			impl.variosIDs();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "Prueba Vídeos";
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addVideo(InputStream inputStream) {
		System.out.println("VAS NO?");
		try {
			String path = "C:\\Users\\jtamb\\Desktop\\Trabajos\\Cuarto\\SSDD\\a.mp4";
			File file = new File(path);
			Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
//			saveFile(inputStream, path);
			return Response.status(Response.Status.OK).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}
	
//	private void saveFile(InputStream inputStream, String path) {
//		try {
//			OutputStream outputStream = new FileOutputStream(new File(path));
//			int read = 0;
//			byte[] bytes = new byte[1024];
//			while((read = inputStream.read(bytes)) != -1) {
//				outputStream.write(bytes, 0, read);
//			}
//			outputStream.flush();
//			outputStream.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
