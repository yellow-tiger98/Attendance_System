package com.hd.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.hd.database.JdbcUtils;

public class EmlpoyeeDao {
	//打卡
	public void clientWorkUp(OutputStream os, String e_account) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				// 首先判断今天是不是休息
				if (isRest() == true) {
					try {
						// 是休息日则告诉客户端是休息日，不用进行打卡
						os.write("休息日".getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Connection conn = JdbcUtils.getConnection();
					PreparedStatement pstmt = null;
					ResultSet rs = null;
					String employeeId = null;
					String status;
					// 先获取普通员工的id
					String sql = "select employeeId from employee where employeeAccount=?";
					try {
						// 第一次编译:用于获取员工ID
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, e_account);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							// 获取普通员工的唯一ID
							employeeId = rs.getString("employeeId");
							Date date = new Date();
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
							// 获取当前日期
							String dateNow = dateFormat.format(date);
							// 查看有没有打过上班卡或者下班卡，打了任何一个卡都不能再打卡
							String sql2 = "select workUp from clock where employeeId=? and dayDate=?";
							// 第二次编译，用于判断是否打过卡
							pstmt = conn.prepareStatement(sql2);
							pstmt.setString(1, employeeId);
							pstmt.setString(2, dateNow);
							rs = pstmt.executeQuery();
							// 有结果就是打过卡了
							if (rs.next()) {
								// 这里对结果做一个判断，如果结果不为空，说明是正常的上班打卡，如果为空，说明用户没有进行上班打卡，只进行了下班打卡
								if (rs.getString("workUp") != null) {
									String info = "已经打过卡，打卡时间为:" + rs.getString("workUp");
									os.write(info.getBytes()); // 告诉客户端已经打过卡了
								} else {
									System.out.println(359);
									String info = "你已经进行下班打卡，今日不能再进行上班打卡！";
									os.write(info.getBytes()); // 告诉客户端已经打过卡了
								}
							} else {// 没打卡,进行打卡
								dateFormat = new SimpleDateFormat("HH:mm");
								// 获取当前打卡时间
								String workUpTime = dateFormat.format(date);
								// 正常打卡最迟时间
								String normalTime = "09:00";
								// 迟到打卡最迟时间
								String latestTime = "11:00";
								// 获取打卡状态
								status = workUpStatus(workUpTime, normalTime, latestTime);
								if (status.equals("过早")) {
									// 告诉客户端打卡太早了，不能打
									os.write("过早".getBytes());
								} else {
									String sql3 = "insert into clock(employeeId,dayDate,workUp,workUpStatus) values"
											+ "('" + employeeId + "','" + dateNow + "','" + workUpTime + "','" + status
											+ "')";
									pstmt = conn.prepareStatement(sql3);
									int ret = pstmt.executeUpdate();
									if (ret > 0) {
										// 打卡信息
										String clockInfo = "打卡成功,状态为:" + status + ",打卡时间为:" + workUpTime;
										// 告诉客户端打卡成功
										os.write(clockInfo.getBytes());
									} else {
										// 告诉客户端打卡失败
										os.write("打卡失败".getBytes());
									}
								}
							}

						} else {
							os.write("打卡失败".getBytes());
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
	
	// 下班打卡方法
		public void clientWorkDown(OutputStream os, String e_account) {
			Runnable task = new Runnable() {
				public void run() {
					Connection conn = JdbcUtils.getConnection();
					PreparedStatement pstmt = null;
					ResultSet rs = null;
					String employeeId = null;
					// 打卡状态
					String status;
					// 打卡时间
					String workDownTime;
					// 正常下班时间
					String normalTime = "18:00";
					// 早退旷工分界时间点
					String notWorkTime = "16:00";
					Date date = new Date();
					// 日期格式
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					// 打卡时间格式
					SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm");
					// 获取当前日期
					String dateNow = dateFormat.format(date);
					// 获取当前时间
					workDownTime = dateFormat2.format(date);
					// 首先判断早上有没有打卡，如果没有，则需要插入日期,下班打卡时间,下班打卡状态
					String sql1 = "select * from clock where "
							+ "employeeId=(select employeeId from employee where employeeAccount=?)" + "and dayDate=?";
					try {
						if (isRest() == true) {
							os.write("今天是休息日！".getBytes());
						} else {
							pstmt = conn.prepareStatement(sql1);
							pstmt.setString(1, e_account);
							pstmt.setString(2, dateNow);
							rs = pstmt.executeQuery();
							// 如果有结果，则证明早上有打卡，再判断有没有打过下班卡，然后更新打卡记录
							if (rs.next()) {
								String sql2 = "select workDown,workDownStatus from clock where "
										+ "employeeId=(select employeeId from employee where employeeAccount=?)"
										+ "and dayDate=?";
								pstmt = conn.prepareStatement(sql2);
								pstmt.setString(1, e_account);
								pstmt.setString(2, dateNow);
								rs = pstmt.executeQuery();
								// 如果有结果表示已经进行过下班打卡
								if (rs.next() && rs.getString("workDown") != null) {
									String info = "已经打过下班卡,打卡时间为:" + rs.getString("workDown")+",状态为:"+rs.getString("workDownStatus");
									// 告诉客户端用户已经打过卡了
									os.write(info.getBytes());
								} else { // 还没打下班卡
									// 获取打卡状态
									status = workDownStatus(workDownTime, normalTime, notWorkTime);
									// 更新打卡记录，添加下班打卡时间和状态
									String sql3 = "update clock set workDown=?,workDownStatus=? "
											+ "where employeeId=(select employeeId from employee where employeeAccount=?)"
											+ "and dayDate=?";
									pstmt = conn.prepareStatement(sql3);
									pstmt.setString(1, workDownTime);
									pstmt.setString(2, status);
									pstmt.setString(3, e_account);
									pstmt.setString(4, dateNow);
									int ret = pstmt.executeUpdate();
									if (ret > 0) {
										String downInfo = "下班打卡成功,打卡时间为:" + workDownTime;
										os.write(downInfo.getBytes());
									} else {
										os.write("打卡失败".getBytes());
									}
								}
							} else {
								if ((workDownTime.compareTo("11:00")) >= 0) {
									// 早上没有打卡，需要插入打卡记录
									status = workDownStatus(workDownTime, normalTime, notWorkTime);
									// 先获得员工id
									String sql4 = "select employeeId from employee where employeeAccount=?";
									String e_id = null;
									pstmt = conn.prepareStatement(sql4);
									pstmt.setString(1, e_account);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										e_id = rs.getString("employeeId");
									}
									String sql5 = "insert into clock(employeeId,dayDate,workDown,workDownStatus) values(?,?,?,?)";
									pstmt = conn.prepareStatement(sql5);
									pstmt.setString(1, e_id);
									pstmt.setString(2, dateNow);
									pstmt.setString(3, workDownTime);
									pstmt.setString(4, status);
									int ret = pstmt.executeUpdate();
									if (ret > 0) {
										String downInfo = "下班打卡成功,打卡时间为:" + workDownTime;
										os.write(downInfo.getBytes());
									} else {
										os.write("打卡失败".getBytes());
									}
								} else {

									os.write("十一点前未进行上班打卡，请先进行上班打卡！".getBytes());
								}
							}
						}
					} catch (SQLException | IOException e) {
						e.printStackTrace();
					} finally {
						// 关闭数据库资源
						JdbcUtils.closeResultSet(rs);
						JdbcUtils.closeConnection(conn);
					}

				}
			};
			new Thread(task).start();
		}
		
		// 查看本人考勤信息方法
		public void viewMyInfo(OutputStream os, String acceptInfo) {
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
					String e_account = arr[0];
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
						String idSql = "select employeeId from employee where employeeAccount=?";
						try {
							pstmt = conn.prepareStatement(idSql);
							pstmt.setString(1, e_account);
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
								String allMyInfo = "查询成功！出勤:"+clockDay+"天 {迟到:"+lateCount+" "
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
						}finally {
							JdbcUtils.closeResultSet(rs);
							JdbcUtils.closeConnection(conn);
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
								String idSql = "select employeeId from employee where employeeAccount=?";
									pstmt = conn.prepareStatement(idSql);
									pstmt.setString(1, e_account);
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
											os.write("查询失败，该员工不存在！".getBytes());
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
//										System.out.println(allInfo.length());
										String msg = allMyInfo+allInfo;
										
										os.write(msg.getBytes());
										
									}else {
										os.write("查询失败，获取用户信息时出错".getBytes());
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
	
		// 用于判断上班打卡状态 9:00
		public String workUpStatus(String workUpTime, String normalTime, String latestTime) {
			String status = null;
			// 进行打卡时间的对比，
			int res = workUpTime.compareTo(normalTime);
			// 如果等于指定打卡时间或则前两小时
			if (res == 0 || (res > -2 && res < 0)) {
				status = "正常";
			} else if (res < -2) { // 七点前打卡太早//拒绝
				status = "过早";
			} else if (res > 0) {
				res = workUpTime.compareTo(latestTime);
				if (res <= 0) {
					status = "迟到";
				} else {
					status = "旷工";
				}
			}
			return status;
		}
		
		// 用于判断下班打卡状态
		public String workDownStatus(String workDownTime, String normalTime, String notWorkTime) {
			String status = null;
			// 进行打卡时间的对比，
			int res = workDownTime.compareTo(normalTime);
			// 如果下班打卡时间大于或等于正常下班时间，都视为正常打卡
			if (res == 0 || res > 0) {
				status = "正常";
			}
			// 如果小于0，则证明员工有早退或旷工嫌疑，进行判断
			else if (res < 0) {
				// 和早退旷工分界点进行对比
				res = workDownTime.compareTo(notWorkTime);
				// 如果大于则为早退
				if (res > 0) {
					status = "早退";
				} else {// 如果小于则为旷工
					status = "旷工";
				}
			}
			return status;
		}
	
		// 判断是不是休息日
		public boolean isRest() {
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String currDate = dateFormat.format(date);
			Connection conn = JdbcUtils.getConnection();
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String sql = "select * from worksheet where dayDate=?";
			try {
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, currDate);
				rs = pstmt.executeQuery();
				if (rs.next()) { // 有代表不是休息日
					return false;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				JdbcUtils.closeResultSet(rs);
				JdbcUtils.closeConnection(conn);
			}
			return true;
		}
}
