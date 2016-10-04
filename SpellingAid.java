import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
 * This class creates the GUI and required objects to run the program.
 * @author wayne
 *
 */
public class SpellingAid implements ActionListener{

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
	private WordList _wordSource;
	private WordList _failedWords;
	
	private int _level;
	
	//new stuff
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
	
	private ArrayList<String> voiceOptions = new ArrayList<String>();
	private JComboBox voiceCBox;
	
	private String _voice;
	
	/**
	 * Initializes swing components.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SpellingAid() {
		_voice = "kal_diphone";
		window = new JFrame("Spelling Aid V2.0");
		window.setSize(900, 500);
		window.setDefaultCloseOperation(window.EXIT_ON_CLOSE);
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
		// new stuff
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
		
		// add possible voice packages to check
		voiceOptions.add("kal_diphone");
		voiceOptions.add("akl_nz_jdt_diphone");
		voiceOptions.add("rab_diphone");
		voiceOptions.add("cmu_us_rms_arctic_clunits"); // Added _clunits so that it would work
		voiceOptions.add("cmu_us_slt_arctic_clunits");
		voiceOptions.add("cmu_us_bdl_arctic_clunits");
		voiceOptions.add("cmu_us_clb_arctic_clunits");
		voiceOptions.add("cmu_us_awb_cg");
		
		// Process the echo video using FFMPEG at the start to ensure there is enough time
		if (checkVoice("ffmpeg")) {
			makeEchoVideo();
		}
		
		// Check if the voice packages exists.
		for (int i = 0; i < voiceOptions.size(); i++) {
			if (!checkVoice(voiceOptions.get(i) ) ) {
				voiceOptions.remove(i );
				i = -1;
			}
		}
		
		voiceCBox = new JComboBox((String[]) voiceOptions.toArray(new String[0]) );
		voiceCBox.addActionListener(this);
		textAndButton.add(voiceCBox);

		//Asking user for which spelling level they want to start with
		boolean isAnswer = false;
		while (!isAnswer) {
			Object[] levels = new String[_wordSource.numOfLevels()];
			for (int i = 0; i < _wordSource.numOfLevels(); i++) {
				levels[i] = "Level " + (i + 1);
			}

			String answer = (String)JOptionPane.showInputDialog(window, "Please pick a spelling level to start with: ", "Spelling Level", JOptionPane.QUESTION_MESSAGE, null, levels, levels[0]);
			if (answer != null) {
				int level = Integer.parseInt(answer.split(" ")[1]); // This is where to continue coding from. I haven't finished this line.
				if (level <= _wordSource.numOfLevels() ) {
					_level = level;
					break;
				}
			}
		}
		
		window.setVisible(true);
	}
	
	/**
	 * Performs different actions, depending on what action was performed.
	 * @editor Mike Lee
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		//String action = e.getActionCommand();
		Object action = e.getSource();
		
		if (action.equals(newQuizBtn)) {
		//if (action.equals(newQuizBtn.getActionCommand())) {
			removeQuizListeners();
			_currentQuiz = new Quiz(_level, this, _wordSource); // change the input number here to change level for now
			inputText.addActionListener(_currentQuiz);
			instructions.setText("Spell the word below and press Enter: ");
			inputText.setEnabled(true);
			previousInput.setText("");
			inputText.requestFocusInWindow();
			boolean status = _currentQuiz.sayNextWord(null);
			if (!status) { inputText.setEnabled(false); };
		} else if (action.equals(reviewMistakesBtn)) {	
		//} else if (action.equals(reviewMistakesBtn.getActionCommand())) {
			removeQuizListeners();
			_currentQuiz = new Quiz( _level, this, _failedWords);
			inputText.addActionListener(_currentQuiz);
			instructions.setText("Spell the word below and press Enter: ");
			inputText.setEnabled(true);
			previousInput.setText("");
			inputText.requestFocusInWindow();
			boolean status = _currentQuiz.sayNextWord(null);
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
		} else if (action.equals(voiceCBox)) {
			_voice = (String)voiceCBox.getSelectedItem();
		} else {
			previousInput.setText(previousInput.getText() + e.getActionCommand() + "\n");
			inputText.setText("");
		}
	}
	
	/**
	 * getVoice() is a getter method to return the _voice field.
	 */
	public String getVoice() {
		return _voice;
	}
	
