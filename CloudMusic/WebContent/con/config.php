<?php
	header('Content-Type:text/html; charset=utf-8');

	define('DB_HOST', 'localhost');
	define('DB_USER', 'root');
	define('DB_PWD', '1q2w3e');
	define('DB_NAME', 'music');

	$connect = mysql_connect(DB_HOST, DB_USER, DB_PWD) or die("数据库连接失败：".mysql_error());
	
	mysql_select_db(DB_NAME) or die(mysql_error());

	@mysql_query("SET NAMEs UTF8") or die('字符集设置错误');

?>