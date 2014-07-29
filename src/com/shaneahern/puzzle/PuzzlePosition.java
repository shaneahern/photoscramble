package com.shaneahern.puzzle;

/**
 * Holds state of an individual puzzle tile position
 *
 * @author  Shane Ahern (shane@shaneahern.com)
 */
public class PuzzlePosition {
	private int mRow;
	private int mCol;
	
	public PuzzlePosition(int row, int col) {
		this.mRow = row;
		this.mCol = col;
	}
	
	public int getRow() {
		return mRow;
	}
	
	public int getCol() {
		return mCol;
	}
}