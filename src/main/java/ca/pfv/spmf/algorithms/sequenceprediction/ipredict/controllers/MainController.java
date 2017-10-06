package ca.pfv.spmf.algorithms.sequenceprediction.ipredict.controllers;

import java.io.IOException;

import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.helpers.StatsLogger;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.Evaluator;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.CPT.CPT.CPTPredictor;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.CPT.CPTPlus.CPTPlusPredictor;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.DG.DGPredictor;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.LZ78.LZ78Predictor;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.Markov.MarkovAllKPredictor;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.Markov.MarkovFirstOrderPredictor;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.TDAG.TDAGPredictor;
/*
 * This file is copyright (c) Ted Gueniche 
 * <ted.gueniche@gmail.com>
 *
 * This file is part of the IPredict project
 * (https://github.com/tedgueniche/IPredict).
 *
 * IPredict is distributed under The MIT License (MIT).
 * You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT 
 */
/**
 * Main controller to compare all the predictors
 */
public class MainController {

	public static void main(String[] args) throws IOException {
		
			//instantiate the evaluator
			Evaluator evaluator = new Evaluator("/home/ted/java/IPredict/datasets");
			
			//Loading data set
			evaluator.addDataset("BMS", 		5000);
			evaluator.addDataset("SIGN", 		1000);
			evaluator.addDataset("MSNBC", 		5000);
			evaluator.addDataset("BIBLE_WORD", 	5000);
			evaluator.addDataset("BIBLE_CHAR", 	5000);
			evaluator.addDataset("KOSARAK", 	45000);
			evaluator.addDataset("FIFA", 		5000);
			
			//Loading predictors
			evaluator.addPredictor(new DGPredictor("DG", "lookahead:4"));
			evaluator.addPredictor(new TDAGPredictor());
			evaluator.addPredictor(new CPTPlusPredictor("CPT+",		"CCF:true CBS:true"));
			evaluator.addPredictor(new CPTPredictor());
			evaluator.addPredictor(new MarkovFirstOrderPredictor());
			evaluator.addPredictor(new MarkovAllKPredictor());
			evaluator.addPredictor(new LZ78Predictor());
			
			//Start the experiment
			StatsLogger results = evaluator.Start(Evaluator.KFOLD, 14 , true, true, true);
			
	}

}
