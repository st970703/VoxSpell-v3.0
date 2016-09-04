import java.awt.BorderLayout;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * This class creates the GUI and required objects to run the program.
 * @author wayne
 *
 */
public class SpellingAid extends JFrame implements ActionListener{

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
	
	//new stuff
	private JButton relistenToWord = new JButton("Listen to the word again.");
	private JPanel textAndButton = new JPanel();
	private JScrollPane previousInputScroll;
	
	/**
	 * Initializes swing components.
	 */
	public SpellingAid() {
		super("Spelling Aid V2.0");
		setSize(900, 400);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
		inputText.setPreferredSize(new Dimension(650, 25));
		textAndButton.add(inputText);
		textAndButton.add(relistenToWord);
		inputArea.add(textAndButton);
		previousInputScroll = new JScrollPane(previousInput);
		add(inputArea, BorderLayout.SOUTH);
		add(menuBtns, BorderLayout.WEST);
		add(previousInputScroll, BorderLayout.CENTER);
		previousInput.setEditable(false);
//		previousInput.setPreferredSize(new Dimension(300, 300));
		_wordSource = new List(new File("NZCER-spelling-lists.txt"));
		_stats = new Statistics(_wordSource);
		_statsTable = new JTable(_stats);
		_statsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		_scrollPane = new JScrollPane(_statsTable);
		_scrollPane.setPreferredSize(new Dimension(400, 300));
		_statsTable.setFillsViewportHeight(true);
		add(_scrollPane, BorderLayout.EAST);
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
			_currentQuiz = new Quiz(QuizType.NEW, 11, _stats); // change the input number here to change level for now
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
			_currentQuiz = new Quiz(QuizType.REVIEW, 1, _stats);
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
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				SpellingAid frame = new SpellingAid();
				frame.setVisible(true);
			}
			
		});
	}
}

