package CH4.DAL;
/**
* @author Eric_Chan
* @version 2016.10.14
*/
import java.sql.SQLException;
import CH4.DAL.DbHealper;
import CH4.Model.*;

public class User {
    public static Boolean login(Student stu)
    {
    	DbHealper connector = DbHealper.getInstance();
    	Boolean isEx = null;
    	String user = stu.getSt_name();
    	String pwd = stu.getSt_Password();
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
}
