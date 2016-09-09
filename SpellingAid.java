import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 * This class creates the GUI and required objects to run the program.
 * @author wayne
 *
 */
public class SpellingAid /*extends JFrame */implements ActionListener{
	//new
	private JFrame spellingJFrame = new JFrame("Spelling Aid V2.0");
	
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JPanel spellingPanel = new JPanel();
	private JPanel videoPanel = new JPanel();
	
	// many many Swing components
	private JTextField inputText = new JTextField();
	private JButton newQuizBtn = new JButton("New Spelling Quiz");
	private JButton reviewMistakesBtn = new JButton("Review Mistakes");
	private JButton viewStatsBtn = new JButton("View Statistics");
	private JButton clearStatsBtn = new JButton("Clear Statistics");
	
	private JPanel menuBtns = new JPanel(new GridLayout(0, 1));
	private JPanel inputArea = new JPanel(new GridLayout(0, 1));
	private JTextArea previousInput = new JTextArea("Please select one of the options to the left.");
	private JLabel instructions = new JLabel();
	
	private Statistics _stats;
	private JTable _statsTable;
	private JScrollPane _scrollPane;
	
	private Quiz _currentQuiz;
	private List _wordSource;
	
	private int _level;
	
	//new stuff
	private JButton relistenToWord = new JButton("Listen to the word again.");
	private JButton voice1 = new JButton("Voice 1");
	private JButton voice2 = new JButton("Voice 2");
	private JPanel textAndButton = new JPanel();
	private JScrollPane previousInputScroll;
	
	/**
	 * Initializes swing components.
	 */
	public SpellingAid() {
		spellingJFrame.setSize(900, 400);
		spellingJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		newQuizBtn.addActionListener(this);
		reviewMistakesBtn.addActionListener(this);
		viewStatsBtn.addActionListener(this);
		clearStatsBtn.addActionListener(this);
		inputText.addActionListener(this);
		inputText.setEnabled(false);
		relistenToWord.addActionListener(this);
		menuBtns.add(newQuizBtn);
		menuBtns.add(reviewMistakesBtn);
		menuBtns.add(viewStatsBtn);
		menuBtns.add(clearStatsBtn);
		inputArea.add(instructions);
		// new stuff
		inputText.setPreferredSize(new Dimension(450, 25));
		textAndButton.add(inputText);
		textAndButton.add(relistenToWord);
		textAndButton.add(voice1);
		textAndButton.add(voice2);
		inputArea.add(textAndButton);
		previousInputScroll = new JScrollPane(previousInput);
		spellingPanel.add(inputArea, BorderLayout.SOUTH);
		spellingPanel.add(menuBtns, BorderLayout.WEST);
		spellingPanel.add(previousInputScroll, BorderLayout.CENTER);
		previousInput.setEditable(false);
		_wordSource = new List(new File("NZCER-spelling-lists.txt"));
		_stats = new Statistics(_wordSource);
		_statsTable = new JTable(_stats);
		_statsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		_scrollPane = new JScrollPane(_statsTable);
		_scrollPane.setPreferredSize(new Dimension(400, 300));
		_statsTable.setFillsViewportHeight(true);
		spellingPanel.add(_scrollPane, BorderLayout.EAST);
		
		//Asking user for which spelling level they want to start with
				boolean isAnswer = false;
				while (!isAnswer) {
					Object[] levels = new String[_wordSource.numOfLevels()];
					for (int i = 0; i < _wordSource.numOfLevels(); i++) {
						levels[i] = "Level " + (i + 1);
					}
					
					String answer = (String)JOptionPane.showInputDialog(spellingJFrame, "Please pick a spelling level to start with: ", "Spelling Level", JOptionPane.QUESTION_MESSAGE, null, levels, levels[0]);
					int level = Integer.parseInt(answer.split(" ")[1]); // This is where to continue coding from. I haven't finished this line.
					if (level <= _wordSource.numOfLevels() ) {
						_level = level;
						System.out.println("Level "+_level+" selected");
						break;
					}

				}
				
				//new 
				tabbedPane.add(spellingPanel, "Spelling Tab");
				tabbedPane.add(videoPanel, "Video Tab");
				
	}
	
