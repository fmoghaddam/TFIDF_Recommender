package main;

import java.sql.SQLException;

import model.Runner;

public class Main {
	public static void main(String[] args) throws SQLException {
		final Runner run = new Runner();
		run.execute();
	}
}