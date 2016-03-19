<%@page import="ca.diro.javadocindexer.Settings"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="fr">
<head><title>En tête</title></head>
<body style="background-image:url('Picture2.jpg');background-size:100% 100%;
	background-attachment:fixed;
	background-repeat:no-repeat;
	margin-top:2px;
	margin-left:125px;
	margin-right:40px;">
	
	<h3 width="50%" style="
	display:inline-block;
	color:#AAB010;
	font-family:imapct;
	font-size:200%;">
		<span style="cursor:pointer;"  
		onclick="top.location='<%= request.getContextPath()+"/Search.do?"+ java.net.URLDecoder.decode(request.getParameter("backToSearchResults"),"UTF-8") %>';">
		Javadoc Index
		</span>
	</h3>
</body>
</html>