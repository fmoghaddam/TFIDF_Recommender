package model;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.j256.ormlite.dao.Dao;

import main.News;
import main.Similarity;
import tfidf.TFIDFCalc;

public class RecommenderService {

	private final ExecutorService similarityCalculatorService = Executors.newFixedThreadPool(1);
	private static final long DELAY_BETWEEN_EACH_RUN = 10000; 
	public RecommenderService(DatabaseAccess db) {
		similarityCalculatorService.submit(() -> {
			while (true) {
				calculateSimilarities(db.getSimilarityDao(), db.getNewsDao());
				Thread.sleep(DELAY_BETWEEN_EACH_RUN );
			}
		});
	}
	
	private static void calculateSimilarities(Dao<Similarity, String> similarityDao, Dao<News, String> newsDao){
		try {
			final List<News> queryForAll = newsDao.queryForAll();
			final News[] allNews = queryForAll.toArray(new News[0]);

			for(int i=0;i<allNews.length;i++) {
				final News firstNews = allNews[i];
				for(int j=i+1;j<allNews.length;j++) {
					final News secondtNews = allNews[j];
					final List<Similarity> allSim = similarityDao.queryForAll();
					boolean found=false;
					for(Similarity s:allSim) {
						if(s.getNews1().getId()==firstNews.getId() && s.getNews2().getId() == secondtNews.getId()) {
							found=true;
							break;
						}
					}
					if(found) {
						continue;
					}else {
						double sim = TFIDFCalc.calc(firstNews, secondtNews, queryForAll);
						Similarity similarity = new Similarity();
						similarity.setNews1(firstNews);
						similarity.setNews2(secondtNews);
						similarity.setValue(sim);
						similarityDao.create(similarity);
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
