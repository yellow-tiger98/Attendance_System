package com.hd.main;

import com.hd.network.NetWork;

import message.Base;

//用于运行考勤系统服务器
public class ServerMain extends Base {
	private NetWork netWork;
	
	
	public static void main(String[] args) {
		ServerMain serverMain = new ServerMain();
		serverMain.start();
	}
	
	public void start() {
		netWork = new NetWork(this);
		netWork.statrServer(6666);
	}

	@Override
	public void onCreateServer(boolean ret) {
		String msg = ret?"启动成功":"启动失败";
		print(msg);
	}

	@Override
	public void onAccept(String ip, int port) {
		print(ip+":"+port+"已连入");
	}
	
	public void print(String str) {
		System.out.println("[YTG服务器]"+str);
	}
	
	
}