	//new
	public JFrame getJFrame() {
		return spellingJFrame;
	}

	/**
	 * Performs different actions, depending on what action was performed.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		//String action = e.getActionCommand();
		Object action = e.getSource();
		
		if (action.equals(newQuizBtn)) {
		//if (action.equals(newQuizBtn.getActionCommand())) {
			removeQuizListeners();
			_currentQuiz = new Quiz(QuizType.NEW, _level, _stats, this); // change the input number here to change level for now
			inputText.addActionListener(_currentQuiz);
			instructions.setText("Spell the word below and press Enter: ");
			inputText.setEnabled(true);
			previousInput.setText("");
			inputText.requestFocusInWindow();
			boolean status = _currentQuiz.sayNextWord();
			if (!status) { inputText.setEnabled(false); };
		} else if (action.equals(reviewMistakesBtn)) {	
		//} else if (action.equals(reviewMistakesBtn.getActionCommand())) {
			removeQuizListeners();
			_currentQuiz = new Quiz(QuizType.REVIEW, 1, _stats, this);
			inputText.addActionListener(_currentQuiz);
			instructions.setText("Spell the word below and press Enter: ");
			inputText.setEnabled(true);
			previousInput.setText("");
			inputText.requestFocusInWindow();
			boolean status = _currentQuiz.sayNextWord();
			if (!status) { inputText.setEnabled(false); };
		//} else if (action.equals(viewStatsBtn.getActionCommand())) {
		} else if (action.equals(viewStatsBtn)) {
			instructions.setText("");
			previousInput.setText("");
			inputText.setEnabled(false);
			ArrayList<String> formattedStats = _stats.getStats();
			
			for (String line : formattedStats) {
				previousInput.append(line);
			}
		//} else if (action.equals(clearStatsBtn.getActionCommand())) {
		} else if (action.equals(clearStatsBtn)) {
			instructions.setText("");
			previousInput.setText("");
			inputText.setEnabled(false);
			_stats.clearStats();
			clearList(".failedlist");
			clearList(".faultedlist");
		} else if (action.equals(relistenToWord)) {
			if (_currentQuiz != null) {
				_currentQuiz.repeatWordWithNoPenalty();
			}
			inputText.requestFocusInWindow();
		} else {
			previousInput.setText(previousInput.getText() + e.getActionCommand() + "\n");
			inputText.setText("");
		}
	}
	
	/**
	 * Clears the given list by deleting it and re-making it.
	 * @param list - the list the clear
	 */
	private void clearList(String list) {
		File listFile = new File(list);
		
		if (listFile.exists()) {
			listFile.delete();
			try {
				listFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Removes old Quiz ActionListeners from the input field.
	 */
	private void removeQuizListeners() {
		ActionListener[] listeners = inputText.getActionListeners();
		for (ActionListener listener : listeners) {
			if (listener != this) {
				inputText.removeActionListener(listener);
			}
		}
	}
	
	public void levelCompleted() {
		//create pop up to ask user either move up, stay at level, or play video
		Object[] options = {"Move up a Spelling level", "Stay at current Spelling level", "Play reward video"};
		while (true) {
			int n = JOptionPane.showOptionDialog(spellingJFrame, "Please select an option:", "Congratulations!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			
			if (n == 0) {
				if (_level < _wordSource.numOfLevels()) {
					_level++;
				}
				break;
			} else if (n == 1) {
				break;
			} else if (n == 2) {
				playVideo();
				break;
			}
		}
	}
	
	private void playVideo() {
		NativeLibrary.addSearchPath(
	            RuntimeUtil.getLibVlcLibraryName(), "/Applications/vlc-2.0.0/VLC.app/Contents/MacOS/lib"
	        );
	        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
	        
	        SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                new RewardMediaPlayer();
	            }
	        });
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				SpellingAid frame = new SpellingAid();
				frame.getJFrame().setVisible(true);
			}
			
		});
	}
}

