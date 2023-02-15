<jsp:include page="../WEB-INF/jsp/head.jsp"/>
<jsp:include page="../WEB-INF/jsp/menu.jsp"/>

<% 	if(request.getAttribute("message")!=null){
		out.println(request.getAttribute("message"));
	}
%>
<br>


<div id="screen">
<textarea id="consoleLog"><% 
if(request.getAttribute("result")!=null){
	out.println(request.getAttribute("result"));
}
%></textarea>
</div>
<jsp:include page="../WEB-INF/jsp/footer.jsp"/>
</body>
</html>
