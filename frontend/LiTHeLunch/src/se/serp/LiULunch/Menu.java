package se.serp.LiULunch;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Menu {
	public String restaurantName;
	public DayMenu[] dayMenus;
	
	public Menu(String restaurantName, DayMenu[] dayMenus)
	{
		this.restaurantName = restaurantName;
		this.dayMenus = dayMenus;
	}
	
	public DayMenu getTodaysMenu()
	{
		Calendar now = new GregorianCalendar();
		Calendar today = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH)+1, now.get(Calendar.DATE));
		for (DayMenu dayMenu : this.dayMenus)
		{
			if (dayMenu.date.equals(today))
			{
				return dayMenu;
			}
		}
		return null;
	}
}
