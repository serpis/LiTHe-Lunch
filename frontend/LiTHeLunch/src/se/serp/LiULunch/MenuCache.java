package se.serp.LiULunch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;

public class MenuCache {
	public static final int UPDATE_EVERY_DAY = 0;
	public static final int UPDATE_EVERY_PROGRAM_START = 1;
	public static final int UPDATE_ONLY_ON_EXPLICIT_REQUEST = 2;
	
	Context context;
	String cacheFilename;
	
	MenuCache(Context context, String cacheFilename) {
		this.context = context;
		this.cacheFilename = cacheFilename;
	}
	
	public String loadMenu() {
		try {
			File menu = new File(this.context.getCacheDir(), this.cacheFilename);
			InputStream is = new FileInputStream(menu);
			DataInputStream dis = new DataInputStream(is);
			String content = dis.readUTF();
			dis.close();
			return content;
		} catch (FileNotFoundException e) {
			// TODO: handle this better?
			//e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO: handle this better?
			//e.printStackTrace();
			return null;
		}
	}
	
	public boolean saveMenu(String menu) {
		try {
			File f = new File(this.context.getCacheDir(), this.cacheFilename);
			OutputStream os = new FileOutputStream(f);
			DataOutputStream dos = new DataOutputStream(os);
			dos.writeUTF(menu);
			dos.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO: handle this better?
			//e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO: handle this better?
			//e.printStackTrace();
			return false;
		}
	}
	
	// returns true if any of this is true:
	// * there is no cached menu
	// * updateFrequency is set to update menu every program start
	// * the stored cached menu is more than 12 hours old 
	public boolean needsUpdateOnProgramStart(int updateFrequency) {
		File menu = new File(this.context.getCacheDir(), this.cacheFilename);
		long millisecondsPerHour = 60 * 60 * 1000;
		if (!menu.exists()
			|| (updateFrequency == UPDATE_EVERY_PROGRAM_START
					|| (updateFrequency == UPDATE_EVERY_DAY && System.currentTimeMillis() - menu.lastModified() > 12 * millisecondsPerHour))) {
			return true;
		}
		else {
			return false;
		}
	}
}
