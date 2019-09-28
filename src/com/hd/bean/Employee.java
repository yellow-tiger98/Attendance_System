package com.hd.bean;


public class Employee {
	// 员工编号
	private String employeeId;
	// 员工名字
	private String employeeName;
	// 员工登录账户名
	private String employeeAccount;
	// 员工登录密码
	private String employeePwd;
	// 员工薪水
	private String salary;
	// 员工入职日期
	private String workDate;
	// 员工身份
	private String capacity;
	// 员工职位
	private String position;

	public Employee() {

	}

	public Employee(String employeeAccount, String employeePwd) {
		this.employeeAccount = employeeAccount;
		this.employeePwd = employeePwd;
	}

	public Employee(String employeeId, String employeeName, String employeeAccount, String employeePwd, String salary,
			String workDate, String capacity, String position) {
		super();
		this.employeeId = employeeId;
		this.employeeName = employeeName;
		this.employeeAccount = employeeAccount;
		this.employeePwd = employeePwd;
		this.salary = salary;
		this.workDate = workDate;
		this.capacity = capacity;
		this.position = position;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public String getEmployeeAccount() {
		return employeeAccount;
	}

	public void setEmployeeAccount(String employeeAccount) {
		this.employeeAccount = employeeAccount;
	}

	public String getEmployeePwd() {
		return employeePwd;
	}

	public void setEmployeePwd(String employeePwd) {
		this.employeePwd = employeePwd;
	}

	public String getSalary() {
		return salary;
	}

	public void setSalary(String salary) {
		this.salary = salary;
	}

	public String getWorkDate() {
		return workDate;
	}

	public void setWorkDate(String workDate) {
		this.workDate = workDate;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return "Employee [employeeId=" + employeeId + ", employeeName=" + employeeName + ", employeeAccount="
				+ employeeAccount + ", employeePwd=" + employeePwd + ", salary=" + salary + ", workDate=" + workDate
				+ ", capacity=" + capacity + ", position=" + position + "]";
	}

}
