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

public class Stitch {
	
	private String path;
	private WhitespaceAnalyzer analyser;
	private SimpleFSDirectory index;
	
	
	public Stitch() throws IOException{
		this.path="filesToIndex/chemical sources.tsv";
		this.analyser = new WhitespaceAnalyzer();
		this.index = new SimpleFSDirectory(Paths.get("indexDirectory/Stitchindex"));
	}
	
	
	public void indexing(){
		File file = new File(path);
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader);
			IndexWriterConfig configuration = new IndexWriterConfig(analyser);
			try {
				IndexWriter indexWriter = new IndexWriter(index, configuration);
				String line;
				while (  (line = reader.readLine()) != null){
					String[] array = line.split("\t");
					if (array.length==4){
						if (array[2]=="ATC"){
							addToIndex(indexWriter, array[0], array[1], array[3]);
						}
					}
				}
				reader.close();
				indexWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public void addToIndex(IndexWriter indexWriter, String chemicalName, String alias, String nbATC){
		Document document = new Document();
		StringField Chemical = new StringField("chemical", chemicalName.toLowerCase(), Field.Store.YES);
		StringField Alias = new StringField("alias",alias.toLowerCase(), Field.Store.YES);
		StringField NbATC = new StringField("nbATC", nbATC.toLowerCase(), Field.Store.YES);
		document.add(Chemical);
		document.add(Alias);
		document.add(NbATC);
		try {
			indexWriter.addDocument(document);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<ArrayList<String>> research(String researchKey, String field){
		QueryParser parser = new QueryParser(field, analyser);
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
