<?php
	
	require './config.php';

	$_email = $_POST['user'];
	$_pwd = md5($_POST['pwd']);

	$query = mysql_query("SELECT email, pwd, id, root FROM app_user WHERE email='{$_email}' AND pwd='{$_pwd}'") 
			
			or die('SQL错误!');

	while ($rows = mysql_fetch_array($query, MYSQL_ASSOC)) {
		echo $rows['id'];
	}

	mysql_close();
?>