/**
 * 
 */
package es.um.sisdist.videofaces.backend.dao.user;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.Random;

import es.um.sisdist.videofaces.backend.dao.models.User;

/**
 * @author dsevilla
 *
 */
@SuppressWarnings("deprecation")
public class SQLUserDAO implements IUserDAO {
	private static String alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	Connection conn;

	public SQLUserDAO() {
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
	public Optional<User> getUserById(String id) {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("SELECT * from users WHERE id = ?");
			stm.setString(1, id);
			ResultSet result = stm.executeQuery();
			if (result.next())
				return createUser(result);
		} catch (SQLException e) {
			// Fallthrough
		}
		return Optional.empty();
	}

	@Override
	public Optional<User> getUserByEmail(String id) {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("SELECT * from users WHERE email = ?");
			stm.setString(1, id);
			ResultSet result = stm.executeQuery();
			if (result.next())
				return createUser(result);
		} catch (SQLException e) {
			// Fallthrough
		}
		return Optional.empty();
	}

	private Optional<User> createUser(ResultSet result) {
		try {
			return Optional.of(new User(result.getString(1), // id
					result.getString(2), // email
					result.getString(3), // pwhash
					result.getString(4), // name
					result.getString(5), // token
					result.getInt(6))); // visits
		} catch (SQLException e) {
			return Optional.empty();
		}
	}

	public void newVisit(String email) {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("UPDATE users SET visits = visits + 1 WHERE email = ?");
			stm.setString(1, email);
			stm.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Integer getVideos(String id) {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("SELECT count(*) from videos WHERE userid = ?");
			stm.setString(1, id);
			ResultSet result = stm.executeQuery();
			if (result.next()) {
				return result.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Optional<User> addUser(String email, String name, String password) {
		// Get the max ID

		String queryID = "SELECT max(CAST(id AS UNSIGNED)) FROM users";
		PreparedStatement preparedStmtID;
		try {
			preparedStmtID = conn.prepareStatement(queryID);
			ResultSet rs = preparedStmtID.executeQuery();
			rs.next();
			String id = rs.getString(1) == null ? "0" : String.valueOf(Long.valueOf(rs.getString(1)) + 1);
			System.out.println("-------->" + id);

			// the mysql insert statement
			String query = " insert into users (id, email, password_hash, name, token, visits)"
					+ " values (?, ?, ?, ?, ?, ?)";

			// create the mysql insert preparedstatement
			PreparedStatement preparedStmt;
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, id);
			preparedStmt.setString(2, email);
			preparedStmt.setString(3, User.md5pass(password));
			preparedStmt.setString(4, name);
			String token = "";
			for(int i = 0; i < 32; i++) {
				token = token + alphabet.charAt(new Random().nextInt(alphabet.length()));
            }
			preparedStmt.setString(5, token);
			preparedStmt.setInt(6, 0);
			preparedStmt.execute();
			return Optional.of(new User(id, email, User.md5pass(password), name, "TOKEN", 0));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	public void deleteUsers() {
		String query = "delete from users";
		PreparedStatement preparedStmt;
		try {
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void printUsers() {
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("select * from users");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
