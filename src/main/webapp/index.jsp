<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<meta charset="UTF-8">
<title>文件上传范例 - Servlet 基础教程 | 简单教程(www.twle.cn)</title>
<h3>文件上传范例 - Servlet 基础教程 | 简单教程(www.twle.cn) </h3>
<form method="post" action="/servlet/upload" enctype="multipart/form-data">
    <p>选择一个文件: <input type="file" name="uploadFile" /></p>
    <p><input type="submit" value="上传" /></p>
</form>