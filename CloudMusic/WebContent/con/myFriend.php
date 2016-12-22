<?php

	require './config.php';

	if (isset($_GET['uid'])) {
		$uid = $_GET['uid'];
		
		$query = mysql_query("SELECT friend_id, name FROM app_friends, app_info WHERE friend_id=app_info.user_id AND app_friends.user_id='{$uid}'") or die('MySQL错误'.mysql_error());

		$json = '';

		while( !!$row = mysql_fetch_array($query, MYSQL_ASSOC)) {

			foreach ($row as $key => $value) {
				$row[$key] = urlencode(str_replace("\n", "", $value));
			}
			$json .= urldecode(json_encode($row)).',';
		}

		echo '['.substr($json, 0, strlen($json)-1).']';

		mysql_close();

	}

	

?>