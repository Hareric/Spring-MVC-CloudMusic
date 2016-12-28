define ( function ( require, exports, module ) {

	function Reg () {
		this.name = 'form input[name=name]';
		this.email = 'form input[name=email]';
		this.pwd = 'form input[name=pwd]';
		this.repwd = 'form input[name=repwd]';
		this.submit = 'form input[type=submit]';
		this.tips = 'form .tips';
	}

	module.exports = Reg;

	Reg.prototype.render = function () {
		this._init();
		this._bind();
	}

	Reg.prototype._init = function () {

	}

	Reg.prototype._bind = function () {

		var self = this;

		$('.wrap-in').on('focus', this.name, function () {

			$(self.tips).html('');

		}).on('focus', this.email, function () {

			$(self.tips).html('');

		}).on('focus', this.pwd, function () {

			$(self.tips).html('');

		}).on('focus', this.repwd, function () {

			$(self.tips).html('');

		}).on('click', this.submit, function ( e ) {
			e = e || event;
			e.preventDefault();

			if ( !!$.trim($(self.name).val()) && !!$.trim($(self.name).val()) && !!$.trim($(self.pwd).val()) && !!$.trim($(self.repwd).val()) ) {

				$.ajax({
					url : '/CloudMusic/userRegister',
					type : 'POST',
					data : $('form').serialize(),
					success : function(response) {
						if(/^\d+$/.test(response)){  
							cookie('unique', response);
							$(self.tips).html("注册成功");
							window.location.href = './index';  
							
					    }else{  
					    	$(self.tips).html(response);
					    }  
					}

				});

			} else {
				$(self.tips).html('表单未完成，请继续填写');

			}

		});
	}
});