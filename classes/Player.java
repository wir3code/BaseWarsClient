/**
 * @author Spencer Brydges
 * Program works with the network class in order to perform player-specific functions such as spending money,
 * writing data to the socket, recording scores, loggin in etc.
 */
package classes;

import java.util.ArrayList;
import java.sql.*;
import java.awt.Point;

import exception.DatabaseException;

public class Player
{
	private static ArrayList<Tower> towers; //Holds all the active towers under player
	private static ArrayList<Unit> units; //Holds all the active units under player
	private static ObjectLoader loader; //For handling the loading of units/tower data
	private static Network n;
	
	private static double money;
	private static double health;
	private static String race;
	
	public Player(String rc, Network net)
	{
		money = 20000;
		health = 3000;
		units = new ArrayList<Unit>();
		towers = new ArrayList<Tower>();
		loader = new ObjectLoader();
		n = net;
	}
	
	public void buyTower(String towerName, int x, int y)
	{
		n.write("BLD " + towerName + " " + x + " " + y);
	}
	
	public void buyUnit(String unitName)
	{
		n.write("PUR " + unitName);
	}
	
	public void move(int unitIndex, int x, int y)
	{
		n.write("MOV " + unitIndex + " " + x + " " + y);
	}
	
	public void ready()
	{
		n.write("STR");
	}
	
	public double getMoney()
	{
		return money;
	}
	
	public double getHealth()
	{
		return health;
	}
	
	public void takeHit(double damage)
	{
		health -= damage;
	}
	
	public boolean spendMoney(double amount)
	{
		if(amount > money)
			return false;
		money -= amount;
			return true;
	}
	
	public void addMoney(double amount)
	{
		money += amount;
	}
	
	/**
	 * Method registers user on the server
	 * @param username Username to register
	 * @param passwd Password to use
	 * @param matchPasswd Match password
	 * @param email Player's email address
	 * @return True if registered successfully, false otherwise
	 */
	
	public boolean registerUser(String username, String passwd, String matchPasswd, String email) throws DatabaseException
	{
		return true;
	}
	
	public boolean loginUser(String username, String passwd)  throws DatabaseException
	{
		n.write("USR " + username);
		String resp = n.read(); //Just discard, can't do anything with acknowledgment response
		n.write("PWD " + passwd);
		resp = n.read();
		if(resp.equals("DWN"))
		{
			throw new DatabaseException("Fatal error: The database is down");
		}
		return (resp.equals("OKL")) ? true : false;
	}
	 
	 /**
	  * Method queries SQL server and retrieves the user's score.
	  * @return userScore Score obtained from SQL server 
	  */
	   
	public double getUserScore() throws DatabaseException
	{
		return -1;
	}
	
	/**
	  * Method queries SQL server and retrieves the user's rank.
	  * @return userRank rank obtained from SQL server 
	  */
	
	public int getUserRank() throws DatabaseException
	{
		return -1;
	}
	
	/**
	  * Method queries SQL server and retrieves the user's class.
	  * @return userClass class obtained from SQL server 
	  */
	
	public String getUserClass() throws DatabaseException
	{
		return "";
	}
	
	/**
	  * Method queries SQL server and retrieves # of wins the user has accumulated
	  * @return userWins wins obtained from SQL server 
	  */
	
	public int getUserWins() throws DatabaseException
	{
		return -1;
	}
	
	/**
	  * Method queries SQL server and retrieves # of losses the user has accumulated
	  * @return userLosses losses obtained from SQL server 
	  */

	public int getUserLosses() throws DatabaseException
	{
		return -1;
	} 
}	
