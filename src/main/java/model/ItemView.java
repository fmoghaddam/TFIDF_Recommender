package model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
 
@DatabaseTable(tableName = "itemview")
public class ItemView {
    
    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField(uniqueCombo = true , canBeNull = false, foreign = true)
    private Item item;
    
    @DatabaseField
    private long view;
    
    public ItemView() {
        // ORMLite needs a no-arg constructor 
    }

	public Item getItem() {
		return item;
	}

	public void increaseView() {
		view++;
	}
	
	public long getView() {
		return view;
	}

	public void setView(long view) {
		this.view = view;
	}

	public void setItem(Item retrivedItem) {
		item = retrivedItem;
	}
}
