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

import java.io.File;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Note_addNew extends Activity {

    private Long mRowId;
    SharedPreferences mPref_style;
    SharedPreferences mPref_delete_warn;
    Note_common note_common;
    private boolean mEnSaveDb = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.note_add_new);
        setTitle(R.string.add_new_note_title);// set title

        note_common = new Note_common(this);
        note_common.UI_init();
        
    	// new add button
        Button addButton = (Button) findViewById(R.id.note_add_new_add);
        addButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_input_add, 0, 0, 0);
		//add
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setResult(RESULT_OK);
                mEnSaveDb = true;
                finish();
            }

        });
        
        // cancel button
        Button cancelButton = (Button) findViewById(R.id.note_add_new_cancel);
        cancelButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
        //cancel
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                
            	if(note_common.isEdited())
            	{
            		confirmToSaveDlg();
            	}
            	else
            	{
                    note_common.deleteNote(mRowId);
                    mEnSaveDb = false;
                    finish();
            	}

            }
        });
			
        // get row Id from saved instance
        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DB.KEY_NOTE_ID);
        
        note_common.populateFields(mRowId);
    }
    
    // confirm to update change or not
    void confirmToSaveDlg()
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(Note_addNew.this);
		builder.setTitle(R.string.confirm_dialog_title)
	           .setMessage(R.string.add_new_note_confirm_save)
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
						note_common.deleteNote(mRowId);
	                    mEnSaveDb = false;
	                    finish();
					}})
			   .show();
    }

    // for Add new note, Rotate screen
    @Override
    protected void onPause() {
        super.onPause();
//        if(mPictureFileName == "")
//        	mRowId = note_common.saveState(mRowId,mEnSaveDb);
//        else
        	mRowId = note_common.saveState(mRowId,mEnSaveDb,mPictureFileName);
        	
    }

    // for Rotate screen
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        mRowId = note_common.saveState(mRowId,mEnSaveDb);
        mRowId = note_common.saveState(mRowId,mEnSaveDb,mPictureFileName);
        outState.putSerializable(DB.KEY_NOTE_ID, mRowId);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        note_common.populateFields(mRowId);
    }
    
    @Override
    public void onBackPressed() {
    	if(note_common.isEdited())
    	{
    		confirmToSaveDlg();
    	}
    	else
    	{
            note_common.deleteNote(mRowId);
            mEnSaveDb = false;
            finish();
    	}
    }
    
    static final int TAKE_PICTURE = R.id.TAKE_PICTURE;
	private static int TAKE_PICTURE_ACT = 1;    
	private Uri imageUri;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(Build.VERSION.SDK_INT >= 11)
		{
		    menu.add(0, TAKE_PICTURE, 0, "Take Picture" )
		    .setIcon(android.R.drawable.ic_menu_camera)
		    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
		else
		{	
			menu.add(0, TAKE_PICTURE, 1,  "Take Picture" )
		    .setIcon(R.drawable.ic_input_add);
		}	

		return super.onCreateOptionsMenu(menu);
	}
    
	private String mPictureFileName = "";
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	FragmentManager fragmentManager = null;
    	
        switch (item.getItemId()) 
        {
            case TAKE_PICTURE:
            	Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				mPictureFileName = System.currentTimeMillis() + ".jpg";
            	imageUri = Util.getPictureUri(mPictureFileName, Note_addNew.this);
			    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			    startActivityForResult(intent, TAKE_PICTURE_ACT); 
			    return true;
            
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) 
	{
		if (requestCode == TAKE_PICTURE_ACT)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				Toast.makeText(this, "Image Captured", Toast.LENGTH_LONG).show();
		
	            Uri selectedImage = imageUri;
	            Bitmap bitmap;
	            ImageView imageView = (ImageView) findViewById(R.id.edit_picture);

//	            getContentResolver().notifyChange(selectedImage, null);
	            ContentResolver cr = getContentResolver();
	            Bitmap thumbNail = null;
	            try 
	            {
	                bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, selectedImage);
	                thumbNail = Bitmap.createScaledBitmap(bitmap, 50, 50, false);
	                imageView.setImageBitmap(thumbNail);
	                Toast.makeText(this, selectedImage.toString(), Toast.LENGTH_LONG).show();
	            } 
	            catch (Exception e) 
	            {
	                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
	                Log.e("Camera: Failed to load", e.toString());
	            }
			} 
			else if (resultCode == RESULT_CANCELED)
			{
				Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
			}
		}
	}
	
    
}
