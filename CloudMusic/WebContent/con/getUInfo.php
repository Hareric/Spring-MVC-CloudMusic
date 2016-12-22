<?php

	require './config.php';

	$id = $_POST['id'];

	$query = mysql_query("SELECT name FROM app_info WHERE user_id='{$id}'") or die('SQL错误!'.mysql_error());

	if ($rows = mysql_fetch_array($query, MYSQL_ASSOC)) {
		echo $rows['name'];
	} else{
		echo 'false';
	}

?>