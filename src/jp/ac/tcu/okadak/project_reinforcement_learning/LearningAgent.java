package jp.ac.tcu.okadak.project_reinforcement_learning;

import java.util.Random;

/**
 *
 * 学習エージェント.
 *
 *
 * @author K.Okada
 *
 */
public class LearningAgent {

	static final private double EPSILON = 0.20e0D; // ε-Greedy 法 のε
	static final private double ALPHA = 0.10e0D; // 学習率
	static final private double GAMMA = 1.00e0D; // 割引率

	private Random randomizer; // 乱数生成器

	private double[][][][][][][] qTable;

	/**
	 *
	 *	コンストラクタ
	 *
	 */
	LearningAgent() {
		super();

		// 乱数生成器の生成
		randomizer = new Random(0);	// 再現性確保のためシードを固定した場合
//		randomizer = new Random();

		// Ｑテーブルの初期化
		// State:[simTime][spi][cpi]-Action:[pressure][efforts]
		qTable = new double[11][4][4][4][4][4][4];
		for (int i0 = 0; i0 < 11; i0++) {
			for (int i1 = 0; i1 < 4; i1++) {
				for (int i2 = 0; i2 < 4; i2++) {
					for (int i3 = 0; i3 < 4; i3++) {
						for (int i4 = 0; i4 < 4; i4++) {
							for (int i5 = 0; i5 < 4; i5++) {
								for (int i6 = 0; i6 < 4; i6++) {
									qTable[i0][i1][i2][i3][i4][i5][i6] = 0.0e0;
								}
							}
						}
					}
				}
			}
		}

		return;
	}

	/**
	 *
	 * 行動を決定する.
	 * (ε-Greedy 法)
	 *
	 */
	ProjectManagementAction decideAction(ProjectState state,
			Boolean afterLearningFlag) {

		int applyingPressure = 0;
		int increasingEfforts = 0;

		int simTime = state.simTime;

		// 状態量 progressRate の値を離散化する.
		int progressRate = discretizeProgressRate(state.progressRate);

		// 状態量 SPI の値を離散化する
		int spi = discretizeSPI(state.spi);

		// 状態量 CPI の値を離散化する
		int cpi = discretizeCPI(state.cpi);

		// 状態量 averageApplyingPressure の値を離散化する
		int averageApplyingPressure = discretizeAverageApplyingPressure(
				state.averageApplyingPressure);

		// 状態量 averageIncreasingEfforts の値を離散化する
		int averageIncreasingEfforts = discretizeAverageIncreasingEfforts(
				state.averageIncreasingEfforts);

		if ((EPSILON < randomizer.nextDouble()) || (afterLearningFlag)) {
			// 最適値を適用
			double maxQ = -1.0e8;
			int maxArg0 = 1;
			int maxArg1 = 1;
			for (int a0 = 0; a0 < 4; a0++) {
				for (int a1 = 0; a1 < 4; a1++) {
					//					System.out.println("["+ simTime + "][" + spi + "][" + cpi + "][" + a0 + "][" + a1 + "]");
					if (qTable[progressRate][spi][cpi][averageApplyingPressure][averageIncreasingEfforts][a0][a1] > maxQ) {
						maxQ = qTable[progressRate][spi][cpi][averageApplyingPressure][averageIncreasingEfforts][a0][a1];
						maxArg0 = a0;
						maxArg1 = a1;
					}
				}
			}
			applyingPressure = maxArg0 - 1;
			increasingEfforts = maxArg1 - 1;
		} else {
			// 乱数で行動を選択
			applyingPressure = (int) (Math.floor(randomizer.nextDouble()
					* 4.0e0)) - 1;
			increasingEfforts = (int) (Math.floor(randomizer.nextDouble()
					* 4.0e0)) - 1;

			//			System.out.println("*");
		}

		ProjectManagementAction action = new ProjectManagementAction(
				applyingPressure, increasingEfforts);

		return action;
	}

