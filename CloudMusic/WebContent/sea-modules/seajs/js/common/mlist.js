/*		播放列表
*	
*/	
define( function ( require, exports, module ) {

	function MList() {

		this.divPlay = '.fix-play';								//播放条
		this.divList = '.play-form';							//播放列表

		this.btnColle = '.play-form .form-title .icon-colle';	//收藏按钮
		this.btnEmpty = '.play-form .form-title .icon-empty';	//清空按钮
		this.btnFClose = '.play-form .form-title .table-close';	//关闭按钮

		this.btnScl = '.play-form .scrol .icon-scl';			//下拉按钮
		this.objList = '.play-form .form-tab ul.mtab';			//列表
		this.list = '.play-form .form-tab ul.mtab li';			//li单元
		this.ulempty = '.play-form .form-tab .empty';

		this.liCOL = 'ul.mtab li .col-2 a.icn-col';			//收藏按钮
		this.liDWN = 'ul.mtab li .col-2 a.icn-dwn';			//下载按钮
		this.liDEL = 'ul.mtab li .col-2 a.icn-del';			//删除按钮

		this.numLi = '.play-ctrl .music-list a';				//曲数
	}

	module.exports = MList;

	MList.prototype = {

		render: function() {
			this._bindUI();
			this._scroll();
		},

		//update 播放列表的各种 size
		_update : function ( str ) {
			var self = this;
			var hg = $( self.list ).height() * $( self.list ).length;
			var	subHg = $( self.objList ).parent('div').height();

			if (hg >= subHg) {
				var p = parseInt( subHg / hg * 1000) / 10; 
				$(self.btnScl).css('height', p + '%');
				$(self.btnScl).parent('.scrol').show();

			} else {
				$(self.btnScl).parent('.scrol').hide();
			}

			var client = parseInt( $(this.objList).css('top') ),
				cell = $(this.list).height() || 30;

			if ( str == 'remove' ) {

				client = client < - cell ? client + cell : 0;

			} else if ( str == 'append' ) {
				// 待定
			}

			var per = parseInt( - client / $(this.objList).height() * 1000) / 10;

			$(this.objList).css('top', client + 'px');

			$(this.btnScl).css('top', per + '%');

			$(self.numLi).html($(self.list).length);

			if ($(self.list).length > 0) {
				$(self.ulempty).hide();

			}else {
				$(self.ulempty).show();
			}

		},

		/*鼠标事件*/
		_bindUI: function() {

			var self = this,
				allowMove = false,
				off = 0;

			$('.fix-bottom').on('mousedown', this.divList, function () {

				return false;

			}).on('click', this.btnEmpty, function() {

				if ($(self.list).length > 0) {
					$(self.objList).empty();
					self._update();
				}

			}).on( 'click', this.btnFClose, function() {

				$( self.divList ).hide();

			}).on({
				mouseover : function() {
					$(this).find('.col').first().addClass('txtOF');
					$(this).find('.col-2 a').show();
				},
				mouseleave : function() {
					$(this).find('.col').first().removeClass('txtOF');
					$(this).find('.col-2 a').hide();
				},
				dblclick : function() {
					$.get('../controller/getMInfo.php?id=' + $(this).attr('data-id'), function(res) {
						var json = $.parseJSON(res)[0];
						$('audio')[0].src = json.src;
						$('audio')[0].play();
					});
				}

			}, this.list ).on('click', this.liCOL, function() {

				if ( !cookie('unique') ) {
					alert('您尚未登录');

				} else {
					var trgid = $(this).parents('li').attr('data-id');
					$.get('../controller/colMusic.php', {
						uid : cookie('unique'),
						mid : trgid
					}, function (result) {
						alert(result);
					});
				}

			}).on('click', this.liDEL, function() {

				$(this).parents('li').remove();
				self._update( 'remove' );

			}).on({

				mousemove: function() {
					var _p,
						_dis,
						_parent = $( self.btnScl ).parent( 'div' ),
						MAX_TOP = $( _parent ).innerHeight() - $( self.btnScl ).innerHeight();

					if ( allowMove ) {
						_dis = event.pageY - $( _parent ).offset().top - off;

						if ( _dis < 0 ) {
							_dis = 0;

						}else if ( _dis > MAX_TOP ) {
							_dis = MAX_TOP;

						}

						$( self.btnScl ).css( 'top', _dis + 'px' );			//滚动按钮移动

						_p = parseInt( _dis / $( _parent ).innerHeight() * $(self.objList).height() ) ;

						$( self.objList ).css( 'top', -_p+'px' );			//页面滚动
					}
				},
				mouseup: function() {
					if ( allowMove ) {
						allowMove = false;
					}	
				},
				mouseover: function() {
					self.tout && clearTimeout( self.tout );
				},
				mouseleave: function() {
					self.tout && clearTimeout( self.tout );
					self.tout = setTimeout( function() {
						allowMove = false;
					},200 );
				}

			}, this.divList).on('mousedown', this.btnScl, function() {

				allowMove = true;
				event.preventDefault();
				off = event.pageY - $( this ).offset().top;
				
			});

			$('audio').on('canplay', function() {
				var isset = false,
					dataSrc = $('audio')[0].src;
				$.get('../controller/getMInfo.php?src=' + dataSrc, function(res) {
					var json = $.parseJSON(res)[0],
						dataID = json.music_id;

					// 检查歌曲列表中有没有存在该歌曲，没有则添加
					for (var i = 0, length = $(self.list).length; i < length; i++) {
						if ($(self.list).eq(i).attr('data-id') == dataID) {
							isset = true;
							$(self.list).children('.abs-stus').hide();
							$(self.list).eq(i).children('.abs-stus').show();
							break;
						}
					}
					if (!isset) {
						$(self.list).children('.abs-stus').hide();		//先全部隐藏
						var html = '<li data-id="' + json.music_id + '">' +
							'<div class="abs-stus" style="display:block"><span class="icn-stus"></span></div>' +
							'<div class="col col-1">' + json.name + '</div>' +
							'<div class="col col-2">' +
							'<a href="javascript:;" class="icn-col" title="收藏"></a>' +
							'<a href="javascript:;" class="icn-dwn" title="下载"></a>' +
							'<a href="javascript:;" class="icn-del" title="删除"></a>' +
							'</div>' +
							'<div class="col col-3">' + json.singer_name + '</div>' +
							'<div class="col col-4">03:23</div>' +
							'</li>';
						$(self.objList).append(html);
						var num = $(self.numLi).text();
						$(self.numLi).text(++num);
						$(self.objList).siblings('.empty').hide();
					}
				});
				
			});

		},
		
		/* 滚轮 */
		_scroll: function() {
			
			var delta = 0,			//偏移量		
				isWheel,			//计算溢出
				_per = 0,			//下拉条 位移
				_ceil = 30;			//位移单位

			if ( document.addEventListener ) {	/*注册事件*/

				document.addEventListener( "DOMMouseScroll", fnWheel, false );//W3C

			}

			document.getElementById('form-tab').onmousewheel = fnWheel;

			/*执行函数*/
			function fnWheel(e) {

				/* 火狐的this指代不明 问题 */
				var _objList = '.play-form .form-tab .mtab';
				var _btnScl = '.play-form .scrol .icon-scl';
				delta = -parseInt($(_objList).css('top')) || 0;

				isWheel = $(_objList).innerHeight() - $(_objList).parent('div').innerHeight();

				if ( wheel(e) === 1 && delta >= 0) {			//向上 && 允许
					delta = delta - _ceil;
					if ( delta < 0 ) { 
						delta = 0;
					}
					$(_objList).css('top', -delta + 'px');
					_per = parseInt( delta / $(_objList).height() * 1000) / 10;
					$(_btnScl).css('top', _per + '%');

				} else if ( wheel(e) === -1 && isWheel > 0) {	//向下 && 允许
					delta = delta + _ceil;
					if ( delta >= isWheel ) {
						delta = isWheel;
					}
					$(_objList).css('top', -delta + 'px');
					_per = parseInt( delta / $(_objList).height() * 1000) / 10;
					$(_btnScl).css('top', _per + '%');

				}
			}


		}


	};



	//helpers

	//滚轮事件
	function wheel( e ) {

		var delta = 0;
		var id = document.getElementById('mtab');

		EVT = e || window.event; 

		if ( EVT.wheelDelta ) {		/*IE Opera*/
			delta = EVT.wheelDelta / 120;

		} else if ( EVT.detail ) {	/*FireFox*/
			delta = -EVT.detail / 3;

		}

		/* 禁用滚轮 */
		if ( EVT.preventDefault ) {
			EVT.preventDefault();

		}
		EVT.returnValue = false;


		if ( delta > 0 ) {
			return 1;		// do something

		} else if ( delta < 0) {
			return -1;		// do another thing

		}


	}



});