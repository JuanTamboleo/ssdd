package es.um.sisdist.videofaces.backend.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

import es.um.sisdist.videofaces.backend.dao.DAOFactoryImpl;
import es.um.sisdist.videofaces.backend.dao.IDAOFactory;
import es.um.sisdist.videofaces.backend.dao.photo.IPhotoDao;

public class AppLogicImpl {
	IDAOFactory daoFactory;
	IPhotoDao photodao;

	private static final Logger logger = Logger.getLogger(AppLogicImpl.class.getName());

	static AppLogicImpl instance = new AppLogicImpl();

	private AppLogicImpl() {
		daoFactory = new DAOFactoryImpl();
		photodao = daoFactory.createSQLPhotoDao();
	}

	public void addAllPhotos(String videoID) {
		File folder = new File("photos");

		System.out.println(folder.getAbsolutePath());

		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getAbsolutePath().toString().contains(videoID + "-img")) {
				byte[] photodata;
				try {
					photodata = Files.readAllBytes(listOfFiles[i].toPath());
					photodao.addPhoto(videoID, photodata);
					listOfFiles[i].delete();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}
	
	public static AppLogicImpl getInstance() {
		return instance;
	}

}
