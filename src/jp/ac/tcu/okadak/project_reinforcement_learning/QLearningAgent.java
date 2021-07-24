package jp.ac.tcu.okadak.project_reinforcement_learning;

import java.util.Random;

/**
 * 学習エージェント.
 *
 * @author K.Okada T.Hayashi
 */
public class QLearningAgent {

	/**
	 * ε-Greedy 法 の ε. この値の率で探索
	 */
	private double epsilon = 0.20e0D;

	/**
	 * 学習率 α. この値の率で Q値を更新
	 */
	private double alpha = 0.10e0D;

	/**
	 * 割引率 γ. この値の率を乗算
	 */
	private double gamma = 1.00e0D;

	/**
	 * 乱数生成器.
	 */
	private Random randomizer;

	/**
	 * Qテーブル. Qテーブルの構成は以下の 9次元配列 == STATE == [progress] [spi] [cpi] [averageAP]
	 * [averageIE] [averageSA] == ACTION == [applyingPressure] [IncreasingEffort]
	 * [ScopeAdjust]
	 */
	private double[][][][][][][][][] qTable;

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
	 * Qテーブル配列 averageSA軸の上限値.
	 */
	private static final int MAX_Q_AVG_SA = 4;

	/**
	 * Qテーブル配列 applyingPressure軸の上限値.
	 */
	private static final int MAX_Q_AP = 4;

	/**
	 * Qテーブル配列 increasingEffort軸の上限値.
	 */
	private static final int MAX_Q_IE = 4;

	/**
	 * Qテーブル配列 scopeAdjust軸の上限値.
	 */
	private static final int MAX_Q_SA = 4;

