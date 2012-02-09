package se.serp.LiULunch;

public class Versioning {
	public static final int MENU_VERSION = 1;
	public static final String CACHED_MENU_FILENAME = String.format("cached_menu%04d.txt", MENU_VERSION);
	public static final String MENU_URL = String.format("http://lunch.serp.se/menu%04d.txt", MENU_VERSION);
	//public static final String MENU_URL = "http://www.davidg.nu/~serp/gojs3.txt";
	
	public static final String PREFERENCES_FILENAME = String.format("preferences_file%04d", MENU_VERSION);
}
