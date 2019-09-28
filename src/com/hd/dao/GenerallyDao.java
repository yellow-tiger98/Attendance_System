package com.hd.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hd.database.JdbcUtils;

public class GenerallyDao {
	// 登录方法
		public void clientLogin(OutputStream os, String accountInfo) {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					// 连接数据库
					Connection conn = JdbcUtils.getConnection();
					// 创建预编译器
					PreparedStatement pstmt = null;
					// 创建结果集
					ResultSet rs = null;

					// 使用字符串分割获得登录用户的身份、账号、密码
					String[] arr1 = accountInfo.split("]");
					String[] arr2 = arr1[0].split("\\[");
					String[] arr3 = arr1[1].split(",");
					// 用户身份
					String capacity = arr2[1];
					// 用户账号
					String account = arr3[0];
					// 用户密码
					String pwd = arr3[1];
					// 两条sql语句对应不同身份用户执行登录操作
					String[] sqls = { "select * from employee where employeeAccount=? and employeePwd=?",
							"select * from admin where adminId=? and adminPwd=?" };

					// 普通员工执行sqls[0]
					if (capacity.equals("普通员工")) {
						try {
							// 实例预编译器
							pstmt = conn.prepareStatement(sqls[0]);
							// 通配符赋值
							pstmt.setString(1, account);
							pstmt.setString(2, pwd);
							// 获取结果集
							rs = pstmt.executeQuery();
							// 如果有结果，代表账号密码正确
							if (rs.next()) {
								// 告诉客户端其登录成功
								os.write("登录成功".getBytes());
							} else {
								// 告诉客户端其登录失败
								os.write("登录失败".getBytes());
							}
						} catch (SQLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							JdbcUtils.closeResultSet(rs);
							JdbcUtils.closeConnection(conn);
						}

					} else {
						try {
							// 实例预编译器
							pstmt = conn.prepareStatement(sqls[1]);
							// 通配符赋值
							pstmt.setString(1, account);
							pstmt.setString(2, pwd);
							// 获取结果集
							rs = pstmt.executeQuery();
							// 如果有结果，代表账号密码正确
							if (rs.next()) {
								// 告诉客户端其登录成功
								os.write("登录成功".getBytes());
							} else {
								// 告诉客户端其登录失败
								os.write("登录失败".getBytes());
							}
						} catch (SQLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							JdbcUtils.closeResultSet(rs);
							JdbcUtils.closeConnection(conn);
						}
					}
				}
			};
			new Thread(task).start();

		}
}
