package com.hd.service;

import java.lang.reflect.Field;

import com.hd.network.NetWork;

public class GeneralService {
	private NetWork netWork;
	public GeneralService(NetWork netWork) {
		this.netWork = netWork;
	}
	
	//遵循一读一写，防止粘包
	public <T>Object login(Class<T> clazz,String account,String pwd,int capacityId,String socketKey){
		//普通用户身份
		if(capacityId==1) {
			//告诉服务器我要执行登录
			netWork.send("登录",socketKey);
			//得到服务器反馈
			String response1 = netWork.clientRead(socketKey);
			print("服务器成功接收到"+response1+"请求");
			//拼接登录信息
			String accountInfo = "[普通员工]"+account+","+pwd;
			//将登录信息发送给服务器
			netWork.send(accountInfo,socketKey);
			//获取登录结果
			String response2 = netWork.clientRead(socketKey);
			
			//登录成功就返回一个当前员工对象
			if(response2.equals("登录成功")) {
				Field field;
				try {
					Object obj = clazz.newInstance();
					//获取普通员工帐号字段
					field = clazz.getDeclaredField("employeeAccount");
					//打开权限
					field.setAccessible(true);
					//设置字段值
					field.set(obj, account);
					//获取普通员工密码字段
					field = clazz.getDeclaredField("employeePwd");
					//打开权限
					field.setAccessible(true);
					//设置字段值
					field.set(obj, pwd);
					//返回返回值
					return obj;
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		//管理员身份用户
		else {
			//告诉服务器我要执行登录功能
			netWork.send("登录",socketKey);
			//得到服务器反馈
			String response1 = netWork.clientRead(socketKey);
			print("服务器成功接收到"+response1+"请求");
			//拼接登录信息
			String accountInfo = "[管理员]"+account+","+pwd;
			//发送账号信息
			netWork.send(accountInfo,socketKey);
			//得到服务器反馈，登陆成功与否
			String response2 = netWork.clientRead(socketKey);
			System.out.println(response2);
			//登录成功则创建当前管理员对象
			if(response2.equals("登录成功")) {
				Field field;
				try {
					Object obj = clazz.newInstance();
					//获取管理员id字段
					field = clazz.getDeclaredField("adminId");
					//打开权限
					field.setAccessible(true);
					//设置字段值
					field.set(obj, account);
					//获取管理员密码字段
					field = clazz.getDeclaredField("adminPwd");
					//打开权限
					field.setAccessible(true);
					//设置字段值
					field.set(obj, pwd);
					//返回管理员对象给外面处理
					return obj;
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			
		}
		return null;
	}
	
	//自定义打印方法
	public void print(String str) {
		System.out.println("[YTG]"+str);
	}

}
