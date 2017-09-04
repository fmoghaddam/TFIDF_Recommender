package model;

import java.sql.Timestamp;
import java.util.Date;

import org.json.simple.JSONObject;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
 
@DatabaseTable(tableName = "item")
public class Item {
    
    @DatabaseField(id = true)
    private int itemId;
    
    @DatabaseField
    private String content;
    
    @DatabaseField
    private Timestamp date;
    
    public Item() {
        // ORMLite needs a no-arg constructor 
    }

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}
	
	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public JSONObject toJson() {
		final JSONObject obj = new JSONObject();
        obj.put("id", itemId);        
        return obj;
	}

	@Override
	public String toString() {
		return toJson().toJSONString();
	}
	
	
}
