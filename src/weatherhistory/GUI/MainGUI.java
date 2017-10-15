package weatherhistory.GUI;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;

import weatherhistory.Config;
import weatherhistory.DatabaseService;
import weatherhistory.UploadWorker;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import java.awt.Dimension;

public class MainGUI 
{
	private final String version = "1.0";
	public JTextArea console;
	public JProgressBar progressBar;
	public JFrame frame;
	private JTextField textField;
	private JButton btnUpload;
	
	private int numLines = 0;
	private File loadedFile = null;
	private Config config;
	private DatabaseService database;
	
	private String regionName = "";

	/**
	 * Create the application.
	 */
	public MainGUI(Config config) 
	{
		this.config = config;
		this.database = new DatabaseService(this.config);
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() 
	{
		frame = new JFrame();
		frame.setTitle("Weather History CSV Upload Tool " + version);
		frame.setBounds(100, 100, 656, 440);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		textField = new JTextField();
		textField.addCaretListener(new CaretListener() 
		{
			public void caretUpdate(CaretEvent arg0) 
			{
				// Get the file when the textField changes. Doing it this way lets us just copy/paste the link to the file instead of using the file browser if we want.   
				File testFile = new File(textField.getText());
				if (testFile != null && testFile.exists() && testFile.getAbsolutePath().toLowerCase().contains(".csv"))
				{
					loadedFile = testFile;
					updateChosenFile();
					btnUpload.setEnabled(true);
				}
				else
				{
					btnUpload.setEnabled(false);
				}
			}
		});
		panel.add(textField);
		textField.setColumns(64);
		
		JButton btnGetFile = new JButton("Load Data File");
		btnGetFile.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				chooseFile();
				if (loadedFile != null)
				{
					openRegionDialog();
				}
			}
		});
		panel.add(btnGetFile);
		
		JPanel panel_1 = new JPanel();
		frame.getContentPane().add(panel_1, BorderLayout.SOUTH);
		
		btnUpload = new JButton("Upload to Database");
		btnUpload.setEnabled(false);
		btnUpload.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				uploadFileToDatabase();
			}
		});
		btnUpload.setHorizontalAlignment(SwingConstants.LEFT);
		panel_1.add(btnUpload);
		
		this.progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(350, 14));
		panel_1.add(this.progressBar);
		
		JPanel panel_2 = new JPanel();
		frame.getContentPane().add(panel_2, BorderLayout.WEST);
		
		JPanel panel_3 = new JPanel();
		frame.getContentPane().add(panel_3, BorderLayout.EAST);
		
		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JTextArea textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		this.console = textArea;
		
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				//exit program
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);
		
		JMenu mnDatabase = new JMenu("Database");
		menuBar.add(mnDatabase);
		
		JMenuItem mntmConfigure = new JMenuItem("Configure");
		mntmConfigure.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				openConfigDialog();
			}
		});
		mnDatabase.add(mntmConfigure);
		
		JMenuItem mntmInitilizeSchema = new JMenuItem("Initilize Schema");
		mntmInitilizeSchema.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				initilizeDatabaseSchema();
			}
		});
		mnDatabase.add(mntmInitilizeSchema);
		
		JMenu mnAbout = new JMenu("About");
		menuBar.add(mnAbout);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				String aboutString = "The Weather History CSV Upload Tool will load a set of weather data from \n"
								   + "https://www.ncdc.noaa.gov/cdo-web/datasets/LCD/stations/WBAN:12816/detail \n"
								   + "and upload the set of data to the CISE database with a preset schema \n" 
								   + "matching the weather data. \n"
								   + "\n"
								   + "Developed by Joseph D Sinclair in Group 1 as part of a semester long \n" 
								   + "project for the class CIS4301 at the University of Florida. \n "
								   + "                                            Created October 2017 \n"
								   + "                                                  Version " + version + " \n"; 
				JOptionPane.showMessageDialog(frame, aboutString, "About This Program", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		mnAbout.add(mntmAbout);
	}

	private void chooseFile()
	{
		this.btnUpload.setEnabled(false);
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Comma Seperated Value, .csv", "csv");
		fileChooser.setFileFilter(filter);
		if (fileChooser.showOpenDialog(textField) == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			if (file.exists() && file.getAbsolutePath().toLowerCase().contains(".csv"))
			{
				this.textField.setText(file.getAbsolutePath());
			}
		}
	}
	
	private void updateChosenFile()
	{
		try 
		{
			LineNumberReader lineReader = new LineNumberReader(new FileReader(this.loadedFile));
			String firstLine = lineReader.readLine();
			lineReader.skip(Long.MAX_VALUE);
			this.numLines = lineReader.getLineNumber() + 1;
			console.append("Loaded " + this.loadedFile.getName() + " which has " + numLines + " entries. \n");
			console.append("Data points in CSV are: \n " + firstLine + " \n");
			lineReader.close();
			this.btnUpload.setEnabled(true);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			console.append(this.loadedFile.getName() + " not found. \n");
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			console.append("Error reading " + this.loadedFile.getName() + " \n");
		}
	}

	private void openRegionDialog()
	{
		JTextField regionTextField = new JTextField(50);
		JPanel regionPanel = new JPanel();
		regionPanel.setLayout(new BoxLayout(regionPanel, BoxLayout.Y_AXIS));
		regionPanel.add(new JLabel("Enter the name of the region this data belongs to:"));
		regionPanel.add(regionTextField);

		int result = JOptionPane.showConfirmDialog(null, regionPanel,"Enter the region name", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION)
		{
			regionName = regionTextField.getText();
			console.append("Region name set to: " + regionName + "\n");
		}
	}
	
	private void openConfigDialog()
	{
		JTextField conURL = new JTextField(50);
		JTextField conName = new JTextField(50);
		JTextField conPass = new JTextField(50);
		conURL.setText(config.getDatabaseURL().replace("DatabaseURL=", ""));
		conName.setText(config.getName().replace("Name=", ""));
		conPass.setText(config.getPassword().replace("Password=", ""));
		JPanel configPanel = new JPanel();
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
		configPanel.add(new JLabel("Database URL:"));
		configPanel.add(conURL);
		configPanel.add(new JLabel("User Name:"));
		configPanel.add(conName);
		configPanel.add(new JLabel("Password:"));
		configPanel.add(conPass);
		
		int result = JOptionPane.showConfirmDialog(null, configPanel, "Configure Database URL, Login Name, and Password", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION)
		{
			config.setDatabaseURL(conURL.getText());
			config.setName(conName.getText());
			config.setPassword(conPass.getText());
			config.updateConfig();
			console.append("The config file has been updated. \n");
		}
	}
	
	public void uploadFileToDatabase()
	{
		if (this.loadedFile != null && this.loadedFile.exists())
		{
			btnUpload.setEnabled(false);
			console.append("Starting Upload Process. \n");
			try 
			{
				console.append("Connecting to database. \n");
				Connection connect = this.database.connectToDatabase();
				console.append("Connected. \n");
				console.append("Creating worker. \n");
				UploadWorker worker = new UploadWorker(connect, loadedFile, numLines, regionName, console);
				worker.addPropertyChangeListener(new PropertyChangeListener() 
				{	
					@Override
					public void propertyChange(PropertyChangeEvent evt) 
					{
						if (evt.getPropertyName().equals("progress"))
						{
							int progress = (Integer)evt.getNewValue();
							progressBar.setValue(progress);
							console.append("Upload is " + progress + "% complete \n");
							if (progress >= 100)
							{
								btnUpload.setEnabled(true);
							}
						}
						
					}
				});
				worker.execute();
			} 
			catch (SQLException e) 
			{
				console.append("Connection to database failed. \n");
				e.printStackTrace();
				console.append(e.getMessage() + "\n");
				btnUpload.setEnabled(true);
			}
		}
	}
	
	private void initilizeDatabaseSchema()
	{
		Object[] option = {"Yes", "No"};
		
		 int result = JOptionPane.showOptionDialog(frame, "Are you sure you want to initilize \n the database with the Weather History Schema? \n", 
				 "Confirm Database Schema Initilization", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, option, option[1]);
		 if (result == 0)
		 {
			 // clicked yes
			 console.append("Starting Schema Initilization Process. \n");
			 try 
			 {
				 console.append("Connecting to database. \n");
				 Connection connect = this.database.connectToDatabase();
				 console.append("Connected. \n");
				 Statement query = connect.createStatement();
				 
				 String station_table = "CREATE TABLE Weather_Station (id CHAR (20), name CHAR (255), "
						   + "PRIMARY KEY (id) )";
				 console.append(station_table + "\n");
				 query.executeUpdate(station_table);
				 console.append("Weather_Station table added. \n");
				 
				 String location_table = "CREATE TABLE Location (id CHAR (20), latitude FLOAT (10), longitude FLOAT (10), region CHAR (255), "
								       + "PRIMARY KEY (latitude, longitude), FOREIGN KEY (id) REFERENCES Weather_Station)";
				 console.append(location_table + "\n");
				 query.executeUpdate(location_table);
				 console.append("Location table added. \n");
				 
				 String daily_table = "CREATE TABLE Daily_Condition (id CHAR (20), conditon_date DATE, sunset_time DATE, sunrise_time DATE, avg_temperature FLOAT (5), min_temperature FLOAT (5), max_temperature FLOAT (5), "
		 				   + "total_precipitation FLOAT (5), avg_pressure FLOAT (5), avg_wind_speed FLOAT (5), peak_wind_speed FLOAT (5), sustained_wind_speed FLOAT (5), "
		 				   + "PRIMARY KEY (conditon_date ), FOREIGN KEY (id) REFERENCES Weather_Station)";
				 console.append(daily_table + "\n");
				 query.executeUpdate(daily_table);
				 console.append("Daily_Condition table added. \n");
				 
				 String hourly_table = "CREATE TABLE Hourly_Condition (id CHAR (20), condition_date DATE, temperature FLOAT (5), precipitation FLOAT (5), wind_speed FLOAT (5), humidity FLOAT (5), pressure FLOAT (5), time DATE, "
				 				   + "PRIMARY KEY (time), FOREIGN KEY (condition_date) REFERENCES Daily_Condition)";
				 console.append(hourly_table + "\n");
				 query.executeUpdate(hourly_table);
				 console.append("Hourly_Condition table added. \n");
				
				 console.append("Schema Initilization Completed. \n");
				 connect.close();
			 }
			 catch (SQLException e) 
			 {
				 console.append("Connection to database failed or something was wrong with the SQL commands. \n");
				 console.append(e.getMessage() + "\n");
				 e.printStackTrace();
			 }
		 }
	}
	
	public void updateProgressbar(int progress)
	{
		if (progress >= 0 && progress <= 100)
		{
			this.progressBar.setValue(progress);
		}
		else if (progress > 100)
		{
			this.progressBar.setValue(100);
		}
		else
		{
			this.progressBar.setValue(0);
		}
	}
	
	public void updateConsole(String string)
	{
		this.console.append(string);
	}
	
}
