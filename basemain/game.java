/**
 * @author Spencer Brydges
 * Class contain primary code for game logic.
 * Necessary, previously initialized classes are used extensively to glue every piece of the game together.
 */

package basemain;
import java.util.ArrayList;

import java.io.*;
import java.nio.file.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import classes.*;
import exception.*;


class GameMain extends JPanel
{
	private static JPanel jMain;
	
	private static globals glob;
	
	private static int screen_width; //Holds width of client's screen
	private static int screen_height; //Holds height of client's screen
	
	private static Image player1Base; //Contains image of player 1's base
	private static Image player2Base; //Contains image of player 2's base
	
	private static ArrayList<Sprite> world; //Holds all objects specific to the map
	private static ArrayList<Sprite> clickAbles; //Contains clickable images (interface buttons + bases)
	private static ArrayList<Tower> towers; //Holds all the active towers for player 1
	private static ArrayList<Unit> units; //Holds all the active units for player 1
	private static ArrayList<Unit> enemies; //Holds all the active units for player 2
	private static ArrayList<Tower> enemyTowers; //Holds all the active units for player 2
	
	private static Player player; //Object for handling player-specific transactions
	private static Network net; //Object for handling communication to and from server
	private static ObjectLoader loader; //Handles the organization of sprites and their respective speeds, images, health, cost etc
	private static String [] towerDatabase; //Contains list of available towers for purchase
	private static String [] unitDatabase; //Contains list of available units for purchase
	
	private static boolean playerType; //True = player 1, false = player 2
	private static boolean loggedOn; //Determine if player is logged on before loading main window
	private static boolean waitForClients; //Determine if other players have joined
	
	private static boolean roundActive; //Determines if player #1 & player #2 have clicked start round
	private static boolean ready;
	private static boolean player2_ready;
	private static boolean whatPlayer; //True if player1, false otherwise
	private String [] data; //Holds map data (walls, regions etc)
	private Timer timer;
	
	private static Rectangle enemyRegion;
	private String message; //Holds status message to be displayed on interface
	private double healthContext; //Specifies what object to display health of (base, unit etc).
	
	private boolean leftDirection = false; //Set to true when user presses left key
    private boolean rightDirection = false; //Set to true when user presses right key
    private boolean upDirection = false; //Set to true when user presses up key
    private boolean downDirection = false; //Set to true when user presses down key
    
    private static int pos_x; //Holds X position for initial unit placement
    private static int pos_y; //Holds Y position for initial unit placement
    
	
	/**
	 * 
	 * Inner class for handling network reading
	 * Class implements Runnable so that an object may be created, allowing for a thread to be created
	 * This will allow for the client to be listening for new data passing through the socket 24/7
	 * (or until termination)
	 * Appropriate functions are then called based on the data being received and parsed
	 * Most important piece of code for the entire multiplayer infrastructure
	 * DO NOT MODIFY WHATSOEVER, ENTIRE GAME WILL BREAK
	 *
	 */
	
	class DataListener implements Runnable 
	{
		public void run()
		{
			String resp = "";
			while((resp = net.read()) != null && !resp.equals(".")) //Constantly read data from the socket until termination
			{
				 int playerNum = (whatPlayer) ? 1 : 2;
		         System.out.println("Data received:" + resp + " (received by " + playerNum + ")"); //Debugging purposes
		         String [] splitter = resp.split("\\s+"); //Split application message, determine what action was performed by other player
		         if(splitter[0].equals("BLD") && splitter.length > 3) //Message was passed by enemy, a tower was built
		         {
		        	 Point p = new Point(Integer.parseInt(splitter[2]), Integer.parseInt(splitter[3])); //Get point out of application message
		        	 buildEnemyTower(splitter[1], p); //Build tower as placed by enemy & display on screen
		         }
		         else if(splitter[0].equals("PL1"))
		         {
		        	 whatPlayer = true;
		         }
		         else if(splitter[0].equals("PL2"))
		         {
		        	 whatPlayer = false;
		         }
		         else if(splitter[0].equals("PUR")) //Messaged was passed by enemy, units were purchased
		         {
		        	 buyEnemyUnits(splitter[1]); //Add units purchased by the enemy to arraylist
		         }
		         else if(splitter[0].equals("MOV")) //Enemy moved their units, obtain position coordinates from message and redisplay units
		         {
		        	 moveEnemyUnit(Integer.parseInt(splitter[1]), Integer.parseInt(splitter[2]), Integer.parseInt(splitter[3]));
		         }
		         else if(splitter[0].equals("STR"))
		         {
		        	 System.out.println("Received confirmation: Player is ready to initiate round");
		        	 player2_ready = true;
		        	 if(ready)
		        	 {
		        		 startRound();
		        	 }
		         }
			}
		}
	}
	
