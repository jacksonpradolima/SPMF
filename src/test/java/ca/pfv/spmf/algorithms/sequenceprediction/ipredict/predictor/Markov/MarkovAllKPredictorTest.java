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
package ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.Markov;

import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Item;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Sequence;
import org.junit.Test;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceStatsGenerator;
import java.io.IOException;
import org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jackson A. Prado Lima <jacksonpradolima at gmail.com>
 */
public class MarkovAllKPredictorTest {

    public String fileToPath(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource(filename).getFile();
    }

    @Test
    public void testAKOM() {
        try {
            // Load the set of training sequences
            String inputPath = fileToPath("datasets/contextCPT.txt");
            SequenceDatabase trainingSet = new SequenceDatabase();
            trainingSet.loadFileSPMFFormat(inputPath, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

            // Print the training sequences to the console
            System.out.println("--- Training sequences ---");
            for (Sequence sequence : trainingSet.getSequences()) {
                System.out.println(sequence.toString());
            }
            System.out.println();

            // Print statistics about the training sequences
            SequenceStatsGenerator.prinStats(trainingSet, " training sequences ");

            // The following line is to set optional parameters for the prediction model. 
            // Here we set the order of the markov model to 5.
            String optionalParameters = "order:4";

            // Train the prediction model
            MarkovAllKPredictor predictionModel = new MarkovAllKPredictor("AKOM", optionalParameters);
            predictionModel.Train(trainingSet.getSequences());

            // Now we will make a prediction.
            // We want to predict what would occur after the sequence <1, 3>.
            // We first create the sequence
            Sequence sequence = new Sequence(0);
            sequence.addItem(new Item(1));
            sequence.addItem(new Item(4));

            // Then we perform the prediction
            Sequence thePrediction = predictionModel.Predict(sequence);
            System.out.println("For the sequence <(1),(4)>, the prediction for the next symbol is: +" + thePrediction);

            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
