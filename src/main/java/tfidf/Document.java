package tfidf;

import java.util.HashMap;
import java.util.Set;

import model.Item;

/**
 * This class represents one document.
 * It will keep track of the term frequencies.
 * @author swapneel
 *
 */
public class Document implements Comparable<Document>{

	/**
	 * A hashmap for term frequencies.
	 * Maps a term to the number of times this terms appears in this document. 
	 */
	private HashMap<String, Integer> termFrequency;

	private Item item;

	public Document(Item item) {
		this.item = item;
		termFrequency = new HashMap<String, Integer>();
		preProcess();
	}

	private void preProcess() {
		String body = item.getContent();
		String[] split = body.split(" ");
		for(int i=0;i<split.length;i++) {
			String nextWord = split[i];
			String filteredWord = nextWord.replaceAll("[^A-Za-z0-9]", "").toLowerCase();

			if (!(filteredWord.equalsIgnoreCase(""))) {
				if (termFrequency.containsKey(filteredWord)) {
					int oldCount = termFrequency.get(filteredWord);
					termFrequency.put(filteredWord, ++oldCount);
				} else {
					termFrequency.put(filteredWord, 1);
				}
			}
		}
	}

	public double getTermFrequency(String word) {
		if (termFrequency.containsKey(word)) {
			return termFrequency.get(word);
		} else {
			return 0;
		}
	}

	public Set<String> getTermList() {
		return termFrequency.keySet();
	}

	private Item getNews() {
		return item;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Document arg0) {
		boolean equals = item.getContent().equals(arg0.getNews().getContent());
		if(equals) {
			return 0;
		}else {
			return 1;
		}
	}

	@Override
	public int hashCode() {
		return item.getContent().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Document other = (Document) obj;
		if (item == null) {
			if (other.item != null)
				return false;
		} else if (!item.equals(other.item))
			return false;
		return true;
	}
}