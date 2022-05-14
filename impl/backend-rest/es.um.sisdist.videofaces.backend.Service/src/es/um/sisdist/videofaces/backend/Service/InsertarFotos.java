package es.um.sisdist.videofaces.backend.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;

public class InsertarFotos {

	public static void main(String[] args) throws IOException {
		AppLogicImpl impl = AppLogicImpl.getInstance();
		impl.deletePhotos();
		impl.deleteVideos();
		
		File folder = new File("photos");
//		File[] listOfFiles = folder.listFiles();
//
//		for (int i = 0; i < listOfFiles.length; i++) {
//			if (listOfFiles[i].isFile()) {
//				byte[] photodata;
//				try {
//					photodata = Files.readAllBytes(listOfFiles[i].toPath());
//					impl.savePhoto("0", photodata);
////					listOfFiles[i].delete();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
		
		impl.printPhotos();
		impl.printVideos();
	}

}
