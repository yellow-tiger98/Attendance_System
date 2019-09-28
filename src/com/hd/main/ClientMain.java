package com.hd.main;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import com.hd.bean.Admin;
import com.hd.bean.Employee;
import com.hd.network.NetWork;
import com.hd.service.AdminService;
import com.hd.service.GeneralService;
import com.hd.service.UserService;

import message.Base;

/*
 * 客户端
 * 1.使用network连接服务器(返回一个socket)
 * 2.连接成功后，连接服务器上的数据库
 * 3.然后执行主程序
 * 4.选择身份
 * 5.登录   ---->Generally.login()---->向服务器中方法传入输入的信息---->服务器接收---->服务器中向数据库执行查询---->返回结果
 * 6.做出功能选择
 * 7.返回上一页
 * 8.退出
 */

//用于运行考勤系统主程序
public class ClientMain extends Base {
	private NetWork netWork;
	private Boolean status = false;
	private String serverIp = "127.0.0.1";
	private int serverPort = 6666;
	private String socketKey = serverIp + ":" + serverPort;

	// 主方法
	public static void main(String[] args) {
		ClientMain clientMain = new ClientMain();
		clientMain.start();
	}

	// 用于启动客户端的方法
	public void start() {
		// 创建一个网络工具类
		netWork = new NetWork(this);
		// 连接到服务器
		netWork.connectServer(serverIp, serverPort);

	}

	// 自定义打印方法
	public void print(String str) {
		System.out.println("[YTG]" + str);
	}

	@Override
	public void onConnecteServer(boolean ret, String myScoketId) {
		String info = ret ? "连接成功" : "连接失败";
		if (info.equals("连接成功")) {
			print("服务器" + info);
			this.socketKey = myScoketId;
			this.status = true;
			// 程序主体
			program(status);
		} else {
			print("服务器连接失败，无法进入系统！");
		}
	}

