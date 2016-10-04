import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * This class represents a quiz, taking words from the provided WordList object. 
 * @author wayne
 * @editor Mike Lee
 */
public class Quiz implements ActionListener{
	
	private int _wordCount;
	private int _totalWords;
	private int _attempts;
	private int _masteredWords;
	private ArrayList<ArrayList<String>> _lists;
	private ArrayList<String> _words; 
	private ArrayList<Integer> _previousWords;
	private Statistics _stats;
	private SpellingAid _parent;
	private WordList _wordList;
	
	/**
	 * Initializes fields, selects words for the quiz, and checks that there are enough words to perform a quiz.
	 * @param level - the spelling level of the quiz to be created
	 * @param parent - the SpellingAid which called the constructor
	 * @param wordList - the WordList object that words should be drawn from
	 */
	public Quiz(int level, SpellingAid parent, WordList wordList) {
		_parent = parent;
		_stats = Statistics.getInstance();
		_wordCount = 0;
		_masteredWords = 0;
		_previousWords = new ArrayList<>();
		_words = new ArrayList<>();
		_lists = wordList.getWords();
		_wordList = wordList;
		
		selectWords(level);
		_totalWords = Math.min(_words.size(), 10);
		
		if (_totalWords == 0) {
			JOptionPane.showMessageDialog(null, "Not enough words to make a quiz!", "Uh oh!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Selects 10 or less random words from the WordList, to be used in the quiz. Only selects less than 10 words
	 * if there are less than 10 words to select from. In those situations, it selects all words available, in 
	 * random order.
	 * @param level - the spelling level that the words should be chosen from
	 */
	private void selectWords(int level) {
		ArrayList<String> tempList = _lists.get(level - 1);
		int pos = 0;
		for (int i = 0; i < Math.min(10, tempList.size()); i++) {
			while (_words.contains(tempList.get(pos = (int)(Math.random() * tempList.size())))) { }
			_words.add(tempList.get(pos));
		}
	}

	/**
	 * If there are more words left to test, calls sayWord with the next word, prefixing it with the provided line. If all the words have 
	 * been tested, the method notifies the user that the quiz is finished and calls {@link SpellingAid#levelCompleted()} if enough words
	 * were spelled correctly.
	 * @param line - String to prefix the word with
	 * @return boolean indicating whether or not a word was pronounced
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
			if (_masteredWords >= 9 && _wordList.getQuizType() == QuizType.NEW) {
				// call method from SpellingAid, which allows user to move up levels
				_parent.levelCompleted();
			}
			return false;
		}
	}
	
	/**
	 * Repeats the word, allowing the user to relisten to it.
	 */
	public void repeatWordWithNoPenalty() {
		String word = _words.get(_previousWords.get(_previousWords.size() - 1));
		sayWord(word);
	}
	
	/**
	 * Repeats the last word pronounced, when the user has incorrectly spelled it once.
	 */
	public void repeatWord() {
		String word = _words.get(_previousWords.get(_previousWords.size() - 1));
		sayWord("Incorrect, Please try again ... ... " + word + ", ... ... " + word);
	}
	
	/**
	 * Spells the word out for the user, used in review mode when word was failed.
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
	private void sayWord(final String word) {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				try {
					File file = new File(".sayText.scm");

					if (!file.exists()) {
						file.createNewFile();
					}
					
					String voice = _parent.getVoice();
					
					BufferedWriter bw = new BufferedWriter(new FileWriter(file));
					
					bw.write("(" + "voice_" + voice + ")\n");
					
					bw.write("(SayText \"" + word + " ... ... ... ... \")");
					
					bw.close();
					
					ProcessBuilder pb = new ProcessBuilder("bash", "-c", "festival -b .sayText.scm");
					
					Process pro = pb.start();
					
					pro.waitFor();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
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
			// do nothing
		} else {
			String userWord = trimSpaces(e.getActionCommand());
			_attempts++;
			// if the user spells word correctly
			if (userWord.toLowerCase().equals(_words.get(_previousWords.get(_previousWords.size() - 1)).toLowerCase())) {
				// if this is the second attempt at spelling this word
				if (_attempts == 2) { 
					_stats.addFaulted(userWord);
				} else if (_attempts == 1) { // if this is the first attempt at spelling this word
					_stats.addMastered(userWord);
					_masteredWords++;
					_wordList.removeFromFailedList(userWord.toLowerCase());
				}
				sayNextWord(" ... ... Correct! ... "); // move onto the next word
			} else { // if the user spells the word wrong
				if (_attempts == 1) { // if this was their first attempt 
					repeatWord();
				} else if (_attempts == 2) { // if the user fails the word
					if (_wordList.getQuizType().equals(QuizType.NEW)) { //if this is a new quiz, remove from/add to appropriate lists and add stats
						_stats.addFailed(_words.get(_previousWords.get(_previousWords.size() - 1)));
						_wordList.addToFailedList(_words.get(_previousWords.get(_previousWords.size() - 1)));
						sayNextWord("Incorrect. ... ");
					} else { //if this is a review quiz, give user another chance to spell word, spelling it out for them
						spellWord(); //however, don't remove from failedlist, even if they get it right, because they clearly need more practice
						_stats.addFailed(_words.get(_previousWords.get(_previousWords.size() - 1)));
					}
				} else { //if they fail the extra try they get in review quizzes
					sayNextWord(" ... ... Incorrect. ... ");
				}
			}
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
	 * Checks the given string if it consists of only A-Z, a-z , apostrophes, and spaces.
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
}
