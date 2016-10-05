
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class NaughtsAndCrosses extends JPanel{
	
	private static final int ROWS = 3;
	private static final int COLS = 3;
	
	private enum GameStatus { NAUGHT_WON, CROSS_WON, IN_PROGRESS, DRAW };
	
	private enum Turn {Naughts, Crosses};
	
	private Turn sideToMove = Turn.Naughts;
	private static int _naughtWins = 0;
	private static int _crossWins = 0; 
	private static int _draws = 0;
	
	
	
	// Values to be used for button text within a board. 
	private static String NAUGHT = "0";
	private static String CROSS = "X";
	private static String BLANK = " ";
	
	// A 2 dimensional array of JButton objects. Each button is intended to 
	// display the text NAUGHT, CROSS or BLANK (see above). 
	private JButton[][] _board;
	private JButton _newGame = new JButton("New Game");
	private JButton _reset = new JButton("Reset Scores");
	private JLabel _noOfNWins = new JLabel(String.valueOf(_naughtWins));
	private JLabel _noOfCWins = new JLabel(String.valueOf(_crossWins));
	private JLabel _noOfDraws = new JLabel(String.valueOf(_draws));
	
	public NaughtsAndCrosses() {
		// Build the GUI.
		buildGUI();


		// TO DO: set up event handlers.
		
		
		NaughtsAndCrosses game = this;
		
		AbstractAction gameAction = new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				switch(sideToMove){
				case Naughts: 
					((AbstractButton) e.getSource()).setText(NAUGHT);
					sideToMove = Turn.Crosses;
					break;
				case Crosses: 
					((AbstractButton) e.getSource()).setText(CROSS);
					sideToMove = Turn.Naughts;
					break;
				}
				((JButton) e.getSource()).setEnabled(false);	

				GameStatus status = game.getGameStatus();
				if (status!=GameStatus.DRAW && status!=GameStatus.IN_PROGRESS){
					
					for(int row = 0; row < ROWS; row++) {
						for(int col = 0; col < COLS; col++) {
							_board[row][col].setEnabled(false);
						}
					}
					
					if (status==GameStatus.CROSS_WON){
						System.out.println("Crosses wins!");
						_crossWins++;
						_noOfCWins.setText(String.valueOf(_crossWins));
						return;
					} else {
						System.out.println("Naughts wins!");
						_naughtWins++;
						_noOfNWins.setText(String.valueOf(_naughtWins));
						return;
					}
					
				}
				
				for(int row = 0; row < ROWS; row++) {
					for(int col = 0; col < COLS; col++) {
						if (_board[row][col].isEnabled()){
							return;
						}
					}
				}
				
				System.out.println("Draws are unacceptable...");
				_draws++;
				_noOfDraws.setText(String.valueOf(_draws));
				
				
				
			}	
			
		
		};
		
		
		
		
		for(int row = 0; row < ROWS; row++) {
			for(int col = 0; col < COLS; col++) {
				_board[row][col].addActionListener(gameAction);
			}
		}
		
		_newGame.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				for(int row = 0; row < ROWS; row++) {
					for(int col = 0; col < COLS; col++) {
						_board[row][col].setText(BLANK);
						_board[row][col].setEnabled(true);
					}
				}
				System.out.println("----------NEW_GAME----------");
				
			}
			
		});
		
		_reset.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				for(int row = 0; row < ROWS; row++) {
					for(int col = 0; col < COLS; col++) {
						_board[row][col].setText(BLANK);
						_board[row][col].setEnabled(true);
					}
				}
				_naughtWins = 0;
				_crossWins = 0;
				_draws = 0;
				
				_noOfNWins.setText(String.valueOf(_naughtWins));
				_noOfCWins.setText(String.valueOf(_crossWins));
				_noOfDraws.setText(String.valueOf(_draws));
				
				System.out.println("----------SCORES_RESET----------");
				
			}
			
		});

	}


	private void buildGUI() {
		// Initialise the board.
		_board = new JButton[ROWS][COLS];
		
		for(int row = 0; row < ROWS; row++) {
			for(int col = 0; col < COLS; col++) {
				_board[row][col] = new JButton(BLANK);
				_board[row][col].setPreferredSize(new Dimension(70,70));

			}
		}

		// TO DO: create other GUI components and lay them out as appropriate.
		// Note that _board only stores 9 JButton objects using a 3x3 array. 
		// The buttons still need to be added to the GUI - use GridLayout to 
		// add and layout the JButtons on a JPanel.  
		
		
		
		JPanel inputPanel = new JPanel( ); 
		JPanel newGameAndReset = new JPanel( );
		JPanel layout = new JPanel();
		JPanel scores = new JPanel();
		BoxLayout boxLayout = new BoxLayout(layout,BoxLayout.PAGE_AXIS);
		
		
		GridLayout ticTacToeBoard = new GridLayout(3,3);
		GridLayout scoreBoard = new GridLayout(2,3);
		
		inputPanel.setLayout(ticTacToeBoard);
		newGameAndReset.add(_newGame);
		newGameAndReset.add(_reset);
			
		scores.setLayout(scoreBoard);
		scores.add(new JLabel("Naughts"));
		scores.add(new JLabel("Crosses"));
		scores.add(new JLabel("Draws"));
		scores.add(_noOfNWins);
		scores.add(_noOfCWins);
		scores.add(_noOfDraws);
		
		for(int row = 0; row < ROWS; row++) {
			for(int col = 0; col < COLS; col++) {
				inputPanel.add(_board[row][col]);
			}
		}
		
		layout.setLayout(boxLayout);
		layout.add(inputPanel);
		layout.add(newGameAndReset);
		layout.add(scores);
		
		add(layout);
	}
	
	/*public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// TO DO: Create and configure a JFrame to display the GUI.
				// Make class NaughtsAndCrosses extend JPanel so that a 
				// NaughtsAndCrosses object can be added to the JFrame.
				NaughtsAndCrosses game = new NaughtsAndCrosses();
				JFrame frame = new JFrame("TicTacToe");
				
				frame.add(game);
				frame.pack();
				
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				
				
			}
		});
	}*/

	// Helper method to determine the status of a Naughts and Crosses game. 
	// This method processes the 2 dimensional array of JButton objects, and 
	// uses the text value (NAUGHT, CROSS or BLANK) of each JButton to 
	// calculate the game's state of play.
	private GameStatus getGameStatus() {
		GameStatus status = GameStatus.DRAW;
		
		List<String> lines = new ArrayList<String>();
		
		// Top row
		lines.add(_board[0][0].getText() + _board[0][1].getText() + _board[0][2].getText());
		
		// Middle row
		lines.add(_board[1][0].getText() + _board[1][1].getText() + _board[1][2].getText());
		
		// Bottom row
		lines.add(_board[2][0].getText() + _board[2][1].getText() + _board[2][2].getText());
		
		// Left col
		lines.add(_board[0][0].getText() + _board[1][0].getText() + _board[2][0].getText());
		
		// Middle col
		lines.add(_board[0][1].getText() + _board[1][1].getText() + _board[2][1].getText());
		
		// Right col
		lines.add(_board[0][2].getText() + _board[1][2].getText() + _board[2][2].getText());
		
		// Diagonals
		lines.add(_board[0][0].getText() + _board[1][1].getText() + _board[2][2].getText());
		lines.add(_board[0][2].getText() + _board[1][1].getText() + _board[2][0].getText());
		
		// Check to see if there's any cell without a NAUGHT or a CROSS.
		for(String line : lines) {
			System.out.println("Looking at line: |" + line + "|");
			if(line.contains(BLANK)) {
				status = GameStatus.IN_PROGRESS;
				System.out.println("In progress");
				break;
			}
		}
		
		// Check to see if there's any line of NAUGHTs or CROSSes.
		if(lines.contains(CROSS + CROSS + CROSS)) {
			status = GameStatus.CROSS_WON; 
			
		} else if (lines.contains(NAUGHT + NAUGHT + NAUGHT)) {
			
			status = GameStatus.NAUGHT_WON;
		}
		
		return status;
	}
	
}
