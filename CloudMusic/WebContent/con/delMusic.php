<?php
	require './config.php';

	if (isset($_GET['uid']) && isset($_GET['mid'])) {	// delete some collection record
		
		$uid = $_GET['uid'];
		$mid = $_GET['mid'];
		$result = mysql_query("DELETE FROM app_collection WHERE user_id = '{$uid}' AND music_id = '{$mid}'");

		if ( !!$result ) {
			echo "删除成功";
		}
	}
?>