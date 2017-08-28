package tfidf;

import java.util.ArrayList;
import java.util.List;

import main.News;

public class TFIDFCalc {

	public static double calc(News n1,News n2,List<News> all) {
		ArrayList<Document> documents = new ArrayList<Document>();
		for(News n:all) {
			documents.add(new Document(n));
		}
		Corpus corpus = new Corpus(documents);
		VectorSpaceModel vectorSpace = new VectorSpaceModel(corpus);
		
		Document query = new Document(n1);
		Document d = new Document(n2);
		
		return vectorSpace.cosineSimilarity(query, d);
	}
}