package se.serp.LiULunch;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import se.serp.LiULunch.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.ViewFlipper;

class Animations {
	static public class SlideInLeft extends TranslateAnimation {
		public SlideInLeft() {
			super(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
			this.setDuration(400);
		}
	}
	static public class SlideOutLeft extends TranslateAnimation {
		public SlideOutLeft() {
			super(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
			this.setDuration(400);
		}
	}
	
	static public class SlideOutRight extends TranslateAnimation {
		public SlideOutRight() {
			super(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
			this.setDuration(400);
		}
	}
	static public class SlideInRight extends TranslateAnimation {
		public SlideInRight() {
			super(Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
			this.setDuration(400);
		}
	}
}

public class MenusActivity extends Activity {
	boolean updatingMenus;
	GestureDetector gestureDetector;
	MenuCache menuCache;
	int updateFrequency; 
	
	private void updateMenus()
	{
		if (updatingMenus)
		{
			Toast.makeText(getApplicationContext(),
					"Nu lugnar du ner dig! Jag håller ju redan på och uppdaterar menyn!",
					Toast.LENGTH_SHORT).show();
		}
		else
		{
			updatingMenus = true;
			new ContentGetter() {
				protected void onPostExecute(GetContentResult result) {
					updatingMenus = false;
					if (result.failReason != null)
					{
						Toast.makeText(getApplicationContext(),
								result.failReason,
								Toast.LENGTH_LONG).show();
					}
					else
					{
						if (parseAndDisplayMenus(result.content)) {
							Toast.makeText(getApplicationContext(),
									"Så där ja!",
									Toast.LENGTH_SHORT).show();
							menuCache.saveMenu(result.content);
						}
					}
				}
			}.execute(Versioning.MENU_URL);
			Toast.makeText(getApplicationContext(),
					"Hämtar menyer...",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private boolean parseAndDisplayMenus(String content)
	{
		// special case: catch users trying to use the app without logging in using netlogon  
		if (content.indexOf("Netlogon") != -1) {
			Toast.makeText(getApplicationContext(),
					"Det verkar som att du glömt logga in på netlogon. Gör det och försök uppdatera menyerna igen!",
					Toast.LENGTH_LONG).show();
			return false;
		}
		
		
		DayMenu[] dayMenus;
		
		try
		{
			Object o = new JSONTokener(content).nextValue();
			
			if (o instanceof JSONArray)
			{
				dayMenus = MenuParser.parseDayMenus((JSONArray)o);
			}
			else
			{
				Toast.makeText(getApplicationContext(),
						"Allvarligt fel vid parsning av menyerna. Välj Uppdatera i menyn för att hämta nya menyer!",
						Toast.LENGTH_LONG).show();
				return false;
			}
		}
		catch (JSONException e)
		{
			Toast.makeText(getApplicationContext(),
					"Fel vid parsning av menyerna. Välj Uppdatera i menyn för att hämta nya menyer!",
					Toast.LENGTH_LONG).show();
			return false;
		}
		
		ViewFlipper viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);

		viewFlipper.clearAnimation();
		viewFlipper.removeAllViews();

		viewFlipper.setInAnimation(null);
		viewFlipper.setOutAnimation(null);
				
		Calendar now = new GregorianCalendar();
		Calendar today = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
		
		Calendar bestViewDate = null;
		int bestViewIndex = -1;
		
		for (DayMenu dayMenu : dayMenus) {
			DayMenuView view = new DayMenuView(this, dayMenu);
			viewFlipper.addView(view);
			
			// is this day menu better than the last one?
			if (bestViewDate == null || bestViewDate.before(today) && (today.equals(dayMenu.date) || today.before(dayMenu.date))) {
				bestViewDate = dayMenu.date;
				bestViewIndex = viewFlipper.getChildCount() - 1;
			}
		}
		
		if (bestViewIndex != -1) {
			viewFlipper.setDisplayedChild(bestViewIndex);
		}
		
		return true;
	}
	
	public void showNextMenu()
	{
		ViewFlipper viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);
		
		// I want to set the selected tab of the next DayMenuView the same as the tab of this one.
		// I don't know a pretty way to do it, so here goes...
		View currentView = viewFlipper.getCurrentView();
		if (currentView != null && currentView instanceof DayMenuView) {
			String currentTag = ((TabHost)((DayMenuView)currentView).findViewById(android.R.id.tabhost)).getCurrentTabTag();
			for (int i = 0; i < viewFlipper.getChildCount(); i++) {
				View view = viewFlipper.getChildAt(i);
				if (view != currentView && view instanceof DayMenuView) {
					DayMenuView dmv = (DayMenuView)view;
					((TabHost)(dmv).findViewById(android.R.id.tabhost)).setCurrentTabByTag(currentTag);
				}
			}
		}
		
		viewFlipper.setInAnimation(new Animations.SlideInLeft());
		viewFlipper.setOutAnimation(new Animations.SlideOutLeft());
		viewFlipper.showNext();
	}
	
	public void showPreviousMenu()
	{
		ViewFlipper viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);
		
		// I want to set the selected tab of the next DayMenuView the same as the tab of this one.
		// I don't know a pretty way to do it, so here goes...
		View currentView = viewFlipper.getCurrentView();
		if (currentView != null && currentView instanceof DayMenuView) {
			String currentTag = ((TabHost)((DayMenuView)currentView).findViewById(android.R.id.tabhost)).getCurrentTabTag();
			for (int i = 0; i < viewFlipper.getChildCount(); i++) {
				View view = viewFlipper.getChildAt(i);
				if (view != currentView && view instanceof DayMenuView) {
					DayMenuView dmv = (DayMenuView)view;
					((TabHost)(dmv).findViewById(android.R.id.tabhost)).setCurrentTabByTag(currentTag);
				}
			}
		}
		
		viewFlipper.setInAnimation(new Animations.SlideInRight());
		viewFlipper.setOutAnimation(new Animations.SlideOutRight());
		viewFlipper.showPrevious();
	}
	
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    switch (this.updateFrequency) {
	    /*case MenuCache.UPDATE_EVERY_PROGRAM_START:
	    	menu.findItem(R.id.updateEveryStart).setChecked(true);
	    	break;*/
	    case MenuCache.UPDATE_EVERY_DAY:
	    default:
	    	menu.findItem(R.id.updateDaily).setChecked(true);
	    	break;
	    case MenuCache.UPDATE_ONLY_ON_EXPLICIT_REQUEST:
	    	menu.findItem(R.id.updateManually).setChecked(true);
	    	break;
	    }
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			updateMenus();
			return true;
		/*case R.id.updateEveryStart:
			item.setChecked(true);
			this.updateFrequency = MenuCache.UPDATE_EVERY_PROGRAM_START;
			this.savePreferences();
			return true;*/
		case R.id.updateDaily:
			item.setChecked(true);
			this.updateFrequency = MenuCache.UPDATE_EVERY_DAY;
			this.savePreferences();
			return true;
		case R.id.updateManually:
			item.setChecked(true);
			this.updateFrequency = MenuCache.UPDATE_ONLY_ON_EXPLICIT_REQUEST;
			this.savePreferences();
			return true;
		default:
			return super.onOptionsItemSelected(item); 
		}
	}
	
