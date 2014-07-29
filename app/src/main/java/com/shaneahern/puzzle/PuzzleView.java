package com.shaneahern.puzzle;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Main view class for puzzle. Takes square input bitmap and divides into equal sized bitmap tiles according to the number of rows and columns in the puzzle.
 * Allows touch interface to drag tiles to a new position, and animates tiles that are partially moved to the destination position if moved more than 50%
 * of the way to the new position, or else animates back to the starting position. Notifies registered listeners when a move has completed.
 *
 * @author  Shane Ahern (shane@shaneahern.com)
 */
public class PuzzleView extends View {

	
	/// 4 x 4 puzzle with starting empty postion at row 1, col 1
	public static final int PUZZLE_ROWS = 4;
	public static final int PUZZLE_COLS = 4;
	public static final int INITIAL_EMPTY_TILE_ROW = 0;
	public static final int INITIAL_EMPTY_TILE_COL = 0;

	// when tile dragged more than 50% of the distance to the next position, 
	// complete the move, otherwise return to original position
	private static final float PERCENT_THRESHOLD_FOR_DRAG_MOVE = 0.5f;
	// to distinguish touch drag events from click events
	private static final int MS_TOUCH_THRESHOLD_FOR_DRAG_DETECTION = 250;
	private static final float PERCENT_MOVEMENT_THRESHOLD_FOR_DRAG_DETECTION = 0.1f;
	
	// for tile animation
	private int mAnimationSpeed = 5; // number of ms between animation updates
	private int mAnimationIncrement = 2; // number of pixels moved in each animation update

	// thin white border around each tile
	private static final float BORDER_WIDTH = 2.0f;
	private static final int BORDER_COLOR = 0xffffffff;

	private Paint mBorderPaint;
	
	// listeners to be notified on each completed move
	List<PuzzleMoveListener> mMoveListeners;
	
	// overall puzzle screen dimensions
	int mWidth;
	int mHeight;
	
	// individual tile dimensions
	int mTileWidth;
	int mTileHeight;
	
	// tile state and positions in puzzle grid
	Tile[] mTiles;
	int[][] mPuzzlePositions;
	PuzzlePosition mEmptyPosition;
	
	//for touch input
	private float mStartTouchX;
	private float mStartTouchY;
	private float mTouchX;
	private float mTouchY;
	private long mTouchStartTime;
	private PuzzlePosition mActivePosition;
	
	// for tile animation
	private Handler mAnimationHandler = new Handler();
	private Paint mTilePaint;
	
	// holder class for tile state
	class Tile {
		public Bitmap tileImage;
		// touch movement delta from original position
		public int deltaX;
		public int deltaY;
		// target x, y position that tile should animate to on move end
		public int targetX;
		public int targetY;
		
		public Tile (Bitmap tileImage) {
			this.tileImage = tileImage;
			deltaX = 0;
			deltaY = 0;
			targetX = 0;
			targetY = 0;
		}
	}
	
	public PuzzleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public PuzzleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PuzzleView(Context context) {
		super(context);
		init(context);
	}
	
	public int getPuzzleWidth() {
		return mWidth;
	}
	
	private void init(Context context) {
		
		// show empty placeholder in xml layout viewer
		if (isInEditMode()) {
			mWidth = 480;
	        mHeight = 480;
			return;
		}
		
		// puzzle view will be a square thats fills display view width in portrait mode (app is locked to portrait)
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		mWidth = display.getWidth();
		mHeight = display.getWidth();
		
        setMeasuredDimension(mWidth, mHeight);
        
        mTilePaint = new Paint();
    	mTilePaint.setFilterBitmap(true);
    	
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStrokeWidth(BORDER_WIDTH);
    	mBorderPaint.setStyle(Paint.Style.STROKE);
    	mBorderPaint.setColor(BORDER_COLOR);
    	
        initTiles();
	}

	/**
     * Initialize tiles with the globe bitmap image stored in app resources
     *
     */
	private void initTiles() {
		
		mTiles = new Tile[PUZZLE_ROWS * PUZZLE_COLS];
        
        Bitmap globe = BitmapFactory.decodeResource(getResources(), R.drawable.globe);
        globe = Bitmap.createScaledBitmap(globe, mWidth, mWidth, true);
        
		initTiles(globe);
	}

