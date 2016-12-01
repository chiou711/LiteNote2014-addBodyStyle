package com.cwc.litenote;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cwc.litenote.lib.DragSortController;
import com.cwc.litenote.lib.DragSortListView;
import com.cwc.litenote.lib.SimpleDragSortCursorAdapter;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


// main control
public class NoteFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<List<String>> 
{
	private static Cursor mNotesCursor;
	private static DB mDb;
    SharedPreferences mPref_delete_warn;
    SharedPreferences mPref_style;
	SharedPreferences mPref_show_note_attribute;
    private Long mNoteNumber1 = (long) 1;
	private String mNoteTitle1;
	private String mNotePictureString1;
	private String mNoteBodyString1;
	private int mMarkingIndex1;
	private Long mCreateTime1;
	private Long mNoteNumber2 ;
	private String mNotePictureString2;
	private String mNoteTitle2;
	private String mNoteBodyString2;
	private int mMarkingIndex2;
	private Long mCreateTime2;
	private List<Boolean> mHighlightList = new ArrayList<Boolean>();
	
	// This is the Adapter being used to display the list's data.
	NoteListAdapter mAdapter;
	DragSortListView mDndListView;
	ImageView mView2;
	private DragSortController mController;
	int MOVE_TO = 0;
	int COPY_TO = 1;
	int ADD_NEW_TO_TOP = 2;
    int mStyle = 0;

	public NoteFragment(){}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		  
		mDndListView = (DragSortListView)getActivity().findViewById(R.id.list1);
    	mDb = new DB(getActivity()); 
    	
    	mStyle = Util.getCurrentPageStyle(getActivity());