	/**
	 * Define inner class for handling mouse hovers
	 */
	
	public class MotionListener extends JPanel implements MouseMotionListener 
	{
		private Point p;
		
		public void mouseMoved(MouseEvent e) 
		{
		    p = e.getPoint();
		    ArrayList<Rectangle> regions = getClickableRegions(); //Get clickables map regions
			int context = 0; //See what user clicked on
			
			//Begin by going through arraylist and seeing if the user hit a clickable region
			for(int i = 0; i < regions.size(); i++)
			{
				if(regions.get(i).contains(p)) //User clicked on a sprite, find out what sprite & execute appropriate action
				{
					context = clickAbles.get(i).getContext();
					break; //Save processing time...it is impossible to click on more than one region
				}
			}
			
			/*
			 * User hovered over a clickable region, update context message
			 */
			
			if(context != 0) 
			{
				switch(context)
				{
					case 1: //Player base was clicked, set health context to base object
						setMessage("Friendly Base");
						break;
					case 2:
						setMessage("Enemy Base");
						break;
					case 3:
						setMessage("Buy Basic Tower");
						break;
					case 4:
						setMessage("Buy Special Tower");
						break;
					case 5:
						setMessage("Buy Basic units (x5)");
						break;
					case 6:
						setMessage("Buy Special units (x1)");
						break;
					case 7:
						setMessage("Start Round");
						break;
				}
			}
		}
		
		//Only needed to complete class, doesn't need to be implemented
		
		public void mouseDragged(MouseEvent e) 
	    {
	    	
	    }
	}
	
	
	class MouseDetector extends MouseAdapter 
	{
		private int awaitNextAction = 0; //Determine if user clicked on a sprite to build towers. Check value next click and perform appropriate action
		private Point p; //Coordinates of where user clicked
	    
		public void mousePressed(MouseEvent e)
		{
			p = e.getPoint(); //Get coordinates of clicked area
			if(awaitNextAction != 0) //User clicked an event that required another click position (i.e, tower placement)
			{
				switch(awaitNextAction)
				{
					case 3:
						buildTower("TOWER_BASIC", p);
						break;
					case 4:
						buildTower("TOWER_SPECIAL", p);
						break;
				}
				awaitNextAction = 0;
			}
			
			ArrayList<Rectangle> regions = getClickableRegions(); //Get clickables map regions
			int context = 0; //See what user clicked on
			
			//Begin by going through arraylist and seeing if the user hit a clickable region
			for(int i = 0; i < regions.size(); i++)
			{
				if(regions.get(i).contains(p)) //User clicked on a sprite, find out what sprite & execute appropriate action
				{
					context = clickAbles.get(i).getContext();
					break; //Save processing time...it is impossible to click on more than one region
				}
			}
			
			if(context != 0 && !roundActive) //Can't click any interface buttons UNTIL round is over
			{
				switch(context)
				{
					case 1: //Player base was clicked, set health context to base object
						healthContext = player.getHealth();
						break;
					case 2: //Enemy base was clicked, set health context to base object
						break;
					case 3:
						awaitNextAction = 3;
						break;
					case 4:
						awaitNextAction = 4;
						break;
					case 5:
						buyUnits("UNIT_BASIC");
						break;
					case 6:
						buyUnits("UNIT_SPECIAL");
						break;
					case 7:
						ready = true;
						player.ready();
						if(ready && player2_ready)
						{	
							startRound();
						}
						break;
				}
			}
		}
	}
	
