import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 * This SpellingAid class creates the main GUI and create instances of all the other related Java classes.
 * This class is an ActionListener itself. 
 * @author wayne
 * @editor mike
 */
public class SpellingAid implements ActionListener{

	// fields of Swing components
	private JTextField inputText = new JTextField();
	private JButton newQuizBtn = new JButton("New Spelling Quiz");
	private JButton reviewMistakesBtn = new JButton("Review Mistakes");
	private JButton viewStatsBtn = new JButton("View Statistics");
	private JButton clearStatsBtn = new JButton("Clear Statistics");	
	private JPanel menuBtns = new JPanel(new GridLayout(0, 1));
	private JPanel inputArea = new JPanel(new GridLayout(0, 1));
	private JTextArea previousInput = new JTextArea("Please select one of the options to the left.");
	private JLabel instructions = new JLabel();
	private JTable _statsTable;
	private JScrollPane _scrollPane;
	private JButton relistenToWord = new JButton("Listen to the word again.");
	private JPanel textAndButton = new JPanel();
	private JScrollPane previousInputScroll;	
	private JPanel mainScreen = new JPanel(new BorderLayout());
	private JPanel videoScreen = new JPanel(new BorderLayout());	
	private JFrame window;
	private JPanel overAllPanel = new JPanel(new CardLayout());
	private JPanel statsPanel = new JPanel(new BorderLayout());
	private JLabel statsTitle = new JLabel("Statistics (by Level)");	
	private JLabel previousInputTitle = new JLabel("Previous Input");
	private JPanel previousInputPanel = new JPanel(new BorderLayout());	
	private JLabel menuTitle = new JLabel("Menu");
	private JPanel menuPanel = new JPanel(new BorderLayout());
	private JComboBox<String> voiceCBox;

	// fields of the related Java classes
	private Statistics _stats;
	private Quiz _currentQuiz;
	private WordList _wordSource;
	private WordList _failedWords;

	private int _level;
	private ArrayList<String> voiceOptions = new ArrayList<String>();	
	private String _voice;

