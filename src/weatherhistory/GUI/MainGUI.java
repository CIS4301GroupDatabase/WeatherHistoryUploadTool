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

public class MainGUI 
{
	public JTextArea console;
	public JProgressBar progressBar;
	public JFrame frame;
	private JTextField textField;
	private JButton btnUpload;
	
	private int numLines = 0;
	private File loadedFile = null;
	private Config config;
	private DatabaseService database;

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
		frame.setTitle("Weather History CSV Upload Tool");
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
								   + "                                            Created October 2017 \n"; 
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
			console.append("The config file has been updated.");
		}
	}
	
	public void uploadFileToDatabase()
	{
		if (this.loadedFile != null && this.loadedFile.exists())
		{
			console.append("Starting Upload Process. \n");
			try 
			{
				console.append("Connecting to database. \n");
				Connection connect = this.database.connectToDatabase();
				console.append("Connected. \n");
				console.append("Creating worker. \n");
				UploadWorker worker = new UploadWorker(connect, loadedFile, numLines);
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
								console.append("Disconnecting from server \n");
								try 
								{
									connect.close();
								} 
								catch (SQLException e) 
								{
									e.printStackTrace();
								}
								console.append("Uploaded Completed. \n");
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
				 
				 // TODO run the schema initilization here
				 
				 console.append("Schema Initilization Completed. \n");
				 connect.close();
			 }
			 catch (SQLException e) 
			 {
				 console.append("Connection to database failed. \n");
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
