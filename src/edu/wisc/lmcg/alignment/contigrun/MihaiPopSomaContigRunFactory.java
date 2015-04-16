/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wisc.lmcg.alignment.contigrun;

import edu.wisc.lmcg.alignment.AlignmentException;
import edu.wisc.lmcg.alignment.mapalignment.MapAlignment;
import edu.wisc.lmcg.alignment.mapalignment.StandardMapAlignment;
import edu.wisc.lmcg.gui.BasicAlignmentFrame;
import edu.wisc.lmcg.gui.MapGUI;
import edu.wisc.lmcg.map.Mapset;
import edu.wisc.lmcg.map.RestrictionFragment;
import edu.wisc.lmcg.map.RestrictionMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author dipaco
 */
public final class MihaiPopSomaContigRunFactory implements AlignerAlgorithmContigRunFactory {

    private final LinkedList<StandardContigRun> contigRuns;

    private final String CREATOR = "Mihai Pop Soma";

    public MihaiPopSomaContigRunFactory() {
        this.contigRuns = new LinkedList<>();
        startNewContingRun();
    }

    @Override
    /**
     * Creates a new contig run and make it the current one.
     */
    public void startNewContingRun() {
        StandardContigRun n_contigRun = new StandardContigRun();

        //TODO: Fill all contig run metadata
        n_contigRun.setCreator(CREATOR);

        contigRuns.add(n_contigRun);
    }

