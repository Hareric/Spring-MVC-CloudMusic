package CH3;

import java.sql.SQLException;
import CH2.DBhealper;
/**
* @author Eric_Chan
* @version 2016.10.11
*/
public class StudentLogin extends StudentLoginFrame{
	
	private DBhealper connector;
	/**
	 * 初始化框体时连接数据库
	 */
    public StudentLogin(){
    	super();
    	try
    	{
    		connector = DBhealper.getInstance();  
    		connector.connSQL("root", "1q2w3e", "student", 3307);  // 连接数据库
    	}
    	catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    }
    
    /**
     * 重写登录按钮
     * 数据库匹配帐号密码
     */
    protected void jbtLoginActionPerformed(java.awt.event.ActionEvent evt)
    {                                         
        String user = userTextField.getText();
        String pwd = pwdTextField.getText();
        Boolean isEx = login(user, pwd);
        if (isEx)
        {
            LoginStatus.setText("登录成功");
        }
        else
        {
            LoginStatus.setText("登录失败,帐号密码有误");
        }
    }                                        
                               
    public Boolean login(String user, String pwd)
    {
    	Boolean isEx = null;
    	try
    	{
    		
            isEx = connector.isExist("SELECT * FROM student WHERE st_name=? and st_Password=?", user, pwd);
    	}
    	catch (SQLException e)
    	{
    		e.printStackTrace();
    	}
    	
        return isEx;
    }
    
    public static void main(String args[]) 
    {
    	StudentLogin st = new StudentLogin();
    	st.setVisible(true);
    }
    
}