	public class KeyListener extends KeyAdapter 
	{
        public void keyPressed(KeyEvent e) 
        {
            int key = e.getKeyCode();
            if((key == KeyEvent.VK_LEFT) && (!rightDirection)) 
            {
                leftDirection = true;
                upDirection = false;
                downDirection = false;
            }

            if((key == KeyEvent.VK_RIGHT) && (!leftDirection)) 
            {
                rightDirection = true;
                upDirection = false;
                downDirection = false;
            }

            if((key == KeyEvent.VK_UP) && (!downDirection)) 
            {
                upDirection = true;
                rightDirection = false;
                leftDirection = false;
            }

            if((key == KeyEvent.VK_DOWN) && (!upDirection)) 
            {
                downDirection = true;
                rightDirection = false;
                leftDirection = false;
            }
            
            if(roundActive)
            {
            	for(int i = 0; i < units.size(); i++)
    			{
            		if(i == units.size()-1) //Need to move ALL the units and know when to reset movement vars
            			moveUnit(i, true);
            		else
            			moveUnit(i, false);
    			}
            	detectCollisions();
            	detectTowerRange();
            }
        }
    }
	
	public void actionPerformed(ActionEvent e) 
	{
		//Nothing needed here
    }
	
	public void startRound()
	{
		System.out.println("Round in progress");
		roundActive = true;
	}
	
	/**
	 * Method moves an enemy unit to the specified X,Y position
	 * @param unitIndex Unit index to move
	 * @param newX X coordinate to move unit to
	 * @param newY Y coordinate to move unit to
	 */
	
	public void moveEnemyUnit(int unitIndex, int newX, int newY)
	{
		if(unitIndex == enemies.size())
			unitIndex--;
		enemies.get(unitIndex).move(newX, newY);
		repaint();
	}
	
	/**
	 * Method handles individual unit objects and modifies their coordinates,
	 * effectively changing their position on screen
	 * @param u Unit object to be moved on screen
	 * @param reset Indicates whether movements variables are to be reset.
	 * Only set to true AFTER all the unit objects on screen have been moved,
	 * otherwise only unit 1 will be moved
	 */
	
	public void moveUnit(int unitIndex, boolean reset)
	{
		Unit u = units.get(unitIndex);
		int moveY = u.getY();
		int moveX = u.getX();
		int speed = u.getSpeed();
		
		if(leftDirection) //X decreases to the left
		{
			moveX -= speed;
		}
		if(rightDirection) //X increases to the left
		{
			moveX += speed;
		}
		if(downDirection) //Y increases down
		{
			moveY += speed;
		}
		if(upDirection) //Y decreases up
		{
			moveY -= speed;
		}
		
		//Move only if unit doesn't collide with walls or friendly units
		
		if(!isWall(u.getMoveRegion(moveX, moveY))) 
		{
			u.move(moveX, moveY);
			player.move(unitIndex, moveX, moveY);
		}
		
		if(reset) //Reset movement variables for next key press
		{
			leftDirection = false; 
			rightDirection = false;
			downDirection = false;
			upDirection = false;
		}
		repaint();
	}
	
	/**
	 * Method determines if an anticipated movement is going to collide with a wall
	 * @param coords Anticipated coordinates where object is going to be moved to
	 * @return True if there is a wall blocking movement, false otherwise
	 */
	
	public boolean isWall(Rectangle coords)
	{
		boolean wall = false;
		Rectangle check;
		for(int i = 0; i < world.size(); i++) //Check coordinates of all wall objects on screen
		{
			check = world.get(i).getRegion(); //Get region of wall
			if(coords.intersects(check)) //See if anticipated movement will collide with wall
			{
				wall = true; //A wall was detected
				break; //No need to check for presence of other wall objects, unit cannot move as is
			}
		}
		return wall;
	}
	
	/**
	 * Method determines if an anticipated movement is going to collide with a friendly unit
	 * @param coords Anticipated coordinates where object is going to be moved to
	 * @return True if there is a unit blocking movement, false otherwise
	 */
	
