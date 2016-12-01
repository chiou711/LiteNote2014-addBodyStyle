package com.cwc.litenote;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class Util {
    SharedPreferences mPref_vibration;
    Context mContext;
    Activity mActivity;
    String mEMailString;
    private static DB mDb;
    static String NEW_LINE = "\r" + System.getProperty("line.separator");

	static int STYLE_DEFAULT = 1;
    
	static int ACTIVITY_CREATE = 0;
    static int ACTIVITY_VIEW_NOTE = 1;
    static int ACTIVITY_EDIT_NOTE = 2;
    static int ACTIVITY_IMPORT = 3;
    
    int clrGgDefault;
    int clrTxtDefault;

    // style
    // 0,2,4,6,8: dark background, 1,3,5,7,9: light background
	static int[] mBG_ColorArray = new int[]{Color.rgb(34,34,34), //#222222
											Color.rgb(255,255,255),
											Color.rgb(38,87,51), //#265733
											Color.rgb(186,249,142),
											Color.rgb(87,38,51),//#572633
											Color.rgb(249,186,142),
											Color.rgb(38,51,87),//#263357
											Color.rgb(142,186,249),
											Color.rgb(87,87,51),//#575733
											Color.rgb(249,249,140)};
	static int[] mText_ColorArray = new int[]{Color.rgb(255,255,255),
											  Color.rgb(0,0,0),
											  Color.rgb(255,255,255),
											  Color.rgb(0,0,0),
											  Color.rgb(255,255,255),
											  Color.rgb(0,0,0),
											  Color.rgb(255,255,255),
											  Color.rgb(0,0,0),
											  Color.rgb(255,255,255),
											  Color.rgb(0,0,0)};

    
    public Util(){};
    
	public Util(FragmentActivity activity) {
		mContext = activity;
		mActivity = activity;
	}
	
	public Util(Context context) {
		mContext = context;
	}
	
	// set vibration time
	void vibrate()
	{
		mPref_vibration = mContext.getSharedPreferences("vibration", 0);
    	if(mPref_vibration.getString("KEY_ENABLE_VIBRATION","yes").equalsIgnoreCase("yes"))
    	{
			Vibrator mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
			if(mPref_vibration.getString("KEY_VIBRATION_TIME","25") != "")
			{
				int vibLen = Integer.valueOf(mPref_vibration.getString("KEY_VIBRATION_TIME","25"));
				mVibrator.vibrate(vibLen); //length unit is milliseconds
				System.out.println("vibration len = " + vibLen);
			}
    	}
	}
	
	// save to SD card: for checked pages
	String saveToSdCard(String filename, List<Boolean> checkedArr,boolean enableToast)
	{   
	    // SD card path + "/" + directory path
	    String dirString = Environment.getExternalStorageDirectory().toString() + 
	    		              "/" + 
	    		              Util.getAppName(mContext);
	    
		File dir = new File(dirString);
		if(!dir.isDirectory())
			dir.mkdir();
		File file = new File(dir, filename);
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
		} catch (IOException e1) {
			System.out.println("_FileWriter error");
			e1.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(fw);
		
		//first row text
		
		//get data from DB
		String data ="";
		if(checkedArr == null)
			data = queryDB(data,null);// all pages
		else
			data = queryDB(data,checkedArr);
		
		data = addRssVersionAndChannel(data);
		mEMailString = data;
		
		try {
			bw.write(data);
			bw.flush();
			bw.close();
			if(enableToast)
				Toast.makeText(mContext, R.string.config_export_SDCard_toast ,Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		
		return mEMailString;
	}
	
	// save to SD card: for NoteView class
	String saveStringToSdCard(String filename, String curString)
	{   
	    // SD card path + "/" + directory path
	    String dirString = Environment.getExternalStorageDirectory().toString() + 
	    		              "/" + 
	    		              Util.getAppName(mContext);
	    
		File dir = new File(dirString);
		if(!dir.isDirectory())
			dir.mkdir();
		File file = new File(dir, filename);
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
		} catch (IOException e1) {
			System.out.println("_FileWriter error");
			e1.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(fw);
		
		//sent data
		String data = "";
		data = data.concat(curString);
		
		mEMailString = data;
		
		try {
			bw.write(data);
			bw.flush();
			bw.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		
		return mEMailString;
	}
	
    /**
     * Query current data base
     * @param checkedArr 
     * 
     */
    String queryDB(String data, List<Boolean> checkedArr)
    {
    	String curData = data;
    	
		String strFinalPageViewed_tableId = Util.getPrefFinalPageTableId((Activity) mContext);
        DB.setTableNumber(strFinalPageViewed_tableId);
    	
    	mDb = new DB(mContext);
    	mDb.doOpen();
    	int tabCount = DB.getAllTabCount();
    	mDb.doClose();
    	for(int i=0;i<tabCount;i++)
    		
    	{
    		// null: all pages
        	if((checkedArr == null ) || ( checkedArr.get(i) == true  ))
    		{
	        	// set Sent string Id
				List<Long> rowArr = new ArrayList<Long>();
        		mDb.doOpen();
				DB.setTableNumber(String.valueOf(DB.getTabTableId(i)));
				mDb.doClose();
				
        		mDb.doOpen();
	    		for(int k=0;k<mDb.getAllCount();k++)
	    		{
    				rowArr.add(k,(long) mDb.getNoteId(k));
	    		}
	    		mDb.doClose();
	    		curData = curData.concat(getSendString(rowArr));
    		}
    	}
    	return curData;
    	
    }
    
    // get current time string
    static String getCurrentTimeString()
    {
		// set time
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
	
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONDAY)+ 1; //month starts from 0
		int date = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR);//12h 
//		int hour = cal.get(Calendar.HOUR_OF_DAY);//24h
		String am_pm = (cal.get(Calendar.AM_PM)== 0) ?"AM":"PM"; // 0 AM, 1 PM
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		String strTime = year 
				+ "-" + month
				+ "-" + date
				+ "_" + am_pm
				+ "-" + hour
				+ "-" + min
				+ "-" + sec ;
//		System.out.println("time = "+  strTime );
		return strTime;
    }
    
    // get time string
    static String getTimeString(Long time)
    {
		// set time
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
	
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONDAY)+ 1; //month starts from 0
		int date = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);//24h
//		int hour = cal.get(Calendar.HOUR);//12h 
//		String am_pm = (cal.get(Calendar.AM_PM)== 0) ?"AM":"PM"; // 0 AM, 1 PM
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		String strTime = year 
				+ "/" + month
				+ "/" + date
//				+ "_" + am_pm
				+ " " + hour
				+ ":" + min
				+ ":" + sec ;
//		System.out.println("time = "+  strTime );
		return strTime;
    }
    
    void deleteAttachment(String mAttachmentFileName)
    {
		// delete file after sending
		String attachmentPath_FileName = Environment.getExternalStorageDirectory().getPath() + "/" +
										 mAttachmentFileName;
		File file = new File(attachmentPath_FileName);
		boolean deleted = file.delete();
		if(deleted)
			System.out.println("delete file is OK");
		else
			System.out.println("delete file is NG");
    }
    

	void markCurrent(DialogInterface alert)
	{
		mDb = new DB(mActivity);
	    ListView listView = ((AlertDialog) alert).getListView();
	    final ListAdapter originalAdapter = listView.getAdapter();
	    final int style = Util.getCurrentPageStyle(mActivity);
        TextView textViewDefault = new TextView(mActivity) ;
        clrGgDefault = textViewDefault.getDrawingCacheBackgroundColor();
        clrTxtDefault = textViewDefault.getCurrentTextColor();
		
	    listView.setAdapter(new ListAdapter()
	    {
	
	        @Override
	        public int getCount() {
	            return originalAdapter.getCount();
	        }
	
	        @Override
	        public Object getItem(int id) {
	            return originalAdapter.getItem(id);
	        }
	
	        @Override
	        public long getItemId(int id) {
	            return originalAdapter.getItemId(id);
	        }
	
	        @Override
	        public int getItemViewType(int id) {
	            return originalAdapter.getItemViewType(id);
	        }
	
	        @Override
	        public View getView(int position, View convertView, ViewGroup parent) {
	            View view = originalAdapter.getView(position, convertView, parent);
	            TextView textView = (TextView)view;
				mDb.doOpen();
	            if(DB.getTabTableId(position) == Integer.valueOf(DB.getTableNumber()))
	            {
		            textView.setTypeface(null, Typeface.BOLD_ITALIC);
		            textView.setBackgroundColor(mBG_ColorArray[style]);
		            textView.setTextColor(mText_ColorArray[style]);
	            }
	            else
	            {
	            	textView.setTypeface(null, Typeface.NORMAL);
		            textView.setBackgroundColor(clrGgDefault);
		            textView.setTextColor(clrTxtDefault);
	            }
				mDb.doClose();
	            return view;
	        }

	        @Override
	        public int getViewTypeCount() {
	            return originalAdapter.getViewTypeCount();
	        }

	        @Override
	        public boolean hasStableIds() {
	            return originalAdapter.hasStableIds();
	        }
	
	        @Override
	        public boolean isEmpty() {
	            return originalAdapter.isEmpty();
	        }

	        @Override
	        public void registerDataSetObserver(DataSetObserver observer) {
	            originalAdapter.registerDataSetObserver(observer);
	
	        }
	
	        @Override
	        public void unregisterDataSetObserver(DataSetObserver observer) {
	            originalAdapter.unregisterDataSetObserver(observer);
	
	        }
	
	        @Override
	        public boolean areAllItemsEnabled() {
	            return originalAdapter.areAllItemsEnabled();
	        }
	
	        @Override
	        public boolean isEnabled(int position) {
	            return originalAdapter.isEnabled(position);
	        }
	    });
	}
	
	// get App name
	static public String getAppName(Context context)
	{
		return context.getResources().getString(R.string.app_name);
	}
	
	// get style
	static public int getNewPageStyle(Context context)
	{
		SharedPreferences mPref_style;
		mPref_style = context.getSharedPreferences("style", 0);
		return mPref_style.getInt("KEY_STYLE",STYLE_DEFAULT);
	}
	
	static String[] mItemArray = new String[]{"1","2","3","4","5","6","7","8","9","10"};
    public static void setButtonColor(RadioButton rBtn,int iBtnId)
    {
		rBtn.setBackgroundColor(Util.mBG_ColorArray[iBtnId]);
		rBtn.setText(mItemArray[iBtnId]);
		rBtn.setTextColor(Util.mText_ColorArray[iBtnId]);
    }
	
	static public int getCurrentPageStyle(Context context)
	{
		int style = 0;
		mDb = new DB(context);
		mDb.doOpen();
		style = mDb.getTabStyle(TabsHost.mCurrentTabIndex);
		mDb.doClose();
		
		return style;
	}
	
	static String getSendString(List<Long> rowArr)
	{
        String PAGE_TAG_B = "<page>";
        String TAB_TAG_B = "<tabname>";
        String TAB_TAG_E = "</tabname>";
        String TITLE_TAG_B = "<title>";
        String TITLE_TAG_E = "</title>";
        String BODY_TAG_B = "<body>";
        String BODY_TAG_E = "</body>";
        String PAGE_TAG_E = "</page>";
        
        String sentString = NEW_LINE;

    	// when page has tab name only, no notes
    	if(rowArr.size() == 0)
    	{
        	mDb.doOpen();
        	sentString = sentString.concat(NEW_LINE + PAGE_TAG_B );
	        sentString = sentString.concat(NEW_LINE + TAB_TAG_B + DB.getCurrentTabName() + TAB_TAG_E);
	    	sentString = sentString.concat(NEW_LINE + TITLE_TAG_B + TITLE_TAG_E);
	    	sentString = sentString.concat(NEW_LINE + BODY_TAG_B +  BODY_TAG_E);
	    	sentString = sentString.concat(NEW_LINE + PAGE_TAG_E );
    		sentString = sentString.concat(NEW_LINE);
    		mDb.doClose();
    	}
    	else
    	{
	        for(int i=0;i< rowArr.size();i++)
	        {
	        	mDb.doOpen();
		    	Cursor cursorNote = mDb.get(rowArr.get(i));
		    	
		        String strTitleEdit = cursorNote.getString(
		        		cursorNote.getColumnIndexOrThrow(DB.KEY_NOTE_TITLE));
		        
		        String strBodyEdit = cursorNote.getString(
		        		cursorNote.getColumnIndexOrThrow(DB.KEY_NOTE_BODY));
		    	
		        int mark = cursorNote.getInt(cursorNote.getColumnIndexOrThrow(DB.KEY_NOTE_MARKING));
		        String srtMark = (mark == 1)? "[s]":"[n]";
		        
		        if(i==0)
		        {
		        	sentString = sentString.concat(NEW_LINE + PAGE_TAG_B );
		        	sentString = sentString.concat(NEW_LINE + TAB_TAG_B + DB.getCurrentTabName() + TAB_TAG_E );
		        }
		        
		        sentString = sentString.concat(NEW_LINE + TITLE_TAG_B + srtMark + strTitleEdit + TITLE_TAG_E);
		        sentString = sentString.concat(NEW_LINE + BODY_TAG_B + strBodyEdit + BODY_TAG_E);
		    	sentString = sentString.concat(NEW_LINE);
		    	if(i==rowArr.size()-1)
		        	sentString = sentString.concat(NEW_LINE +  PAGE_TAG_E);
		    		
		    	mDb.doClose();
	        }
    	}
    	return sentString;
	}
	
	// add RSS tag
	public static String addRssVersionAndChannel(String str)
	{
        String RSS_TAG_B = NEW_LINE + "<rss version=\"2.0\">";
        String RSS_TAG_E = "</rss>";
        String CHANNEL_TAG_B = "<channel>";
        String CHANNEL_TAG_E = "</channel>";
        
        String data = RSS_TAG_B + CHANNEL_TAG_B;
        data = data.concat(str);
		data = data.concat(CHANNEL_TAG_E + RSS_TAG_E);
		
		return data;
	}

	public String trimXML(String string) {
		string = string.replace("<rss version=\"2.0\">","");
		string = string.replace("<channel>","");
		string = string.replace("<page>","");
		string = string.replace("<tabname>","--- Page: ");
		string = string.replace("</tabname>"," ---");
		string = string.replace("<title>","Title: ");
		string = string.replace("</title>","");
		string = string.replace("<body>","Body: ");
		string = string.replace("</body>","");
		string = string.replace("[s]","");
		string = string.replace("[n]","");
		string = string.replace("</page>"," ");
		string = string.replace("</channel>","");
		string = string.replace("</rss>","");
		string = string.trim();
		return string;
	}
	
	public static int getScreenWidth(Activity activity)
	{
	    Display display = activity.getWindowManager().getDefaultDisplay();
		if(Build.VERSION.SDK_INT >= 13)
		{
		    Point outSize = new Point();
	        display.getSize(outSize);
	//        System.out.println("outSize.x = " + outSize.x);
	        return outSize.x;
		}
		else
		{
			return display.getWidth();
		}
	}
	
	public static int getScreenHeight(Activity activity)
	{
	    Display display = activity.getWindowManager().getDefaultDisplay();
		if(Build.VERSION.SDK_INT >= 13)
		{
		    Point outSize = new Point();
	        display.getSize(outSize);
	        return outSize.y;
		}
		else
		{
			return display.getHeight();
		}
	}
	
	public static String getPrefFinalPageTableId(Activity act)
	{
	       // get final viewed table Id
		SharedPreferences mPref_FinalPageViewed = act.getSharedPreferences("final_page_viewed", 0);
		String strFinalPageViewed_tableId = mPref_FinalPageViewed.getString("KEY_FINAL_PAGE_VIEWED","1");
		return strFinalPageViewed_tableId;
	}
	
	public static Uri getPictureUri(String pictureName, Activity act)
    {
	    String dirString = Environment.getExternalStorageDirectory().toString() + 
	    		              "/" + Util.getAppName(act);
	    
		File dir = new File(dirString);
		if(!dir.isDirectory())
			dir.mkdir();
		

		File photo = new File(dir,  pictureName);
//	    Uri imageUri = Uri.fromFile(photo);
	    return Uri.fromFile(photo);
    }

}
