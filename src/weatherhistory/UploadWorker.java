package weatherhistory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.SwingWorker;

public class UploadWorker extends SwingWorker<String, Integer>
{
	private Connection connection;
	private File csvFile;
	private int numLines;
	
	public UploadWorker(Connection connection, File csv, int size)
	{
		this.connection = connection;
		this.csvFile = csv;
		this.numLines = size;
	}

	@Override
	protected String doInBackground() throws Exception 
	{
		String result = "Data has been successfully uploaded.";
		UploadData();
		return result;
	}
	
	public void UploadData()
	{
		try (BufferedReader reader = new BufferedReader(new FileReader(csvFile)))
		{
			Statement query = connection.createStatement();
			String line = "";
			int index = 0;
			int progress = 0;
			int stepSize = (int)((numLines * 1.0) / 100.0);
			while ((line = reader.readLine()) != null)
			{
				String[] dataArray = line.split(",");
				if (index == 0)
				{
					// This is the indexing array. I probly don't even need this data?, not sure yet
				}
				else
				{
					// First datapoint starts here
					// I then need to know the prober SQL commands to upload each piece of data
					//query.executeUpdate("");
				}
				index++;
				if (index % stepSize == 0)
				{
					progress++;
					setProgress(progress);
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
		} catch (SQLException e) 
		{
			e.printStackTrace();
		} 
	}

}