	public boolean collidesFriendly(Rectangle coords)
	{
		boolean collision = false;
		Rectangle check;
		for(int i = 0; i < units.size(); i++) //Check coordinates of all friendly unit objects on screen
		{
			check = units.get(i).getRegion(); //Get region of wall
			if(coords.intersects(check)) //See if anticipated movement will collide with unit
			{
				collision = true; //A unit collision was detected
				break; //No need to check for presence of other unit objects, unit cannot move as is
			}
		}
		return collision;
	}
	
	public void setMessage(String msg)
	{
		message = msg;
		repaint();
	}
	
	/**
	 * Method determines regions of only clickable areas (regions for buttons + bases).
	 * @return ArrayList containing regions of only clickable regions
	 */
	
	public ArrayList<Rectangle> getClickableRegions()
	{
		ArrayList<Rectangle> regions = new ArrayList<Rectangle>();

		for(int i = 0; i < clickAbles.size(); i++) //Go through list of clickable sprites
		{
			regions.add(clickAbles.get(i).getRegion());
		}
		return regions;
	}
	
	/**
	 * Method determines regions of only towers areas
	 * @return ArrayList containing regions of only tower regions
	 */
	
	public ArrayList<Rectangle> getTowerRegions()
	{
		ArrayList<Rectangle> regions = getClickableRegions();
		for(int i = 0; i < towers.size(); i++)
		{
			regions.add(towers.get(i).getRegion());
		}
		return regions;
	}
	
	/**
	 * Method determines regions on ALL sprites and drawnAreas as towers cannot overlap set regions
	 * @return ArrayList containing regions of all drawn areas
	 */
	
	public ArrayList<Rectangle> getAllRegions()
	{
		ArrayList<Rectangle> regions = getClickableRegions(); //Reuse = good, repeat = bad

		for(int i = 0; i < clickAbles.size(); i++)
		{
			regions.add(clickAbles.get(i).getRegion());
		}
		
		for(int i = 0; i < towers.size(); i++)
		{
			regions.add(towers.get(i).getRegion());
		}
		
		for(int i = 0; i < world.size(); i++)
		{
			regions.add(world.get(i).getRegion());
		}
		return regions;
	}
	
	/**
	 * Method adds tower built by enemy to enemy sprite list
	 * & gets displayed on (this) client's screen
	 */
	
	public void buildEnemyTower(String tower, Point p)
	{
		int x = (int) p.getX(); //X coordinate to build tower on
		int y = (int) p.getY(); //Y coordinate to build tower on
		double cost = 0.;
		double health = 0.;
		double damage = 0.;
		int range = 0;
		String img = "";
		
		for(String line : towerDatabase)
		{
			if(line != null)
			{
				String [] split = line.split(":");
				if(split[0].equals(tower))
				{
					img = split[1];
					cost = Double.parseDouble(split[2]);
					health = Double.parseDouble(split[3]);
					damage = Double.parseDouble(split[4]);
					range = Integer.parseInt(split[5]);
				}
			}
		}
		enemyTowers.add(new Tower(damage, health, cost, range, 11, img, x, y));
		repaint(); //Repaint everything to show newly placed enemy tower
	}
	
	/**
	 * Method builds a new tower assuming player has enough money to purchase it
	 * @param tower Tower to build
	 * @param p Coordinates to build tower
	 */
	
	public void buildTower(String tower, Point p)
	{
		int x = (int) p.getX(); //X coordinate to build tower on
		int y = (int) p.getY(); //Y coordinate to build tower on
		int index = 0; //Keep track of sprite list size
		boolean canBuild = true; //Determine if user can build tower or not
		ArrayList<Rectangle> regions = getAllRegions();
		
		/*
		 * Look through database to get tower cost and image
		 */
		
		double cost = 0.;
		double health = 0.;
		double damage = 0.;
		int range = 0;
		String img = "";
		
		Rectangle buildRegion = new Rectangle(p.x, p.y, 40, 40);
		
		for(String line : towerDatabase)
		{
			if(line != null)
			{
				String [] split = line.split(":");
				if(split[0].equals(tower))
				{
					img = split[1];
					cost = Double.parseDouble(split[2]);
					health = Double.parseDouble(split[3]);
					damage = Double.parseDouble(split[4]);
					range = Integer.parseInt(split[5]);
				}
			}
		}
		
		if(player.getMoney() >= 500)
		{
			for(int i = 0; i < regions.size(); i++)
			{
				if(regions.get(i).intersects(buildRegion)) //User cannot build on region that already contains an object.
				{
					canBuild = false;
					break; //Save processing time
				}
			}
		}
		else
		{
			canBuild = false; //User lacks the resources to build tower
		}
		
		if(canBuild)
		{
			player.spendMoney(cost);
			player.buyTower(tower, x, y);
			towers.add(new Tower(damage, health, cost, range, 11, img, x, y));
			repaint();
		}
	}
	
