<!DOCTYPE html>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%> 
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Lyric~音乐工厂</title>
    <link rel="stylesheet" href="css/frame.css">
    <link rel="stylesheet" href="css/music.css">
    <link rel="stylesheet" href="css/index.css">
    <link rel="shortcut icon" type="image/x-icon" href="image/favicon.ico" />
</head>
<body>	
<div class="header">
	<div class="header-center center">
		<h1>Lyric 音乐场</h1>
		<ul class="nav">
			<li><a href="javascript:;" class="active">发现音乐</a></li>
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
                <a href="./login" class="user-login">登录</a>
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

		<div class="cloumn main-top">
            <div class="btns">
                <a href="javascript:;" class="btn-down" title="客户端下载">客户端下载</a>
                <a href="javascript:;" class="btn-reg sm">注册帐号</a>
                <a class="tool-border"></a>
                <a href="javascript:;" class="btn-browse sm">站台一览</a>
                <a href="javascript:;" class="btn-gift sm">等级礼包</a>
            </div>
            <div class="slides">
                <ul class="points">
                    <li><a href="javascript:;"><img src="image/player-1.jpg" alt="图片-1"/></a></li>
                    <li><a href="javascript:;"><img src="image/player-2.jpg" alt="图片-2"/></a></li>
                    <li><a href="javascript:;"><img src="image/player-3.jpg" alt="图片-3"/></a></li>
                    <li><a href="javascript:;"><img src="image/player-4.jpg" alt="图片-4"/></a></li>
                    <li><a href="javascript:;"><img src="image/player-5.jpg" alt="图片-5"/></a></li>
                </ul>
                <ul class="sub-tips">
                    <li class="active">●</li><li>●</li><li>●</li><li>●</li><li>●</li>
                </ul>
            </div>
            <div class="aside">
                <ul class="aside-tab">
                    <li><a href="javascript:;" class="active">最新</a></li>
                    <li><a href="javascript:;">活动</a></li>
                    <li><a href="javascript:;">公告</a></li>
                    <li><a href="javascript:;">焦点</a></li>
                    <li class="tab-tool">+</li>
                </ul>
                <ul class="aside-list"></ul>
            </div>
        </div>

        <div class="column main-type">
            <div class="enter">
                <ul class="enter-btns">
                    <li><a href="javascript:;">听歌</a></li>
                    <li class="border-aside"></li>
                    <li><a href="javascript:;">活动</a></li>
                    <li class="border"></li>
                    <li><a href="javascript:;">VIP</a></li>
                    <li class="border-aside"></li>
                    <li><a href="javascript:;">领奖</a></li>
                </ul>
                <ul class="enter-share">
                    <li><a href="javascript:;">官方微信</a></li><li class="bdr-aside">|</li>
                    <li><a href="javascript:;">新浪微博</a></li><li class="bdr-aside">|</li>
                    <li><a href="javascript:;">YY语音</a></li>
                </ul>
            </div>
            <div class="acts">
                <ul class="acts-list">
                    <li><a href="javascript:;"><img src="image/hit-1.jpg" alt=""></a></li>
                    <li><a href="javascript:;"><img src="image/hit-2.jpg" alt=""></a></li>
                    <li><a href="javascript:;"><img src="image/hit-3.jpg" alt=""></a></li>
                    <li><a href="javascript:;"><img src="image/hit-4.jpg" alt=""></a></li>
                </ul>
            </div>
        </div>

        <div class="column main-pro">
            <div class="section">
                <h3>新碟上架<span>PROMOTION</span></h3>
                <ul class="pro-list">
                    <li><a href="javascript:;"><img src="image/view-1.jpg" alt=""></a></li>
                    <li><a href="javascript:;"><img src="image/view-2.jpg" alt=""></a></li>
                    <li><a href="javascript:;"><img src="image/view-3.jpg" alt=""></a></li>
                </ul>
            </div>
            <div class="sidebar"><img src="image/wrap.png" alt=""></div>
        </div>

        <div class="column main-hot">
            <div class="sidebar">
                <h3><i class="icon"></i>歌曲分类</h3>
                <ul class="hot-tab">
                    <li><a href="javascript:;">华语 - 流行<i></i></a></li>
                    <li><a href="javascript:;">轻音 - 弹奏<i></i></a></li>
                    <li><a href="javascript:;">摇滚 - 电子<i></i></a></li>
                    <li><a href="javascript:;">古典 - 民谣</a></li>
                </ul>
                <a href="javascript:;" class="hot-load">所有分类&gt;</a>
            </div>
            <div class="section">
                <h3>热门歌单<span>PLAYLIST</span></h3>
                <ul class="hot-list">
                    <li><img src="image/player-2.jpg" alt=""></li>
                    <li><img src="image/player-3.jpg" alt=""></li>
                    <li><img src="image/player-4.jpg" alt=""></li>
                    <li><img src="image/player-5.jpg" alt=""></li>
                </ul>
            </div>
        </div>

        <div class="column main-rank">
            <div class="section">
                <h3>排行榜<span>RANKLIST</span></h3>
                <div class="rank">
                    <dl class="rank-blk">
                        <dt class="top">
                            <a href="javascript:;" class="dt-img"><img src="image/musicUp.jpg" alt=""></a>
                            <div class="dt-txt">
                                <a href="javascript:;" class="title">云音乐飙升榜</a>
                                <a href="javascript:;" class="icon icon-play"></a>
                                <a href="javascript:;" class="icon icon-store" data-type="up"></a>
                            </div>
                        </dt>
                    </dl>
                    <div class="blk-border"></div>
                    <dl class="rank-blk">
                        <dt class="top">
                            <a href="javascript:;" class="dt-img"><img src="image/musicNew.jpg" alt=""></a>
                            <div class="dt-txt">
                                <a href="javascript:;" class="title">云音乐新歌榜</a>
                                <a href="javascript:;" class="icon icon-play"></a>
                                <a href="javascript:;" class="icon icon-store" data-type="new"></a>
                            </div>
                        </dt>
                    </dl>
                    <div class="blk-border"></div>
                    <dl class="rank-blk">
                        <dt class="top">
                            <a href="javascript:;" class="dt-img"><img src="image/musicCreate.jpg" alt=""></a>
                            <div class="dt-txt">
                                <a href="javascript:;" class="title">原创歌曲榜</a>
                                <a href="javascript:;" class="icon icon-play"></a>
                                <a href="javascript:;" class="icon icon-store" data-type="create"></a>
                            </div>
                        </dt>
                    </dl>
                </div>
            </div>
            <div class="sidebar">
                <h3>听友评论</h3>
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
                        </div>'
                        <!-- <span class="btn-cur" id="data-cur"><i><!-- loading... --></i></span> -->
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
    seajs.use('js/index/main');

</script>

</body>
</html>