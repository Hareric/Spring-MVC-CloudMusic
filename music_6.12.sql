-- phpMyAdmin SQL Dump
-- version 4.5.2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: 2016-05-31 17:04:53
-- 服务器版本： 10.1.13-MariaDB
-- PHP Version: 5.6.21

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;

--
-- Database: `music`
--

-- --------------------------------------------------------

--
-- 表的结构 `app_adminRnews`
--

CREATE TABLE `app_adminRnews` (
  `admin_id` int(11) NOT NULL,
  `news_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- 表的结构 `app_Class`
--

CREATE TABLE `app_Class` (
  `class_name` varchar(80) COLLATE utf8_unicode_ci NOT NULL,
  `class_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 转存表中的数据 `app_Class`
--

INSERT INTO `app_Class` (`class_name`, `class_id`) VALUES
('up', 1),
('new', 2),
('create', 3);

-- --------------------------------------------------------

--
-- 表的结构 `app_collection`
--

CREATE TABLE `app_collection` (
  `user_id` int(10) UNSIGNED NOT NULL,
  `music_id` int(10) UNSIGNED NOT NULL,
  `colDate` date NOT NULL,
  `id` int(10) UNSIGNED NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 转存表中的数据 `app_collection`
--

INSERT INTO `app_collection` (`user_id`, `music_id`, `colDate`, `id`) VALUES
(2, 10, '2016-02-26', 25),
(3, 5, '2016-02-18', 20),
(2, 2, '2016-02-26', 46),
(2, 1, '2016-02-26', 45),
(2, 4, '2016-02-26', 44),
(2, 5, '2016-02-26', 43),
(2, 6, '2016-02-26', 42),
(2, 8, '2016-02-26', 41),
(2, 10, '2016-02-26', 40),
(2, 7, '2016-02-26', 39),
(2, 9, '2016-02-26', 38),
(2, 3, '2016-02-26', 37);

-- --------------------------------------------------------

--
-- 表的结构 `app_comment`
--

CREATE TABLE `app_comment` (
  `user_id` int(11) NOT NULL,
  `music_id` int(11) NOT NULL,
  `content` text COLLATE utf8_unicode_ci NOT NULL,
  `time` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- 表的结构 `app_friends`
--

CREATE TABLE `app_friends` (
  `user_id` int(11) NOT NULL,
  `friend_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- 表的结构 `app_Info`
--

CREATE TABLE `app_Info` (
  `user_id` int(10) NOT NULL,
  `name` varchar(16) COLLATE utf8_unicode_ci NOT NULL,
  `image` varchar(34) COLLATE utf8_unicode_ci NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 转存表中的数据 `app_Info`
--

INSERT INTO `app_Info` (`user_id`, `name`, `image`) VALUES
(2, 'Zenaro', '../../../src/home/image/face.png'),
(27, 'ASD', '../../../src/home/image/profile.jp');

-- --------------------------------------------------------

--
-- 表的结构 `app_Music`
--

CREATE TABLE `app_Music` (
  `name` text COLLATE utf8_unicode_ci NOT NULL,
  `src` text COLLATE utf8_unicode_ci NOT NULL,
  `lyric` text COLLATE utf8_unicode_ci NOT NULL,
  `music_id` int(10) UNSIGNED NOT NULL,
  `listeners` int(10) UNSIGNED NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 转存表中的数据 `app_Music`
--

INSERT INTO `app_Music` (`name`, `src`, `lyric`, `music_id`, `listeners`) VALUES
('V.A.-キミを思うメロディー', 'http://7xstax.com1.z0.glb.clouddn.com/VA-missing.mp3', '纯音乐无歌词', 2, 2500),
('you -- Graveyard-you', 'http://7xstax.com1.z0.glb.clouddn.com/M.Graveyard-you.mp3', '纯音乐无歌词', 4, 3200),
('ばんばんしー-in the autumn sky', 'http://7xstax.com1.z0.glb.clouddn.com/in-the-autumn-sky.mp3', '纯音乐无歌词', 11, 2233),
('相爱很难', 'http://7xstax.com1.z0.glb.clouddn.com/lrc-love.mp3', '[ti:相爱很难(张学友梅艳芳)]\r\n[ar:梅艳芳]\r\n[al:]\r\n[00:03.69] 词曲：作曲／编曲：陈辉阳 填词：林夕\r\n[00:19.78] 女：最好 有生一日都爱下去\r\n[00:26.69]  但谁人 能将恋爱当做终生兴趣\r\n[00:33.75] 男：生活 其实旨在找到个伴侣\r\n[00:40.76]  面对现实 热恋很快变长流细水\r\n[00:47.03] 女：可惜我 不智或侥倖\r\n[00:50.50]  对火花天生敏感\r\n[00:54.80] 男：不过 两只手拉得太紧\r\n[00:58.71] 合：爱到过了界那对爱人\r\n[01:01.73]  同时亦最易变成一对敌人\r\n[01:10.79] 女：也许相爱很难\r\n[01:10.50]  就难在其实双方各有各寄望\r\n[01:15.77]  怎么办\r\n[01:15.44] 男：要单恋都难\r\n[01:17.46]  受太大的礼会内疚却也无力归还\r\n[01:23.82] 女：也许不爱不难\r\n[01:25.68]  但如未成佛升仙也会怕\r\n[01:29.51]  爱情前途黯淡\r\n[01:31.96] 男：爱不爱都难\r\n[01:32.81]  未快乐先有责任给予对方面露欢颜\r\n[01:37.48] 女：得到浪漫 又要有空间\r\n[01:41.02] 男：得到定局 却怕去到终站\r\n[01:44.30] 合：然后付出多得到少不介意豁达\r\n[01:48.23]  又担心 有人看不过眼\r\n[02:04.47] 女：可惜我 不智或侥倖\r\n[02:07.22]  对火花天生敏感\r\n[02:11.59] 男：不过 两只手拉得太紧\r\n[02:14.41] 合：爱到过了界那对爱人\r\n[02:17.36]  同时亦最易变成一对敌人\r\n[02:25.95] 女：也许相爱很难\r\n[02:27.69]  就难在其实双方各有各寄望\r\n[02:31.15]  怎么办\r\n[02:32.03] 男：要单恋都难\r\n[02:34.74]  受太大的礼会内疚却也无力归还\r\n[02:39.81] 女：也许不爱不难\r\n[02:41.14]  但如未成佛升仙也会怕\r\n[02:44.29]  爱情前途黯淡\r\n[02:46.90] 男：爱不爱都难\r\n[02:48.93]  未快乐先有责任给予对方面露欢颜\r\n[02:53.14] 女：得到浪漫 又要有空间\r\n[02:56.24] 男：得到定局 却怕去到终站\r\n[03:00.91] 合：然后付出多得到少不介意豁达\r\n[03:04.70]  又担心 有人看不过眼\r\n[03:10.18] 合：无论热恋中失恋中\r\n[03:13.96] 都永远记住第一戒\r\n[03:18.64] 别要张开双眼', 24, 3500);

-- --------------------------------------------------------

--
-- 表的结构 `app_musicRclass`
--

CREATE TABLE `app_musicRclass` (
  `class_id` int(11) NOT NULL,
  `music_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 转存表中的数据 `app_musicRclass`
--

INSERT INTO `app_musicRclass` (`class_id`, `music_id`) VALUES
(1, 2),
(1, 4),
(1, 11),
(1, 24),
(2, 2),
(2, 4),
(2, 11),
(2, 24),
(3, 2),
(3, 4),
(3, 11),
(3, 24);

-- --------------------------------------------------------

--
-- 表的结构 `app_News`
--

CREATE TABLE `app_News` (
  `title` text COLLATE utf8_unicode_ci NOT NULL,
  `content` text COLLATE utf8_unicode_ci NOT NULL,
  `id` int(11) NOT NULL,
  `type` varchar(16) COLLATE utf8_unicode_ci NOT NULL,
  `pubDate` date NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 转存表中的数据 `app_News`
--

INSERT INTO `app_News` (`title`, `content`, `id`, `type`, `pubDate`) VALUES
('迎新年，全民福利活动Let''s Go！', '内容我已经想好了', 1, '活动', '2016-01-08'),
('银翼邮递员传递祝福！圣诞祝福征集正式开始', '下星期有活动，嗯', 2, '活动', '2016-01-07'),
('12·12线上活动，买赠大行动！', '双十二买增大活动', 3, '焦点', '2015-11-19'),
('新专辑体验资格已更新，请登录页面查询', '到场楼主就告诉你', 4, '公告', '2016-01-07'),
('猜歌无级限 活动嗨不停', '说好的推文呢？', 5, '公告', '2016-01-10'),
('节日歌单大折扣', '编辑部去旅游了，没人写稿', 6, '焦点', '2016-01-16'),
('音乐概念站·Silver Light From The Above', '音乐大礼包任性送', 7, '活动', '2016-01-22');

-- --------------------------------------------------------

--
-- 表的结构 `app_Singer`
--

CREATE TABLE `app_Singer` (
  `singer_id` int(11) NOT NULL,
  `singer_name` varchar(80) COLLATE utf8_unicode_ci NOT NULL,
  `counrtry` varchar(80) COLLATE utf8_unicode_ci NOT NULL,
  `birthday` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 转存表中的数据 `app_Singer`
--

INSERT INTO `app_Singer` (`singer_id`, `singer_name`, `counrtry`, `birthday`) VALUES
  (1, 'V.A.', 'Japan', '2015-11-04'),
  (2, '《寒蝉鸣泣之时》原生', 'Japan', '2016-05-18'),
  (3, '秋姉妹のなく頃に', 'Japan', '2016-05-13'),
  (4, '李克勤--容祖儿', '香港', '2016-02-09');

-- --------------------------------------------------------

--
-- 表的结构 `app_singerRmusic`
--

CREATE TABLE `app_singerRmusic` (
  `singer_id` int(11) NOT NULL,
  `music_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 转存表中的数据 `app_singerRmusic`
--

INSERT INTO `app_singerRmusic` (`singer_id`, `music_id`) VALUES
(1, 2),
(2, 4),
(3, 11),
(4, 24);

-- --------------------------------------------------------

--
-- 表的结构 `app_User`
--

CREATE TABLE `app_User` (
  `email` varchar(16) COLLATE utf8_unicode_ci NOT NULL,
  `pwd` char(34) COLLATE utf8_unicode_ci NOT NULL,
  `id` int(11) NOT NULL,
  `regDate` date NOT NULL,
  `root` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 转存表中的数据 `app_User`
--

INSERT INTO `app_User` (`email`, `pwd`, `id`, `regDate`, `root`) VALUES
('572001239@qq.com', 'dcf44e9c52c45576ad75f04c37a52166', 2, '2016-01-24', 1),
('asd@qq.com', 'dcf44e9c52c45576ad75f04c37a52166', 27, '2016-02-26', 0);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `app_Class`
--
ALTER TABLE `app_Class`
  ADD PRIMARY KEY (`class_id`);

--
-- Indexes for table `app_collection`
--
ALTER TABLE `app_collection`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `app_Info`
--
ALTER TABLE `app_Info`
  ADD UNIQUE KEY `user_id` (`user_id`);

--
-- Indexes for table `app_Music`
--
ALTER TABLE `app_Music`
  ADD PRIMARY KEY (`music_id`);

--
-- Indexes for table `app_News`
--
ALTER TABLE `app_News`
  ADD PRIMARY KEY (`type`,`pubDate`,`id`),
  ADD UNIQUE KEY `id` (`id`);

--
-- Indexes for table `app_Singer`
--
ALTER TABLE `app_Singer`
  ADD PRIMARY KEY (`singer_id`);

--
-- Indexes for table `app_User`
--
ALTER TABLE `app_User`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `id` (`id`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `app_Class`
--
ALTER TABLE `app_Class`
  MODIFY `class_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- 使用表AUTO_INCREMENT `app_collection`
--
ALTER TABLE `app_collection`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=47;
--
-- 使用表AUTO_INCREMENT `app_Music`
--
ALTER TABLE `app_Music`
  MODIFY `music_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;
--
-- 使用表AUTO_INCREMENT `app_News`
--
ALTER TABLE `app_News`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;
--
-- 使用表AUTO_INCREMENT `app_Singer`
--
ALTER TABLE `app_Singer`
  MODIFY `singer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;
--
-- 使用表AUTO_INCREMENT `app_User`
--
ALTER TABLE `app_User`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;


-- ---------------------------------------------next by Hareric
INSERT INTO `app_Singer` (`singer_id`, `singer_name`, `counrtry`, `birthday`) VALUES
  (5, '周杰伦', '台湾', '1984-03-02'),
  (6, '张惠妹', '大陆', '1985-04-12'),
  (7, '陈奕迅', '香港', '1984-01-23');

INSERT INTO `app_Music` (`name`, `src`, `lyric`, `music_id`, `listeners`) VALUES
 ('好久不见', 'http://m2.music.126.net/UUc8k9mJvRF8vgHvl5XZ8g==/7965961744696366.mp3', '该歌曲暂无歌词', 185670, 3988),
 ('花海', 'http://m2.music.126.net/-XksZ5J-p_fvag38Wpfs8A==/7939573465716474.mp3', '该歌曲暂无歌词', 185697, 6780),
 ('时光机', 'http://m2.music.126.net/VFNwKhFXXjWHLeEAHkGaqg==/7939573465716475.mp3', '该歌曲暂无歌词', 185705, 9171),
 ('彩虹', 'http://m2.music.126.net/sLKP3na0M4mdXocWFcVHlA==/3236962235602026.mp3', '该歌曲暂无歌词', 185809, 2718),
 ('甜甜的', 'http://m2.music.126.net/O1-e4JylX2IhqQTV6ZLZkg==/3235862723975492.mp3', '该歌曲暂无歌词', 185820, 7438),
 ('心雨', 'http://m2.music.126.net/rUhLQwYcRc6k9ZoS9Xa_KQ==/7945071023769296.mp3', '该歌曲暂无歌词', 185888, 4401),
 ('迷迭香', 'http://m2.music.126.net/CimYKAz5upIGVYja-j6ccQ==/7950568581910017.mp3', '该歌曲暂无歌词', 185892, 3506),
 ('发如雪', 'http://m2.music.126.net/KUonV6L_FkzpggJ30BGajA==/7973658326167752.mp3', '该歌曲暂无歌词', 185906, 4790),
 ('浪漫手机', 'http://m2.music.126.net/GWYTPtAOzZDpnGFLLgcWPw==/3341415836881395.mp3', '该歌曲暂无歌词', 185914, 5003),
 ('麦芽糖', 'http://m2.music.126.net/t0tJ_hsTx9Fjo2tLbdiq5A==/3304032441536670.mp3', '该歌曲暂无歌词', 185918, 4026);

INSERT INTO `app_singerRmusic` (`singer_id`, `music_id`) VALUES
 (5, 185670),
 (5, 185697),
 (5, 185705),
 (5, 185809),
 (5, 185820),
 (5, 185888),
 (5, 185892),
 (5, 185906),
 (5, 185914),
 (5, 185918);

INSERT INTO `app_musicRclass` (`class_id`, `music_id`) VALUES
 (1, 185670),
 (1, 185697),
 (1, 185705),
 (1, 185809),
 (1, 185820),
 (1, 185888),
 (1, 185892),
 (1, 185906),
 (1, 185914),
 (1, 185918);

INSERT INTO `app_Music` (`name`, `src`, `lyric`, `music_id`, `listeners`) VALUES
 ('我最亲爱的', 'http://m2.music.126.net/6eX2Yz5qp6Kjh6xEtkOt_Q==/6621259022545038.mp3', '该歌曲暂无歌词', 326784, 6685),
 ('你和我的时光', 'http://m2.music.126.net/kS5BMXsKGuQgr5JPCYd_sg==/6672936069051725.mp3', '该歌曲暂无歌词', 326799, 3869),
 ('听得见的梦想', 'http://m2.music.126.net/wkjD6hj3kOf0L2TvWCMNNA==/7940672976961287.mp3', '该歌曲暂无歌词', 326885, 4329),
 ('如果你也听说', 'http://m2.music.126.net/VJ8F5FMsBlxy2frn71Grxw==/6622358534172426.mp3', '该歌曲暂无歌词', 326904, 6941),
 ('别在伤口洒盐', 'http://m2.music.126.net/CkA4qNW8n7_j_KIDKXPGiQ==/1123700883596190.mp3', '该歌曲暂无歌词', 326931, 3703),
 ('让每个人都心碎', 'http://m2.music.126.net/26XBoEgBiTph2bxqBMbzvg==/1167681348707239.mp3', '该歌曲暂无歌词', 326939, 2469),
 ('最爱的人伤我最深', 'http://m2.music.126.net/Ni50fgkTqiHaoTF4S1oeyw==/995058023146343.mp3', '该歌曲暂无歌词', 327065, 783),
 ('人质', 'http://m2.music.126.net/Lc7SmemVuh1kLWT_alGXvQ==/7698780419901474.mp3', '该歌曲暂无歌词', 327096, 9894),
 ('我要快乐?', 'http://m2.music.126.net/l8pJpkubSAWTBMPr7XggIQ==/7940672977445944.mp3', '该歌曲暂无歌词', 327104, 4412),
 ('也许明天', 'http://m2.music.126.net/RYdhjgN6nmaIJjBcoOEdzQ==/1190771092890141.mp3', '该歌曲暂无歌词', 327136, 9135);

INSERT INTO `app_singerRmusic` (`singer_id`, `music_id`) VALUES
 (6, 326784),
 (6, 326799),
 (6, 326885),
 (6, 326904),
 (6, 326931),
 (6, 326939),
 (6, 327065),
 (6, 327096),
 (6, 327104),
 (6, 327136);

INSERT INTO `app_musicRclass` (`class_id`, `music_id`) VALUES
 (2, 326784),
 (2, 326799),
 (2, 326885),
 (2, 326904),
 (2, 326931),
 (2, 326939),
 (2, 327065),
 (2, 327096),
 (2, 327104),
 (2, 327136);

INSERT INTO `app_Music` (`name`, `src`, `lyric`, `music_id`, `listeners`) VALUES
 ('最冷一天', 'http://m2.music.126.net/8XS5yFzvaonY_Qr4NeTr3w==/7949469070463563.mp3', '该歌曲暂无歌词', 186331, 9761),
 ('稳稳的幸福', 'http://m2.music.126.net/Q-iLjEnh-hwbro8SgtUoqA==/2069280883483485.mp3', '该歌曲暂无歌词', 25730757, 2191),
 ('不要说话', 'http://m2.music.126.net/u9nJupi9yzpKzJB_sgSa1A==/3268848069435294.mp3', '该歌曲暂无歌词', 25906124, 6115),
 ('改造人', 'http://m2.music.126.net/rU43tcFL2LveZR3QolAmcw==/1983518976526611.mp3', '该歌曲暂无歌词', 26075126, 8092),
 ('飘飘飘飘', 'http://m2.music.126.net/rHamnWREo5XccNne8p8m1g==/2083574534653795.mp3', '该歌曲暂无歌词', 26075129, 6490),
 ('时光倒流二十年', 'http://m2.music.126.net/aLJIXvLscXbSONkEbVTJfg==/2070380395120441.mp3', '该歌曲暂无歌词', 26075130, 1745),
 ('主旋律', 'http://m2.music.126.net/pVDJ5QPDKQBNHla0FTCe9w==/3190782743860301.mp3', '该歌曲暂无歌词', 26523013, 3908),
 ('任我行', 'http://m2.music.126.net/ul7kqM0EwMsxblP2EdaG3A==/5754843859832382.mp3', '该歌曲暂无歌词', 27483202, 5333),
 ('失忆蝴蝶', 'http://m2.music.126.net/uxDafAZTaUizrU2q8z0RMw==/5714161929607979.mp3', '该歌曲暂无歌词', 27483203, 1275),
 ('远在咫尺', 'http://m2.music.126.net/ASxaYTENydeCcLi422_-iA==/5632798069165307.mp3', '该歌曲暂无歌词', 27483206, 4362);

INSERT INTO `app_singerRmusic` (`singer_id`, `music_id`) VALUES
 (7, 186331),
 (7, 25730757),
 (7, 25906124),
 (7, 26075126),
 (7, 26075129),
 (7, 26075130),
 (7, 26523013),
 (7, 27483202),
 (7, 27483203),
 (7, 27483206);

INSERT INTO `app_musicRclass` (`class_id`, `music_id`) VALUES
 (3, 186331),
 (3, 25730757),
 (3, 25906124),
 (3, 26075126),
 (3, 26075129),
 (3, 26075130),
 (3, 26523013),
 (3, 27483202),
 (3, 27483203),
 (3, 27483206);