	/**
	 * Method adds units purchased by the enemy to the enemy unit list
	 * @param unit Unit enemy purchased
	 */
	
	public void buyEnemyUnits(String unit)
	{
		int unit_qty = 0;
		String unit_img = "";
		double unit_health = 0.;
		int unit_speed = 0;
		double unit_cost = 0.;
		double unit_damage = 0.;
		
		for(String line : unitDatabase)
		{
			if(line != null)
			{
				String [] split = line.split(":");
				if(split[0].equals(unit))
				{
					unit_img = split[1];
					unit_health = Double.parseDouble(split[2]);
					unit_speed = Integer.parseInt(split[3]);
					unit_cost = Double.parseDouble(split[4]);
					unit_damage = Double.parseDouble(split[5]);
					unit_qty = Integer.parseInt(split[6]);
				}
			}
		}
		
		for(int i = 0; i < unit_qty; i++)
		{
			enemies.add(new Unit(unit_speed, unit_health, unit_cost, unit_damage, unit_img, 15, pos_x, pos_y));
		}
	}
	
	/**
	 * Method builds new unit assuming player can purchase them with money allocated
	 * @param unit Unit to purchase. Quantity is determined by unit supplied.
	 */
	
	public void buyUnits(String unit)
	{
		int unit_qty = 0;
		String unit_img = "";
		double unit_health = 0.;
		int unit_speed = 0;
		double unit_cost = 0.;
		double unit_damage = 0.;
		
		for(String line : unitDatabase)
		{
			if(line != null)
			{
				String [] split = line.split(":");
				if(split[0].equals(unit))
				{
					unit_img = split[1];
					unit_health = Double.parseDouble(split[2]);
					unit_speed = Integer.parseInt(split[3]);
					unit_cost = Double.parseDouble(split[4]);
					unit_damage = Double.parseDouble(split[5]);
					unit_qty = Integer.parseInt(split[6]);
				}
			}
		}
		
		if(player.getMoney() >= unit_cost)
		{
			player.spendMoney(unit_cost);
			player.buyUnit(unit);
			for(int i = 0; i < unit_qty; i++)
			{
				units.add(new Unit(unit_speed, unit_health, unit_cost, unit_damage, unit_img, 15, pos_x, pos_y));
				pos_x += units.get(i).getWidth()+4;
			}
		}
		else
		{
			setMessage("Insufficient funds");
		}
		repaint();
	}
	
	/**
	 * Method is routinely called after every action
	 * The unit ArrayList is scanned and the coordinates of every unit
	 * is compared against appropriate regions (enemy bases, walls, etc)
	 */
	
	public void detectCollisions()
	{
		Rectangle region;

		for(int i = 0; i < units.size(); i++)
		{
			region = units.get(i).getRegion();
			if(region.intersects(enemyRegion)) //Unit hit the enemy base
			{
				hitEnemy(i);
			}
			for(int k = 0; k < towers.size(); k++)
			{
				if(region.intersects(towers.get(k).getRegion()))
				{
					hitTower(i, k);
				}
			}
		}
	}
	
	/**
	 * Method is routinely called after every action as well
	 * The unit arraylist is once again compared to the enemy tower arraylist
	 * If a unit is within the range of an enemy tower, the tower
	 * will begin firing at the unit 
	 */
	
