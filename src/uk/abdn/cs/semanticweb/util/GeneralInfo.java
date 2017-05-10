package uk.abdn.cs.semanticweb.util;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

public class GeneralInfo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {		

		GeneralInfo dCls = new GeneralInfo();
		dCls.infoFolder("C:/Users/r01ig15/Desktop/Func2", "General");
		
	}
	
	public String infoFolder(String pFolderPath, String pFormat) {

		File folder = new File(pFolderPath);
		File[] listOfFiles = folder.listFiles();
		
		for (File file : listOfFiles) {
			if (file.isFile()) {
				
				System.out.println("--- General Info of an Ontology ---");
				long processTime = 0;
				long start = System.nanoTime(); 
				
				if (pFormat.equalsIgnoreCase("General")){
					consoleAxiomTypesGeneral(pFolderPath + "/" + file.getName());
				}else if (pFormat.equalsIgnoreCase("Logical")){
					consoleAxiomTypesLogical(pFolderPath + "/" + file.getName());
				}
 				
				processTime = System.nanoTime() - start;
				System.out.println("--- End of Task. (" + (processTime/1000000) + " msec.) ---");
			}
		}

		return "";
	}

	
	
	
	/*
	 *  Isa: old usages
	 *  countABoxTBox("raw/snomed_jan11.owl");
	 *  	-> Ontology:raw/snomed_jan11.owl; AxiomCount:881256; Logical:293718; TBox:293706.0; ABox:0; ABox/TBox:0.0
	 *  countABoxTBox("raw/bookstoreTest3.owl");
	 *  	-> raw/bookstoreTest3.owl; AxiomCount:199; Logical:130; TBox15.0; ABox:115; ABox/TBox7.666666666666667
	 */

	
	public static void countABoxTBox(String pFileName){		
		try {

			File ontFile = new File(pFileName);
			OWLOntologyManager om = OWLManager.createOWLOntologyManager();
			OWLOntology ont = om.loadOntologyFromOntologyDocument(ontFile);
			double TBoxAxioms = ont.getTBoxAxioms(true).size();
			System.out.println("Ontology:" + ontFile.toString() + "; AxiomCount:"
					+ ont.getAxiomCount() + "; Logical:"
					+ ont.getLogicalAxiomCount() + "; TBox:" + TBoxAxioms
					+ "; ABox:" + ont.getABoxAxioms(true).size()
					+ "; ABox/TBox:"
					+ ((double) ont.getABoxAxioms(true).size() / TBoxAxioms));

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public static void consoleTBoxAxioms(String pFileName){		
		try {

			File ontFile = new File(pFileName);
			OWLOntologyManager om = OWLManager.createOWLOntologyManager();
			OWLOntology ont = om.loadOntologyFromOntologyDocument(ontFile);
			System.out.println(ont.getTBoxAxioms(true).toString().replace(",", ",\n"));
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public static void consoleAxiomTypesGeneral(String pFileName){		
		try {

			File ontFile = new File(pFileName);
			OWLOntologyManager om = OWLManager.createOWLOntologyManager();
			OWLOntology ont = om.loadOntologyFromOntologyDocument(ontFile);
			System.out.println("Ontology: " + ontFile.toString() + " - " + ont.getAxioms().size() + " axioms.");
			System.out.println("(Axiom Types)");
			System.out.println("-------------");
			// ArrayList<String> dList = new  ArrayList<String>();
			
			Map<String, Integer> dMap = new HashMap<String, Integer>();
			
			for (OWLAxiom axiom: ont.getAxioms()) {
				/*
				System.out.println("*** axiom:\t" + axiom.toString());
				System.out.println("ObjectPropertiesInSignature:\t" + axiom.getObjectPropertiesInSignature());
				System.out.println("ClassesInSignature:\t" + axiom.getClassesInSignature());
				System.out.println("DataPropertiesInSignature:\t" + axiom.getDataPropertiesInSignature());
				System.out.println("IndividualsInSignature:\t" + axiom.getIndividualsInSignature());
				System.out.println("NestedClassExpressions:\t" + axiom.getNestedClassExpressions());
				
				if (!dList.contains(axiom.getAxiomType().toString())){
					dList.add(axiom.getAxiomType().toString());
				}
				*/
				Integer dCount = dMap.get(axiom.getAxiomType().toString());
				if (dCount == null){
					dMap.put(axiom.getAxiomType().toString(), 1);
				}else{
					dMap.put(axiom.getAxiomType().toString(), dCount + 1);
				}
				
			}
			
			Iterator it = dMap.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry pair = (Map.Entry) it.next();
				System.out.println(pair.getKey() + " = " + pair.getValue());
				it.remove();
			}
			System.out.println("TOTAL = " + ont.getAxioms().size());
			
			/*
			for (int i = 0; i < dList.size(); i++){
				System.out.println((i+1) + ". " + dList.get(i));
			}
			*/
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public static void consoleAxiomTypesLogical(String pFileName){		
		try {

			File ontFile = new File(pFileName);
			OWLOntologyManager om = OWLManager.createOWLOntologyManager();
			OWLOntology ont = om.loadOntologyFromOntologyDocument(ontFile);
			System.out.println("Ontology: " + ontFile.toString() + " - " + ont.getAxioms().size() + " axioms.");
			System.out.println("(Logical Axiom Types)");
			System.out.println("-------------");
			// ArrayList<String> dList = new  ArrayList<String>();
			
			Map<String, Integer> dMap = new HashMap<String, Integer>();
			
			for (OWLAxiom axiom: ont.getLogicalAxioms()) {
				/*
				System.out.println("*** axiom:\t" + axiom.toString());
				System.out.println("ObjectPropertiesInSignature:\t" + axiom.getObjectPropertiesInSignature());
				System.out.println("ClassesInSignature:\t" + axiom.getClassesInSignature());
				System.out.println("DataPropertiesInSignature:\t" + axiom.getDataPropertiesInSignature());
				System.out.println("IndividualsInSignature:\t" + axiom.getIndividualsInSignature());
				System.out.println("NestedClassExpressions:\t" + axiom.getNestedClassExpressions());
				
				if (!dList.contains(axiom.getAxiomType().toString())){
					dList.add(axiom.getAxiomType().toString());
				}
				*/
				Integer dCount = dMap.get(axiom.getAxiomType().toString());
				if (dCount == null){
					dMap.put(axiom.getAxiomType().toString(), 1);
				}else{
					dMap.put(axiom.getAxiomType().toString(), dCount + 1);
				}
				
			}
			
			Iterator it = dMap.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry pair = (Map.Entry) it.next();
				System.out.println(pair.getKey() + " = " + pair.getValue());
				it.remove();
			}
			System.out.println("TOTAL = " + ont.getAxioms().size());
			
			/*
			for (int i = 0; i < dList.size(); i++){
				System.out.println((i+1) + ". " + dList.get(i));
			}
			*/
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
