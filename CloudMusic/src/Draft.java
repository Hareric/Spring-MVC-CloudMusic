import java.sql.SQLException;
import java.util.regex.*;

import com.mysql.jdbc.ResultSet;

import model.db.Connector;
import model.db.DbHelper;

public class Draft {
	public static void main(String[] args) throws SQLException {
		String url = "http://localhost:8080/CloudMusic/index/colMusic?uid=1320651455&mid=00";
		Pattern uidPattern = Pattern.compile("uid=(\\d*)&");
		Pattern midPattern = Pattern.compile("&mid=(\\d*)");
		String mid = null, uid = null;
		Matcher m = midPattern.matcher(url);
		if (m.find()) {
			mid = m.group(1);
		}
		m = uidPattern.matcher(url);
		if (m.find()) {
			uid = m.group(1);
		}
		System.out.print(mid);

	}
}
