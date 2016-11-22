<!DOCTYPE html>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%> 
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Lyric~上传音乐</title>
    <link rel="stylesheet" href="css/upload.css"/>
</head>
<body>
<div class="container">
    <h3>上传音乐</h3>
    <form action="../controller/upload_file.php" method="post" enctype="multipart/form-data">
        <h4>选择文件</h4>
        <div class="form-group">
            <label for="name">曲名</label>
            <input type="text" name="name" id="name"/>
        </div>
        <div class="form-group">
            <label for="singer">歌手</label>
            <input type="text" name="singer" id="singer"/>
        </div>
        <div class="form-group">
            <label for="type">类型</label>
            <select name="type" id="type">
                <option value="1">up</option>
                <option value="2">new</option>
                <option value="3">create</option>
            </select>
        </div>
        <div class="form-group">
            <label for="lrc">歌词</label>
            <textarea name="" id="lrc">纯音乐无歌词</textarea>
        </div>
        <div class="form-group">
            <input type="file" name="file" id="file" />
        </div>
        <input type="submit" name="submit" value="上传" />
    </form>
</div>
</body>
</html>