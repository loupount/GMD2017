package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

public class HPOindex {
	
	private String path;
	private FSDirectory index;
	private WhitespaceAnalyzer analyzer;
	
	public HPOindex() throws IOException{
		this.path="hpo/hp.obo";
		this.analyzer= new WhitespaceAnalyzer();
		this.index= SimpleFSDirectory.open(Paths.get("HpObo"));
	}
	
	public void indexing(){
		IndexWriterConfig configuration = new IndexWriterConfig(analyzer);
		configuration.setOpenMode(OpenMode.CREATE_OR_APPEND);
		try {
			IndexWriter writer = new IndexWriter(index, configuration);
			BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(path))));
			
			String read;
			int state =0;
			Document document = null;
			String line;
			while ((read=reader.readLine())!=null){
				if (read.startsWith("[Term]")){
					if (state==0){
						state=1;
					}
					else{
						writer.addDocument(document);
					}
					document = new Document();
				}
				
				if (read.startsWith("name:")){
					line = read.substring(6);
					TextField Name = new TextField("name", read, Field.Store.YES);
					document.add(Name);
				}
				
				if (read.startsWith("synonym:")){
					read = read.substring(10);
					line="";
					for (int charNb=0; charNb<=read.length();charNb++){
						if (read.charAt(charNb)== '"'){
							break;
						}
						else{
							line += read.charAt(charNb+1);
						}
						TextField Synonym= new TextField("synonym", line, Field.Store.YES);
						document.add(Synonym);
					}
				
					
				if (read.startsWith("id:")){
					line = read.substring(4);
					TextField Id = new TextField("id", read, Field.Store.YES);
					document.add(Id);
				}
					
				
				if (read.startsWith("def:")){
					read=read.substring(6);
					line="";
					for (int charNb=0; charNb<= read.length();charNb++){
						if(read.charAt(charNb)=='"'){
							break;
						}
						else{
							line += read.charAt(charNb+1);
						}
					}
					StoredField Def = new StoredField("def",line);
					document.add(Def);
				}
				
				if(read.startsWith("is_a:")){
					read = read.substring(6);
					line="";
					for (int charNb=0; charNb<=read.length();charNb++){
						if (read.charAt(charNb)=='!'){
							break;
						}
						else{
							line += read.charAt(charNb+1);
						}
					}
				}
					
				}
			}
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
