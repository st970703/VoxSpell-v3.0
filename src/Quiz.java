import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 * This class represents a quiz, either taking words from wordlist or failedlist.
 * @author wayne
 *
 */
public class Quiz implements ActionListener{
	
	private int _wordCount;
	private int _totalWords;
	private int _attempts;
	private int _masteredWords;
	private File _wordList;
	private File _failedList = new File(".failedlist");
	private File _faultedList = new File(".faultedlist");
	private ArrayList<ArrayList<String>> _lists;
	private ArrayList<String> _words; 
	private ArrayList<Integer> _previousWords;
	private QuizType _quizType; 
	
	private Statistics _stats;
	private SpellingAid _parent;
	
	/**
	 * Initializes most private fields, depending on which quiz type is needed.
	 * @param quizType
	 */
	public Quiz(QuizType quizType, int level, Statistics stats, SpellingAid parent) {
		_parent = parent;
		_stats = stats;
		_wordCount = 0;
		_masteredWords = 0;
		_previousWords = new ArrayList<>();
		_words = new ArrayList<>();
		
		if (quizType.getQuizType().equals(QuizType.NEW.getQuizType())) {
			_wordList = new File("NZCER-spelling-lists.txt");
		} else {
			_wordList = new File(".failedlist");
		}
		
		_quizType = quizType;
		
		if (!_wordList.exists()) {
			try {
				_wordList.createNewFile();
				initializeList(_wordList);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		_lists = getWords(_wordList);
		selectWords(level);
		_totalWords = Math.min(_words.size(), 10);
		if (_totalWords == 0) {
			JOptionPane.showMessageDialog(null, "Not enough words to make a quiz!", "Uh oh!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void initializeList(File list) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(list.getAbsoluteFile()));
			
			for (int i = 0; i < numOfLevels(); i++) {
				bw.write("%Level " + (i + 1) + "\n");
			}
			
			bw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void selectWords(int level) {
		ArrayList<String> tempList = _lists.get(level - 1);
		int pos = 0;
		for (int i = 0; i < Math.min(10, tempList.size()); i++) {
			while (_words.contains(tempList.get(pos = (int)(Math.random() * tempList.size())))) { }
			_words.add(tempList.get(pos));
		}
	}
	
	/**
	 * Grabs words from file and stores in field.
	 */
	private ArrayList<ArrayList<String>> getWords(File wordList) {
		BufferedReader br = null;
		ArrayList<ArrayList<String>> lists = new ArrayList<>();
		
		try {
			String line;
			
			br = new BufferedReader(new FileReader(wordList.getAbsoluteFile()));
			
			ArrayList<String> currentList = new ArrayList<>();
			
			while ((line = br.readLine()) != null) {
				//do stuff with each line
				
				if (line.startsWith("%")) {
					currentList = new ArrayList<>();
					lists.add(currentList);
				} else {
					currentList.add(line);
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return lists;
	}

	/**
	 * Increments wordcount field, keeping track of how many words have been finished, and calls sayWord with a random word from the wordlist.
	 * @return
	 */
	public boolean sayNextWord(String line) {
		_wordCount++;
		if (_wordCount <= _totalWords) {
			_attempts = 0;

			_previousWords.add(_wordCount - 1);
			if (line == null) {
				sayWord(_words.get(_wordCount - 1));
			} else {
				sayWord(line + " " + _words.get(_wordCount - 1));
			}
			return true;
		} else {
			sayWord("Quiz is finished.");
			// call method in spellingaid to potentially shift up levels
			if (_masteredWords >= 9) {
				// call method from SpellingAid, which allows user to move up levels
				_parent.levelCompleted();
			}
			return false;
		}
	}
	
	public void repeatWordWithNoPenalty() {
		String word = _words.get(_previousWords.get(_previousWords.size() - 1));
		sayWord(word);
	}
	
	/**
	 * Repeats the last word pronounced.
	 */
	public void repeatWord() {
		String word = _words.get(_previousWords.get(_previousWords.size() - 1));
		sayWord("Incorrect, Please try again ... ... " + word + ", ... ... " + word);
	}
	
	/**
	 * Spells the word out for the user.
	 */
	public void spellWord() {
		String word = _words.get(_previousWords.get(_previousWords.size() - 1));
		String spelling = word + " is spelled ... ";
		
		for (int i = 0; i < word.length(); i++) {
			spelling += word.charAt(i) + " ... ";
		}
		
		sayWord(spelling);
	}
	
	/**
	 * Uses festival to pronounce the string passed in.
	 * @param word - a String containing what you want to pronounce
	 */
	private void sayWord(String word) {
		System.out.println(word);
		final String wordword = word;
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				try {
					String command = "echo " + wordword + " | festival --tts ";
					ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);


					Process process = pb.start();
					
					try {
						process.waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			
		};
		worker.execute();
	}

	/**
	 * Triggers whenever the user presses Enter in the Input text field.
	 * Calls sayNextWord when appropriate, recording statistics and adding words to required lists.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (!isValidInput(e.getActionCommand())) {
			JOptionPane.showMessageDialog(null, "Not a valid input! Please only use A-Z and a-z.", "Uh oh!", JOptionPane.ERROR_MESSAGE);
		} else if (trimSpaces(e.getActionCommand()).isEmpty()) {
			// do nuthin
		} else {
			String userWord = trimSpaces(e.getActionCommand());
			_attempts++;
			// if the user spells word correctly
			if (userWord.toLowerCase().equals(_words.get(_previousWords.get(_previousWords.size() - 1)).toLowerCase())) {
				if (_attempts == 2) { // add and remove from appropriate lists, add stats, all depending on number of attempts so far
					addToList(_faultedList);
					removeFromList(_failedList);
					_stats.addFaulted(userWord);
				} else if (_attempts == 1) {
					removeFromList(_failedList);
					removeFromList(_faultedList);
					_stats.addMastered(userWord);
					_masteredWords++;
				}
				sayNextWord("Correct! ... "); // move onto the next word
			} else { // if the user spells the word wrong
				if (_attempts == 1) {
					repeatWord();
				} else if (_attempts == 2) { // if the user fails the word
					if (_quizType.getQuizType().equals(QuizType.NEW.getQuizType())) { //if this is a new quiz, remove from / add to appropriate lists and add stats
						removeFromList(_faultedList);
						addToList(_failedList);
						_stats.addFailed(_words.get(_previousWords.get(_previousWords.size() - 1)));
						sayNextWord("Incorrect. ... ");
					} else { //if this is a review quiz, give user another chance to spell word, spelling it out for them
						spellWord(); //however, don't remove from failedlist, even if they get it right, because they clearly need more practice
						_stats.addFailed(_words.get(_previousWords.get(_previousWords.size() - 1)));
					}
				} else { //if they fail the extra try they get in review quizzes
					sayNextWord("Incorrect. ... ");
				}
			}
		}
	}
	
	/**
	 * Removes the most recent word from the given list.
	 * @param list - list to remove the most recent word from
	 */
	private void removeFromList(File list) {
		ArrayList<String> wordsInList = new ArrayList<>();

		BufferedReader br = null;

		try {
			if (!list.exists()) {
				list.createNewFile();
			}

			String line;

			br = new BufferedReader(new FileReader(list));

			while ((line = br.readLine()) != null) {
				//do stuff with each line
				wordsInList.add(line);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		if (wordsInList.contains(_words.get(_previousWords.get(_previousWords.size() - 1)))) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(list.getAbsoluteFile()));

				for (String word : wordsInList) {
					if (!word.equals(_words.get(_previousWords.get(_previousWords.size() - 1)))) {
						bw.write(word + "\n");
					}
				}

				bw.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds the most recent word to the given list.
	 * @param list - list to add the most recent word to
	 */
	private void addToList(File list) {
		ArrayList<String> wordsInList = new ArrayList<>();
		
		BufferedReader br = null;
		
		try {
			if (!list.exists()) {
				list.createNewFile();
			}
			
			String line;
			
			br = new BufferedReader(new FileReader(list));
			
			while ((line = br.readLine()) != null) {
				//do stuff with each line
				wordsInList.add(line);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if (!wordsInList.contains(_words.get(_previousWords.get(_previousWords.size() - 1)))) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(list.getAbsoluteFile()));
				
				for (String word : wordsInList) {
					bw.write(word + "\n");
				}
				
				bw.write(_words.get(_previousWords.get(_previousWords.size() - 1)) + "\n");
				
				bw.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes the spaces from the given string.
	 * @param word - string to remove spaces from
	 * @return - string containing the given string, with spaces removed
	 */
	private String trimSpaces(String word) {
		String returnWord = "";
		for (int i = 0; i < word.length(); i++) {
			if (word.charAt(i) != ' ') {
				returnWord += word.charAt(i);
			}
		}
		return returnWord;
	}
	
	/**
	 * Checks the given string if it consists of only A-Z, a-z and spaces.
	 * @param input - string to check validity of
	 * @return - true if the string is valid, false otherwise
	 */
	private boolean isValidInput(String input) {
		for (int i = 0; i < input.length(); i++) {
			if (!Character.isLetter(input.charAt(i)) && !(input.charAt(i) == ' ') && !(input.charAt(i) == '\'')) {
				return false;
			}
		}
		return true;
	}
	
	private int numOfLevels() {
		File list = new File("NZCER-spelling-lists.txt");
		
		ArrayList<ArrayList<String>> tempList = getWords(list);
		
		return tempList.size();
	}
}
