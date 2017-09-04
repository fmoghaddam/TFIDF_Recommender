package model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
 
@DatabaseTable(tableName = "rating")
public class RatingFullData {
    
    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField(uniqueCombo = true)
    private int userId;
    
    @DatabaseField(uniqueCombo = true , canBeNull = false, foreign = true)
    private Item Item;
    
    @DatabaseField
    private int rating;
    
    @DatabaseField
    private boolean fake;
    
    @DatabaseField
    private String personality;
    
    public RatingFullData() {
        // ORMLite needs a no-arg constructor 
    }

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Item getItem() {
		return Item;
	}

	public void setItem(Item Item) {
		this.Item = Item;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public boolean isFake() {
		return fake;
	}

	public void setFake(boolean fake) {
		this.fake = fake;
	}

	public String getPersonality() {
		return personality;
	}

	public void setPersonality(String personality) {
		this.personality = personality;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "RatingFullData [id=" + id + ", userId=" + userId + ", Item=" + Item + ", rating=" + rating + ", fake="
				+ fake + ", personality=" + personality + "]";
	}
}
