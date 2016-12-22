<?php
	require './config.php';

	if (isset($_GET['uid'])) {
		$uid = $_GET['uid'];
		$mid = $_GET['mid'];
		$txt = $_GET['com'];

		$query = mysql_query("INSERT INTO app_comment (user_id, music_id, content, time) VALUES('{$uid}', '{$mid}', '{$txt}', NOW())");
		echo "ok";

		mysql_close();
	} 
	
?>