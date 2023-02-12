<HTML>
<%@page import="ca.diro.javadocindexer.Settings"%>
<HEAD><TITLE><%= Settings.dynamicHostURI + request.getContextPath() %></TITLE></HEAD>
<FRAMESET rows="10%,90%" title="">
<FRAME src="<%= Settings.dynamicHostURI + request.getContextPath() %>/viewHead.jsp?backToSearchResults=<%= request.getAttribute("backToSearchResults") %>"></iframe>
<FRAME src="<%= Settings.staticHostURI + request.getAttribute("tube") %>"></iframe>
</FRAMESET>
</HTML>