	void loadPreferences() {
		SharedPreferences settings = getSharedPreferences(Versioning.PREFERENCES_FILENAME, 0);
		this.updateFrequency = settings.getInt("updateFrequency", MenuCache.UPDATE_EVERY_DAY);
		
	}
	
	void savePreferences() {
		SharedPreferences settings = getSharedPreferences(Versioning.PREFERENCES_FILENAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("updateFrequency", this.updateFrequency);
		editor.commit();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		ViewFlipper viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);
		View currentView = viewFlipper.getCurrentView();
		if (currentView != null && currentView instanceof DayMenuView) {
			DayMenuView dmv = (DayMenuView)currentView;
			
			// a DayMenuView always has a TabHost with id android.R.id.tabhost
			TabHost th = (TabHost)dmv.findViewById(android.R.id.tabhost);
			String currentTag = th.getCurrentTabTag();
			if (currentTag != null && currentTag.length() > 0) {
				// yay a menu is selected!
				outState.putLong("selectedDate", dmv.dayMenu.date.getTimeInMillis());
				outState.putString("selectedTag", currentTag);
			}
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		String selectedTag = savedInstanceState.getString("selectedTag");
		if (selectedTag != null) {
			Calendar selectedDate = new GregorianCalendar();
			selectedDate.setTimeInMillis(savedInstanceState.getLong("selectedDate"));
			ViewFlipper viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);
			for (int i = 0; i < viewFlipper.getChildCount(); i++) {
				View view = viewFlipper.getChildAt(i);
				if (view instanceof DayMenuView) {
					DayMenuView dmv = (DayMenuView)view;
					if (dmv.dayMenu.date.equals(selectedDate)) {
						viewFlipper.setDisplayedChild(i);
						
						// a DayMenuView always has a TabHost with id android.R.id.tabhost
						TabHost th = (TabHost)dmv.findViewById(android.R.id.tabhost);
						th.setCurrentTabByTag(selectedTag);
						
						break;
					}
				}
			}
		}
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		gestureDetector = new GestureDetector(new SimpleOnGestureListener() {
				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
					
					final int SWIPE_MIN_DISTANCE = 60;
					final int SWIPE_THRESHOLD_VELOCITY = 200;
					
					if (Math.abs(e1.getY() - e2.getY()) > Math.abs(e1.getX() - e2.getX()))
					{
	                    return false;
					}
					
					if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						showNextMenu();
						return true;
			        }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			        	showPreviousMenu();
			        	return true;
			        }
					
					return false;
				}
			});
		
		loadPreferences();
		this.menuCache = new MenuCache(this, Versioning.CACHED_MENU_FILENAME);
		String menu = menuCache.loadMenu();
		if (menu != null) {
			this.parseAndDisplayMenus(menu);
		}
		
		if (this.menuCache.needsUpdateOnProgramStart(this.updateFrequency)) {
			updateMenus();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		savePreferences();		
	}
	
	float downX, downY;
	boolean gestureStarted = false;
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = ev.getX();
			downY = ev.getY();
			gestureStarted = true;
			break;
		case MotionEvent.ACTION_MOVE:
			float dx = ev.getX() - downX;
			float dy = ev.getY() - downY;
			
			float length = (float)Math.sqrt(dx*dx + dy*dy);
			if (gestureStarted && length > 100.0f) {
				if (Math.abs(dx) > Math.abs(dy)) {
					if (Math.signum(dx) > 0.0f) {
						showPreviousMenu();
					} else {
						showNextMenu();
					}
				}
				gestureStarted = false;
			}
			break;
		case MotionEvent.ACTION_UP:
			gestureStarted = false;
			break;
		}
		//gestureDetector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}
}