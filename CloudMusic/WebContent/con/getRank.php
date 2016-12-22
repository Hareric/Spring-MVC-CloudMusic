<?php

	require './config.php';

	if (isset($_GET['data'])) {
		$cid = $_GET['data'] + 1;
		$query = "SELECT name, music_id FROM app_musicRclass NATURAL JOIN app_Music NATURAL JOIN app_Class WHERE class_id='{$cid}' ORDER BY listeners DESC LIMIT 10";
		$result = @mysql_query($query) or die("SQL语句有误".mysql_error());

		$json = '';
		while (!!$row = mysql_fetch_array($result, MYSQL_ASSOC)) {
			foreach ( $row as $key => $value ) {
				$row[$key] = urlencode(str_replace("\n","", $value));
			}
			$json .= urldecode(json_encode($row)).',';
		}
		echo '['.substr($json, 0, strlen($json) - 1).']';

		mysql_close();
	}
?>