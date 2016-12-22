define(function(require) {

	require('../common/cookie');

	var $ = require('jquery');

	var Dialog = require('../common/dialog');
	var d = new Dialog();
	d.render();

	var Music = require('../common/music');
	var m = new Music();
	m.render();

	var MList = require('../common/mlist');
	var l = new MList();
	l.render();

	var Result = require('./result');
	var r = new Result();
	r.render();
});