	/**
	 * Initializes swing components.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SpellingAid() {

		_voice = "kal_diphone";
		// Construct the JFrame
		window = new JFrame("Spelling Aid V2.0");
		window.setSize(900, 500);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Construct the Swing components
		newQuizBtn.addActionListener(this);
		reviewMistakesBtn.addActionListener(this);
		viewStatsBtn.addActionListener(this);
		clearStatsBtn.addActionListener(this);
		inputText.addActionListener(this);
		inputText.setEnabled(false);
		relistenToWord.addActionListener(this);

		menuTitle.setPreferredSize(new Dimension(100, 30));
		menuTitle.setHorizontalAlignment(SwingConstants.CENTER);
		menuTitle.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		menuBtns.add(newQuizBtn);
		menuBtns.add(reviewMistakesBtn);
		menuBtns.add(viewStatsBtn);
		menuBtns.add(clearStatsBtn);
		menuPanel.add(menuTitle, BorderLayout.NORTH);
		menuPanel.add(menuBtns, BorderLayout.CENTER);
		inputArea.add(instructions);
		inputText.setPreferredSize(new Dimension(450, 25));
		textAndButton.add(inputText);
		textAndButton.add(relistenToWord);	
		inputArea.add(textAndButton);

		previousInputScroll = new JScrollPane(previousInput);
		previousInputTitle.setHorizontalAlignment(SwingConstants.CENTER);
		previousInputTitle.setPreferredSize(new Dimension(350, 30));
		previousInputTitle.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		previousInputPanel.add(previousInputTitle, BorderLayout.NORTH);
		previousInputPanel.add(previousInputScroll, BorderLayout.CENTER);

		mainScreen.add(inputArea, BorderLayout.SOUTH);
		mainScreen.add(menuPanel, BorderLayout.WEST);
		mainScreen.add(previousInputPanel, BorderLayout.CENTER);

		previousInput.setEditable(false);
		_wordSource = new WordList(new File("NZCER-spelling-lists.txt"), QuizType.NEW);
		_failedWords = new WordList(new File(".failedWords"), QuizType.REVIEW);
		_stats = Statistics.getInstance();

		_statsTable = new JTable(_stats);
		_statsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		_scrollPane = new JScrollPane(_statsTable);
		_scrollPane.setPreferredSize(new Dimension(350, 300));
		_statsTable.setFillsViewportHeight(true);
		_statsTable.setRowHeight(28);

		statsTitle.setHorizontalAlignment(SwingConstants.CENTER);
		statsTitle.setPreferredSize(new Dimension(350, 30));
		statsTitle.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		statsPanel.add(statsTitle, BorderLayout.NORTH);
		statsPanel.add(_scrollPane, BorderLayout.CENTER);
		mainScreen.add(statsPanel, BorderLayout.EAST);

		overAllPanel.add(mainScreen, "MAIN");
		overAllPanel.add(videoScreen, "VIDEO");

		window.add(overAllPanel);

		// Add string of voices to be tested
		voiceOptions.add("akl_nz_jdt_diphone");
		voiceOptions.add("rab_diphone");
		voiceOptions.add("kal_diphone");
		voiceOptions.add("cmu_us_rms_arctic_clunits"); // Added _clunits so that it would work
		voiceOptions.add("cmu_us_slt_arctic");
		voiceOptions.add("cmu_us_bdl_arctic");
		voiceOptions.add("cmu_us_clb_arctic");

		// do FFMPEG processing in the background thread, it should be done by the time the user finishes the first quiz.
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				// Make the new video file if it doesn't exist already
				File file = new File("big_buck_bunny_1_minute_aecho.avi");
				if (!file.exists() && checkVoice("ffmpeg")) {
					ProcessBuilder pb = new ProcessBuilder("bash", "-c", "ffmpeg -i big_buck_bunny_1_minute.avi -af aecho big_buck_bunny_1_minute_aecho.avi");
					pb.start();
				}
				return null;	
			}
		};
		worker.execute();	

		// Test the voices options to eliminate uninstalled options
		for (int i = 0; i < voiceOptions.size(); i++) {
			if (!checkVoice(voiceOptions.get(i) ) ) {
				voiceOptions.remove(i );
				// Start checking from the beginning again. 
				i = -1;
			}
		}

		// Construct the JComboBox after testing the String list
		voiceCBox = new JComboBox((String[]) voiceOptions.toArray(new String[0]) );
		voiceCBox.addActionListener(this);
		textAndButton.add(voiceCBox);

		//Asking user for which spelling level they want to start with
		while (true) {
			// Construct the String array to be used by the JOptionPane's Dialog
			Object[] levels = new String[_wordSource.numOfLevels()];
			for (int i = 0; i < _wordSource.numOfLevels(); i++) {
				levels[i] = "Level " + (i + 1);
			}

			String answer = (String)JOptionPane.showInputDialog(window, "Please pick a spelling level to start with: ", "Spelling Level", JOptionPane.QUESTION_MESSAGE, null, levels, levels[0]);
			if (answer != null) {
				int level = Integer.parseInt(answer.split(" ")[1]);
				if (level <= _wordSource.numOfLevels() ) {
					_level = level;
					// Break the infinite while loop
					break;
				}
			}
		}
		// Show the JFrame
		window.setVisible(true);
	}

	/**
	 * Performs different actions, depending on what action event is created by the JButtons.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object action = e.getSource();

		// if statement to check the sources of action events
		if (action.equals(newQuizBtn)) {
			// Removes old Quiz ActionListeners
			removeQuizListeners();
			_currentQuiz = new Quiz(_level, this, _wordSource);
			// Adds new Quiz ActionListeners
			inputText.addActionListener(_currentQuiz);
			instructions.setText("Spell the word below and press Enter: ");
			inputText.setEnabled(true);
			// Clear the JTextArea
			previousInput.setText("");
			// change the focus so the user can start typing right away
			inputText.requestFocusInWindow();

			boolean status = _currentQuiz.sayNextWord(null);
			if (!status) { inputText.setEnabled(false); };
		} else if (action.equals(reviewMistakesBtn)) {	
			// Removes old Quiz ActionListeners
			removeQuizListeners();
			_currentQuiz = new Quiz( _level, this, _failedWords);
			// Adds new Quiz ActionListeners
			inputText.addActionListener(_currentQuiz);
			instructions.setText("Spell the word below and press Enter: ");
			inputText.setEnabled(true);
			// Clear the JTextArea
			previousInput.setText("");
			// change the focus so the user can start typing right away
			inputText.requestFocusInWindow();

			boolean status = _currentQuiz.sayNextWord(null);
			if (!status) { inputText.setEnabled(false); };
		} else if (action.equals(viewStatsBtn)) {
			// Clear the texts
			instructions.setText("");
			previousInput.setText("");
			// Disable the textArea and append the statistics data
			inputText.setEnabled(false);
			ArrayList<String> formattedStats = _stats.getStats();

			for (String line : formattedStats) {
				previousInput.append(line);
			}
		} else if (action.equals(clearStatsBtn)) {
			// Clear the texts
			instructions.setText("");
			previousInput.setText("");
			// Clear the statistics
			inputText.setEnabled(false);
			_stats.clearStats();
			clearList(".failedlist");
			clearList(".faultedlist");
		} else if (action.equals(relistenToWord)) {
			// if the Quiz field exists
			if (_currentQuiz != null) {
				_currentQuiz.repeatWordWithNoPenalty();
			}
			// change the focus so the user can start typing right away 
			inputText.requestFocusInWindow();
		}  else if (action.equals(voiceCBox)) {
			// Set the private voice field to the JComboBox item selected
			_voice = (String)voiceCBox.getSelectedItem();
		} else {
			// This would happen when the user presses enter
			previousInput.setText(previousInput.getText() + e.getActionCommand() + "\n");
			// Clear the text
			inputText.setText("");
		}
	}

	/**
	 * getter method to return _voice.
	 * @return
	 */
	public String getVoice() {
		return _voice;
	}

