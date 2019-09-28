package com.hd.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.HashMap;
import java.util.Map;

import com.hd.dao.AdminDao;
import com.hd.dao.EmlpoyeeDao;
import com.hd.dao.GenerallyDao;

import message.MsgCallBack;

public class NetWork {

	private MsgCallBack msgCallBack;

	// 用一个Map集合接收连接信息
	Map<String, Socket> socketMap = new HashMap<String, Socket>();
	GenerallyDao generallyDao = new GenerallyDao();
	EmlpoyeeDao emlpoyeeDao = new EmlpoyeeDao();
	AdminDao adminDao = new AdminDao();

	public NetWork(MsgCallBack msgCallBack) {
		this.msgCallBack = msgCallBack;
	}

	// 开启服务器
	public void statrServer(int port) {
		Runnable task = new Runnable() {
			public void run() {
				ServerSocket server = null;
				Socket socket = null;
				String clientIp;
				int clientPort;
				try {
					// 开启服务器
					server = new ServerSocket(port);
					// 返回开启成功的信息给调用者
					msgCallBack.onCreateServer(true);
					while (true) {
						socket = server.accept();
						clientIp = socket.getInetAddress().getHostAddress();
						clientPort = socket.getPort();
						// 回传消息，告诉调用者连入的客户端信息
						msgCallBack.onAccept(clientIp, clientPort);

						// 用来处理接入客户端传来信息的socket
						sproccessSocket(socket, clientIp, clientPort);

					}

				} catch (IOException e) {
					// System.out.println(e.getMessage());
					msgCallBack.onCreateServer(false);
				} finally {

				}
			}
		};
		new Thread(task).start();
	}

	// 客户端连接服务器 客户端与服务器建立连接后，开始发出指令给客户端，让其能够响应
	public void connectServer(String serverIp, int serverPort) {
		Runnable task = new Runnable() {
			public void run() {
				Socket socket = null;
				try {
					socket = new Socket(serverIp, serverPort);
					// 用于客户端的socket标记
					String SocketKey = cproccessSocket(socket, serverIp, serverPort);
					// 客户端用于发送指令
					msgCallBack.onConnecteServer(true, SocketKey);

				} catch (IOException e) {
					msgCallBack.onConnecteServer(false, null);
				}
			}
		};
		new Thread(task).start();
	}

	// 处理服务器端socket
	public String sproccessSocket(Socket socket, String ip, int port) {
		String socketKey = ip + ":" + port;
		socketMap.put(socketKey, socket);
		Runnable task = new Runnable() {

			@Override
			public void run() {
				read(socketKey);
			}

		};
		new Thread(task).start();
		return socketKey;
	}

	// 处理客户端端socket
	public String cproccessSocket(Socket socket, String ip, int port) {
		String socketKey = ip + ":" + port;
		socketMap.put(socketKey, socket);
		return socketKey;
	}

	// 服务端接收指令
	public void read(String socketKey) {
		Socket socket = socketMap.get(socketKey);
		OutputStream os = null;
		InputStream is = null;
		int count = 0;
		byte[] buff = new byte[1024];
		String content = null;
		try {
			is = socket.getInputStream();
			os = socket.getOutputStream();
			while (true) {
				count = is.read(buff);
				if (count > 0) {
					content = new String(buff, 0, count, "UTF-8");
					switch (content) {
					case "登录":
						print(socketKey+"执行登录操作");
						// 服务器给客户端一个响应，防止粘包
						os.write("登录".getBytes());
						// 接着读取账号信息
						count = is.read(buff);
						// 避免接收到-1
						if (count > 0) {
							// 获取字符串的账号信息
							String accountInfo = new String(buff, 0, count, "UTF-8");
							// 交给服务端的客户端登录方法处理
							generallyDao.clientLogin(os, accountInfo);
						} else { // 告诉客户端它登录失败
							os.write("登录失败...".getBytes());
						}

						break;
					case "上班打卡":
						print(socketKey+"执行上班打卡操作");
						// 服务器给客户端一个响应，防止粘包
						os.write("上班打卡".getBytes());
						// 接着读取上班打卡的账号名
						count = is.read(buff);
						// 避免接收到-1
						if (count > 0) {
							String e_account = new String(buff, 0, count, "UTF-8");
							// 执行上班打卡方法
							emlpoyeeDao.clientWorkUp(os, e_account);
						} else {
							os.write("打卡失败".getBytes());
						}
						break;
					case "下班打卡":
						print(socketKey+"执行下班打卡操作");
						// 服务器给客户端一个响应，防止粘包
						os.write("下班打卡".getBytes());
						// 接着读取上班打卡的账号名
						count = is.read(buff);
						// 避免接收到-1
						if (count > 0) {
							String e_account = new String(buff, 0, count, "UTF-8");
							// 执行下班打卡方法
							emlpoyeeDao.clientWorkDown(os, e_account);
						} else {
							os.write("打卡失败".getBytes());
						}
						break;
					case "查看我的考勤记录":
						print(socketKey+"执行查看考勤信息操作");
						// 服务器给客户端一个响应，表示接收到命令
						os.write("查看我的考勤记录".getBytes());
						// 接着读取账号以及查询条件
						count = is.read(buff);
						// 避免接收到-1
						if (count > 0) {
							String acceptInfo = new String(buff, 0, count, "UTF-8");
							// 执行查询方法
							emlpoyeeDao.viewMyInfo(os, acceptInfo);
						} else {
							os.write("查询失败".getBytes());
						}
						break;
					case "添加员工信息":
						print(socketKey+"执行添加员工操作");
						// 告诉客户端收到请求
						os.write("添加员工信息".getBytes());
						// 接着读取员工信息
						count = is.read(buff);
						// 避免接收到-1
						if (count > 0) {
							String e_info = new String(buff, 0, count, "UTF-8");
							adminDao.addEmployee(os, e_info);
						} else {
							os.write("添加失败".getBytes());
						}
						break;
					case "查看员工考勤统计":
						print(socketKey+"执行查看员工考勤统计操作");
						// 服务器给客户端一个响应，表示接收到命令
						os.write("查看员工考勤统计".getBytes());
						// 接着读取账号以及查询条件
						count = is.read(buff);
						// 避免接收到-1
						if (count > 0) {
							String acceptInfo = new String(buff, 0, count, "UTF-8");
							// 执行查询方法
							adminDao.viewAllInfo(os, acceptInfo);
						} else {
							os.write("查询失败".getBytes());
						}
						break;
					}
				}
			}
		} catch (IOException e) {
			print(socketKey + "断开连接，错误信息：" + e.getMessage());
		}

	}

