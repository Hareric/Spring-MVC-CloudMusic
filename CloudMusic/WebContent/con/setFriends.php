<?php
	require './config.php';

	if (isset($_GET['my_id']) && isset($_GET['f_id'])) {
		
		$my_id = $_GET['my_id'];
		$f_id = $_GET['f_id'];

		$query = mysql_query("SELECT user_id, friend_id FROM app_friends WHERE user_id='{$my_id}' AND friend_id='{$f_id}'") or die('SQL错误'.mysql_error());

		if (!$rows = mysql_fetch_array($query, MYSQL_ASSOC)) {

			$insert = mysql_query("INSERT INTO app_friends (user_id, friend_id) VALUES ('{$my_id}', '{$f_id}')") or die('SQL错误'.mysql_error());
			echo "添加成功";

			
		} else {
			echo "你们已经是好友了";
		}

		mysql_close();
	}

?>