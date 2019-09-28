package com.hd.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcUtils {
	// 连接格式：[jdbc：mysql：//ip地址：端口号/数据库名称？选项1&选项2...] useSSL=false 关闭SSL连接警告
	private static String url = "jdbc:mysql://localhost:3306/attendance_system?"
			+ "useUnicode=true&characterEncoding=utf-8&useSSL=false";
	// mysql数据库用户名
	private static String user = "root";
	// mysql数据库密码
	private static String password = "220352";

	private static boolean loadStatus = false;

	// 静态代码块，在类被调用时就自动执行
	static {
		// 【1】加载MySQL提供的驱动类(通过反射的形式)
		// 【2】利用驱动管理器来读取驱动的相关信息以及利用驱动管理器来管理JVM中的驱动
		try {
			Class.forName("com.mysql.jdbc.Driver");
			loadStatus = true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// 用于建立连接
	public static Connection getConnection() {
		Connection conn = null;
		if (loadStatus == true) {
			try {
				conn = DriverManager.getConnection(url, user, password);
				System.out.println("[YTG服务器]数据库连接建立成功");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return conn;
	}

	// 用于关闭连接
	public static void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 用于关闭结果集连接
	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