	public void print(String str) {
		System.out.println("[YTG服务器]"+str);
		
	}

	// 客户端向服务器发送指令
	public void send(String str, String socketKey) {
		Socket socket = socketMap.get(socketKey);
		OutputStream os = null;
		try {
			os = socket.getOutputStream();
			// 客户端向服务器发送数据
			os.write(str.getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 客户端读取服务器返回信息
	public String clientRead(String socketKey) {
		Socket socket = socketMap.get(socketKey);
		InputStream is = null;
		int count;
		byte[] buff = new byte[1024];
		try {
			is = socket.getInputStream();
			count = is.read(buff);
			if (count > 0) {
				String response = new String(buff, 0, count);
				return response;
			} else {
				return "服务器无响应";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	
	
	
	
	
	
	
	
//	// 登录方法
//	public void clientLogin(OutputStream os, String accountInfo) {
//		Runnable task = new Runnable() {
//			@Override
//			public void run() {
//				// 连接数据库
//				Connection conn = JdbcUtils.getConnection();
//				// 创建预编译器
//				PreparedStatement pstmt = null;
//				// 创建结果集
//				ResultSet rs = null;
//
//				// 使用字符串分割获得登录用户的身份、账号、密码
//				String[] arr1 = accountInfo.split("]");
//				String[] arr2 = arr1[0].split("\\[");
//				String[] arr3 = arr1[1].split(",");
//				// 用户身份
//				String capacity = arr2[1];
//				// 用户账号
//				String account = arr3[0];
//				// 用户密码
//				String pwd = arr3[1];
//				// 两条sql语句对应不同身份用户执行登录操作
//				String[] sqls = { "select * from employee where employeeAccount=? and employeePwd=?",
//						"select * from admin where adminId=? and adminPwd=?" };
//
//				// 普通员工执行sqls[0]
//				if (capacity.equals("普通员工")) {
//					try {
//						// 实例预编译器
//						pstmt = conn.prepareStatement(sqls[0]);
//						// 通配符赋值
//						pstmt.setString(1, account);
//						pstmt.setString(2, pwd);
//						// 获取结果集
//						rs = pstmt.executeQuery();
//						// 如果有结果，代表账号密码正确
//						if (rs.next()) {
//							// 告诉客户端其登录成功
//							os.write("登录成功".getBytes());
//						} else {
//							// 告诉客户端其登录失败
//							os.write("登录失败".getBytes());
//						}
//					} catch (SQLException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					} finally {
//						JdbcUtils.closeResultSet(rs);
//						JdbcUtils.closeConnection(conn);
//					}
//
//				} else {
//					try {
//						// 实例预编译器
//						pstmt = conn.prepareStatement(sqls[1]);
//						// 通配符赋值
//						pstmt.setString(1, account);
//						pstmt.setString(2, pwd);
//						// 获取结果集
//						rs = pstmt.executeQuery();
//						// 如果有结果，代表账号密码正确
//						if (rs.next()) {
//							// 告诉客户端其登录成功
//							os.write("登录成功".getBytes());
//						} else {
//							// 告诉客户端其登录失败
//							os.write("登录失败".getBytes());
//						}
//					} catch (SQLException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					} finally {
//						JdbcUtils.closeResultSet(rs);
//						JdbcUtils.closeConnection(conn);
//					}
//				}
//			}
//		};
//		new Thread(task).start();
//
//	}
//
//	// 客户端执行上班打卡方法
//	public void clientWorkUp(OutputStream os, String e_account) {
//		Runnable task = new Runnable() {
//			@Override
//			public void run() {
//				// 首先判断今天是不是休息
//				if (isRest() == true) {
//					try {
//						// 是休息日则告诉客户端是休息日，不用进行打卡
//						os.write("休息日".getBytes());
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				} else {
//					Connection conn = JdbcUtils.getConnection();
//					PreparedStatement pstmt = null;
//					ResultSet rs = null;
//					String employeeId = null;
//					String status;
//					// 先获取普通员工的id
//					String sql = "select employeeId from employee where employeeAccount=?";
//					try {
//						// 第一次编译:用于获取员工ID
//						pstmt = conn.prepareStatement(sql);
//						pstmt.setString(1, e_account);
//						rs = pstmt.executeQuery();
//						if (rs.next()) {
//							// 获取普通员工的唯一ID
//							employeeId = rs.getString("employeeId");
//							Date date = new Date();
//							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//							// 获取当前日期
//							String dateNow = dateFormat.format(date);
//							// 查看有没有打过上班卡或者下班卡，打了任何一个卡都不能再打卡
//							String sql2 = "select workUp from clock where employeeId=? and dayDate=?";
//							// 第二次编译，用于判断是否打过卡
//							pstmt = conn.prepareStatement(sql2);
//							pstmt.setString(1, employeeId);
//							pstmt.setString(2, dateNow);
//							rs = pstmt.executeQuery();
//							// 有结果就是打过卡了
//							if (rs.next()) {
//								// 这里对结果做一个判断，如果结果不为空，说明是正常的上班打卡，如果为空，说明用户没有进行上班打卡，只进行了下班打卡
//								if (rs.getString("workUp") != null) {
//									String info = "已经打过卡，打卡时间为:" + rs.getString("workUp");
//									os.write(info.getBytes()); // 告诉客户端已经打过卡了
//								} else {
//									System.out.println(359);
//									String info = "你已经进行下班打卡，今日不能再进行上班打卡！";
//									os.write(info.getBytes()); // 告诉客户端已经打过卡了
//								}
//							} else {// 没打卡,进行打卡
//								dateFormat = new SimpleDateFormat("HH:mm");
//								// 获取当前打卡时间
//								String workUpTime = dateFormat.format(date);
//								// 正常打卡最迟时间
//								String normalTime = "09:00";
//								// 迟到打卡最迟时间
//								String latestTime = "11:00";
//								// 获取打卡状态
//								status = workUpStatus(workUpTime, normalTime, latestTime);
//								if (status.equals("过早")) {
//									// 告诉客户端打卡太早了，不能打
//									os.write("过早".getBytes());
//								} else {
//									String sql3 = "insert into clock(employeeId,dayDate,workUp,workUpStatus) values"
//											+ "('" + employeeId + "','" + dateNow + "','" + workUpTime + "','" + status
//											+ "')";
//									pstmt = conn.prepareStatement(sql3);
//									int ret = pstmt.executeUpdate();
//									if (ret > 0) {
//										// 打卡信息
//										String clockInfo = "打卡成功,状态为:" + status + ",打卡时间为:" + workUpTime;
//										// 告诉客户端打卡成功
//										os.write(clockInfo.getBytes());
//									} else {
//										// 告诉客户端打卡失败
//										os.write("打卡失败".getBytes());
//									}
//								}
//							}
//
//						} else {
//							os.write("打卡失败".getBytes());
//						}
//					} catch (SQLException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					} finally {
//						JdbcUtils.closeResultSet(rs);
//						JdbcUtils.closeConnection(conn);
//					}
//				}
//			}
//		};
//		new Thread(task).start();
//
//	}
//
//	// 客户端执行下班打卡方法
//	public void clientWorkDown(OutputStream os, String e_account) {
//		Runnable task = new Runnable() {
//			public void run() {
//				Connection conn = JdbcUtils.getConnection();
//				PreparedStatement pstmt = null;
//				ResultSet rs = null;
//				String employeeId = null;
//				// 打卡状态
//				String status;
//				// 打卡时间
//				String workDownTime;
//				// 正常下班时间
//				String normalTime = "18:00";
//				// 早退旷工分界时间点
//				String notWorkTime = "16:00";
//				Date date = new Date();
//				// 日期格式
//				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//				// 打卡时间格式
//				SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm");
//				// 获取当前日期
//				String dateNow = dateFormat.format(date);
//				// 获取当前时间
//				workDownTime = dateFormat2.format(date);
//				// 首先判断早上有没有打卡，如果没有，则需要插入日期,下班打卡时间,下班打卡状态
//				String sql1 = "select * from clock where "
//						+ "employeeId=(select employeeId from employee where employeeAccount=?)" + "and dayDate=?";
//				try {
//					if (isRest() == true) {
//						os.write("今天是休息日！".getBytes());
//					} else {
//						pstmt = conn.prepareStatement(sql1);
//						pstmt.setString(1, e_account);
//						pstmt.setString(2, dateNow);
//						rs = pstmt.executeQuery();
//						// 如果有结果，则证明早上有打卡，再判断有没有打过下班卡，然后更新打卡记录
//						if (rs.next()) {
//							String sql2 = "select workDown from clock where "
//									+ "employeeId=(select employeeId from employee where employeeAccount=?)"
//									+ "and dayDate=?";
//							pstmt = conn.prepareStatement(sql2);
//							pstmt.setString(1, e_account);
//							pstmt.setString(2, dateNow);
//							rs = pstmt.executeQuery();
//							// 如果有结果表示已经进行过下班打卡
//							if (rs.next() && rs.getString("workDown") != null) {
//								String info = "已经下班打卡,打卡时间为:" + rs.getString("workDown");
//								// 告诉客户端用户已经打过卡了
//								os.write(info.getBytes());
//							} else { // 还没打下班卡
//								// 获取打卡状态
//								status = workDownStatus(workDownTime, normalTime, notWorkTime);
//								// 更新打卡记录，添加下班打卡时间和状态
//								String sql3 = "update clock set workDown=?,workDownStatus=? "
//										+ "where employeeId=(select employeeId from employee where employeeAccount=?)"
//										+ "and dayDate=?";
//								pstmt = conn.prepareStatement(sql3);
//								pstmt.setString(1, workDownTime);
//								pstmt.setString(2, status);
//								pstmt.setString(3, e_account);
//								pstmt.setString(4, dateNow);
//								int ret = pstmt.executeUpdate();
//								if (ret > 0) {
//									String downInfo = "下班打卡成功,打卡时间为:" + workDownTime;
//									os.write(downInfo.getBytes());
//								} else {
//									os.write("打卡失败".getBytes());
//								}
//							}
//						} else {
//							if ((workDownTime.compareTo("11:00")) >= 0) {
//								// 早上没有打卡，需要插入打卡记录
//								status = workDownStatus(workDownTime, normalTime, notWorkTime);
//								// 先获得员工id
//								String sql4 = "select employeeId from employee where employeeAccount=?";
//								String e_id = null;
//								pstmt = conn.prepareStatement(sql4);
//								pstmt.setString(1, e_account);
//								rs = pstmt.executeQuery();
//								if (rs.next()) {
//									e_id = rs.getString("employeeId");
//								}
//								String sql5 = "insert into clock(employeeId,dayDate,workDown,workDownStatus) values(?,?,?,?)";
//								pstmt = conn.prepareStatement(sql5);
//								pstmt.setString(1, e_id);
//								pstmt.setString(2, dateNow);
//								pstmt.setString(3, workDownTime);
//								pstmt.setString(4, status);
//								int ret = pstmt.executeUpdate();
//								if (ret > 0) {
//									String downInfo = "下班打卡成功,打卡时间为:" + workDownTime;
//									os.write(downInfo.getBytes());
//								} else {
//									os.write("打卡失败".getBytes());
//								}
//							} else {
//
//								os.write("十一点前未进行上班打卡，请先进行上班打卡！".getBytes());
//							}
//						}
//					}
//				} catch (SQLException | IOException e) {
//					e.printStackTrace();
//				} finally {
//					// 关闭数据库资源
//					JdbcUtils.closeResultSet(rs);
//					JdbcUtils.closeConnection(conn);
//				}
//
//			}
//		};
//		new Thread(task).start();
//	}
//
//	// 客户端执行查看本人考勤信息方法
//	public void viewMyInfo(OutputStream os, String acceptInfo) {
//		Runnable task = new  Runnable() {
//			public void run() {
//				//与数据库建立连接
//				Connection conn = JdbcUtils.getConnection();
//				//创建预编译执行器
//				PreparedStatement pstmt = null;
//				//创建结果集
//				ResultSet rs = null;
//				//先将拿到的信息进行处理（其格式为账号,查询月份）
//				String e_id = null;
//				//出勤天数
//				String clockDay = null;
//				//迟到次数
//				String lateCount = null;
//				//早退次数
//				String earlyOutCount = null;
//				//旷工次数
//				String notWorkCount = null;
//				String[] arr = acceptInfo.split(",");
//				String e_account = arr[0];
//				String selecCond = arr[1];
//				Date date = new Date();
//				// 日期格式
//				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//				//获取当前日期
//				String dateNow = dateFormat.format(date);
//				String _dateNow = dateNow.substring(0, 7);
//				//首先判断查询的月份是否为当前月份，是则查询到今天的记录，不是则查一整个月的
//				/*
//				 * 三种情况，【1】查询月份为当前月 ：查当前月开始至今天为止
//				 * 		     【2】查询月份大于当前月：返回错误信息
//				 * 		     【3】查询月份小于当前月：查询整月信息
//				 */
//				
//				//为当前月
//				if(selecCond.equals(_dateNow)) {
//					//首先查一下员工的ID
//					String idSql = "select employeeId from employee where employeeAccount=?";
//					try {
//						pstmt = conn.prepareStatement(idSql);
//						pstmt.setString(1, e_account);
//						rs = pstmt.executeQuery();
//						if(rs.next()) {
//							e_id = rs.getString("employeeId");
//							//获得id后开始进行查询
//							//{1}首先查询迟到人数
//							String lateSql = "select count(workUpStatus) late "
//									+ "from (select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus "
//									+ "from (select  * from worksheet   where dayDate like ? and  "
//									+ "dayDate between '' and ?) wk  "
//									+ "left join (select * from clock where employeeId=?) ck  on wk.dayDate = ck.dayDate) dk  "
//									+ "where workUpStatus in ('迟到') and workDownStatus in('正常','早退')";
//							pstmt = conn.prepareStatement(lateSql);
//							pstmt.setString(1, _dateNow+"%");
//							pstmt.setString(2, dateNow);
//							pstmt.setString(3, e_id);
//							rs = pstmt.executeQuery();
//							if(rs.next()) {
//								//获得迟到次数
//								lateCount = rs.getString("late");
//							}else {
//								os.write("查询失败，获取用户信息时出错".getBytes());
//							}
//							//{2}查询早退次数
//							String earlySql = "select count(workDownStatus) early from "
//									+ "(select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus from "
//									+ "(select  * from worksheet   where dayDate like ? and dayDate "
//									+ "between '' and ?) wk  "
//									+ "left join (select * from clock where employeeId=?) ck  on wk.dayDate = ck.dayDate) dk  "
//									+ "where workDownStatus in ('早退') and workUpStatus in('正常','迟到')";
//							pstmt = conn.prepareStatement(earlySql);
//							pstmt.setString(1, _dateNow+"%");
//							pstmt.setString(2, dateNow);
//							pstmt.setString(3, e_id);
//							rs = pstmt.executeQuery();
//							if(rs.next()) {
//								//获得早退次数
//								earlyOutCount = rs.getString("early");
//							}else {
//								os.write("查询失败，获取用户信息时出错".getBytes());
//							}
//							//{3}查询旷工次数
//							String notWorlSql = "select count(ek.dayDate) ow "
//									+ "from (select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus "
//									+ "from (select  * from worksheet   where dayDate like ? "
//									+ "and dayDate between '' and ?) wk  "
//									+ "left join (select * from clock where employeeId=?) ck  "
//									+ "on wk.dayDate = ck.dayDate order by wk.dayDate) ek where ek.employeeId is null "
//									+ "or (ek.workUpStatus is null or ek.workUpStatus='旷工') "
//									+ "or (ek.workDownStatus is null or ek.workDownStatus='旷工') "
//									+ "or (ek.workUpStatus is null and ek.workDownStatus='旷工') "
//									+ "or (ek.workUpStatus ='旷工' and ek.workDownStatus is null)";
//													
//							pstmt = conn.prepareStatement(notWorlSql);
//							pstmt.setString(1, _dateNow+"%");
//							pstmt.setString(2, dateNow);
//							pstmt.setString(3, e_id);
//							rs = pstmt.executeQuery();
//							if(rs.next()) {
//								//获得旷工次数
//								notWorkCount = rs.getString("ow");
//							}else {
//								os.write("查询失败，获取用户信息时出错".getBytes());
//							}
//							//{4}查询出勤次数
//							String clockDaySql = "select count(ek.dayDate) ow from "
//									+ "(select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus "
//									+ "from (select  * from worksheet   "
//									+ "where dayDate like ? and dayDate between '' and ?) wk  "
//									+ "left join (select * from clock where employeeId=?) ck  "
//									+ "on wk.dayDate = ck.dayDate order by wk.dayDate) ek "
//									+ "where  ek.employeeId is not null;";
//													
//							pstmt = conn.prepareStatement(clockDaySql);
//							pstmt.setString(1, _dateNow+"%");
//							pstmt.setString(2, dateNow);
//							pstmt.setString(3, e_id);
//							rs = pstmt.executeQuery();
//							if(rs.next()) {
//								//获得早退次数
//								clockDay = rs.getString("ow");
//							}else {
//								os.write("查询失败，获取用户信息时出错".getBytes());
//							}
//							String allMyInfo = "查询成功！出勤:"+clockDay+"天 {迟到:"+lateCount+","
//													+ "早退:"+earlyOutCount+","
//													+ "旷工:"+notWorkCount+"}";
//							os.write(allMyInfo.getBytes());
//							
//						}else {
//							os.write("查询失败，获取用户信息时出错".getBytes());
//						}
//					} catch (SQLException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					
//					
//				}
//				//大于当前月
//				else if((selecCond.compareTo(_dateNow))>0) {
//					try {
//						os.write("查询月份大于当前月，查询失败！".getBytes());
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//				//小于当前月
//				else {
//					
//				}
//			}
//		};
//		new Thread(task).start();
//	}
//
//	// 客户端执行添加员工
//	public void addEmployee(OutputStream os, String e_info) {
//		Runnable task = new Runnable() {
//			@Override
//			public void run() {
//				// 与数据库建立连接
//				Connection conn = JdbcUtils.getConnection();
//				// 创建预编译执行器
//				PreparedStatement pstmt = null;
//				// 创建结果集
//				ResultSet rs = null;
//				// 将各字段值存入Map集合中
//				HashMap<String, String> map = spiltEmplyeeInfo(e_info);
//				System.out.println(map.get(" employeeAccount"));
//
//				// 首先查询id是不重复，登录账号是否重复
//				String idSelectSql = "select * from employee where employeeId=?";
//				try {
//					pstmt = conn.prepareStatement(idSelectSql);
//					pstmt.setString(1, map.get("employeeId"));
//					rs = pstmt.executeQuery();
//					// 有查询结果
//					if (rs.next()) {
//						String info = "该员工id已经被使用，请更换员工id!";
//						os.write(info.getBytes());
//					} else { // 没有查询结果，代表该id没被使用
//						// 接着判断员工账号是否被使用
//						String accounSelecttSql = "select * from employee where employeeAccount=?";
//						pstmt = conn.prepareStatement(accounSelecttSql);
//						pstmt.setString(1, map.get(" employeeAccount"));
//						rs = pstmt.executeQuery();
//						if (rs.next()) {
//							String info = "该员工账号已经被使用，请更改员工账号!";
//							os.write(info.getBytes());
//						} else {
//							// 添加普通员工
//							if (map.get(" capacity").equals("普通员工")) {
//								String insertSql = "insert into employee values(?,?,?,?,?,?,?,?)";
//								pstmt = conn.prepareStatement(insertSql);
//								pstmt.setString(1, map.get("employeeId"));
//								pstmt.setString(2, map.get(" employeeName"));
//								pstmt.setString(3, map.get(" employeeAccount"));
//								pstmt.setString(4, map.get(" employeePwd"));
//								pstmt.setString(5, map.get(" position"));
//								pstmt.setString(6, map.get(" salary"));
//								pstmt.setString(7, map.get(" workDate"));
//								pstmt.setString(8, map.get(" capacity"));
//								int ret = pstmt.executeUpdate();
//								if (ret > 0) {
//									String addInfo = "添加员工成功！";
//									os.write(addInfo.getBytes());
//								} else {
//									os.write("添加失败".getBytes());
//								}
//							} else {// 添加管理员，则需要向员工表和管理员表中都添加信息
//								String employeeSql = "insert into employee values(?,?,?,?,?,?,?,?)";
//								pstmt = conn.prepareStatement(employeeSql);
//								pstmt.setString(1, map.get("employeeId"));
//								pstmt.setString(2, map.get(" employeeName"));
//								pstmt.setString(3, map.get(" employeeAccount"));
//								pstmt.setString(4, map.get(" employeePwd"));
//								pstmt.setString(5, map.get(" position"));
//								pstmt.setString(6, map.get(" salary"));
//								pstmt.setString(7, map.get(" workDate"));
//								pstmt.setString(8, map.get(" capacity"));
//								int ret1 = pstmt.executeUpdate();
//								if (ret1 > 0) {
//									System.out.println("添加员工表成功");
//									String adminSql = "insert into admin values(?,?,?)";
//									pstmt = conn.prepareStatement(adminSql);
//									pstmt.setString(1, map.get("employeeId"));
//									pstmt.setString(2, map.get(" employeeName"));
//									pstmt.setString(3, map.get(" employeePwd"));
//									int ret2 = pstmt.executeUpdate();
//									if (ret2 > 0) {
//										String addInfo = "添加管理员成功！";
//										os.write(addInfo.getBytes());
//									} else {
//										os.write("添加管理员失败！".getBytes());
//									}
//								} else {
//									os.write("添加员工失败".getBytes());
//								}
//							}
//
//						}
//					}
//
//				} catch (SQLException e) {
//					String errorInfo = "数据格式有错误，错误信息：" + e.getMessage();
//					try {
//						os.write(errorInfo.getBytes());
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				} finally {
//					JdbcUtils.closeResultSet(rs);
//					JdbcUtils.closeConnection(conn);
//				}
//
//			}
//		};
//		new Thread(task).start();
//
//	}
//
//	// 客户端执行查看所有员工考勤统计
//	public void viewAllInfo(OutputStream os,String acceptInfo) {
//		Runnable task = new  Runnable() {
//			public void run() {
//				//与数据库建立连接
//				Connection conn = JdbcUtils.getConnection();
//				//创建预编译执行器
//				PreparedStatement pstmt = null;
//				//创建结果集
//				ResultSet rs = null;
//				//先将拿到的信息进行处理（其格式为账号,查询月份）
//				String e_id = null;
//				//出勤天数
//				String clockDay = null;
//				//迟到次数
//				String lateCount = null;
//				//早退次数
//				String earlyOutCount = null;
//				//旷工次数
//				String notWorkCount = null;
//				String[] arr = acceptInfo.split(",");
//				String e_name = arr[0];
//				String selecCond = arr[1];
//				Date date = new Date();
//				// 日期格式
//				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//				//获取当前日期
//				String dateNow = dateFormat.format(date);
//				String _dateNow = dateNow.substring(0, 7);
//				//首先判断查询的月份是否为当前月份，是则查询到今天的记录，不是则查一整个月的
//				/*
//				 * 三种情况，【1】查询月份为当前月 ：查当前月开始至今天为止
//				 * 		     【2】查询月份大于当前月：返回错误信息
//				 * 		     【3】查询月份小于当前月：查询整月信息
//				 */
//				
//				//为当前月
//				if(selecCond.equals(_dateNow)) {
//					//首先查一下员工的ID
//					String idSql = "select employeeId from employee where employeeName=?";
//					try {
//						pstmt = conn.prepareStatement(idSql);
//						pstmt.setString(1, e_name);
//						rs = pstmt.executeQuery();
//						if(rs.next()) {
//							e_id = rs.getString("employeeId");
//							//获得id后开始进行查询
//							//{1}首先查询迟到人数
//							String lateSql = "select count(workUpStatus) late "
//									+ "from (select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus "
//									+ "from (select  * from worksheet   where dayDate like ? and  "
//									+ "dayDate between '' and ?) wk  "
//									+ "left join (select * from clock where employeeId=?) ck  on wk.dayDate = ck.dayDate) dk  "
//									+ "where workUpStatus in ('迟到') and workDownStatus in('正常','早退')";
//							pstmt = conn.prepareStatement(lateSql);
//							pstmt.setString(1, _dateNow+"%");
//							pstmt.setString(2, dateNow);
//							pstmt.setString(3, e_id);
//							rs = pstmt.executeQuery();
//							if(rs.next()) {
//								//获得迟到次数
//								lateCount = rs.getString("late");
//							}else {
//								os.write("查询失败，获取用户信息时出错".getBytes());
//							}
//							//{2}查询早退次数
//							String earlySql = "select count(workDownStatus) early from "
//									+ "(select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus from "
//									+ "(select  * from worksheet   where dayDate like ? and dayDate "
//									+ "between '' and ?) wk  "
//									+ "left join (select * from clock where employeeId=?) ck  on wk.dayDate = ck.dayDate) dk  "
//									+ "where workDownStatus in ('早退') and workUpStatus in('正常','迟到')";
//							pstmt = conn.prepareStatement(earlySql);
//							pstmt.setString(1, _dateNow+"%");
//							pstmt.setString(2, dateNow);
//							pstmt.setString(3, e_id);
//							rs = pstmt.executeQuery();
//							if(rs.next()) {
//								//获得早退次数
//								earlyOutCount = rs.getString("early");
//							}else {
//								os.write("查询失败，获取用户信息时出错".getBytes());
//							}
//							//{3}查询旷工次数
//							String notWorlSql = "select count(ek.dayDate) ow "
//									+ "from (select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus "
//									+ "from (select  * from worksheet   where dayDate like ? "
//									+ "and dayDate between '' and ?) wk  "
//									+ "left join (select * from clock where employeeId=?) ck  "
//									+ "on wk.dayDate = ck.dayDate order by wk.dayDate) ek where ek.employeeId is null "
//									+ "or (ek.workUpStatus is null or ek.workUpStatus='旷工') "
//									+ "or (ek.workDownStatus is null or ek.workDownStatus='旷工') "
//									+ "or (ek.workUpStatus is null and ek.workDownStatus='旷工') "
//									+ "or (ek.workUpStatus ='旷工' and ek.workDownStatus is null)";
//													
//							pstmt = conn.prepareStatement(notWorlSql);
//							pstmt.setString(1, _dateNow+"%");
//							pstmt.setString(2, dateNow);
//							pstmt.setString(3, e_id);
//							rs = pstmt.executeQuery();
//							if(rs.next()) {
//								//获得旷工次数
//								notWorkCount = rs.getString("ow");
//							}else {
//								os.write("查询失败，获取用户信息时出错".getBytes());
//							}
//							//{4}查询出勤次数
//							String clockDaySql = "select count(ek.dayDate) ow from "
//									+ "(select id,wk.dayDate,employeeId,workUp,workUpStatus,workDown,workDownStatus "
//									+ "from (select  * from worksheet   "
//									+ "where dayDate like ? and dayDate between '' and ?) wk  "
//									+ "left join (select * from clock where employeeId=?) ck  "
//									+ "on wk.dayDate = ck.dayDate order by wk.dayDate) ek "
//									+ "where  ek.employeeId is not null;";
//													
//							pstmt = conn.prepareStatement(clockDaySql);
//							pstmt.setString(1, _dateNow+"%");
//							pstmt.setString(2, dateNow);
//							pstmt.setString(3, e_id);
//							rs = pstmt.executeQuery();
//							if(rs.next()) {
//								//获得早退次数
//								clockDay = rs.getString("ow");
//							}else {
//								os.write("查询失败，获取用户信息时出错".getBytes());
//							}
//							String allMyInfo = "查询成功！"
//									+ "["+e_name+"]出勤:"+clockDay+"天 {迟到:"+lateCount+","
//													+ "早退:"+earlyOutCount+","
//													+ "旷工:"+notWorkCount+"}";
//							os.write(allMyInfo.getBytes());
//							
//						}else {
//							os.write("查询失败，获取用户信息时出错".getBytes());
//						}
//					} catch (SQLException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					
//					
//				}
//				//大于当前月
//				else if((selecCond.compareTo(_dateNow))>0) {
//					try {
//						os.write("查询月份大于当前月，查询失败！".getBytes());
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//				//小于当前月
//				else {
//					
//				}
//			}
//		};
//		new Thread(task).start();
//	}
//
//	// 判断是不是休息日
//	public boolean isRest() {
//		Date date = new Date();
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//		String currDate = dateFormat.format(date);
//		Connection conn = JdbcUtils.getConnection();
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//		String sql = "select * from worksheet where dayDate=?";
//		try {
//			pstmt = conn.prepareStatement(sql);
//			pstmt.setString(1, currDate);
//			rs = pstmt.executeQuery();
//			if (rs.next()) { // 有代表不是休息日
//				return false;
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		} finally {
//			JdbcUtils.closeResultSet(rs);
//			JdbcUtils.closeConnection(conn);
//		}
//		return true;
//	}
//
//	// 用于判断上班打卡状态 9:00
//	public String workUpStatus(String workUpTime, String normalTime, String latestTime) {
//		String status = null;
//		// 进行打卡时间的对比，
//		int res = workUpTime.compareTo(normalTime);
//		// 如果等于指定打卡时间或则前两小时
//		if (res == 0 || (res > -2 && res < 0)) {
//			status = "正常";
//		} else if (res < -2) { // 七点前打卡太早//拒绝
//			status = "过早";
//		} else if (res > 0) {
//			res = workUpTime.compareTo(latestTime);
//			if (res <= 0) {
//				status = "迟到";
//			} else {
//				status = "旷工";
//			}
//		}
//		return status;
//	}
//
//	// 用于判断下班打卡状态
//	public String workDownStatus(String workDownTime, String normalTime, String notWorkTime) {
//		String status = null;
//		// 进行打卡时间的对比，
//		int res = workDownTime.compareTo(normalTime);
//		// 如果下班打卡时间大于或等于正常下班时间，都视为正常打卡
//		if (res == 0 || res > 0) {
//			status = "正常";
//		}
//		// 如果小于0，则证明员工有早退或旷工嫌疑，进行判断
//		else if (res < 0) {
//			// 和早退旷工分界点进行对比
//			res = workDownTime.compareTo(notWorkTime);
//			// 如果大于则为早退
//			if (res > 0) {
//				status = "早退";
//			} else {// 如果小于则为旷工
//				status = "旷工";
//			}
//		}
//		return status;
//	}
//
//	public HashMap<String, String> spiltEmplyeeInfo(String e_info) {
//		HashMap<String, String> map = new HashMap<String, String>();
//		String str = null;
//		// System.out.println(e_info);
//		// 【1】先取出中括号内的内容
//		Pattern pattern = Pattern.compile("(\\[[^\\]]*\\])");
//		Matcher matcher = pattern.matcher(e_info);
//		while (matcher.find()) {
//			str = matcher.group().substring(1, matcher.group().length() - 1);
//		}
//		// System.out.println(str);
//		// 【2】然后将中括号的内容按逗号分隔
//		String[] str2 = str.split(",");
//		// 以键值对形式存储
//		for (String string : str2) {
//			// 【3】然后按照“=”分隔
//			String[] str3 = string.split("=");
//			map.put(str3[0], str3[1]);
//		}
//		// System.out.println(map);
//		return map;
//	}
}
