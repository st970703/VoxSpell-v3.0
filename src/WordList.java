import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class WordList {

	private File _listFile;
	
	public WordList(File list) {
		_listFile = list;
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
		
		return lists;
	}
	
	public int numOfLevels() {
		ArrayList<ArrayList<String>> tempList = getWords();
		
		return tempList.size();
	}
	
}
