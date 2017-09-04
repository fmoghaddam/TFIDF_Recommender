package controler;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import model.Item;
import model.ItemView;
import model.RatingFullData;
import model.Similarity;

public class DatabaseAccess {
	private final ConnectionSource cs;
	private static final String dbUrl = "jdbc:postgresql://127.0.0.1:5432/postgres";
	private static final String dbUserName = "postgres";
	private static final String dbPassword = "postgres";

	private final Dao<Item, String> itemDao;
	private final Dao<ItemView, String> itemViewDao;
	private final Dao<RatingFullData, String> ratingDao;
	private final Dao<Similarity, String> similarityDao;

	public DatabaseAccess() throws SQLException {
		cs = new JdbcConnectionSource(dbUrl);
		((JdbcConnectionSource) cs).setUsername(dbUserName);
		((JdbcConnectionSource) cs).setPassword(dbPassword);

		itemDao = DaoManager.createDao(cs, Item.class);
		itemViewDao = DaoManager.createDao(cs, ItemView.class);
		ratingDao = DaoManager.createDao(cs, RatingFullData.class);
		similarityDao = DaoManager.createDao(cs, Similarity.class);

		initTables();
	}

	private void initTables() throws SQLException {
		try {
			itemDao.countOf();
		} catch (SQLException ex) {
			TableUtils.createTableIfNotExists(cs, Item.class);
			itemDao.executeRaw("ALTER TABLE item DROP COLUMN content;");
			itemDao.executeRaw("ALTER TABLE item ADD COLUMN content text;");
		}
		try {
			ratingDao.countOf();
		} catch (SQLException ex) {
			TableUtils.createTableIfNotExists(cs, RatingFullData.class);
		}
		try {
			itemViewDao.countOf();
		} catch (SQLException ex) {
			TableUtils.createTableIfNotExists(cs, ItemView.class);
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

	public Dao<Item, String> getItemDao() {
		return itemDao;
	}

	public Dao<ItemView, String> getItemViewDao() {
		return itemViewDao;
	}
	
	public Dao<RatingFullData, String> getRatingDao() {
		return ratingDao;
	}

	public Dao<Similarity, String> getSimilarityDao() {
		return similarityDao;
	}

}