	/**
	 * コンストラクタ.
	 */
	QLearningAgent() {
		super();

		// 乱数生成器を生成する
		this.randomizer = new Random();

		// Qテーブルを初期化する
		// 初期値は全て 0.0e0D
		qTable = new double[MAX_Q_PRG][MAX_Q_SPI][MAX_Q_CPI][MAX_Q_AVG_AP][MAX_Q_AVG_IE][MAX_Q_AVG_SA][MAX_Q_AP][MAX_Q_IE][MAX_Q_SA];
		for (int i0 = 0; i0 < MAX_Q_PRG; i0++) {
			for (int i1 = 0; i1 < MAX_Q_SPI; i1++) {
				for (int i2 = 0; i2 < MAX_Q_CPI; i2++) {
					for (int i3 = 0; i3 < MAX_Q_AVG_AP; i3++) {
						for (int i4 = 0; i4 < MAX_Q_AVG_IE; i4++) {
							for (int i5 = 0; i5 < MAX_Q_AVG_SA; i5++) {
								for (int i6 = 0; i6 < MAX_Q_AP; i6++) {
									for (int i7 = 0; i7 < MAX_Q_IE; i7++) {
										for (int i8 = 0; i8 < MAX_Q_SA; i8++) {
											qTable[i0][i1][i2][i3][i4][i5][i6][i7][i8] = 0.0e0D;
										}
									}
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
	 * @param agentID
	 */
	QLearningAgent(int agentID) {
		
		this();
		this.SetRandomSeed(agentID);
	}
	
	/**
	 * 乱数生成器の乱数種を設定する (再現性確保のため)
	 */
	void SetRandomSeed(int randomSeed) {
		// 新たな乱数生成器を生成する
		this.randomizer = new Random(randomSeed);
	}

	/**
	 * 学習エージェントのクローンを作成する. (Qテーブルのみクローンで、乱数シードはクローンでない)
	 *
	 * @return 学習エージェントのクローン
	 */
	QLearningAgent agentClone() {

		// 学習エージェントを生成する
		QLearningAgent clonedAgent = new QLearningAgent();

		// Qテーブルを複写する
		for (int i0 = 0; i0 < MAX_Q_PRG; i0++) {
			for (int i1 = 0; i1 < MAX_Q_SPI; i1++) {
				for (int i2 = 0; i2 < MAX_Q_CPI; i2++) {
					for (int i3 = 0; i3 < MAX_Q_AVG_AP; i3++) {
						for (int i4 = 0; i4 < MAX_Q_AVG_IE; i4++) {
							for (int i5 = 0; i5 < MAX_Q_AVG_SA; i5++) {
								for (int i6 = 0; i6 < MAX_Q_AP; i6++) {
									for (int i7 = 0; i7 < MAX_Q_IE; i7++) {
										for (int i8 = 0; i8 < MAX_Q_SA; i8++) {
											clonedAgent.qTable[i0][i1][i2][i3][i4][i5][i6][i7][i8] = this.qTable[i0][i1][i2][i3][i4][i5][i6][i7][i8];
										}
									}
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
	 * @param state     状態
	 * @param exploring 探索学習モードか否か
	 *
	 * @return 決定された行動
	 */
	final ProjectManagementAction decideAction(final ProjectState state, final boolean exploring) {

		int applyingPressure = 0;
		int increasingEfforts = 0;
		int scopeAdjust = 0;

		// 状態量 progressRate の値を離散化する.
		double dPrgR = state.getProgressRate();
		int iPrgR = discretizeProgressRate(dPrgR);

		// 状態量 SPI の値を離散化する
		double dSpi = state.getSPI();
		int iSpi = discretizeSpi(dSpi);

		// 状態量 CPI の値を離散化する
		double dCpi = state.getCPI();
		int iCpi = discretizeCpi(dCpi);

		// 状態量 averageApplyingPressure の値を離散化する
		double dAvgAppPrs = state.getAverageAP();
		int iAvgAppPrs = discretizeAverageApplyingPressure(dAvgAppPrs);

		// 状態量 averageIncreasingEfforts の値を離散化する
		double dAvgIncEff = state.getAverageIE();
		int iAvgIncEff = discretizeAverageIncreasingEfforts(dAvgIncEff);

		// 状態量 averageScopeAdjust の値を離散化する
		double dAvgScpAdj = state.getAverageSA();
		int iAvgScpAdj = discretizeAverageScopeAdjust(dAvgScpAdj);

		if ((epsilon < this.randomizer.nextDouble()) || (!exploring)) {
			// 最適値を適用する
			double maxQ = -1.0e8;
			int maxArg0 = 1;
			int maxArg1 = 1;
			int maxArg2 = 1;
			for (int a0 = 0; a0 < 4; a0++) {
				for (int a1 = 0; a1 < 4; a1++) {
					for (int a2 = 0; a2 < 4; a2++) {
						if (qTable[iPrgR][iSpi][iCpi][iAvgAppPrs][iAvgIncEff][iAvgScpAdj][a0][a1][a2] > maxQ) {
							maxQ = qTable[iPrgR][iSpi][iCpi][iAvgAppPrs][iAvgIncEff][iAvgScpAdj][a0][a1][a2];
							maxArg0 = a0;
							maxArg1 = a1;
							maxArg2 = a2;
						}
					}
				}
			}
			applyingPressure = maxArg0 - 1;
			increasingEfforts = maxArg1 - 1;
			scopeAdjust = maxArg2 - 1;
		} else {
			// 乱数で行動を選択する
			applyingPressure = (int) (Math.floor(this.randomizer.nextDouble() * (double) MAX_Q_AP)) - 1;
			increasingEfforts = (int) (Math.floor(this.randomizer.nextDouble() * (double) MAX_Q_IE)) - 1;
			scopeAdjust = (int) (Math.floor(this.randomizer.nextDouble() * (double) MAX_Q_SA)) - 1;
		}

		ProjectManagementAction action = new ProjectManagementAction(applyingPressure, increasingEfforts, scopeAdjust);

		return action;
	}

	/**
	 * 学習する. (Qテーブル更新)
	 *
	 * @param preState  行動前の状態
	 * @param action    行動
	 * @param reward    報酬
	 * @param postState 行動後の状態
	 * @return 行動前Q値と行動後Q値の誤差平方
	 */
	double learn(ProjectState preState, ProjectManagementAction action, double reward, ProjectState postState) {

		// 変化後状態量 progressRate の値を離散化する.
		double dPostPrgR = postState.getProgressRate();
		int iPostPrgR = discretizeProgressRate(dPostPrgR);

		// 変化後状態量 SPI の値を離散化する
		double dPostSpi = postState.getSPI();
		int iPostSpi = discretizeSpi(dPostSpi);

		// 変化後状態量 CPI の値を離散化する
		double dPostCpi = postState.getCPI();
		int iPostCpi = discretizeCpi(dPostCpi);

		// 変化後状態量 averageApplyingPressure の値を離散化する
		double dPostAvgAppPrs = postState.getAverageAP();
		int iPostAvgAppPrs = discretizeAverageApplyingPressure(dPostAvgAppPrs);

		// 変化後状態量 averageIncreasingEfforts の値を離散化する
		double dPostAvgIncEff = postState.getAverageIE();
		int iPostAvgIncEff = discretizeAverageIncreasingEfforts(dPostAvgIncEff);

		// 変化後状態量 averageScopeAdjust の値を離散化する
		double dPostAvgScpAdj = postState.getAverageSA();
		int iPostAvgScpAdj = discretizeAverageScopeAdjust(dPostAvgScpAdj);

		// 状態変化後の最適値を検索
		double maxQ = -1.0e8;
		for (int a0 = 0; a0 < 4; a0++) {
			for (int a1 = 0; a1 < 4; a1++) {
				for (int a2 = 0; a2 < 4; a2++) {
					try {
						if (qTable[iPostPrgR][iPostSpi][iPostCpi][iPostAvgAppPrs][iPostAvgIncEff][iPostAvgScpAdj][a0][a1][a2] > maxQ) {
							maxQ = qTable[iPostPrgR][iPostSpi][iPostCpi][iPostAvgAppPrs][iPostAvgIncEff][iPostAvgScpAdj][a0][a1][a2];
						}
					} catch (Exception e) {
						System.out.println(e);
					}
				}
			}
		}

		// Ｑテーブルを更新する

		// 変化前状態量 progressRate の値を離散化する.
		double preProgressRate = preState.getProgressRate();
		int iPrePrgR = discretizeProgressRate(preProgressRate);

		// 変化前状態量 SPI の値を離散化する
		double preSpi = preState.getSPI();
		int iPreSpi = discretizeSpi(preSpi);

		// 変化前状態量 CPI の値を離散化する
		double preCpi = preState.getCPI();
		int iPreCpi = discretizeCpi(preCpi);

		// 変化前状態量 averageApplyingPressure の値を離散化する
		double preAverageApplyingPressure = preState.getAverageAP();
		int iPreAvgAppPrs = discretizeAverageApplyingPressure(preAverageApplyingPressure);

		// 変化前状態量 averageIncreasingEfforts の値を離散化する
		double preAverageIncreasingEfforts = preState.getAverageIE();
		int iPreAvgIncEff = discretizeAverageIncreasingEfforts(preAverageIncreasingEfforts);

		// 変化前状態量 averageScopeAdjust の値を離散化する
		double preAverageScopeAdjust = preState.getAverageSA();
		int iPreAvgScpAdj = discretizeAverageScopeAdjust(preAverageScopeAdjust);

		// 制御行動 applyingPressure の値を離散化(テーブル用)する
		int iAppPrs = action.getApplyingPressure() + 1;
		
		// 制御行動 applyingPressure の値を離散化(テーブル用)する
		int iIncEff = action.getIncreasingEffort() + 1;

		// 制御行動 scopeAdjust の値を離散化(テーブル用)する
		int iScpAdj = action.getScopeAdjust() + 1;		
		
		double q0 = qTable[iPrePrgR][iPreSpi][iPreCpi][iPreAvgAppPrs][iPreAvgIncEff][iPreAvgScpAdj][iAppPrs][iIncEff][iScpAdj];
		double q1 = reward + gamma * maxQ;

		qTable[iPrePrgR][iPreSpi][iPreCpi][iPreAvgAppPrs][iPreAvgIncEff][iPreAvgScpAdj][iAppPrs][iIncEff][iScpAdj] = (1.0e0 - alpha) * q0 + alpha * q1;

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
	private int discretizeSpi(final double orgValue) {
		final double th1 = 0.82e0D; // 閾値1
		final double th2 = 0.94e0D; // 閾値2
		final double th3 = 1.06e0D; // 閾値3

		int discreteValue;
		if (orgValue <= th1) {
			discreteValue = 0;
		} else if (orgValue <= th2) {
			discreteValue = 1;
		} else if (orgValue <= th3) {
			discreteValue = 2;
		} else {
			discreteValue = 3;
		}

		return discreteValue;
	}

	/**
	 * CPI値を離散化する.
	 *
	 * @param orgValue 元の値
	 * @return 離散化後の値 (0,1,2,3)
	 */
	private int discretizeCpi(final double orgValue) {
		final double th1 = 0.82e0D; // 閾値1
		final double th2 = 0.94e0D; // 閾値2
		final double th3 = 1.06e0D; // 閾値3

		int discreteValue;
		if (orgValue <= th1) {
			discreteValue = 0;
		} else if (orgValue <= th2) {
			discreteValue = 1;
		} else if (orgValue <= th3) {
			discreteValue = 2;
		} else {
			discreteValue = 3;
		}

		return discreteValue;
	}

	/**
	 * averageApplyingPressure の値を離散化する.
	 *
	 * @param orgValue 元の値
	 * @return 離散化後の値 (0,1,2,3)
	 */
	private int discretizeAverageApplyingPressure(final double orgValue) {

		final double th1 = -0.5e0D; // 閾値1
		final double th2 = 0.5e0D; // 閾値2
		final double th3 = 1.5e0D; // 閾値3

		int discreteValue;
		if (orgValue <= th1) {
			discreteValue = 0;
		} else if (orgValue <= th2) {
			discreteValue = 1;
		} else if (orgValue <= th3) {
			discreteValue = 2;
		} else {
			discreteValue = 3;
		}

		return discreteValue;
	}

	/**
	 * averageIncreasingEfforts の値を離散化する.
	 *
	 * @param orgValue 元の値
	 * @return 離散化後の値 (0,1,2,3)
	 */
	private int discretizeAverageIncreasingEfforts(final double orgValue) {

		final double th1 = -0.5e0D; // 閾値1
		final double th2 = 0.5e0D; // 閾値2
		final double th3 = 1.5e0D; // 閾値3

		int discreteValue;
		if (orgValue <= th1) {
			discreteValue = 0;
		} else if (orgValue <= th2) {
			discreteValue = 1;
		} else if (orgValue <= th3) {
			discreteValue = 2;
		} else {
			discreteValue = 3;
		}

		return discreteValue;
	}

	/**
	 * averageScopeAdjust の値を離散化する.
	 *
	 * @param orgValue 元の値
	 * @return 離散化後の値 (0,1,2,3)
	 */
	private int discretizeAverageScopeAdjust(final double orgValue) {

		final double th1 = -0.5e0D; // 閾値1
		final double th2 = 0.5e0D; // 閾値2
		final double th3 = 1.5e0D; // 閾値3

		int discreteValue;
		if (orgValue <= th1) {
			discreteValue = 0;
		} else if (orgValue <= th2) {
			discreteValue = 1;
		} else if (orgValue <= th3) {
			discreteValue = 2;
		} else {
			discreteValue = 3;
		}

		return discreteValue;
	}

	/**
	 * 挙動確認用.
	 *
	 *
	 * @return Q値
	 */
	double getQV(int prg, int spi, int cpi, int avAP, int avIE, int avSA, int aP, int iE, int sA) {

		// Qテーブルの構成は以下の 9次元配列
		// == STATE ==
		// [progress]
		// [spi]
		// [cpi]
		// [averageAP]
		// [averageIE]
		// [averageSA]
		// == ACTION ==
		// [applyingPressure]
		// [IncreasingEffort]
		// [ScopeAdjust]

		return this.qTable[prg][spi][cpi][avAP][avSA][avIE][aP][iE][sA];
	}
}
