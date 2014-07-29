package com.shaneahern.puzzle;

/**
 * Listener interface for to receive move update events from PuzzleView
 *
 * @author  Shane Ahern (shane@shaneahern.com)
 */
public interface PuzzleMoveListener {
	
	/**
     * Indicate that the tile at the specified position has moved
     *
     * @param position
     * 			Position from which the move originated
     */
	public void puzzleMoved(PuzzlePosition position);

}
