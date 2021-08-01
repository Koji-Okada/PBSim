package jp.ac.tcu.okadak.project_reinforcement_learning;

import java.util.Random;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/**
 * 学習エージェント.
 *
 * @author K.Okada
 */
public class QNetLearningAgent {
	
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
	 * 乱数生成器の乱数種を設定する (再現性確保のため)
	 */
	void SetRandomSeed(int randomSeed) {
		// 新たな乱数生成器を生成する
		this.randomizer = new Random(randomSeed);
	}
	
	/**
	 * Q-Network 関数.
	 */
	QNet qNet;

	/**
	 * コンストラクタ.
	 */
	QNetLearningAgent() {
		super();

		// 乱数生成器を生成する
		this.randomizer = new Random();

		// Qネット関数を生成する
		qNet = new QNet();
		qNet.init(9);
		
		return;
	}

	/**
	 * コンストラクタ.
	 *
	 * @param agentID エージェントID.
	 */
	QNetLearningAgent(int agentID) {

		this();
		this.SetRandomSeed(agentID);
		
		return;
	}


	// ======================================================
		
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

		double dPrgR = state.getProgressRate();
		double dSpi = state.getSPI();
		double dCpi = state.getCPI();
		double dAvgAppPrs = state.getAverageAP();
		double dAvgIncEff = state.getAverageIE();
		double dAvgScpAdj = state.getAverageSA();

