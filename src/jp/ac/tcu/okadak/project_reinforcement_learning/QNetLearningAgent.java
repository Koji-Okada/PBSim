package jp.ac.tcu.okadak.project_reinforcement_learning;

import java.util.Random;

import org.nd4j.linalg.api.ndarray.INDArray;
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
//	private double alpha = 0.10e0D;

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
//		int[] inNodes = {10, 5, 5, 5, 5, 5, 5, 5, 5};
		int[] inNodes = { 1, 1, 1, 1, 1, 1, 1, 1, 1 };

		qNet.generate(inNodes);
		qNet.initialize();

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

	private static final int MAX_Q_AP = 4;
	private static final int MAX_Q_IE = 4;
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

		ProjectManagementAction action;

		if ((epsilon < this.randomizer.nextDouble()) || (!exploring)) {
			// 最適値を適用する

			QAction qAc = getMaxQ(state);
			action = qAc.action;

		} else {
			// 乱数で行動を選択する
			int applyingPressure = (int) (Math.floor(this.randomizer.nextDouble() * (double) MAX_Q_AP)) - 1;
			int increasingEfforts = (int) (Math.floor(this.randomizer.nextDouble() * (double) MAX_Q_IE)) - 1;
			int scopeAdjust = (int) (Math.floor(this.randomizer.nextDouble() * (double) MAX_Q_SA)) - 1;

			action = new ProjectManagementAction(applyingPressure, increasingEfforts, scopeAdjust);
		}

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

		QAction qAc = getMaxQ(postState);
		double maxQ = qAc.qValue;

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

		
		int iStep = preState.getSimTime();
		if ((iStep % 8 == 7) || postState.isComplete()){
			double q0 = getQV(dPrePrgR, dPreSpi, dPreCpi, dPreAvgAppPrs, dPreAvgIncEff, dPreAvgScpAdj, iAppPrs, iIncEff,
					iScpAdj);
			double q1 = reward + gamma * maxQ;
			double updateQ = (1.0e0 - alpha) * q0 + alpha * q1;

			addRecords(updateQ, dPrePrgR, dPreSpi, dPreCpi, dPreAvgAppPrs, dPreAvgIncEff, dPreAvgScpAdj, iAppPrs,
					iIncEff, iScpAdj);
			
			return (q1 - q0) * (q1 - q0);
		}
		
		return 0.0e0D;
	}

	/**
	 * 
	 * @param action
	 * @param state
	 * @return
	 */
	QAction getMaxQ(final ProjectState state) {

		int applyingPressure = 0;
		int increasingEfforts = 0;
		int scopeAdjust = 0;

		double dPrgR = state.getProgressRate();
		double dSpi = state.getSPI();
		double dCpi = state.getCPI();
		double dAvgAppPrs = state.getAverageAP();
		double dAvgIncEff = state.getAverageIE();
		double dAvgScpAdj = state.getAverageSA();

		// 取り得る全ての行動のデータを生成する
		double[][] tmp = new double[4 * 4 * 4][9];
		int cnt = 0;
		for (int a0 = 0; a0 < 4; a0++) {
			for (int a1 = 0; a1 < 4; a1++) {
				for (int a2 = 0; a2 < 4; a2++) {
					tmp[cnt][0] = transProgress(dPrgR);
					tmp[cnt][1] = transRatio(dSpi);
					tmp[cnt][2] = transRatio(dCpi);
					tmp[cnt][3] = transActionMemory(dAvgAppPrs);
					tmp[cnt][4] = transActionMemory(dAvgIncEff);
					tmp[cnt][5] = transActionMemory(dAvgScpAdj);
					tmp[cnt][6] = transAction(a0);
					tmp[cnt][7] = transAction(a1);
					tmp[cnt][8] = transAction(a2);
					cnt++;
				}
			}
		}

		// 取り得る全ての行動のデータの Q値を算出する
		INDArray in = Nd4j.create(tmp);
		INDArray out = qNet.getValues(in);

		// 最適値を求める
		double maxQ = -1.0e8;
		int maxArg0 = 1;
		int maxArg1 = 1;
		int maxArg2 = 1;
		cnt = 0;
		for (int a0 = 0; a0 < 4; a0++) {
			for (int a1 = 0; a1 < 4; a1++) {
				for (int a2 = 0; a2 < 4; a2++) {
					double qValue = out.getDouble(cnt++);
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

		QAction qAc = new QAction(maxQ, applyingPressure, increasingEfforts, scopeAdjust);

		return qAc;
	}

	// ======================================================
	/**
	 * Q-Net関数により Q値を取得する.
	 *
	 * @return Q値
	 */
	private double getQV(double dPrgR, double dSpi, double dCpi, double dAvgAppPrs, double dAvgIncEff,
			double dAvgScpAdj, int aP, int iE, int sA) {

		float[][] tmp = new float[1][9];
		tmp[0][0] = transProgress(dPrgR);
		tmp[0][1] = transRatio(dSpi);
		tmp[0][2] = transRatio(dCpi);
		tmp[0][3] = transActionMemory(dAvgAppPrs);
		tmp[0][4] = transActionMemory(dAvgIncEff);
		tmp[0][5] = transActionMemory(dAvgScpAdj);
		tmp[0][6] = transAction(aP);
		tmp[0][7] = transAction(iE);
		tmp[0][8] = transAction(sA);
		INDArray index = Nd4j.create(tmp);
		return qNet.getValues(index).getDouble(0, 0);
	}

	/**
	 *
	 * @param ratio
	 * @return
	 */
	private float transRatio(double ratio) {
		double weight = 5.0e0D;

		double logValue = Math.log(ratio);
		double value = 1.0e0D / (1.0e0D + Math.exp(-logValue * weight));

		return (float)value;
	}

	/**
	 *
	 * @param input
	 * @return
	 */
	private float transProgress(double input) {

		double value = input;

		// 揺らぎを加える
		value += (randomizer.nextDouble() - 0.5e0D) * 1.0e-3D;

		return (float)value;
	}

	/**
	 *
	 * @param input
	 * @return
	 */
	private float transActionMemory(double input) {

		double value = (input + 1.5e0D) / 4.0e0D;

		// 揺らぎを加える
		value += (randomizer.nextDouble() - 0.5e0D) * 1.0e-3D;

		return (float)value;
	}

	/**
	 *
	 * @param input
	 * @return
	 */
	private float transAction(int input) {

		double value = ((double) input + 0.5e0D) / 4.0e0D;

		// 揺らぎを加える
		value += (randomizer.nextDouble() - 0.5e0D) * 1.0e-3D;

		return (float)value;
	}

	// ======================================================
	// ここからバッチ更新に関連する処理

	private int maxBatchSize = 512; // バッチサイズ.
	private int dummyRate = 3; // ダミーの比率.
	private int nParam = 9;

	/**
	 * 記録保持領域.
	 */
	private float recordsIn[][] = new float[maxBatchSize][nParam];
	private float recordsOut[][] = new float[maxBatchSize][1];
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
		recordsIn[recCounter][0] = transProgress(dPrePrgR);
		recordsIn[recCounter][1] = transRatio(dPreSpi);
		recordsIn[recCounter][2] = transRatio(dPreCpi);
		recordsIn[recCounter][3] = transActionMemory(dPreAvgAppPrs);
		recordsIn[recCounter][4] = transActionMemory(dPreAvgIncEff);
		recordsIn[recCounter][5] = transActionMemory(dPreAvgScpAdj);
		recordsIn[recCounter][6] = transAction(iAppPrs);
		recordsIn[recCounter][7] = transAction(iIncEff);
		recordsIn[recCounter][8] = transAction(iScpAdj);

		recordsOut[recCounter][0] = (float)updateQ;

		if (maxBatchSize == ++recCounter) {
			// バッチサイズ上限に達した場合
			leanRecords();
		}

		return;
	}

	/**
	 * 
	 * @return
	 */
	double leanRecords() {

		System.out.println("counter = " + recCounter);

		INDArray allIn;
		INDArray allOut;

		// 必要な部分だけ取り出す
		float[][] sIn = new float[recCounter][nParam];
		float[][] sOut = new float[recCounter][1];
		for (int i = 0; i < recCounter; i++) {
			for (int j = 0; j < nParam; j++) {
				sIn[i][j] = recordsIn[i][j];
			}
			sOut[i][0] = recordsOut[i][0];
		}
		INDArray samplesIn = Nd4j.create(sIn);
		INDArray samplesOut = Nd4j.create(sOut);

		// ダミーを追加する
		int dummySize = recCounter * dummyRate;
		if (0 != dummySize) {
			// ダミーのデータセットを加える
			float[][] dIn = new float[dummySize][nParam];
			for (int i = 0; i < dummySize; i++) {
				for (int j = 0; j < nParam; j++) {
					dIn[i][j] = qNet.sampleX();

					if (0 == j) { // 局面平準化制御のお試し
						dIn[i][j] *= 0.75e0F;
					}
				}
			}
			INDArray dummyIn = Nd4j.create(dIn);
			INDArray dummyOut = qNet.getValues(dummyIn);
			allIn = Nd4j.vstack(dummyIn, samplesIn);
			allOut = Nd4j.vstack(dummyOut, samplesOut);
		} else {
			allIn = samplesIn;
			allOut = samplesOut;
		}

//		System.out.println("allIn  = [" + allIn.size(0) + " : " +allIn.size(1) + " ]");
//		System.out.println("allOut = [" + allOut.size(0) + " : " +allOut.size(1) + " ]");

//		checkRec(allIn, allOut);
		double v = qNet.update(allIn, allOut); // 更新処理.

		// 確認
		INDArray confirm = qNet.getValues(samplesIn);
		for (int i = 0; i < confirm.size(0); i++) {
			double v0 = Math.tanh(samplesOut.getDouble(i, 0) * 0.01e0D);
			double v1 = confirm.getDouble(i, 0);
			if (((v0 - v1) * (v0 - v1) > 0.1e0D) || (v0 <= -0.1e0D)) {
				System.out.printf("!%3d %7.4f <-> %7.4f \n", i, v0, v1);
			}
		}

		checkQ();
		System.out.println("! Update : " + recCounter + " : " + v);

		recCounter = 0; // 記録消去.
		return v;
	}

	/**
	 * 
	 * @param in
	 * @param out
	 */
	void checkRec(INDArray in, INDArray out) {

		int size = recCounter * (1 + dummyRate);
		System.out.println("-- " + size + ":" + in.size(1));
		for (int i = 0; i < size; i++) {
			System.out.printf(" %4d\t:\t", i);
			for (int j = 0; j < in.size(1); j++) {
				System.out.printf("%10.4f\t", in.getDouble(i, j));
			}
			System.out.printf(":\t%10.4f", out.getDouble(i, 0));
			System.out.println();
		}
		System.out.println("--");

	}

	/**
	 * 
	 */
	void checkQ() {

		float[][] tmp = new float[11 * 4][9];
		int cnt;

		System.out.println("Q Learnt --");

		System.out.println("  Applying Pressure.");

		cnt = 0;
		for (int a0 = 0; a0 < 4; a0++) {
			for (int i = 0; i <= 10; i++) {
				tmp[cnt][0] = (float) i / 10.0e0F;
				tmp[cnt][1] = 0.5e0F;
				tmp[cnt][2] = 0.5e0F;
				tmp[cnt][3] = 0.5e0F;
				tmp[cnt][4] = 0.5e0F;
				tmp[cnt][5] = 0.5e0F;
				tmp[cnt][6] = (float) a0 / 4.0e0F;
				tmp[cnt][7] = 0.5e0F;
				tmp[cnt][8] = 0.5e0F;
				cnt++;
			}
		}

		INDArray in1 = Nd4j.create(tmp);
		INDArray out1 = qNet.getValues(in1);

		cnt = 0;
		for (int a0 = 0; a0 < 4; a0++) {
			for (int i = 0; i <= 10; i++) {
				double val = out1.getDouble(cnt++);
				System.out.printf("%10.4f\t", val);
			}
			System.out.println();
		}

		System.out.println("  Increasing Resource.");

		cnt = 0;
		for (int a0 = 0; a0 < 4; a0++) {
			for (int i = 0; i <= 10; i++) {
				tmp[cnt][0] = (float) i / 10.0e0F;
				tmp[cnt][1] = 0.5e0F;
				tmp[cnt][2] = 0.5e0F;
				tmp[cnt][3] = 0.5e0F;
				tmp[cnt][4] = 0.5e0F;
				tmp[cnt][5] = 0.5e0F;
				tmp[cnt][6] = 0.5e0F;
				tmp[cnt][7] = (float) a0 / 4.0e0F;
				tmp[cnt][8] = 0.5e0F;
				cnt++;
			}
		}

		INDArray in2 = Nd4j.create(tmp);
		INDArray out2 = qNet.getValues(in2);

		cnt = 0;
		for (int a0 = 0; a0 < 4; a0++) {
			for (int i = 0; i <= 10; i++) {
				double val = out2.getDouble(cnt++);
				System.out.printf("%10.4f\t", val);
			}
			System.out.println();
		}

		System.out.println("  Scope Adjustment.");

		cnt = 0;
		for (int a0 = 0; a0 < 4; a0++) {
			for (int i = 0; i <= 10; i++) {
				tmp[cnt][0] = (float) i / 10.0e0F;
				tmp[cnt][1] = 0.5e0F;
				tmp[cnt][2] = 0.5e0F;
				tmp[cnt][3] = 0.5e0F;
				tmp[cnt][4] = 0.5e0F;
				tmp[cnt][5] = 0.5e0F;
				tmp[cnt][6] = 0.5e0F;
				tmp[cnt][7] = 0.5e0F;
				tmp[cnt][8] = (float) a0 / 4.0e0F;
				cnt++;
			}
		}

		INDArray in3 = Nd4j.create(tmp);
		INDArray out3 = qNet.getValues(in3);

		cnt = 0;
		for (int a0 = 0; a0 < 4; a0++) {
			for (int i = 0; i <= 10; i++) {
				double val = out3.getDouble(cnt++);
				System.out.printf("%10.4f\t", val);
			}
			System.out.println();
		}

		System.out.println("-- Q Learnt");
	}

}
