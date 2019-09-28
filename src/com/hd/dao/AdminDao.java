package com.hd.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hd.database.JdbcUtils;

public class AdminDao {
	// 客户端执行添加员工
		public void addEmployee(OutputStream os, String e_info) {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					// 与数据库建立连接
					Connection conn = JdbcUtils.getConnection();
					// 创建预编译执行器
					PreparedStatement pstmt = null;
					// 创建结果集
					ResultSet rs = null;
					// 将各字段值存入Map集合中
					HashMap<String, String> map = spiltEmplyeeInfo(e_info);
					System.out.println(map.get(" employeeAccount"));

					// 首先查询id是不重复，登录账号是否重复
					String idSelectSql = "select * from employee where employeeId=?";
					try {
						pstmt = conn.prepareStatement(idSelectSql);
						pstmt.setString(1, map.get("employeeId"));
						rs = pstmt.executeQuery();
						// 有查询结果
						if (rs.next()) {
							String info = "该员工id已经被使用，请更换员工id!";
							os.write(info.getBytes());
						} else { // 没有查询结果，代表该id没被使用
							// 接着判断员工账号是否被使用
							String accounSelecttSql = "select * from employee where employeeAccount=?";
							pstmt = conn.prepareStatement(accounSelecttSql);
							pstmt.setString(1, map.get(" employeeAccount"));
							rs = pstmt.executeQuery();
							if (rs.next()) {
								String info = "该员工账号已经被使用，请更改员工账号!";
								os.write(info.getBytes());
							} else {
								// 添加普通员工
								if (map.get(" capacity").equals("普通员工")) {
									String insertSql = "insert into employee values(?,?,?,?,?,?,?,?)";
									pstmt = conn.prepareStatement(insertSql);
									pstmt.setString(1, map.get("employeeId"));
									pstmt.setString(2, map.get(" employeeName"));
									pstmt.setString(3, map.get(" employeeAccount"));
									pstmt.setString(4, map.get(" employeePwd"));
									pstmt.setString(5, map.get(" position"));
									pstmt.setString(6, map.get(" salary"));
									pstmt.setString(7, map.get(" workDate"));
									pstmt.setString(8, map.get(" capacity"));
									int ret = pstmt.executeUpdate();
									if (ret > 0) {
										String addInfo = "添加员工成功！";
										os.write(addInfo.getBytes());
									} else {
										os.write("添加失败".getBytes());
									}
								} else {// 添加管理员，则需要向员工表和管理员表中都添加信息
									String employeeSql = "insert into employee values(?,?,?,?,?,?,?,?)";
									pstmt = conn.prepareStatement(employeeSql);
									pstmt.setString(1, map.get("employeeId"));
									pstmt.setString(2, map.get(" employeeName"));
									pstmt.setString(3, map.get(" employeeAccount"));
									pstmt.setString(4, map.get(" employeePwd"));
									pstmt.setString(5, map.get(" position"));
									pstmt.setString(6, map.get(" salary"));
									pstmt.setString(7, map.get(" workDate"));
									pstmt.setString(8, map.get(" capacity"));
									int ret1 = pstmt.executeUpdate();
									if (ret1 > 0) {
										System.out.println("添加员工表成功");
										String adminSql = "insert into admin values(?,?,?)";
										pstmt = conn.prepareStatement(adminSql);
										pstmt.setString(1, map.get("employeeId"));
										pstmt.setString(2, map.get(" employeeName"));
										pstmt.setString(3, map.get(" employeePwd"));
										int ret2 = pstmt.executeUpdate();
										if (ret2 > 0) {
											String addInfo = "添加管理员成功！";
											os.write(addInfo.getBytes());
										} else {
											os.write("添加管理员失败！".getBytes());
										}
									} else {
										os.write("添加员工失败".getBytes());
									}
								}

							}
						}

					} catch (SQLException e) {
						String errorInfo = "数据格式有错误，错误信息：" + e.getMessage();
						try {
							os.write(errorInfo.getBytes());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						JdbcUtils.closeResultSet(rs);
						JdbcUtils.closeConnection(conn);
					}

				}
			};
			new Thread(task).start();

		}
		
		// 客户端执行查看所有员工考勤统计
		public void viewAllInfo(OutputStream os,String acceptInfo) {
			Runnable task = new  Runnable() {
				public void run() {
					//与数据库建立连接
					Connection conn = JdbcUtils.getConnection();
					//创建预编译执行器
					PreparedStatement pstmt = null;
					//创建结果集
					ResultSet rs = null;
					//先将拿到的信息进行处理（其格式为账号,查询月份）
					String e_id = null;
					//出勤天数
					String clockDay = null;
					//迟到次数
					String lateCount = null;
					//早退次数
					String earlyOutCount = null;
					//旷工次数
					String notWorkCount = null;
					String[] arr = acceptInfo.split(",");
					String e_name = arr[0];
					String selecCond = arr[1];
					Date date = new Date();
					// 日期格式
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					//获取当前日期
					String dateNow = dateFormat.format(date);
					String _dateNow = dateNow.substring(0, 7);
					//首先判断查询的月份是否为当前月份，是则查询到今天的记录，不是则查一整个月的
					/*
					 * 三种情况，【1】查询月份为当前月 ：查当前月开始至今天为止
					 * 		     【2】查询月份大于当前月：返回错误信息
					 * 		     【3】查询月份小于当前月：查询整月信息
					 */
					
					//为当前月
					if(selecCond.equals(_dateNow)) {
						//首先查一下员工的ID
						String idSql = "select employeeId from employee where employeeName=?";
						try {
							pstmt = conn.prepareStatement(idSql);
							pstmt.setString(1, e_name);
							rs = pstmt.executeQuery();
							if(rs.next()) {
								e_id = rs.getString("employeeId");
								//获得id后开始进行查询
								//{1}首先查询迟到人数
								String lateSql = "select count(workUpStatus) late "
										+ "from (select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus "
										+ "from (select  * from worksheet   where dayDate like ? and  "
										+ "dayDate between '' and ?) wk  "
										+ "left join (select * from clock where employeeId=?) ck  on wk.dayDate = ck.dayDate) dk  "
										+ "where workUpStatus in ('迟到') and workDownStatus in('正常','早退')";
								pstmt = conn.prepareStatement(lateSql);
								pstmt.setString(1, _dateNow+"%");
								pstmt.setString(2, dateNow);
								pstmt.setString(3, e_id);
								rs = pstmt.executeQuery();
								if(rs.next()) {
									//获得迟到次数
									lateCount = rs.getString("late");
								}else {
									os.write("查询失败，获取用户信息时出错".getBytes());
								}
								//{2}查询早退次数
								String earlySql = "select count(workDownStatus) early from "
										+ "(select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus from "
										+ "(select  * from worksheet   where dayDate like ? and dayDate "
										+ "between '' and ?) wk  "
										+ "left join (select * from clock where employeeId=?) ck  on wk.dayDate = ck.dayDate) dk  "
										+ "where workDownStatus in ('早退') and workUpStatus in('正常','迟到')";
								pstmt = conn.prepareStatement(earlySql);
								pstmt.setString(1, _dateNow+"%");
								pstmt.setString(2, dateNow);
								pstmt.setString(3, e_id);
								rs = pstmt.executeQuery();
								if(rs.next()) {
									//获得早退次数
									earlyOutCount = rs.getString("early");
								}else {
									os.write("查询失败，获取用户信息时出错".getBytes());
								}
								//{3}查询旷工次数
								String notWorlSql = "select count(ek.dayDate) ow "
										+ "from (select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus "
										+ "from (select  * from worksheet   where dayDate like ? "
										+ "and dayDate between '' and ?) wk  "
										+ "left join (select * from clock where employeeId=?) ck  "
										+ "on wk.dayDate = ck.dayDate order by wk.dayDate) ek where ek.employeeId is null "
										+ "or (ek.workUpStatus is null or ek.workUpStatus='旷工') "
										+ "or (ek.workDownStatus is null or ek.workDownStatus='旷工') "
										+ "or (ek.workUpStatus is null and ek.workDownStatus='旷工') "
										+ "or (ek.workUpStatus ='旷工' and ek.workDownStatus is null)";
														
								pstmt = conn.prepareStatement(notWorlSql);
								pstmt.setString(1, _dateNow+"%");
								pstmt.setString(2, dateNow);
								pstmt.setString(3, e_id);
								rs = pstmt.executeQuery();
								if(rs.next()) {
									//获得旷工次数
									notWorkCount = rs.getString("ow");
								}else {
									os.write("查询失败，获取用户信息时出错".getBytes());
								}
								//{4}查询出勤次数
								String clockDaySql = "select count(ek.dayDate) ow from "
										+ "(select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus "
										+ "from (select  * from worksheet   "
										+ "where dayDate like ? and dayDate between '' and ?) wk  "
										+ "left join (select * from clock where employeeId=?) ck  "
										+ "on wk.dayDate = ck.dayDate order by wk.dayDate) ek "
										+ "where  ek.employeeId is not null;";
														
								pstmt = conn.prepareStatement(clockDaySql);
								pstmt.setString(1, _dateNow+"%");
								pstmt.setString(2, dateNow);
								pstmt.setString(3, e_id);
								rs = pstmt.executeQuery();
								if(rs.next()) {
									//获得早退次数
									clockDay = rs.getString("ow");
								}else {
									os.write("查询失败，获取用户信息时出错".getBytes());
								}
								String allMyInfo = "查询成功！"
										+ "["+e_name+"]出勤:"+clockDay+"天 {迟到:"+lateCount+" "
														+ "早退:"+earlyOutCount+" "
														+ "旷工:"+notWorkCount+"}";
								//查询所有的信息
								String allSql = "select id,wk.dayDate,employeeId,workUp,"
										+ "workUpStatus,workDown,workDownStatus from (select  * from worksheet   "
										+ "where dayDate like ? and dayDate between '' and ?) wk  "
										+ "left join (select * from clock where employeeId=?) ck  "
										+ "on wk.dayDate = ck.dayDate order by wk.dayDate";
								pstmt = conn.prepareStatement(allSql);
								pstmt.setString(1, _dateNow+"%");
								pstmt.setString(2, dateNow);
								pstmt.setString(3, e_id);
								rs = pstmt.executeQuery();
								StringBuffer allInfo = new StringBuffer();
								int id = 1;
								while(rs.next()) {
									String dayDate = rs.getString("wk.dayDate");
									String employeeId = rs.getString("employeeId");
									String workUp = rs.getString("workUp");
									String workUpStatus = rs.getString("workUpStatus");
									String workDown = rs.getString("workDown");
									String workDownStatus = rs.getString("workDownStatus");
									String line = ","+id+"\t"+dayDate+"\t"+employeeId+"\t"+workUp+"\t"+workUpStatus
											+"\t"+workDown+"\t"+workDownStatus;
									id++;
									allInfo.append(line);
								}
								System.out.println(allInfo.length());
								String msg = allMyInfo+allInfo;
								
								os.write(msg.getBytes());
								
							}else {
								os.write("查询失败，该员工不存在！".getBytes());
							}
						} catch (SQLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						
					}
					//大于当前月
					else if((selecCond.compareTo(_dateNow))>0) {
						try {
							os.write("查询月份大于当前月，查询失败！".getBytes());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					//小于当前月
					else {
						//先看查询的月份是不是工作月份，是才执行查询，不是的话则提示不是
						String isWorkMonth = "select * from worksheet where dayDate like ?";
						try {
							pstmt = conn.prepareStatement(isWorkMonth);
							pstmt.setString(1, selecCond+"%");
							rs = pstmt.executeQuery();
							if(rs.next()) {
								//首先查一下员工的ID
								String idSql = "select employeeId from employee where employeeName=?";
									pstmt = conn.prepareStatement(idSql);
									pstmt.setString(1, e_name);
									rs = pstmt.executeQuery();
									if(rs.next()) {
										e_id = rs.getString("employeeId");
										//获得id后开始进行查询
										//{1}首先查询迟到人数
										String lateSql = "select count(workUpStatus) late "
												+ "from (select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus "
												+ "from (select  * from worksheet   where dayDate like ?"
												+ ") wk  "
												+ "left join (select * from clock where employeeId=?) ck  on wk.dayDate = ck.dayDate) dk  "
												+ "where workUpStatus in ('迟到') and workDownStatus in('正常','早退')";
										pstmt = conn.prepareStatement(lateSql);
										pstmt.setString(1, selecCond+"%");
										pstmt.setString(2, e_id);
										rs = pstmt.executeQuery();
										if(rs.next()) {
											//获得迟到次数
											lateCount = rs.getString("late");
										}else {
											os.write("查询失败，获取用户信息时出错".getBytes());
										}
										//{2}查询早退次数
										String earlySql = "select count(workDownStatus) early from "
												+ "(select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus from "
												+ "(select  * from worksheet   where dayDate like ?"
												+ ") wk  "
												+ "left join (select * from clock where employeeId=?) ck  on wk.dayDate = ck.dayDate) dk  "
												+ "where workDownStatus in ('早退') and workUpStatus in('正常','迟到')";
										pstmt = conn.prepareStatement(earlySql);
										pstmt.setString(1, selecCond+"%");
										pstmt.setString(2, e_id);
										rs = pstmt.executeQuery();
										if(rs.next()) {
											//获得早退次数
											earlyOutCount = rs.getString("early");
										}else {
											os.write("查询失败，获取用户信息时出错".getBytes());
										}
										//{3}查询旷工次数
										String notWorlSql = "select count(ek.dayDate) ow "
												+ "from (select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus "
												+ "from (select  * from worksheet   where dayDate like ? "
												+ ") wk  "
												+ "left join (select * from clock where employeeId=?) ck  "
												+ "on wk.dayDate = ck.dayDate order by wk.dayDate) ek where ek.employeeId is null "
												+ "or (ek.workUpStatus is null or ek.workUpStatus='旷工') "
												+ "or (ek.workDownStatus is null or ek.workDownStatus='旷工') "
												+ "or (ek.workUpStatus is null and ek.workDownStatus='旷工') "
												+ "or (ek.workUpStatus ='旷工' and ek.workDownStatus is null)";
																
										pstmt = conn.prepareStatement(notWorlSql);
										pstmt.setString(1, selecCond+"%");
										pstmt.setString(2, e_id);
										rs = pstmt.executeQuery();
										if(rs.next()) {
											//获得旷工次数
											notWorkCount = rs.getString("ow");
										}else {
											os.write("查询失败，获取用户信息时出错".getBytes());
										}
										//{4}查询出勤次数
										String clockDaySql = "select count(ek.dayDate) ow from "
												+ "(select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus "
												+ "from (select  * from worksheet   "
												+ "where dayDate like ?) wk  "
												+ "left join (select * from clock where employeeId=?) ck  "
												+ "on wk.dayDate = ck.dayDate order by wk.dayDate) ek "
												+ "where  ek.employeeId is not null;";
																
										pstmt = conn.prepareStatement(clockDaySql);
										pstmt.setString(1, selecCond+"%");
										pstmt.setString(2, e_id);
										rs = pstmt.executeQuery();
										if(rs.next()) {
											//获得早退次数
											clockDay = rs.getString("ow");
										}else {
											os.write("查询失败，获取用户信息时出错".getBytes());
										}
										String allMyInfo = "查询成功！出勤:"+clockDay+"天 {迟到:"+lateCount+" "
																+ "早退:"+earlyOutCount+" "
																+ "旷工:"+notWorkCount+"}";
										//查询所有的信息
										String allSql = "select id,wk.dayDate,employeeId,workUp,"
												+ "workUpStatus,workDown,workDownStatus from (select  * from worksheet   "
												+ "where dayDate like ? and dayDate) wk  "
												+ "left join (select * from clock where employeeId=?) ck  "
												+ "on wk.dayDate = ck.dayDate order by wk.dayDate";
										pstmt = conn.prepareStatement(allSql);
										pstmt.setString(1, selecCond+"%");
										pstmt.setString(2, e_id);
										rs = pstmt.executeQuery();
										StringBuffer allInfo = new StringBuffer();
										int id = 1;
										while(rs.next()) {
											String dayDate = rs.getString("wk.dayDate");
											String employeeId = rs.getString("employeeId");
											String workUp = rs.getString("workUp");
											String workUpStatus = rs.getString("workUpStatus");
											String workDown = rs.getString("workDown");
											String workDownStatus = rs.getString("workDownStatus");
											String line = ","+id+"\t"+dayDate+"\t"+employeeId+"\t"+workUp+"\t"+workUpStatus
															+"\t"+workDown+"\t"+workDownStatus;
											id++;
											allInfo.append(line);
										}
										System.out.println(allInfo.length());
										String msg = allMyInfo+allInfo;
										
										os.write(msg.getBytes());
										
									}else {
										os.write("查询失败，该员工不存在！".getBytes());
									}
							}else {
								os.write("查询月份不是工作月，查询失败！".getBytes());
							}
						} catch (SQLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					}
				}
			};
			new Thread(task).start();
		}
		
		public HashMap<String, String> spiltEmplyeeInfo(String e_info) {
			HashMap<String, String> map = new HashMap<String, String>();
			String str = null;
			// System.out.println(e_info);
			// 【1】先取出中括号内的内容
			Pattern pattern = Pattern.compile("(\\[[^\\]]*\\])");
			Matcher matcher = pattern.matcher(e_info);
			while (matcher.find()) {
				str = matcher.group().substring(1, matcher.group().length() - 1);
			}
			// System.out.println(str);
			// 【2】然后将中括号的内容按逗号分隔
			String[] str2 = str.split(",");
			// 以键值对形式存储
			for (String string : str2) {
				// 【3】然后按照“=”分隔
				String[] str3 = string.split("=");
				map.put(str3[0], str3[1]);
			}
			// System.out.println(map);
			return map;
		}
}