	/**
     * Initialize tiles with the specified bitmap image, which will be divided into equal sized tile images with borders.
     * Image can be from resources, or other origin such as the Gallery or Camera
     *
     * @param image
     * 			Bitmap image to display in puzzle view
     */
	public void initTiles(Bitmap image) {
		int tilePos = 0;
        int x = 0;
        int y = 0;
        mTileWidth = image.getWidth() / PUZZLE_COLS;
        mTileHeight = image.getHeight() / PUZZLE_ROWS;
        for (int row=0; row < PUZZLE_ROWS; row++) {
        	for (int col=0; col < PUZZLE_COLS; col++) {
        		if (row == INITIAL_EMPTY_TILE_ROW && col == INITIAL_EMPTY_TILE_COL) {
        			// create tile with no image for empty position
        			mTiles[tilePos++] = new Tile(null);
        		} else {
        			if (mTiles[tilePos] != null) {
        				// if tile array contains tile from a previous image, recycle the bitmap
        				mTiles[tilePos].tileImage.recycle();
        				mTiles[tilePos] = null;
        			}
        			x = col * mTileWidth;
	        		y = row * mTileHeight;
	        		// extract a subset of the full image at the location for the current tile
	        		Bitmap tile = Bitmap.createBitmap(image, x, y, mTileWidth, mTileHeight);
	        		// add a border
	        		Canvas canvas = new Canvas(tile);
	        		canvas.drawRect(0, 0, mTileWidth, mTileHeight, mBorderPaint);
	        		// save ordered array of tiles
	        		mTiles[tilePos++] = new Tile(tile);
        		}
    		}
        }
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(mWidth, mHeight);
	}
	
	/**
     * Set values of tiles involved in a move after a click event is detected
     * 
     */
	public void tileClicked(float touchX, float touchY) {
		
		mActivePosition = getTouchPosition(touchX, touchY);
		
		for (int row=0; row < PUZZLE_ROWS; row++) {
        	for (int col=0; col < PUZZLE_COLS; col++) {
        		
        		if (this.isPartOfMove(row, col)) {
        			Tile tile = mTiles[mPuzzlePositions[row][col]];
        			if (canMoveRight()) {
        				tile.targetX = mTileWidth;
        			} else if (canMoveLeft()) {
        				tile.targetX = -mTileWidth;
        			}
        			if (canMoveDown()) {
        				tile.targetY = mTileHeight;
        			} else if (canMoveUp()) {
        				tile.targetY = -mTileHeight;
        			}
        		}
        	}
		}
		
		//faster speed when animating on click compared to after drag
		setAnimationSpeedFast();
		mAnimationHandler.post(mAnimationTask);
	}
	
	/**
     * Set values of tiles involved in a move after a drag touch event is detected
     * 
     */
	private void tileDragged() {
		for (int row=0; row < PUZZLE_ROWS; row++) {
			for (int col=0; col < PUZZLE_COLS; col++) {
				// when tile dragged more than 50% of the distance to the next position, 
				// animate to complete the move, otherwise animate return to original position
				if (this.isPartOfMove(row, col)) {
					Tile tile = mTiles[mPuzzlePositions[row][col]];
					tile.deltaX = getXTouchMove(row);
					if (((float)tile.deltaX / (float)mTileWidth) > PERCENT_THRESHOLD_FOR_DRAG_MOVE) {
						tile.targetX = mTileWidth;
					} else if ((float)(tile.deltaX / (float)mTileWidth) < -PERCENT_THRESHOLD_FOR_DRAG_MOVE) {
						tile.targetX = -mTileWidth;
					} else {
						tile.targetX = 0;
					}
					tile.deltaY = getYTouchMove(col);
					if (((float)tile.deltaY / (float)mTileHeight) > PERCENT_THRESHOLD_FOR_DRAG_MOVE) {
						tile.targetY = mTileHeight;
					} else if (((float)(tile.deltaY) / (float)mTileHeight) < -PERCENT_THRESHOLD_FOR_DRAG_MOVE) {
						tile.targetY = -mTileHeight;
					} else {
						tile.targetY = 0;
					}
				}
			}
		}
		
		// slower speed when animating after drag compared to on click
		setAnimationSpeedSlow();
		mAnimationHandler.post(mAnimationTask);
	}
	
	/**
     * Set animation values to create a slower animation speed
     * 
     */
	private void setAnimationSpeedSlow() {
		// number of ms between animation updates
		mAnimationSpeed = 5;
		// number of pixels moved in each animation update
		mAnimationIncrement = 2;
	}
	
