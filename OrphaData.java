package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class OrphaData {
	
	private String symptomNames;
	//ça sert pas ce parma:
	private ArrayList<OrphaData> listOrpha = new ArrayList<OrphaData>();
	
	
	public ArrayList<String> researchOrphaData(String symptom) throws IOException, ParseException{
		symptomNames=symptom;
		String[] symptoms = symptomNames.split(" ");
		JSONParser parser = new JSONParser();
		ArrayList<String> researchResult = new ArrayList<String>();
		URL connection = new URL("http://couchdb.telecomnancy.univ-lorraine.fr/orphadatabase/_design/clinicalsigns/_view/GetDiseaseByClinicalSign");
		InputStream stream = connection.openStream();
		InputStreamReader streamReader = new InputStreamReader(stream);
		BufferedReader reader = new BufferedReader(streamReader);
		Object parsed = parser.parse(reader);
		JSONObject jsonParsed = (JSONObject) parsed;
		JSONArray array = ((JSONArray) jsonParsed.get("rows"));
		
		
		if(array!=null){
			for (int rowNb=0; rowNb<array.size();rowNb++){
				JSONObject row = (JSONObject) array.get(rowNb);
				
				//ça sert à R
				String rowId= (String) row.get("id");
				String rowKey = (String) row.get("key");
				//
				
				JSONObject rowValue = (JSONObject) row.get("value");
				JSONObject rowDisease = (JSONObject) row.get("disease");
				//ça non plus
				String idDisease = (String) rowDisease.get("id");
				//
				JSONObject nameDisease = (JSONObject) rowDisease.get("Name");
				
				String textDisease = (String) nameDisease.get("text");
				
				
				JSONObject diseaseClinicalSign = (JSONObject) rowValue.get("clinicalSign");
				//ça non plus
				String diseaseClinicalSignId = (String) diseaseClinicalSign.get("id");
				//
				JSONObject diseaseClinicalSignName = (JSONObject) diseaseClinicalSign.get("Name");
				String diseaseClinicalSignText = (String) diseaseClinicalSignName.get("text");
				
				int count=0;
				for (String symp: symptoms){
					if(diseaseClinicalSignText.contains(symp)){
						count += 1;
					}
				}
				
				if(count==symptoms.length){
					researchResult.add(textDisease);
				}
			}
		}
		return researchResult;
	}
}
