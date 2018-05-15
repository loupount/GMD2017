package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.FSDirectory;

public class OmimOnto {
	private String path;
	private FSDirectory index;
	private WhitespaceAnalyzer analyzer;
	
	public OmimOnto(){
		this.path="omim_nto.csv";
	}
	
	public void indexing() throws IOException{
		index = FSDirectory.open(Paths.get("indexOmimOnto"));
		analyzer = new WhitespaceAnalyzer();
		IndexWriterConfig configuration = new IndexWriterConfig(analyzer);
		configuration.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter w = new IndexWriter (index, configuration);
		if(Files.isDirectory(Paths.get("indexOmimOnto"))){
			
			SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>(){
				public FileVisitResult fileVisit(String pathFile, BasicFileAttributes attributs) throws IOException{
					indexingFile(w, Paths.get(pathFile));
					return FileVisitResult.CONTINUE;
				}
				
			};
			Files.walkFileTree(Paths.get("indexOmimOnto"), visitor);
			
		}
		else{
			indexingFile(w,Paths.get("indexOmimOnto"));
		}
	}
	
	public void indexingFile(IndexWriter w, Path pathFile) throws IOException{
		InputStream inputStream = Files.newInputStream(pathFile);
		File file = new File(pathFile.toString());
		FileReader fileReader = new FileReader(file);
		BufferedReader reader = new BufferedReader(fileReader);
		
		
		
		String read;
		while((read=reader.readLine())!=null){
			Document document = new Document();
			StoredField storedId = new StoredField("id",read);
			document.add(storedId);
			
			String[] line = read.split(";");
			String idClass= (line[0]).substring(42);
			document.add(new TextField("idClass",idClass,TextField.Store.YES));
			
			
			document.add(new TextField("label",line[1],TextField.Store.YES));
			
			document.add(new TextField("cui",line[5],TextField.Store.YES));
			
			/////////////////
			if (w.getConfig().getOpenMode() == OpenMode.CREATE) {
	               // New index, so we just add the document (no old document can be there):
	               w.addDocument(document);       
	             } 
		  else {
	              
	               w.updateDocument(new Term("path", file.toString()), document);
	             }	
			
			///////////////////
			
		}
		reader.close();
		
	}
	
	public ArrayList<ArrayList<String>> research(String reasearchKey,String field) throws ParseException, IOException{
		analyzer = new WhitespaceAnalyzer();
		QueryParser parser = new QueryParser(field, analyzer);
		parser.setDefaultOperator(QueryParser.Operator.AND);
		Query query = parser.parse(reasearchKey.toLowerCase());
		index = FSDirectory.open(Paths.get("indexOmimOnto"));
		IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(index));
		TopDocs topDocs = indexSearcher.search(query, 30);
		ScoreDoc[] score = topDocs.scoreDocs;
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		for (int hit=0; hit<score.length; hit++){
			ArrayList<String> array = new ArrayList<String>();
			int idDoc = score[hit].doc;
			Document document = indexSearcher.doc(idDoc);
			for (IndexableField fields: document.getFields()){
				array.add(fields.stringValue().toUpperCase());
			}
			result.add(array);
		}
		return result;
		
		
	}
	
	public void researchFieldOminOnto(String field, String queries, String queryString) throws IOException, ParseException{
		index = FSDirectory.open(Paths.get("indexOmimOnto"));
		IndexSearcher indexSearcher =new IndexSearcher(DirectoryReader.open(index));
		analyzer = new WhitespaceAnalyzer();
		BufferedReader reader = Files.newBufferedReader(Paths.get(queries));
		QueryParser parser = new QueryParser(field,analyzer);
		Query query = parser.parse(queryString);
		indexSearcher.search(query, 100);
		reasearchPage(field, reader, indexSearcher, query );
		
		
	}
	
	public void reasearchPage(String field, BufferedReader reader,IndexSearcher indexSearcher, Query query) throws IOException{
		TopDocs topDocs = indexSearcher.search(query, 50);
		ScoreDoc[] score = topDocs.scoreDocs;
		
		int hits=topDocs.totalHits;
		
		int firstHit=0;
		int lastHit=Math.min(hits, 10);
		boolean bool=false;
		while(true){
			if(lastHit>score.length){
				if((reader.readLine()).length()==0){
					break;
				}
				score = indexSearcher.search(query, hits).scoreDocs;
			}
			lastHit= Math.min(score.length, firstHit+10);
			
			for (int hit=firstHit;hit<lastHit;hit++){
				if(bool){
					System.out.println("dolastHit="+score[hit].doc+" score="+score[hit].score);
		              continue;
				}
				
				Document dolastHitument = indexSearcher.doc(score[hit].doc);
				String fieldPath = dolastHitument.get(field);
				System.out.println((hit+1) + ". " + path);
			}
			
			if(lastHit==0){
				break;
			}
			
			if(hits>=lastHit){
				int i=0;
				while (i==0){
					System.out.print("Press");
					if(firstHit-10>=0){
						System.out.print("(p)revious page, ");  
		              }
		             if (firstHit + 10 < hits) {
		                System.out.print("(n)ext page, ");
		              }
		             System.out.println("(q)uit or enter number to jump to a page.");
		              
		             String read=reader.readLine();
		             
		             if(read.charAt(0)=='p'){
		            	 firstHit=Math.max(0, firstHit-10);		             
					}
		             
		             if(read.charAt(0)=='n'){
		            	 if(firstHit+10<=hits){
		            		 firstHit=firstHit+10;
		            	 }
		            	 i=1;
		            	 
		             }
		             
		             if(read.charAt(0)=='q'){
		            	 i=1;
		             }
		             
		             else{
		            	 int numberOfPage = Integer.parseInt(read);
		            	 if((numberOfPage-1)*10<hits){
		            		 firstHit=(numberOfPage-1)*10;
		            		 i=1;
		            	 }
		            	 
		             }
		             
					
				}
				lastHit=Math.min(hits, firstHit+10);
			}
			
			}
		
	}
}
