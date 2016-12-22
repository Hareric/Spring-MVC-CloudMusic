<?php

	require './config.php';

	if (isset($_POST['uid'])) {
		$uid = $_POST['uid'];
		
		$query = mysql_query("SELECT music_id, name, singer_name FROM app_collection NATURAL JOIN app_Music NATURAL JOIN app_singerRmusic NATURAL JOIN app_Singer WHERE user_id='{$uid}' ORDER BY colDate DESC") or die('MySQL错误'.mysql_error());

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