    /**
     * Construct the alignment in one contig based on the results from the Mihai
     * Pop library
     *
     * @param referenceMap Reference map for the contig
     * @param unalignedMaps List of unaligned maps
     * @param baseDir Directory in which the aligned data was stored
     * @return Returns a list with the alignments found by the Mihai Pop library
     */
    private List<MapAlignment> constructMapAligment(RestrictionMap referenceMap, List<RestrictionMap> unalignedMaps, String baseDir) {

        java.util.List<MapAlignment> alignments = new LinkedList<>();
        MapAlignment m_alignment;

        try {

            BufferedReader br = new BufferedReader(new FileReader(new File(baseDir + File.separator + "contigs.pre-fig.all")));
            //reads an discard the first line of the file (this line contains the fragment sizes for the reference map)
            br.readLine();

            while (br.ready()) {
                String ctg_info = br.readLine();
                String opt_seq = br.readLine();
                String ctg_seq = br.readLine();
                String ctg_matches = br.readLine();

                String[] pieces = ctg_info.split(" ");
                String alignedMapName = pieces[0];
                boolean isForward = !pieces[1].equals("0");
                double size = Double.parseDouble(pieces[2]);
                int startIndex = Integer.parseInt(pieces[3]);
                int endPosition = Integer.parseInt(pieces[4]);

                java.util.List<RestrictionMap> result = unalignedMaps.stream()
                        .filter(a -> a.getName().equals(alignedMapName))
                        .collect(Collectors.toList());

                RestrictionMap m = result.get(0);

                //Creates the MapAlignment object with the above information
                m_alignment = new StandardMapAlignment();
                m_alignment.setAlignedMapName(alignedMapName);
                if (isForward) {
                    m_alignment.setOrientation("N");
                } else {
                    m_alignment.setOrientation("R");
                }
                m_alignment.setPvalue(0.0);
                m_alignment.setReferenceMapName(referenceMap.getName());
                m_alignment.setScore(0.0);

                String[] opt_aln = opt_seq.replace(";", " ;").replaceFirst("\\s+\\n", "").split("\\s+");
                String[] ctg_aln = ctg_seq.replace(";", " ;").replaceFirst("\\s+\\n", "").split("\\s+");

                int j = 0;
                int opt_ind = 0;
                int ctg_ind = 0;
                String ctg_start = "";
                m_alignment.addAlignment(ctg_ind, startIndex, startIndex);
                for (String opt_aln1 : opt_aln) {
                    if (!opt_aln1.equals(";")) {
                        opt_ind++;
                    } else {
                        for (; j < ctg_aln.length; j++) {

                            if (!ctg_aln[j].equals(";")) {
                                ctg_ind++;
                            } else {
                                m_alignment.addAlignment(ctg_ind, startIndex + opt_ind, startIndex + opt_ind);

                                j++;
                                break;
                            }
                        }
                    }
                }

                /*List<RestrictionFragment> res_fragments = m.getFragments();                
                 for ( int i = 0; i < res_fragments.size(); i++){                    
                 m_alignment.addAlignment(i, startIndex, startIndex);                    
                 startIndex++;
                 }*/
                alignments.add(m_alignment);

            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MihaiPopSomaContigRunFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(MihaiPopSomaContigRunFactory.class.getName()).log(Level.SEVERE, null, ex);
        }

        return alignments;

        /*java.util.List<MapAlignment> alignments = new LinkedList<>();
         MapAlignment m_alignment;
        
         //Reads the contigs.result file in the working directory and construct one alignment map for each one of the maps in it
         try {
         BufferedReader br = new BufferedReader(new FileReader(new File(baseDir + File.separator + "contigs.result")));
         String line;
         Scanner sc;
         int startIndex = - 1;
         double p_value = -1;
            
         //Check for aligned maps in every line of the file
         while ( br.ready() ){
         line = br.readLine();
                
         //Every line is a potential aligned map and it needs to check what could it be
         for ( RestrictionMap m : unalignedMaps ){
         if ( line.startsWith(m.getName()) ){
                        
         line = line.replaceFirst(m.getName(), "");
                       
         //Obtains the basic information of the alignment
         sc = new Scanner(line);
         while ( sc.hasNext() ){
                            
         switch (sc.next()) {
         case "Start:":
         startIndex = sc.nextInt();
         break;
         case "P-value:":
         p_value = sc.nextDouble();
         break;
         default:
         break;
         }
                            
         }
                        
         //There is not enough information to create the alignment map
         if ( startIndex == -1 || p_value == -1 )
         break;
                        
         //Creates the MapAlignment object with the above information
         m_alignment = new StandardMapAlignment();
         m_alignment.setAlignedMapName(m.getName());
         m_alignment.setOrientation("N"); //TODO: Chech this type of orientation
         m_alignment.setPvalue(p_value);
         m_alignment.setReferenceMapName(referenceMap.getName());
         m_alignment.setScore(0.0);

         java.util.List<RestrictionFragment> res_fragments = m.getFragments();
         for ( int i = 0; i < res_fragments.size(); i++ ){
         m_alignment.addAlignment(i, startIndex, startIndex);
         startIndex++;
         }

         alignments.add(m_alignment);
         break;
         }
         }                                
         }
            
         } catch (FileNotFoundException ex) {
         Logger.getLogger(MapGUI.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
         Logger.getLogger(MapGUI.class.getName()).log(Level.SEVERE, null, ex);
         }catch( Exception e ){
            
         }
        
         return alignments;*/
    }

    /**
     * Converts a restriction map into Opt Format, in order to let the Mihai Pop
     * algorithm read the data
     *
     * @param map Restriction map
     * @param optFilename Output filename for the data in Opt format
     * @throws IOException
     */
    private void restrictionMap2OptFormat(RestrictionMap map, String optFilename) throws IOException {
        double standar_dev;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(optFilename))) {
            for (RestrictionFragment fg : map.getFragments()) {
                double length_kb = ((double) fg.getMassInBp()) / 1000;
                standar_dev = 0.05 * length_kb;     //Standar deviation
                bw.write(length_kb + " " + standar_dev);
                bw.newLine();
            }
        }
    }

    /**
     * Converts a list of restriction maps into silico format, in order to let
     * the Mihai Pop algorithm read the data
     *
     * @param maps List of restriction maps
     * @param silicoFilename Output filename for the data in silico format
     * @throws IOException
     */
    private void restrictionMaps2SilicoFormat(java.util.List<RestrictionMap> maps, String silicoFilename) throws IOException {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(silicoFilename))) {
            for (RestrictionMap m : maps) {

                //List of fragments in the map
                java.util.List<RestrictionFragment> frags = m.getFragments();
                int totalMass = 0;
                String cutPoints = "";
                for (int i = 0; i < frags.size(); i++) {

                    RestrictionFragment fg = frags.get(i);
                    totalMass += fg.getMassInBp();

                    //Collects all the cut points in a single line
                    if (i < frags.size() - 1) {
                        cutPoints += totalMass + " ";
                    }
                }

                //Writes the name, total size and total number of cuts in the map
                bw.write(m.getName() + "\t" + totalMass + " " + (frags.size() - 1));
                bw.newLine();
                bw.write(cutPoints);
                bw.newLine();
            }
        }
    }

    /**
     * Performs calls to the Mihai Pop library in order to do an alignment of
     * the data
     *
     * @param basicFolder Folder in which the data for the alignment is stored
     * @param silicoFile In silico file with all the maps to be aligned
     * @param optFile Opt file with the information for the reference map
     */
    private void alignMaps(String basicFolder, String silicoFile, String optFile) {

        //Creates the "commands" for excecuting the alignment procedures
        String commandPath = "soma" + File.separator + "bin" + File.separator;
        LinkedList<String> cmds = new LinkedList<>();
        String st = commandPath + "match" + " -s @silico -m @opt -p 0.01 -f 0.01 -o @basic_folder/contigs";
        String completeCmd = st.replaceAll("@basic_folder", basicFolder)
                .replace("@silico", silicoFile)
                .replace("@opt", optFile);
        cmds.add(completeCmd);
        cmds.add(commandPath + "place_rest.pl @basic_folder/contigs".replace("@basic_folder", basicFolder));
        cmds.add(commandPath + "schedule @basic_folder/contigs".replace("@basic_folder", basicFolder));
        cmds.add(commandPath + "collect_results.pl @basic_folder/contigs".replace("@basic_folder", basicFolder));

        //Execute the alignment via calls to an external library
        try {
            String s;
            for (String cmd : cmds) {
                System.out.println(cmd);
                Process p = Runtime.getRuntime().exec(cmd);
                int ec = p.waitFor();

                /*BufferedReader stdInput = new BufferedReader(new
                 InputStreamReader(p.getInputStream()));
                
                 BufferedReader stdInputErr = new BufferedReader(new
                 InputStreamReader(p.getErrorStream()));

                 // read the output from the command
                 System.out.println("output:");
                 while ((s = stdInput.readLine()) != null) {
                 System.out.println(s);
                 }
                
                 // read the error output from the command
                 System.out.println("errors:");
                 while ((s = stdInputErr.readLine()) != null) {
                 System.err.println(s);
                 }*/
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(MapGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Perform one alignment and adds the results to the current contig run.
     *
     * @param parameters Parameter list for the alignment algorithm
     * @throws AlignmentException
     */
    @Override
    public void performAlignment(Map<String, Object> parameters) throws AlignmentException {

        //Checks all the parameters for the alignment
        RestrictionMap referenceMap;
        List<RestrictionMap> unalignedMaps;
        if (parameters.containsKey("refMap")) {
            referenceMap = (RestrictionMap) parameters.get("refMap");
        } else {
            throw new AlignmentException("Missing Paramter: refMap");
        }

        if (parameters.containsKey("unalignedMaps")) {
            unalignedMaps = (List<RestrictionMap>) parameters.get("unalignedMaps");
        } else {
            throw new AlignmentException("Missing Paramter: unalignmentMaps");
        }

        try {
            //Creates a temporary folder to store the intermediate data of the alignment
            //File baseDire = File.createTempFile("temp", Long.toString(System.nanoTime()));                                    
            File baseDire = new File("/home/dipaco/Desktop/p");
            if (!(baseDire.delete())) {
                //throw new IOException("Could not delete temp file: " + baseDire.getAbsolutePath());
            }
            if (!(baseDire.mkdir())) {
                //throw new IOException("Could not create temp directory: " + baseDire.getAbsolutePath());
            }
            baseDire.deleteOnExit();

            File silicoFile = new File(baseDire, "contigs.silico");
            File optFile = new File(baseDire, "reference_map.opt");

            //Writes the reference map into a .opt file
            restrictionMap2OptFormat(referenceMap, optFile.getAbsolutePath());

            //Writes all the maps to be aligned into a .silico file
            restrictionMaps2SilicoFormat(unalignedMaps, silicoFile.getAbsolutePath());

            //Perform the alignment
            alignMaps(
                    baseDire.getAbsolutePath(),
                    silicoFile.getAbsolutePath(),
                    optFile.getAbsolutePath());

            List<MapAlignment> map_alignment = constructMapAligment(referenceMap, unalignedMaps, baseDire.getAbsolutePath());

            //If everything went good it adds the alignment to the current contig run            
            Mapset ms = contigRuns.getLast().getMapset();

            ms.addRestrictionMap(referenceMap);
            for (RestrictionMap m : unalignedMaps) {
                ms.addRestrictionMap(m);
            }

            contigRuns.getLast().setMapset(ms);

            //Adds the alignment map to the contig run
            for (MapAlignment m : map_alignment) {
                try {
                    contigRuns.getLast().addMapAlignment(m);
                } catch (Exception ex) {
                    throw new AlignmentException("Couldn't add a MapAlignment to the ContigRun.");
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(BasicAlignmentFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets the next ContigRun available
     *
     * @return A contig run
     * @throws NullPointerException
     * @throws Exception
     */
    @Override
    public ContigRun getNextContigRun() throws NullPointerException, Exception {
        return contigRuns.remove();
    }

    /**
     * Restarts the factory
     *
     * @throws Exception
     */
    @Override
    public void reset() throws Exception {

    }

    /**
     * Indicates if the reset functionality is available
     *
     * @return
     */
    @Override
    public boolean resetSupported() {
        return false;
    }

    /**
     * Returns the parameter list for the algorithm
     *
     * @return parameter list for the algorithm
     */
    @Override
    public Map<String, Type> getParamterList() {

        TreeMap<String, Type> par_list = new TreeMap<>();

        par_list.put("refMap", RestrictionMap.class);
        par_list.put("refMapunalignedMaps", List.class);

        return par_list;
    }
}