	public void detectTowerRange()
	{
		Rectangle rangeRegion;
		Rectangle unitRegion;
		boolean resetTower = true; //Determine if a tower previously firing has no more units in range
		boolean isSet = false; //Determine if a tower was previously firing
		for(int unitIndex = 0; unitIndex < units.size(); unitIndex++) //Scan through all units on map
		{
			unitRegion = units.get(unitIndex).getRegion();
			for(int towerIndex = 0; towerIndex < towers.size(); towerIndex++) //Scan all enemy towers on map
			{
				resetTower = true;
				isSet = false;
				rangeRegion = towers.get(towerIndex).getRangeRegion(); //Get firing region
				isSet = towers.get(towerIndex).isFiring(); //Was tower already firing?
				if(unitRegion.intersects(rangeRegion)) //Unit is within tower firing range
				{
					System.out.println("IN RANGE");
					if(isSet) //Tower was already firing, don't reset firing status
					{
						resetTower = false;
					}
					else //Tower has unit in range, set to fire
					{
						towers.get(towerIndex).setFire(true);
					}
				}
				if(resetTower && isSet) //Tower was previously firing and has no more units in range, reset
				{
					towers.get(towerIndex).setFire(false);
				}
			}
		}
	}
	
	public void hitEnemy(int unitIndex)
	{
		double damage = units.get(unitIndex).getDamage();
		player.takeHit(damage);
		units.remove(unitIndex);
	}
	
	public void hitTower(int unitIndex, int towerIndex)
	{
		System.out.println("Tower taking hit: " + enemyTowers.get(towerIndex).getHealth());
		double damage = units.get(unitIndex).getDamage();
		enemyTowers.get(towerIndex).takeHit(damage);
		if(enemyTowers.get(towerIndex).getHealth() <= 0)
		{
			enemyTowers.remove(towerIndex);
		}
	}
	
	/**
	 * Constructor method, initializes important game constants and initializes basic objects for
	 * gameplay
	 * @param n Network object passed from main class
	 * @param p Player object passed from main class
	 */
	
	public GameMain(Network n, Player p)
	{
		this.setFocusable(true);
		roundActive = false;
		message = "";
		net = n;
		player = p;
		healthContext = player.getHealth();
		Thread t = new Thread(new DataListener());
		if(glob.multiplayer)
		{
			t.start();
		}
		loggedOn = false; //Player has to login first
		loader = new ObjectLoader();
		addMouseListener(new MouseDetector());
		MotionListener myListener = new MotionListener();
	    addMouseMotionListener(myListener);
	    addKeyListener(new KeyListener());
		initObjects();
		//try { Thread.yield();} catch (Exception e) {}
	}
	
	/**
	 * Separate method for initializing objects. Loads 
	 * the objectLoader class, populates unit and tower databases and 
	 * creates the player's basic special unit
	 */
	
	public void initObjects()
	{
		int p1_x = 150;
		int p1_y = 450;
		int p2_x = 750;
		int p2_y = 450;
		if(whatPlayer) //Starting x,y coords are different for each player
		{
			pos_x = 150; //Starting unit placement x position
			pos_y = 450; //Starting unit placement y position
		}
		else
		{
			pos_x = 750; //Starting unit placement x position
			pos_y = 450; //Starting unit placement y position
		}
		loader = new ObjectLoader();
		towerDatabase = loader.loadAvailableTowers();
		unitDatabase = loader.loadAvailableUnits();
		towers = new ArrayList<Tower>();
		units = new ArrayList<Unit>();
		enemyTowers = new ArrayList<Tower>();
		enemies = new ArrayList<Unit>();
		units.add(new Unit(15, 100, 0, 200, "unit_special.png", 12, pos_x, pos_y));
		if(whatPlayer)
		{
			enemies.add(new Unit(15, 100, 0, 200, "unit_special.png", 12, p2_x, p2_y));
		}
		else
		{
			enemies.add(new Unit(15, 100, 0, 200, "unit_special.png", 12, p1_x, p1_y));
		}
		pos_x += units.get(0).getWidth()+1;
		data = new String[256];
		
		try
		{
			BufferedReader levelData = new BufferedReader(new FileReader("C:\\BaseWars\\level1.txt"));
			String line = "";
			int sz = 0;
			while((line = levelData.readLine()) != null)
			{
				data[sz] = line;
				sz++;
			}
			levelData.close();
		}
		catch(FileNotFoundException ex)
		{
			System.out.println("Fatal error: Map file not found, exiting...");
			System.exit(0);
		}
		catch(IOException e)
		{
			
		}
	}
	
