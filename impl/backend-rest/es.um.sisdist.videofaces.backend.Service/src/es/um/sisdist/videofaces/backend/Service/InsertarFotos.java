package es.um.sisdist.videofaces.backend.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;

public class InsertarFotos {

	public static void main(String[] args) throws IOException {
		AppLogicImpl impl = AppLogicImpl.getInstance();

		String path = "C:\\Users\\jtamb\\Desktop\\Trabajos\\Cuarto\\SSDD\\ssdd-cambiosGrpc0.1\\impl\\backend-grpc\\es.um.sisdist.videofaces.backend.grpc.GrpcServiceImpl\\videos\\tmp\\";
		for (int i = 10; i < 100; i++) {
			File file = new File(path+"img000"+i+".jpg");
			byte[] photodata = Files.readAllBytes(file.toPath());
			impl.savePhoto("1", photodata);
		}

	}

}
