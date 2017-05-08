/**
 * @author Spencer Brydges
 * Program loads tower & unit data from a flat file database and parses the data accordingly.
 * The main driver program uses this data in order to create appropriate sprites
 */
package classes;
import java.io.*;

public class ObjectLoader
{
	
	/**
	 * Default constructor, needs no arguments.
	 */
	
	public ObjectLoader()
	{
		
	}
	
	/**
	 * Private helper method for trimming the contents array
	 * @return Array of trimmed size
	 */
	
	private String [] trimToSize(String [] oldArray)
	{
		int newSize = 0;
		while(oldArray[newSize++] != null);
		String [] newArray = new String[newSize];
		for(int i = 0; i < newSize; i++)
			newArray[i] = oldArray[i];
		return newArray;
	}
	
	/**
	 * Retrieves contents of file (change to MySQL DB)
	 * @param file File to load -- change to MySQL table string later..
	 * @return contents of file
	 */
	
	private String [] loadContents(String file)
	{
		BufferedReader rd;
		String [] ret = new String[200];
		int inc = 0;
		try
		{
			rd = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = rd.readLine()) != null)
			{
				ret[inc] = line;
				inc++;
			}
			rd.close();
		}
		catch(IOException e)
		{
			System.out.println("Failed to open file " + file + " for reading!");
			e.printStackTrace();
		}
		return trimToSize(ret);
	}
	
	/**
	 * Parses returned data and grabs objects available to player
	 * @param race Player's race
	 * @return Towers available to player's race
	 */
	
	public String [] loadAvailableTowers()
	{
		String [] towers = new String[200];
		String [] data = loadContents("C:\\BaseWars\\database.txt");
		int inc = 0;
		String [] line;
		boolean isTower = false;
		for(int i = 0; i < data.length-1; i++)
		{
			if(data[i].length() > 1)
			{
				line = data[i].split("\\s+");
				if(line[0].equals("Tower"))
				{
					isTower = true;
					i++;
					continue;
				}
				if(isTower)
				{
					towers[inc] = line[0] + ":" + line[1] + ":" + line[2] + ":" + line[3] +":" + line[4] + ":" + line[5];
					inc++;
				}
			}
		}
		return trimToSize(towers);
	}
	
	/**
	 * Parses returned data and grabs objects available to player
	 * @param race Player's race
	 * @return Towers available to player's race
	 */
	
	public String [] loadAvailableUnits()
	{
		String [] units = new String[200];
		String [] data = loadContents("C:\\BaseWars\\database.txt");
		int inc = 0;
		String [] line;
		boolean isUnit = false;
		for(int i = 0; i < data.length-1; i++)
		{
			if(data[i].length() > 1)
			{
				line = data[i].split("\\s+");
				if(line[0].equals("Tower"))
				{
					isUnit = false;
					break;
				}
				if(line[0].equals("Unit"))
				{
					isUnit = true;
					i++;
					continue;
				}
				if(isUnit)
				{
					units[inc] = line[0] + ":" + line[1] + ":" + line[2] + ":" + line[3] +":" + line[4] + ":" + line[5] + ":" + line[6];
					inc++;
				}
			}
		}
		return trimToSize(units);
	}
	
}