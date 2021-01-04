package ca.pfv.spmf.algorithms.graph_mining.tseqminer;

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
 * Parameters used by TSeqMiner
 * @see AlgoTSeqMiner
 */
public class ParametersSetting {

    /** This flag specify different dataset
     *  case 0: DBLP
     *  case 1: USA flight
     */
    public static int TASK_FLAG = 0;
    public static String projectPath = System.getProperty("user.dir");
    //follow parameters are specific for different datasets
    // main parameters
    /** minimal support ratio */
    public static double MINSUP ;
    /** minimal significance */
    public static double MIN_SIGNIFICANCE ;
    /** minimal support of tail itemset(useful for pruning and acquire more meaningful results) */
    public static int MIN_TAIL_SUP;
    /** threshold for determine significant increase/decrase (used in process of naive dicretization)*/
    public static double INCRE_THRESHOLD;
    public static int TOTAL_NUM_ATTR = 43;

    //input and output file path
    /** path of file recording attribute mapping */
    public static String ATTRI_MAPPING_PATH;
    /** path of file describing attributes of vertices*/
    public static String ATTR_FILE_PATH;
    /** path of file describing edges of vertices */
    public static String EDGE_FILE_PATH;
    /** path of file recording mined patterns */
    public static String PATTERN_PATH;
    /** specify total number attribute when reading attribute file (depend on dataset) */
    public static String EVENTTYPE_MAPPING_PATH;

    public static String TRANSACTION_PATH;
    public static String FRE_ITEMSET_PATH;
    public static String VERTEX_MAP_NAME_PATH;


    //following parameters are common for different datasets
    /** This flag indicate if we allow overlapping when compute neighboring space.
     * Generally, sequence will have larger significance when allowing overlapping.
     */
    public static boolean ALLOW_OVERLAPPING = false;

    /** This flag indicate if we will use pruning technique */
    public static boolean ADOPTING_PRUNING = true;

    /** this flag specify traversal behavior
     * case 0: complete DFS
     * case 1: process outer and inner separately, outer DFS, inner BFS
     * case 2: process outer and inner separately, outer DFS, inner DFS
     */
    public static int TRAVERSAL_FLAG = 1;

    public static boolean LARGE_GRAINED_PRUNING = true;
    public static boolean MINI_GRAINED_PRUNING = true;

    public static boolean EXHIBIT_SUPPORTING_POINTS = false;

    public static boolean OUTPUT_PATTERNS = true;

    /** This flag specify discretization strategy
     * case 0: '-', '0', '+'
     * case 1: '--', '-', '0', '+', '++'
     */
    public static int DISCRE_FLAG = 0;

    /** This parameter specify the number of repetition of data*/
    public static int REPEAT = 1;

    /** Runtime of preparation (load graph, preprocess)*/
    public static long PREPARE;
    public static double SCALE = 0.5;

