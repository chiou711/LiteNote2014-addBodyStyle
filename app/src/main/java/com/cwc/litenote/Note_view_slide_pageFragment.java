package com.cwc.litenote;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a single step in a wizard. The fragment shows a dummy title indicating
 * the page number, along with some dummy text.
 *
 * <p>This class is used by the {@link CardFlipActivity} and {@link
 * Note_view_slide} samples.</p>
 */
public class Note_view_slide_pageFragment extends Fragment implements OnTouchListener {
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;
    
    static int mEntryPosition;
    static String mStrTitle;
    static String mStrBody;
    static String mStrTime;
    static String mEntryStrTitle;
    static String mEntryStrBody;
    static String mEntryStrTime;
    static String mBeforeEntryStrTitle;
    static String mBeforeEntryStrBody;
    static String mBeforeEntryStrTime;
    static int mStyle;
    TextView mTxtVw_Title;
    TextView mTxtVw_Body;
    TextView mTxtVw_Time;
    
    
    
    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     * @param mRowId 
     */
    public static Note_view_slide_pageFragment create(int pageNumber, int entryPosition,
    					String strTitle,String strBody,Long time, int style) 
    {
        Note_view_slide_pageFragment fragment = new Note_view_slide_pageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        mEntryPosition = entryPosition;
        mStrTitle = strTitle;
        mStrBody = strBody;
        mStrTime = Util.getTimeString(time);
        mStyle = style;
        
        if(pageNumber == mEntryPosition) // keep entry note strings
        {
        	mEntryStrTitle = strTitle;
        	mEntryStrBody = strBody;
        	mEntryStrTime = Util.getTimeString(time);
        }
        else if(pageNumber == (mEntryPosition-1)) // keep before_entry note strings
        {
        	mBeforeEntryStrTitle = strTitle;
        	mBeforeEntryStrBody = strBody;
        	mBeforeEntryStrTime = Util.getTimeString(time);
        }
        return fragment;
    }

    public Note_view_slide_pageFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.note_view_slide_page_fragment,
        													container, false);
        
        // set background color
        rootView.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
        
        mTxtVw_Title = ((TextView) rootView.findViewById(R.id.textTitle));
        mTxtVw_Body = ((TextView) rootView.findViewById(R.id.textBody));
        mTxtVw_Time = ((TextView) rootView.findViewById(R.id.textTime));
        
        // set title text color
        mTxtVw_Title.setTextColor(Util.mText_ColorArray[mStyle]);
//      mTxtVw_Title.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
		//set body text color 
        mTxtVw_Body.setTextColor(Util.mText_ColorArray[mStyle]);
//		mTxtVw_Body.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
        mTxtVw_Time.setTextColor(Util.mText_ColorArray[mStyle]);
        
        if(mPageNumber == mEntryPosition)
        {
        	mTxtVw_Title.setText(mEntryStrTitle);
        	mTxtVw_Body.setText(mEntryStrBody);
        	mTxtVw_Time.setText(mEntryStrTime);
        }
        else if( mPageNumber == (mEntryPosition - 1) )
        {
        	mTxtVw_Title.setText(mBeforeEntryStrTitle);
        	mTxtVw_Body.setText(mBeforeEntryStrBody);
        	mTxtVw_Time.setText(mBeforeEntryStrTime);
        }
        else
        {
        	mTxtVw_Title.setText(mStrTitle);
        	mTxtVw_Body.setText(mStrBody);
        	mTxtVw_Time.setText(mStrTime);
        }
        
        //A
//        mTxtVw_Title.setTextSize(mRatio + 13);
//        mTxtVw_Body.setTextSize(mRatio + 13);

        scaleGestureDetector = new ScaleGestureDetector(getActivity(), new simpleOnScaleGestureListener());
        rootView.setOnTouchListener(Note_view_slide_pageFragment.this);
        
        return rootView;
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }
    
    
//    // A
//	    final static float STEP = 200;
//	    float mRatio = 1.0f;
//	    int mBaseDist;
//	    float mBaseRatio;
//	    float fontsize = 13;
//		
//	    public boolean onTouchEvent(MotionEvent event) 
//	    {
//	      if (event.getPointerCount() == 2) {
//	          int action = event.getAction();
//	          int pureaction = action & MotionEvent.ACTION_MASK;
//	          if (pureaction == MotionEvent.ACTION_POINTER_DOWN) {
//	              mBaseDist = getDistance(event);
//	              mBaseRatio = mRatio;
//	          } else {
//	              float delta = (getDistance(event) - mBaseDist) / STEP;
//	              float multi = (float) Math.pow(2, delta);
//	              mRatio = Math.min(1024.0f, Math.max(0.1f, mBaseRatio * multi));
//	              mTxtVw_Title.setTextSize(mRatio + 13);
//	              mTxtVw_Body.setTextSize(mRatio + 13);
//	          }
//	      }
//	      return true;
//	  }
//	
//	  int getDistance(MotionEvent event) 
//	  {
//	      int dx = (int) (event.getX(0) - event.getX(1));
//	      int dy = (int) (event.getY(0) - event.getY(1));
//	      return (int) (Math.sqrt(dx * dx + dy * dy));
//	  }
//	
//		@Override
//		public boolean onTouch(View v, MotionEvent event) {
//			onTouchEvent(event);
//			return false;
//		}
//    //
    
    
    
    // B
    //pinch zooming
    ScaleGestureDetector scaleGestureDetector;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		scaleGestureDetector.onTouchEvent(event);
		return false;
	}
	
	int MAX_SIZE = 719;
	int MIN_SIZE = 24;
	
    public class simpleOnScaleGestureListener extends SimpleOnScaleGestureListener 
    {
    	
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
		    float sizeTitle = mTxtVw_Body.getTextSize();
		    float sizeBody = mTxtVw_Body.getTextSize();
		    float sizeTime = mTxtVw_Time.getTextSize();
		    Log.d("TextSizeStart", String.valueOf(sizeBody));
		
		    float factor = detector.getScaleFactor();
		    Log.d("Factor", String.valueOf(factor));
		
		    float productTitle = sizeTitle*factor;
		    float productBody = sizeBody*factor;
		    float productTime = sizeTime*factor;
		    
		    
		    productTitle = Math.min(productTitle, MAX_SIZE);
		    productTitle = Math.max(productTitle, MIN_SIZE);
		    productBody = Math.min(productBody, MAX_SIZE);
		    productBody = Math.max(productBody, MIN_SIZE);
		    productTime = Math.min(productTime, MAX_SIZE);
		    productTime = Math.max(productTime, MIN_SIZE);
		    
		    Log.d("TextSize", String.valueOf(productBody));
		    mTxtVw_Title.setTextSize(TypedValue.COMPLEX_UNIT_PX, productTitle);
		    mTxtVw_Body.setTextSize(TypedValue.COMPLEX_UNIT_PX, productBody);
		    mTxtVw_Time.setTextSize(TypedValue.COMPLEX_UNIT_PX, productTime);
		
		    sizeTitle = mTxtVw_Title.getTextSize();
		    sizeBody = mTxtVw_Body.getTextSize();
		    sizeTime = mTxtVw_Time.getTextSize();
		    
		    Log.d("TextSizeEnd", String.valueOf(sizeBody));
		    return true;
		}
		
	}
}
