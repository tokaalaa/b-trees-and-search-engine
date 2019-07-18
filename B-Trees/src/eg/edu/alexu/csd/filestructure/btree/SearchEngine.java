package eg.edu.alexu.csd.filestructure.btree;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SearchEngine implements ISearchEngine{
	private String ID;
	IBTree<String, List<ISearchResult>> Tree;
	
	public SearchEngine(int d) {
		Tree = new BTree<String, List<ISearchResult>>(d);
	}
	
	@Override
	public void indexWebPage(String filePath) {
		// TODO Auto-generated method stub
		if(filePath == null || filePath.length() == 0) {
			throw new RuntimeErrorException(null);
		}
		File fXmlFile = new File(filePath);
		if(fXmlFile.exists()) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("doc");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);	
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					ID = eElement.getAttribute("id");
					insertDoc(nNode.getTextContent().toString());
				}
				
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
	}}
	
	private void insertDoc(String txt) {
		// TODO Auto-generated method stub
		String after = txt.trim().replaceAll(" +", " ");
		String text = after.replaceAll("\\r|\\n", " ");
		text = text.trim().replaceAll(" +", " ");
		text = text.toLowerCase();
		String[] splited = text.split("\\s+");
		Map< String,Integer> docMap = wordRankMap(splited);
		insertToBTree(docMap);
	}


	private Map< String,Integer> wordRankMap(String[] splited) {
		// TODO Auto-generated method stub
		 Map< String,Integer> m =  
                 new HashMap< String,Integer>(); 
		for(int i = 0; i< splited.length;i++) {
			String s = splited[i];
			if(m.containsKey(s)) {
				m.put(s, m.get(s)+1);
			}else {
				m.put(s, 1);
			}
		}
		return m;
	}
	

	private void insertToBTree(Map<String, Integer> docMap) {
		// TODO Auto-generated method stub
		for(Map.Entry< String,Integer> m: docMap.entrySet()) {
			ISearchResult searchResult = new SearchResult(ID, m.getValue());
			if(Tree.search(m.getKey()) == null) {
				List<ISearchResult> list = new ArrayList<>();
				list.add(searchResult);
				Tree.insert(m.getKey(), list);
			}else {
				Tree.search(m.getKey()).add(searchResult);
			}
		}
	}


	@Override
	public void indexDirectory(String directoryPath) {
		// TODO Auto-generated method stub
		if(directoryPath == null || directoryPath.length() == 0) {
			throw new RuntimeErrorException(null);
		}
		File file = new File(directoryPath);
		if(file.exists()) {
		for (File childFile : file.listFiles()) {
			String f = childFile.toString();
			if (childFile.isDirectory()) {
				indexDirectory(f);
			} else {
				indexWebPage(f);
			}
		}
		}
	}

	@Override
	public void deleteWebPage(String filePath) {
		// TODO Auto-generated method stub
		if(filePath == null || filePath.length() == 0) {
			throw new RuntimeErrorException(null);
		}
		File fXmlFile = new File(filePath);
		if(fXmlFile.exists()) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("doc");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);	
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					ID = eElement.getAttribute("id");
					deleteDoc(nNode.getTextContent().toString());
				}
				
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }}
	}
	private void deleteDoc(String txt) {
		// TODO Auto-generated method stub
		String after = txt.trim().replaceAll(" +", " ");
		String text = after.replaceAll("\\r|\\n", " ");
		text = text.trim().replaceAll(" +", " ");
		text = text.toLowerCase();
		String[] splited = text.split("\\s+");	
		Set<String> hashSet = new HashSet<String>();
		hashSet.addAll(Arrays.asList(splited));
		deleteFromBtree(hashSet);
	}

	private void deleteFromBtree(Set<String> hashSet) {
		// TODO Auto-generated method stub
		boolean found = false;
		for(String s: hashSet) {
			List<ISearchResult> res = Tree.search(s);
			if(res != null) {
				for(int i = 0; i < res.size();i++) {
					if(res.get(i).getId().equals(ID)) {
						Tree.search(s).remove(i);
						 found = true;
						 break;
					}
				}
				if(Tree.search(s).size() == 0) {
					Tree.delete(s);
				}
				if(!found) {// ID is not found
					break;
				}
			}else {//word is not found in the tree
				break;
			}
		}
	}

	@Override
	public List<ISearchResult> searchByWordWithRanking(String word) {
		// TODO Auto-generated method stub
		if(word == null)
			throw new RuntimeErrorException(null);
	    if(word == "")
	    	return new ArrayList<ISearchResult>();

	    List<ISearchResult> l = Tree.search(word.toLowerCase());
	    if(l == null) {
	    	return new ArrayList<ISearchResult>();
	    }else return l;
	}

	@Override
	public List<ISearchResult> searchByMultipleWordWithRanking(String sentence) {
		// TODO Auto-generated method stub
			if(sentence == null)
				throw new RuntimeErrorException(null);
			 if(sentence == "")
			    	return new ArrayList<ISearchResult>();
			 sentence = sentence.trim().replaceAll(" +", " ");
			 if(sentence.charAt(0) == ' ') {
				 sentence = sentence.substring(1, sentence.length());
			 }
			 String[] splited = (sentence.toLowerCase()).split("\\s+");
			 List<ISearchResult> result = Tree.search(splited[0]);
			 for(int i = 1; i < splited.length; i++) {
				List<ISearchResult> l = Tree.search(splited[i]);
				Map<String ,Integer> m = new HashMap<String, Integer>();
				while(!l.isEmpty()) {
					m.put(l.get(0).getId(), l.remove(0).getRank());
				}
				for(int j = 0; j < result.size(); j++) {
					if(m.containsKey(result.get(j).getId())){
						if(m.get(result.get(j).getId()) < result.get(j).getRank()) {
							result.get(j).setRank(m.get(result.get(j).getId()));
						}
					} else {
						result.remove(j);
					}
				}
			 }
			 
			return result;
	}

}
