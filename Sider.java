package src;

import java.util.ArrayList;
import java.sql.*;

public class Sider {
	
	public String userName;
	public String psw;
	public String URL;
	
	public Sider(){
		userName="gmd-read";
		psw="esial";
		URL = "jdbc:mysql://neptune.telecomnancy.univ-lorraine.fr:3306/gmd";
	
	}
	
	public ArrayList<ArrayList<String>> research(String query, String table, String column) throws SQLException{
		Connection co = DriverManager.getConnection(URL, userName, psw);
		Statement statement = co.createStatement();
		String request = "SELECT * FROM" + table + "WHERE" + column + "LIKE \"%" + query + "%\"";
		statement.execute(request);
		ResultSetMetaData metadata = statement.getResultSet().getMetaData();
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		while(statement.getResultSet().next()){
			ArrayList<String> lineResult = new ArrayList<String>();
			for (int col=1; col<= metadata.getColumnCount(); col++){
				lineResult.add(statement.getResultSet().getString(col));
			}
			result.add(lineResult);
		}
		statement.close();
		co.close();
		return result;
		
	}

}
