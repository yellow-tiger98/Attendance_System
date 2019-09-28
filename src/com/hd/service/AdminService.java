package com.hd.service;

import com.hd.bean.Employee;
import com.hd.network.NetWork;

import message.MsgCallBack;

public class AdminService {
	private NetWork netWork;
	private MsgCallBack msgCallBack;
	
	public AdminService(NetWork netWork,MsgCallBack msgCallBack) {
		this.netWork = netWork;
		this.msgCallBack = msgCallBack;
	}
	
	//添加员工
	public void addEmployee(Employee employee,String socketKey) {
		netWork.send("添加员工信息",socketKey);
		String response1 = netWork.clientRead(socketKey);
		print("服务器成功接收到"+response1+"请求");
		String e_info = employee.toString();
		netWork.send(e_info, socketKey);
		String response2 = netWork.clientRead(socketKey);
		if(response2.contains("员工id已经被使用")) {
			msgCallBack.addEmployeeFalied(response2);
		}else if(response2.contains("员工账号已经被使用")) {
			msgCallBack.addEmployeeFalied(response2);
		}else if(response2.contains("成功")) {
			msgCallBack.addEmployeeSuccess(response2);
		}else if(response2.contains("失败")) {
			msgCallBack.addEmployeeFalied(response2);
		}else if(response2.contains("数据格式")) {
			msgCallBack.addEmployeeFalied(response2);
		}
	}
	
	//查看某员工的考勤统计
	public void viewAllInfo(String e_name,String month,String socketKey) {
		//首先向服务器发送请求指令
		netWork.send("查看员工考勤统计", socketKey);
		//接收服务器反馈
		String response1 = netWork.clientRead(socketKey);
		print("服务器成功接收到" + response1 + "请求");;
		//将账号与查询日期合并
		String sendInfo = e_name+","+month;
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
	
	public void exit(boolean isRun) {
	
	}

	public void print(String str) {
		System.out.println("[YTG]"+str);
	}
}
