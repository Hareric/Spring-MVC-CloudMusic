define ( function ( require ) {

	var $ = require('jquery');

	require('../common/cookie');

	var Login = require('./login');
	var sl = new Login();
	sl.render();

	var Music = require('../common/music');
	var m = new Music();
	m.render();

	var MList = require('../common/mlist');
	var l = new MList();
	l.render();

});




