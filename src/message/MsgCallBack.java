package message;



public interface MsgCallBack {
	//------客户端与服务器----
	
	//开启服务器状态
	public void onCreateServer(boolean ret);
	
	//接收socket状态
	public void onAccept(String ip,int port);
	
	//连接服务器状态
	public void onConnecteServer(boolean ret,String myScoketId);
	
	
	
	//普通员工
	
	//普通员工上班打卡成功
	public void workUpSuccess(boolean ret,String info);
	
	//普通员工上班打卡失败
	public void workUpOnFailed(String str);
	
	//普通员工下班打卡成功
	public void workDownSuccess(boolean ret,String info);
	
	//普通员工下班打卡失败
	public void workDownOnFailed(String str);
	
	//管理员
	
	//添加员工成功
	public void addEmployeeSuccess(String str);
	
	//添加员工失败
	public void addEmployeeFalied(String str);

}
