import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class represents a list of words, from which words are drawn from for spelling quizzes.
 * @author wayne
 *
 */
public class WordList {

	private File _listFile;
	private QuizType _quizT;
	
	/**
	 * Constructor, takes a file and a quiztype to construct a corresponding WordList object.
	 * @param list - File containing the list of words
	 * @param qt - The type of quiz this WordList is for (either NEW or REVIEW)
	 */
	public WordList(File list, QuizType qt) {
		if (!list.exists()) {
			try {
				list.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			initializeList(list);
		}
		
		_listFile = list;
		_quizT = qt;
	}
	
	/**
	 * Reads through the _listFile and returns an ArrayList<ArrayList<String>> which contains the words stored in _listFile, 
	 * organized into levels. E.g. The first ArrayList contained in returned ArrayList has words from Level 1, the second 
	 * has words from Level 2... etc.
	 * @return - ArrayList<ArrayList<String>> containing the words from list of words, organized by level.
	 */
	public ArrayList<ArrayList<String>> getWords() {
		BufferedReader br = null;
		ArrayList<ArrayList<String>> lists = new ArrayList<>();
		
		try {
			String line;
			
			br = new BufferedReader(new FileReader(_listFile.getAbsoluteFile()));
			
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
		
		// If the file was completely empty, not even containing the %Level structure, then add it in
		if (lists.size() == 0) {
			initializeList(_listFile);
			lists = getWords();
		}
		
		return lists;
	}
	
	/**
	 * Creates a temporary WordList, using the NZCER-spelling-lists.txt, to determine the number of levels in it.
	 * @return - int containing the number of levels in the word list
	 */
	public int numOfLevels() {
		WordList temp = new WordList(new File("NZCER-spelling-lists.txt"), QuizType.NEW);

		ArrayList<ArrayList<String>> tempList = temp.getWords();
		
		return tempList.size();
	}
	
	/**
	 * Returns the QuizType for this particular WordList.
	 * @return this WordLists QuizType
	 */
	public QuizType getQuizType() {
		return _quizT;
	}
	
	/**
	 * Fills a file with the correct, empty structure for a word list. E.g. Writes %Level 1, %Level 2, ... until it reaches
	 * the level returned by numOfLevels().
	 * @param list - the File which will be overwritten
	 */
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
	
	/**
	 * Adds the provided word to the list of failed words, in the correct structure, provided it's not there already. 
	 * @param failedWord - the word to add to the failed list
	 */
	public void addToFailedList(String failedWord) {
		ArrayList<String> wordsInList = new ArrayList<>();
		
		BufferedReader br = null;
		
		File failedWords = new File(".failedWords");
		
		try {
			String line;
			br = new BufferedReader(new FileReader(failedWords));
			
			// Store all current content into ArrayList
			while ((line = br.readLine()) != null) {
				//do stuff with each line
				wordsInList.add(line);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if (!wordsInList.contains(failedWord) && !wordsInList.contains(addCapital(failedWord.toLowerCase())) && _quizT == QuizType.NEW) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(failedWords.getAbsoluteFile()));
				
				int level = findLevel(failedWord);
				if (level == 0) { //If, for some reason, the word cannot be found within NZCER-spelling-lists.txt
					System.out.println("For some reason, you tried to add a word to this list which isn't in the source list of words.");
				}

				for (String word : wordsInList) {
					bw.write(word + "\n");
					if (word.equals("%Level " + level)) {
						bw.write(failedWord + "\n");
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
	 * Removes the given word from the list of failed words, provided it's there.
	 * @param userWord - the word to remove from the list of failed words
	 */
	public void removeFromFailedList(String userWord) {
		ArrayList<String> wordsInList = new ArrayList<>();

		BufferedReader br = null;
		
		File failedWords = new File(".failedWords");

		try {
			String line;

			br = new BufferedReader(new FileReader(failedWords));

			// Stores all current content into ArrayList
			while ((line = br.readLine()) != null) {
				wordsInList.add(line);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		if (wordsInList.contains(userWord.toLowerCase()) || wordsInList.contains(addCapital(userWord.toLowerCase()))) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(failedWords.getAbsoluteFile()));

				for (String word : wordsInList) {
					// As long as the word doesn't equal the word to be removed, it is written back to the file
					if (!word.equals(userWord) && !word.equals(addCapital(userWord))) {
						bw.write(word + "\n");
					}
				}

				bw.close();

			} catch (IOException e) {
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
	 * Changes the first letter of word given to a capital letter.
	 * @param word - word to change first letter to capital
	 * @return - word, with the first letter changed to capitals
	 */
	private String addCapital(String word) {
		if (word.length() < 2) {
			return word.toUpperCase();
		} else {
			String start = word.substring(0, 1);
			String rest = word.substring(1);
			String result = start.toUpperCase() + rest;
			return result;
		}
	}
	
	/**
	 * Finds and returns the level that the given word belongs to.
	 * @param word - word to find level for
	 * @return - and integer containing the level which the given word belongs to
	 */
	private int findLevel(String word) {
		WordList temp = new WordList(new File("NZCER-spelling-lists.txt"), QuizType.NEW);
		ArrayList<ArrayList<String>> words = temp.getWords();
		
		for (int i = 0; i < words.size(); i++) {
			ArrayList<String> tempList = words.get(i); 
			for (int j = 0; j < tempList.size(); j++) {
				if (tempList.get(j).toLowerCase().equals(word.toLowerCase())) {
					return (i + 1);
				}
			}
		}
		
		return 0;
	}
	
}
