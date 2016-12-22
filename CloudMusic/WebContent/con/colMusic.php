<?php
	require './config.php';

	if (isset($_GET['uid']) && isset($_GET['mid'])) { //  收藏某首歌曲
		$uid = $_GET['uid'];
		$mid = $_GET['mid'];

		$query = mysql_query("SELECT id FROM app_collection WHERE user_id = '{$uid}' AND music_id = '{$mid}' ") or die('SQL错误');

		if ($rows = mysql_fetch_array($query, MYSQL_ASSOC)) {
			echo '已收藏过该歌曲';
			
		} else{
			$result = mysql_query("INSERT INTO app_collection (user_id, music_id, colDate) VALUES ('{$uid}', '{$mid}', NOW())");
			if ( !!$result ) {
				echo "收藏成功";
			}
		}
		mysql_close();

	} else if (isset($_POST['uid']) && isset($_POST['type'])) {	// 收藏整个类型的歌曲
		$type = $_POST['type'];
		$uid = $_POST['uid'];

		$query = mysql_query("SELECT music_id FROM app_Class NATURAL JOIN app_musicRclass NATURAL JOIN app_music WHERE class_name='{$type}' ORDER BY listeners DESC LIMIT 10");

		while (!!$row = mysql_fetch_array($query, MYSQL_ASSOC)) {
			$mid = $row['music_id'];

			$sql = mysql_query("SELECT id FROM app_collection WHERE user_id = '{$uid}' AND music_id = '{$mid}' ") or die('SQL错误');
			if (!$r = mysql_fetch_array($sql, MYSQL_ASSOC)) {	// 不存在则插入
				$result = mysql_query("INSERT INTO app_collection (user_id, music_id, colDate) VALUES ('{$uid}', '{$mid}', NOW())");
			}
		}
		echo "全部收藏~";
		mysql_close();
	}
	


?>