package se.serp.LiULunch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MenuParser {
	private static Calendar parseDate(JSONObject jsonDate) throws JSONException
	{
		return new GregorianCalendar(jsonDate.getInt("year"), jsonDate.getInt("month") - 1, jsonDate.getInt("day"));
	}
	
	private static RestaurantDayMenu parseRestaurantDayMenu(JSONObject jsonRestaurantDayMenu) throws JSONException
	{
		String restaurantName = jsonRestaurantDayMenu.getString("name");
		ArrayList<Dish> dishList = new ArrayList<Dish>();
		
		JSONArray dishes = jsonRestaurantDayMenu.getJSONArray("dishes");
		for (int j = 0; j < dishes.length(); j++)
		{
			JSONObject dish = dishes.getJSONObject(j);
			String price = dish.getString("price");
			String dishName = dish.getString("name");
			dishList.add(new Dish(dishName, price));
		}
	
		return new RestaurantDayMenu(restaurantName, dishList.toArray(new Dish[0]));
	}
	
	private static DayMenu parseDayMenu(JSONObject jsonDayMenu) throws JSONException
	{
		ArrayList<RestaurantDayMenu> restaurantDayMenus = new ArrayList<RestaurantDayMenu>();
		
		Calendar date = parseDate(jsonDayMenu.getJSONObject("date"));
		JSONArray jsonRestaurantDayMenus = jsonDayMenu.getJSONArray("menus");
		for (int i = 0; i < jsonRestaurantDayMenus.length(); i++)
		{
			restaurantDayMenus.add(parseRestaurantDayMenu(jsonRestaurantDayMenus.getJSONObject(i)));
		}
		
		return new DayMenu(date, restaurantDayMenus.toArray(new RestaurantDayMenu[0]));
	}
	
	public static DayMenu[] parseDayMenus(JSONArray root) throws JSONException
	{
		ArrayList<DayMenu> dayMenus = new ArrayList<DayMenu>();
		for (int i = 0; i < root.length(); i++)
		{
			dayMenus.add(parseDayMenu(root.getJSONObject(i)));
		}
		return (DayMenu[])dayMenus.toArray(new DayMenu[0]);
	}
}
