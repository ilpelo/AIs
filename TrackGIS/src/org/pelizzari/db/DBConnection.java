package org.pelizzari.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	public static Connection con;

	public static Connection getCon() {
		if(con == null) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				String url = "jdbc:mysql://localhost:3306/ai";
				con = DriverManager.getConnection(url, "root", "mysql");
				//String url = "jdbc:mysql://tstatdata1.emsa.local:3306/ai";
				//con = DriverManager.getConnection(url, "pelizan", "ais");
			} catch (ClassNotFoundException | SQLException e) {
				System.err.println("Cannot make DB connection");
				e.printStackTrace();
				System.exit(-1);			}			
		}
		return con;
	}

}
