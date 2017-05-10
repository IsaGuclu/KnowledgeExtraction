package uk.abdn.cs.semanticweb.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Translator {

	/******************************************* Tests *******************************************/
	public static void main(String[] args) {

		try{
			
		/*
		PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
		System.setOut(out);
		*/
		Translator dCls = new Translator();
		// dCls.convertFolder("C:/Users/r01ig15/Desktop/1", "OWLXML");
		// dCls.convertFolder("C:/Users/r01ig15/Desktop/OWLXML", "Functional");		
		// dCls.translate2OWLXML("C:/Users/r01ig15/Desktop/3/DerGrossOntology.owl_functional.owl");
		
		// dCls.translate2FunctionalWOAnnotations("C:/Users/r01ig15/Desktop/OWLXML/go.owl");
		dCls.translate2FunctionalWOAnnotations("C:/Users/r01ig15/Desktop/OWLXML/emap.owl");
		dCls.translate2FunctionalWOAnnotations("C:/Users/r01ig15/Desktop/OWLXML/fma-el.owl");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/******************************************* Tests *******************************************/
	
	public String convertFolder(String pFolderPath, String pFormat) {

		File folder = new File(pFolderPath);
		File[] listOfFiles = folder.listFiles();
		
		for (File file : listOfFiles) {
			if (file.isFile()) {
				System.out.print(pFolderPath + "/" + file.getName() + "\t");
				if (pFormat.equalsIgnoreCase("OWLXML")){
					translate2OWLXML(pFolderPath + "/" + file.getName());
				}else if (pFormat.equalsIgnoreCase("Functional")){
					translate2Functional(pFolderPath + "/" + file.getName());
				}
 				
			}
		}

		return "";
	}

	public String translate2OWLXML(String pOntologyCannocicalName) {

		File dOntology = new File(pOntologyCannocicalName);
		String dOutputFolder = "";
		String dOutputFile = "";
		OWLOntologyManager manager = null;
		IRI ontologyIRI = null;
		OWLOntology ont = null;

		try {

			dOutputFolder = folderCanonicalOfFile(dOntology) + "/" + "OWLXML";
			File directoryOutput = new File(dOutputFolder);
			if (!directoryOutput.exists()) {
				directoryOutput.mkdir();
			}

			manager = OWLManager.createOWLOntologyManager();
			ontologyIRI = IRI.create("file:////" + pOntologyCannocicalName);
			ont = manager.loadOntologyFromOntologyDocument(ontologyIRI);

			dOutputFile = dOutputFolder + "/" + excludeExtentionOfFile(dOntology.getName()) + "_2OWLXML.owl";
			// System.out.println(dOutputFile);
			System.out.println("dOutputFile: " + dOutputFile);
			
			File f = new File(dOutputFile);
			IRI documentIRI = IRI.create(f);
			manager.saveOntology(ont, new OWLXMLOntologyFormat(), documentIRI);
			manager.removeOntology(ont);
			System.gc();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public String translate2Functional(String pOntologyCannocicalName) {

		File dOntology = new File(pOntologyCannocicalName);
		String dOutputFolder = "";
		String dOutputFile = "";
		OWLOntologyManager manager = null;
		IRI ontologyIRI = null;
		OWLOntology ont = null;

		try {

			dOutputFolder = folderCanonicalOfFile(dOntology) + "/" + "FunctionalSyntax";
			File directoryOutput = new File(dOutputFolder);
			if (!directoryOutput.exists()) {
				directoryOutput.mkdir();
			}

			manager = OWLManager.createOWLOntologyManager();
			ontologyIRI = IRI.create("file:////" + pOntologyCannocicalName);
			ont = manager.loadOntologyFromOntologyDocument(ontologyIRI);

			dOutputFile = dOutputFolder + "/" + excludeExtentionOfFile(dOntology.getName()) + "_Func.owl";
			// System.out.println(dOutputFile);
			System.out.println("dOutputFile: " + dOutputFile);
			
			File f = new File(dOutputFile);
			IRI documentIRI = IRI.create(f);
			manager.saveOntology(ont, new OWLFunctionalSyntaxOntologyFormat(), documentIRI);
			manager.removeOntology(ont);
			System.gc();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public String translate2FunctionalWOAnnotations(String pOntologyCannocicalName) {
		
			File dOntology = new File(pOntologyCannocicalName);
			String dOutputFolder = "";
			String dOutputFile = "";
			OWLOntologyManager manager = null;
			IRI ontologyIRI = null;
			OWLOntology ont = null;

			try {

				dOutputFolder = folderCanonicalOfFile(dOntology) + "/" + "FunctionalSyntax";
				File directoryOutput = new File(dOutputFolder);
				if (!directoryOutput.exists()) {
					directoryOutput.mkdir();
				}

				manager = OWLManager.createOWLOntologyManager();
				ontologyIRI = IRI.create("file:////" + pOntologyCannocicalName);
				ont = manager.loadOntologyFromOntologyDocument(ontologyIRI);

				// AnnotationAxiom := AnnotationAssertion | SubAnnotationPropertyOf | AnnotationPropertyDomain | AnnotationPropertyRange
				HashSet<OWLAxiom> dAxiomsOfOntology = new HashSet<OWLAxiom>();
				for (OWLAxiom dAxiom : ont.getAxioms()) {
					if ((!dAxiom.getAxiomType().toString()
							.equalsIgnoreCase("AnnotationAssertion"))
							&& (!dAxiom.getAxiomType().toString()
									.equalsIgnoreCase("SubAnnotationPropertyOf"))
							&& (!dAxiom.getAxiomType().toString()
									.equalsIgnoreCase("AnnotationPropertyDomain"))
							&& (!dAxiom.getAxiomType().toString()
									.equalsIgnoreCase("AnnotationPropertyRange"))) {
						
						dAxiomsOfOntology.add(dAxiom);
					}
				}
				
				dOutputFile = dOutputFolder + "/" + excludeExtentionOfFile(dOntology.getName()) + "_FuncWOAnnot.owl";
				System.out.println("dOutputFile: " + dOutputFile);

				writeOntologyFromAxioms(dAxiomsOfOntology,"Functional",dOutputFile);
				
				manager.removeOntology(ont);
				System.gc();
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			return "";
		}
	
/******************************************* OWL-Utils *******************************************/

private void writeOntologyFromAxioms(HashSet<OWLAxiom> pAxioms, String pFormat, String pFullFileName) {
	try {
		pFullFileName = pFullFileName.replace("\\", "/");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology newonto = manager.createOntology();
		manager.addAxioms(newonto, pAxioms);
		
		if (pFormat.equalsIgnoreCase("Functional")){
			manager.saveOntology(newonto, new OWLFunctionalSyntaxOntologyFormat(),
					IRI.create("file:////" + pFullFileName));	
		}
		
		System.out.println("* Axioms written to File: " + pFullFileName);
		manager = null;
		newonto = null;
	} catch (Exception e) {
		e.printStackTrace();
	}
}
	
	/******************************************* Java-Utils *******************************************/
	
	public String excludeExtentionOfFile(String pFileName) {
		return pFileName.substring(0, pFileName.lastIndexOf("."));
	}

	public String folderCanonicalOfFile(File pFile) {
		String dResult = "";
		try {
			int p = Math.max(pFile.getCanonicalPath().lastIndexOf("/"), pFile.getCanonicalPath().lastIndexOf("\\"));
			dResult = pFile.getCanonicalPath().substring(0, p);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dResult;
	}
	
	public String StringOfNow() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss.SSS");
		Date date = new Date();
		String dNow = dateFormat.format(date);
		return dNow;
	}

}
