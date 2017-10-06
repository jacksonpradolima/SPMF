/*
 * Copyright (C) 2017 Jackson A. Prado Lima <jacksonpradolima at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth;

import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRules;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jackson A. Prado Lima <jacksonpradolima at gmail.com>
 */
public class AlgoFPGrowthTest {

    public String fileToPath(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource("datasets/" + filename).getFile();
    }

    /**
     * Example of how to mine all association rules with FPGROWTH and save the
     * result to a file, from the source code.
     *
     * @author Philippe Fournier-Viger (Copyright 2008)
     */
    @Test
    public void TestAllAssociationRules_FPGrowth_saveToFile() {
        try {
            String input = fileToPath("contextIGB.txt");
            String output = ".//output.txt";

            // By changing the following lines to some other values
            // it is possible to restrict the number of items in the antecedent and
            // consequent of rules
            int maxConsequentLength = 40;
            int maxAntecedentLength = 40;

            // STEP 1: Applying the FP-GROWTH algorithm to find frequent itemsets
            double minsupp = 0.5;
            AlgoFPGrowth fpgrowth = new AlgoFPGrowth();
            fpgrowth.setMaximumPatternLength(maxAntecedentLength + maxConsequentLength);
            Itemsets patterns = fpgrowth.runAlgorithm(input, null, minsupp);
//		patterns.printItemsets(database.size());
            fpgrowth.printStats();
            int databaseSize = fpgrowth.getDatabaseSize();

            // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
            double minconf = 0.60;
            AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
            algoAgrawal.setMaxConsequentLength(maxConsequentLength);
            algoAgrawal.setMaxAntecedentLength(maxAntecedentLength);
            algoAgrawal.runAlgorithm(patterns, output, databaseSize, minconf);
            algoAgrawal.printStats();
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Example of how to mine all association rules with FPGROWTH and use the
     * lift, and save the result to a file, from the source code.
     *
     * @author Philippe Fournier-Viger (Copyright 2013)
     */
    @Test
    public void TestAllAssociationRules_FPGrowth_saveToFile_withLift() {
        try {
            String input = fileToPath("contextIGB.txt");
            String output = ".//output.txt";

            // By changing the following lines to some other values
            // it is possible to restrict the number of items in the antecedent and
            // consequent of rules
            int maxConsequentLength = 40;
            int maxAntecedentLength = 40;

            // STEP 1: Applying the FP-GROWTH algorithm to find frequent itemsets
            double minsupp = 0.5;
            AlgoFPGrowth fpgrowth = new AlgoFPGrowth();
            fpgrowth.setMaximumPatternLength(maxAntecedentLength + maxConsequentLength);
            Itemsets patterns = fpgrowth.runAlgorithm(input, null, minsupp);
//		patterns.printItemsets(database.size());
            int databaseSize = fpgrowth.getDatabaseSize();
            fpgrowth.printStats();

            // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
            double minlift = 0.1;
            double minconf = 0.50;
            AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
            algoAgrawal.setMaxConsequentLength(maxConsequentLength);
            algoAgrawal.setMaxAntecedentLength(maxAntecedentLength);
            algoAgrawal.runAlgorithm(patterns, output, databaseSize, minconf, minlift);
            algoAgrawal.printStats();
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void TestAllAssociationRules_FPGrowth_saveToMemory_with_lift() {
        try {
            // Loading the binary context
            String input = fileToPath("contextIGB.txt");

            // By changing the following lines to some other values
            // it is possible to restrict the number of items in the antecedent and
            // consequent of rules
            int maxConsequentLength = 40;
            int maxAntecedentLength = 40;

            // STEP 1: Applying the FP-GROWTH algorithm to find frequent itemsets
            double minsupp = 0.5;
            AlgoFPGrowth fpgrowth = new AlgoFPGrowth();
            fpgrowth.setMaximumPatternLength(maxAntecedentLength + maxConsequentLength);
            Itemsets patterns = fpgrowth.runAlgorithm(input, null, minsupp);
            int databaseSize = fpgrowth.getDatabaseSize();
            patterns.printItemsets(databaseSize);

            // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
            double minlift = 0;
            double minconf = 0.90;
            AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
            algoAgrawal.setMaxConsequentLength(maxConsequentLength);
            algoAgrawal.setMaxAntecedentLength(maxAntecedentLength);
            // the next line run the algorithm.
            // Note: we pass null as output file path, because we don't want
            // to save the result to a file, but keep it into memory.
            AssocRules rules = algoAgrawal.runAlgorithm(patterns, null, databaseSize, minconf, minlift);
            rules.printRulesWithLift(databaseSize);
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Example of how to mine all association rules with FPGROWTH, from the
     * source code.
     *
     * @author Philippe Fournier-Viger (Copyright 2008)
     */
    @Test
    public void TestAllAssociationRules_FPGrowth_saveToMemory() {
        try {
            // Loading the binary context
            String input = fileToPath("contextIGB.txt");

            // By changing the following lines to some other values
            // it is possible to restrict the number of items in the antecedent and
            // consequent of rules
            int maxConsequentLength = 100;
            int maxAntecedentLength = 100;

            // STEP 1: Applying the FP-GROWTH algorithm to find frequent itemsets
            double minsup = 0.5;
            AlgoFPGrowth fpgrowth = new AlgoFPGrowth();
            fpgrowth.setMaximumPatternLength(maxAntecedentLength + maxConsequentLength);
            Itemsets patterns = fpgrowth.runAlgorithm(input, null, minsup);
            int databaseSize = fpgrowth.getDatabaseSize();
            patterns.printItemsets(databaseSize);

            // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
            double minconf = 0.60;
            AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
            // 
            algoAgrawal.setMaxConsequentLength(maxConsequentLength);
            algoAgrawal.setMaxAntecedentLength(maxAntecedentLength);

            // the next line run the algorithm.
            // Note: we pass null as output file path, because we don't want
            // to save the result to a file, but keep it into memory.
            AssocRules rules = algoAgrawal.runAlgorithm(patterns, null, databaseSize, minconf);
            rules.printRules(databaseSize);
            System.out.println("DATABASE SIZE " + databaseSize);
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

}
