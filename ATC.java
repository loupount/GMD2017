package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.SimpleFSDirectory;

public class ATC {
	
	private String path;
	private WhitespaceAnalyzer analyzer;
	private SimpleFSDirectory index;
	
	
	public ATC() throws IOException{
		this.path="filesToIndex/ATC.keg";
		this.analyzer= new WhitespaceAnalyzer();
		index = new SimpleFSDirectory(Paths.get("indexDirectory/ATCindex"));
	}
	
	public void indexing(){
		File file = new File(path);
		FileReader fileReader;
		try {
			fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader);
			IndexWriterConfig configuration = new IndexWriterConfig(analyzer);
			try {
				IndexWriter indexWriter = new IndexWriter(index, configuration);
				String read;
				while ((read=reader.readLine())!=null){
						
					String code;
					String name;
					String processing;
					int i=0;
					name="";
					processing="";
					code="";
					for (int charNb=0; charNb<read.length(); charNb++){
						
						if (read.charAt(charNb)==' ' || read.charAt(charNb)=='\t'){
							if (i==0){
								continue;
							}
							else{
								code=processing;
								processing="";
								i=2;
							}
						}
						else{
							if (i!=2){
								processing= processing + read.charAt(charNb);
								i=1;
							}
							else{
								name=read.substring(charNb);
								break;
							}
						}
					}
					addToIndex(indexWriter, ""+ read.charAt(0), code, name);
				}
				indexWriter.close();
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
		
		
	public void addToIndex(IndexWriter indexWriter, String hierarchy, String code, String name){
		Document document = new Document();
		StringField Hierarchy = new StringField("hierarchy", hierarchy.toLowerCase(), Field.Store.YES);
		StringField Code = new StringField("code",code.toLowerCase(), Field.Store.YES);
		StringField Name = new StringField("name", name.toLowerCase(), Field.Store.YES);
		document.add(Hierarchy);
		document.add(Code);
		document.add(Name);
		try {
			indexWriter.addDocument(document);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
		
		
	public ArrayList<ArrayList<String>> research(String researchKey, String field){
		QueryParser parser = new QueryParser(field, analyzer);
		parser.setDefaultOperator(QueryParser.Operator.AND);
		try {
			Query query = parser.parse(researchKey.toLowerCase());
			try {
				IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(index));
				TopDocs topDocs = indexSearcher.search(query, 500);
				ScoreDoc[] score = topDocs.scoreDocs;
				ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
				for ( int k =0; k <score.length; k++){
					ArrayList<String> array = new ArrayList<String>();
					int id=score[k].doc;
					Document document = indexSearcher.doc(id);
					for (IndexableField fields:document.getFields()){
						array.add(fields.stringValue().toUpperCase());
					}
					result.add(array);
				}
				return result;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
