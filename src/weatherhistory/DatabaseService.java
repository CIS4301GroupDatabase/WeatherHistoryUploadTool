package weatherhistory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JTextArea;


public class DatabaseService 
{
	public Config config;

	public DatabaseService(Config config)
	{
		this.config = config;
	}
	
	public Connection connectToDatabase() throws SQLException
	{
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		return DriverManager.getConnection(config.getDatabaseURL(), config.getName(), config.getPassword());
	}
	
	public void disconnectFromDatabase(Connection connect) throws SQLException
	{
		connect.close();
	}
}
