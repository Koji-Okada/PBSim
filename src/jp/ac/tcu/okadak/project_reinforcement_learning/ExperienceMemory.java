package jp.ac.tcu.okadak.project_reinforcement_learning;

import java.util.Random;


/**
 * 経験の記憶器.
 * 
 * @author K.Okada
 */
public class ExperienceMemory {

	private QNet qNet;
	private Random rdm;
	static int MAX_MEMORY = 128; // 記憶単位
	static int MAX_GENERATION = 16; // 記憶の世代
	static int EXPERIENCE_RATE = 3;
	private Experience[][] histories; // 世代を通した記憶
	int gen = 0;
	int cnt = 0;

	/**
	 * コンストラクタ.
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
	 * 経験を記憶に追加する.
	 * 
	 * @param exp
	 * @return 記憶が上限に達した場合には経験の集合、それ以外は Null
	 */
	Experience[] addExperience(Experience exp) {

		Experience[] data = null; // 返り値を初期化する
		histories[gen][cnt] = exp; // 記憶を追加する

		if (MAX_MEMORY == ++cnt) {
			// 記憶単位の上限に達した場合
			cnt = 0; // 記憶カウントを初期化する
			if (MAX_GENERATION == ++gen) {
				// 世代カウントが上限に達した場合
				data = samplingPlus();
				for (int i = 1; i < MAX_GENERATION; i++) {
					histories[i-1] = histories[i]; // 世代を順送りし、最も古い世代は消去する
				}
				gen--; // 世代カウントを1つ戻す
			}
		} 

		return data;
	}
	
	/**
	 * 直近の記憶に、経験の記憶からサンプリングした記憶を加え、
	 * 記憶の集合を返す.
	 * 
	 * @return 経験の集合
	 */
	private Experience[] samplingPlus() {
		
		Experience[] sampleData = new Experience[MAX_MEMORY * (EXPERIENCE_RATE + 1)]; 
		
		int cnt = 0;
		for (int i = 0; i < MAX_MEMORY; i++) {
			// 直近の経験を転写する
			sampleData[cnt++] = histories[MAX_GENERATION-1][i];
			
			// 過去の経験をランダムサンプリングにより加える
			for (int j = 0; j < EXPERIENCE_RATE; j ++) {
				int n = (int) (rdm.nextFloat() * (MAX_GENERATION - 1)) ;
				int m = (int) (rdm.nextFloat() * MAX_MEMORY);
				sampleData[cnt++] = histories[n][m];
			}
		}
				
		return sampleData;
	}	
}