	/**
     * Set animation values to create a faster animation speed
     * 
     */
	private void setAnimationSpeedFast() {
		// number of ms between animation updates
		mAnimationSpeed = 1;
		// number of pixels moved in each animation update
		mAnimationIncrement = 10;
	}
	
	/**
     * Update state of touch interface during active touch
     *
     * @param touchX
     * 				X coordinate of the touch event
     * @param touchY
     * 				Y coordinate of the touch event
     * @return PuzzlePosition corresponding to the x, y location of the touch position
     */
	private PuzzlePosition getTouchPosition(float touchX, float touchY) {
		int row = (int)((touchY/mHeight) * PUZZLE_ROWS);
		int col = (int)((touchX/mWidth) * PUZZLE_COLS);
		return new PuzzlePosition(row, col);
	}
	
	
	/**
     * Set state of touch interface, on touch start set isActive to true, on touch end set isActive to false.
     * 
     * @param isActive
     * 			Sets whether this is the beginning of the touch and drag event or the end
     * @param x
     * 			The x coordinate of the touch
     * @param y
     * 			The y coordinate of the touch
     */
	public void setIsTouchActive(boolean isActive, float x, float y) {
		
		if (isActive) {
			mActivePosition = getTouchPosition(x, y);
			mStartTouchX = x;
			mStartTouchY = y;
			mTouchStartTime = System.currentTimeMillis();
		} else {
			mTouchX = x;
			mTouchY = y;
			long touchDuration = System.currentTimeMillis() - mTouchStartTime;
			// get absolute movement distance for x and y from starting touch position
			float moveX = Math.abs(mStartTouchX - mTouchX);
			float moveY = Math.abs(mStartTouchY - mTouchY);
			if (isClickEvent(touchDuration, moveX, moveY)) {
				tileClicked(x, y);
			} else {
				tileDragged();
			}
		}
	}

	/**
     * 	Detect click events from touch event. To distinguish touch drag events from click events, 
     *  examine the touch duration and the amount of movement from the start touch position.
     * 
     * @param touchDuration
     * 			Sets whether this is the beginning of the touch and drag event or the end
     * @param moveX
     * 			The absolute change in the x position after touch end from the start touch x position
     * @param moveY
     * 			The absolute change in the y position after touch end from the start touch y position
     */
	private boolean isClickEvent(long touchDuration, float moveX, float moveY) {
		return touchDuration < MS_TOUCH_THRESHOLD_FOR_DRAG_DETECTION 
				&& moveX < mTileWidth * PERCENT_MOVEMENT_THRESHOLD_FOR_DRAG_DETECTION 
				&& moveY < mTileWidth * PERCENT_MOVEMENT_THRESHOLD_FOR_DRAG_DETECTION;
	}
	
	/**
     * Update state of touch interface during active touch
     * 
     * @param x
     * 			The x coordinate of the touch
     * @param y
     * 			The y coordinate of the touch
     */
	public void setTouchXY(float x, float y) {
		mTouchX = x;
		mTouchY = y;
		for (int row=0; row < PUZZLE_ROWS; row++) {
        	for (int col=0; col < PUZZLE_COLS; col++) {
        		
        		if (this.isPartOfMove(row, col)) {
        			Tile tile = mTiles[mPuzzlePositions[row][col]];
        			tile.deltaX = getXTouchMove(row);
        			tile.deltaY = getYTouchMove(col);
        		}
        	}
		}
		invalidate();
	}
	
