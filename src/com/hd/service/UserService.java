package com.hd.service;

import com.hd.bean.Employee;
import com.hd.network.NetWork;

import message.MsgCallBack;

public class UserService {
	private MsgCallBack msgCallBack;
	private NetWork netWork;

	public UserService(NetWork netWork, MsgCallBack msgCallBack) {
		this.netWork = netWork;
		this.msgCallBack = msgCallBack;
	}
	
	//上班打卡
	public void workUp(Employee employee,String socketKey) {
		// 向服务器发送上班打卡指令
		netWork.send("上班打卡",socketKey);
		String response1 = netWork.clientRead(socketKey);
		print("服务器成功接收到" + response1 + "请求");
		// 获得员工的账号名，作为数据库操作的条件
		String e_account = employee.getEmployeeAccount();
		// 将其发送给服务器
		netWork.send(e_account,socketKey);
		// 获取打卡结果
		String response2 = netWork.clientRead(socketKey);
		// 根据返回结果的不同做出相应处理
		if (response2.equals("休息日")) {
			msgCallBack.workUpOnFailed(response2);
		} else if (response2.equals("过早")) {
			msgCallBack.workUpOnFailed(response2);
		} else if (response2.contains("打卡成功")) {
			msgCallBack.workUpSuccess(true, response2);
		} else if (response2.contains("打卡失败")) {
			msgCallBack.workUpOnFailed(response2);
		} else if (response2.contains("已经打过卡")) {
			msgCallBack.workUpOnFailed(response2);
		} else if(response2.contains("下班打卡")){
			msgCallBack.workUpOnFailed(response2);
		}
	}
	
	//下班打卡
	public void workDown(Employee employee,String socketKey) {
		// 向服务器发送上班打卡指令
		netWork.send("下班打卡",socketKey);
		String response1 = netWork.clientRead(socketKey);
		print("服务器成功接收到" + response1 + "请求");
		// 获得员工的账号名，作为数据库操作的条件
		String e_account = employee.getEmployeeAccount();
		// 将其发送给服务器
		netWork.send(e_account,socketKey);
		// 获取打卡结果
		String response2 = netWork.clientRead(socketKey);
		// 根据返回结果的不同做出相应处理
		if(response2.contains("已经打过下班卡")) {
			msgCallBack.workDownOnFailed(response2);
		}else if(response2.contains("下班打卡成功")) {
			msgCallBack.workDownSuccess(true, response2);
		}else if(response2.contains("打卡失败")) {
			msgCallBack.workDownOnFailed(response2);
		}else if(response2.contains("十一点前")) {
			msgCallBack.workDownOnFailed(response2);
		}else if(response2.contains("休息日")) {
			msgCallBack.workDownOnFailed(response2);
		}
	}
	
	//查看自己的考勤统计
	public void viewMyInfo(String selectDate,Employee employee,String socketKey) {
		//首先向服务器发送请求指令
		netWork.send("查看我的考勤记录", socketKey);
		//接收服务器反馈
		String response1 = netWork.clientRead(socketKey);
		print("服务器成功接收到" + response1 + "请求");
		//获取员工账号
		String e_account = employee.getEmployeeAccount();
		//将账号与查询日期合并
		String sendInfo = e_account+","+selectDate;
		//将信息发送给服务器
		netWork.send(sendInfo, socketKey);
		//接收服务器的查询反馈
		String response2 = netWork.clientRead(socketKey);
		if(response2.contains("查询失败")) {
			print(response2);
		}else if(response2.contains("查询成功")) {
			String[] arr = response2.split(",");
			print(arr[0]);
			System.out.println("id         日期                               员工id  上班打卡时间   状态          下班打卡时间       状态");
			for (int i = 1; i < arr.length; i++) {
				System.out.println(arr[i]);
			}
		}
	}

	public void exit() {

	}

	// 自定义打印方法
	public void print(String str) {
		System.out.println("[YTG]" + str);
	}
}
