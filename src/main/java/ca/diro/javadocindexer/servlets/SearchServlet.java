package ca.diro.javadocindexer.servlets;

import java.io.File; 
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.ReaderUtil;
import org.apache.lucene.util.Version;

import ca.diro.javadocindexer.Settings;
import ca.diro.javadocindexer.JavadocIndexerUtilities;
import ca.diro.javadocindexer.ManagedException;

/**
 * 
 * Ce servlet r&eacute;pond au commande de recherche dans la documentation javadoc
 * 
 * 
 * @author aramesim
 *
 */

public class SearchServlet extends javax.servlet.http.HttpServlet {

	static IndexSearcher searcher;
	static int GOOGLE = Integer.MAX_VALUE;
	private static final long serialVersionUID = 1L;
	private static final int req_maxlength = 50;
	protected static int contextBefore = 4;
	protected static int contextAfter = 6;
	protected static int numberOfHighlightedHits = 3;
	
	/**
	 * les parametres du servlet sont : 
	 * - request.desired : la requete textuelle
	 * - request.class : boolean pour chercher un classe
	 * - request.method : boolean pour chercher un m&eacute;thode
	 * 
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String req = (String)request.getParameter("desired");
		request.setAttribute("resultList",null);
		String out="";
		try{
			if(req.length()>req_maxlength){
				throw new IOException("Request must no exceed "+req_maxlength+" chars");
			}
			
			if(request.getParameter("libraryName")==null){
				throw new ManagedException("managed");
			}
			
			ArrayList<IndexReader> libraryIndexes = new ArrayList<IndexReader>();
			String libraryName = request.getParameter("libraryName");
			
			if(!libraryName.equals("all")) {
				
				String indexPath = Settings.INDEX_DIR_PATH+Settings.sep+libraryName;
				searcher = new IndexSearcher(IndexReader.open(FSDirectory.open(new File(indexPath))));
				
				
			}else {
				IndexReader[] readers = new IndexReader[Settings.libraryList.size()];
				for(int i = 0;i<Settings.libraryList.size();i++) {
					String indexPath = Settings.INDEX_DIR_PATH + Settings.sep + Settings.libraryList.get(i);
					readers[i] = IndexReader.open(FSDirectory.open(new File(indexPath)));
					
				}
				MultiReader mreader = new MultiReader(readers,true);
				
				searcher = new IndexSearcher(mreader);
			}
			
			Analyzer analyzer = JavadocIndexerUtilities.getAnalyzer();
			Query query = null;
			ArrayList<String> fieldsList = new ArrayList<String>();
			
			//A cause d'un bug Lucene, l'Analyseur par d�faut (StandardAnalyzer) n'est pas charg�
			//si l'on cherche des "fields" qui utilisent uniquement le KeywordAnalyser LUCENE3434(commentaire)
			fieldsList.add("modified");
			
			if(request.getParameter("class")!=null){
				fieldsList.add("classTitle");
				fieldsList.add("camelCase");
			}
			if(request.getParameter("method")!=null){
				fieldsList.add("methodNames");
			}
			if(request.getParameter("package")!=null){
				fieldsList.add("packageName");
			}
			if(request.getParameter("returnType")!=null ){
				fieldsList.add("returnType");
			}
			if(request.getParameter("content")!=null || fieldsList.size()==0){
				fieldsList.add("contents");
			}
			String [] fields = {""};
			fields = fieldsList.toArray(fields);
			
			QueryParser parser = null;
			parser = new MultiFieldQueryParser(Version.LUCENE_34,fields , analyzer);
			query = parser.parse(req);
			/*org.apache.lucene.queryParser.
			queryNode = new org.apache.lucene.queryParser.core.nodes.BoostQueryNode();
			*/
			TopDocs results = searcher.search(query, GOOGLE);
			
			SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
			Highlighter highlighter = new Highlighter(htmlFormatter, new SimpleHTMLEncoder(), new QueryScorer(query));
			
