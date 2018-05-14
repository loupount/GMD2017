package src;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public abstract class HPOread {
	
	private static ArrayList<String> idSymptome;
	private static ArrayList<String> idName;
	private static WhitespaceAnalyzer analyzer;
	
	private static void ReadHpObo(String query){
		Path uri= Paths.get("HpObo");
		try {
			FSDirectory index = FSDirectory.open(uri);
			IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(index));
			analyzer = new WhitespaceAnalyzer();
			
			
			String[] fields = {"name","synonym"};
			String[] queries = {query,query};
			BooleanClause.Occur[] occurs={BooleanClause.Occur.SHOULD,BooleanClause.Occur.SHOULD};
			try {
				Query q = MultiFieldQueryParser.parse(queries,fields,occurs, analyzer);
			
				TopDocs topDocs = indexSearcher.search(q, 10000);
				ScoreDoc[] scoreDoc = topDocs.scoreDocs;
				int hits = topDocs.totalHits;
				
				idSymptome = new ArrayList<String>();
				idName=new ArrayList<String>();
				
				if(hits!=0){
					scoreDoc = indexSearcher.search(q, hits).scoreDocs;
				}
				
				String symptomeName="";
				String symptomeId="";
				for (int hit=0; hit<hits; hit++){
					Document document = indexSearcher.doc(scoreDoc[hit].doc);
					symptomeId = document.get("id").substring(document.get("id").indexOf(":")+2);
			    	symptomeName = document.get("name").substring(document.get("name").indexOf(":")+2);
			    	idSymptome.add(symptomeId);
			    	idName.add(symptomeName);
				}
				
				
				
				
			
			
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	public static ArrayList<String> getIdSymptome(String query){
		ReadHpObo(query);
		return idSymptome;
	}
	
	public static ArrayList<String> getIdName(String query){
		ReadHpObo(query);
		return idName;
	}
	
	
	public static String searchForDefinition(String searchedWord){
		String definition="";
		ArrayList<String> symptomeId = getIdSymptome(searchedWord);
		for (String id : symptomeId){
			definition += "\n" + id + "\n";
		}
		return definition;
		
	}
}
