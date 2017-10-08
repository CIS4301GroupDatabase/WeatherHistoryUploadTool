package weatherhistory;

import java.awt.EventQueue;

import javax.swing.UIManager;

import weatherhistory.GUI.MainGUI;

public class Main 
{
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					Config config = new Config();
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					MainGUI window = new MainGUI(config);
					window.frame.setVisible(true);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}
}