	/**
	 *
	 * 学習する.
	 * (Ｑテーブル更新)
	 *
	 */
	double learn(ProjectState preState, ProjectManagementAction action,
			double reward, ProjectState postState) {

		int postSimTime = postState.simTime;

		// 状態量 progressRate の値を離散化する.
		int postProgressRate = discretizeProgressRate(postState.progressRate);

		// 状態量 SPI の値を離散化する
		int postSpi = discretizeSPI(postState.spi);

		// 状態量 CPI の値を離散化する
		int postCpi = discretizeCPI(postState.cpi);

		// 状態量 averageApplyingPressure の値を離散化する
		int postAverageApplyingPressure = discretizeAverageApplyingPressure(
				postState.averageApplyingPressure);

		// 状態量 averageIncreasingEfforts の値を離散化する
		int postAverageIncreasingEfforts = discretizeAverageIncreasingEfforts(
				postState.averageIncreasingEfforts);

		// 状態変化後の最適値を検索
		double maxQ = -1.0e8;
		int maxArg0 = 1;
		int maxArg1 = 1;
		for (int a0 = 0; a0 < 4; a0++) {
			for (int a1 = 0; a1 < 4; a1++) {
				//				System.out.println("["+ simTime + "][" + spi + "][" + cpi + "][" + a0 + "][" + a1 + "]");
				if (qTable[postProgressRate][postSpi][postCpi][postAverageApplyingPressure][postAverageIncreasingEfforts][a0][a1] > maxQ) {
					maxQ = qTable[postProgressRate][postSpi][postCpi][postAverageApplyingPressure][postAverageIncreasingEfforts][a0][a1];
					maxArg0 = a0;
					maxArg1 = a1;
				}
			}
		}

		// Ｑテーブルを更新する

		int preSimTime = preState.simTime;

		// 状態量 progressRate の値を離散化する.
		int preProgressRate = discretizeProgressRate(preState.progressRate);

		// 状況 SPI の値を離散化する
		int preSpi = discretizeSPI(preState.spi);

		// 状況 CPI の値を離散化する
		int preCpi = discretizeCPI(preState.cpi);

		// 状態量 averageApplyingPressure の値を離散化する
		int preAverageApplyingPressure = discretizeAverageApplyingPressure(
				preState.averageApplyingPressure);

		// 状態量 averageIncreasingEfforts の値を離散化する
		int preAverageIncreasingEfforts = discretizeAverageIncreasingEfforts(
				preState.averageIncreasingEfforts);

		double q0 = qTable[preProgressRate][preSpi][preCpi][preAverageApplyingPressure][preAverageIncreasingEfforts][action.applyingPressure
				+ 1][action.increasingEfforts + 1];
		double q1 = reward + GAMMA
				* qTable[postProgressRate][postSpi][postCpi][postAverageApplyingPressure][postAverageIncreasingEfforts][maxArg0][maxArg1];

		qTable[preProgressRate][preSpi][preCpi][preAverageApplyingPressure][preAverageIncreasingEfforts][action.applyingPressure
				+ 1][action.increasingEfforts + 1] = (1.0e0 - ALPHA) * q0 + ALPHA * q1;


		//		System.out.println("T1 = " + preSimTime + " : T2 = " + postSimTime + " : Q1 = " + qTable[preSimTime][preSpi][preCpi]
		//				[action.applyingPressure + 1][action.increasingEfforts + 1] + " : Q2 = " + qTable[postSimTime][postSpi][postCpi][maxArg0][maxArg1]);

		return (q1 - q0) * (q1 -q0);
	}

	/**
	 *
	 * ProgressRate の値を離散化する.
	 *
	 */
	private int discretizeProgressRate(double org) {

		int progressRate;

		progressRate = (int) Math.floor(org * 10.0e0);

		return progressRate;
	}

	/**
	 *
	 * SPI値を離散化する．
	 *
	 */
	private int discretizeSPI(double org) {

		int spi;
		if (org <= 0.9e0) {
			spi = 0;
		} else if (org <= 1.0e0) {
			spi = 1;
		} else if (org <= 1.1e0) {
			spi = 2;
		} else {
			spi = 3;
		}

		return spi;
	}

	/**
	 *
	 * CPI値を離散化する．
	 *
	 */
	private int discretizeCPI(double org) {

		int cpi;
		if (org <= 0.9e0) {
			cpi = 0;
		} else if (org <= 1.0e0) {
			cpi = 1;
		} else if (org <= 1.1e0) {
			cpi = 2;
		} else {
			cpi = 3;
		}

		return cpi;
	}

	/**
	 *
	 * averageApplyingPressure の値を離散化する.
	 *
	 */
	private int discretizeAverageApplyingPressure(double org) {

		int averageApplyingPressure;
		if (org <= -0.5e0) {
			averageApplyingPressure = -1;
		} else if (org <= 0.5e0) {
			averageApplyingPressure = 0;
		} else if (org <= 1.5e0) {
			averageApplyingPressure = 1;
		} else {
			averageApplyingPressure = 2;
		}

		return averageApplyingPressure + 1;
	}

	/**
	 *
	 * averageIncreasingEfforts の値を離散化する.
	 *
	 */
	private int discretizeAverageIncreasingEfforts(double org) {

		int averageIncreasingEfforts;
		if (org <= -0.5e0) {
			averageIncreasingEfforts = -1;
		} else if (org <= 0.5e0) {
			averageIncreasingEfforts = 0;
		} else if (org <= 1.5e0) {
			averageIncreasingEfforts = 1;
		} else {
			averageIncreasingEfforts = 2;
		}

		return averageIncreasingEfforts + 1;
	}

	/**
	 *
	 * Ｑテーブルを表示する.
	 *
	 */
	void showQTable(int time) {

		String str;
		for (int i0 = 0; i0 < 4; i0++) {
			for (int i1 = 0; i1 < 4; i1++) {
				str = "";
				for (int i2 = 0; i2 < 4; i2++) {
					for (int i3 = 0; i3 < 4; i3++) {
						str = str + "\t" + qTable[time][i0][i1][1][1][i2][i3];
					}
				}
				System.out.println(str);
			}
		}

	}
}
