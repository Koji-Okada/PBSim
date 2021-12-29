package jp.ac.tcu.okadak.project_reinforcement_learning;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * 学習エージェント.
 *
 * @author K.Okada
 */
public class QNetLearningAgent {

	int recCnt = 0;

	/**
	 * ε-Greedy 法 の ε. この値の率で探索
	 */
	private double epsilon = 0.20e0D;

	/**
	 * 学習率 α. この値の率で Q値を更新
	 */
//	private double alpha = 0.20e0D;
	private double alpha = 1.00e0D;

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
	 * 経験メモリ
	 */
	ExperienceMemory expMem;

	/**
	 * コンストラクタ.
	 */
	QNetLearningAgent() {
		super();

		// 乱数生成器を生成する
		this.randomizer = new Random();

		// Qネット関数を生成する
		qNet = new QNet();

		// 経験記憶器を生成する
		expMem = new ExperienceMemory(qNet);

//		int[] inNodes = { 1, 1, 1, 1, 1, 1, 1, 1, 1 };
//		int[] inNodes = { 10, 4, 4, 4, 4, 4, 4, 4, 4 };
		int[] inNodes = { 10, 1, 1, 1, 1, 1, 1, 1, 1 };
		int df = QNet.NORMAL | QNet.DIFFERENTIAL;
		int tbdf = QNet.TOP_BOUNDARY_PLUS | QNet.DIFFERENTIAL;
		int nr = QNet.NORMAL;
		int[] encodings = { tbdf, nr, nr, nr, nr, nr, nr, nr, nr };

		qNet.generate(inNodes, encodings);
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

			QAction qAc = getMaxQAction(state);
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
	double qLearn(ProjectState preState, ProjectManagementAction action, double reward, ProjectState postState) {

		double gap = 0.0e0D; // 返り値を初期化する

		Experience exp = new Experience(preState, action, postState, reward);

		Experience[] data = null;
		double rate = 0.20e0D;

		if ((randomizer.nextDouble() <= rate)) {
			// 学習データに加える際に間引く
			data = expMem.addExperience(exp); // 経験として記憶させる
		} else if (postState.isComplete()) {
			// プロジェクト完了時は大きな報酬が与えられるので強制的に記憶させる
			data = expMem.addExperience(exp); // 経験として記憶させる
		}

		if (null != data) {
			// 経験記憶が溜まったら
			gap = updateByBatch(data); // Q関数をバッチ更新する
		}

		return gap;
	}

	/**
	 * 
	 * @param exp
	 */
	private double updateByBatch(Experience[] exp) {

//		int anchorRate = 2;
		int anchorRate = 1;
		int num = exp.length;
		int numForPre = num;
		int numForPost = numForPre * MAX_Q_AP * MAX_Q_IE * MAX_Q_SA;
		int numForAnchor = num * anchorRate;

		float[][] data = new float[numForPost + numForPre + numForAnchor][9]; // Q値を纏めて算出するため
		float[][] upIn = new float[numForPre + numForAnchor][9]; // Q値更新用の入力
		float[][] upOut = new float[numForPre + numForAnchor][1]; // Q値更新用のラベル

		// ==== 遷移後状態での MaxQ を求めるためのデータを作成 (Step-1A)
		int cnt = 0; // Step-1 を通じて使用
		for (int i = 0; i < numForPre; i++) {
			ProjectState state = exp[i].getPostState();
			double dPrgR = state.getProgressRate();
			double dSpi = state.getSPI();
			double dCpi = state.getCPI();
			double dAvgAppPrs = state.getAverageAP();
			double dAvgIncEff = state.getAverageIE();
			double dAvgScpAdj = state.getAverageSA();

			for (int a0 = ProjectManagementAction.MIN_ACTION_AP; a0 <= ProjectManagementAction.MAX_ACTION_AP; a0++) {
				for (int a1 = ProjectManagementAction.MIN_ACTION_IE; a1 <= ProjectManagementAction.MAX_ACTION_IE; a1++) {
					for (int a2 = ProjectManagementAction.MIN_ACTION_SA; a2 <= ProjectManagementAction.MAX_ACTION_SA; a2++) {
						data[cnt][0] = transProgress(dPrgR, false);
						data[cnt][1] = transSpi(dSpi);
						data[cnt][2] = transCpi(dCpi);
						data[cnt][3] = transActionMemory(dAvgAppPrs, false);
						data[cnt][4] = transActionMemory(dAvgIncEff, false);
						data[cnt][5] = transActionMemory(dAvgScpAdj, false);
						data[cnt][6] = transAction(a0, false);
						data[cnt][7] = transAction(a1, false);
						data[cnt][8] = transAction(a2, false);
						cnt++;
					}
				}
			}
		}

		// ==== 遷移前状態での更新前 Q値を求めるためのデータを作成 (Step-1B)
		for (int i = 0; i < numForPre; i++) {
			ProjectState state = exp[i].getPreState();
			double dPrgR = state.getProgressRate();
			double dSpi = state.getSPI();
			double dCpi = state.getCPI();
			double dAvgAppPrs = state.getAverageAP();
			double dAvgIncEff = state.getAverageIE();
			double dAvgScpAdj = state.getAverageSA();
			ProjectManagementAction action = exp[i].getAction();
			int iAppPrs = action.getApplyingPressure();
			int iIncEff = action.getIncreasingEffort();
			int iScpAdj = action.getScopeAdjust();

			data[cnt][0] = transProgress(dPrgR, false);
			data[cnt][1] = transSpi(dSpi);
			data[cnt][2] = transCpi(dCpi);
			data[cnt][3] = transActionMemory(dAvgAppPrs, false);
			data[cnt][4] = transActionMemory(dAvgIncEff, false);
			data[cnt][5] = transActionMemory(dAvgScpAdj, false);
			data[cnt][6] = transAction(iAppPrs, false);
			data[cnt][7] = transAction(iIncEff, false);
			data[cnt][8] = transAction(iScpAdj, false);

			for (int j = 0; j < 9; j++) {
				upIn[i][j] = data[cnt][j];
			}
			cnt++;
		}

		// ==== 錨点の Q値を求めるためのデータを作成 (Step-1C)
		for (int i = 0; i < numForAnchor; i++) {


			float th = 1.0e0f;
			boolean cFlag = false;
			float d =10.0e0f;
			do {
				data[cnt][0] = qNet.sampleX();
				data[cnt][1] = qNet.sampleX();
				data[cnt][2] = qNet.sampleX();
				data[cnt][3] = qNet.sampleX();
				data[cnt][4] = qNet.sampleX();
				data[cnt][5] = qNet.sampleX();
				data[cnt][6] = qNet.sampleX();
				data[cnt][7] = qNet.sampleX();
				data[cnt][8] = qNet.sampleX();
				
				for (int j = 0; j < numForPre; j++) {
					int p = numForPost + j;
					d = squreDist(data[cnt], data[p]);
					if (d < th) {
						cFlag = true;
//						System.out.println("    break!");
						break;
					} else {
						cFlag = false;
					}
				}
//				System.out.println("** d = " + d);					
			} while (cFlag);

			for (int j = 0; j < 9; j++) {
				upIn[numForPre + i][j] = data[cnt][j];
			}
			cnt++;
		}

		// ==== Q値を算出する (Step-2)
		INDArray in = Nd4j.create(data);
		INDArray out = qNet.getValues(in);

		// ==== 状態遷移後の最大Q値を求める (Step-3A)
		cnt = 0; // Step-3 を通じて使用
		double[] postQ = new double[numForPre];
		for (int i = 0; i < numForPre; i++) {
			double maxQ = -1.0e8;
			for (int a0 = ProjectManagementAction.MIN_ACTION_AP; a0 <= ProjectManagementAction.MAX_ACTION_AP; a0++) {
				for (int a1 = ProjectManagementAction.MIN_ACTION_IE; a1 <= ProjectManagementAction.MAX_ACTION_IE; a1++) {
					for (int a2 = ProjectManagementAction.MIN_ACTION_SA; a2 <= ProjectManagementAction.MAX_ACTION_SA; a2++) {
						double qValue = out.getDouble(cnt++);
						System.out.printf("%10.4f\t", qValue);
						if (qValue > maxQ) {
							maxQ = qValue;
						}
					}
				}
			}
			System.out.println(" ---!");
			postQ[i] = maxQ;
//			if (maxQ > 100.0e0f) {
//				System.out.println(" *-- " + maxQ + " : " + data[cnt-1][0] + ", " + data[cnt-1][1] + ", " + data[cnt-1][2]);
//			}
		}

		// ==== (状態遷移前＋行動)の Q値を求める (Step-3B)
		double gap = 0.0e0D;
		for (int i = 0; i < numForPre; i++) {
			double q0 = out.getDouble(cnt++);
			double reward = exp[i].getReward();

			double g = gamma;
			if (exp[i].getPostState().isComplete()) {
				// プロジェクト完了時の状態遷移後Q値無効化処理
				g = 0.0e0D;
			}
			double q1 = reward + g * postQ[i];

			System.out.printf("!-\t%10.4f\t=\t%10.4f\t%10.4f\t%10.4f \n", q1, reward, g, postQ[i]);

			double updateQ = (1.0e0D - alpha) * q0 + alpha * q1;
			upOut[i][0] = (float) updateQ;
			gap += (q1 - q0) * (q1 - q0);
		}

		// ==== 錨点の Q値を求める (Step-3C)
		for (int i = 0; i < numForAnchor; i++) {
			double q0 = out.getDouble(cnt++);

			upOut[numForPre + i][0] = (float) q0;
		}

		// ==== 更新処理を行う (Step-4)
		INDArray updateIn = Nd4j.create(upIn);
		INDArray updateOut = Nd4j.create(upOut);

		checkRec(updateIn, updateOut);
		double v = qNet.update(updateIn, updateOut); // 更新処理.
		checkQ();

		return gap;
	}

	/**
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	private float squreDist(float[] d1, float[] d2) {

		int dim = 9;

		float dist = 0.0e0f;
		for (int i = 0; i < dim; i++) {
			dist += (d2[i] - d1[i]) * (d2[i] - d1[i]);
		}

		return dist;
	}

	/**
	 * 状態S において最大の Q値となる行動A を求める
	 * 
	 * @param action
	 * @param state
	 * @return
	 */
	QAction getMaxQAction(final ProjectState state) {

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
		double[][] tmp = new double[MAX_Q_AP * MAX_Q_IE * MAX_Q_SA][9];
		int cnt = 0;
		for (int a0 = ProjectManagementAction.MIN_ACTION_AP; a0 <= ProjectManagementAction.MAX_ACTION_AP; a0++) {
			for (int a1 = ProjectManagementAction.MIN_ACTION_IE; a1 <= ProjectManagementAction.MAX_ACTION_IE; a1++) {
				for (int a2 = ProjectManagementAction.MIN_ACTION_SA; a2 <= ProjectManagementAction.MAX_ACTION_SA; a2++) {
					tmp[cnt][0] = transProgress(dPrgR, false);
					tmp[cnt][1] = transSpi(dSpi);
					tmp[cnt][2] = transCpi(dCpi);
					tmp[cnt][3] = transActionMemory(dAvgAppPrs, false);
					tmp[cnt][4] = transActionMemory(dAvgIncEff, false);
					tmp[cnt][5] = transActionMemory(dAvgScpAdj, false);
					tmp[cnt][6] = transAction(a0, false);
					tmp[cnt][7] = transAction(a1, false);
					tmp[cnt][8] = transAction(a2, false);
					cnt++;
				}
			}
		}

		// 取り得る全ての行動のデータの Q値を算出する
		INDArray in = Nd4j.create(tmp);
		INDArray out = qNet.getValues(in);

		// 最適値を求める
		double maxQ = -1.0e6;
		int maxArg0 = 1;
		int maxArg1 = 1;
		int maxArg2 = 1;
		cnt = 0;
		for (int a0 = ProjectManagementAction.MIN_ACTION_AP; a0 <= ProjectManagementAction.MAX_ACTION_AP; a0++) {
			for (int a1 = ProjectManagementAction.MIN_ACTION_IE; a1 <= ProjectManagementAction.MAX_ACTION_IE; a1++) {
				for (int a2 = ProjectManagementAction.MIN_ACTION_SA; a2 <= ProjectManagementAction.MAX_ACTION_SA; a2++) {
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

		applyingPressure = maxArg0;
		increasingEfforts = maxArg1;
		scopeAdjust = maxArg2;

		QAction qAc = new QAction(maxQ, applyingPressure, increasingEfforts, scopeAdjust);

		return qAc;
	}

	// ======================================================
	/**
	 *
	 * @param input (0.0 ～ 1.0)
	 * @return
	 */
	private float transProgress(double input, boolean dFlag) {

		double diversity = 1.0e-3D; // 揺らぎの大きさ
		double value = input;

		if (dFlag) {
			// 小さな揺らぎを加える
			value += (randomizer.nextDouble() - 0.5e0D) * diversity;
		}

		return (float) value;
	}

	/**
	 *
	 * @param ratio
	 * @return
	 */
	private float transSpi(double ratio) {

		// 0.75 ～ 1.25 と想定
		double min = 0.75e0D;
		double max = 1.25e0D;
		double range = max - min;
		double value = (ratio - min) / range;

		if (value < 0.0e0F) {
			value = 0.0e0F;
		} else if (value > 1.0e0F) {
			value = 1.0e0F;
		}

		return (float) value;
	}

	/**
	 *
	 * @param ratio
	 * @return
	 */
	private float transCpi(double ratio) {

		// 0.70 ～ 1.10 と想定
		double min = 0.70e0D;
		double max = 1.10e0D;
		double range = max - min;
		double value = (ratio - min) / range;

		if (value < 0.0e0F) {
			value = 0.0e0F;
		} else if (value > 1.0e0F) {
			value = 1.0e0F;
		}

		return (float) value;
	}

	/**
	 *
	 * @param input (-1.0 ～ 2.0)
	 * @return 0.0 ～ 1.0
	 */
	private float transActionMemory(double input, boolean dFlag) {

		double diversity = 1.0e-3D; // 揺らぎの大きさ
		double value = (input + 1.5e0D) / 4.0e0D;

		if (dFlag) {
			// 小さな揺らぎを加える
			value += (randomizer.nextDouble() - 0.5e0D) * diversity;

		}

		return (float) value;
	}

	/**
	 *
	 * @param input {-1, 0, 1, 2}
	 * @return 0.0 ～ 1.0
	 */
	private float transAction(int input, boolean dFlag) {

		double diversity = 1.0e-3D; // 揺らぎの大きさ
		double value = ((double) input + 1.5e0D) / 4.0e0D;

		if (dFlag) {
			// 小さな揺らぎを加える
			value += (randomizer.nextDouble() - 0.5e0D) * diversity;
		}

		return (float) value;
	}

	/**
	 * 学習のためのサンプル記録をファイル出力する.
	 * 
	 * @param in  入力値
	 * @param out 出力値
	 */
	void checkRec(INDArray in, INDArray out) {

		String path = "D:/PBSimTmp/"; // 出力フォルダパス
		String fName = "rec-" + (recCnt++) + ".txt";
		try {
			File file = new File(path + fName);
			PrintWriter pw = new PrintWriter(new FileWriter(file));

			pw.println("Num\tProgressRate\tSpi\tCpi\tAvAppPrs\tAvIncEff\tAvScpAdj\tAppPrs\tIncEff\tScpAdj\t\tTargetQ");

			for (int i = 0; i < in.size(0); i++) {
				pw.printf(" %4d\t", i);
				for (int j = 0; j < in.size(1); j++) {
					pw.printf("%16.8f\t", in.getFloat(i, j));
				}
				pw.printf("\t%16.8f", out.getFloat(i, 0));
				pw.println();
			}

			pw.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * 
	 * 学習途中における Q値の値を、Action と ProgressRate の断面で確認する.
	 * 
	 */
	void checkQ() {

		float[][] tmp = new float[11 * 4][9];
		int cnt;

		System.out.println("Q Learnt --");

		// Applying Pressure と Progress Rate の断面で、Q値を表示
		System.out.println("  Applying Pressure.");
		cnt = 0;
		for (int a = ProjectManagementAction.MIN_ACTION_AP; a <= ProjectManagementAction.MAX_ACTION_AP; a++) {
			for (int i = 0; i <= 10; i++) {
				tmp[cnt][0] = (float) i / 10.0e0F;
				tmp[cnt][1] = 0.5e0F;
				tmp[cnt][2] = 0.5e0F;
				tmp[cnt][3] = 0.5e0F;
				tmp[cnt][4] = 0.5e0F;
				tmp[cnt][5] = 0.5e0F;
				tmp[cnt][6] = transAction(a, false);
				tmp[cnt][7] = 0.5e0F;
				tmp[cnt][8] = 0.5e0F;
				cnt++;
			}
		}
		INDArray in1 = Nd4j.create(tmp);
		INDArray out1 = qNet.getValues(in1);
		cnt = 0;
		for (int a = ProjectManagementAction.MIN_ACTION_AP; a <= ProjectManagementAction.MAX_ACTION_AP; a++) {
			for (int i = 0; i <= 10; i++) {
				double val = out1.getDouble(cnt++);
				System.out.printf("%10.4f\t", val);
			}
			System.out.println();
		}

		// Increasing Effort と Progress Rate の断面で、Q値を表示
		System.out.println("  Increasing Effort.");
		cnt = 0;
		for (int a = ProjectManagementAction.MIN_ACTION_IE; a <= ProjectManagementAction.MAX_ACTION_IE; a++) {
			for (int i = 0; i <= 10; i++) {
				tmp[cnt][0] = (float) i / 10.0e0F;
				tmp[cnt][1] = 0.5e0F;
				tmp[cnt][2] = 0.5e0F;
				tmp[cnt][3] = 0.5e0F;
				tmp[cnt][4] = 0.5e0F;
				tmp[cnt][5] = 0.5e0F;
				tmp[cnt][6] = 0.5e0F;
				tmp[cnt][7] = transAction(a, false);
				tmp[cnt][8] = 0.5e0F;
				cnt++;
			}
		}
		INDArray in2 = Nd4j.create(tmp);
		INDArray out2 = qNet.getValues(in2);
		cnt = 0;
		for (int a = ProjectManagementAction.MIN_ACTION_IE; a <= ProjectManagementAction.MAX_ACTION_IE; a++) {
			for (int i = 0; i <= 10; i++) {
				double val = out2.getDouble(cnt++);
				System.out.printf("%10.4f\t", val);
			}
			System.out.println();
		}

		// Scope Adhustment と Progress Rate の断面で、Q値を表示
		System.out.println("  Scope Adjustment.");
		cnt = 0;
		for (int a = ProjectManagementAction.MIN_ACTION_SA; a <= ProjectManagementAction.MAX_ACTION_SA; a++) {
			for (int i = 0; i <= 10; i++) {
				tmp[cnt][0] = (float) i / 10.0e0F;
				tmp[cnt][1] = 0.5e0F;
				tmp[cnt][2] = 0.5e0F;
				tmp[cnt][3] = 0.5e0F;
				tmp[cnt][4] = 0.5e0F;
				tmp[cnt][5] = 0.5e0F;
				tmp[cnt][6] = 0.5e0F;
				tmp[cnt][7] = 0.5e0F;
				tmp[cnt][8] = transAction(a, false);
				cnt++;
			}
		}
		INDArray in3 = Nd4j.create(tmp);
		INDArray out3 = qNet.getValues(in3);
		cnt = 0;
		for (int a = ProjectManagementAction.MIN_ACTION_SA; a <= ProjectManagementAction.MAX_ACTION_SA; a++) {
			for (int i = 0; i <= 10; i++) {
				double val = out3.getDouble(cnt++);
				System.out.printf("%10.4f\t", val);
			}
			System.out.println();
		}

		System.out.println("-- Q Learnt");
	}
}
