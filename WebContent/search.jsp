<jsp:include page="WEB-INF/jsp/head.jsp"/>
<jsp:include page="WEB-INF/jsp/menu.jsp"/>
	
		<jsp:include page="WEB-INF/jsp/FormsBox.jsp"/>
	
	
	<%=request.getAttribute("resultList")==null?"":request.getAttribute("resultList") %>
	<%
		if(request.getAttribute("resultList")!=null){
			int start = Integer.parseInt((String)request.getAttribute("start"));
			int end = Integer.parseInt((String)request.getAttribute("end"));
			if(request.getAttribute("hasLessBeforeStart").equals("true")){
				out.println("<a target=\"_self\" href=\"Search.do?"+request.getQueryString().replaceAll("pageNumber=([0-9]+)","pageNumber=1")+"\">1</a> ... ");
			}
			for(int i=start;i<=end;i++){
				if(request.getParameter("pageNumber").equals(""+i)){
					out.println(i+" ");
				}else{
					out.println("<a target=\"_self\" href=\"Search.do?"+request.getQueryString().replaceAll("pageNumber=([0-9]+)","pageNumber="+i)+"\">"+i+"</a> ");
				}
			}
			if(request.getAttribute("hasMorePastEnd").equals("true")){
				out.println("... <a target=\"_self\" href=\"Search.do?"+request.getQueryString().replaceAll("pageNumber=([0-9]+)","pageNumber="+request.getAttribute("last"))+"\">"+request.getAttribute("last")+"</a>");
			}
		}
	%>
	<br><br>
<jsp:include page="WEB-INF/jsp/footer.jsp"/>