<?php
	require './config.php';
	
	if (isset($_GET['id'])) {
		$mid = $_GET['id'];
		$query = mysql_query("SELECT name, content, user_id FROM app_comment NATURAL JOIN app_info WHERE music_id = '{$mid}' ORDER BY time DESC") or die("连接数据库失败".mysql_error());

		$json = '';

		while (!!$row = mysql_fetch_array($query, MYSQL_ASSOC)) {
			foreach ($row as $key => $value) {
				$row[$key] = urlencode(str_replace("\n", "", $value));
			}
			$json .= urldecode(json_encode($row)).',';
		}

		echo '['.substr($json, 0, strlen($json)-1).']';

		mysql_close();
	} 
	
?>