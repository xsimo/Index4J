<jsp:include page="WEB-INF/jsp/head.jsp"/>
<div style="float:right;">
<a target="_self" href="<%= request.getContextPath() %>/search.jsp">Search</a>
</div>
<div style="clear:both;"/>
<form target="_self" action="j_security_check"" method="post">
	<table class="gridtable">
	<tr><td>Username</td><td><input type="text" name="j_username"></td></tr>
	<tr><td>Password</td><td><input type="password" name="j_password"></td></tr>
	<tr><td colspan="2" style="text-align:right;"><input type="submit" value="Login"/></td></tr>
	</table>
</form>