package tfidf;

import java.util.ArrayList;
import java.util.List;

import model.Item;

public class TFIDFCalc {

	public static double calc(Item n1,Item n2,List<Item> all) {
		ArrayList<Document> documents = new ArrayList<Document>();
		for(Item n:all) {
			documents.add(new Document(n));
		}
		Corpus corpus = new Corpus(documents);
		VectorSpaceModel vectorSpace = new VectorSpaceModel(corpus);
		
		Document query = new Document(n1);
		Document d = new Document(n2);
		
		return vectorSpace.cosineSimilarity(query, d);
	}
}