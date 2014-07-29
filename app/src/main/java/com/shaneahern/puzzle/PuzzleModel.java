package com.shaneahern.puzzle;

/**
 * Model class for puzzle. Stores the grid of puzzle tile positions, with an empty tile at a specified position.
 * The position of the empty tile determines which other tiles are available for legal moves.
 * If a move origin position is specified for which a move is possible, the model will update the positions grid,
 * and the move origin position becomes the empty position. Illegal moves do not change the positions grid. 
 *
 * @author  Shane Ahern (shane@shaneahern.com)
 */
public class PuzzleModel {
	
	// storage of puzzle grid and empty position
	private int[][] mPuzzlePositions;
	private PuzzlePosition mEmptyPosition;
	private PuzzlePosition mInitialEmptyPosition;
	
	private int mRows;
	private int mCols;
	
	public PuzzleModel(int rows, int cols, PuzzlePosition initalEmptyPosition) {
		mRows = rows;
		mCols = cols;
		mPuzzlePositions = new int[mRows][mCols];
		mInitialEmptyPosition = initalEmptyPosition;
		mEmptyPosition = mInitialEmptyPosition;
		
		initPuzzle();
	}
	
	/**
     * Initialize or restore puzzle to its original ordered state
     *
     */
	public void initPuzzle() {
		mEmptyPosition = mInitialEmptyPosition;
		int puzzleId = 0;
		for (int row=0; row < mRows; row++) {
			for (int col=0; col < mCols; col++) {
				mPuzzlePositions[row][col] = puzzleId++;
			}
		}
	}

	/**
     * Move puzzle tiles starting from the specified position. From any puzzle position, 
     * there is either no legal move, or one possible move, depending on the location of the empty tile.
     * If no legal move is possible from the specified position, the state of the puzzle model will be
     * unchanged. A legal move is possible if the empty tile is either in the same row, or the same 
     * column as the move starting tile. If a legal move is possible, the tile at the starting position,
     * along with any tiles between it and the empty tile, will move one tile towards in the direction,
     * along either the row or column, of the empty tile, and the positions of tiles in the model is 
     * updated accordingly. Finally, the location of the empty tile which is now at the location of the 
     * starting position of the move, will be updated. 
     *
     * @param p
     * 			Position from which move should originate
     */
	public void move(PuzzlePosition p) {
		int row = p.getRow();
		int col = p.getCol();
		
		
		if (rowContainsEmptyPosition(row)) {
			// the starting position is in the same row as the empty position, legal move along row is possible
			
			if (col < mEmptyPosition.getCol()) {
				// starting position column is to the left of the empty position, shift tiles right
				for (int i=mEmptyPosition.getCol(); i > col; i--) {
					mPuzzlePositions[row][i] = mPuzzlePositions[row][i-1];
				}
			} else {
				// starting position column is to the right of the empty position, shift tiles left
				for (int i=mEmptyPosition.getCol(); i < col; i++) {
					mPuzzlePositions[row][i] = mPuzzlePositions[row][i+1];
				}
			}
			// update empty position to locaton of move starting position
			mPuzzlePositions[row][col] = 0;
			mEmptyPosition = new PuzzlePosition(row, col);
			
		} else if (colContainsEmptyPosition(col)) {
			// the starting position is in the same column as the empty position, legal move along column is possible
			
			if (row < mEmptyPosition.getRow()) {
				// starting position row is above of the empty position, shift tiles down
				for (int i=mEmptyPosition.getRow(); i > row; i--) {
					mPuzzlePositions[i][col] = mPuzzlePositions[i-1][col];
				}
			} else {
				// starting position row is below of the empty position, shift tiles up
				for (int i=mEmptyPosition.getRow(); i < row; i++) {
					if (mPuzzlePositions[i+1][col] != 0) {
						mPuzzlePositions[i][col] = mPuzzlePositions[i+1][col];
					}
				}
			}
			// update empty position to locaton of move starting position
			mPuzzlePositions[row][col] = 0;
			mEmptyPosition = new PuzzlePosition(row, col);
			
		}
	}
	
	/**
     * Test if the row contains the empty position
     * 
     * @param row
     * 			Row to check
     * @return true if the the specified row contains the empty position
     */
	public boolean rowContainsEmptyPosition(int row) {
		return mEmptyPosition.getRow() == row;
	}
	
	/**
     * Test if the column contains the empty position
     * 
     * @param col
     * 			Column to check
     * @return true if the the specified column contains the empty position
     */
	public boolean colContainsEmptyPosition(int col) {
		return mEmptyPosition.getCol() == col;
	}
	
	/**
    * Test if the position is the empty position
    *
    * @param p
    * 		Position to test
    * @return true if the the specified position is the empty position
    */
	public boolean positionIsEmpty(PuzzlePosition p) {
		return mEmptyPosition.getRow() == p.getRow() && mEmptyPosition.getCol() == p.getCol();
	}
	
	/**
    * Getter for the current tile position
    * 
    * @return int[][] containing the current state of the puzzle tile position 
    */
	public int[][] getPuzzlePositions() {
		return mPuzzlePositions;
	}
	
	/**
    * Getter for the empty postions
    * 
    * @return Position containing empty position
    */
	public PuzzlePosition getEmptyPosition() {
		return mEmptyPosition;
	}
	
	
}
