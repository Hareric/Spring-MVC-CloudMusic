<?php
	
	sleep(3);	// just for checking the loading operation
	require './config.php';

	if (isset($_POST['name']) && isset($_POST['email']) && isset($_POST['pwd'])) {

		$_name = $_POST['name'];
		$_email = $_POST['email'];
		$_pwd = md5($_POST['pwd']);

		$query1 = "INSERT INTO app_user (email, pwd, regDate, root) 
				VALUES ('{$_email}', '{$_pwd}', NOW(), 0)";

		mysql_query($query1) or die('新增失败！'.mysql_error());

		$result = mysql_query("SELECT email, pwd, id FROM app_user WHERE email='{$_email}' AND pwd='{$_pwd}'");

		while ($rows = mysql_fetch_array($result, MYSQL_ASSOC)) {
			$_uid = $rows['id'];

			$query2 = "INSERT INTO app_info (user_id, name, image)
					VALUES ('{$_uid}', '{$_name}', '../../../src/home/image/profile.jpg')";
			mysql_query($query2) or die('新增失败！'.mysql_error());
			
			echo $_uid;

		}		

		mysql_close();
	}

	
?>