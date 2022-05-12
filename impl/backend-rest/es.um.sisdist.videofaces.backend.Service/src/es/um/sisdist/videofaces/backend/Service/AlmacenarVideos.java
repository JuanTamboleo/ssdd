package es.um.sisdist.videofaces.backend.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.eclipse.persistence.tools.file.FileUtil;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/sendvideos")
public class AlmacenarVideos {
	
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayPlainTextHello() {
//		impl.deleteUsers();
		return "Hello AAAAA";
	}

	@POST
	@Path("/{id}/video")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addVideo(@PathParam("id") String userid, InputStream inputStream) {
		System.out.println("VAS NO?");
		try {
			String path = "C:\\Users\\Samuel\\Documents\\a.mp4";
			File auxfile = new File(path);
			if(!auxfile.exists()) {
				auxfile.createNewFile();
			}
									
			Files.copy(inputStream, auxfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
