package com.shaneahern.puzzle;

/**
 * Controller for puzzle, relays moves to model, checks if moves are legal, resets puzzle to orignal state, checks if puzzle is solved.
 *
 * @author  Shane Ahern (shane@shaneahern.com)
 */
public class PuzzleController {

	PuzzleModel mModel;
	PuzzleView mView;
	
	private int mNumMoves;
	
	public PuzzleController(PuzzleView view) {
		mView = view;
		mModel = new PuzzleModel(PuzzleView.PUZZLE_ROWS, PuzzleView.PUZZLE_COLS, new PuzzlePosition(PuzzleView.INITIAL_EMPTY_TILE_ROW, PuzzleView.INITIAL_EMPTY_TILE_COL));
		// first move from initial position doesn't count towards moves to solve puzzle, so set to -1, first move will set to 0
		mNumMoves = -1;
	}
	
	public PuzzleController() {
		mModel = new PuzzleModel(PuzzleView.PUZZLE_ROWS, PuzzleView.PUZZLE_COLS, new PuzzlePosition(PuzzleView.INITIAL_EMPTY_TILE_ROW, PuzzleView.INITIAL_EMPTY_TILE_COL));
		// first move from initial position doesn't count towards moves to solve puzzle, so set to -1, first move will set to 0
		mNumMoves = -1;
	}
		
	/**
     * Test if any move from row, col is legal
     * 
     * @param row
     * 			Row of move
     * @param col
     * 			Column of move
     * @return true if there is a legal move from row, col
     */
	public boolean isMoveLegal(int row, int col) {
		return isMoveLegal(new PuzzlePosition(row, col));
	}
	
	/**
     * Test if any move from position p is legal
     * 
     * @param p
     * 			Position of move
     * @return true if there is a legal move from p
     */
	public boolean isMoveLegal(PuzzlePosition p) {
		return !mModel.positionIsEmpty(p) && (mModel.rowContainsEmptyPosition(p.getRow()) || mModel.colContainsEmptyPosition(p.getCol()));
	}
	
	/**
     * Make move from row, col if legal
     * 
     * @param row
     * 			Row of move
     * @param col
     * 			Column of move
     * @return true if there is a legal move from row, col, move succeeded and model updated
     */
	public boolean move(int row, int col) {
		return move(new PuzzlePosition(row, col));
	}	
	
	/**
     * Make move from position p if legal
     * 
     * @param p
     * 			Postion of move
     * @return true if there is a legal move from p, move succeeded and model updated
     */
	public boolean move(PuzzlePosition p) {
		boolean moveSuccess = false;
		
		if (isMoveLegal(p)) {
			
			mModel.move(p);
			
			if (mView != null) {
				mView.setPuzzlePositions(mModel.getPuzzlePositions(), mModel.getEmptyPosition());
			}
			
			moveSuccess = true;
			
			mNumMoves++;
		}
		
		return moveSuccess;
	}
	
	/**
     * Test if puzzle solved, e.g. all tiles returned to starting position
     * 
     * @return true if puzzle is solved, all tiles returned to starting position
     */
	public boolean isPuzzleSolved() {
		
		boolean isSolved = true;
		if (mNumMoves < 1) {
			isSolved = false;
		} else {
			int[][] puzzlePositions = mModel.getPuzzlePositions();
			int lastValue = -1;
			for (int i=0; i < PuzzleView.PUZZLE_ROWS; i++) {
				for (int j=0; j < PuzzleView.PUZZLE_COLS; j++) {
					if (puzzlePositions[i][j] != lastValue + 1) {
						isSolved = false;
						break;
					}
					lastValue = puzzlePositions[i][j]; 
				}
			}
		}
		return isSolved;
	}
	
	/**
     * Reset puzzle to original state with tiles in ordered position
     * 
     */
	public void resetPuzzle() {
		// first move from initial position doesn't count towards moves to solve puzzle, so set to -1, first move will set to 0
		mNumMoves = -1;
		mModel.initPuzzle();
		mView.setPuzzlePositions(mModel.getPuzzlePositions(), mModel.getEmptyPosition());
	}
	
	/**
     * @return int number of moves from starting position
     */
	public int getNumMoves() {
		return mNumMoves;
	}
	
	/**
     * Reset number of moves counter to 0
     * 
     */
	public void resetNumMoves() {
		// first move from initial position doesn't count towards moves to solve puzzle, so set to -1, first move will set to 0
		mNumMoves = -1;
	}
	
	/**
     * @return PuzzleModel model for puzzle
     */
	public PuzzleModel getModel() {
		return mModel;
	}
	
	
}
