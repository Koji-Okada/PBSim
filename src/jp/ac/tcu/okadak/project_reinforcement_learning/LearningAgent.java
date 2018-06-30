package jp.ac.tcu.okadak.project_reinforcement_learning;

import java.util.Random;

/**
 * 学習エージェント.
 *
 * @author K.Okada
 */
public class LearningAgent {

	/**
	 * ε-Greedy 法 のε.
	 * この値の率で探索
	 */
	static final double EPSILON = 0.20e0D;

	/**
	 * 学習率α.
	 * この値の率で Q値を更新
	 */
	private static final double ALPHA = 0.10e0D;

	/**
	 * 割引率γ.
	 * この値の率を乗算
	 */
	private static final double GAMMA = 1.00e0D;

	/**
	 * 乱数生成器.
	 */
	private Random randomizer;

	/**
	 * Qテーブル.
	 */
	private double[][][][][][][] qTable;
	// Qテーブルの構成は以下の 7次元配列
	// == STATE ==
	// [progress]
	// [spi]
	// [cpi]
	// [averageAP]
	// [averageIE]
	// == ACTION ==
	// [applyingPressure]
	// [IncreasingEffort]

	/**
	 * Qテーブル配列 progress軸の上限値.
	 */
	private static final int MAX_Q_PRG = 11;

	/**
	 * Qテーブル配列 SPI軸の上限値.
	 */
	private static final int MAX_Q_SPI = 4;

	/**
	 * Qテーブル配列 CPI軸の上限値.
	 */
	private static final int MAX_Q_CPI = 4;

	/**
	 * Qテーブル配列 averageAP軸の上限値.
	 */
	private static final int MAX_Q_AVG_AP = 4;

	/**
	 * Qテーブル配列 averageIE軸の上限値.
	 */
	private static final int MAX_Q_AVG_IE = 4;

	/**
	 * Qテーブル配列 applyingPressure軸の上限値.
	 */
	private static final int MAX_Q_AP = 4;

	/**
	 * Qテーブル配列 increasingEffort軸の上限値.
	 */
	private static final int MAX_Q_IE = 4;

	/**
	 *
	 */
	private Boolean recordAction = false;

	/**
	 *
	 * @param flag
	 */
	void setRecordAction(Boolean flag) {
		this.recordAction = flag;

		return;
	}


