package main;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
 
@DatabaseTable(tableName = "similarity")
public class Similarity {
    
    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField(uniqueCombo = true , canBeNull = false, foreign = true)
    private News news1;
    
    @DatabaseField(uniqueCombo = true , canBeNull = false, foreign = true)
    private News news2;
    
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

	public News getNews1() {
		return news1;
	}

	public void setNews1(News news1) {
		this.news1 = news1;
	}

	public News getNews2() {
		return news2;
	}

	public void setNews2(News news2) {
		this.news2 = news2;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Similarity [id=" + id + ", news1=" + news1 + ", news2=" + news2 + ", value=" + value + "]";
	}
		
}
