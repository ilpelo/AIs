package org.pelizzari.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	Connection con;
	
	public DBConnection() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://localhost:3306/ai";
		this.con = DriverManager.getConnection(url, "root", "mysql");
	}

	public Connection getCon() {
		return con;
	}

}
