package se.serp.LiULunch;

import java.util.Calendar;

public class DayMenu {
	Calendar date;
	RestaurantDayMenu[] restaurantDayMenus;
	
	public DayMenu(Calendar date, RestaurantDayMenu[] restaurantDayMenus) {
		this.date = date;
		this.restaurantDayMenus = restaurantDayMenus;
	}
}