	public void drawMissles()
	{
		Rectangle towerRegion;
		Unit closest;
		int min_x = 0;
		int min_y = 0;
		
		for(int i = 0; i < towers.size(); i++)
		{
			if(towers.get(i).isFiring())
			{
				towerRegion = towers.get(i).getRegion();
				for(int k = 0; k < units.size(); k++) //Find closest unit in range and fire at it
				{
					Rectangle uRegion = units.get(k).getRegion();
					if(k == 0)
					{
						min_x = uRegion.x;
						min_y = uRegion.y;
					}
					else
					{
						if(uRegion.x < min_x)
						{
							min_x = uRegion.x;
						}
						if(uRegion.y < min_y)
						{
							min_y = uRegion.y;
						}
					}
				}
				
				for(int k = 0; k < units.size(); k++)
				{
					Rectangle uRegion = units.get(k).getRegion();
					if(uRegion.x == min_x && uRegion.y == min_y)
					{
						units.get(k).takeHit(towers.get(i).towerDamage());
						if(units.get(k).getHealth() <= 0)
						{
							units.remove(k);
						}
					}
				}
			}
		}
	}
	
	public void drawMaze(Graphics2D g2)
	{		
		world = new ArrayList<Sprite>();
		data = new String[200];
		
		try
		{
			BufferedReader levelData = new BufferedReader(new FileReader("C:\\BaseWars\\level1.txt"));
			String line = "";
			int sz = 0;
			while((line = levelData.readLine()) != null)
			{
				data[sz] = line;
				sz++;
			}
			levelData.close();
		}
		catch(IOException e)
		{
			System.out.println("Fatal error: Failed to open map data!");
		}
		//Defines width of maze
		int width = 10;
		
		/*
		 * Following points reference initial maze position (player 1's base)
		 */
		
		int p_x = 120;
		int p_y = (screen_height/2)+30;
		
		/*
		 * The following points reference the final line position (player 2's base)
		 */
		
		int p_x_end = screen_width - 100; 
		int p_y_end = screen_height / 2;
		
		/*
		 * Define upper/lower maze boundaries, lines cannot go past these points
		 */
		int p_y_upper = screen_height-100;
		int p_y_lower = 100;
		
		//ImageIcon ii = new ImageIcon(this.getClass().getResource("wall_side.png"));
	    //Image img = ii.getImage();
	    //int iWidth = img.getWidth(null);
		
		int iter = 0;
	    for(String ln : data)
	    {
	    	if(ln != null)
	    	{
		    	for(int i = 0; i < ln.length(); i++)
		    	{
		    		char c = ln.charAt(i);
		    		if(c == '-')
		    		{
				    	world.add(new Sprite("wall_side.png", p_x, p_y, 10));
				    	g2.drawImage(world.get(iter).getImage(), world.get(iter).getX(), world.get(iter).getY(), null);
				    	p_x += world.get(iter).getWidth();
				    	iter++;
		    		}
		    		else if(c == '#')
		    		{
		    			world.add(new Sprite("wall_bottom.png", p_x, p_y, 10));
				    	g2.drawImage(world.get(iter).getImage(), world.get(iter).getX(), world.get(iter).getY(), null);
				    	p_x += world.get(iter).getWidth();
				    	iter++;
		    		}
		    		else //White space, increase x
		    		{
		    			p_x += 21;
		    		}
		    	}
	    	}
	    	p_x = 120;
	    	p_y += 20;
	    }
	}
	