			ScoreDoc[] hits = results.scoreDocs;
			int numTotalHits = results.totalHits;
			request.setAttribute("numTotalHits", numTotalHits);
			int numberOfPages = numTotalHits/Integer.parseInt(request.getParameter("resultsPerPage"));
			int pageNumber = Integer.parseInt(request.getParameter("pageNumber"));
			request.setAttribute("numberOfPages", numberOfPages);
			int start=1,end,last;
			if(pageNumber<=3 || numberOfPages<=5){start=1;}
			else{start = pageNumber-2;}
			end = Math.min(numberOfPages+1, start+5);
			last = (int) java.lang.Math.ceil(results.totalHits/Integer.parseInt(request.getParameter("resultsPerPage")));
			if(last>end){
				request.setAttribute("hasMorePastEnd", "true");
			}else{
				request.setAttribute("hasMorePastEnd", "false");
			}
			if(start>1){
				request.setAttribute("hasLessBeforeStart", "true");
			}else{
				request.setAttribute("hasLessBeforeStart", "false");
			}
			request.setAttribute("start",""+start);
			request.setAttribute("end",""+end);
			request.setAttribute("last", last+"");
			out+=numTotalHits+" R&eacute;sultats pour \"<i>"+req+"\"</i>";
			out+="<br><br><div class=\"search-result\"><ul>";
			int resultsStart, resultsEnd;
			resultsStart = (Integer.parseInt(request.getParameter("resultsPerPage")))*((pageNumber)-1);
			resultsEnd = resultsStart + Integer.parseInt(request.getParameter("resultsPerPage"));
			