	/**
     * Animation loop to move tiles that have been moved to a location in between two tile positions by touch input.
     * Animation loops with a delay between loops until all tiles have reached their target location. When all tiles
     * have reached their target location, reset their target  
     * 
     */
	private Runnable mAnimationTask = new Runnable() {

		public void run() {
		
			boolean animationComplete = true;
			for (int row=0; row < PUZZLE_ROWS; row++) {
	        	for (int col=0; col < PUZZLE_COLS; col++) {
	        		Tile tile = mTiles[mPuzzlePositions[row][col]];
	    			if (tile.deltaX < tile.targetX) {
	    				// tile delta from starting position has not reached target, so increment delta
	    				tile.deltaX += mAnimationIncrement;
	    				// make sure delta is not beyond starting or target location
	    				tile.deltaX = Math.min(tile.deltaX, mTileWidth);
	    				tile.deltaX = Math.min(tile.deltaX, tile.targetX);
	    				// continue animation until tile has reached target location
	    				animationComplete = false;
	    			} else if (tile.deltaX > tile.targetX) {
	    				// tile delta from starting position has not reached target, so increment delta
	    				tile.deltaX -= mAnimationIncrement;
	    				// make sure delta is not beyond starting or target location
	    				tile.deltaX = Math.max(tile.deltaX, -mTileWidth);
	    				tile.deltaX = Math.max(tile.deltaX, tile.targetX);
	    				// continue animation until tile has reached target location
	    				animationComplete = false;
	    			}
	    			if (tile.deltaY < tile.targetY) {
	    				// tile delta from starting position has not reached target, so increment delta
	    				tile.deltaY += mAnimationIncrement;
	    				// make sure delta is not beyond starting or target location
	    				tile.deltaY = Math.min(tile.deltaY, mTileHeight);
	    				tile.deltaY = Math.min(tile.deltaY, tile.targetY);
	    				// continue animation until tile has reached target location
	    				animationComplete = false;
	    			} else if (tile.deltaY > tile.targetY) {
	    				// tile delta from starting position has not reached target, so increment delta
	    				tile.deltaY -= mAnimationIncrement;
	    				// make sure delta is not beyond starting or target location
	    				tile.deltaY = Math.max(tile.deltaY, -mTileHeight);
	    				tile.deltaY = Math.max(tile.deltaY, tile.targetY);
	    				// continue animation until tile has reached target location
	    				animationComplete = false;
	    			}
	    		}
			}
			if (!animationComplete) {
				// continue animation after delay until all tiles have reached target location
				invalidate();
				mAnimationHandler.postDelayed(mAnimationTask, mAnimationSpeed);
			} else {
				// animate complete b/c all tiles have reached target location, so reset tile delta and target locations to 0
				boolean didMove = false;
				for (int row=0; row < PUZZLE_ROWS; row++) {
		        	for (int col=0; col < PUZZLE_COLS; col++) {
		        		Tile tile = mTiles[mPuzzlePositions[row][col]];
		        		//if any tiles have a non-zero target x or y coordinate, at least one tile has moved
		        		if (tile.targetX != 0 || tile.targetY != 0) {
		        			didMove = true;
		        		} 
		        		tile.deltaX = 0;
		        		tile.deltaY = 0;
		        		tile.targetX = 0;
		        		tile.targetY = 0;
		        	}
				}
				if (didMove) {
					//at least one tile has moved to a new position, so need to update board
	    			notifyMoveListeners(mActivePosition);
				}
			}
		}
	};
	
	/**
     * Register to be notified when a puzzle tile position has changed 
     * 
     * @param listener
     * 			Listener that wants to be notified of move events
     */
	public void addMoveListener(PuzzleMoveListener listener) {
		if (mMoveListeners == null) {
			mMoveListeners = new ArrayList<PuzzleMoveListener>();
		}
		mMoveListeners.add(listener);
	}
	
	/**
     * Notify all registered listeners that the tile at position p has moved 
     * 
     * @param p
     * 			The position of the move event to broadcast to all listeners
     */
	private void notifyMoveListeners(PuzzlePosition p) {
		if (mMoveListeners != null) {
			for (PuzzleMoveListener listener :  mMoveListeners) {
				listener.puzzleMoved(p);
			}
		}
	}
	
	/**
     * Test if the currently selected tile is moving and the tile at row, col is also moving along with it
     * 
     * @return boolean true if the currently selected tile is moving and the tile at row, col is also moving along with it
     */
	private boolean isPartOfMove(int row, int col) {
		boolean isPartOfMove = false;
		
		if (mActivePosition != null) {
			if (mActivePosition.getRow() == row && mEmptyPosition.getRow() == row) {
				if (canMoveLeft()) {
					if (col > mEmptyPosition.getCol() && col <= mActivePosition.getCol()) {
						isPartOfMove = true;
					}
				} else if (canMoveRight()) {
					if (col < mEmptyPosition.getCol() && col >= mActivePosition.getCol()) {
						isPartOfMove = true;
					}
				}
			}
			if (mActivePosition.getCol() == col && mEmptyPosition.getCol() == col) {
				if (canMoveDown()) {
					if (row < mEmptyPosition.getRow() && row >= mActivePosition.getRow()) {
						isPartOfMove = true;
					}
				} else if (canMoveUp()) {
					if (row > mEmptyPosition.getRow() && row <= mActivePosition.getRow()) {
						isPartOfMove = true;
					}
				}
			}
		}
		
		return isPartOfMove;
	}
	
