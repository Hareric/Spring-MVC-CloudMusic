package CH4.BLL;


import java.sql.SQLException;
import CH4.UI.LoginFrame;
import CH4.DAL.*;
import CH4.Model.*;
/**
* @author Eric_Chan
* @version 2016.10.14
*/
public class Login extends LoginFrame
{
	private DbHealper connector;
	/**
	 * 初始化框体时连接数据库
	 */
    public Login()
    {
    	super();
    	try
    	{
    		connector = DbHealper.getInstance();  
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
        Student stu = new Student();
        stu.setSt_name(user);
        stu.setSt_password(pwd);
        Boolean isEx = User.login(stu);
        
        if (isEx)
        {
            LoginStatus.setText("登录成功");
        }
        else
        {
            LoginStatus.setText("登录失败,帐号密码有误");
        }
    }                                        
                               

}
