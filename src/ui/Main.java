package ui;

import dao.JdbcService;
import service.JvmArgumentConfig;

public class Main {
	
	public static void main(String[] args) {
		MainWindow main = new MainWindow();
		main.setJdbcProvider(new JdbcService(new JvmArgumentConfig()));
		main.run();
	}

}
