package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;

public class Omim {
	private String path;
	private String doc;
	private FSDirectory index;
	private WhitespaceAnalyzer analyzer;
	private static int create=0;
	
	
	
	public Omim(){
		this.path="indexOmim";
		this.doc="omim.txt";
	}
	
	public void indexOmim() throws IOException{
		index = FSDirectory.open(Paths.get(path));
		analyzer = new WhitespaceAnalyzer();
		IndexWriterConfig configuration = new IndexWriterConfig(analyzer);
		configuration.setOpenMode(OpenMode.CREATE);
		IndexWriter w = new IndexWriter(index,configuration);
		if(Files.isDirectory(Paths.get(doc))){
			
			SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>(){
				public FileVisitResult fileVisit(String pathFile, BasicFileAttributes attributs) throws IOException{
					indexingFile(w, Paths.get(pathFile));
					return FileVisitResult.CONTINUE;
				}
				
			};
			Files.walkFileTree(Paths.get(doc), visitor);
			
		}
		else{
			indexingFile(w,Paths.get(doc));
		}
	}
	
	static void indexingFile(IndexWriter w, Path pathFile) throws IOException{
		
		InputStream inputStream = Files.newInputStream(pathFile);
		File file = new File(pathFile.toString());
		FileReader fileReader = new FileReader(file);
		BufferedReader reader = new BufferedReader(fileReader);
		
		String start="*RECORCD*";
		String end="*FIELD* ED";
		
		
		String bloc="";
		Document document = new Document();
		while(reader.readLine()!=null){
			
			if(reader.readLine().contains(start)){
				document = new Document();
				document.add(new StoredField("id",reader.readLine().substring(8)));
				
			}
			
			else if (reader.readLine().contains(end)){
				if (create==0){
					w.addDocument(document);
				}
				else{
					Term term = new Term("path", file.toString());
					w.updateDocument(term, document);
					
				}
			}
			
			else if (reader.readLine().contains(start)==false && reader.readLine().contains(end)==false){
				if(reader.readLine()==("FIELD* NO")){
					String line = reader.readLine();
					document.add(new StringField("Disease_id", line, TextField.Store.YES));
					
				}
			}
		}
	}
}
