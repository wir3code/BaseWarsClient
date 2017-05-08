/**
 * @author Spencer Brydges
 * Main driver program for basewars games.
 * Program loads simple GUI and attempts to establish a connection to the game server.
 * Once a connection has been established, the program waits for another client to join
 * if multiplayer mode is on, otherwise game class is loaded right away.
 */
package basemain;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import classes.*;
import exception.*;


class globals
{
	public static boolean multiplayer;
	public static Network n;
	public static Player p;
}

class MainWindow extends JFrame
{
	private static JTextField username;
	private static JTextField password;
	private static JLabel userL;
	private static JLabel passL;
	private static JButton login;
	private static JPanel jMain;
	
	private static Player player;
	public static Network net;
	public static globals glob;
	private static Thread t;
	
	class DataListener implements Runnable 
	{
		public void run()
		{
			String resp = "";
			while((resp = globals.n.read()) != null) //Constantly read data from the socket until termination
			{
				String [] splitter = resp.split("\\s+");
				if(splitter[0].equals("GOK"))
				{
					startGame();
					break;
				}
			}
		}
	}
	
	class LoginListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			String user = username.getText();
			String pass = password.getText();
			try
			{
				if(globals.p.loginUser(user, pass))
				{
					waitForClients();
				}
				else
				{
					JOptionPane.showMessageDialog(jMain, "Login failed");
				}
			}
			catch(DatabaseException er)
			{
				JOptionPane.showMessageDialog(jMain, "Fatal error: The database is down");
			}
		}
	}
	
	public void waitForClients()
	{
		jMain.remove(userL);
		jMain.remove(username);
		jMain.remove(passL);
		jMain.remove(password);
		jMain.remove(login);
		jMain.revalidate();
		JLabel clientMsg = new JLabel("Logged in! Waiting for another client to join...");
		jMain.add(clientMsg);
		jMain.revalidate();
		if(!globals.multiplayer)
		{
			startGame();
		}
		else
		{
			t = new Thread(new DataListener());
			t.start();
		}
	}
	
	public void startGame()
	{
		remove(jMain);
		remove(this);
		setBackground(Color.BLACK);
		Network net = globals.n;
		Player player = globals.p;
		add(new GameMain(net, player));
	}
	
	public MainWindow()
	{
		if(!globals.n.connectToServer("142.51.24.219", 3393))
		{
			JOptionPane.showMessageDialog(this, "Fatal error: Failed to connect to server");
		}
		jMain = new JPanel();
		username = new JTextField(25);
		password = new JTextField(25);
		login = new JButton("Login");
		login.addActionListener(new LoginListener());
		userL = new JLabel("Username: ");
		passL = new JLabel("Password: ");
		jMain.add(userL);
		jMain.add(username);
		jMain.add(passL);
		jMain.add(password);
		jMain.add(login);
		add(jMain);
		//waitForClients();
		startGame();
	}
}

class main
{
	public static void main(String [] args)
	{
		globals glob = new globals();
		glob.multiplayer = true;
		glob.n= new Network();
		glob.p = new Player("Omega", glob.n);
		MainWindow w = new MainWindow();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		w.setSize(950, 700);
		w.setTitle("Base Wars");
		w.setVisible(true);
	}
}