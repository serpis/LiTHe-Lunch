package se.serp.LiULunch;

public class Dish {
	public String name;
	public String price;
	
	public Dish(String name, String price)
	{
		this.name = name;
		this.price = price;
	}
	
	public String toString()
	{
		return this.name;
	}

}