    /** This flag indicate what neighboring relationship we will use
     * case 0: for p=(t,id), if point (t,id1) is connected with p at time t, then (t+1,id1) is a neighbor.
     * case 1: for p=(t,id), point (t+1,id) is a neighbor.
     */
    public static int NEIGHBOR_FLAG = 0;

/*
    public static void main(String[] args) throws IOException {
        System.out.println("************************");
        String graphDir = args[0];
        ParametersSetting.INCRE_THRESHOLD = Float.parseFloat(args[1]);
        ParametersSetting.MINSUP = Float.parseFloat(args[2]);
        ParametersSetting.MIN_TAIL_SUP = Integer.parseInt(args[3]);
        ParametersSetting.MIN_SIGNIFICANCE = Float.parseFloat(args[4]);
        ParametersSetting.TOTAL_NUM_ATTR = Integer.parseInt(args[5]);


        System.out.println(graphDir);

        ParametersSetting.EDGE_FILE_PATH = projectPath + "/" + graphDir + "/graph.txt";
        ParametersSetting.ATTRI_MAPPING_PATH = projectPath + "/" + graphDir + "/attributes_mapping.txt";
        ParametersSetting.ATTR_FILE_PATH = projectPath + "/" + graphDir + "/attributes.txt";
        ParametersSetting.VERTEX_MAP_NAME_PATH = projectPath +  "/" + graphDir + "/vertices_mapping.txt";


        File res_dir = new File(projectPath + "/" + graphDir + "/result");
        File preprocess_dir = new File(projectPath + "/" + graphDir + "/preprocess");
        if (! res_dir.exists())
            res_dir.mkdir();
        if (! preprocess_dir.exists())
            preprocess_dir.mkdir();
        ParametersSetting.PATTERN_PATH = projectPath + "/" + graphDir + "/result/sequential_patterns.txt";
        ParametersSetting.TRANSACTION_PATH = projectPath + "/" + graphDir + "/preprocess/transactions.txt";
        ParametersSetting.EVENTTYPE_MAPPING_PATH = projectPath + "/" + graphDir + "/preprocess/event_type_mapping.txt";
        ParametersSetting.FRE_ITEMSET_PATH = projectPath + "/" + graphDir + "/result/frequent_itemset.txt";

        System.out.println(ParametersSetting.projectPath);
        pruningTest();
    }

    static {
        switch (TASK_FLAG) {
            case 0: {
                //for DBLP dataset

                MINSUP = 0.008;
                INCRE_THRESHOLD = 0.1;
                MIN_TAIL_SUP = 100; //0.00136 - 30
                MIN_SIGNIFICANCE = 8;

                TOTAL_NUM_ATTR = 43;
                ATTRI_MAPPING_PATH = projectPath + "/dataset/DBLP/attributes_mapping.txt";
                ATTR_FILE_PATH = projectPath + "/dataset/DBLP/attributes.txt";
                EDGE_FILE_PATH = projectPath + "/dataset/DBLP/graph.txt";
                PATTERN_PATH = projectPath + "/dataset/DBLP/result/sequential_patterns_dblp.txt";
                EVENTTYPE_MAPPING_PATH = projectPath + "/dataset/DBLP/preprocess/event_type_mapping.txt";
                FRE_ITEMSET_PATH = projectPath + "/dataset/DBLP/result/frequent_itemset.txt";
                VERTEX_MAP_NAME_PATH = projectPath + "/dataset/DBLP/vertices_mapping.txt";
                break;
            }

            case 1: {
                //for USA flight dataset

                MINSUP = 0.002;
                INCRE_THRESHOLD = 0.1;
                MIN_TAIL_SUP = 5;
                MIN_SIGNIFICANCE = 4;
                //good parameters for finding meaningful result:
                //minTailSup    6   10
                //minSig        6   3.5
                TOTAL_NUM_ATTR = 8;
                ATTRI_MAPPING_PATH = projectPath + "/dataset/USFlight/attributes_mapping.txt";
                ATTR_FILE_PATH = projectPath + "/dataset/USFlight/attributes.txt";
                EDGE_FILE_PATH = projectPath + "/dataset/USFlight/graphFlightsKatrina.txt";
                PATTERN_PATH = projectPath + "/dataset/USFlight/result/sequential_patterns_flight.txt";
                TRANSACTION_PATH = projectPath + "/dataset/USFlight/preprocess/transactions.txt";
                EVENTTYPE_MAPPING_PATH = projectPath + "/dataset/USFlight/preprocess/event_type_mapping.txt";
                FRE_ITEMSET_PATH = projectPath + "/dataset/USFlight/result/frequent_itemset.txt";
                VERTEX_MAP_NAME_PATH = projectPath + "/dataset/USFlight/vertices_mapping.txt";
                break;
            }

            case 2: {
                //for other dataset
            }
        }
    }
    
    */
/*
    public static void pruningTest()
            throws IOException {

//        // parameter info
//        System.out.println("*************************** test pruning ***************************");
//        System.out.println("total number of frequent itemsets: " + SqeMiner.itemsetMapSup.size());
//        System.out.println("allow overlapping ? " + ALLOW_OVERLAPPING);
//        System.out.println("traversal method ? " + TRAVERSAL_FLAG);
//        System.out.println();
//
//        int testNum = 5;
//        int[] traversalFlags = new int[]{2, 2, 2, 1, 1};
//        //{BFS pruning{mini, modest, large}, BFS not-pruning, DFS pruning{modest, large}, DFS not-pruning}
//        boolean[] adoptingPrunings = new boolean[]{true, true, false, true, true};
//        boolean[] largeGraindPrunings = new boolean[]{false, false, false, false, false};
//        boolean[] miniGrainedPrunings = new boolean[]{true, false, false, true, false};
//
//        ParametersSetting.MINSUP=0.0022;                                                                                                                                                                                   ;
//        int[] repeatArray = new int[]{1};
//        int[] timeNumArray = new int[]{8};
//        int[] minTailSupArray = new int[]{(int)(ParametersSetting.MIN_TAIL_SUP * TIME_NUM /8.0)};
//        double[] sigArray = new double[]{8};
//        for (int repeat : repeatArray) {
//            for (int timeNum : timeNumArray) {
//                ParametersSetting.TIME_NUM = timeNum;
//                for (int minTailSup: minTailSupArray) {
//                    ParametersSetting.MIN_TAIL_SUP = minTailSup;
//                    ParametersSetting.REPEAT = repeat;
//                    for (double sig : sigArray) {
//                        ParametersSetting.MIN_SIGNIFICANCE = sig;
//                        for (int i = testNum-1; i >= 0; i--) {
//                            if (i ==1 || i == 4) {
//                                continue;
//                            }
//                            long time1 = System.currentTimeMillis();
//                            TRAVERSAL_FLAG = traversalFlags[i];
//                            ADOPTING_PRUNING = adoptingPrunings[i];
//                            LARGE_GRAINED_PRUNING = largeGraindPrunings[i];
//                            MINI_GRAINED_PRUNING = miniGrainedPrunings[i];
//                            SqeMiner.runAlgor();
//                            long time2 = System.currentTimeMillis();
//                            System.out.println("sig=" + sig + "     repeat=" + repeat + "    timeNum=" + timeNum);
//                            System.out.println("traversal method? " + TRAVERSAL_FLAG + "\nallow pruning? " + ADOPTING_PRUNING + "\nlarge granularity? " + LARGE_GRAINED_PRUNING
//                                    + "\nmini granularity? " + MINI_GRAINED_PRUNING);
//                            System.out.println((time2 - time1 - ParametersSetting.PREPARE)/ 1000);
//                        }
//                    }
//                }
//            }
//        }

//        findEventTypeMapping();
//        writeEventTypeMapping();

        //parameter info
        System.out.println("*************************** test pruning ***************************");
        System.out.println("allow overlapping ? " + ALLOW_OVERLAPPING);
        System.out.println("traversal method ? " + TRAVERSAL_FLAG);
        System.out.println();
        long time1 = System.currentTimeMillis();
        AlgoTSeqMiner.runAlgorithm();
        long time2 = System.currentTimeMillis();
        System.out.println((time2 - time1 - ParametersSetting.PREPARE)/ 1000);

        //memory usage test
//        long time1 = System.currentTimeMillis();
//        System.gc();
//        SqeMiner.runAlgor();
//        MemoryLogger.getInstance().checkMemory();
//        long time2 = System.currentTimeMillis();
//        System.out.println((time2 - time1 - ParametersSetting.PREPARE)/ 1000);
//        System.out.println("max memory: " + MemoryLogger.getInstance().getMaxMemory());

//        for (int tailSup : tailArray) {
//            ParametersSetting.MIN_TAIL_SUP=tailSup;
//            for (int i = 0; i < testNum; i++) {
//                if (i != 2 && i != 0){
//                    continue;
//                }
//                long time1 = System.currentTimeMillis();
//                TRAVERSAL_FLAG = traversalFlags[i];
//                ADOPTING_PRUNING = adoptingPrunings[i];
//                LARGE_GRAINED_PRUNING = largeGraindPrunings[i];
//                MINI_GRAINED_PRUNING = miniGrainedPrunings[i];
//                SqeMiner.runAlgor();
//                long time2 = System.currentTimeMillis();
//                System.out.println("traversal method? " + TRAVERSAL_FLAG + "\nallow pruning? " + ADOPTING_PRUNING + "\nlarge granularity? " + LARGE_GRAINED_PRUNING
//                        + "\nmini granularity? " + MINI_GRAINED_PRUNING);
//                System.out.println((time2 - time1)/ 1000);
//            }
//        }

//        for (int i = 0; i < testNum; i++) {
//            if (i != 2 && i != 0){
//                continue;
//            }
//            long time1 = System.currentTimeMillis();
//            TRAVERSAL_FLAG = traversalFlags[i];
//            ADOPTING_PRUNING = adoptingPrunings[i];
//            LARGE_GRAINED_PRUNING = largeGraindPrunings[i];
//            MINI_GRAINED_PRUNING = miniGrainedPrunings[i];
//            SqeMiner.runAlgor();
//            long time2 = System.currentTimeMillis();
//            System.out.println("traversal method? " + TRAVERSAL_FLAG + "\nallow pruning? " + ADOPTING_PRUNING + "\nlarge granularity? " + LARGE_GRAINED_PRUNING
//                    + "\nmini granularity? " + MINI_GRAINED_PRUNING);
//            System.out.println((time2 - time1)/ 1000);
//        }




//        long time1 = 0L, time2 = 0L;
//
//        //BFS mini-granularity pruning
//        System.gc();
//        time1 = System.currentTimeMillis();
//        TRAVERSAL_FLAG = 0;
//        ADOPTING_PRUNING = true;
//        LARGE_GRAINED_PRUNING = false;
//        MINI_GRAINED_PRUNING = true;
//        SqeMiner.runAlgor();
//        time2 = System.currentTimeMillis();
//        System.out.println("allow pruning? " + ADOPTING_PRUNING + "  large granularity? " + LARGE_GRAINED_PRUNING + "  mini granularity? " + MINI_GRAINED_PRUNING);
//        System.out.println((time2 - time1)/ 1000);

//        time1 = System.currentTimeMillis();
//        TRAVERSAL_FLAG = 0;
//        ADOPTING_PRUNING = true;
//        LARGE_GRAINED_PRUNING = false;
//        MINI_GRAINED_PRUNING = false;
//        SqeMiner.runAlgor();
//        time2 = System.currentTimeMillis();
//        System.out.println("allow pruning? " + ADOPTING_PRUNING + "  large granularity? " + LARGE_GRAINED_PRUNING + "  mini granularity? " + MINI_GRAINED_PRUNING);
//        System.out.println((time2 - time1)/ 1000);

//
//        //BFS modest-granularity pruning
//        TRAVERSAL_FLAG = 2;
//        ADOPTING_PRUNING = true;
//        LARGE_GRAINED_PRUNING = false;
//        MINI_GRAINED_PRUNING = false;
//        SqeMiner.runAlgor();
//        System.out.println("allow pruning? " + ADOPTING_PRUNING + "  allow optimization? " + LARGE_GRAINED_PRUNING);
//        System.out.println((time2 - time1)/ 1000);
//
//        //BFS largest-granularity pruning
//        TRAVERSAL_FLAG = 2;
//        ADOPTING_PRUNING = true;
//        LARGE_GRAINED_PRUNING = false;
//        MINI_GRAINED_PRUNING = false;
//        SqeMiner.runAlgor();
//        System.out.println("allow pruning? " + ADOPTING_PRUNING + "  allow optimization? " + LARGE_GRAINED_PRUNING);
//        System.out.println((time2 - time1)/ 1000);

//
//        ADOPTING_PRUNING = false;
//        SqeMiner.runAlgor();
//        long time4 = System.currentTimeMillis();
//        System.out.println("allow pruning? " + ADOPTING_PRUNING + "  allow optimization? " + LARGE_GRAINED_PRUNING);
//        System.out.println((time4 - time3)/ 1000);

//
//        //parameter info
//        System.out.println("*************************** test pruning ***************************");
//        System.out.println("total number of frequent itemsets: " + SqeMiner.itemsetMapSup.size());
//        System.out.println("allow overlapping ? " + ALLOW_OVERLAPPING);
//        System.out.println("traversal method ? " + TRAVERSAL_FLAG);
//
//        System.out.println("\nnot use pruning ##### mining time(s)" + (time3 - time2)/ 1000);
    }
    */
}
