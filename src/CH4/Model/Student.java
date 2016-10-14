package CH4.Model;

public class Student {
	private int st_no;
	private String st_name;
	private String st_dept;
	private String st_Password;
	private String st__emial;  // 数据库拼错了 这里跟着数据库变量名
	
	public int getSt_no()
	{
		return this.st_no;
	}
	
	public String getSt_name()
	{
		return this.st_name;
	}
	
	public String getSt_dept()
	{
		return this.st_dept;
	}
	public String getSt_Password()
	{
		return this.st_Password;
	}
	public String getSt__emial()
	{
		return this.st__emial;
	}
	public void setSt_no(int st_no)
	{
		this.st_no = st_no;
	}
	public void setSt_name(String st_name)
	{
		this.st_name = st_name;
	}
	public void setSt_dept(String st_dept)
	{
		this.st_dept = st_dept;
	}
	public void setSt_password(String st_password)
	{
		this.st_Password = st_password;
	}
	public void setSt__email(String st__eml)
	{
		this.st__emial = st__emial;
	}
	
}
