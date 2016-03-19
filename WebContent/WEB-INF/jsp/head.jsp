<%@page import="ca.diro.javadocindexer.Settings"%>
<%@page import="java.util.Enumeration"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="fr">
<head>
<title>Javadoc Index <%=request.getAttribute("resultList")==null?"":"- R&eacute;sultat de recherche"%></title>
<base target="<%= Settings.dynamicHostURI+request.getContextPath() %>"/>
<link rel="icon" type="image/bmp" href="<%=request.getContextPath()%>/geodes.jpg" />
<link rel="shortcut icon" href="<%=request.getContextPath()%>/geodes.jpg" />
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/style.css" />
</head>
<body>
	<h3 width="50%" style="display:inline-block;">
		<span style="cursor:pointer;" onclick="window.location.href='<%=request.getContextPath()%>/search.jsp';">Javadoc Index</span>
	</h3>
