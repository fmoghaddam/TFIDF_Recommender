package model;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import main.News;
import main.RatingFullData;
import main.Similarity;

public class DatabaseAccess {
	private final ConnectionSource cs;
	private static final String dbUrl = "jdbc:postgresql://127.0.0.1:5432/postgres";
	private static final String dbUserName = "postgres";
	private static final String dbPassword = "postgres";

	private final Dao<News, String> newsDao;
	private final Dao<RatingFullData, String> ratingDao;
	private final Dao<Similarity, String> similarityDao;

	public DatabaseAccess() throws SQLException {
		cs = new JdbcConnectionSource(dbUrl);
		((JdbcConnectionSource) cs).setUsername(dbUserName);
		((JdbcConnectionSource) cs).setPassword(dbPassword);

		newsDao = DaoManager.createDao(cs, News.class);
		ratingDao = DaoManager.createDao(cs, RatingFullData.class);
		similarityDao = DaoManager.createDao(cs, Similarity.class);

		initTables();
	}

	private void initTables() throws SQLException {
		try {
			newsDao.countOf();
		} catch (SQLException ex) {
			TableUtils.createTableIfNotExists(cs, News.class);
			newsDao.executeRaw("ALTER TABLE news DROP COLUMN body;");
			newsDao.executeRaw("ALTER TABLE news ADD COLUMN body text;");
		}
		try {
			ratingDao.countOf();
		} catch (SQLException ex) {
			TableUtils.createTableIfNotExists(cs, RatingFullData.class);
		}
		try {
			similarityDao.countOf();
		} catch (SQLException ex) {
			TableUtils.createTableIfNotExists(cs, Similarity.class);
		}
	}

	public ConnectionSource getCs() {
		return cs;
	}

	public Dao<News, String> getNewsDao() {
		return newsDao;
	}

	public Dao<RatingFullData, String> getRatingDao() {
		return ratingDao;
	}

	public Dao<Similarity, String> getSimilarityDao() {
		return similarityDao;
	}

}