		if ((epsilon < this.randomizer.nextDouble()) || (!exploring)) {
			// 最適値を適用する
			double maxQ = -1.0e8;
			int maxArg0 = 1;
			int maxArg1 = 1;
			int maxArg2 = 1;
			for (int a0 = 0; a0 < 4; a0++) {
				for (int a1 = 0; a1 < 4; a1++) {
					for (int a2 = 0; a2 < 4; a2++) {
						
						double qValue = getQV(dPrgR, dSpi, dCpi, dAvgAppPrs, dAvgIncEff, dAvgScpAdj, a0, a1, a2);
						
						if (qValue > maxQ) {
							maxQ = qValue;
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
	 * 学習する. (Q更新)
	 *
	 * @param preState  行動前の状態
	 * @param action    行動
	 * @param reward    報酬
	 * @param postState 行動後の状態
	 * @return 行動前Q値と行動後Q値の誤差平方
	 */
	double learn(ProjectState preState, ProjectManagementAction action, double reward, ProjectState postState) {

		double dPostPrgR = postState.getProgressRate();
		double dPostSpi = postState.getSPI();
		double dPostCpi = postState.getCPI();
		double dPostAvgAppPrs = postState.getAverageAP();
		double dPostAvgIncEff = postState.getAverageIE();
		double dPostAvgScpAdj = postState.getAverageSA();
				
		// 状態変化後の最適値を検索
		double maxQ = -1.0e8;
		for (int a0 = 0; a0 < 4; a0++) {
			for (int a1 = 0; a1 < 4; a1++) {
				for (int a2 = 0; a2 < 4; a2++) {
					try {
						double qValue = getQV(dPostPrgR, dPostSpi, dPostCpi, dPostAvgAppPrs, dPostAvgIncEff, dPostAvgScpAdj, a0, a1, a2);

						if (qValue > maxQ) {
							maxQ = qValue;
						}
					} catch (Exception e) {
						System.out.println(e);
					}
				}
			}
		}

		// Ｑテーブルを更新する

		double dPrePrgR = preState.getProgressRate();
		double dPreSpi = preState.getSPI();
		double dPreCpi = preState.getCPI();
		double dPreAvgAppPrs = preState.getAverageAP();
		double dPreAvgIncEff = preState.getAverageIE();
		double dPreAvgScpAdj = preState.getAverageSA();
				
		// 制御行動 applyingPressure の値を離散化(テーブル用)する
		int iAppPrs = action.getApplyingPressure() + 1;

		// 制御行動 applyingPressure の値を離散化(テーブル用)する
		int iIncEff = action.getIncreasingEffort() + 1;

		// 制御行動 scopeAdjust の値を離散化(テーブル用)する
		int iScpAdj = action.getScopeAdjust() + 1;

		double q0 = getQV(dPrePrgR, dPreSpi, dPreCpi, dPreAvgAppPrs, dPreAvgIncEff, dPreAvgScpAdj, iAppPrs, iIncEff, iScpAdj);		
		double q1 = reward + gamma * maxQ;
		double updateQ = (1.0e0 - alpha) * q0 + alpha * q1;

		addRecords(updateQ, dPrePrgR, dPreSpi, dPreCpi, dPreAvgAppPrs, dPreAvgIncEff, dPreAvgScpAdj, iAppPrs, iIncEff, iScpAdj);

		return (q1 - q0) * (q1 - q0);
	}

	
	// ======================================================	
	/**
	 * Q-Net関数により Q値を取得する.
	 *
	 * @return Q値
	 */
	private double getQV(double dPrgR, double dSpi, double dCpi, double dAvgAppPrs, double dAvgIncEff, double dAvgScpAdj, int aP, int iE, int sA) {

		double[][] tmp = new double[1][9];
		tmp[0][0] = transLinear(dPrgR, 10.0e0D);
		tmp[0][1] = transRatio(dSpi);
		tmp[0][2] = transRatio(dCpi);
		tmp[0][3] = transLinear(dAvgAppPrs, 4.0e0D);
		tmp[0][4] = transLinear(dAvgIncEff, 4.0e0D);
		tmp[0][5] = transLinear(dAvgScpAdj, 4.0e0D);
		tmp[0][6] = transLinear((double)aP, 4.0e0D);
		tmp[0][7] = transLinear((double)iE, 4.0e0D);
		tmp[0][8] = transLinear((double)sA, 4.0e0D);
		INDArray index = Nd4j.create(tmp);
		return qNet.getValue(index);
	}	
	
	/**
	 * 
	 * @param ratio
	 * @return
	 */
	private double transRatio(double ratio) {
		double weight = 5.0e0D;
		
		double logValue = Math.log(ratio);
		double value = 1.0e0D / (1.0e0D + Math.exp(-logValue * weight));		

		return value;
	}
	
	/**
	 * 
	 * @param input
	 * @return
	 */
	private double transLinear(double input, double max) {
		
		double value = input / max;		

		return value;
	}

	private double transLinear(int input, double max) {
		
		double value = (double)input / max;
		value += (randomizer.nextDouble() - 0.5e0D) / (max * 10.0e0);

		return value;
	}
	
	
	
	// ======================================================
	// ここからバッチ更新に関連する処理
	
	private int batchSize = 256 * 2;		// バッチサイズ.
	private int dummyRate = 3;				// ダミーの比率.
	
	private int maxRec = batchSize + (1 + dummyRate);
	private int nStParam = 6;
	private int nAcParam = 3;
	private int nParam = nStParam + nAcParam;
	
	/**
	 * 記録保持領域.
	 */
	private double recordsIn[][] = new double[maxRec][nParam];
	private double recordsOut[][] = new double[maxRec][1];
	private int recCounter = 0;

	/**
	 * 記録を追加する.
	 *
	 * @param in
	 * @param out
	 */
	private void addRecords(double updateQ, double dPrePrgR, double dPreSpi, double dPreCpi, double dPreAvgAppPrs,
			double dPreAvgIncEff, double dPreAvgScpAdj, int iAppPrs, int iIncEff, int iScpAdj) {
		
		// データセットを加える
		recordsIn[recCounter][0] = transLinear(dPrePrgR, 10.0e0D);
		recordsIn[recCounter][1] = transRatio(dPreSpi);
		recordsIn[recCounter][2] = transRatio(dPreCpi);
		recordsIn[recCounter][3] = transLinear(dPreAvgAppPrs, 4.0e0D);
		recordsIn[recCounter][4] = transLinear(dPreAvgIncEff, 4.0e0D);
		recordsIn[recCounter][5] = transLinear(dPreAvgScpAdj, 4.0e0D);
		recordsIn[recCounter][6] = transLinear(iAppPrs, 4.0e0D);
		recordsIn[recCounter][7] = transLinear(iIncEff, 4.0e0D);
		recordsIn[recCounter][8] = transLinear(iScpAdj, 4.0e0D);
		recordsOut[recCounter][0] = updateQ;

		// ダミーのデータセットを加える
		for (int i = 0; i < dummyRate ; i++) {
			++ recCounter;
			
			double[][] tmp = new double[1][9];
			
			for (int j = 0; j < 9; j++) {
				recordsIn[recCounter][j] = tmp[0][j] = qNet.sampleX();
			}
			INDArray index = Nd4j.create(tmp);
			recordsOut[recCounter][0] = qNet.getValue(index);		
		}

		
		if (maxRec == ++recCounter) {
			// バッチサイズ上限に達した場合
			
			INDArray in = Nd4j.create(recordsIn);
			INDArray out = Nd4j.create(recordsOut);

			DataSet allData = new DataSet(in, out);
			
			double v = qNet.update(allData);	// 更新処理.
			recCounter = 0;						// 記録消去.
			
			System.out.println("! Update : " + v);
		}

		return;
	}
}