	/**
	 * コンストラクタ.
	 */
	LearningAgent() {
		super();

		// 乱数生成器を生成する
		this.randomizer = new Random();

		// Qテーブルを初期化する
		// 初期値は全て 0.0e0D
		qTable = new double[MAX_Q_PRG][MAX_Q_SPI][MAX_Q_CPI][MAX_Q_AVG_AP][MAX_Q_AVG_IE][MAX_Q_AP][MAX_Q_IE];
		for (int i0 = 0; i0 < MAX_Q_PRG; i0++) {
			for (int i1 = 0; i1 < MAX_Q_SPI; i1++) {
				for (int i2 = 0; i2 < MAX_Q_CPI; i2++) {
					for (int i3 = 0; i3 < MAX_Q_AVG_AP; i3++) {
						for (int i4 = 0; i4 < MAX_Q_AVG_IE; i4++) {
							for (int i5 = 0; i5 < MAX_Q_AP; i5++) {
								for (int i6 = 0; i6 < MAX_Q_IE; i6++) {
									qTable[i0][i1][i2][i3][i4][i5][i6] = 0.0e0D;
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
	 * 乱数生成器の乱数種を設定する
	 * (再現性確保のため)
	 */
	void SetRandomSeed(int randomSeed) {
		// 新たな乱数生成器を生成する
		this.randomizer = new Random(randomSeed);
	}


	/**
	 * 学習エージェントのクローンを作成する.
	 * (Qテーブルのみクローンで、乱数シードはクローンでない)
	 *
	 * @return	学習エージェントのクローン
	 */
	LearningAgent agentClone() {

		// 学習エージェントを生成する
		LearningAgent clonedAgent = new LearningAgent();

		// Qテーブルを複写する
		for (int i0 = 0; i0 < MAX_Q_PRG; i0++) {
			for (int i1 = 0; i1 < MAX_Q_SPI; i1++) {
				for (int i2 = 0; i2 < MAX_Q_CPI; i2++) {
					for (int i3 = 0; i3 < MAX_Q_AVG_AP; i3++) {
						for (int i4 = 0; i4 < MAX_Q_AVG_IE; i4++) {
							for (int i5 = 0; i5 < MAX_Q_AP; i5++) {
								for (int i6 = 0; i6 < MAX_Q_IE; i6++) {
									clonedAgent.qTable[i0][i1][i2][i3][i4][i5][i6] = this.qTable[i0][i1][i2][i3][i4][i5][i6];
								}
							}
						}
					}
				}
			}
		}

		return clonedAgent;
	}

	/**
	 * 行動を決定する. (ε-Greedy 法)
	 *
	 * @param state 状態
	 * @param exploring 探索学習モードか否か
	 *
	 * @return 決定された行動
	 */
	final ProjectManagementAction decideAction(final ProjectState state,
			final Boolean exploring) {

		int applyingPressure = 0;
		int increasingEfforts = 0;

		// 状態量 progressRate の値を離散化する.
		int progressRate = discretizeProgressRate(state.getProgressRate());

		// 状態量 SPI の値を離散化する
		int spi = discretizeSPI(state.getSPI());

		// 状態量 CPI の値を離散化する
		int cpi = discretizeCPI(state.getCPI());

		// 状態量 averageApplyingPressure の値を離散化する
		int averageApplyingPressure = discretizeAverageApplyingPressure(state
				.getAverageAP()) + 1;

		// 状態量 averageIncreasingEfforts の値を離散化する
		int averageIncreasingEfforts = discretizeAverageIncreasingEfforts(state
				.getAverageIE()) + 1;

		if ((EPSILON < this.randomizer.nextDouble()) || (!exploring)) {
			// 最適値を適用する
			double maxQ = -1.0e8;
			int maxArg0 = 1;
			int maxArg1 = 1;
			for (int a0 = 0; a0 < 4; a0++) {
				for (int a1 = 0; a1 < 4; a1++) {
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
			// 乱数で行動を選択する
			applyingPressure = (int) (Math.floor(this.randomizer.nextDouble()
					* (double) MAX_Q_AP)) - 1;
			increasingEfforts = (int) (Math.floor(this.randomizer.nextDouble()
					* (double) MAX_Q_IE)) - 1;
		}

		ProjectManagementAction action = new ProjectManagementAction(
				applyingPressure, increasingEfforts);

		if (recordAction) {
			if (0 == state.getSimTime()) {
				System.out.println("SimTime\t" + "ProgRate\t" + "SPI\t"
						+ "CPI\t" + "AvrgAplPressr\t" + "AvrgIncEffort\t"
						+ "ApprlyPressure\t" + "IncreaseEffort");
			}
			System.out.println(state.getSimTime() + "\t" + progressRate + "\t"
					+ (spi - 1) + "\t" + (cpi - 1) + "\t"
					+ (averageApplyingPressure - 1) + "\t"
					+ (averageIncreasingEfforts - 1) + "\t" + applyingPressure
					+ "\t" + increasingEfforts);
		}

		return action;
	}

	/**
	 * 学習する.
	 * (Qテーブル更新)
	 *
	 * @param preState		行動前の状態
	 * @param action		行動
	 * @param reward		報酬
	 * @param postState	行動後の状態
	 * @return				行動前Q値と行動後Q値の誤差平方
	 */
	double learn(ProjectState preState, ProjectManagementAction action,
			double reward, ProjectState postState) {

		// 状態量 progressRate の値を離散化する.
		int postProgressRate = discretizeProgressRate(postState
				.getProgressRate());

		// 状態量 SPI の値を離散化する
		int postSpi = discretizeSPI(postState.getSPI());

		// 状態量 CPI の値を離散化する
		int postCpi = discretizeCPI(postState.getCPI());

		// 状態量 averageApplyingPressure の値を離散化する
		int postAverageApplyingPressure = discretizeAverageApplyingPressure(
				postState.getAverageAP()) + 1;

		// 状態量 averageIncreasingEfforts の値を離散化する
		int postAverageIncreasingEfforts = discretizeAverageIncreasingEfforts(
				postState.getAverageIE()) + 1;

		// 状態変化後の最適値を検索
		double maxQ = -1.0e8;
		int maxArg0 = 1;
		int maxArg1 = 1;
		for (int a0 = 0; a0 < 4; a0++) {
			for (int a1 = 0; a1 < 4; a1++) {
				// System.out.println("["+ simTime + "][" + spi + "][" + cpi +
				// "][" + a0 + "][" + a1 + "]");
				if (qTable[postProgressRate][postSpi][postCpi][postAverageApplyingPressure][postAverageIncreasingEfforts][a0][a1] > maxQ) {
					maxQ = qTable[postProgressRate][postSpi][postCpi][postAverageApplyingPressure][postAverageIncreasingEfforts][a0][a1];
					maxArg0 = a0;
					maxArg1 = a1;
				}
			}
		}

		// Ｑテーブルを更新する

		// 状態量 progressRate の値を離散化する.
		int preProgressRate = discretizeProgressRate(preState
				.getProgressRate());

		// 状況 SPI の値を離散化する
		int preSpi = discretizeSPI(preState.getSPI());

		// 状況 CPI の値を離散化する
		int preCpi = discretizeCPI(preState.getCPI());

		// 状態量 averageApplyingPressure の値を離散化する
		int preAverageApplyingPressure = discretizeAverageApplyingPressure(
				preState.getAverageAP()) + 1;

		// 状態量 averageIncreasingEfforts の値を離散化する
		int preAverageIncreasingEfforts = discretizeAverageIncreasingEfforts(
				preState.getAverageIE()) + 1;

		double q0 = qTable[preProgressRate][preSpi][preCpi][preAverageApplyingPressure][preAverageIncreasingEfforts][action
				.getApplyingPressure() + 1][action.getIncreasingEffort() + 1];
		double q1 = reward + GAMMA
				* qTable[postProgressRate][postSpi][postCpi][postAverageApplyingPressure][postAverageIncreasingEfforts][maxArg0][maxArg1];

		qTable[preProgressRate][preSpi][preCpi][preAverageApplyingPressure][preAverageIncreasingEfforts][action
				.getApplyingPressure() + 1][action.getIncreasingEffort()
						+ 1] = (1.0e0 - ALPHA) * q0 + ALPHA * q1;

		return (q1 - q0) * (q1 - q0);
	}

	/**
	 * ProgressRate の値を離散化する.
	 *
	 * @param orgValue 元の値 (0.0 ～ 1.0)
	 * @return 離散化後の値 (0,1,2,...,10)
	 */
	private int discretizeProgressRate(final double orgValue) {

		final int division = 10; // 分割数

		int progressRate;
		progressRate = (int) Math.floor(orgValue * (double) division);

		return progressRate;
	}

	/**
	 * SPI値を離散化する.
	 *
	 * @param orgValue 元の値
	 * @return 離散化後の値 (0,1,2,3)
	 */
	private int discretizeSPI(final double orgValue) {

		final double th1 = 0.9e0D; // 閾値1
		final double th2 = 1.0e0D; // 閾値2
		final double th3 = 1.1e0D; // 閾値3

		final int discreteValue0 = 0; // 離散値0
		final int discreteValue1 = 1; // 離散値1
		final int discreteValue2 = 2; // 離散値2
		final int discreteValue3 = 3; // 離散値3

		int spi;
		if (orgValue <= th1) {
			spi = discreteValue0;
		} else if (orgValue <= th2) {
			spi = discreteValue1;
		} else if (orgValue <= th3) {
			spi = discreteValue2;
		} else {
			spi = discreteValue3;
		}

		return spi;
	}

	/**
	 * CPI値を離散化する.
	 *
	 * @param orgValue 元の値
	 * @return 離散化後の値
	 */
	private int discretizeCPI(final double orgValue) {

		final double th1 = 0.9e0D; // 閾値1
		final double th2 = 1.0e0D; // 閾値2
		final double th3 = 1.1e0D; // 閾値3

		final int discreteValue0 = 0; // 離散値0
		final int discreteValue1 = 1; // 離散値1
		final int discreteValue2 = 2; // 離散値2
		final int discreteValue3 = 3; // 離散値3

		int cpi;
		if (orgValue <= th1) {
			cpi = discreteValue0;
		} else if (orgValue <= th2) {
			cpi = discreteValue1;
		} else if (orgValue <= th3) {
			cpi = discreteValue2;
		} else {
			cpi = discreteValue3;
		}

		return cpi;
	}

	/**
	 * averageApplyingPressure の値を離散化する.
	 *
	 * @param orgValue 元の値
	 * @return 離散化後の値 (-1,0,1,2)
	 */
	private int discretizeAverageApplyingPressure(final double orgValue) {

		final double th1 = -0.5e0D; // 閾値1
		final double th2 = 0.5e0D; // 閾値2
		final double th3 = 1.5e0D; // 閾値3

		final int discreteValueM1 = -1; // 離散値0
		final int discreteValue0 = 0; // 離散値1
		final int discreteValueP1 = 1; // 離散値2
		final int discreteValueP2 = 2; // 離散値3

		int averageApplyingPressure;
		if (orgValue <= th1) {
			averageApplyingPressure = discreteValueM1;
		} else if (orgValue <= th2) {
			averageApplyingPressure = discreteValue0;
		} else if (orgValue <= th3) {
			averageApplyingPressure = discreteValueP1;
		} else {
			averageApplyingPressure = discreteValueP2;
		}

		return averageApplyingPressure;
	}

	/**
	 * averageIncreasingEfforts の値を離散化する.
	 *
	 * @param orgValue 元の値
	 * @return 離散化後の値 (-1,0,1,2)
	 */
	private int discretizeAverageIncreasingEfforts(final double orgValue) {

		final double th1 = -0.5e0D; // 閾値1
		final double th2 = 0.5e0D; // 閾値2
		final double th3 = 1.5e0D; // 閾値3

		final int discreteValueM1 = -1; // 離散値0
		final int discreteValue0 = 0; // 離散値1
		final int discreteValueP1 = 1; // 離散値2
		final int discreteValueP2 = 2; // 離散値3

		int averageIncreasingEfforts;
		if (orgValue <= th1) {
			averageIncreasingEfforts = discreteValueM1;
		} else if (orgValue <= th2) {
			averageIncreasingEfforts = discreteValue0;
		} else if (orgValue <= th3) {
			averageIncreasingEfforts = discreteValueP1;
		} else {
			averageIncreasingEfforts = discreteValueP2;
		}

		return averageIncreasingEfforts;
	}
}
