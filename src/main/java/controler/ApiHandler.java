package controler;

import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;

import model.Item;
import model.ItemView;
import model.RatingFullData;
import model.Similarity;
import util.MapUtil;

public class ApiHandler {

	public ApiHandler(DatabaseAccess db) {
		startApis(db);
	}

	private static Object returnAllItem(Dao<Item, String> ItemDao) throws SQLException {
		List<String> result = new ArrayList<>();
		ItemDao.iterator().forEachRemaining(p -> result.add(p.toString()));
		return result.toString();
	}

	private static Object returnRecommendationForUser(String userId, final DatabaseAccess db) {
		try {
			List<Integer> allItemIdUserRate = new ArrayList<>();

			QueryBuilder<RatingFullData, String> queryBuilder = db.getRatingDao().queryBuilder();
			final Where<RatingFullData, String> where = queryBuilder.where();
			final SelectArg selectArg = new SelectArg();
			where.eq("userId", selectArg);
			PreparedQuery<RatingFullData> preparedQuery = queryBuilder.prepare();
			selectArg.setValue(userId);

			final List<RatingFullData> allItemUserRate = db.getRatingDao().query(preparedQuery);
			if(!allItemIdUserRate.isEmpty()) {
				allItemIdUserRate = allItemUserRate.stream().map(p->p.getId()).collect(Collectors.toList());
				final List<Item> allItem = db.getItemDao().queryForAll();
				final Map<Item, Double> result = new HashMap<>();
				for (Item n : allItem) {
					if (TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - n.getDate().getTime()) <= 24) {
						result.put(n, calculateRating(allItemUserRate, db.getSimilarityDao(), n));
					}
				}
				final Map<Item, Double> sortedResult = MapUtil.sortByValueDescending(result);
				final List<Item> finalResult = new ArrayList<>();
				for (final Item n : sortedResult.keySet()) {
					if(!allItemIdUserRate.contains(n.getItemId())){
						finalResult.add(n);
					}
				}
				return finalResult;
			}else {
				QueryBuilder<ItemView, String> queryBuilder2 = db.getItemViewDao().queryBuilder();
				queryBuilder2.offset(0L).limit(5L).orderBy("view", false);
				PreparedQuery<ItemView> preparedQuery2 = queryBuilder2.prepare();
				final List<ItemView> popularItems = db.getItemViewDao().query(preparedQuery2);
				return popularItems.stream().map(p->p.getItem()).collect(Collectors.toList());
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return "[]";
	}

	private static double calculateRating(List<RatingFullData> allRatingsUserHas, Dao<Similarity, String> similarityDao,
			Item n) {
		try {
			double nom = 0;
			double denom = 0;
			for (RatingFullData r : allRatingsUserHas) {
				final Item Item = r.getItem();
				double sim = 0;
				if(Item.getItemId()==n.getItemId()){
					continue;
				}
				if (Item.getItemId() < n.getItemId()) {
					sim = similarityDao.queryBuilder().where().eq("item1_id", Item.getItemId()).and()
							.eq("item2_id", n.getItemId()).query().get(0).getValue();
				} else {
					sim = similarityDao.queryBuilder().where().eq("item1_id", n.getItemId()).and()
							.eq("item2_id", Item.getItemId()).query().get(0).getValue();
				}
				nom += r.getRating() * sim;
				denom += sim;
			}
			final double res = nom / denom;
			if (Double.isNaN(res)) {
				return 0;
			}
			return res;
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void startApis(final DatabaseAccess db) {
		get("/recom/:userId", (req, res) -> returnRecommendationForUser(req.params(":userId"), db));

		get("/item", (req, res) -> returnAllItem(db.getItemDao()));

		post("/rating", (req, res) -> {
			final JSONParser parser = new JSONParser();
			try {
				final Object obj = parser.parse(req.body());
				final JSONObject jsonObject = (JSONObject) obj;
				final int userId = Integer.parseInt((String) jsonObject.get("userId"));
				final int itemId = Integer.parseInt((String) jsonObject.get("itemId"));
				final boolean fake = Boolean.parseBoolean((String) jsonObject.get("fake"));
				final int ratingNumber = Integer.parseInt((String) jsonObject.get("rating"));
				final String personality = (String)jsonObject.get("personality");
				
				Item Item = null;
				for (final CloseableIterator<Item> iterator = db.getItemDao().iterator(); iterator.hasNext();) {
					final Item localItem = (Item) iterator.next();
					if (localItem.getItemId() == itemId) {
						Item = localItem;
						break;
					}
				}


				final RatingFullData rating = new RatingFullData();
				rating.setPersonality(personality);
				rating.setFake(fake);
				rating.setUserId(userId);
				rating.setItem(Item);
				rating.setRating(ratingNumber);

				try {
					db.getRatingDao().create(rating);
				} catch (Exception e) {
					for (final CloseableIterator<RatingFullData> iterator = db.getRatingDao().iterator(); iterator
							.hasNext();) {
						final RatingFullData localRating = (RatingFullData) iterator.next();
						if (localRating.getUserId() == userId && localRating.getItem().getItemId() == Item.getItemId()) {
							rating.setId(localRating.getId());
							break;
						}
					}
					db.getRatingDao().update(rating);
				}
				res.status(201);
				return "Rating added";
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return "error";
		});

		post("/item", (req, res) -> {

			final JSONParser parser = new JSONParser();
			try {
				final Object obj = parser.parse(req.body());
				final JSONObject jsonObject = (JSONObject) obj;
				final String id = (String) jsonObject.get("id");
				final String body = (String) jsonObject.get("body");
				final String date = (String) jsonObject.get("date");
				final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
				final Item item = new Item();
				item.setContent(body);
				item.setDate(new Timestamp(formatter.parse(date).getTime()));
				item.setItemId(Integer.parseInt(id));
				db.getItemDao().create(item);
				res.status(201);
				return "Item added";
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return "error";
		});
		
		post("/view", (req, res) -> {

			final JSONParser parser = new JSONParser();
			try {
				final Object obj = parser.parse(req.body());
				final JSONObject jsonObject = (JSONObject) obj;
				final String itemId = (String) jsonObject.get("itemid");
				
				
				final QueryBuilder<ItemView, String> queryBuilder = db.getItemViewDao().queryBuilder();
				final Where<ItemView, String> where = queryBuilder.where();
				final SelectArg selectArg = new SelectArg();
				where.eq("item_id", selectArg);
				final PreparedQuery<ItemView> preparedQuery = queryBuilder.prepare();
				selectArg.setValue(itemId);

				final List<ItemView> itemView = db.getItemViewDao().query(preparedQuery);
				
				if(!itemView.isEmpty()) {
					final ItemView retrivedItemView = itemView.get(0);
					retrivedItemView.increaseView();
					db.getItemViewDao().update(retrivedItemView);
					res.status(204);
					return "ItemView updated";
				}else {
					final Item retrivedItem = db.getItemDao().queryForId(itemId);
					if(retrivedItem==null) {
						res.status(404);
						return "item with id "+itemId+" does not exist";
					}else {
						final ItemView itemViewLocal = new ItemView();
						itemViewLocal.setItem(retrivedItem);
						itemViewLocal.increaseView();
						db.getItemViewDao().create(itemViewLocal);
						res.status(201);
						return "ItemView added";
					}
				}
				
				
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return "error";
		});
	}

}
