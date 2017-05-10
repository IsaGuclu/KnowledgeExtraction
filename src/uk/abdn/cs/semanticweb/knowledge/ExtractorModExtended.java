/////////////////////////////////////////////////////////////////////////////
// File: ExtractorModExtended.java 
// Author: Carlos Bobed 
// Date: April 2017
// Version: 0.02
// Comments: Class that implements the different extraction strategies using 
// 			Cuenca's modules as starting point and the agnostic extraction to finish
// 			it. 
// Modifications: 
// 		- Pametrized the type of module to be used
///////////////////////////////////////////////////////////////////////////////

package uk.abdn.cs.semanticweb.knowledge;

import java.beans.DesignMode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.syntactic_locality.ModuleExtractor;
import uk.ac.manchester.syntactic_locality.ModuleExtractorManager;

public class ExtractorModExtended {

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
		// String dOntoFileName = "00540.owl_functional.owl"; 
		
		String dDesiredFileName = "Desired_4thScenario_snomed_jan17_LME.txt";
		ExtractorModExtended dCls = new ExtractorModExtended();
		String[] dModuleTypes = {"LM", "UM", "LUM", "ULM", "DCM", "DRM"};
		
		
			try {

				String dOntoOriginalFullPath = dOntoFolder + File.separator + dOntoFileName;
				System.out.println("dOntoOriginalFullPath : " + dOntoOriginalFullPath);
				String dDesiredFullPath = dOntoFolder + File.separator + dDesiredFileName;
				System.out.println("dDesiredFullPath : " + dDesiredFullPath);
				
				BufferedReader br = new BufferedReader(new FileReader(dOntoFolder + File.separator + "AxiomCounts4thScenario.txt"));
				
				int dLine = 0;
				String line;
				while ((line = br.readLine()) != null) {
					dLine = Integer.parseInt(line);
					System.out.println("*** parseInt(line) : " + dLine);
					for(int i=0; i< dModuleTypes.length ;i++){
						dCls.extractKnowledge(dOntoOriginalFullPath, dDesiredFullPath,dLine, dModuleTypes[i]);
						Thread.sleep(1000L);
					}
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		
	}
	/******************************************* Tests *******************************************/
	
	public String extractKnowledge(String pOntologyFilePath,
			String pDesiredFilePath, int maxAxiomCount, String moduleType) throws Exception {
		
		String moduleFilenameHeader = moduleType+"-EXT-"; 
		
		startTime = System.nanoTime();
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
			// First of all, we load the ontology as usual
			OWLOntologyManager dOm = OWLManager.createOWLOntologyManager();
			dFileName = nameOfFile(ontFile);
			dFileExtention = extensionOfFile(ontFile);
			OWLOntology dOriginalOnt = dOm.loadOntologyFromOntologyDocument(ontFile);
			
			// Log File Name: DateTime_File.log
			String dLogFileName = directoryLog + File.separator + moduleFilenameHeader + "_" + dNow + "_" + dFileName + "_" + moduleType + "_" + maxAxiomCount + ".log";
			PrintStream out = new PrintStream(new FileOutputStream(dLogFileName));
			System.setOut(out);
			System.out.println("--- Ontology Traversor ---");
			System.out.println("--- Type of modules: "+moduleType); 
			System.out.println("Used Memory before: " + usedMemoryBefore + " KB.");	
			
			ModuleExtractorManager moduleManager = new ModuleExtractorManager(dOriginalOnt, moduleType, true, false, false);
			
			HashSet<OWLAxiom> dAxioms = new HashSet<OWLAxiom>();
			// this hashset contains the expanded signatures
			HashSet<OWLEntity> dSignatures = new HashSet<OWLEntity>();
			// we get the desiredSignature
			HashSet<OWLEntity> dDesiredSignatures = getDesired(pDesiredFilePath, dOriginalOnt);
			System.out.println("Matched "+dDesiredSignatures.size()+" entities in the ontology"); 
			// check the memory again
			long t1_RAM = (runtime.totalMemory() - runtime.freeMemory()) / 1024L;
			System.out.println("(Ontology Loaded to OWLOntology - RAM Beginning:" + usedMemoryBefore + ": KB. RAM End:" + t1_RAM
							+ ": KB. RAM Diff:" + (t1_RAM - usedMemoryBefore) + ": KB.)");
			long t1_Time = System.nanoTime();

			// we extract the first module
			OWLOntology moduleOntology = moduleManager.extractModule(dDesiredSignatures); 
			
			// now, if the axiom count is above the threshold 
			// we add to the candidate axioms the whole module
			// if not, only the difference
			HashSet<OWLAxiom> candidateAxioms = new HashSet<>(); 
			if (moduleOntology.getAxiomCount() > maxAxiomCount) {
				candidateAxioms.addAll(moduleOntology.getLogicalAxioms()); 
				for (OWLEntity entity: moduleOntology.getSignature(true)) {
					candidateAxioms.addAll(moduleOntology.getDeclarationAxioms(entity)); 
				}
			}
			else {
				// we add directly the axioms that are contained in the module
				dAxioms.addAll(moduleOntology.getAxioms());
				// we extend the signature to the signature of the extracted module
				dDesiredSignatures.addAll(moduleOntology.getSignature(true)); 
				
				candidateAxioms.addAll(dOriginalOnt.getLogicalAxioms()); 
				candidateAxioms.removeAll(moduleOntology.getLogicalAxioms()); 
				
				// we add only the declarationAxioms that are not already included
				// in dAxioms
				for (OWLEntity entity: dOriginalOnt.getSignature(true)) {					
					for (OWLDeclarationAxiom decAx: dOriginalOnt.getDeclarationAxioms(entity)) {
						if (!dAxioms.contains(decAx)){
							candidateAxioms.add(decAx); 
						}
					} 
				}
			}
			
			// output of the axioms copy
			// this copy can be done faster => without the checkings
			long t2_RAM = (runtime.totalMemory() - runtime.freeMemory()) / 1024L;
			System.out.println("(Copying Axioms to Hashset - RAM Beginning:" + usedMemoryBefore + ": KB. RAM End:" + t2_RAM
							+ ": KB. RAM Diff:" + (t2_RAM - usedMemoryBefore) + ": KB.)");
			long t2_Time = System.nanoTime();
			System.out.println("(Copying Axioms to Hashset took : " + (t2_Time - t1_Time) / 1000000L + " msec.)");
			loadingTime = ((System.nanoTime() - startTime) / 1000000L);
			System.out.println("(So far Time passed : " + loadingTime + " msec.)");

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
			
			dExtactedFileName = dOutputFolder + File.separator + moduleFilenameHeader + "_" + dNow + "_" + dFileName + "_" + moduleType + "_" + maxAxiomCount + "." + dFileExtention;
			if (!dAddTimeStampToFileName) {
				dExtactedFileName = dOutputFolder + File.separator + moduleFilenameHeader + "_" + dFileName + "_" + moduleType + "_" + maxAxiomCount + "." + dFileExtention;
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
	 * @param originalOntology OWLOntology which the entities have to be matched to  
	 * @return HashSet with the OWLEntities of the ontology
	 */
	public HashSet<OWLEntity> getDesired(String pDesiredFilePath, OWLOntology originalOntology) {
		HashSet<OWLEntity> dDesiredSignatures = null;
		Hashtable<String, OWLEntity> entityTable = new Hashtable<>(); 		
		for (OWLEntity ent: originalOntology.getSignature()) {
			entityTable.put(ent.getIRI().toString(), ent); 
		}		
		try {
			BufferedReader br = new BufferedReader(new FileReader(pDesiredFilePath));
			String dLine;
			while ((dLine = br.readLine()) != null) {
				if (!dLine.trim().equalsIgnoreCase("")) {
					System.out.println("Signature : " + dLine);
					if (dDesiredSignatures == null) {
						dDesiredSignatures = new HashSet<OWLEntity>();
					}
					if (entityTable.containsKey(dLine.trim())) {
						dDesiredSignatures.add(entityTable.get(dLine.trim())); 
					}
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
			HashSet<OWLAxiom> pAxioms, HashSet<OWLEntity> pSignatures,
			HashSet<OWLEntity> pDesiredSignatures, int maxAxiomCount) {
		HashSet<OWLAxiom> addedAxioms = new HashSet<>(); 
		for (OWLAxiom dAxiom : candidateAxioms) {
			if (isThereRelatedSignatures(dAxiom, pDesiredSignatures)) {
				pAxioms.add(dAxiom);
				for (OWLEntity dEntity: dAxiom.getSignature()) {
					pSignatures.add(dEntity); 
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
			HashSet<OWLEntity> pDesiredSignatures) {
		Set<OWLEntity> dEntities = pAxiom.getSignature(); 
		for (OWLEntity dEntity : dEntities) {
			if (pDesiredSignatures.contains(dEntity)) {
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
