/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cwc.litenote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Note_edit extends Activity {

    private Long mRowId;
    SharedPreferences mPref_style;
    SharedPreferences mPref_delete_warn;
    Note_common note_common;
    private boolean mEnSaveDb = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note_title);// set title
    	
        note_common = new Note_common(this);
        note_common.UI_init();
		
		//***
//			// new thread executor
//	        String getString;
//	        ExecutorService es = Executors.newSingleThreadExecutor();
//	        
//	        Future<String> result = es.submit(new Callable<String>() 
//	        {
//	            public String call() throws Exception {
//	            	
//	             // get content by URL and then parse by XML format  
//           		 HandleXmlByUrl obj;
//           		 String url = "http://192.168.56.1:8080/ilan/t1.xml";
////           		 String url = "http://tutorialspoint.com/android/sampleXML.xml";
//        	     obj = new HandleXmlByUrl(url,EditNote.this);
//        	     obj.fetchXML();
//        	     while(obj.parsingComplete);
//        	     return obj.getXmlContent();
//	            }
//	        });
//	        
//	        // future object for getting string
//	        try 
//	        {
//	            getString = result.get();
//	            System.out.println("x = " + getString);
//	            mBodyEditText.setText(getString);
//	        } catch (Exception e) {
//	            // failed
//	        }
		//**
	        
		// OK button
        Button okButton = (Button) findViewById(R.id.note_edit_ok);
        okButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
		// OK
        okButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                mEnSaveDb = true;
                finish();
            }

        });
        
        // delete button
        Button delButton = (Button) findViewById(R.id.note_edit_delete);
        delButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0);
        // delete
        delButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
				//warning :start
        		mPref_delete_warn = getSharedPreferences("delete_warn", 0);
            	if(mPref_delete_warn.getString("KEY_DELETE_WARN_MAIN","enable").equalsIgnoreCase("enable") &&
            	   mPref_delete_warn.getString("KEY_DELETE_NOTE_WARN","yes").equalsIgnoreCase("yes")) 
            	{
        			Util util = new Util(Note_edit.this);
    				util.vibrate();
            		
            		Builder builder1 = new Builder(Note_edit.this ); 
            		builder1.setTitle(R.string.confirm_dialog_title)
                        .setMessage(R.string.confirm_dialog_message)
                        .setNegativeButton(R.string.confirm_dialog_button_no, new OnClickListener()
                        {   @Override
                            public void onClick(DialogInterface dialog1, int which1)
                        	{/*nothing to do*/}
                        })
                        .setPositiveButton(R.string.confirm_dialog_button_yes, new OnClickListener()
                        {   @Override
                            public void onClick(DialogInterface dialog1, int which1)
                        	{
                        		note_common.deleteNote(mRowId);
                            	finish();
                        	}
                        })
                        .show();//warning:end
            	}
            	else{
            	    //no warning:start
	                setResult(RESULT_CANCELED);
	                note_common.deleteNote(mRowId);
	                finish();
            	}
            }
        });
        
        // cancel button
        Button cancelButton = (Button) findViewById(R.id.note_edit_cancel);
        cancelButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
        // cancel
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
            	if(note_common.isModified())
            	{
            		confirmToUpdateDlg();
            	}
            	else
            	{
                    mEnSaveDb = false;
                    finish();
            	}
            }
        });
			
        // get row Id from saved instance
        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DB.KEY_NOTE_ID);
        
        // get row Id from intent extras
        if (mRowId == null ) {
        	Bundle extras = getIntent().getExtras();
        	mRowId = extras.getLong(DB.KEY_NOTE_ID);
        }
		
        note_common.populateFields(mRowId);
    }
    
    // confirm to update change or not
    void confirmToUpdateDlg()
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(Note_edit.this);
		builder.setTitle(R.string.confirm_dialog_title)
	           .setMessage(R.string.edit_note_confirm_update)
			   .setPositiveButton(R.string.confirm_dialog_button_yes, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
					    mEnSaveDb = true;
					    finish();
					}})
			   .setNegativeButton(R.string.confirm_dialog_button_no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
	                    mEnSaveDb = false;
	                    finish();
					}})
			   .show();
    }
    

    // for Add new note, Rotate screen
    @Override
    protected void onPause() {
        super.onPause();
//        mRowId = note_common.saveState(mRowId,mEnSaveDb);
        mRowId = note_common.saveState(mRowId,mEnSaveDb,""); //???
    }

    // for Rotate screen
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        mRowId = note_common.saveState(mRowId,mEnSaveDb);
        mRowId = note_common.saveState(mRowId,mEnSaveDb,""); //???
        outState.putSerializable(DB.KEY_NOTE_ID, mRowId);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        note_common.populateFields(mRowId);
    }
    
    @Override
    public void onBackPressed() {
    	if(note_common.isModified())
    	{
    		confirmToUpdateDlg();
    	}
    	else
    	{
            mEnSaveDb = false;
            finish();
    	}
    }
}
