define( function ( require, exports, module ) {

	function Result () {
		this.mid = 0;
		this.lrc = null;
	}

	module.exports = Result;

	Result.prototype.render = function(){
		this._init();
		this._bind();
	};

	Result.prototype._init = function() {
		var self = this;

		var url = location.href;

		if ( url.indexOf("?") !== -1) {
			var subStr1 = url.indexOf("="),
				data_id = url.substring( subStr1 + 1,  url.length );

			this.mid = data_id;

			$.get('../controller/getMInfo.php?id=' + data_id, function ( result ) {	//get歌曲信息
            	var json = $.parseJSON(result)[0];
				$('.main .main-title h3').html(json.name);
				$('.main .main-title span').html(json.singer_name);
				$('.play-ing .ptitle a.title').html(json.name);
				$('.play-ing .ptitle a.singer').html(json.singer_name);
				$('.fix-bottom audio')[0].src = json.src;
				$('audio')[0].play();
				$('.fix-bottom').trigger("mouseover");
			});

			$.post('../controller/getMInfo.php', {id: data_id}, function (res) {	//get歌词
				var html = '';
				if (res.length > 6) {
					self.lrc = parseLyric(res);
					for (var i = 0, length = self.lrc.length; i < length; i++) {
						html += '<p>' + self.lrc[i][1] + '</p>';
					}
				} else {
					self.lrc = res;
					html = '<p>' + self.lrc + '</p>';
				}
				$('.content').append(html);
			});

			$.get('../controller/getComment.php', {id: data_id}, function(res) {	// get 评论
				var html = '',
					json = $.parseJSON(res);
				$.each(json, function(index, value) {
					html += '<div class="cell" data-id="' + value.user_id +'">' +
                				'<p>' +
                    				'<span class="user-name">' + value.name + '：</span>' +
                    				'<span class="user-comment">'+ value.content + '</span>' +
                				'</p>' +
                				'<i class="btn-add"> + 加为好友</i>' +
            				'</div>';
				});
				$('.comment').append(html);
			})
		}
		
	};

	Result.prototype._bind = function() {
		var self = this,
			lrc_i = 0;		// 第i行歌词

		$('body').on('click', '.main a.toggle', function() {

			if ($('.main .content').hasClass('txtOF')) {
				$(this).html('收起');
				$('.main .content').removeClass('txtOF');

			} else {
				$(this).html('展开');
				$('.main .content').addClass('txtOF');
			}

		}).on('focus', 'textarea', function() {

			$(this).text('');

		}).on('click', 'input[type=submit]', function() {

			var html = '',
				user_id = cookie('unique'),
				txt = $.trim($('textarea').val());
			$.get('../controller/setComment.php', {uid: user_id, mid: self.mid, com: txt}, function(res) {
				if (res === 'ok') {
					html += '<div class="cell" data-id="' + user_id +'">' +
	            				'<p>' +
	                				'<span class="user-name">' + $('.user-memb h4').text() + '：</span>' +
	                				'<span class="user-comment">'+ txt + '</span>' +
	            				'</p>' +
	            				'<i class="btn-add"> + 加为好友</i>' +
	        				'</div>';
				}
				$('.comment .cell').eq(0).after(html);
				$('textarea').val('');
			});

		}).on('click', '.comment .cell i.btn-add', function() {
			var my_id = cookie('unique'),
				f_id = $(this).parent('.cell').attr('data-id');
			$.get('../controller/setFriends.php', {my_id, f_id}, function(res) {
				alert(res);
			});
		});

		$('audio').on('timeupdate', function() {			// 歌词滚动
			var length = self.lrc.length;
			
			if (this.currentTime >= self.lrc[lrc_i][0]) {
				$('.main .content p').removeClass('active').eq(lrc_i).addClass('active');
				lrc_i++;
			}
		}).on('seeked', function() {
			var temp = 0;

			while (this.currentTime > self.lrc[temp][0]) {
				temp++;
			}
			lrc_i = temp;
			$('.main .content p').removeClass('active').eq(lrc_i).addClass('active');
		});
	}

	// helpers

	function parseLyric(text) {
	    //将文本分隔成一行一行，存入数组
	    var lines = text.split('\n'),
	        //用于匹配时间的正则表达式，匹配的结果类似[xx:xx.xx]
	        pattern = /\[\d{2}:\d{2}.\d{2}\]/g,
	        //保存最终结果的数组
	        result = [];
	    //去掉不含时间的行
	    while (!pattern.test(lines[0])) {
	        lines = lines.slice(1);
	    };
	    //上面用'\n'生成生成数组时，结果中最后一个为空元素，这里将去掉
	    lines[lines.length - 1].length === 0 && lines.pop();
	    lines.forEach(function(v /*数组元素值*/ , i /*元素索引*/ , a /*数组本身*/ ) {
	        //提取出时间[xx:xx.xx]
	        var time = v.match(pattern),
	            //提取歌词
	            value = v.replace(pattern, '');
	        //因为一行里面可能有多个时间，所以time有可能是[xx:xx.xx][xx:xx.xx][xx:xx.xx]的形式，需要进一步分隔
	        time.forEach(function(v1, i1, a1) {
	            //去掉时间里的中括号得到xx:xx.xx
	            var t = v1.slice(1, -1).split(':');
	            //将结果压入最终数组
	            result.push([parseInt(t[0], 10) * 60 + parseFloat(t[1]), value]);
	        });
	    });
	    //最后将结果数组中的元素按时间大小排序，以便保存之后正常显示歌词
	    result.sort(function(a, b) {
	        return a[0] - b[0];
	    });
	    return result;
	}
});