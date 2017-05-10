package uk.abdn.cs.semanticweb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class LogParser {

	public static void main(String[] args) {
		
		LogParser dCls = new LogParser();
		dCls.processFolder("C:/Users/r01ig15/Desktop/LogsRuningExtracted");
		// dCls.processFolder("/Users/isa/Desktop/LogsRuningExtracted");
	}

	public String processFolder(String pFolderPath) {

		File folder = new File(pFolderPath);
		File[] listOfFiles = folder.listFiles();
		
		for (File file : listOfFiles) {
			if (file.isFile()) {
				//System.out.println(pFolderPath + "/" + file.getName());
				if (!file.getName().startsWith(".")){
					fromFileToConsole1(pFolderPath + "/" + file.getName());
				}
			}
		}

		return "";
	}
	
	public void fromFileToConsole1(String pFileName) {
		
		File dFile = new File(pFileName);
		String[] dFilePieces = dFile.getName().toString().split("_");
		System.out.print(dFilePieces[3] + "\t" ); //  + "_" + dFilePieces[5]
		BufferedReader br;
		String line;
		
		
		try {			
			br = new BufferedReader(new FileReader(dFile));
		    while ((line = br.readLine()) != null) {
		    	if (line.contains("RAM End")){
		    		String[] dLinePieces = line.split(":");    		
		    		System.out.print("RAM" + "\t" + dLinePieces[3] + " KB.\t");
		    	}
		    	if (line.contains("ELK took")){
		    		String[] dLinePieces = line.split("took ");    		
		    		System.out.print("Time" + "\t" + dLinePieces[1] + "\t");
		    	}

		    }
		    System.out.println("");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	
	}
	
}
