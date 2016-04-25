package ca.diro.javadocindexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.w3c.dom.Node;

import ca.diro.javadocindexer.Indexer.DocType;

public class JavadocIndexerUtilities {

	public static int insert;
	public static Analyzer getAnalyzer(){
		StandardAnalyzer sa = new StandardAnalyzer(Version.LUCENE_34);
		KeywordAnalyzer kwa = new KeywordAnalyzer();
		PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(sa);
		analyzer.addAnalyzer("classTitle", kwa);
		analyzer.addAnalyzer("returnType", kwa);
		analyzer.addAnalyzer("methodNames", kwa);
		analyzer.addAnalyzer("camelCase", kwa);
		analyzer.addAnalyzer("path", kwa);
		//analyzer.addAnalyzer("contents",sa);
		return analyzer;
	}
	
	public static DocType getDocType(Node node){
		Node current = node;
		do{
			do{
				do{
					current = getNextNode(current);
				}while(current!=null && current.getClass()!=org.apache.xerces.dom.CommentImpl.class);
			}while(current!=null && !current.getNodeValue().contains("==="));
			if(current!=null){
				//Le critère pour savoir si le fichier en considÃ©ration est une classe
				if(current.getNodeValue().equals(" ======== START OF CLASS DATA ======== ")){
					return DocType.CLASSE;
				}
			}
		}while(current!=null && !current.equals(node));
		return DocType.AUTRES;
	}
	public static Node getNextNode(Node current){
		if(current==null){
			System.out.println("NODE IS NULL");
			return null;
		}
		boolean found = false;
		boolean childVisited = false;
		while(found !=true){
			if(current.hasChildNodes() && childVisited == false){
				current = current.getFirstChild();
				found = true;
			}else{
				if(current.getNextSibling()!=null){
					current = current.getNextSibling();
					found = true;
					childVisited = false;
				}else{
					if(current.getParentNode()!=null)
						current = current.getParentNode();
					else{
						return null;
					}
					childVisited = true;
				}
			}
		}
		return current;
	}
	
	/**
	 * http://stackoverflow.com/questions/2843366/how-to-add-new-elements-to-a-string-array
	 * @param <T>
	 * @param arr
	 * @param element
	 * @return
	 */
	static <T> T[] append(T[] arr, T element) {
	    final int N = arr.length;
	    Object[] newArray = new Object[N+1];
	    for(int i = 0;i<N ;i++){newArray[i]=arr[i];}
	    //arr = java.util.Arrays.copyOf(arr, N + 1);
	    newArray[N] = element;
	    return (T[]) newArray;
	}
	static <T> T[] insert(T[] arr, T element) {
		
		final int N =arr.length;
		@SuppressWarnings("unchecked")
		T[] newArray = (T[])new Object[N+1];
		//arr = java.util.Arrays.copyOf(arr,N+1);
		for(int i = N;i>0;i--){
			newArray[i] = arr[i-1];
		}
		newArray[0] = element;
		return (T[]) newArray;
	}
}
