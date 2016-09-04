import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 * This class represents statistics of the user. 
 * @author wayne
 *
 */
public class Statistics extends AbstractTableModel{
	private ArrayList<String> _words;
	private ArrayList<String> _sortedWords;
	private ArrayList<Integer> _masteredCount;
	private ArrayList<Integer> _faultedCount;
	private ArrayList<Integer> _failedCount;
	private ArrayList<ArrayList<ArrayList<String>>> _levelCounts; //0 = mastered, 1 = faulted, 2 = failed, 3 = no attempts
	// rip going to need ArrayList<ArrayList<ArrayList<String>>>
	private final String[] columnNames = {"Level", "Mastered", "Faulted", "Failed", "No Attempts"};
	private ArrayList<ArrayList<String>> _lists;
	
	/**
	 * Initializes the fields and then loads previously stored data from .stats file
	 */
	public Statistics(List list) {
		_words = new ArrayList<>();
		_sortedWords = new ArrayList<>();
		_masteredCount = new ArrayList<>();
		_faultedCount = new ArrayList<>();
		_failedCount = new ArrayList<>();
		_lists = list.getWords();
		_levelCounts = new ArrayList<>();
		initializeLevelCount(); //TODO: implement loading memory from previous sessions
		loadStats();
	}
	
	private void modifyLevelCount(int listToAddTo, String word, int level) {
		for (int i = 0; i < _levelCounts.size(); i++) {
			if (listToAddTo != i) {
				_levelCounts.get(i).get(level).remove(word);
			} else {
				if (!_levelCounts.get(i).get(level).contains(word)) {
					_levelCounts.get(i).get(level).add(word);
				}
			}
		}
	}

	private void initializeLevelCount() {		
		for (int i = 0; i < 3; i++) {
			_levelCounts.add(new ArrayList<ArrayList<String>>());
			
			for (int j = 0; j < _lists.size(); j++) {
				_levelCounts.get(i).add(new ArrayList<String>());
			}
		}
		
		initializeNoAttempts();
	}
	
	private void initializeNoAttempts() {
		_levelCounts.add(new ArrayList<ArrayList<String>>());
		for (int i = 0; i < _lists.size(); i++) {
			_levelCounts.get(_levelCounts.size() - 1).add(new ArrayList<String>());
			for (String word : _lists.get(i)) {
				_levelCounts.get(_levelCounts.size() - 1).get(i).add(word);
			}
		}
	}
	
	/**
	 * Add to the mastered count and sorts
	 * @param word - the word to add stats to
	 */
	public void addMastered(String word) {
		if (_words.contains(word)) {
			int pos = _words.indexOf(word);
			_masteredCount.set(pos, _masteredCount.get(pos) + 1);
		} else {
			addWord(word);
			_masteredCount.add(1);
			_faultedCount.add(0);
			_failedCount.add(0);
		}
		addLevelCount(0, word);
		saveStats();
	}
	
	/**
	 * Add to the faulted count and sorts
	 * @param word - the word to add stats to
	 */
	public void addFaulted(String word) {
		if (_words.contains(word)) {
			int pos = _words.indexOf(word);
			_faultedCount.set(pos, _faultedCount.get(pos) + 1);
		} else {
			addWord(word);
			_masteredCount.add(0);
			_faultedCount.add(1);
			_failedCount.add(0);
		}
		addLevelCount(1, word);
		saveStats();
	}
	
	/**
	 * Add to the failed count and sorts
	 * @param word - the word to add stats to
	 */
	public void addFailed(String word) {
		if (_words.contains(word)) {
			int pos = _words.indexOf(word);
			_failedCount.set(pos, _failedCount.get(pos) + 1);
		} else {
			addWord(word);
			_masteredCount.add(0);
			_faultedCount.add(0);
			_failedCount.add(1);
		}
		addLevelCount(2, word);
		saveStats();
	}
	
	private void addLevelCount(int list, String word) {
		for (int i = 0; i < _lists.size(); i++) {
			if (_lists.get(i).contains(word)) {
				//_levelCounts.get(list).get(i).set(i, _levelCounts.get(list).get(i) + 1);
				//replace with use of modifyLevelCount()
				modifyLevelCount(list, word, i);
			}
		}
		fireTableDataChanged();
	}
	
	/**
	 * Add given word to both lists, then sorts one.
	 * @param word
	 */
	private void addWord(String word) {
		_words.add(word);
		_sortedWords.add(word);
		sortWords();
	}
	
	/**
	 * Sorts words in the sortedWords arraylist
	 */
	private void sortWords() {
		_sortedWords.sort(null);
	}
	
	/**
	 * Formats and returns the current stats
	 * @return
	 */
	public ArrayList<String> getStats() {
		ArrayList<String> formattedStats = new ArrayList<>();
		String headers = "Mastered\tFaulted\tFailed\tWord\n";
		String underLine = "-----------------------------------------------------------------------------------\n";
		formattedStats.add(headers);
		formattedStats.add(underLine);
		
		for (String word : _sortedWords) {
			int pos = _words.indexOf(word);
			String format = "";
			format += _masteredCount.get(pos) + "\t";
			format += _faultedCount.get(pos) + "\t";
			format += _failedCount.get(pos) + "\t";
			format += word + "\n";
			formattedStats.add(format);
		}
		
		return formattedStats;
	}
	
	/**
	 * Saves the current stats to the .stats file
	 */
	private void saveStats() {
		try {
			File statsFile = new File(".stats"); 
			if (!statsFile.exists()) {
				statsFile.createNewFile();
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(statsFile.getAbsoluteFile()));
			
			ArrayList<String> stats = getStats();
			
			for (int i = 2; i < stats.size(); i++) {
				bw.write(stats.get(i));
			}
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads stats from the .stats file
	 */
	private void loadStats() {
		try {
			File statsFile = new File(".stats");
			
			if (!statsFile.exists()) {
				statsFile.createNewFile();
			}
			
			String line;
			
			BufferedReader br = new BufferedReader(new FileReader(statsFile.getAbsoluteFile()));
			
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				_masteredCount.add(Integer.parseInt(tokens[0]));
				_faultedCount.add(Integer.parseInt(tokens[1]));
				_failedCount.add(Integer.parseInt(tokens[2]));
				addWord(tokens[3]);
			}
			
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Clears all fields, and then saves it, clearing the .stats file too
	 */
	public void clearStats() {
		_masteredCount.clear();
		_faultedCount.clear();
		_failedCount.clear();
		_words.clear();
		_sortedWords.clear();
		saveStats();
	}
	
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public int getRowCount() {
		return _lists.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex > 0) {
			int count = _levelCounts.get(columnIndex - 1).get(rowIndex).size();
			
			return count + "/" + _lists.get(rowIndex).size();
		} else {
			return "Level " + (rowIndex + 1);
		}
	}
}
