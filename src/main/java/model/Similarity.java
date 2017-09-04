package model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
 
@DatabaseTable(tableName = "similarity")
public class Similarity {
    
    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField(uniqueCombo = true , canBeNull = false, foreign = true)
    private Item item1;
    
    @DatabaseField(uniqueCombo = true , canBeNull = false, foreign = true)
    private Item item2;
    
    @DatabaseField
    private double value;
    
    public Similarity() {
        // ORMLite needs a no-arg constructor 
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Item getItem1() {
		return item1;
	}

	public void setItem1(Item item1) {
		this.item1 = item1;
	}

	public Item getItem2() {
		return item2;
	}

	public void setItem2(Item item2) {
		this.item2 = item2;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Similarity [id=" + id + ", news1=" + item1 + ", news2=" + item2 + ", value=" + value + "]";
	}
		
}
