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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Note_common {

    private EditText mTitleEditText;
    private ImageView mTitleImageView;
    private String mPictureFileName;
    private EditText mBodyEditText;
    private String mOriginalTitle;
    private String mOriginalBody;
    private DB mDb;
    SharedPreferences mPref_style;
    SharedPreferences mPref_delete_warn;
    Activity mAct;

    public Note_common(Activity act)
    {
    	mAct = act;
    }
    
    void UI_init()
    {
    	DB.setTableNumber(DB.getTableNumber()); // add for install first time error
        mDb = new DB(mAct);

        mTitleEditText = (EditText) mAct.findViewById(R.id.edit_title);
        mTitleImageView = (ImageView) mAct.findViewById(R.id.edit_picture);
        mBodyEditText = (EditText) mAct.findViewById(R.id.edit_body);
        
        mDb.doOpen();
		int style = mDb.getTabStyle(TabsHost.mCurrentTabIndex);
		mDb.doClose();
        
		//set title color
		mTitleEditText.setTextColor(Util.mText_ColorArray[style]);
		mTitleEditText.setBackgroundColor(Util.mBG_ColorArray[style]);
		
		mTitleImageView.setBackgroundColor(Util.mBG_ColorArray[style]);
		
		//set body color 
		mBodyEditText.setTextColor(Util.mText_ColorArray[style]);
		mBodyEditText.setBackgroundColor(Util.mBG_ColorArray[style]);	
    }
    
    void deleteNote(Long rowId)
    {
        mDb.open();
        // for Add new note (mRowId is null first), but decide to cancel 
        if(rowId != null)
        	mDb.delete(rowId);
        mDb.close();
    }
    
    void populateFields(Long rowId) {
    	Long mRowId = rowId;
    	mDb.open();
        if (mRowId != null) {
            Cursor note = mDb.get(mRowId);
            
            String strTitleEdit = note.getString(note.getColumnIndexOrThrow(DB.KEY_NOTE_TITLE));
            mTitleEditText.setText(strTitleEdit);
            mTitleEditText.setSelection(strTitleEdit.length());
            
            ///
//            {
//			    String dirString = Environment.getExternalStorageDirectory().toString() + 
//			    		              "/" + Util.getAppName(mAct);
//			    
//				File dir = new File(dirString);
//				if(!dir.isDirectory())
//					dir.mkdir();
//				
//				mPictureFileName = note.getString(note.getColumnIndexOrThrow(DB.KEY_NOTE_PICTURE));
//				File photo = new File(dir,  mPictureFileName);
//			    Uri imageUri = Uri.fromFile(photo);
//            }
    		mPictureFileName = note.getString(note.getColumnIndexOrThrow(DB.KEY_NOTE_PICTURE));
    		Uri imageUri = Util.getPictureUri(mPictureFileName, mAct);
            // set picture
            ImageView imageView = (ImageView) mAct.findViewById(R.id.edit_picture);
            ContentResolver cr = mAct.getContentResolver();
            Bitmap bitmap, thumbNail = null;
			try 
			{
				bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, imageUri);
				thumbNail = Bitmap.createScaledBitmap(bitmap, 50, 50, false);
				imageView.setImageBitmap(thumbNail);
			} 
			catch (Exception e) 
			{
                Toast.makeText(mAct, "Failed to load", Toast.LENGTH_SHORT).show();
		        Log.e("Camera: Failed to load", e.toString());
		    }
            ///
            
            String strBodyEdit = note.getString(
          		note.getColumnIndexOrThrow(DB.KEY_NOTE_BODY));
            mBodyEditText.setText(strBodyEdit);
            mBodyEditText.setSelection(strBodyEdit.length());
            
            // keep original strings
            mOriginalTitle = strTitleEdit;
            mOriginalBody = strBodyEdit;
        }
        mDb.close();
    }
    
    boolean isModified()
    {
    	boolean bModify = false;
    	String curTitle = mTitleEditText.getText().toString();
    	String curBody = mBodyEditText.getText().toString();
    	if(!mOriginalTitle.equals(curTitle) ||
    	   !mOriginalBody.equals(curBody)	)
    	{
    		bModify = true;
    	}
    	
    	return bModify;
    }
    
    boolean isEdited()
    {
    	boolean bEdit = false;
    	String curTitle = mTitleEditText.getText().toString();
    	String curBody = mBodyEditText.getText().toString();
    	if(!curTitle.isEmpty() || !curBody.isEmpty() )    		
    		bEdit = true;
    	
    	return bEdit;
    }
    
//    Long saveState(Long rowId,boolean enSaveDb) {
//    	boolean mEnSaveDb = enSaveDb;
//    	Long mRowId = rowId;
//    	mDb.open();
//        String title = mTitleEditText.getText().toString();
//        String body = mBodyEditText.getText().toString();
//
//        if(mEnSaveDb)
//        {
//	        if (mRowId == null) // for Add new
//	        {
//	        	if( (!title.isEmpty()) || (!body.isEmpty()) )
//	        		mRowId = mDb.insert(title, "", body, (long) 0);// add new note, get return row Id
//	        } 
//	        else // for Edit
//	        {
//    	        Date now = new Date(); 
//	        	if( (!title.isEmpty()) || (!body.isEmpty()) )
//	        		mDb.updateNote(mRowId, title,"",  body, 0, now.getTime()); // update note
//	        	else if(title.isEmpty() && body.isEmpty() )
//	        		mDb.delete(mRowId);
//	        }
//        }
//        mDb.close();
//		return mRowId;
//    }

	public Long saveState(Long rowId,boolean enSaveDb, String mPictureFileName) {
		boolean mEnSaveDb = enSaveDb;
    	Long mRowId = rowId;
    	mDb.open();
        String title = mTitleEditText.getText().toString();
        String body = mBodyEditText.getText().toString();

        if(mEnSaveDb)
        {
	        if (mRowId == null) // for Add new
	        {
	        	if( (!title.isEmpty()) || (!body.isEmpty()) )
	        		mRowId = mDb.insert(title, mPictureFileName, body, (long) 0);// add new note, get return row Id
	        } 
	        else // for Edit
	        {
    	        Date now = new Date(); 
	        	if( (!title.isEmpty()) || (!body.isEmpty()) )
	        		mDb.updateNote(mRowId, title, mPictureFileName, body, 0, now.getTime()); // update note
	        	else if(title.isEmpty() && body.isEmpty() )
	        		mDb.delete(mRowId);
	        }
        }
        mDb.close();
		return mRowId;
	}
}
