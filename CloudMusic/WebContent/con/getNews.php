<?php

	require './config.php';

	$query = "SELECT title, id, pubDate FROM app_News LIMIT 7";

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
?>