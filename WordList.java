import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class WordList {

	private File _listFile;
	private QuizType _quizT;
	
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
		
		if (lists.size() == 0) {
			initializeList(_listFile);
			lists = getWords();
		}
		
		return lists;
	}
	
	public int numOfLevels() {
		WordList temp = new WordList(new File("NZCER-spelling-lists.txt"), QuizType.NEW);

		ArrayList<ArrayList<String>> tempList = temp.getWords();
		
		return tempList.size();
	}
	
	public QuizType getQuizType() {
		return _quizT;
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
	
	/**
	 * Adds the most recent word to the given list.
	 * @param list - list to add the most recent word to
	 */
	public void addToFailedList(String failedWord) {
		ArrayList<String> wordsInList = new ArrayList<>();
		
		BufferedReader br = null;
		
		File failedWords = new File(".failedWords");
		
		try {
			String line;
			br = new BufferedReader(new FileReader(failedWords));
			
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
				if (level == 0) {
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
	
	public void removeFromFailedList(String userWord) {
		ArrayList<String> wordsInList = new ArrayList<>();

		BufferedReader br = null;
		
		File failedWords = new File(".failedWords");

		try {
			String line;

			br = new BufferedReader(new FileReader(failedWords));

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
