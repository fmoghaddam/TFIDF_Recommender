package main;
import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import tfidf.TFIDFCalc;
import util.MapUtil;

public class Main {
	public static void main(String[] args) throws SQLException {
		final ConnectionSource initDb = initDb();

		final Dao<News, String> newsDao = DaoManager.createDao(initDb, News.class);
		final Dao<RatingFullData, String> ratingDao = DaoManager.createDao(initDb, RatingFullData.class);
		final Dao<Similarity, String> similarityDao = DaoManager.createDao(initDb, Similarity.class);

		//		 TableUtils.createTableIfNotExists(initDb, News.class);
		//		 TableUtils.createTableIfNotExists(initDb, RatingFullData.class);
		//		 TableUtils.createTableIfNotExists(initDb, Similarity.class);

		final ExecutorService similarityCalculatorService = Executors.newFixedThreadPool(1);
		similarityCalculatorService.submit(() -> {
			while (true) {
				calculateSimilarities(similarityDao, newsDao);
				Thread.sleep(10000);
			}
		});

		/**
		 * Main API
		 */
		get("/recom/:userId", (req, res) -> returnAnswer(req.params(":userId"),newsDao,ratingDao,similarityDao));
		/**
		 * Extra, can be removed
		 */
		get("/news", (req, res) -> returnNews(newsDao));

		post("/rating", (req, res) -> {
			final int userId = Integer.parseInt(req.queryParams("userId"));
			final String newsId = req.queryParams("newsId");
			News news = null;
			for (final CloseableIterator<News> iterator = newsDao.iterator(); iterator.hasNext();) {
				final News localNews = (News) iterator.next();
				if (localNews.getId() == Integer.parseInt(newsId)) {
					news = localNews;
					break;
				}
			}

			final boolean fake = Boolean.parseBoolean(req.queryParams("fake"));
			final int ratingNumber = Integer.parseInt(req.queryParams("rating"));
			final String personality = req.queryParams("personality");

			final RatingFullData rating = new RatingFullData();
			rating.setPersonality(personality);
			rating.setFake(fake);
			rating.setUserId(userId);
			rating.setNews(news);
			rating.setRating(ratingNumber);
			rating.setView(1);

			try {
				ratingDao.create(rating);
			} catch (Exception e) {
				for (final CloseableIterator<RatingFullData> iterator = ratingDao.iterator(); iterator.hasNext();) {
					final RatingFullData localRating = (RatingFullData) iterator.next();
					if (localRating.getUserId() == userId && localRating.getNews().getId() == news.getId()) {
						rating.setId(localRating.getId());
						break;
					}
				}
				ratingDao.update(rating);
			}
			res.status(201);
			return null;
		});

		post("/news", (req, res) -> {
			final String body = req.queryParams("body");
			final String date = req.queryParams("date");

			final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

			final News news = new News();
			news.setBody(body);
			news.setDate(formatter.parse(date));

			newsDao.create(news);
			res.status(201);
			return null;
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

	private static Object returnNews(Dao<News, String> newsDao) throws SQLException {
		StringBuilder result = new StringBuilder();
		newsDao.iterator().forEachRemaining(p -> result.append(p.toString()).append("\n"));
		return result.toString();
	}

	private static ConnectionSource initDb() {
		try {
			String databaseUrl = "jdbc:postgresql://127.0.0.1:5432/postgres";
			// String databaseUrl = "jdbc:h2:mem:account";
			ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);
			((JdbcConnectionSource) connectionSource).setUsername("farshad");
			((JdbcConnectionSource) connectionSource).setPassword("farshad");
			return connectionSource;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Object returnAnswer(String userId, Dao<News, String> newsDao, Dao<RatingFullData, String> ratingDao, Dao<Similarity, String> similarityDao) {
		try {
			final List<RatingFullData> allRatings = ratingDao.queryForAll();
			final List<RatingFullData> allNewsUseRate = new ArrayList<>();
			for(RatingFullData r:allRatings) {
				if(r.getUserId()==Integer.parseInt(userId)) {
					allNewsUseRate.add(r);
				}
			}

			final List<News> allNews = newsDao.queryForAll();
			final Map<News,Double> result = new HashMap<>();
			for(News n:allNews) {
				if(TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis()-n.getDate().getTime())<=24) {
					result.put(n,calculateRating(allNewsUseRate,similarityDao,n));
				}
			}
			final Map<News, Double> sortedResult = MapUtil.sortByValueDescending(result);
			for(News n:sortedResult.keySet()) {
				return n;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return "[]";
	}

	private static double calculateRating(List<RatingFullData> allRatingsUserHas, Dao<Similarity, String> similarityDao, News n) {
		try {
			double nom = 0;
			double denom = 0;
			for(RatingFullData r: allRatingsUserHas) {
				final News news = r.getNews();
				double sim=0;
				if(news.getId()<n.getId()) {
					sim = similarityDao.queryBuilder().where().eq("news1_id",news.getId()).and().eq("news2_id",n.getId()).query().get(0).getValue();
				}else {
					sim = similarityDao.queryBuilder().where().eq("news1_id",n.getId()).and().eq("news2_id",news.getId()).query().get(0).getValue();
				}
				nom += r.getRating()*sim;
				denom +=sim;
			}
			final double res = nom/denom;
			if(Double.isNaN(res)) {
				return 0;
			}
			return res;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
}