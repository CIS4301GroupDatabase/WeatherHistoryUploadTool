package weatherhistory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class UploadWorker extends SwingWorker<String, Integer>
{
	public JTextArea console;
	private Connection connection;
	private File csvFile;
	private int numLines;
	private String regionName;
	
	private Date startTime;
	private Date endTime;
	
	private int progress = 0;
	
	public UploadWorker(Connection connection, File csv, int size, String region, JTextArea con)
	{
		this.connection = connection;
		this.csvFile = csv;
		this.numLines = size;
		this.regionName = region;
		this.console = con;
	}

	@Override
	protected String doInBackground() throws Exception 
	{
		startTime = new Date();
		console.append("Started at time = " + startTime + "\n");
		String result = "Data has been successfully uploaded.";
		progress = 0;
		int stepSize = (int)((numLines * 1.0) / 50.0);
		Upload(0, stepSize);
		Upload(1, stepSize);
		endTime = new Date();
		console.append("Ended at time = " + endTime + "\n");
		double timeTakenMin = (endTime.getTime() - startTime.getTime())/ 60000.0; //
		console.append("Upload has taken " + timeTakenMin + " minutes.\n");
		console.append("Disconnecting from server. \n");
		connection.close();
		return result;
	}
	
    /** 			// dataArray[6] == report type (use for sorting)  
					
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
	 */
	public void Upload(int pass, int stepSize)
	{
		try (BufferedReader reader = new BufferedReader(new FileReader(csvFile)))
		{
			Statement query = connection.createStatement();
			String line = "";
			int index = 0;
			while ((line = reader.readLine()) != null)
			{
				String[] dataArray = line.split(",");
				if (index > 0)
				{
					if (index == 1 && pass == 0)
					{
						//weather_station table
						String station_table = "INSERT INTO weather_station VALUES ('" + dataArray[0] + "','" + dataArray[1] + "')";
						console.append(station_table + "\n");
						query.executeUpdate(station_table);
						
						//location table
						String location_table = "INSERT INTO location VALUES ('" + dataArray[0] + "', '" + dataArray[3] + "', '" + dataArray[4] + "', '" + regionName + "')";
						console.append(location_table + "\n");
						query.executeUpdate(location_table);
					}

					String date = dataArray[5].substring(0, 10);
					String time = dataArray[5];
					if (dataArray[6].contentEquals("SOD") && pass == 0)
					{
						// daily_condition table
						String sunrise = dataArray[35].substring(0, 2) + ":" + dataArray[35].substring(2, 4);
						String sunset = dataArray[36].substring(0, 2) + ":" + dataArray[36].substring(2, 4);
						String rainfall = dataArray[38];
						String avgWindSpeed = dataArray[43].replace("s", "");
						String peakWindSpeed = dataArray[44].replace("s", "");;
						String susWindSpeed = dataArray[46].replace("s", "");;
						if (rainfall.contains("T"))
							rainfall = "0.00";
						
						String daily_table = "INSERT INTO daily_condition VALUES ('" + dataArray[0] + "', TO_DATE('" + date + "', 'yyyy/mm/dd'), TO_DATE('" + sunset + "', 'HH24:MI'), TO_DATE('" + sunrise + "', 'HH24:MI'), "
								  + "'" + dataArray[28] + "', '" + dataArray[27] + "', '" + dataArray[26] + "', '" + rainfall + "', '" + dataArray[41] + "', "
								  + "'" + avgWindSpeed + "', '" + peakWindSpeed + "', '" + susWindSpeed + "')";
						console.append(daily_table + "\n");
						query.executeUpdate(daily_table);
					}
					
					if ((dataArray[6].contentEquals("FM-15") || dataArray[6].contentEquals("FM-16")) && pass == 1)
					{
						// hourly_condition table
						String rainfall = dataArray[24].replace("s", "");
						String windspeed = dataArray[17];
						String temperature = dataArray[10].replace("s", "");
						String pressure = dataArray[20].replace("s", "");
						if (windspeed.length() == 0)
							windspeed = "0";
						if (rainfall.length() == 0 || rainfall.contains("T"))
							rainfall = "0.00";
						String hourly_table = "INSERT INTO hourly_condition VALUES ('" + dataArray[0] + "', TO_DATE('" + date + "', 'yyyy/mm/dd'), '" + temperature + "', '" + rainfall + "', '" + windspeed + "', "
								  + "'" + dataArray[16] + "', '" + pressure + "', TO_DATE('" + time + "', 'yyyy/mm/dd:HH24:MI'))";
						console.append(hourly_table + "\n");
						query.executeUpdate(hourly_table);
					}
				}
				
				index++;
				if (index % stepSize == 0)
				{
					progress++;
					setProgress(progress);
				}
			}
			query.executeUpdate("COMMIT");
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			console.append(e.getMessage() + "\n");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			console.append(e.getMessage() + "\n");
		} catch (SQLException e) 
		{
			e.printStackTrace();
			console.append(e.getMessage() + "\n");
		} 
	}

}
