package ca.pfv.spmf.algorithms.graph_mining.tseqminer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
/* This file is copyright (c) 2018 by Chao Cheng
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/
/**
 * This class is used to perform naive discretization. It convert dynamic attribute graph to item dynamic attributed
 * graph and record mapping between them.
 * @see AlgoTSeqMiner
 */
public class Preprocess {
    /** naive discretization */
    private static String[] trends = new String[] {"-", "=", "+"};

    /** map from attribute type(integer) -> attribute name */
    static Map<Integer, String> attrMapping;
    /** map from event type(integer) -> enent type name(String) */
    static Map<Integer, String> eventTypeMapping = new LinkedHashMap<>();
    /** map from event type name(String) -> event type(integer) */
    static Map<String, Integer> eventTypeMappingRe = new LinkedHashMap<>();
    /** item dynamic attributed graph(ItDyAG) deriving from original dynamic attributed graph(DyAG) */
    static Map<Integer, ItemAttributedGraph> itDyAG = new LinkedHashMap<>();

    /** set maximal number of attribute */
    static double INCRE_THRESHOLD = ParametersSetting.INCRE_THRESHOLD;

    /** store path of mapping of attribute form integer to string */
    private static String ATTRI_MAPPING_PATH = ParametersSetting.ATTRI_MAPPING_PATH;

    /** this flag indicate how to do discretization */
    private static int DISCRET_FLAG = ParametersSetting.DISCRE_FLAG;

    private static int PASS_FLAG = -999;

    private static String EVENTTYPE_MAPPING_PATH = ParametersSetting.EVENTTYPE_MAPPING_PATH;


    private static void repeatGraph(Map<Integer, ItemAttributedGraph> tempItemDyAG) {
        int repeatNum = ParametersSetting.REPEAT;
        int oriSize = tempItemDyAG.size();
        for (int timeStamp = 0; timeStamp < oriSize; timeStamp++) {
            ItemAttributedGraph itemAG = tempItemDyAG.get(timeStamp);
            for (int i = 1; i < repeatNum; i++) {
                tempItemDyAG.put(oriSize * i + timeStamp, itemAG);
            }
        }
    }


    /** */
    private static Map<Integer, Map<Integer, List<Double>>> vertexMapAttrMapVals;

    static {
        switch (DISCRET_FLAG) {
            case 0: {
                trends = new String[] {"-", "0", "+"};
                break;
            }
            case 1: {
                trends = new String[] {"--", "-", "0", "+", "++"};
                break;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        findEventTypeMapping();
        writeEventTypeMapping();

//        System.out.println(itDyAG.get(0).getItemV(0).getAllItems());
    }

    /**
     * This method read attribute mapping from integer to attribute name
     * @return attribute mapping
     * @throws IOException
     */
    public static Map<Integer, String> readAttrMapping() throws IOException {
        //use a map to store relationship between attribute type and integer
        Map<Integer, String> attrMap = new LinkedHashMap<>();
//        System.out.println(ATTRI_MAPPING_PATH);
        File attrMapFile = new File(ATTRI_MAPPING_PATH);
        if (! attrMapFile.exists())
            attrMapFile.createNewFile();
        BufferedReader br = new BufferedReader(new FileReader(ATTRI_MAPPING_PATH));
        int count = 1;
        String line = br.readLine();
        while (line != null) {
            attrMap.put(count++, line);
            line = br.readLine();
        }
        //test if read successfully
//        for (Integer i : attrMap.keySet()) {
//            System.out.println(i + " " + attrMap.get(i));
//        }
        return attrMap;
    }

    /**
     * This method do naive discretization for original attribute types
     * @throws IOException
     */
    private static void findEventTypeMapping() throws IOException {
        attrMapping = readAttrMapping();
        int count = 1;
        for (int attrType : attrMapping.keySet()) {
            String attrName = attrMapping.get(attrType);
            for (String trend: trends){
                String eventName = attrName + trend;
                eventTypeMapping.put(count, eventName);
                eventTypeMappingRe.put(eventName, count);
                count++;
            }
        }
    }

    /**
     * This method write result of discretization to file
     * @throws IOException
     */
    public static void writeEventTypeMapping() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(EVENTTYPE_MAPPING_PATH));
        for (int eventType : eventTypeMapping.keySet()) {
            bw.write(eventTypeMapping.get(eventType));
            bw.newLine();
        }
        bw.close();
    }


    private static void acquireAllVals(Map<Integer, AttributedGraph> oriDyAG) {
        //compute mean and standard deviation for each vertex
        vertexMapAttrMapVals = new HashMap<>();
        for (int i = 0; i < oriDyAG.size() - 1; i++) {
            AttributedGraph aG = oriDyAG.get(i);
            if (i == 0) {
                for( Integer vId : aG.getAllVerticeId()) {
                    Map<Integer, List<Double>> attrMapVal = new HashMap<>();
                    vertexMapAttrMapVals.put(vId, attrMapVal);
                    Map<Integer, Double> attrMap = aG.getVertex(vId).getAttrDouMap();
                    for (int attrType : attrMap.keySet()) {
                        attrMapVal.put(attrType, new LinkedList<>());
                    }
                }
            }
            for (Integer vId : aG.getAllVerticeId()) {
                Map<Integer, Double> attrMapVal = aG.getVertex(vId).getAttrDouMap();
                for (Integer attrType: attrMapVal.keySet()) {
                    vertexMapAttrMapVals.get(vId).get(attrType).add(attrMapVal.get(attrType));
                }
            }
        }
    }


