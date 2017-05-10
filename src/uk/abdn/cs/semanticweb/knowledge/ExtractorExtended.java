///////////////////////////////////////////////////////////////////////////////
// File: ExtractorExtended.java 
// Author: Isa Guclu  
// Date: April 2017
// Version: 0.03
// Comments: Class that extracts a RAM consumption-aware module from a given 
// 		set of desired concepts. 
// Modifications: 
// 		April 2017: Carlos Bobed
// 			- optimization of the previous code
// 		April 2017: Carlos Bobed	
// 			- modified the expansion of the axioms
///////////////////////////////////////////////////////////////////////////////

package uk.abdn.cs.semanticweb.knowledge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ExtractorExtended {

	private boolean dAddTimeStampToFileName = false;
	
	private Runtime runtime = Runtime.getRuntime();
	
	long startTime;
	long loadingTime;
	long processTime;
	long ontoWritingTime;

	/******************************************* Tests *******************************************/
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dOntoFolder = "assets";
		String dOntoFileName = "snomed_jan17.owl";
//		String dOntoFileName = "00540.owl_functional.owl"; 
		
		String dDesiredFileName = "Desired_4thScenario_snomed_jan17.txt";
		ExtractorExtended dCls = new ExtractorExtended();
		
		try {
			// the ontology to be modularized
			String dOntoOriginalFullPath = dOntoFolder + File.separator + dOntoFileName;
			System.out.println("dOntoOriginalFullPath : " + dOntoOriginalFullPath);

			// the file which contains the desired signature
			// one IRI per line
			String dDesiredFullPath = dOntoFolder + File.separator + dDesiredFileName;
			System.out.println("dDesiredFullPath : " + dDesiredFullPath);
			
			// the file which contains the required sizes
			BufferedReader br = new BufferedReader(new FileReader(dOntoFolder + File.separator + "AxiomCounts4thScenario.txt"));
			
			int dLine = 0;
			String line;
			while ((line = br.readLine()) != null) {
				dLine = Integer.parseInt(line);
				System.out.println("*** parseInt(line) : " + dLine);
				dCls.extractKnowledge(dOntoOriginalFullPath, dDesiredFullPath,dLine);
				Thread.sleep(1000L);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/******************************************* Tests *******************************************/
	
	public String extractKnowledge(String pOntologyFilePath,
			String pDesiredFilePath, int maxAxiomCount) throws Exception {
		
		String moduleFilenameHeader = "EXTV-"; 
		
		
		long usedMemoryBefore = (runtime.totalMemory() - runtime.freeMemory()) / 1024L;
		
		String dExtactedFileName = "";
		String dNow = StringOfNow();
		
		File ontFile = new File(pOntologyFilePath);
		String dCanonicalPath = folderCanonicalOfFile(ontFile);
		
		String dFileName = "";
		String dFileExtention = "";

		String dOutputFolder = dCanonicalPath + File.separator + "output";
		File directoryOutput = new File(dOutputFolder);
		if (!directoryOutput.exists()) {
			directoryOutput.mkdir();
		}
		
		String dLogFolder = dCanonicalPath + File.separator + "log";
		File directoryLog = new File(dLogFolder);
		if (!directoryLog.exists()) {
			directoryLog.mkdir();
		}
		
		try {
			
			startTime = System.nanoTime();
			
			// First of all, we load the ontology as usual
			OWLOntologyManager dOm = OWLManager.createOWLOntologyManager();
			dFileName = nameOfFile(ontFile);
			dFileExtention = extensionOfFile(ontFile);
			OWLOntology dOnt = dOm.loadOntologyFromOntologyDocument(ontFile);
			
			// Log File Name: DateTime_File.log
			String dLogFileName = directoryLog + File.separator + moduleFilenameHeader + "_" + dNow + "_" + dFileName + "_" + maxAxiomCount + ".log";
			PrintStream out = new PrintStream(new FileOutputStream(dLogFileName));
			System.setOut(out);
			System.out.println("--- Ontology Traversor ---");
			System.out.println("Used Memory before: " + usedMemoryBefore + " KB.");	

			HashSet<OWLAxiom> dAxioms = new HashSet<OWLAxiom>();
			// this hashset contains the expanded signatures
			HashSet<String> dSignatures = new HashSet<String>();
			// we get the desiredSignature
			HashSet<String> dDesiredSignatures = getDesired(pDesiredFilePath);
			
			// check the memory again
			long t1_RAM = (runtime.totalMemory() - runtime.freeMemory()) / 1024L;
			System.out.println("(Ontology Loaded to OWLOntology - RAM Beginning:" + usedMemoryBefore + ": KB. RAM End:" + t1_RAM
							+ ": KB. RAM Diff:" + (t1_RAM - usedMemoryBefore) + ": KB.)");
			long t1_Time = System.nanoTime();
			
			loadingTime = ((t1_Time - startTime) / 1000000L);
			System.out.println("(Loading Ontology by OWLAPI : " + loadingTime + " msec.)");
			
			// this hashset contains the axioms of the ontology
			HashSet<OWLAxiom> candidateAxioms = new HashSet<OWLAxiom>();
			candidateAxioms.addAll(dOnt.getLogicalAxioms());
			for (OWLEntity entity: dOnt.getSignature(true)) {
				candidateAxioms.addAll(dOnt.getDeclarationAxioms(entity)); 
			}
			
			dOnt = null;
			dOm = null;

			// output of the axioms copy
			// this copy can be done faster => without the checkings
			long t2_RAM = (runtime.totalMemory() - runtime.freeMemory()) / 1024L;
			System.out.println("(Copying Axioms to Hashset - RAM Beginning:" + usedMemoryBefore + ": KB. RAM End:" + t2_RAM
							+ ": KB. RAM Diff:" + (t2_RAM - usedMemoryBefore) + ": KB.)");
			long t2_Time = System.nanoTime();
			System.out.println("(Copying Axioms to Hashset took : " + (t2_Time - t1_Time) / 1000000L + " msec.)");
			
			System.out.println("AxiomCount in Ontology : " + candidateAxioms.size());
			System.out.println("* Desired Axioms: " + dDesiredSignatures.size());

			boolean dContinueIteration = true;
			int dPreviousAxiomCount = 0;
			int iterationCount = 0;
			
			while (dContinueIteration) {
				iterationCount++;
				dPreviousAxiomCount = dAxioms.size();
				// now the candidates are sieved
				dContinueIteration = findRelatedAxioms(candidateAxioms, dAxioms, dSignatures, dDesiredSignatures, maxAxiomCount);

				System.out.println("* iterationCount: " + iterationCount);
				System.out.println("Axioms.size(): " + dAxioms.size());
				System.out.println("DesiredSignatures.size(): " + dDesiredSignatures.size());
				System.out.println("ContinueIteration: " + dContinueIteration);
				System.out.println("dAxiomsOfOntology Size : " + candidateAxioms.size());
				if ((!dContinueIteration) || (dPreviousAxiomCount == dAxioms.size())) {
					break;
				}
			}
			candidateAxioms = null;
			dSignatures = null;
			dDesiredSignatures = null;

			// System.gc();

			processTime = System.nanoTime();
			System.out.println("(Extracting New Ontology : " + (processTime - t2_Time) / 1000000L + " msec.)");

			System.out.println("--- Stats ---");
			
			dExtactedFileName = dOutputFolder + File.separator + dNow + "_" + dFileName + "_" + maxAxiomCount + "."
					+ dFileExtention; // moduleFilenameHeader+
			if (!dAddTimeStampToFileName) {
				dExtactedFileName = dOutputFolder + File.separator + dFileName + "_" + maxAxiomCount + "." + dFileExtention; // +moduleFilenameHeader
			}
			 
			writeOntologyFromAxioms(dAxioms, dExtactedFileName, maxAxiomCount);

			ontoWritingTime = System.nanoTime();
			System.out.println("(Writing New Ontology : " + (ontoWritingTime - processTime) / 1000000L + " msec.)");
			
			long usedMemoryAfter = (runtime.totalMemory() - runtime.freeMemory()) / 1024L;
			System.out.println("RAM Beginning:" + usedMemoryBefore + ": KB. RAM End:" + usedMemoryAfter + ": KB. RAM Diff:"
					+ (usedMemoryAfter - usedMemoryBefore) + ": KB.");

			
			System.out.println("TOTAL PROCESSING TIME : " + (ontoWritingTime - t1_Time) / 1000000L + " msec.");
			
			
			System.out.println("--- The End ---");
			out.close();
		} catch (Exception e) {
		
			e.printStackTrace();
		}
		return dExtactedFileName;
	}

	/** 
	 * Obtains the desired signature read from the file which is passed as an argument
	 *  
	 * @param pDesiredFilePath File which contains the different elements conforming the desired
	 * 	signature (one IRI per line). 
	 * @return HashSet with the strings of the IRIs
	 */
	public HashSet<String> getDesired(String pDesiredFilePath) {
		HashSet<String> dDesiredSignatures = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(pDesiredFilePath));
			String dLine;
			while ((dLine = br.readLine()) != null) {

				if (!dLine.trim().equalsIgnoreCase("")) {
					System.out.println("Signature : " + dLine);
					if (dDesiredSignatures == null) {
						dDesiredSignatures = new HashSet<String>();
					}
					// Done this way to check validity of the format of the read IRIs
					dDesiredSignatures.add(IRI.create(dLine.trim()).toString());
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dDesiredSignatures;
	}

	/** 
	 * Select the axioms that have something to do with the given signature. It is done at distance 1, i.e., it selects the axioms 
	 * where the signature is participating directly.  
	 * @param candidateAxioms Set of candidate axioms to be selected
	 * @param pAxioms Set where the results are added to 
	 * @param pSignatures New elements found in the expanded axioms 
	 * @param pDesiredSignatures Original set of elements to be expanded
	 * @param maxAxiomCount
	 * @return
	 */
	public boolean findRelatedAxioms(HashSet<OWLAxiom> candidateAxioms,
			HashSet<OWLAxiom> pAxioms, HashSet<String> pSignatures,
			HashSet<String> pDesiredSignatures, int maxAxiomCount) {
		HashSet<OWLAxiom> addedAxioms = new HashSet<OWLAxiom>(); 
		for (OWLAxiom dAxiom : candidateAxioms) {
			if (isThereRelatedSignatures(dAxiom, pDesiredSignatures)) {
				pAxioms.add(dAxiom);
				for (OWLEntity dEntity: dAxiom.getSignature()) {
					pSignatures.add(dEntity.toString()); 
				}
				// we get rid of it 
				addedAxioms.add(dAxiom); 
			}
			if (pAxioms.size() >= maxAxiomCount) {
				System.out.println("* MAX AXIOM REACHED:" + pAxioms.size());
				return false;
			}
		}
		pDesiredSignatures.addAll(pSignatures);
		candidateAxioms.removeAll(addedAxioms); 
		pSignatures.clear();

		return true;
	}

	public static boolean isThereRelatedSignatures(OWLAxiom pAxiom,
			HashSet<String> pDesiredSignatures) {
		Set<OWLEntity> dEntities = pAxiom.getSignature(); 
		for (OWLEntity dEntity : dEntities) {
			String dIri = dEntity.toString();
			if (pDesiredSignatures.contains(dIri)) {
				return true;
			}
		}
		return false;
	}

	private void writeOntologyFromAxioms(HashSet<OWLAxiom> pAxioms,
			String pFullFileName, int maxAxiomCount) {
		try {
			System.out.println("* Axioms written to File: " + pFullFileName);
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology newonto = manager.createOntology();
			manager.addAxioms(newonto, pAxioms); 
			
			// added the replace for windows environments
			manager.saveOntology(newonto, new OWLFunctionalSyntaxOntologyFormat(),
					IRI.create("file:///" + pFullFileName.replace("\\", "/")));
			manager = null;
			newonto = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/******************************************* Utils *******************************************/
	
	public String StringOfNow() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss.SSS");
		Date date = new Date();
		String dNow = dateFormat.format(date);
		return dNow;
	}

	public String extensionOfFile(File pFile) {
		return pFile.getName().substring(pFile.getName().lastIndexOf(".") + 1,pFile.getName().length());
	}

	public String nameOfFile(File pFile) {
		return pFile.getName().substring(0, pFile.getName().lastIndexOf("."));
	}
	
	public String folderCanonicalOfFile(File pFile) {
		String dResult = "";
		try{
			int p = Math.max(pFile.getCanonicalPath().lastIndexOf("/"), pFile.getCanonicalPath().lastIndexOf("\\"));
			dResult = pFile.getCanonicalPath().substring(0, p);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dResult;
	}
}
