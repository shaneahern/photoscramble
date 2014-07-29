package com.shaneahern.puzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

/**
 * Main activity for puzzle. Implements listeners for the sensor module (to detect device "shake" action),
 * on touch and on click listeners for user input and a app specific listener for puzzle moves.
 *
 * @author  Shane Ahern (shane@shaneahern.com)
 */
public class PuzzleActivity extends Activity implements OnTouchListener, SensorEventListener, OnClickListener, PuzzleMoveListener {

	// constants for motion detection to detect device shake action
	private static final float SHAKE_MOVEMENT_RATIO_THRESHOLD = 0.1f;
    private static final int MOTION_SENSOR_UPDATE_FREQUENCY_MS = 100;
	
    // result code for gallery image selection intent
	private static final int ACTIVITY_SELECT_IMAGE = 0;
	
    PuzzleView mView;
	PuzzleController mController;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    
    private Button mResetPuzzleButton;
    private Button mGalleryButton;
    
	// For shake motion detection.
    private long mLastMotionUpdate = -1;
    private float mMotionX, mMotionY, mMotionZ;
    private float mLastMotionX, mLastMotionY, mLastMotionZ;
    

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setContentView(R.layout.main);
        
        mView = (PuzzleView) findViewById(R.id.puzzleView);
        mView.setOnTouchListener(this);
        mView.setOnClickListener(this);
        
        mController = new PuzzleController(mView);
        mView.addMoveListener(this);
        
        mResetPuzzleButton = (Button)findViewById(R.id.btnResetPuzzle);
        mResetPuzzleButton.setOnClickListener(this);
        mGalleryButton = (Button)findViewById(R.id.btnGallery);
        mGalleryButton.setOnClickListener(this);
        
