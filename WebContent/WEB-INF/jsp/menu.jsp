	<div style="float:right;">
	<% 
		boolean notLoggingOut = true;
		if(request.getAttribute("loggingOut")!=null){
			try{
				String begone = (String)request.getAttribute("loggingOut");
				if(begone.equals("begone")){
					notLoggingOut = false;
				}
			}catch(Exception stays){
				notLoggingOut = true;
			}
		}
		if(request.getUserPrincipal() != null && notLoggingOut ){
			if(!request.getRequestURI().contains("upload")){
				out.println("<a target=\"_self\" href=\""+request.getContextPath()+"/upload/Upload.jsp\">Upload</a>");
			}else{
				out.println("<a target=\"_self\" href=\""+request.getContextPath()+"/search.jsp\">Search</a>");
			}
			out.println("<a target=\"_self\" href=\""+request.getContextPath()+"/Logout.do\">Logout");
			out.println("( "+request.getUserPrincipal().getName()+" )" );
		} else {
			out.println("<a target=\"_self\" href=\""+request.getContextPath()+"/upload/Upload.jsp\">Login");
			
		}
		out.println("</a>");
		%>
	</div>
	<div style="clear:both;"/>