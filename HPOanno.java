package src;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class HPOanno {
	Connection connection;
	Statement statement;
	String className="org.sqlite.JDBC";
	String url="jdbc:sqlite:hpo_annotations.sqlite";
	
	public HPOanno() throws ClassNotFoundException, SQLException{
		Class.forName(className);
		connection = DriverManager.getConnection(url);
		connection.setAutoCommit(false);
	}
	
	public ArrayList<ArrayList<String>> research(String column, String id) throws SQLException{
		statement = connection.createStatement();
		String request="SELECT * FROM phenotype_annotation WHERE "+ column +" LIKE '%" + id + "%';";
		ResultSet results = statement.executeQuery(request);
		ResultSetMetaData metadata = results.getMetaData();
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		while (results.next()){
			ArrayList<String> array = new ArrayList<String>();
			for (int col=0; col<=metadata.getColumnCount();col++){
				array.add(results.getString(col));
			}
			result.add(array);
		}
		statement.close();
		results.close();
		return result;
		
	}
