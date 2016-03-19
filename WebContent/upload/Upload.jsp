<jsp:include page="../WEB-INF/jsp/head.jsp"/>
<jsp:include page="../WEB-INF/jsp/menu.jsp"/>

<form class="form-class" 
	target="_self" 
	action="<%= request.getContextPath() %>/upload/Upload.do" 
	enctype="multipart/form-data" 
	method="post"
	onsubmit="document.getElementById('consoleLog').innerHTML='';">
<h4>UPLOAD</h4>
	Soumettez votre propre javadoc et elle sera index&eacute;e par ce syst&egrave;me <i>(..l'op&eacute;ration peut prendre plusieurs minutes... )</i><br><br>	
	Fichier de javadoc *.zip ou *.jar : <br>&nbsp;&nbsp;&nbsp;&nbsp;<input type="file" name="file1"/><br>
	Nom de la librarie et version : <br>&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="libraryName"/><br><br>
	&nbsp;&nbsp;&nbsp;&nbsp;<input type="Submit" value="Soummettre"/><br>
</form>
<form style="clear:both;"></form>
<% 	if(request.getAttribute("message")!=null){
		out.println(request.getAttribute("message"));
	}
%>
<br>
<div id="top-screen">
<code>Javadoc Indexer Upload Console Log</code>
</div>
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
