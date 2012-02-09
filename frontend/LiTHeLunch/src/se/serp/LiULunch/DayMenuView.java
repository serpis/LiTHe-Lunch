package se.serp.LiULunch;

import java.util.Calendar;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.TabContentFactory;

public class DayMenuView extends LinearLayout {
	private static String weekDayString(Calendar date) {
		switch (date.get(Calendar.DAY_OF_WEEK))
		{
			case Calendar.MONDAY:
				return "Måndag";
			case Calendar.TUESDAY:
				return "Tisdag";
			case Calendar.WEDNESDAY:
				return "Onsdag";
			case Calendar.THURSDAY:
				return "Torsdag";
			case Calendar.FRIDAY:
				return "Fredag";
			case Calendar.SATURDAY:
				return "Lördag";
			case Calendar.SUNDAY:
			default:
				return "Söndag";
		}
	}
	
	private static String monthString(Calendar date) {
		switch (date.get(Calendar.MONTH))
		{
			case Calendar.JANUARY:
				return "Januari";
			case Calendar.FEBRUARY:
				return "Februari";
			case Calendar.MARCH:
				return "Mars";
			case Calendar.APRIL:
				return "April";
			case Calendar.MAY:
				return "Maj";
			case Calendar.JUNE:
				return "Juni";
			case Calendar.JULY:
				return "Juli";
			case Calendar.AUGUST:
				return "Augusti";
			case Calendar.SEPTEMBER:
				return "September";
			case Calendar.OCTOBER:
				return "Oktober";
			case Calendar.NOVEMBER:
				return "November";
			case Calendar.DECEMBER:
			default:
				return "December";
		}
	}
	
	private static String dateToString(Calendar date) {
		return weekDayString(date) + " " + date.get(Calendar.DAY_OF_MONTH) + " " + monthString(date).toLowerCase();
	}
	
	public DayMenu dayMenu;
	
	public DayMenuView(final Context context, final DayMenu dayMenu) {
		super(context);
		
		this.dayMenu = dayMenu;
		
		this.setOrientation(LinearLayout.VERTICAL);
		TextView dateLabel = new TextView(context);
		dateLabel.setText(dateToString(dayMenu.date));
		this.addView(dateLabel);
		
		TabHost tabHost = new TabHost(context);
		tabHost.setId(android.R.id.tabhost);
	
		// setup tab host
		{
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);
			tabHost.addView(layout);
			
			TabWidget widget = new TabWidget(context);
			widget.setId(android.R.id.tabs);
			layout.addView(widget);
			
			FrameLayout frameLayout = new FrameLayout(context);
			frameLayout.setId(android.R.id.tabcontent);
			
			
			layout.addView(frameLayout);
			
			tabHost.setup();
		}
		
		this.addView(tabHost);
		
//		listView.s
		
		
		// add a tab for every restaurant that serves food this day
		{
			TabHost.TabSpec spec;
			
			TabContentFactory contentFactory = new TabContentFactory() {
				public View createTabContent(String tag) {
					ListView listView = new ListView(context);
					listView.setId(android.R.id.list);
					listView.setItemsCanFocus(false);
					listView.setFocusable(false);
					//listView.setEnabled(false);
					//listView.setClickable(false);
					
					boolean foundRestaurant = false;
					for (RestaurantDayMenu restaurantDayMenu : dayMenu.restaurantDayMenus) {
						if (restaurantDayMenu.restaurantName.equals(tag)) {
							listView.setAdapter(new ArrayAdapter<Dish>(context,
									android.R.layout.simple_list_item_1, restaurantDayMenu.dishes){
								public boolean isEnabled(int position) {
									return false;
								}
							});
							foundRestaurant = true;
							break;
						}
					}
					
					if (!foundRestaurant)
					{
						TextView tv = new TextView(context);
						tv.setText("Hoppsan! Fick i uppdrag att visa " + tag + "s meny, men den verkar inte finnas.");
						return tv;
					}
	
					/*listView.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							// When clicked, show a toast with the TextView text
							Toast.makeText(context,
									"nom nom: " + ((TextView) view).getText(),
									Toast.LENGTH_SHORT).show();
						}
					});*/
					
					return listView;
				}
			};
			
			if (dayMenu.restaurantDayMenus.length == 0)
			{
				spec = tabHost.newTabSpec("(ingen restaurang)").setIndicator("(ingen restaurang)").setContent(contentFactory);
				tabHost.addTab(spec);
			}
			else
			{
				for (RestaurantDayMenu restaurantDayMenu : dayMenu.restaurantDayMenus)
				{
					spec = tabHost.newTabSpec(restaurantDayMenu.restaurantName).setIndicator(restaurantDayMenu.restaurantName).setContent(contentFactory);
					tabHost.addTab(spec);
				}
			}
		}
	}
}
