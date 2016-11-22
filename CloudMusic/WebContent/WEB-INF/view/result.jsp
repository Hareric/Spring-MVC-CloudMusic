<!DOCTYPE html>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%> 
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Lyric~音乐工厂</title>
    <link rel="stylesheet" href="css/frame.css">
    <link rel="stylesheet" href="css/music.css">
    <link rel="stylesheet" href="css/result.css">
    <link rel="shortcut icon" type="image/x-icon" href="image/favicon.ico" />
</head>
<body>	
<div class="header">
	<div class="header-center center">
		<h1>Lyric 音乐场</h1>
		<ul class="nav">
			<li><a href="./index" class="active">发现音乐</a></li>
            <li><a href="./my">我的音乐</a></li>
            <li><a href="javascript:;">热门专辑</a></li>
            <li><a href="javascript:;">在线好友</a></li>
		</ul>
		<div class="top-tool">
			<div class="top-search">
                <i class="icn-search"></i>
                <input type="text" placeholder="单曲/歌手/专辑/歌单/用户">
                <ul class="result"></ul>
            </div>
			<div class="top-user">
                <a href="javascript:;" class="user-login">登录</a>
                <div class="user-memb">
                    <h4>用户</h4>
                    <ul class="slide-down">
                        <li><a href="./my">我的主页</a></li>
                        <li><a href="javascript:;" class="logout">退出</a></li>
                    </ul>
                </div>
            </div>	
		</div>
	</div>
	<div class="sub-list">
		<div class="center">
			<ul class="sub-nav">
				<li><a class="active" href="javascript:;">推荐</a></li>
				<li><a href="javascript:;">排行榜</a></li>
				<li><a href="javascript:;">歌单</a></li>
				<li><a href="javascript:;">主播电台</a></li>
				<li><a href="javascript:;">歌手</a></li>
				<li><a href="javascript:;">新碟上架</a></li>
			</ul>
		</div>
	</div>
</div>

<div class="wrap">
	<div class="wrap-in">

	    <div class="main">
            <div class="main-title">
                <h3>曲名</h3>
                <span>作者</span>
            </div>
            <div class="content txtOF">
                <!-- 歌词 -->
            </div>
            <a href="javascript:;" class="toggle">展开</a>
        </div>
        <div class="comment">
            <h2>听友评论</h2>
            <div class="cell">
                <textarea name="comment">发表评论</textarea>
                <input type="submit" value="提交">
            </div>
        </div>
	</div>
</div>

<div class="footer">
    <div class="footer-center">
        <a class="recruit" href="javascript:;">
            <p>独立音乐人招募计划</p>
            <span>加入我们 即将与超过亿万乐迷互动</span>
        </a>
        <div class="copy">
            <ul class="copy-list">
                <li><a href="javascript:;">关于本站</a></li><li>-</li>
                <li><a href="javascript:;"> 客户服务</a></li><li>-</li>
                <li><a href="javascript:;"> 服务条款</a></li><li>-</li>
                <li><a href="javascript:;"> 网站导航</a></li>
                <span>Lyric旗下版权所有©1997-2015 </span>
            </ul>
            <p>网络文化经营许可证：粤网文[2014]0332-034号</p>
        </div>
        <a class="feedback" href="javascript:;"><i></i>意见反馈</a> 
    </div>
</div>

<!-- 播放条 -->
<div class="fix-bottom">
    <div class="fix-lock">
        <div class="lock-img"><a href="javascript:;" class="unlock"></a></div>
    </div>
    <div class="fix-play">
        <div class="play-btns" id="play-btns">
            <a href="javascript:;" class="prv" title="上一首"></a>
            <a href="javascript:;" id="data-ps" class="ply" title="播放"><!--.pas--></a>
            <a href="javascript:;" class="nxt" title="下一首"></a>
        </div>
        <div class="play-head">
            <a href="#"><img src="image/default_album.jpg" alt=""></a>
        </div>
        <div class="play-ing" id="play-ing">
            <div class="ptitle">
                <a href="#" class="title" title="曲名"></a>
                <a href="#" class="singer" title="演绎者"></a>
            </div>
            <div class="pbar">
                <div class="barbg"><!-- 总进度条 -->
                    <div class="rdy"></div><!-- 已加载 -->
                    <div class="cur">
                        <div class="cur-inner">
                            <span class="btn-cur"><i></i></span>
                        </div>
                    </div>
                </div>
                <span class="clock"><i>00:00</i> / <em>00:00</em></span>
            </div>
        </div>
        <div class="play-oper" id="play-oper">
            <a href="javascript:;" class="icon-colle" title="收藏"></a>
            <a href="javascript:;" class="icon-share" title="分享"></a>
        </div>
        <div class="play-form">
            <div class="form-title">
                <h3>播放列表</h3>
                <a href="javascript:;" class="icon-colle"><span></span>收藏全部</a>
                <span>|</span>
                <a href="javascript:;" class="icon-empty"><span></span>清空</a>
                <a href="javascript:;" class="table-close" title="关闭"></a>
            </div>
            <div class="form-tab" id="form-tab">
                <ul class="mtab" id="mtab"><!-- 播放列表 --></ul>    
                <div class="empty">播放列表为空哦</div>
            </div>
            <div class="scrol"><span class="icon-scl"></span></div>
        </div>
        <div class="play-ctrl" id="play-ctrl">
            <div class="cbar">  
                <div class="barbg"><!-- 音量调节条 -->
                    <div class="cur">
                        <span class="btn-cur" id="ctrl-cur"></span>
                    </div>
                </div>
            </div>        
            <a href="javascript:;" class="icon-vol" title="音量"></a>
            <a href="javascript:;" class="icon-loop" id="data-lop" title="循环"></a>
            <div class="lp-tip">单曲循环</div>
            <span class="music-list">
                <a href="javascript:;" class="icon-list" title="播放列表">0</a>
                <em>已添加到播放列表</em>
            </span>
        </div>
       
    </div>
    <audio id="player" controls="false">
        <source src=""></source>
        <embed src="" type="" controls="false">
    </audio>
</div>

<script type="text/javascript" src="../../../sea-modules/seajs/sea.js"></script>
<script type="text/javascript">
    
    //设置configuration
    seajs.config({
        base: "../../../sea-modules",
        alias: {
            "jquery" : "jquery/jquery.js"
        }
    });

    //引入main.js
    seajs.use('js/result/main');

</script>
</body>
</html>