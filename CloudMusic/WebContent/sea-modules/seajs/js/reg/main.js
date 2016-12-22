define ( function ( require ) {
	
	require('../common/cookie');

	var $ = require('jquery');

	var Reg = require('./reg');
	var R = new Reg();
	R.render();

	var Music = require('../common/music');
	var M = new Music();
	M.render();

});