    	//view note listener
    	mDndListView.setOnItemClickListener(new OnItemClickListener()
    	{   @Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) 
			{
				mDb.doOpen();
				int count = mDb.getAllCount();
				mDb.doClose();
				if(position < count) // avoid footer error
				{
					Intent i;
					if(Build.VERSION.SDK_INT >= 11)
						i = new Intent(getActivity(), Note_view_slide.class);
					else
						i = new Intent(getActivity(), Note_view.class);
			        i.putExtra("POSITION", position);
			        startActivity(i);
				}
			}
    	}
    	);
    	
    	//edit note listener
    	mDndListView.setOnItemLongClickListener(new OnItemLongClickListener()
    	{
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
             {	
		        Intent i = new Intent(getActivity(), Note_edit.class);
		        i.putExtra(DB.KEY_NOTE_ID, id);
		        startActivity(i);
            	return true;
             }
	    });
    	
        mController = buildController(mDndListView);
        mDndListView.setFloatViewManager(mController);
        mDndListView.setOnTouchListener(mController);
        mDndListView.setDragEnabled(true);
        
		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new NoteListAdapter(getActivity());

		setListAdapter(mAdapter);

		// Start out with a progress indicator.
		setListShown(false); //cw@ //set progress indicator

		// Prepare the loader. Either re-connect with an existing one or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}
	
    /**
     * Called in onCreateView. Override this to provide a custom
     * DragSortController.
     */
    public DragSortController buildController(DragSortListView dslv) {
        // defaults are
        DragSortController controller = new DragSortController(dslv);
        controller.setSortEnabled(true);
        
        //drag
        controller.setDragHandleId(R.id.dragHandler);// handler
        controller.setDragInitMode(DragSortController.ON_DOWN); // click
//        controller.setDragInitMode(DragSortController.ON_LONG_PRESS); //long click to drag
//        controller.setBackgroundColor(Color.rgb(255,128,0));// background color when dragging
        
        // mark
        controller.setMarkEnabled(true);
        controller.setClickMarkId(R.id.img_check);
        controller.setMarkMode(DragSortController.ON_DOWN);

        return controller;
    }        

	@Override
	public Loader<List<String>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. 
		return new NoteListLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<String>> loader,
							   List<String> data) 
	{
		// Set the new data in the adapter.
		mAdapter.setData(data);

		// The list should now be shown.
		if (isResumed()) 
			setListShown(true);
		else 
			setListShownNoAnimation(true);
		
		fillData();
	}

	@Override
	public void onLoaderReset(Loader<List<String>> loader) {
		// Clear the data in the adapter.
		mAdapter.setData(null);
	}
	
    
	/**
	 * fill data
	 */
	
    public void fillData()
    {
    	mDb.doOpen();
    	mNotesCursor = mDb.mNotesCursor;
        
        // save index and top position
        int index = mDndListView.getFirstVisiblePosition();
        View v = mDndListView.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();
        
        String[] from = new String[] { DB.KEY_NOTE_TITLE};
        int[] to = new int[] { R.id.text_whole };
        
    	//show divider color
//        System.out.println("mStyle = " + mStyle);
        if(mStyle%2 == 0)
	    	mDndListView.setDivider(new ColorDrawable(0xFFffffff));//for dark
        else
           	mDndListView.setDivider(new ColorDrawable(0x00000000));//for light

        mDndListView.setDividerHeight(1);
        
        SimpleDragSortCursorAdapter adapter = new SimpleDragSortCursorAdapter(getActivity().getBaseContext(), 
														R.layout.activity_main_list_row,
														mNotesCursor, 
														from, 
														to, 
														0);
        
        //change list item color
        // 1. create a new ViewBinder
        SimpleDragSortCursorAdapter.ViewBinder binder = new SimpleDragSortCursorAdapter.ViewBinder() 
		  {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) 
            {
            	view.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
            	
//            	switch(mStyle)
//            	{
//	        		case 0:
//	                	view.setBackgroundResource(R.drawable.listview_item_shape_0);
//	                	break;
//	        		case 1:
//	                	view.setBackgroundResource(R.drawable.listview_item_shape_1);
//	                	break;
//	        		case 2:
//	                	view.setBackgroundResource(R.drawable.listview_item_shape_2);
//	                	break;
//	        		case 3:
//	                	view.setBackgroundResource(R.drawable.listview_item_shape_3);
//	                	break;
//	        		case 4:
//	                	view.setBackgroundResource(R.drawable.listview_item_shape_4);
//	                	break;
//	        		case 5:
//	                	view.setBackgroundResource(R.drawable.listview_item_shape_5);
//	                	break;	        		
//            	}

                TextView tvTitle = (TextView) view.findViewById(R.id.text_title);
                ImageView imgPicture = (ImageView) view.findViewById(R.id.img_picture);
                ImageView imgCheck = (ImageView) view.findViewById(R.id.img_check);
                // show body or not
            	mPref_show_note_attribute = getActivity().getSharedPreferences("show_note_attribute", 0);
            	if(mPref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
            	{
		            TextView tvBody = (TextView) view.findViewById(R.id.text_body);
		            String strNoteBody = cursor.getString(cursor.getColumnIndex(DB.KEY_NOTE_BODY));
		            TextView tvTime = (TextView) view.findViewById(R.id.text_time);
	                String timeStr = Util.getTimeString( cursor.getLong(cursor.getColumnIndex(DB.KEY_NOTE_CREATED)) );
	                tvTime.setText(timeStr);
//	                tvTime.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
	                tvTime.setTextColor(Util.mText_ColorArray[mStyle]);
		            tvBody.setText(strNoteBody);
//	                tvBody.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
	                tvBody.setTextColor(Util.mText_ColorArray[mStyle]);
	                
            	}
            	else
            	{
                	view.findViewById(R.id.text_body).setVisibility(View.GONE); // 1 invisible
                	view.findViewById(R.id.text_time).setVisibility(View.GONE); // 1 invisible
            	}
            	
                String strNoteTitle = cursor.getString(cursor.getColumnIndex(DB.KEY_NOTE_TITLE));
                tvTitle.setText(strNoteTitle);
                
//                tvTitle.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
                tvTitle.setTextColor(Util.mText_ColorArray[mStyle]);
                
                ///
                // set picture
                String strNotePicture = cursor.getString(cursor.getColumnIndex(DB.KEY_NOTE_PICTURE));
                ContentResolver cr = getActivity().getContentResolver();
                Bitmap bitmap,thumbNail = null;
    			try 
    			{
    				bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, 
    										Util.getPictureUri(strNotePicture, getActivity()));
    				thumbNail = Bitmap.createScaledBitmap(bitmap, 50, 50, false);
    				imgPicture.setImageBitmap(thumbNail); // out of memory ???
    			} 
    			catch (Exception e) 
    			{
//                    Toast.makeText(getActivity(), "Failed to load", Toast.LENGTH_SHORT).show();
    		        Log.e("Camera: Failed to load", e.toString());
    		    }
    			///
                
                if( cursor.getLong(cursor.getColumnIndex(DB.KEY_NOTE_MARKING)) == 1)
                		imgCheck.setBackgroundResource(mStyle%2 == 1?
                					R.drawable.btn_check_on_holo_light:
                					R.drawable.btn_check_on_holo_dark);	
                else
            		imgCheck.setBackgroundResource(mStyle%2 == 1?
        					R.drawable.btn_check_off_holo_light:
        					R.drawable.btn_check_off_holo_dark);	
                
                return true;
            }   
        };

        //2. set the new ViewBinder for adapter
        adapter.setViewBinder(binder);

        mDndListView.setAdapter(adapter);
        
		// for highlight
		for(int i=0; i< mDb.getAllCount() ; i++ )
		{
			mHighlightList.add(true);
			mHighlightList.set(i,true);
		}
		
        // restore index and top position
        mDndListView.setSelectionFromTop(index, top);
        mDb.doClose();
        mDndListView.setDropListener(onDrop);
        mDndListView.setDragListener(onDrag);
        mDndListView.setMarkListener(onMark);
        
        setFooter();
    }
    
    // list view listener: on drag
    private DragSortListView.DragListener onDrag =
            new DragSortListView.DragListener() {
                @Override
                public void drag(int startPosition, int endPosition) {
                	//add highlight boarder
                    View v = mDndListView.mFloatView;
//                    v.setBackgroundColor(Color.rgb(255,128,0));
//                	v.setBackgroundResource(R.drawable.listview_item_shape_dragging);
//                    v.setPadding(0, 4, 0,4);
                }
            };
	
    // list view listener: on drop
    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int startPosition, int endPosition) {
    				mDb.doOpen();
    				if(startPosition >= mDb.getAllCount()) // avoid footer error
    					return;
    		    	mDb.doClose();
    				
    				mHighlightList.set(startPosition, true);
    				mHighlightList.set(endPosition, true);
    				
    				//reorder data base storage
    				int loop = Math.abs(startPosition-endPosition);
    				for(int i=0;i< loop;i++)
    				{
    					swapRows(startPosition,endPosition);
    					if((startPosition-endPosition) >0)
    						endPosition++;
    					else
    						endPosition--;
    				}
    				fillData();
                }
            };
            
    // swap rows
	protected void swapRows(int startPosition, int endPosition) {

		mDb.doOpen();
		mDb.getAllCount();

		mNoteNumber1 = mDb.getNoteId(startPosition);
        mNoteTitle1 = mDb.getNoteTitle(startPosition);
        mNotePictureString1 = mDb.getNotePictureString(startPosition);
        mNoteBodyString1 = mDb.getNoteBodyString(startPosition);
        mMarkingIndex1 = mDb.getNoteMarking(startPosition);
    	mCreateTime1 = mDb.getNoteCreateTime(startPosition); 

		mNoteNumber2 = mDb.getNoteId(endPosition);
        mNoteTitle2 = mDb.getNoteTitle(endPosition);
        mNotePictureString2 = mDb.getNotePictureString(endPosition);
        mNoteBodyString2 = mDb.getNoteBodyString(endPosition);
        mMarkingIndex2 = mDb.getNoteMarking(endPosition);
    	mCreateTime2 = mDb.getNoteCreateTime(endPosition); 
		
        mDb.updateNote(mNoteNumber2,
				 mNoteTitle1,
				 mNotePictureString1,
				 mNoteBodyString1,
				 mMarkingIndex1,
				 mCreateTime1);		        
		
		mDb.updateNote(mNoteNumber1,
		 		 mNoteTitle2,
		 		 mNotePictureString2,
		 		 mNoteBodyString2,
		 		 mMarkingIndex2,
		 		 mCreateTime2);	
    	mDb.doClose();
	}


    // list view listener: on mark
    private DragSortListView.MarkListener onMark =
        new DragSortListView.MarkListener() 
		{   @Override
            public void mark(int position) 
			{
                mDb.doOpen();
                if(position >= mNotesCursor.getCount()) //end of list
                	return ;
                
                String strNote = mDb.getNoteTitle(position);
                String strNotePicture = mDb.getNotePictureString(position);
                String strNoteBody = mDb.getNoteBodyString(position);
                Long idNote =  mDb.getNoteId(position);
			
                DragSortListView parent = mDndListView;
                if(  mDb.getNoteMarking(position) == 0)                
              	  mDb.updateNote(idNote, strNotePicture, strNote, strNoteBody , 1, 0);
                else
              	  mDb.updateNote(idNote, strNotePicture, strNote, strNoteBody ,0, 0);
                
                mDb.doClose();
              
                // save index and top position
                int index = parent.getFirstVisiblePosition();
                View v = parent.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();
              
                fillData();
              
                // restore index and top position
                ((DragSortListView) parent).setSelectionFromTop(index, top);
                
                return;
            }
        };    
    
    // set footer
    void setFooter()
    {
	    mDb.doOpen();
	    TextView footerTextView = (TextView) getActivity().findViewById(R.id.footerText);
	    if(footerTextView != null) //add this for avoiding null exception when after e-Mail action
	    {
		    footerTextView.setText( mDb.getCheckedItemsCount() + 
		    		                " / " +
		    		                mDb.getAllCount() + 
		    		                " ( " +
		    		                getResources().getText(R.string.footer_checked).toString() + 
		    		                " / " +
		    		                getResources().getText(R.string.footer_total).toString() +
		       		                " ) " );
	    }
	    mDb.doClose();
    }
	


	/*******************************************
	 * 					menu
	 *******************************************/
    // Menu identifiers
    static final int ADD_NEW_NOTE = R.id.ADD_NEW_NOTE;
    static final int CHECK_ALL = R.id.CHECK_ALL;
    static final int UNCHECK_ALL = R.id.UNCHECK_ALL;
    static final int MOVE_CHECKED_NOTE = R.id.MOVE_CHECKED_NOTE;
    static final int COPY_CHECKED_NOTE = R.id.COPY_CHECKED_NOTE;
    static final int MAIL_CHECKED_NOTE = R.id.MAIL_CHECKED_NOTE;
    static final int DELETE_CHECKED_NOTE = R.id.DELETE_CHECKED_NOTE;
    
    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
	        case ADD_NEW_NOTE:
	            final Intent intent = new Intent(getActivity(), Note_addNew.class);
	            final String[] items = new String[]{
	            		getResources().getText(R.string.add_new_note_top).toString(),
	            		getResources().getText(R.string.add_new_note_bottom).toString() };
	            mDb.doOpen();
	            int noteCount = mDb.getAllCount();
	            mDb.doClose();
	            
	            if(noteCount > 0)
	            {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					  
					builder.setTitle(R.string.add_new_note_option_title)
					.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which) {
					
						if(which ==0)
							startActivityForResult(intent, ADD_NEW_TO_TOP);
						else
							startActivity(intent);
						//end
						dialog.dismiss();
					}})
					.setNegativeButton(R.string.btn_Cancel, null)
					.show();
	            }
	            else
	            	startActivity(intent);
	            //
	            return true;
	        case CHECK_ALL:
	        	checkAll(1); 
	            return true;
	        case UNCHECK_ALL:
	        	checkAll(0); 
	            return true;
	        case MOVE_CHECKED_NOTE:
	        case COPY_CHECKED_NOTE:
	    		mDb.doOpen();
	        	if(mDb.getCheckedItemsCount() > 0)
	        	{
		    		String copyItems[] = new String[mDb.getCheckedItemsCount()];
		    		String copyItemsPicture[] = new String[mDb.getCheckedItemsCount()];
		    		String copyItemsBody[] = new String[mDb.getCheckedItemsCount()];
		    		Long copyItemsTime[] = new Long[mDb.getCheckedItemsCount()];
		    		int cCopy = 0;
		    		for(int i=0;i<mDb.getAllCount();i++)
		    		{
		    			if(mDb.getNoteMarking(i) == 1)
		    			{
		    				copyItems[cCopy] = mDb.getNoteTitle(i);
		    				copyItemsPicture[cCopy] = mDb.getNotePictureString(i);
		    				copyItemsBody[cCopy] = mDb.getNoteBodyString(i);
		    				copyItemsTime[cCopy] = mDb.getNoteCreateTime(i);
		    				cCopy++;
		    			}
		    		}
		    		mDb.doClose();
		           
		    		if(item.getItemId() == MOVE_CHECKED_NOTE)
		    			operateCheckedTo(copyItems, copyItemsPicture, copyItemsBody, copyItemsTime, MOVE_TO); // move to
		    		else if(item.getItemId() == COPY_CHECKED_NOTE)
			    		operateCheckedTo(copyItems, copyItemsPicture, copyItemsBody, copyItemsTime, COPY_TO);// copy to
		    			
	        	}
	        	else
	        	{
	        		mDb.doClose();
	    			Toast.makeText(getActivity(),
							   R.string.delete_checked_no_checked_items,
							   Toast.LENGTH_SHORT)
					 .show();
	        	}
	            return true;
	        case MAIL_CHECKED_NOTE:
	    		mDb.doOpen();
	        	if(mDb.getCheckedItemsCount() > 0)
	        	{
		        	// set Sent string Id
					List<Long> rowArr = new ArrayList<Long>();
	            	int j=0;
		    		for(int i=0;i<mDb.getAllCount();i++)
		    		{
		    			if(mDb.getNoteMarking(i) == 1)
		    			{
		    				rowArr.add(j,(long) mDb.getNoteId(i));
		    				j++;
		    			}
		    		}
		    		mDb.doClose();
		    		
		    		// send
		    		Intent intentMail = new Intent(getActivity(), SendMailAct.class);
		    		String extraStr = Util.getSendString(rowArr);
		    		extraStr = Util.addRssVersionAndChannel(extraStr);
		    		intentMail.putExtra("SentString", extraStr);
					startActivity(intentMail);
	        	}
	        	else
	        	{
	        		mDb.doClose();
	    			Toast.makeText(getActivity(),
							   R.string.delete_checked_no_checked_items,
							   Toast.LENGTH_SHORT)
					 .show();
	        	}
	        	return true;
	        case DELETE_CHECKED_NOTE:
	        	mDb.doOpen();
	        	if(mDb.getCheckedItemsCount() > 0)
	        	{
	        		mDb.doClose();
	        		deleteCheckedNotes();
	        	}
	        	else
	        	{
	        		mDb.doClose();
	    			Toast.makeText(getActivity(),
	    						   R.string.delete_checked_no_checked_items,
	    						   Toast.LENGTH_SHORT)
	    				 .show();
	        	}
	            return true;     
            
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
        if(requestCode==ADD_NEW_TO_TOP)
        {
            mDb.doOpen();
            int startCursor = mDb.getAllCount()-1;
            mDb.doClose();
            int endCursor = 0;
			
			//reorder data base storage
			int loop = Math.abs(startCursor-endCursor);
			for(int i=0;i< loop;i++)
			{
				swapRows(startCursor,endCursor);
				if((startCursor-endCursor) >0)
					endCursor++;
				else
					endCursor--;
			}
			fillData();
        }   
	}
    
	/**
	 *  check all or uncheck all
	 */
	public void checkAll(int action) 
	{
		mDb.doOpen();
		for(int i=0;i<mDb.getAllCount();i++)
		{
			Long rowId = mDb.getNoteId(i);
			String noteTitle = mDb.getNoteTitle(i);
			String notePicture = mDb.getNotePictureString(i);
			String noteBody = mDb.getNoteBodyString(i);
			mDb.updateNote(rowId, noteTitle, notePicture, noteBody , action, 0);// action 1:check all, 0:uncheck all
		}
		mDb.doClose();
		fillData();
	}
	
    /**
     *   operate checked to: move to, copy to
     * @param copyItemsTime 
     */
	void operateCheckedTo(final String[] copyItems,final String[] copyItemsPicture, final String[] copyItemsBody, final Long[] copyItemsTime, final int action)
	{
		//list all tabs
		mDb.doOpen();
		int tabCount = DB.getAllTabCount();
		final String[] tabNames = new String[tabCount];
		final int[] tableIds = new int[tabCount];
		for(int i=0;i<tabCount;i++)
		{
			tabNames[i] = DB.getTabName(i);
			tableIds[i] = DB.getTabTableId(i);
		}
		tabNames[TabsHost.mCurrentTabIndex] = tabNames[TabsHost.mCurrentTabIndex] + " *"; // add mark to current page 
		mDb.doClose();
		   
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//keep original table id
				String curTableNum = DB.getTableNumber();

				//copy checked item to destination tab
				String destTableNum = String.valueOf(tableIds[which]);
				DB.setTableNumber(destTableNum);
				mDb.doOpen();
				for(int i=0;i< copyItems.length;i++)
				{
					mDb.insert(copyItems[i],copyItemsPicture[i], copyItemsBody[i],copyItemsTime[i]);
				}
				mDb.doClose();
				
				//recover to original table id
				if(action == MOVE_TO)
				{
					DB.setTableNumber(curTableNum);
					mDb.doOpen();
					//delete checked
					for(int i=0;i< mDb.getAllCount() ;i++)
					{
						if(mDb.getNoteMarking(i) == 1)
							mDb.delete(mDb.getNoteId(i));
					}
					mDb.doClose();
					fillData();
				}
				else if(action == COPY_TO)
				{
					DB.setTableNumber(curTableNum);
					if(destTableNum.equalsIgnoreCase(curTableNum))
						fillData();
				}
				
				dialog.dismiss();
			}
		};
		
		if(action == MOVE_TO)
			builder.setTitle(R.string.checked_notes_move_to_dlg);
		else if(action == COPY_TO)
			builder.setTitle(R.string.checked_notes_copy_to_dlg);
		
		builder.setSingleChoiceItems(tabNames, -1, listener)
		  	.setNegativeButton(R.string.btn_Cancel, null);
		
		// override onShow to mark current page status
		AlertDialog alert = builder.create();
		alert.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface alert) {
				// add mark for current page
				Util util = new Util(getActivity());
				util.markCurrent(alert);
			}
		});
		alert.show();
	}
	
	/**
	 * delete checked notes
	 */
	public void deleteCheckedNotes()
	{
		final Context context = getActivity();

		mPref_delete_warn = context.getSharedPreferences("delete_warn", 0);
    	if(mPref_delete_warn.getString("KEY_DELETE_WARN_MAIN","enable").equalsIgnoreCase("enable") &&
           mPref_delete_warn.getString("KEY_DELETE_CHECKED_WARN","yes").equalsIgnoreCase("yes"))
    	{
			Util util = new Util(getActivity());
			util.vibrate();
    		
    		// show warning dialog
			Builder builder = new Builder(context);
			builder.setTitle(R.string.delete_checked_note_title)
					.setMessage(R.string.delete_checked_message)
					.setNegativeButton(R.string.btn_Cancel, 
							new OnClickListener() 
					{	@Override
						public void onClick(DialogInterface dialog, int which) 
						{/*cancel*/} })
					.setPositiveButton(R.string.btn_OK, 
							new OnClickListener() 
					{	@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							mDb.doOpen();
							for(int i=0;i< mDb.getAllCount() ;i++)
							{
								if(mDb.getNoteMarking(i) == 1)
									mDb.delete(mDb.getNoteId(i));
							}
							mDb.doClose();
							fillData();
						}
					});
			
	        AlertDialog d = builder.create();
	        d.show();
    	}
    	else
    	{
    		// not show warning dialog
			mDb.doOpen();
			for(int i=0;i< mDb.getAllCount() ;i++)
			{
				if(mDb.getNoteMarking(i) == 1)
					mDb.delete(mDb.getNoteId(i));
			}
			mDb.doClose();
			fillData();
    	}
	}
    
	@Override
	public void onDestroy() {
		mDb.doClose();
		super.onDestroy();
	}
	
	
	/*
	 * inner class for note list loader
	 */
	public static class NoteListLoader extends AsyncTaskLoader<List<String>> 
	{
		List<String> mApps;

		public NoteListLoader(Context context) {
			super(context);
		}

		@Override
		public List<String> loadInBackground() {
			List<String> entries = new ArrayList<String>();
			return entries;
		}

		@Override
		protected void onStartLoading() {
			System.out.println("_onStartLoading");
			mDb.doOpen();
			forceLoad();
			mDb.doClose();
		}
	}

	/*
	 * 	inner class for note list adapter
	 */
	public static class NoteListAdapter extends ArrayAdapter<String> 
	{
		public NoteListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
		}
		public void setData(List<String> data) {
			clear();
			if (data != null) {		
				if(Build.VERSION.SDK_INT >= 11)
					addAll(data);
			}
		}
	}
}