	/**
	 * checkVoice(String voice) checks if a voice or a file exists in Linux
	 * using the locate command
	 * @param voice
	 * @return
	 */
	private boolean checkVoice(String voice) {
		try {
			// process for the Linux bash command 
			ProcessBuilder pb = new ProcessBuilder("bash", "-c", "locate " + voice);
			Process pro = pb.start();
			// Read the output from Linux terminal 
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(pro.getInputStream()));

			String line = stdOut.readLine();
			// return true if bash finds something
			if (line != null) {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// otherwise the voice doesn't exist, return false
		return false;
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

	/**
	 * uses JOptionPane.showOptionDialog to allow the user to select one of the four options:
	 * "Move up a Spelling level", "Stay at current Spelling level", "Play reward video", "Play reward video with Echo Effect"
	 */
	public void levelCompleted() {
		//create pop up to ask user either move up, stay at level, or play video
		Object[] options = {"Move up a Spelling level", "Stay at current Spelling level", "Play reward video", "Play reward video with Echo Effect"};
		while (true) {
			int n = JOptionPane.showOptionDialog(window, "Please select an option:", "Congratulations!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

			if (n == 0) {
				// increase the level
				if (_level < _wordSource.numOfLevels()) {
					_level++;
				}
				break;
			} else if (n == 1) {
				// stay at current level
				break;
			} else if (n == 2) {
				// play the bunny video
				playVideo("big_buck_bunny_1_minute.avi");
				break;
			} else if (n == 3) {
				// play the new bunny video
				System.out.println("Play reward video with Echo Effect");
				File file = new File("big_buck_bunny_1_minute_aecho.avi");
				if (checkVoice("ffmpeg") && file.exists() ) {
					playVideo("big_buck_bunny_1_minute_aecho.avi");
					break;
				} else if (!checkVoice("ffmpeg")) {
					// FFMPEG not installed
					JOptionPane.showMessageDialog(null, "ffmpeg not installed.", "ffmpeg missing!", JOptionPane.ERROR_MESSAGE);
				} else if (!file.exists()) {
					// the processed video file is missing
					JOptionPane.showMessageDialog(null, "Cannot find video file.", "Video File missing!", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/**
	 * playVideo(final String videoName) uses a background thread to play the video specified in a JPanel.
	 * @param videoName
	 */
	private void playVideo(final String videoName) {
		refreshVideoScreen();
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				NativeLibrary.addSearchPath(
						RuntimeUtil.getLibVlcLibraryName(), "/Applications/vlc-2.0.0/VLC.app/Contents/MacOS/lib"
						);
				Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);

				new RewardMediaPlayer(videoScreen, SpellingAid.this, videoName);

				return null;
			}
		};
		worker.execute();

		switchScreens("VIDEO");
	}

	/**
	 * private void refreshVideoScreen() adds a new JPanel inside overAllPanel.
	 */
	private void refreshVideoScreen() {
		overAllPanel.remove(videoScreen);
		videoScreen = new JPanel(new BorderLayout());
		overAllPanel.add(videoScreen, "VIDEO");
	}

	/**
	 * switchScreens(String screen) switches the JPanels inside the JFrame, CardLayout is involved
	 * @param screen - String
	 */
	public void switchScreens(String screen) {
		CardLayout cl = (CardLayout)(overAllPanel.getLayout());
		cl.show(overAllPanel, screen);
	}

	/**
	 * static main method to create and run the main GUI.
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				@SuppressWarnings("unused")
				SpellingAid spell = new SpellingAid();
			}

		});

		// calls deleteEchoVideo() when the user exits the program
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				deleteEchoVideo();
			}
		});		
	} 

	/**
	 * private static void deleteEchoVideo() deletes the big_buck_bunny_1_minute_aecho.avi
	 */
	private static void deleteEchoVideo() {
		File videoFile = new File("./big_buck_bunny_1_minute_aecho.avi");		
		if (videoFile.exists()) {
			videoFile.delete();
		}
	}	
}