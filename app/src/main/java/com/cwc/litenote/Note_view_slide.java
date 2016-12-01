package com.cwc.litenote;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Demonstrates a "screen-slide" animation using a {@link ViewPager}. Because {@link ViewPager}
 * automatically plays such an animation when calling {@link ViewPager#setCurrentItem(int)}, there
 * isn't any animation-specific code in this sample.
 *
 * <p>This sample shows a "next" button that advances the user to the next step in a wizard,
 * animating the current screen out (to the left) and the next screen in (from the right). The
 * reverse animation is played when the user presses the "previous" button.</p>
 *
 * @see Note_view_slide_pageFragment
 */
public class Note_view_slide extends FragmentActivity {
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    // DB
    DB mDb;
    Long mRowId;
    int mEntryPosition;
    int EDIT_VIEW = 5;
    int MAIL_VIEW = 6;
    int mStyle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_view_slide);

		if(Build.VERSION.SDK_INT >= 11)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
        // DB
		String strFinalPageViewed_tableId = Util.getPrefFinalPageTableId(Note_view_slide.this);
        DB.setTableNumber(strFinalPageViewed_tableId);
        mDb = new DB(Note_view_slide.this);
        
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        
        // set current selection
        mEntryPosition = getIntent().getExtras().getInt("POSITION");
        
        mPager.setCurrentItem(mEntryPosition);
        mDb.doOpen();
        mRowId = mDb.getNoteId(mEntryPosition);
        mStyle = mDb.getTabStyle(TabsHost.mCurrentTabIndex);
        mDb.doClose();

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                invalidateOptionsMenu();
            }
        });
		// edit note button
        Button editButton = (Button) findViewById(R.id.view_edit);
        editButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_edit, 0, 0, 0);
		
        // send note button
        Button sendButton = (Button) findViewById(R.id.view_send);
        sendButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_send, 0, 0, 0);
        
        // back button
        Button backButton = (Button) findViewById(R.id.view_back);
        backButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);
        
		//edit 
        editButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);

		        Intent i = new Intent(Note_view_slide.this, Note_edit.class);
		        i.putExtra(DB.KEY_NOTE_ID, mRowId);
		        startActivityForResult(i, EDIT_VIEW);
            }

        });
        
		//send 
        sendButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                
                // set Sent string Id
				List<Long> rowArr = new ArrayList<Long>();
				rowArr.add(0,mRowId);
				
                // mail
				Intent intent = new Intent(Note_view_slide.this, SendMailAct.class);
		        String extraStr = Util.getSendString(rowArr);
		        extraStr = Util.addRssVersionAndChannel(extraStr);
		        intent.putExtra("SentString", extraStr);
				startActivityForResult(intent, MAIL_VIEW);
            }

        });
        
        //cancel
        backButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }

        });
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
        if((requestCode==EDIT_VIEW) || (requestCode==MAIL_VIEW))
        {
        	finish();
        }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_screen_slide, menu);
        menu.findItem(R.id.action_previous).setEnabled(mPager.getCurrentItem() > 0);
        
        // update row Id
        mDb.doOpen();
        mRowId = mDb.getNoteId(mPager.getCurrentItem());
        mDb.doClose();
        
        // Add either a "next" or "finish" button to the action bar, depending on which page
        // is currently selected.
        MenuItem item = menu.add(Menu.NONE, R.id.action_next, Menu.NONE,
                (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1)
                        ? R.string.slide_action_finish
                        : R.string.slide_action_next);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        // set Gray for Last item
        if((mPager.getCurrentItem() == mPagerAdapter.getCount() - 1))
        	menu.findItem(R.id.action_next).setEnabled( false );
        
        return true;
    }

    // for menu buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate "up" the demo structure to the launchpad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
//                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                NavUtils.navigateUpTo(this, new Intent(this, TabsHost.class));
                return true;

            case R.id.action_previous:
                // Go to the previous step in the wizard. If there is no previous step,
                // setCurrentItem will do nothing.
            	mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                return true;

            case R.id.action_next:
                // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                // will do nothing.
            	mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A simple pager adapter that represents 5 {@link Note_view_slide_pageFragment} objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter 
    {
        public ScreenSlidePagerAdapter(FragmentManager fm) 
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) //position: page number
        {
        	mDb.doOpen();
        	String strTitle = mDb.getNoteTitle(position);
        	String strBody = mDb.getNoteBodyString(position);
        	Long createTime = mDb.getNoteCreateTime(position);
        	mDb.doClose();
            return Note_view_slide_pageFragment.create(position,mEntryPosition,
            											strTitle,strBody,createTime,mStyle);
        }

        @Override
        public int getCount() 
        {
        	mDb.doOpen();
        	int count = mDb.getAllCount();
        	mDb.doClose();
        	return count;
        }
    }
}
