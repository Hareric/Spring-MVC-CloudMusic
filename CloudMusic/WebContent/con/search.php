<?php
	require './config.php';

	if ( isset($_POST['content']) ) {

		$music = $_POST['content'];

		$sql = mysql_query("SELECT name, music_id, singer_name, src FROM app_singerRmusic NATURAL JOIN app_Singer NATURAL JOIN app_Music where (name like '%" .$music. "%') or (singer_name like '%" .$music. "%') ORDER BY name, singer_name DESC LIMIT 10");

		$json = '';

		while ( !!$row = mysql_fetch_array($sql, MYSQL_ASSOC) ) {
			
			foreach ($row as $key => $value) {
				$row[$key] = urlencode(str_replace("\n", "", $value));
			}
			$json .= urldecode(json_encode($row)).',';

		}

		if ( strlen($json) == 0) {
			echo 'false';

		} else {
			echo '['.substr( $json, 0,  strlen($json)-1 ).']';
		}

		mysql_close();
	}

?>