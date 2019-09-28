package com.hd.bean;

public class Admin {
	//管理员id
	private String adminId;
	//管理员名字
	private String adminName;
	//管理员密码
	private String adminPwd;
	
	public Admin() {
		
	}
	
	public Admin(String adminId,String adminPwd) {
		
	}
	
	public Admin(String adminId, String adminName, String adminPwd) {
		super();
		this.adminId = adminId;
		this.adminName = adminName;
		this.adminPwd = adminPwd;
	}
	
	public String getAdminId() {
		return adminId;
	}
	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}
	public String getAdminName() {
		return adminName;
	}
	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}
	public String getAdminPwd() {
		return adminPwd;
	}
	public void setAdminPwd(String adminPwd) {
		this.adminPwd = adminPwd;
	}
	

}
