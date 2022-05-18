package es.um.sisdist.videofaces.backend.dao.photo;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import es.um.sisdist.videofaces.backend.dao.models.Photo;
import es.um.sisdist.videofaces.backend.dao.models.Video;

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
			return Optional.of(new Photo(result.getString(1), result.getString(2), result.getBlob(3)));
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
			return Optional.of(new Photo(id, vid, blob));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	@Override
	public List<Photo> getPhotosFromVideo(String vid) {
		List<Photo> list = new LinkedList<Photo>();
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("SELECT * from faces WHERE videoid  = ?");
			stm.setString(1, vid);
			ResultSet result = stm.executeQuery();
			while (result.next()) {
				list.add(CreatePhoto(result).get());
			}
		} catch (SQLException e) {
		}
		return list;
	}

	@Override
	public void printPhotos() {
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("select id, videoid from faces");
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			// Iterate through the data in the result set and display it.
			while (rs.next()) {
				// Print one row
				for (int i = 1; i <= columnsNumber; i++) {
					System.out.print(rs.getString(i) + " "); // Print one element of a row
				}
				System.out.println();// Move to the next line to print the next row.
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deletePhotos() {
		String query = "delete from faces";
		PreparedStatement preparedStmt;
		try {
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void deletePhotosFromVideo(String vid) {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("DELETE from faces WHERE videoid = ?");
			stm.setString(1, vid);
			stm.executeUpdate();
		} catch (SQLException e) {
		}
		
	}
}
