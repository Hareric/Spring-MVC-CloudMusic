/*
*
*/

function cookie( name, value, exp ) {
				//名	值	有效日期

	if ( exp && value && name ) {	//存在传参的exp 则设置有效日期
		var objDate = new Date();
		objDate.setDate( objDate.getDate() + exp );

		document.cookie = name + '=' + value + ';expires=' + objDate;

	} else if ( value && name ) {				//存在传参的value 则只设置值，默认会话结束后消除cookie
		document.cookie = name + '=' + value;

	}else if ( name ) {							//若只传了name 则返回cookie
		var a,
			arr = document.cookie.split(';');
		for (var i = 0; i < arr.length; i++) {
			a = arr[i].split('=');

			if ( a[0] == name ) {
				return a[1];
			}	
		}
		return '';

	} else {
		return 'lack of variable';
	}
	
}

function removeCookie( name ) {		//设置有效日期为-1天，来删除cookie
	cookie( name, 1, -1 );
}