    private static void computeMeanStdDev() {
        for (int vId : vertexMapAttrMapVals.keySet()) {
            Map<Integer, List<Double>> attrMapVals = vertexMapAttrMapVals.get(vId);
            for (int attrType : attrMapVals.keySet()) {
                List<Double> vals = attrMapVals.get(attrType);
                double mean = 0, preVal = 0, stdDev = 0;
                int count = 0;
                for (double val : vals) {
                    count++;
                    mean += (val - preVal)/count;
                }
                for (double val : vals) {
                    stdDev += (val - mean) * (val - mean);
                }
                stdDev = Math.sqrt(stdDev/vals.size());
                vals.clear();
                vals.add(mean);
                vals.add(stdDev);
            }
        }
    }
    /**
     * This method derive item dynamic attributed graph from original dynamic attributed graph
     * @return resulting item dynamic attributed graph
     * @throws IOException
     */
    public static Map<Integer, ItemAttributedGraph> convertToItDyAGCase() throws IOException {
//        System.out.println("@@@ start to preprocess...");
        findEventTypeMapping();

        //construct dynamic item attributed graph using DyAG which indicate trend of evolution
        Map<Integer, AttributedGraph> oriDyAG = ReadGraph.readGraph();

        if (DISCRET_FLAG == 1) {
            acquireAllVals(oriDyAG);
            computeMeanStdDev();
        }

        Map<Integer, ItemAttributedGraph> tempDyAG = new HashMap<>();

        //for each position in DyAG, other than the last
        for (int i = 0; i < oriDyAG.size() - 1; i++) {
            //get 2 consecutive attributed graphs that are needed to find trend
            AttributedGraph aG1 = oriDyAG.get(i), aG2 = oriDyAG.get(i+1);
            //construct a map of vertex id -> event types
            Map<Integer, ItemVertex> vMap = new HashMap<>();
            //for each vertex
            for (int vId : aG1.getAllVerticeId()) {
                //get attribute maps for these 2 attributed graphs
                Map<Integer, Double> attrMap1 = aG1.getVertex(vId).getAttrDouMap();
                Map<Integer, Double> attrMap2 = aG2.getVertex(vId).getAttrDouMap();
                List<Integer> eventTypeList = new LinkedList<>();
                //for each attribute type
                for (int attrType : attrMap1.keySet()) {
                    //find trend of the values
                    double val1 = attrMap1.get(attrType);
                    double val2 = attrMap2.get(attrType);

                    //***************************** key position of preprocessing ******************************
                    int trendFlag = findTrendFlag(DISCRET_FLAG, vId, attrType, val1, val2);
                    if (trendFlag == PASS_FLAG)
                        continue;

                    String eventName = attrMapping.get(attrType) + trends[trendFlag];
                    int eventType = eventTypeMappingRe.get(eventName);
                    //add it to event type list of the vertex
                    eventTypeList.add(eventType);
                    //***************************************************************************************
                }
                ItemVertex iV = new ItemVertex(vId);
                iV.addItems(eventTypeList);
                vMap.put(vId, iV);
            }
            //construct a new item attributed graph using identifier, vertex map, edge map
            ItemAttributedGraph iAG = new ItemAttributedGraph(i, vMap, aG1.getEdgesMap());
            //add it to ItDyAG
            tempDyAG.put(i, iAG);
        }
//        System.out.println("preprocessing finish !");
        repeatGraph(tempDyAG);
//        System.out.println("repeating finish !");
        Map<Integer, ItemAttributedGraph> subItDyAG = new HashMap<>();

        for (int j = 0; j < tempDyAG.size(); j++) {
            subItDyAG.put(j, tempDyAG.get(j));
        }
        itDyAG = subItDyAG;
        return subItDyAG;
    }

    private static int findTrendFlag(int discretFlag, int vId, int attrType, double val1, double val2) {
        int trendFlag = PASS_FLAG;
        switch (discretFlag) {
            case 0: {
                double diff = val2 - val1;
                if (diff >= INCRE_THRESHOLD) trendFlag = 2;
                else if (diff <= -INCRE_THRESHOLD) trendFlag = 0;
                else if (val1 > 0) trendFlag = 1;
                else ;
                return trendFlag;
            }
            case 1: {
                double diff = val2 - val1;
                double scale = ParametersSetting.SCALE;
                double stdDev = vertexMapAttrMapVals.get(vId).get(attrType).get(1);
                if (diff > 2  *  scale * stdDev) trendFlag = 4;
                else if (diff > scale * stdDev) trendFlag = 3;
                else if (diff < - 2  * scale * stdDev) trendFlag = 0;
                else if (diff < scale * stdDev) trendFlag = 1;
                else trendFlag = PASS_FLAG;
                return trendFlag;
            }
        }
        return 999;
    }



}
