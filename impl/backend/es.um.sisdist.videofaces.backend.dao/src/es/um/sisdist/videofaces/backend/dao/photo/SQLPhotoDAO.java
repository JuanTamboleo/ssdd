package es.um.sisdist.videofaces.backend.dao.photo;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import es.um.sisdist.videofaces.backend.dao.models.Photo;

public class SQLPhotoDAO implements IPhotoDao {
	Connection conn;

	public SQLPhotoDAO() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

			// Si el nombre del host se pasa por environment, se usa aquí.
			// Si no, se usa localhost. Esto permite configurarlo de forma
			// sencilla para cuando se ejecute en el contenedor, y a la vez
			// se pueden hacer pruebas locales
			Optional<String> sqlServerName = Optional.ofNullable(System.getenv("SQL_SERVER"));
			conn = DriverManager.getConnection(
					"jdbc:mysql://" + sqlServerName.orElse("localhost") + "/videofaces?user=root&password=root");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Optional<Photo> getPhotoById(String id) {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("SELECT * from faces WHERE id = ?");
			stm.setString(1, id);
			ResultSet result = stm.executeQuery();
			if (result.next()) {
				return CreatePhoto(result);
			}
		} catch (SQLException e) {

		}
		return Optional.empty();
	}

	private Optional<Photo> CreatePhoto(ResultSet result) {
		try {
			return Optional.of(new Photo(result.getString(1), result.getString(2)));
		} catch (SQLException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Photo> addPhoto(String vid, byte[] photo) {
		String queryID = "SELECT max(CAST(id AS UNSIGNED)) FROM faces";
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement(queryID);
			ResultSet result = stm.executeQuery();
			result.next();
			String id = result.getString(1) == null ? "0" : String.valueOf(Long.valueOf(result.getString(1)) + 1);
			String query = " insert into faces (id, videoid, imagedata) values (?, ?, ?)";

			stm = conn.prepareStatement(query);
			stm.setString(1, id);
			stm.setString(2, vid);
			Blob blob = new SerialBlob(photo);
			stm.setBlob(3, blob);
			stm.execute();
			return Optional.of(new Photo(id, vid));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
}
