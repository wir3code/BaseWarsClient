/**
 * @author Spencer Brydges
 * Class contains code necessary for communication between the client and the game server
 */
package classes;
import java.net.*;
import java.util.*;
import java.io.*;
import java.security.*;
import exception.DatabaseException;


public class Network //implements Runnable
{
	private static Socket conn;
	private static PrintWriter out;
	private static BufferedReader in;
	private static BufferedReader stdIn;
	private static String send;
	
	private static int timeout; //MS to connect to server
	
	public boolean connectToServer(String host, int portNum)
	{
		timeout = 5000;
		try
		{
			conn = new Socket();
			conn.connect(new InetSocketAddress(host, portNum), timeout);
			out = new PrintWriter(conn.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			stdIn = new BufferedReader(new InputStreamReader(System.in));
			send = "";
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	public void write(String data)
	{
		out.println(data);
	}
	
	public String read()
	{
		String resp = "";
		try
		{
			resp = in.readLine();
		}
		catch(IOException e)
		{
			
		}
		return resp.trim(); //Prevent nasty whitespace that occasionally gets mixed up in packet
	}
	
	public void testData()
	{
		try
		{
			while(true)
			{
				System.out.print("Send message to server: ");
				send = stdIn.readLine();
				out.println(send);
				System.out.println("Sent from server: " + in.readLine());
			}
		}
		catch(IOException e)
		{
			
		}
	}
	
}
