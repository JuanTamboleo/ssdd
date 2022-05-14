package es.um.sisdist.videofaces.backend.dao.video;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import es.um.sisdist.videofaces.backend.dao.models.User;
import es.um.sisdist.videofaces.backend.dao.models.Video;
import es.um.sisdist.videofaces.backend.dao.models.Video.PROCESS_STATUS;

public class SQLVideoDAO implements IVideoDAO {
	Connection conn;

	public SQLVideoDAO() {
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
	public Optional<Video> getVideoById(String id) {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("SELECT * from videos WHERE id = ?");
			stm.setString(1, id);
			ResultSet result = stm.executeQuery();
			if (result.next())
				return createVideo(result);
		} catch (SQLException e) {
			// Fallthrough
		}
		return Optional.empty();
	}

	private Optional<Video> createVideo(ResultSet result) {
		try {
			if (result.getInt(5) == 0) {
				return Optional.of(new Video(result.getString(1), // id
						result.getString(2), // userid
						Video.PROCESS_STATUS.PROCESSING, // pwhash
						result.getString(3), // date
						result.getString(4))); // filename;
			} else {
				return Optional.of(new Video(result.getString(1), // id
						result.getString(2), // userid
						Video.PROCESS_STATUS.PROCESSED, // pwhash
						result.getString(3), // date
						result.getString(4))); // filename;
			}
		} catch (SQLException e) {
			return Optional.empty();
		}
	}

	@Override
	public InputStream getStreamForVideo(String id) {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("SELECT * from videos WHERE id = ?");
			stm.setString(1, id);
			ResultSet result = stm.executeQuery();
			if (result.next())
				return result.getBlob(6).getBinaryStream();
		} catch (SQLException e) {
			// Fallthrough
		}
		return null;
	}

	@Override
	public PROCESS_STATUS getVideoStatus(String id) {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("SELECT * from videos WHERE id = ?");
			stm.setString(1, id);
			ResultSet result = stm.executeQuery();
			if (result.next()) {
				int aux = result.getInt(5);
				if (aux == 0) {
					return (PROCESS_STATUS.PROCESSING);
				} else {
					return (PROCESS_STATUS.PROCESSED);
				}
			}
		} catch (SQLException e) {
			// Fallthrough
		}
		return null;
	}

	@Override
	public Optional<Video> addVideo(String userid, String filename, byte[] videodata) {
		String queryID = "SELECT max(CAST(id AS UNSIGNED)) FROM videos";
		PreparedStatement preparedStmtID;
		try {
			preparedStmtID = conn.prepareStatement(queryID);
			ResultSet rs = preparedStmtID.executeQuery();
			rs.next();
			String id = rs.getString(1) == null ? "0" : String.valueOf(Long.valueOf(rs.getString(1)) + 1);
			System.out.println("-------->" + id);

			// the mysql insert statement
			String query = " insert into videos (id, userid, date, filename, process_status, videodata)"
					+ " values (?, ?, ?, ?, ?, ?)";

			// create the mysql insert preparedstatement
			PreparedStatement preparedStmt;
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, id);
			preparedStmt.setString(2, userid);

			Date date = Calendar.getInstance().getTime();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
			String fecha = dateFormat.format(date);
			preparedStmt.setString(3, fecha);
			preparedStmt.setString(4, filename);
			preparedStmt.setInt(5, 0); // Estatus, 0 para processing, 1 para processing

			Blob blob = new SerialBlob(videodata);
			preparedStmt.setBlob(6, blob);
			preparedStmt.execute();
			return Optional.of(new Video(id, userid, Video.PROCESS_STATUS.PROCESSING, fecha, filename));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	@Override
	public List<Video> getVideosFromUser(String userid) {
		List<Video> list = new LinkedList<Video>();
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("SELECT * from videos WHERE userid = ?");
			stm.setString(1, userid);
			ResultSet result = stm.executeQuery();
			while (result.next()) {
				list.add(createVideo(result).get());
			}
		} catch (SQLException e) {
		}
		return list;
	}

	@Override
	public void removeVideoWithId(String id) {
		System.out.println("Morango");
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("DELETE from videos WHERE id = ?");
			stm.setString(1, id);
			stm.executeUpdate();
		} catch (SQLException e) {
		}

	}

	@Override
	public void changeVideoStatus(String id) {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("UPDATE videos SET process_status = 1 WHERE id = ?");
			stm.setString(1, id);
			stm.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void printVideos() {
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("select id, userid, date, filename, process_status from videos");
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
	public void deleteVideos() {
		String query = "delete from videos";
		PreparedStatement preparedStmt;
		try {
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
