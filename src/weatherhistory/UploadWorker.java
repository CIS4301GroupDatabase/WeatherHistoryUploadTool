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
	private String regionName;
	
	public UploadWorker(Connection connection, File csv, int size, String region)
	{
		this.connection = connection;
		this.csvFile = csv;
		this.numLines = size;
		this.regionName = region;
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
					if (index == 1)
					{
						//weather_station table
						//query.executeUpdate("");
						
						//location table
						//query.executeUpdate("");
					}
					
					// dataArray[6] == report type (use for sorting)  
					
					// dataArray[0] == station ID
					// dataArray[1] == station name  
					// dataArray[3] == latitude  
					// dataArray[4] == longitude
					
					// dataArray[5] == date  (formated like this: 2007-10-01 00:53)
					// dataArray[10] == temperature 
					// dataArray[16] == humidity  
					// dataArray[17] == wind speed
					// dataArray[20] == pressure
					// dataArray[24] ==  rainfall
					
					// dataArray[35] == sunrise time (3 or 4 digit in. last 2 digits miniutes, first 2 hours)
					// dataArray[36] == sunset rime (3 or 4 digit in. last 2 digits miniutes, first 2 hours)
					// dataArray[26] == Max temperature
					// dataArray[27] == Min temperature
					// dataArray[28] == Average temperature
					// dataArray[38] == Daily rainfall
					// dataArray[41] == average pressure
					// dataArray[43] == average wind speed
					// dataArray[44] == peak wind speed
					// dataArray[46] == sustained wind speed
					
					// I then need to know the prober SQL commands to upload each piece of data
					
					
					// TODO when I grab the date and time, since they are in the same node, I must string.split them
					//dataArray[5].substring(beginIndex, endIndex)
					
					if (dataArray[6].contentEquals("FM-15") || dataArray[6].contentEquals("FM-16"))
					{
						// hourly_condition table
						// Set the hourly
						//query.executeUpdate("");
					}
					else if (dataArray[6].contentEquals("SOD"))
					{
						// daily averages, happens onces a day.
						// daily_condition table
						//query.executeUpdate("");
					}
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
