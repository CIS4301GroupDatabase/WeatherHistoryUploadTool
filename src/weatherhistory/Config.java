package weatherhistory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;

public class Config 
{
	private String name = "";
	private String password = "";
	private String databaseURL = "jdbc:oracle:thin:hr/hr@oracle.cise.ufl.edu:1521:orcl";
	private File configFile;
	
	public Config() throws IOException
	{
		String configLocation = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		configLocation = URLDecoder.decode(configLocation, "UTF-8");
		configLocation = (new File(configLocation)).getParentFile().getPath();
		configLocation += "\\weatherhistory-config.txt";
		File configFile = new File(configLocation);
		this.configFile = configFile;
		if (configFile.exists())
		{
			getConfig(configFile);
		}
		else
		{
			createConfig(configFile);
		}
	}
	
	public void updateConfig()
	{
		// reset and update the config file with new values
		FileOutputStream stream;
		try 
		{
			configFile.delete();
			configFile.createNewFile();
			stream = new FileOutputStream(configFile);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(stream));
			out.write("DatabaseURL=" + getDatabaseURL());
			out.newLine();
			out.write("Name=" + getName());
			out.newLine();
			out.write("Password=" + getPassword());
			out.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

	}
	
	private void getConfig(File configFile)
	{
		// Load the config file
		try (BufferedReader reader = new BufferedReader(new FileReader(configFile)))
		{
			String line = "";
			while ((line = reader.readLine()) != null)
			{
				if (line.contains("DatabaseURL="))
				{
					setDatabaseURL(line.replace("DatabaseURL=", ""));
				}
				else if (line.contains("Name="))
				{
					setName(line.replace("Name=", ""));
				}
				else if (line.contains("Password="))
				{
					setPassword(line.replace("Password=", ""));
				}
			}
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void createConfig(File configFile) throws IOException
	{
		//create a new empty config file
		configFile.createNewFile();
		FileOutputStream stream = new FileOutputStream(configFile);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(stream));
		out.write("DatabaseURL=" + getDatabaseURL());
		out.newLine();
		out.write("Name=" + getName());
		out.newLine();
		out.write("Password=" + getPassword());
		out.close();
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public String getDatabaseURL()
	{
		return databaseURL;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public void setDatabaseURL(String URL)
	{
		this.databaseURL = URL;
	}
}
