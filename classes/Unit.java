/**
 * @author Spencer Brydges
 * Main unit class for client.
 * program extends sprite class for specific unit objects and contain
 * properties specific to said units (speed, cost, damage, etc)
 */
package classes;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

public class Unit extends Sprite
{
	private int unitSpeed;
	private double unitHealth;
	private double unitCost;
	private double unitDamage;
	private String sprite;
	private int unitName;
	private Image image;
	
	private boolean isAlive;
	
	public Unit(int uS, double uH, double uC, double uD, String img, int uName, int x, int y)
	{
		super(img, x, y, uName);
		sprite = img;
		unitSpeed = uS;
		unitHealth = uH;
		unitCost = uC;
		unitDamage = uD;
		unitName = uName;
		ImageIcon ii = new ImageIcon(this.getClass().getResource(sprite));
	    image = ii.getImage();
	    isAlive = true;
	}
	
	public double getHealth()
	{
		return unitHealth;
	}
	
	public double getCost()
	{
		return unitCost;
	}
	
	public int getSpeed()
	{
		return unitSpeed;
	}
	
	public double getDamage()
	{
		return unitDamage;
	}
	
	public boolean takeHit(double dmg)
	{
		unitHealth -= dmg;
		return (unitHealth <= 0) ? true : false;
	}
	
	public void setX(int x)
	{
		coordX = x;
	}
	
	public void setY(int y)
	{
		coordY = y;
	}
	
	public void move(int x, int y)
	{
		setX(x);
		setY(y);
	}
	
	public int getY()
	{
		return super.getY();
	}
	
	public int getX()
	{
		return super.getX();
	}
	
	public Rectangle getRegion()
	{
		return super.getRegion();
	}
	
}
