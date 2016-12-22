<?php
	
	require './config.php';

	if (isset($_POST['id'])) {		// just get lyric 
		$id = $_POST['id'];

		$query = "SELECT lyric FROM app_singerRmusic NATURAL JOIN app_Music NATURAL JOIN app_Singer WHERE music_id='{$id}'";

		$result = @mysql_query($query) or die("SQL语句有误".mysql_error());

		while (!!$row = mysql_fetch_array($result, MYSQL_ASSOC)) {
			echo $row['lyric'];
		}

	} else if (isset($_GET['id'])) {		// get music info, include name, singer_name, src
		$id = $_GET['id'];
		$query = "SELECT name, singer_name, src, music_id FROM app_singerRmusic NATURAL JOIN app_Music NATURAL JOIN app_Singer WHERE music_id='{$id}'";

		$result = @mysql_query($query) or die("SQL语句有误".mysql_error());

		$json = '';

		while (!!$row = mysql_fetch_array($result, MYSQL_ASSOC)) {
			foreach ( $row as $key => $value ) {
				$row[$key] = urlencode(str_replace("\n","", $value));
			}
			$json .= urldecode(json_encode($row)).',';
		}
		
		echo '['.substr($json, 0, strlen($json) - 1).']';

	} else if (isset($_GET['src'])) {
	        $src = $_GET['src'];

	        $query = "SELECT name, singer_name, src, music_id FROM app_singerRmusic NATURAL JOIN app_Music NATURAL JOIN app_Singer WHERE src='{$src}'";

	        $result = @mysql_query($query) or die("SQL语句有误".mysql_error());

	        $json = '';

	        while (!!$row = mysql_fetch_array($result, MYSQL_ASSOC)) {
	            foreach ( $row as $key => $value ) {
	                $row[$key] = urlencode(str_replace("\n","", $value));
	            }
	            $json .= urldecode(json_encode($row)).',';
	        }

	        echo '['.substr($json, 0, strlen($json) - 1).']';

    	} else if (isset($_GET['name'])) {
		$music = $_GET['name'];

		$query = "SELECT name, singer_name, lyric FROM app_singer-music JOIN app_Singer JOIN app_Music WHERE name like '%" .$music. "%' LIMIT 1";

		$result = @mysql_query($query) or die("SQL语句有误".mysql_error());

		while (!!$row = mysql_fetch_array($result, MYSQL_ASSOC)) {
			echo $row['lyric'];
		}
	}

	mysql_close();
?>