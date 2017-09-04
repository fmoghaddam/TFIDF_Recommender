package controler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.j256.ormlite.dao.Dao;

import model.Item;
import model.Similarity;
import tfidf.TFIDFCalc;

public class RecommenderService {

	private final ExecutorService similarityCalculatorService = Executors.newFixedThreadPool(1);
	private static final long DELAY_BETWEEN_EACH_RUN = 10000; 
	public RecommenderService(DatabaseAccess db) {
		similarityCalculatorService.submit(() -> {
			while (true) {
				calculateSimilarities(db.getSimilarityDao(), db.getItemDao());
				Thread.sleep(DELAY_BETWEEN_EACH_RUN );
			}
		});
	}
	
	private static void calculateSimilarities(Dao<Similarity, String> similarityDao, Dao<Item, String> itemDao){
		try {
			final List<Item> queryForAll = itemDao.queryForAll();
			final Item[] allItem = queryForAll.toArray(new Item[0]);

			for(int i=0;i<allItem.length;i++) {
				final Item firstItem = allItem[i];
				for(int j=i+1;j<allItem.length;j++) {
					final Item secondtItem = allItem[j];
					final List<Similarity> allSim = similarityDao.queryForAll();
					boolean found=false;
					for(Similarity s:allSim) {
						if(s.getItem1().getItemId()==firstItem.getItemId() && s.getItem2().getItemId() == secondtItem.getItemId()) {
							found=true;
							break;
						}
					}
					if(found) {
						continue;
					}else {
						double sim = TFIDFCalc.calc(firstItem, secondtItem, queryForAll);
						final Similarity similarity = new Similarity();
						similarity.setItem1(firstItem);
						similarity.setItem2(secondtItem);
						similarity.setValue(sim);
						similarityDao.create(similarity);
					}
				}
			}
		}catch(final Exception e) {
			e.printStackTrace();
		}
	}
}