        mView.setPuzzlePositions(mController.getModel().getPuzzlePositions(), mController.getModel().getEmptyPosition());
    }
	
	@Override
	protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

	@Override
	protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

	@Override
	public void puzzleMoved(PuzzlePosition p) {
		mController.move(p);
		if (mController.isPuzzleSolved()) {
			showPuzzleSolvedDialog();
			mController.resetNumMoves();
		}
	}

	/**
     * Shows message dialog to be displayed when puzzle is solved
     * 
     */
	private void showPuzzleSolvedDialog() {
		String strSolvedMsg = String.format(getResources().getString(R.string.puzzle_solved_message), mController.getNumMoves());			
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(R.string.congratulations);
		alertDialog.setMessage(strSolvedMsg);
		alertDialog.setButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  dialog.dismiss();
		    } });
		alertDialog.show();
	}
    
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float touchX = event.getX();
		float touchY = event.getY();

		switch (event.getAction()) { 

        case MotionEvent.ACTION_DOWN: // touch down 
        	// set start touch x ,y 
        	mView.setIsTouchActive(true, touchX, touchY);
        	break; 


        case MotionEvent.ACTION_MOVE:   // touch drag
        	// update touch x, y as touch moves
        	mView.setTouchXY(touchX, touchY);
        	break; 

        case MotionEvent.ACTION_UP: // touch up
        	// set end touch x, y
        	mView.setIsTouchActive(false, touchX, touchY);
        	break; 
        } 
        return true; 
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
		    long currentTime = System.currentTimeMillis();
		    if ((currentTime - mLastMotionUpdate) > MOTION_SENSOR_UPDATE_FREQUENCY_MS) {
				long diffTime = (currentTime - mLastMotionUpdate);
				mLastMotionUpdate = currentTime;
		 
				// store current x, y, z values
				mMotionX = event.values[SensorManager.DATA_X];
				mMotionY = event.values[SensorManager.DATA_Y];
				mMotionZ = event.values[SensorManager.DATA_Z];
		 
				// compute ratio of delta from previous values to time since last sensor reading
				float speed = Math.abs(mMotionX + mMotionY + mMotionZ - mLastMotionX - mLastMotionY - mLastMotionZ) / diffTime;
				if (speed > SHAKE_MOVEMENT_RATIO_THRESHOLD) {
				    shufflePuzzle();
				}
				
				// save as last x, y, z
				mLastMotionX = mMotionX;
				mLastMotionY = mMotionY;
				mLastMotionZ = mMotionZ;
		    }
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//required to implement SensorEventListener
	}
	
	/**
     * Generate a series of random legal moves to shuffle puzzle positions
     * 
     */
	private void shufflePuzzle() {
		
		for (int i=0; i < 100; i++) {
			int row = (int)(Math.random() * PuzzleView.PUZZLE_ROWS);
			int col = (int)(Math.random() * PuzzleView.PUZZLE_COLS);
			
			if (mController.isMoveLegal(row, col)) {
				mController.move(row, col);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == mResetPuzzleButton.getId()) {
	        
			mController.resetPuzzle();
		} else if (v.getId() == mGalleryButton.getId()) {
			
	        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	        startActivityForResult(i, ACTIVITY_SELECT_IMAGE); 
		} else if (v.getId() == mView.getId()) {
			Log.i("PuzzleView", ""+ v.getId());
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) { 
	    super.onActivityResult(requestCode, resultCode, returnedIntent); 

	    switch(requestCode) { 
	    case ACTIVITY_SELECT_IMAGE:
	    	// Handle activty result for gallery image selection 
	        if(resultCode == RESULT_OK){

                Uri imageUri = returnedIntent.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    useImageFileInPuzzle(resizeAndCropSquare(bitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (bitmap == null) {
	    			// in some cases, no image is returned, if for example an image is selected from a Picassa album where the image is not stored locally
	    			Toast.makeText(this, R.string.load_photo_error, Toast.LENGTH_LONG).show();
	    		}
	        }
	    
	    }
	}

	/**
     * Reinitialize the puzzle with the specified image
     * 
     * @param imageBitmap
     * 				Image bitmap to display in puzzle view
     */
	private void useImageFileInPuzzle(Bitmap imageBitmap) {
		mView.initTiles(imageBitmap);
		mController.resetPuzzle();
	}
	
	/**
     * Get a bitmap for the image at the specified file path, scale, re-orient, and crop to square
     * 
     * @param filePath
     * 				Path to image file
     * @param orientation
     * 				Image orientation (should normally be 0, 90, 180 or 270)
     * @return Bitmap image resized in correct orientation, cropped to square
     */
	private Bitmap getBitmapFromPath(String filePath, int orientation) {

		BitmapFactory.Options opts = new BitmapFactory.Options();
		// down-sample large images
		opts.inSampleSize = 4;

		Bitmap imageBitmap = BitmapFactory.decodeFile(filePath, opts);
				
		// rotate according to orientation stored for image in media store, if needed
		if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
        }
		
		imageBitmap = resizeAndCropSquare(imageBitmap);
		
		return imageBitmap;
	}
	
	/**
     * Resize the specified bitmap to fit view width and crop to square
     * 
     * @param imageBitmap
     * 			Image bitmap to be resized and cropped
     * @return Bitmap image resized to fit view width and cropped to square
     */
	public Bitmap resizeAndCropSquare(Bitmap imageBitmap) {
		
		//compute scaled dimensions based on puzzle width
		float aspectRatio = (float)imageBitmap.getHeight() / (float)imageBitmap.getWidth();
		int newWidth = mView.getPuzzleWidth();
		int newHeight = (int)(newWidth * aspectRatio);

		//ensure that image height equal or greater than width in order to crop a square image based on width
		if (newHeight < newWidth) {
			newHeight = newWidth;
			newWidth = (int)((float)	newWidth / aspectRatio);
		}
		
		// resize the bitmap
		int width = imageBitmap.getWidth();
		int height = imageBitmap.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
	
		//get new bitmap with scaled dimensions
		imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, width, height, matrix, false);
		
		int squareDim = mView.getPuzzleWidth();
		//crop to square
		imageBitmap = Bitmap.createBitmap(imageBitmap, 
											(imageBitmap.getWidth() - squareDim) / 2,
											(imageBitmap.getHeight() - squareDim) / 2,
											squareDim, 
											squareDim);

		return imageBitmap;

	}

}