			for(int i = resultsStart;i<resultsEnd&&i<hits.length;i++){
				int id = hits[i].doc;
				String libName = null;
				if(!libraryName.equals("all")) {
					libName = libraryName;
				}else {
					MultiReader mr = (MultiReader)searcher.getIndexReader();
					Document d = mr.document(id);
					IndexReader subReader = ReaderUtil.subReader(id, mr);
					libName = ((FSDirectory)subReader.directory()).getDirectory().getName();
				}
			    
			    Document doc = searcher.doc(id);
			    String path = doc.get("path");
				String content = doc.get("contents");
				if(req.split(" ").length==1 && request.getParameter("method")!=null){
					Fieldable [] methodes = doc.getFieldables("methodNames");
					for(Fieldable methode: methodes){
						if(req.equals(methode.stringValue())){
							RandomAccessFile raf = null;
							String fullContent = "";
							try {
								raf = new RandomAccessFile(Settings.WebContentDir+Settings.sep+path.substring(request.getServerName().length()),"r");
								String line = "";
								while((line= raf.readLine())!=null)fullContent +=line;
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							int methodWthParamsStart = StringUtils.indexOfIgnoreCase(fullContent,"<A NAME=\""+req)+9;
							if(methodWthParamsStart!=8){
								int methodWthParamsEnd = fullContent.indexOf("\">",methodWthParamsStart);
								if(methodWthParamsEnd!=-1){
									path+="#"+fullContent.substring(methodWthParamsStart,methodWthParamsEnd);
									break;
								}
							}
						}
					}
				}
				
				path = path.replace('\\', '/');
				String title = doc.get("classTitle")!=null?doc.get("classTitle"):"Package Summary "+doc.get("packageName");
				out+="<li>";
				//path = path.substring(request.getContextPath().length());
				if(true || doc.get("openSource")!=null && doc.get("openSource").equals("yes")){
					out+="<a target=\"_self\" href=\""+request.getContextPath()+"/library/"+libName+path+"?query="+URLEncoder.encode(request.getQueryString(),"UTF-8")+"\">"+title+"</a> "+hits[i].score+"<br>";
				}else{
					out+="<a target=\"_blank\" href=\""+Settings.staticHostURI+request.getContextPath()+"library/"+libName+path+"\">"+title+"</a> "+hits[i].score+"<br>";
				}
				//This new method uses highlight contrib but is no good for CamelCase field
				//String text = doc.get("contents");
				String partialHighlightUsage = "";
			    //TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), id, "contents", analyzer);
				TokenStream tokenStream = TokenSources.getTokenStream(doc,"contents",analyzer);
			    TextFragment[] frag = highlighter.getBestTextFragments(tokenStream, content, true, 3);
			    for (int j = 0; j < frag.length; j++) {
			    	if ((frag[j] != null) && (frag[j].getScore() > 0)) {
			    		partialHighlightUsage+=((frag[j].toString()));
			    	}
			    }
			    out+=partialHighlightUsage;
			    /*if(partialHighlightUsage.equals("")){
			    	System.out.println("CHEVAL content :\n"+content);
			    }*/
			    //So if camelCase has been the only hit we still need something to show in result excerpt
			    if(partialHighlightUsage.equals("") && fieldsList.contains("camelCase")){
			    	Fieldable[] camelCaseHits = doc.getFieldables("camelCase");
			    	camelCaseHits[0].stringValue();
			    	String remain = content;
			    	String beforeExtract, afterExtract;
			    	int r = 0;
			    	String [] reqs = req.split(" ");
					final int N =reqs.length;
					@SuppressWarnings("unchecked")
					String[] newArray = new String[N+1];
					//arr = java.util.Arrays.copyOf(arr,N+1);
					for(int i1 = N;i1>0;i1--){
						newArray[i1] = reqs[i1-1];
					}
					newArray[0] = req;
			    	String[] insert = (String[]) newArray;
					final int N1 =reqs.length;
					@SuppressWarnings("unchecked")
					String[] newArray1 = new String[N1+1];
					//arr = java.util.Arrays.copyOf(arr,N+1);
					for(int i1 = N1;i1>0;i1--){
						newArray1[i1] = reqs[i1-1];
					}
					newArray1[0] = req.replace(" ","");
			    	reqs = (String[]) newArray1;
			    	String currentReq = null;
			    	for(int n = 0; n < numberOfHighlightedHits ; n++){
			    		int min = Integer.MAX_VALUE;
			    		for(int j = 0; j < reqs.length; j++){
			    			reqs[j] = reqs[j].replace("*", "");
			    			if(StringUtils.containsIgnoreCase(remain,reqs[j])){
			    				if(min>StringUtils.indexOfIgnoreCase(remain, reqs[j])){
			    					min = StringUtils.indexOfIgnoreCase(remain, reqs[j]);
									currentReq = reqs[j];
			    				}
			    			}
			    		}
			    		if(min==-1 || min == Integer.MAX_VALUE){
							break;
						}
						beforeExtract="";
						String [] arrBefore = remain.substring(0,StringUtils.indexOfIgnoreCase(remain,currentReq)).split(" ");
						for(int h = Math.max(0,arrBefore.length-contextBefore);h<arrBefore.length;h++){
							beforeExtract += arrBefore[h]+" ";
						}
						String [] arrAfter = remain.substring(StringUtils.indexOfIgnoreCase(remain,currentReq)+currentReq.length()).split(" ");
						afterExtract=" ";
						for(int h = 0;h<contextAfter&&h<arrAfter.length;h++){
							afterExtract+= arrAfter[h]+" ";
						}
						String escapedBeforeExtract = StringEscapeUtils.escapeHtml4(beforeExtract);
						String escapedAfterExtract = StringEscapeUtils.escapeHtml4(afterExtract);
						out+= "..." + escapedBeforeExtract+"<b>"+currentReq+"</b>"+escapedAfterExtract.replace(currentReq,"<b>"+currentReq+"</b>");
						r = StringUtils.indexOfIgnoreCase(remain,currentReq)+afterExtract.length();
						try{
							remain = remain.substring(remain.indexOf(" ",r));
						}catch(StringIndexOutOfBoundsException sioob){
							remain = "";
						}
			    	}
			    	
			    }
				out+="</li>\n";
			}
			out+="</ul></div>";
			request.setAttribute("resultList",out);
		}catch(IOException ioe){
			request.setAttribute("isError",ioe.getMessage());
			ioe.printStackTrace();
		}catch(ManagedException me){
			/* ;-) */
			request.setAttribute("resultList",out);
		}catch(Exception pe){
			request.setAttribute("isError",pe.getMessage());
			pe.printStackTrace();
		}
		
		
		//request.setAttribute("debug", );
		String url="/search.jsp";
		ServletContext ctxt = getServletContext();
		RequestDispatcher rd = ctxt.getRequestDispatcher(url);
		rd.forward(request, response);
	}
}