	public void program(boolean status) {
		// 用于记录命令
		String _number;
		int number;
		Employee employee = null;
		Admin admin = null;
		// 创建一个通用Dao对象,用于执行普通员工与管理员的通用方法
		GeneralService generalDao = new GeneralService(netWork);
		// 创建一个UserDao对象,用于执行普通员工的方法
		UserService userDao = new UserService(netWork, this);
		// 创建一个AdminDao对象,用于执行管理员的方法
		AdminService adminDao = new AdminService(netWork, this);
		boolean isRun = false;
		if (status == true) {
			isRun = true;
			while (isRun) {
				// 以下是控制台交互
				Scanner sc = new Scanner(System.in);
				String account;
				String pwd;
				System.out.println("[欢迎进入YTG人事考勤系统]");
				System.out.println("---------------------------------------------------- ");
				print("请选择你的身份进行登录:1.普通员工 2.管理员(输入数字进行选择)3.退出");
				_number = sc.nextLine();
				number = Integer.valueOf(_number);
				if(number==3) {
					break;
				}
				print("请输入你的账号:");
				account = sc.nextLine();
				print("请输入你的密码:");
				pwd = sc.nextLine();

				// 普通员工身份登录
				if (number == 1) {
					// 利用通用Dao的反射机制方法进行登录，若登录成功返回一个员工对象
					employee = (Employee) generalDao.login(Employee.class, account, pwd, 1, socketKey);
					if (employee != null) {
						print("登录成功！");
						boolean isEmpLogin = true;
						System.out.println("----------------------------------------------------");
						while (isEmpLogin) {
							Date date = new Date();
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
							String nowDate = dateFormat.format(date);
							print("现在是" + nowDate);
							print("请进行功能选择:");
							print("1.上班打卡");
							print("2.下班打卡");
							print("3.查看我的考勤记录");
							print("4.退出");
							_number = sc.nextLine();
							number = Integer.valueOf(_number);
							switch (number) {
							case 1:
								// 执行员工上班打卡方法
								userDao.workUp(employee, socketKey);
								print("请输入任意键返回菜单栏!");
								sc.nextLine();
								break;
							case 2:
								// 执行员工下班打卡方法
								userDao.workDown(employee, socketKey);
								print("请输入任意键返回菜单栏!");
								sc.nextLine();
								break;
							case 3:
								// 执行员工查看自己的考勤信息办法
								print("请输入你要查询的月份(按照格式2019-01)：");
								String selectDate = sc.nextLine();
								userDao.viewMyInfo(selectDate,employee, socketKey);
								print("请输入任意键返回菜单栏!");
								sc.nextLine();
								break;
							case 4:
								// 执行员工退出系统方法
//								userDao.exit();
//								print("请输入任意键返回菜单栏!");
//								sc.nextLine();
								isRun = false;
								isEmpLogin = false;
								break;
							default:
								print("命令非法,请重新输入！");
								break;
							}
						}
					} else {
						print("账号或者密码错误,登录失败！");
					}
				} else { // 管理员身份登录
					admin = (Admin) generalDao.login(Admin.class, account, pwd, 2, socketKey);
					if (admin != null) {
						print("登录成功！");
						boolean isAmLogin = true;
						System.out.println("---------------------------------------");
						while (isAmLogin) {
							Date _nowdate = new Date();
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
							String nowDate = dateFormat.format(_nowdate);
							print("现在是" + nowDate);
							print("请进行功能选择:");
							print("1.添加员工信息");
							print("2.查看所有员工考勤统计");
							print("3.退出");
							_number = sc.nextLine();
							number = Integer.valueOf(_number);
							switch (number) {
							case 1:
								print("请输入员工名:");
								String name = sc.nextLine();
								print("请输入员工编号:");
								String employeeId = sc.nextLine();
								print("请输入员工登录账号:");
								String employeeAccount = sc.nextLine();
								print("请输入员工登录密码:");
								String employeePwd = sc.nextLine();
								print("请输入员工工作职位:");
								String position = sc.nextLine();
								print("请输入员工薪资:");
								String salary = sc.nextLine();
								print("请输入员工入职时间[按照2019-01-01这种格式]:");
								String workDate = sc.nextLine();
								print("请输入员工身份[普通员工\\管理员]:");
								String capacity = sc.nextLine();

								// 创建一个新员工
								Employee newEmployee = new Employee(employeeId, name, employeeAccount, employeePwd,
										salary, workDate, capacity, position);
								// 调用添加员工方法
								adminDao.addEmployee(newEmployee, socketKey);
								break;
							case 2:
								print("请输入查询员工姓名:");
								String e_name = sc.nextLine();
								print("请输入查询月份(按照2019-01格式):");
								String month = sc.nextLine();
								// 调用查看全部员工考勤统计方法
								adminDao.viewAllInfo(e_name,month,socketKey);
								break;
							case 3:
								// 调用退出方法------>此处可写为通用Dao
								isRun = false;
								isAmLogin = false;
								break;
							default:
								System.out.println("命令非法,请重新输入！");
								break;
							}
						}
					} else {
						print("账号或者密码错误,登录失败！");
					}
				}
			}
		}
	}

	// 打开成功的消息调用
	@Override
	public void workUpSuccess(boolean ret, String info) {
		if (ret == true) {
			print(info);
		}
	}

	// 打卡失败的消息调用
	@Override
	public void workUpOnFailed(String str) {
		if (str.equals("休息日")) {
			print("今天是" + str);
		} else if (str.contains("已经打过卡")) {
			print(str);
		} else if (str.equals("过早")) {
			print("打卡时间" + str + ",请在七点之后打卡!");
		} else if (str.equals("打卡失败")) {
			print("出现错误," + str);
		} else if (str.contains("下班打卡")) {
			print(str);
		}
	}

	@Override
	public void workDownSuccess(boolean ret, String info) {
		if (ret == true) {
			print(info);
		}
	}

	@Override
	public void workDownOnFailed(String str) {
		print(str);
	}

	@Override
	public void addEmployeeSuccess(String str) {
		print(str);
	}

	@Override
	public void addEmployeeFalied(String str) {
		print(str);
	}

}
