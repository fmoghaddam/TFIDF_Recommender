package main;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
 
@DatabaseTable(tableName = "news")
public class News {
    
    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField
    private String body;
    
    @DatabaseField
    private Date date;
    
    public News() {
        // ORMLite needs a no-arg constructor 
    }

	public int getId() {
		return id;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "News [id=" + id + ", body=" + body + ", date=" + date + "]";
	}
	
}
