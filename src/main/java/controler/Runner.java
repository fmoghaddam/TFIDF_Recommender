package controler;

import java.sql.SQLException;

public class Runner {

	public void execute() throws SQLException {
		final DatabaseAccess db = new DatabaseAccess();
		new RecommenderService(db); 
		new ApiHandler(db);
	}
}
