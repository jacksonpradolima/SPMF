package ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.profile;
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
 * Apply a parameter profile by name
 */
public class ProfileManager {	
	public static void loadProfileByName(String name) {
		Profile profile = null;
		try {
			Class<?> classI = Class.forName("ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.profile."+ name + "Profile");
			profile = (Profile) classI.newInstance();
		} catch (Exception e) {
			profile = new DefaultProfile();
		}
		
		profile.Apply();
	}
}
