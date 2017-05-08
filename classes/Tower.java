/**
 * @author Spencer Brydges
 * Main tower class for client.
 * program extends sprite class for specific tower objects and contain
 * properties specific to said towers (ranges, cost, damage, etc)
 */
package classes;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Tower extends Sprite
{
	private static double towerDamage;
	private int towerName;
	private double towerCost;
	private int towerRange;
	private double towerHealth;
	private String towerImage;
	private boolean isFiring;
	
	public Tower(double tDamage, double tHealth, double tCost, int tRange, int tName, String tImage, int x, int y)
	{
		super(tImage, x, y, tName);
		towerName = tName;
		towerDamage = tDamage;
		towerCost = tCost;
		towerRange = tRange;
		towerHealth = tHealth;
		towerImage = tImage;
		isFiring = false;
	}
	
	public double towerDamage()
	{
		return towerDamage;
	}
	
	public int getTowerRange()
	{
		return towerRange;
	}
	
	public int getY()
	{
		return super.getY();
	}
	
	public int getX()
	{
		return super.getX();
	}
	
	public void takeHit(double damage)
	{
		towerHealth -= damage;
	}
	
	public double getHealth()
	{
		return towerHealth;
	}
	
	public Rectangle getRegion()
	{
		return super.getRegion();
	}
	
	public Rectangle getRangeRegion()
	{
		int midX = getCenterX();
		int midY = getCenterY();
		return new Rectangle(getX() - getTowerRange(), getY() - getTowerRange(), getWidth() + 2*getTowerRange(), getHeight() + 2*getTowerRange());
	}
	
	public void setFire(boolean set)
	{
		isFiring = set;
	}
	
	public boolean isFiring()
	{
		return isFiring;
	}
}