	public void paintComponent(Graphics g) 
	{
		clickAbles = new ArrayList<Sprite>();
		//Erase entire board, prepare to repaint all visible and active objects on screen
		Rectangle r = this.getBounds();
		screen_height = r.height;
		screen_width = r.width;
		Graphics2D g2 = (Graphics2D)g;
		setBackground(Color.BLACK);
		Color textMain = Color.red;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Font font = new Font("Serif", Font.BOLD, 16);
		g2.setFont(font);
		g.setColor(Color.BLACK);
	    g.fillRect(0, 0, getWidth(), getHeight());
	    g2.setColor(textMain);
	    
	    g2.drawString("Money: " + player.getMoney(), 20, 15); 
	    g2.drawString("Health: " + healthContext, 20, 35);
	    g2.drawString("Units: " + units.size(), 20, 55);
	    g2.drawString("Status: " + message, 20, 75);
	    Graphics2D g2d = (Graphics2D) g;
	    
	    clickAbles.add(new Sprite("p1_base.png", 0, (screen_height/2), 1));
		player1Base = clickAbles.get(0).getImage();
	    g2d.drawImage(player1Base, 0, (screen_height/2), null); 
	    
	    clickAbles.add(new Sprite("p1_base.png", screen_width-130, screen_height/2, 2));
	    player2Base = clickAbles.get(1).getImage();
	    g2d.drawImage(player2Base, screen_width-130, (screen_height/2), null); 
	    
	    int ex = clickAbles.get(1).getX(); //Get initial X coordinate from sprite
		int ey = clickAbles.get(1).getY(); //Get initial Y coordinate from sprite
		int ewidth = clickAbles.get(1).getImage().getWidth(null); //Get width of sprite
		int eheight = clickAbles.get(1).getImage().getHeight(null); //Get height of sprite
		enemyRegion = new Rectangle(ex, ey, ewidth, eheight); //Add coordinates of rectangular region
	    
	    clickAbles.add(new Sprite("buy_tower_basic.png", screen_width/2-80, screen_height-50, 3));
	    g2d.drawImage(clickAbles.get(2).getImage(), screen_width/2-80, screen_height-50, null);
	    
	    clickAbles.add(new Sprite("buy_tower_special.png", screen_width/2-40, screen_height-50, 4));
	    g2d.drawImage(clickAbles.get(3).getImage(), screen_width/2-40, screen_height-50, null);
	    
	    clickAbles.add(new Sprite("buy_unit_basic.png", screen_width/2, screen_height-50, 5));
	    g2d.drawImage(clickAbles.get(4).getImage(), screen_width/2, screen_height-50, null);
	    
	    clickAbles.add(new Sprite("buy_unit_special.png", screen_width/2+40, screen_height-50, 6));
	    g2d.drawImage(clickAbles.get(5).getImage(), screen_width/2+40, screen_height-50, null);
	    
	    clickAbles.add(new Sprite("start_game.png", screen_width/2+80, screen_height-50, 7));
	    g2d.drawImage(clickAbles.get(6).getImage(), screen_width/2+80, screen_height-50, null);
	    
	    for(int i = 0; i < towers.size(); i++)
	    {
	    	g2d.drawImage(towers.get(i).getImage(), towers.get(i).getX(), towers.get(i).getY(), null);
	    	g2d.drawLine(towers.get(i).getRangeRegion().x, towers.get(i).getRangeRegion().y, towers.get(i).getRangeRegion().x + towers.get(i).getRangeRegion().width, towers.get(i).getRangeRegion().y + towers.get(i).getRangeRegion().height);
	    }
	    
	    for(int i = 0; i < enemyTowers.size(); i++)
	    {
	    	g2d.drawImage(enemyTowers.get(i).getImage(), enemyTowers.get(i).getX(), enemyTowers.get(i).getY(), null);
	    }
	    
	    if(roundActive) //Only draw units on screen if there's currently an active round
	    {
	    	if(units.size() == 0)
	    	{
	    		roundActive = false;
	    		repaint();
	    	}
	    	for(int i = 0; i < units.size(); i++)
	    	{
	    		g2d.drawImage(units.get(i).getImage(), units.get(i).getX(), units.get(i).getY(), null);
	    	}
	    	for(int i = 0; i < enemies.size(); i++)
	    	{
	    		g2d.drawImage(enemies.get(i).getImage(), enemies.get(i).getX(), enemies.get(i).getY(), null);
	    	}
	    }
	    message = "";
	    drawMaze(g2);
	    drawMissles();
	    
	    Toolkit.getDefaultToolkit().sync();
		g.dispose();
    }
}