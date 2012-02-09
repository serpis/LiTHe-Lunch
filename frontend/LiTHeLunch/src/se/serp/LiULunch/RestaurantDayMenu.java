package se.serp.LiULunch;

public class RestaurantDayMenu {
	String restaurantName;
	Dish[] dishes;
	
	RestaurantDayMenu(String restaurantName, Dish[] dishes) {
		this.restaurantName = restaurantName;
		this.dishes = dishes;
	}
}
