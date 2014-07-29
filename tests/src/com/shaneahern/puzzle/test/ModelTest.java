package com.shaneahern.puzzle.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.shaneahern.puzzle.PuzzlePosition;
import com.shaneahern.puzzle.PuzzleController;
import com.shaneahern.puzzle.PuzzleModel;
import com.shaneahern.puzzle.PuzzleView;

public class ModelTest {

	@Before
	public void setUp() throws Exception {
	}
	public void printPuzzle(int[][] puzzlePositions) {
		for (int i=0; i < PuzzleView.PUZZLE_ROWS; i++) {
			for (int j=0; j < PuzzleView.PUZZLE_COLS; j++) {
				if (puzzlePositions[i][j] < 10) {
					System.out.print(" ");
				}
				System.out.print(puzzlePositions[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}


	@Test
	public void testPuzzleSetup() {
		PuzzleModel model = new PuzzleModel(PuzzleView.PUZZLE_ROWS, PuzzleView.PUZZLE_COLS, new PuzzlePosition(PuzzleView.INITIAL_EMPTY_TILE_ROW, PuzzleView.INITIAL_EMPTY_TILE_COL));
		int[][] puzzlePositions = model.getPuzzlePositions();
		assertTrue(puzzlePositions[0][0] == 0);
		assertTrue(puzzlePositions[PuzzleView.PUZZLE_ROWS -1][PuzzleView.PUZZLE_COLS -1] == 15);
	}
	
	@Test
	public void testPuzzleMove() {
		PuzzleController c = new PuzzleController();
		
		// Legal moves from starting position
		assertTrue(c.isMoveLegal(0, 1));
		assertTrue(c.isMoveLegal(0, 2));
		assertTrue(c.isMoveLegal(0, 3));
		assertTrue(c.isMoveLegal(1, 0));
		assertTrue(c.isMoveLegal(2, 0));
		assertTrue(c.isMoveLegal(3, 0));
		
		// illegal moves from starting position
		assertFalse(c.isMoveLegal(1, 1));
		assertFalse(c.isMoveLegal(1, 2));
		assertFalse(c.isMoveLegal(1, 3));
		assertFalse(c.isMoveLegal(2, 1));
		assertFalse(c.isMoveLegal(2, 2));
		assertFalse(c.isMoveLegal(2, 3));
		assertFalse(c.isMoveLegal(3, 1));
		assertFalse(c.isMoveLegal(3, 2));
		assertFalse(c.isMoveLegal(3, 3));
		
		assertTrue(c.isPuzzleSolved());


		PuzzleModel model = c.getModel();
		
		printPuzzle(c.getModel().getPuzzlePositions());
		
		//illegal moves
		if (c.move(0, 0)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertTrue(c.isPuzzleSolved());
		
		if (c.move(-1, -1)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertTrue(c.isPuzzleSolved());
		
		if (c.move(4, 4)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertTrue(c.isPuzzleSolved());
		
		//CYCLES
		if (c.move(0, 3)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		if (c.move(3, 3)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		if (c.move(3, 0)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		
		assertFalse(c.isPuzzleSolved());
		
		//undo
		if (c.move(3, 3)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		if (c.move(0, 3)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		if (c.move(0, 0)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		
		assertTrue(c.isPuzzleSolved());
		
		
		//next test
		
		if (c.move(0, 2)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		if (c.move(2, 2)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		if (c.move(2, 0)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		//undo
		if (c.move(2, 2)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		if (c.move(0, 2)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		if (c.move(0, 0)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		
		assertTrue(c.isPuzzleSolved());
		
		//next test
		
		if (c.move(0, 1)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		if (c.move(1, 1)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		if (c.move(1, 0)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		//undo
		if (c.move(1, 1)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		if (c.move(0, 1)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		assertFalse(c.isPuzzleSolved());
		
		if (c.move(0, 0)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		
		assertTrue(c.isPuzzleSolved());
		
		
		//next test
		if (c.move(3, 0)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		
		if (c.move(1, 0)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		
		if (c.move(1, 3)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		
		if (c.move(1, 2)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		
		assertFalse(c.isPuzzleSolved());

		if (c.move(1, 0)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		
		if (c.move(0, 0)) {
			printPuzzle(c.getModel().getPuzzlePositions());
		} else {
			System.out.println("Illegal move");
		}
		assertTrue(isEmptyPositionCorrect(model));
		
		assertTrue(c.isPuzzleSolved());
		
		
		for (int i=0; i < 100; i++) {
			int row = (int)(Math.random() * 4);
			int col = (int)(Math.random() * 4);
			
			if (c.isMoveLegal(row, col)) {
				System.out.println("Trying " + row + ", " + col);
				c.move(row, col);
				printPuzzle(c.getModel().getPuzzlePositions());
			} else {
				System.out.println("Illegal move");
			}
			assertTrue(isEmptyPositionCorrect(model));
				
		}

		
		
		
	}
	
	public boolean isEmptyPositionCorrect(PuzzleModel model) {
		PuzzlePosition emptyPostion = model.getEmptyPosition();
		return model.getPuzzlePositions()[emptyPostion.getRow()][emptyPostion.getCol()] == 0;
	}
}