	/**
     * Test if currently selected position is above the empty position and so can move down
     * 
     * @return boolean true if the currently selected position is above the empty position and so can move down
     */
	private boolean canMoveDown() {
		return mActivePosition.getRow() < mEmptyPosition.getRow();
	}
	
	/**
     * Test if currently selected position is below the empty position and so can move up
     * 
     * @return boolean true if the currently selected position is below the empty position and so can move up
     */
	private boolean canMoveUp() {
		return mActivePosition.getRow() > mEmptyPosition.getRow();
	}
	
	/**
     * Test if currently selected position is to the right of the empty position and so can move left
     * 
     * @return boolean true if the currently selected position is to the right of the empty position and so can move left
     */
	private boolean canMoveLeft() {
		return mActivePosition.getCol() > mEmptyPosition.getCol();
	}
	
	/**
     * Test if currently selected position is to the left of the empty position and so can move right
     * 
     * @return boolean true if the currently selected position is to the left of the empty position and so can move right
     */
	private boolean canMoveRight() {
		return mActivePosition.getCol() < mEmptyPosition.getCol();
	}

	/**
     * For any tiles in the same row as the currently selected tile, compute the amount of movement along the x axis.
     * Movement is restricted to the starting position of the tile and one tile in the direction of the empty tile.
     * 
     * @param row
     * 			The row to examine
     * @return int the amount of movement along the x axis
     */
	private int getXTouchMove(int row) {
		int moveX = 0;
		if (mActivePosition != null) {
			if (mActivePosition.getRow() == row) {
				if (canMoveRight()) {
					// move the difference between the current touch location and the starting touch location
					moveX = (int)(mTouchX - mStartTouchX);
					// don't move beyond start position or next tile position
					moveX = Math.min(moveX, mTileWidth);
					moveX = Math.max(moveX, 0);
				} else if (canMoveLeft()) {
					// move the difference between the current touch location and the starting touch location
					moveX = (int)(mTouchX - mStartTouchX);
					// don't move beyond start position or next tile position
					moveX = Math.max(moveX, - mTileWidth);
					moveX = Math.min(moveX, 0);
				}
			}
		}
		return moveX;
	}
	
	/**
     * For any tiles in the same column as the currently selected tile, compute the amount of movement along the y axis.
     * Movement is restricted to the starting position of the tile and one tile in the direction of the empty tile  
     * 
     * @param col
     * 			The column to examine
     * @return int the amount of movement along the y axis
     */
	private int getYTouchMove(int col) {
		int moveY = 0;
		if (mActivePosition != null) {
			if (mActivePosition.getCol() == col) {
				if (canMoveDown()) {
					// move the difference between the current touch location and the starting touch location
					moveY = (int)(mTouchY - mStartTouchY);
					// don't move beyond start position or next tile position
					moveY = Math.min(moveY, mTileHeight);
					moveY = Math.max(moveY, 0);
				} else if (canMoveUp()) {
					// move the difference between the current touch location and the starting touch location
					moveY = (int)(mTouchY - mStartTouchY);
					// don't move beyond start position or next tile position
					moveY = Math.max(moveY, -mTileHeight);
					moveY = Math.min(moveY, 0);
				}
			}
		}
		return moveY;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		if (mPuzzlePositions != null && mEmptyPosition != null) {
			// draw all tiles at their current positions in the puzzle grid, with any in progress touch or animation 
			
			int x = 0;
	        int y = 0;
	        for (int row=0; row < PUZZLE_ROWS; row++) {
	        	for (int col=0; col < PUZZLE_COLS; col++) {
	        		
	        		Tile tile = mTiles[mPuzzlePositions[row][col]];
	        		
	        		// deltaX and deltaY for tile reflect any in progress touch or animation movement
	        		x = col * mTileWidth  + tile.deltaX;
	        		y = row * mTileHeight + tile.deltaY;
	        		
	        		Bitmap tileImage = tile.tileImage;
	        		if (tileImage != null) {
	        			canvas.drawBitmap(tileImage, x, y, mTilePaint);
	        		}
	        	}
	        }
		}
	}
	
	/**
     * Update puzzle tile positions and redraw
     * 
     * @param puzzlePositions
     * 				The set of current puzzle positions
     * @param emptyPosition
     * 				The location of the positon that is currently empty
     */
	public void setPuzzlePositions(int[][] puzzlePositions, PuzzlePosition emptyPosition) {
		mEmptyPosition = emptyPosition;
		mPuzzlePositions = puzzlePositions;
		invalidate();
	}

}
