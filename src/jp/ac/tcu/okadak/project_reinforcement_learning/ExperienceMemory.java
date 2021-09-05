package jp.ac.tcu.okadak.project_reinforcement_learning;

import java.util.Random;

public class ExperienceMemory {

	private QNet qNet;
	private Random rdm;
	static int MAX_MEMORY = 128;
	static int MAX_GENERATION = 16;
	static int DUMMY_RATE = 3;
	private Experience[][] histories;
	int gen = 0;
	int cnt = 0;

	/**
	 * 
	 */
	public ExperienceMemory(QNet qN) {
		super();

		qNet = qN;
		rdm = new Random();
		
		histories = new Experience[MAX_GENERATION][];
		for (int i = 0; i < MAX_GENERATION; i++) {
			histories[i] = new Experience[MAX_MEMORY];
		}
		
		return;
	}

	/**
	 *
	 * 
	 * @param exp
	 */
	Experience[] addExperience(Experience exp) {

		Experience[] data = null;
		histories[gen][cnt++] = exp;

		if (MAX_MEMORY == cnt) {
			cnt = 0;
			gen++;
			if (MAX_GENERATION == gen) {
				data = sample();
				
				for (int i = 1; i < MAX_GENERATION; i++) {
					histories[i-1] = histories[i];
				}
				gen--;
			}
		} 

		return data;
	}
	
	/**
	 * 
	 * @return
	 */
	private Experience[] sample() {
		
		Experience[] sampleData = new Experience[MAX_MEMORY * (DUMMY_RATE + 1)]; 
		
		int cnt = 0;
		
		for (int i = 0; i < MAX_MEMORY; i++) {
			// 直近の経験を転写する
			sampleData[cnt++] = histories[MAX_GENERATION-1][i];
			
			// 過去の経験をランダムサンプリングにより加える
			for (int j = 0; j < DUMMY_RATE; j ++) {
				int n = (int) (rdm.nextFloat() * (MAX_GENERATION - 1)) ;
				int m = (int) (rdm.nextFloat() * MAX_MEMORY);
				sampleData[cnt++] = histories[n][m];
			}
			
			// 過去 N期分の経験を追加
//			for (int j = 0; j < DUMMY_RATE; j ++) {
//				sampleData[cnt++] = histories[MAX_GENERATION-2-j][i];
//			}
		}
				
		return sampleData;
	}	
}
