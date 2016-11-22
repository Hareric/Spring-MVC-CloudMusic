package model;

import java.util.Calendar;
import java.util.Random;
import java.sql.Date;

import model.db.Connector;
import model.db.DbHelper;

public class UserModel {
	private String name;
	private String repwd;
	private String email; // 注册邮箱
	private String pwd; // 注册密码
	private int id; // 用户id
	private Date regDate; // 注册日期
	private int root=0; // 是否为管理员

	public String getRepwd() {
		return repwd;
	}

	public void setRepwd(String repwd) {
		this.repwd = repwd;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getRegDate() {
		return regDate;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}

	public int getRoot() {
		return root;
	}

	public void setRoot(int root) {
		this.root = root;
	}

	/**
	 * 判断帐号是否为Email 鉴于现在Email帐号前缀和后缀复杂性，所以判断 @ 和 .} 是否存在以及其的位置
	 * 
	 * @param acc
	 * @return
	 */
	public boolean isEmail(String acc) {
		if (acc == null || acc.length() < 5) {
			// #如果帐号小于5位，则肯定不可能为邮箱帐号eg: x@x.x
			return false;
		}
		if (!acc.contains("@")) {// 判断是否含有@符号
			return false;// 没有@则肯定不是邮箱
		}
		String[] sAcc = acc.split("@");
		if (sAcc.length != 2) {// # 数组长度不为2则包含2个以上的@符号，不为邮箱帐号
			return false;
		}
		if (sAcc[0].length() <= 0) {// #@前段为邮箱用户名，自定义的话至少长度为1，其他暂不验证
			return false;
		}
		if (sAcc[1].length() < 3 || !sAcc[1].contains(".")) {
			// # @后面为域名，位数小于3位则不为有效的域名信息
			// #如果后端不包含.则肯定不是邮箱的域名信息
			return false;
		} else {
			if (sAcc[1].substring(sAcc[1].length() - 1).equals(".")) {
				// # 最后一位不能为.结束
				return false;
			}
			String[] sDomain = sAcc[1].split("\\.");
			// #将域名拆分 tm-sp.com 或者 .com.cn.xxx
			for (String s : sDomain) {
				if (s.length() <= 0) {
					System.err.println(s);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 新注册用户存入数据库
	 */
	public void saveSql(){
		DbHelper connector = Connector.getInstance();
		connector.executeUpdate("INSERT INTO `app_User` (`email`, `pwd`, `id`, `regDate`, `root`) VALUES (?, ?, ?, ?, ?)",
				this.email, this.pwd, this.id, this.regDate, this.root);
		connector.executeUpdate("INSERT INTO `app_Info` (`user_id`, `name`, `image`) VALUES (?, ?, ?')",
				this.id, this.name, "https://lh3.googleusercontent.com/-pEKVkqtRgng/AAAAAAAAAAI/AAAAAAAAAAA/AEMOYSAOYCjOu6PGJxyVf1asIPCUerxhww/mo/photo.jpg?sz=46");
	}
	public String register(){
		DbHelper connector = Connector.getInstance();
		if(!isEmail(this.email)){
			return "邮箱输入有误";
		}
		if(connector.isExist("SELECT * FROM app_User WHERE email=?",this.email)){
			return "该邮箱已注册";
		}
		if(!this.pwd.equals(this.repwd)){
			return "密码不一致";
		}
		
		java.util.Date date = new java.util.Date();
		this.regDate = new Date(date.getYear(), date.getMonth(), date.getDate());
		Random random = new Random();
		this.id = Math.abs(random.nextInt());  // 为用户随机生成
		while(connector.isExist("SELECT * FROM app_User WHERE id=?",this.id)){
			this.id = Math.abs(random.nextInt());
		}
		this.saveSql();
		return "注册成功";
	}
	
//	public static void main(String args[]){
//		DbHelper connector = Connector.getInstance();
//		connector.executeUpdate("INSERT INTO `app_User` (`email`, `pwd`, `id`, `regDate`, `root`) VALUES (?, ?, ?, ?, ?)",
//				"qwd@qq.com", "1q2w3e", 13413, new Date(1,2,3), 0);
//		
//	}
}
