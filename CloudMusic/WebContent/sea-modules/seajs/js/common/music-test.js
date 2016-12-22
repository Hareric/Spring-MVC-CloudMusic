/*	Created by Zenaro
*	Jan 12th
*
*
*		播放条
*/
define( function( require, exports, module ) {

	function Music() {
		this.super = '.fix-bottom';				//播放条
		this.btnLock = '.fix-lock a';			//按钮-锁
		this.superPlay = '.fix-play';			//隐藏条

		this.btnPrv = '#play-btns .prv';		//按钮 上一曲
		this.btnPS = '#play-btns #data-ps';		//按钮 播放暂停
		this.btnNxt = '#play-btns .nxt';		//按钮 下一曲

		this.htmlImg = '#play-ing .play-head a img';		//html 图片
		this.htmlName = '#play-ing .ptitle a.title';		//html 曲名
		this.htmlSinger = '#play-ing .ptitle a.singer';		//html 歌手

		this.objSPro = '#play-ing .pbar .barbg';			//歌曲总进度条
		this.objSBuffered = '#play-ing .pbar .rdy'			//灰色缓冲条
		this.objSCurPro = '#play-ing .pbar .cur';			//歌曲当前进度条
		this.dotSong = '#play-ing .pbar #data-cur';			//圆点 进度条
		this.dotBuf = '#play-ing .pbar #data-cur i';		//进度条圆点loading
		this.htmlCur = '#play-ing .pbar .clock i';			//当前时间
		this.htmlDur = '#play-ing .pbar .clock em';			//歌曲长度

		this.btnCol = '#play-oper .icon-colle';			//收藏按钮
		this.btnShr = '#play-oper .icon-share';			//分享按钮

		this.btnVol = '#play-ctrl .icon-vol';			//声音按钮
		this.divVPro = '#play-ctrl .cbar';				//音量调节框
		this.objVPro = '#play-ctrl .cbar .barbg';		//音量总进度条
		this.objVCurPro = '#play-ctrl .cbar .cur';		//音量当前进度条
		this.dotVol = '#play-ctrl .cbar #ctrl-cur';		//音量调节圆点

		this.btnLoop = '#play-ctrl #data-lop';			//循环按钮
		this.btnList = '#play-ctrl .icon-list';			//列表按钮

		this.divList = '.play-form';						//显示播放列表
		this.btnScl = '.play-form .scrol .icon-scl';		//下拉按钮
		this.objList = '.play-form .form-tab ul.mtab';		//列表

		this.typeOfLoop = -1;							// -1顺序播放、0循环播放、1随机播放
	}

	module.exports = Music;

	Music.prototype.render = function() {	// 外部接口

		this._init();
		this._bind();

	}

	// 初始化数据
	Music.prototype._init = function() {
		this.isLock = false;							//播放条隐藏状态
		this.player = $( '.fix-bottom audio' )[0];		//audio对象
		this.player.volume = 0.3;						//音量
		this.player.controls = false;					//隐藏控件
		this.player.loop = false;						//禁止循环

		var self = this;

		this.timer = setInterval( function() {

			if ( !!self.player.readyState ) {		
				$( self.dotBuf ).css( 'visibility', 'hidden' );			//隐藏gif
				$( self.htmlDur ).html( parseTime( self.player.duration ) );	//显示总时间
				$( self.objVCurPro ).css( 'height', self.player.volume*100+'%' );//设置音量长度

				clearInterval( self.timer );				//结束
			}

		}, 200);

		return this;
	}

	// 播放
	Music.prototype._play = function() {
		var self = this;
		this.timer && clearInterval( this.timer );
		this.bufferedTimer && clearInterval( this.bufferedTimer );
		
		if ( !!this.player.readyState ) {
		// if ( this.player.networkState === 1) {
			$( self.dotBuf ).css( 'visibility', 'hidden' );
			this.player.currentTime = parseInt( parsePer( this.objSCurPro, 'x' )*this.player.duration );
			
			this.player.play();
			$( this.btnPS ).attr( 'class', 'pas' ).attr( 'title', '暂停' );

			$( this.htmlCur ).html( parseTime( this.player.currentTime ) );//显示当前时间
			$( this.htmlDur ).html( parseTime( this.player.duration ) );	//显示总时间

			this.timer = setInterval( onward, 1000 );

			this.bufferedTimer = setInterval(function () {
				//获取已缓冲部分的TimeRanges对象
				var timeRanges = self.player.buffered;
				//获取缓存时间
				var timeBuffered = timeRanges.end(0);
				$('.footer .recruit p').html(timeBuffered)
				//计算缓存百分比
				var bufferedPercent = parseInt( timeBuffered / self.player.duration * 1000 ) / 10;
				//设置长度
				$(self.objSBuffered).css('width', bufferedPercent + '%');
				// alert('a')
				
				if (bufferedPercent >= 100) {
					self.bufferedTimer && clearInterval(self.bufferedTimer);
				}
			},5000);
			
		} else {
			$( self.dotBuf ).css( 'visibility', 'visible' );
		}

		function onward() {		//定时函数

			var per = parseInt( self.player.currentTime/self.player.duration*1000 )/10;
			$( self.objSCurPro ).css( 'width', per+'%' );
			$( self.htmlCur ).html( parseTime( self.player.currentTime ) );

			if ( $(self.objSCurPro).width() >= $(self.objSBuffered).width() - 5 ) {
				$( self.dotBuf ).css( 'visibility', 'visible' );
			} else {
				$( self.dotBuf ).css( 'visibility', 'hidden' );
			}

			if( self.player.ended && !self.player.loop ) {
				self._reload();
			}
		}

	}

	// 暂停
	Music.prototype._pause = function() {
		this.player.pause();
		$( this.btnPS ).attr( 'class', 'ply' ).attr( 'title', '播放' );
		clearInterval( this.timer );

	}

	// 重载
	Music.prototype._reload = function( id ) {
		var self = this,
			arrLi = $(this.objList).children('li');

		this.timer && clearInterval( this.timer );
		this.bufferedTimer && clearInterval( this.bufferedTimer );

		if (!this.player) {
			this.player = $( '.fix-bottom audio' )[0];
		}

		var intRand;
		if ( !id && this.typeOfLoop == 1 ) {
			intRand = Math.floor( ( Math.random() * $(arrLi).length ) );	//随机播放
			intRand = $(arrLi).eq(intRand).attr('data-id');

		} else if ( !id && (this.typeOfLoop == -1 || this.typeOfLoop == 0) ) {
			intRand =  getMusicI() + 1;		//顺序播放
			intRand = intRand > $(arrLi).length - 1 ? 0 : intRand;
			intRand = $(self.objList).children('li').eq(intRand).attr('data-id');
		}
		

		this.player.dataID = id || intRand;

		$(arrLi).children('.abs-stus').remove();
		$(arrLi).eq( getMusicI() ).children('.col-1').before('<div class="abs-stus"><span class="icn-stus"></span></div>');

		$.get('../controller/getMInfo.php', {id : this.player.dataID}, function (result) {
			var json = $.parseJSON( result );
			$.each(json, function ( index, value ) {
				$( self.htmlName ).html( value.name );
				$( self.htmlSinger ).html( value.master );
				$(self.super).trigger('mouseover');
				$.ajax({
					url: value.src,
					success: function () {
						self.player.src = value.src;
					},
					error: function(err) {
						alert('该音乐文件已不存在');
					}
				});
				

			});
		});
		
		$( this.objSCurPro ).css( 'width', '0' );
		$( this.dotBuf ).css( 'visibility', 'visible' );

		this.timer = setInterval( function() {
			if ( !!self.player.readyState ) {
			// if ( self.player.networkState == 1 ) {
				$( self.dotBuf ).css( 'visibility', 'hidden' );
				self._play();
			}
		},200 );


		function getMusicI() {		//获取正在播放的li
			for(var i = $(arrLi).length - 1; i >= 0; i--) {
				var data = $(arrLi).eq(i).attr('data-id');
				if (data == this.player.dataID) {
					return i;
				}
			}

		}

	}

	// ----- _bind -----
	Music.prototype._bind = function() {

		var off,				//偏移量
		 	self = this,
			allowMove = false;	

		//***页面委托
		$( 'body' ).on({
			mouseover: function() {
				if (!self.isLock) {
					self.tHide && clearTimeout(self.tHide);
					self.tHide = setTimeout(function() {
						$(self.superPlay).show(200);
					},100);
					
				}
			},
			mouseleave: function() {
				if (!self.isLock) {
					self.tHide && clearTimeout(self.tHide);
					self.tHide = setTimeout(function() {
						$(self.superPlay).hide(500);
						$(self.divList).css('display', 'none');
					}, 800);
				}
			}
		}, this.super );

		//***滚动条委托
		$( this.super ).on('click', this.btnLock, function() {

			if ( !self.isLock ) {
				$(this).attr('class', 'lock');
				self.isLock = true;

			} else {
				$(this).attr('class', 'unlock');
				self.isLock = false;
			}

		}).on( 'click', this.btnPS, function() {	//播放/暂停

			if ( self.player.paused ) {
				self._play();

			} else {
				self._pause();
			}	

		}).on( 'click', this.btnPrv, function() {		//上一曲

			self._reload();

		}).on( 'click', this.btnNxt, function() {		//下一曲

			self._reload();

		}).on({

			click: function() {					//点击 调整进度

				$( self.objSCurPro ).css( 'width', jumpTo( this, 'x' )+'%' );
				if ( !self.player.paused ) {
					self._play();
				}

			},
			mousemove: function() {				// 拖动	
				var _p,
					_dis,
					_parent = $( self.objSCurPro );

				if ( allowMove ) {
					_dis = event.pageX - $( _parent ).offset().left;
					_p = parseInt( _dis / $( _parent ).parent( 'div' ).innerWidth()*1000 )/10;
					
					if ( _p >= 100 ) {
						_p = 100;
					} 
					
					$( _parent ).css( 'width', _p + '%' );
					if ( !self.player.paused ) {
						self.timer && clearInterval(self.timer);
						self.timer = setInterval(function() {
							self._play();
						}, 100);
					}
				}
			},
			mouseup: function() {
				if ( allowMove ) {
					allowMove = false;
				}
			},
			mouseleave: function() {
				if ( allowMove ) {
					allowMove = false;
				}
			}

		}, this.objSPro).on('mousedown', this.dotSong, function(e) {		//歌曲进度条拖动
				if ( !self.player.paused ) {
					self.timer && clearInterval( self.timer );
				} 
				event = e || window.event;
				allowMove = true;
				event.preventDefault();				//阻止默认事件
				

		}).on('click', this.btnCol, function() {

			if ( !cookie('unique') ) {
				alert('您尚未登录');

			} else if ( self.player.dataID ){
				var trgid = self.player.dataID;
				$.get('../controller/colMusic.php', {
					uid : cookie('unique'),
					mid : trgid,
				}, function (result) {
					alert(result);
				});
			}

		}).on('click', this.btnShr, function() {
			alert('share');
		}).on( 'click', this.btnVol, function() {

			$( self.divVPro ).toggle();

			self.tout && clearTimeout( self.tout );
			self.tout = setTimeout( function() {

				$( self.divVPro ).hide();

			}, 2000 );

		}).on({
			mouseover: function() {

				self.tout && clearTimeout( self.tout );

			},
			mouseleave: function() {
				self.tout && clearTimeout( self.tout );
				self.tout = setTimeout( function() {

					$( self.divVPro ).hide();

				}, 500 );

			}
		}, this.divVPro ).on( 'click', this.objVPro, function() {

			$( self.objVCurPro ).css( 'height', 100 - jumpTo( this, 'y' ) + '%' );
			self.player.volume = parseInt( parsePer( self.objVCurPro, 'y' )*100 )/100;

		}).on({
			mousedown: function(e) {
				event = e || window.event;
				allowMove = true;
				event.preventDefault();			
			},
			mousemove: function() {

				var _dis,
					_p;
					_parent = $( this ).parent( 'div' );

				if ( allowMove ) {
					_dis = event.pageY - $( _parent ).parent( 'div' ).offset().top;
					_p = 100 - ( parseInt( _dis / $( _parent ).parent( 'div' ).innerHeight()*1000 )/10 );

					if ( _p >= 100 )  _p = 100;
					$( _parent ).css( 'height', _p + '%' );
				}
			},
			mouseup: function() {
				if ( allowMove ) {
					allowMove = false;
				}
			}, 
			mouseleave: function() {
				if ( allowMove ) {
					allowMove = false;
				}
			}

		}, this.dotVol ).on( 'click', this.btnLoop, function() {

			this.tout || clearTimeout( this.tout );

			if ( self.typeOfLoop == -1 ) {
				self.player.loop = true;
				$( this ).attr( 'class', 'icon-one' );
				self.typeOfLoop = 0;
				tips('单曲循环');

			}else if ( self.typeOfLoop == 0 ){
				self.player.loop = false;
				$( this ).attr( 'class', 'icon-shuffle' );
				self.typeOfLoop = 1;
				tips('随机播放');

			} else {
				self.player.loop = false;
				$( this ).attr( 'class', 'icon-loop' );
				self.typeOfLoop = -1;
				tips('顺序播放');

			}

			function tips ( str ) {
				var h = str || '播放';
				$( '.lp-tip').html( h ).show();
				this.tout = setTimeout( function() {
					$( '.lp-tip').hide();
				}, 2000);
			}

		}).on( 'click', this.btnList, function() {

			$( self.divList ).toggle();

		});


	}		//----- Music.prototype._bind 结束 -----





	// helpers
	
	//数字-时间 转换
	function parseTime( time ) {
		var min = parseInt( time / 60 );
		var sec = parseInt( time % 60 );
		if ( min < 10 ) {
			min = '0' + min; 
		}
		if ( sec < 10 ) {
			sec = '0' + sec;
		}
		return '' + min + ':' + sec;
	}

	//点击后 跳至某位置
	function jumpTo( obj, str ) {
		var dis;
		switch( str ) {
			case 'x': {
				dis = event.pageX - $( obj ).offset().left;
				dis = parseInt( dis / $( obj ).innerWidth() * 1000 ) / 10;
				break;
			}
			case 'y': {
				dis = event.pageY - $( obj ).offset().top;
				dis = parseInt( dis / $( obj ).innerHeight() * 1000 ) / 10;
				break;
			}
			default: ;
		} 

		if ( dis > 100 ) {
			dis = 100;

		} else if ( dis < 0 ) {
			dis = 0;

		}

		return dis;
	}

	// 百分比 转换
	function parsePer( obj, str ) {
		var fra = 1;
		var num = 1;
		switch( str ) {
			case 'x': {
				fra = parseInt( $( obj ).css( 'width' ) );
				num = parseInt( $( obj ).parent( 'div' ).css( 'width' ) );
				break;
			}
			case 'y':{
				fra = parseInt( $( obj ).css( 'height' ) );
				num = parseInt( $( obj ).parent( 'div' ).css( 'height' ) );
				break;
			}
			default: ; 
		}
		var result = fra / num;
		if ( result > 1 ) result = 1;
		return result;
	}

	//获取子元素 所占百分比
	function getPercent( obj, str ) {
		var _parent = $( obj ).parent( 'div' );

			fra = $( obj ).offset().top-$( _parent ).offset().top;
			num = $( _parent ).innerHeight();

		return parseInt( fra/num*1000 )/10;
	}


});