	/**
	 * checkVoice(String voice) checks if the voice specified exists using a ProcessBuilder.
	 */
	private boolean checkVoice(String voice) {
		try {
			ProcessBuilder pb = new ProcessBuilder("bash", "-c", "locate " + voice);
			Process pro = pb.start();
			
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			
			String line = stdOut.readLine();
			
			if (line != null) {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	/**
	 * levelCompleted() prompts the user to select one of the following: "Move up a Spelling level", "Stay at current Spelling level", "Play reward video", "Play reward video with Echo Effect"
	 * @editor Mike Lee
	 */
	public void levelCompleted() {
		//create pop up to ask user either move up, stay at level, or play video
		Object[] options = {"Move up a Spelling level", "Stay at current Spelling level", "Play reward video", "Play reward video with Echo Effect"};
		while (true) {
			int n = JOptionPane.showOptionDialog(window, "Please select an option:", "Congratulations!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			
			if (n == 0) {
				if (_level < _wordSource.numOfLevels()) {
					_level++;
				}
				break;
			} else if (n == 1) {
				break;
			} else if (n == 2) {
				File file = new File("big_buck_bunny_1_minute.avi");
				if (file.exists()) {
					playVideo("big_buck_bunny_1_minute.avi");
					break;
				} else {
					JOptionPane.showMessageDialog(null, "Cannot find video file.", "Video File missing!", JOptionPane.ERROR_MESSAGE);
				}
				
			} else if (n == 3) {
				File file = new File("big_buck_bunny_1_minute_aecho.avi");
				if (checkVoice("ffmpeg") && file.exists() ) {
					playVideo("big_buck_bunny_1_minute_aecho.avi");
					break;
				} else if (!checkVoice("ffmpeg")) {
					JOptionPane.showMessageDialog(null, "ffmpeg not installed.", "ffmpeg missing!", JOptionPane.ERROR_MESSAGE);
				} else if (!file.exists()) {
					JOptionPane.showMessageDialog(null, "Cannot find video file.", "Video File missing!", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	/**
	 * playVideo(final String videoName) is a private helper method that that creates an instance of the RewardMediaPlayer and switches to the video JPanel.
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
	 * refreshVideoScreen() is a private helper method that switches the current JPanel to the video JPanel.
	 */
	private void refreshVideoScreen() {
		overAllPanel.remove(videoScreen);
		videoScreen = new JPanel(new BorderLayout());
		overAllPanel.add(videoScreen, "VIDEO");
	}
	
	/**
	 * switchScreens(String screen) switches the JPanel for performing a quiz or playing a video. 
	 */
	public void switchScreens(String screen) {
		CardLayout cl = (CardLayout)(overAllPanel.getLayout());
		cl.show(overAllPanel, screen);
	}
	
	/**
	 * Private helper method to delete the echo video.
	 * @author Mike Lee
	 */
	private static void deleteEchoVideo() {
		File videoFile = new File("./big_buck_bunny_1_minute_aecho.avi");		
		if (videoFile.exists()) {
			videoFile.delete();
		}
	}
	
	/**
	 * makeEchoVideo() is a private helper method to process the echo video in the background.
	 */
	private static void makeEchoVideo() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				File file = new File("big_buck_bunny_1_minute_aecho.avi");
				if (!file.exists()) {
					ProcessBuilder pb = new ProcessBuilder("bash", "-c", "ffmpeg -i big_buck_bunny_1_minute.avi -af aecho big_buck_bunny_1_minute_aecho.avi");
					Process pro = pb.start();
				}
				return null;	
			}
		};
		worker.execute();
	}
	
	/**
	 * Main static method to run the GUI.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				SpellingAid spell = new SpellingAid();
			}
					
		});
		
		// Delete the echo video when the user exits.
		// @author Mike Lee
		Runtime.getRuntime().addShutdownHook(new Thread() {
		      public void run() {
		        deleteEchoVideo();
		      }
		    });
		
	} 
}

