<%@page import="ca.diro.javadocindexer.Settings"%>
<script type="text/javascript">
			var once="1";
			function erase(){
				if(once=="1"){
					once = "0";
					document.getElementById("text_query").value="";
					document.getElementById("text_query").style.color="black";
				}
			}
			function write_query(){
				if(	document.getElementById("text_query").value==""){
					once = "1";
					document.getElementById("text_query").value="Entrez votre requ\352te";
					document.getElementById("text_query").style.color="gray";
				}
			}
		</script>
<form class="form-class" action="Search.do" target="_self">
<h4>RECHERCHE</h4>
	<table>
		<tr><td rowspan="5">
		<select name="libraryName">
			<% for(String lib : Settings.libraryList){
				%>
				<option value="<%=lib %>" <%if((request.getParameter("libraryName")!=null)&& request.getParameter("libraryName").equals(lib)){%><%="selected=\"selected\"" %><%} %>><%=lib %></option>
			<%	}
			%>
		</select> <br>
		<input maxlength="50" style="color:<%if(request.getParameter("desired")!=null){%>black<%}else{ %>gray<%} %>;" id="text_query" type="text" value="<%if(request.getParameter("desired")!=null){%><%=request.getParameter("desired")%><%}else{ %>Entrez votre requ&ecirc;te"<%} %>" <%if(request.getParameter("desired")==null){%>onblur="write_query();" onfocus="erase();"<%} %> name="desired"></input></td>
		<td><input type="checkbox" name="class" <%if((request.getParameter("class")!=null)){%>checked="class"<%}%>></input>Nom de Classes</td></tr>
		<tr><td><input type="checkbox" name="method" <%if(request.getParameter("method")!=null){%>checked="checked"<%}%>></input>Nom de M&eacute;thodes</td></tr>
		<tr><td><input type="checkbox" name="package" <%if(request.getParameter("package")!=null){%>checked="checked"<%}%>></input>Nom de Package</td></tr>
		<tr><td><input type="checkbox" name="returnType" <%if(request.getParameter("returnType")!=null){%>checked="checked"<%}%>></input>Type de retour</td></tr>
		 <tr><td><input type="checkbox" name="content" <%if((request.getParameter("content")!=null)||request.getParameter("desired")==null){%>checked="checked"<%}%>></input>Contenu</td></tr>
		<tr><td><select name="resultsPerPage">
			<option value="10" <%if((request.getParameter("resultsPerPage")!=null)&& request.getParameter("resultsPerPage").equals("10")){%><%="selected=\"selected\"" %><%} %>><%=10 %></option>
			<option value="20" <%if((request.getParameter("resultsPerPage")!=null)&& request.getParameter("resultsPerPage").equals("20")){%><%="selected=\"selected\"" %><%} %>><%=20 %></option>
			<option value="50" <%if((request.getParameter("resultsPerPage")!=null)&& request.getParameter("resultsPerPage").equals("50")){%><%="selected=\"selected\"" %><%} %>><%=50 %></option>
			<option value="100" <%if((request.getParameter("resultsPerPage")!=null)&& request.getParameter("resultsPerPage").equals("100")){%><%="selected=\"selected\"" %><%} %>><%=100 %></option>
		</select><input type="hidden" name="pageNumber" value="1"/>
		<tr><td><input type="submit" value="go"></input></td></tr>
	</table>
</form>
<form style="clear:both;"></form>