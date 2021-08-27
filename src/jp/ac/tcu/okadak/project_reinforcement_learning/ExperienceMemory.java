package jp.ac.tcu.okadak.project_reinforcement_learning;

public class ExperienceMemory {

	
	static int MAX_MEMORY = 256;
	static int MAX_GENERATION = 16;
	private Experience[] experiences = new Experience[MAX_MEMORY];
	private Experience[][] histories = new Experience[MAX_GENERATION][];
	
	int gen = 0;
	int cnt = 0;
	
	/**
	 *
	 * 
	 * @param exp
	 */
	Experience[] addExperience(Experience exp) {
		
		Experience[] data;
		experiences[cnt++] = exp; 
		
		if (MAX_MEMORY == cnt) {
			data = experiences;		// ここは工夫が必要
			cnt = 0;
			gen ++;

		} else {
			data = null;
		}
		
	return data;
	}	
}
