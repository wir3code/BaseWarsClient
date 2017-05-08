/**
 * @author Spencer Brydges
 * Main sprite class for client.
 * Program manages sprite objects and their appropriate properties (regions etc).
 */
package classes;

import java.awt.*;
import javax.swing.ImageIcon;

public class Sprite
{
	private Image img; //Holds reference to sprite image
	private String file; //Location of sprite image
	protected int coordX; //X coord where sprite is drawn on screen
	protected int coordY; //Y coord where sprite is drawn on screen
	private int context; //Defines what sprite is (unit, tower, etc). Only used for CLICKABLE sprites
	
	public Sprite(String sprite, int x, int y, int c)
	{
		setSprite(sprite, x, y, c);
	}
	
	public void setSprite(String sprite, int x, int y, int c)
	{
		file = sprite;
		ImageIcon ii = new ImageIcon(this.getClass().getResource(file));
	    img = ii.getImage();
	    coordX = x;
	    coordY = y;
	    context = c;
	}
	
	public int getWidth()
	{
		return img.getWidth(null);
	}
	
	public int getHeight()
	{
		return img.getHeight(null);
	}
	
	public int getX()
	{
		return coordX;
	}
	
	public int getY()
	{
		return coordY;
	}
	
	public int getCenterX() //Used for determining range of a tower (starting from its center)
	{
		return (getX() + (getX() + getWidth())) / 2;
	}
	
	public int getCenterY() //Used for determining range of a tower (starting from its center)
	{
		return (getY() + (getY() + getHeight())) / 2;
	}
	
	public int getContext()
	{
		return context;
	}
	
	public Image getImage()
	{
		return img;
	}
	
	public Rectangle getRegion()
	{
		 return new Rectangle(coordX, coordY, getWidth(), getHeight());
	}
	
	public Rectangle getMoveRegion(int x, int y)
	{
		return new Rectangle(x, y, getWidth(), getHeight());
	}
}
