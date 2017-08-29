package model;

import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;

import main.News;
import main.RatingFullData;
import main.Similarity;
import util.MapUtil;

public class ApiHandler {

	public ApiHandler(DatabaseAccess db) {
		start(db);
	}

	private static Object returnNews(Dao<News, String> newsDao) throws SQLException {
		StringBuilder result = new StringBuilder();
		newsDao.iterator().forEachRemaining(p -> result.append(p.toString()).append("\n"));
		return result.toString();
	}

	private static Object returnAnswer(String userId, Dao<News, String> newsDao, Dao<RatingFullData, String> ratingDao,
			Dao<Similarity, String> similarityDao) {
		try {
			final List<RatingFullData> allRatings = ratingDao.queryForAll();
			final List<RatingFullData> allNewsUseRate = new ArrayList<>();
			for (RatingFullData r : allRatings) {
				if (r.getUserId() == Integer.parseInt(userId)) {
					allNewsUseRate.add(r);
				}
			}

			final List<News> allNews = newsDao.queryForAll();
			final Map<News, Double> result = new HashMap<>();
			for (News n : allNews) {
				if (TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - n.getDate().getTime()) <= 24) {
					result.put(n, calculateRating(allNewsUseRate, similarityDao, n));
				}
			}
			final Map<News, Double> sortedResult = MapUtil.sortByValueDescending(result);
			for (News n : sortedResult.keySet()) {
				return n;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "[]";
	}

	private static double calculateRating(List<RatingFullData> allRatingsUserHas, Dao<Similarity, String> similarityDao,
			News n) {
		try {
			double nom = 0;
			double denom = 0;
			for (RatingFullData r : allRatingsUserHas) {
				final News news = r.getNews();
				double sim = 0;
				if (news.getId() < n.getId()) {
					sim = similarityDao.queryBuilder().where().eq("news1_id", news.getId()).and()
							.eq("news2_id", n.getId()).query().get(0).getValue();
				} else {
					sim = similarityDao.queryBuilder().where().eq("news1_id", n.getId()).and()
							.eq("news2_id", news.getId()).query().get(0).getValue();
				}
				nom += r.getRating() * sim;
				denom += sim;
			}
			final double res = nom / denom;
			if (Double.isNaN(res)) {
				return 0;
			}
			return res;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public void start(DatabaseAccess db) {
		get("/recom/:userId", (req, res) -> returnAnswer(req.params(":userId"), db.getNewsDao(), db.getRatingDao(),
				db.getSimilarityDao()));
		/**
		 * Extra, can be removed
		 */
		get("/news", (req, res) -> returnNews(db.getNewsDao()));

		post("/rating", (req, res) -> {
			final int userId = Integer.parseInt(req.queryParams("userId"));
			final String newsId = req.queryParams("newsId");
			News news = null;
			for (final CloseableIterator<News> iterator = db.getNewsDao().iterator(); iterator.hasNext();) {
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
				db.getRatingDao().create(rating);
			} catch (Exception e) {
				for (final CloseableIterator<RatingFullData> iterator = db.getRatingDao().iterator(); iterator
						.hasNext();) {
					final RatingFullData localRating = (RatingFullData) iterator.next();
					if (localRating.getUserId() == userId && localRating.getNews().getId() == news.getId()) {
						rating.setId(localRating.getId());
						break;
					}
				}
				db.getRatingDao().update(rating);
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

			db.getNewsDao().create(news);
			res.status(201);
			return null;
		});
	}

}
