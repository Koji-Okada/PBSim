package jp.ac.tcu.okadak.project_reinforcement_learning;

import jp.ac.tcu.okadak.project_attributes_generator.ProjectAttributes;
import jp.ac.tcu.okadak.project_attributes_generator.ProjectAttributesGenerator;

/**
 * シミュレータ本体.
 *
 * @author K.Okada
 */
public final class QLSimulator {

	/**
	 * 全体の反復回数.
	 */
	private static final int ITERATION_ALL = 250;

	/**
	 * 探索学習モードでの反復回数.
	 */
	private static final int ITERATION_WITH_EXPLORING = 3000;

	/**
	 * 収束学習モードでの反復回数.
	 */
	private static final int ITERATION_WITHOUT_EXPLORING = 1000;

	/**
	 * 学習結果評価用の反復数.
	 */
	private static final int LAST_EVALUATIONS = 100;

	/**
	 * コンストラクタ.
	 * (プライベート化)
	 */
	private QLSimulator() {
		super();
		return;
	}

	/**
	 * メインルーチン.
	 *
	 * @param args	デフォルトの引数指定
	 */
	public static void main(final String[] args) {

		System.out.println("Start ...");
		QLSimulator simulator = new QLSimulator();
		simulator.qLearning();
		System.out.println("... Fin.");

		return;
	}

	/**
	 * 一連の Q学習を行う.
	 */
	private void qLearning() {

		// 最良エージェント
		LearningAgent bestAgent = null;

		LearningAgent agent1 = null;
		LearningAgent agent2 = null;


		// 学習エージェントを生成する
		LearningAgent agent = new LearningAgent();

		// 報酬評価器を生成する
		RewardEvaluator evaluator = new RewardEvaluator();

		// プロジェクト属性生成器を生成する
		ProjectAttributesGenerator pjAtrGen = new ProjectAttributesGenerator();

		// 成果物規模の確率分布を設定する
//				pjAtrGen.setProductSizeDistibution(1000.0e0D * 5.0e0, 100.0e0D);
		pjAtrGen.setProductSizeDistibution(2843.371e0D, 4711.928e0D);

		// 期間変動の確立分布を設定する
//				pjAtrGen.setDurationDistribution(0.0e0D, 0.10e0D);
		pjAtrGen.setDurationDistribution(0.0e0D, 0.45e0D);

		// 工数見積係数を設定する
		pjAtrGen.setEffortEstimationParameter(1.00e0D);		// 1 + 手戻り工数率

		// 理想的工数見積係数を設定する
		pjAtrGen.setIdealEffortEstimationParameter(1.0e0D);

		for (int j = 0; j < ITERATION_ALL; j++) {
			// 全体の反復ループ

			// 学習収束度パラメータを初期化する
			double sumLearningIndex1 = 0.0e0D;
			for (int i = 0; i < ITERATION_WITH_EXPLORING; i++) {
				// プロジェクト反復のループ (探索学習モード(ε-Greedyオン))

				// プロジェクトを生成する
				ProjectAttributes pjAtr = pjAtrGen
						.generateProjectAttribute(false);
				ProjectModel project = new ProjectModel(pjAtr);

				// プロジェクトを実施する
				sumLearningIndex1 += performProject(project, agent, evaluator,
						true, true);
			}

			// 学習収束度パラメータを初期化する
			double sumLearningIndex2 = 0.0e0D;
			for (int i = 0; i < ITERATION_WITHOUT_EXPLORING; i++) {
				// プロジェクト反復のループ (収束学習モード(ε-Greedyオフ))

				// プロジェクトを生成する
				ProjectAttributes pjAtr = pjAtrGen
						.generateProjectAttribute(false);
				ProjectModel project = new ProjectModel(pjAtr);

				// プロジェクトを実施する
				sumLearningIndex2 += performProject(project, agent, evaluator,
						false, true);
			}

			// 学習速度のコンソール出力
			//			System.out
			//					.printf("%8.4f\t", sumLearningIndex1
			//							/ (double) ITERATION_WITH_EXPLORING);
			//			System.out.printf("%10.4f\t",
			//					sumLearningIndex2 / (double) ITERATION_WITHOUT_EXPLORING);
			//			System.out.println();

			double sumDelayRate = 0.0e0D;
			double sumCostOverrunRate = 0.0e0D;
			double sumReward = 0.0e0D;
			double sumProductivity = 0.0e0D;
			double sumDelayRate0 = 0.0e0D;
			double sumCostOverrunRate0 = 0.0e0D;
			double sumReward0 = 0.0e0D;
			double sumProductivity0 = 0.0e0D;

			// 学習結果の評価
			for (int i = 0; i < LAST_EVALUATIONS; i++) {

				// プロジェクト属性を生成する
				ProjectAttributes pjAtr = pjAtrGen
						.generateProjectAttribute(false);

				// プロジェクトを生成する
				ProjectModel project = new ProjectModel(pjAtr);

				// プロジェクトを実施する
				performProject(project, agent, evaluator, false, false);

				// 学習結果の評価を行う
				sumReward += evaluator.evaluate(project.observe());
				sumDelayRate += project.observe().getScheduleDelayRate();
				sumCostOverrunRate += project.observe().getCostOverrunRate();
				sumProductivity += project.observe().getProductivity();

				// 最良学習エージェントにもプロジェクトを実行させる
				if (null == bestAgent) {
					bestAgent = agent.agentClone();
				}

				// プロジェクトを生成する(プロジェクト属性は同一)
				ProjectModel project0 = new ProjectModel(pjAtr);

				// プロジェクトを実施する
				performProject(project0, bestAgent, evaluator, false,
						false);

				// 学習結果の評価を行う
				sumReward0 += evaluator.evaluate(project0.observe());
				sumDelayRate0 += project0.observe().getScheduleDelayRate();
				sumCostOverrunRate0 += project0.observe().getCostOverrunRate();
				sumProductivity0 += project0.observe().getProductivity();
			}

			// 評価結果のコンソール出力
			System.out.print("\t");
			System.out.printf("%10.4f\t",
					sumDelayRate / (double) LAST_EVALUATIONS);
			System.out.printf("%10.4f\t",
					sumCostOverrunRate / (double) LAST_EVALUATIONS);
			System.out.printf("%10.4f\t",
					sumReward / (double) LAST_EVALUATIONS);
			System.out.printf("%10.4f\t",
					sumProductivity / (double) LAST_EVALUATIONS);


			// 最良学習エージェントと結果を比較し淘汰する
			if (sumReward0 < sumReward) {
				// 現行エージェントの方が評価が高い場合
				// 最良学習エージェントを置換する
				bestAgent = agent.agentClone();
				System.out.print("*\t");
			} else {
				// 現行エージェントの方が評価が低い場合
				// 最良学習エージェントの結果に置換する
				sumReward = sumReward0;
				sumDelayRate = sumDelayRate0;
				sumCostOverrunRate = sumCostOverrunRate0;
				sumProductivity = sumProductivity0;
				System.out.print("\t");
			}

			if (ITERATION_ALL - 10 == j) {
				agent1 = agent.agentClone();
			}

			if (ITERATION_ALL - 20 == j) {
				agent2 = agent.agentClone();
			}


			// 評価結果のコンソール出力
			System.out.printf("%10.4f\t",
					sumDelayRate / (double) LAST_EVALUATIONS);
			System.out.printf("%10.4f\t",
					sumCostOverrunRate / (double) LAST_EVALUATIONS);
			System.out.printf("%10.4f\t",
					sumReward / (double) LAST_EVALUATIONS);
			System.out.printf("%10.4f\t",
					sumProductivity / (double) LAST_EVALUATIONS);
			System.out.println();
		}

		// 基準プロジェクトを生成する
		ProjectModel baseProject = new ProjectModel(1000.0e0, 20.0e0, 1.0e0, 1.0e0);

		// プロジェクトを実施する
//		bestAgent.setRecordAction(true);
		System.out.println();
		System.out.println("---- agent 0");
//		performProject(baseProject, bestAgent, evaluator, false,
//				false);
		performProject(baseProject, agent, evaluator, false,
				false);
		System.out.println();

		System.out.println();
		System.out.printf("duration = %4.0f", (double)baseProject.observe().getSimTime());
		System.out.println();
		System.out.printf("cost     = %8.3f", baseProject.observe().getAC());
		System.out.println();
		System.out.printf("schedule deley = %4.0f", (double)baseProject.observe().getScheduleDelay());
		System.out.println();
		System.out.printf("cost overrun   = %8.3f", baseProject.observe().getCostOverrun());
		System.out.println();



		// 基準プロジェクトを生成する
		baseProject = new ProjectModel(1000.0e0, 20.0e0, 1.0e0, 1.0e0);

		// プロジェクトを実施する
//		bestAgent.setRecordAction(true);
		System.out.println();
		System.out.println("---- agent 1");
//		performProject(baseProject, bestAgent, evaluator, false,
//				false);
		performProject(baseProject, agent1, evaluator, false,
				false);
		System.out.println();

		System.out.println();
		System.out.printf("duration = %4.0f", (double)baseProject.observe().getSimTime());
		System.out.println();
		System.out.printf("cost     = %8.3f", baseProject.observe().getAC());
		System.out.println();
		System.out.printf("schedule deley = %4.0f", (double)baseProject.observe().getScheduleDelay());
		System.out.println();
		System.out.printf("cost overrun   = %8.3f", baseProject.observe().getCostOverrun());
		System.out.println();


		// 基準プロジェクトを生成する
		baseProject = new ProjectModel(1000.0e0, 20.0e0, 1.0e0, 1.0e0);

		// プロジェクトを実施する
//		bestAgent.setRecordAction(true);
		System.out.println();
		System.out.println("---- base 2");
//		performProject(baseProject, bestAgent, evaluator, false,
//				false);
		performProject(baseProject, agent2, evaluator, false,
				false);
		System.out.println();

		System.out.println();
		System.out.printf("duration = %4.0f", (double)baseProject.observe().getSimTime());
		System.out.println();
		System.out.printf("cost     = %8.3f", baseProject.observe().getAC());
		System.out.println();
		System.out.printf("schedule deley = %4.0f", (double)baseProject.observe().getScheduleDelay());
		System.out.println();
		System.out.printf("cost overrun   = %8.3f", baseProject.observe().getCostOverrun());
		System.out.println();


		return;
	}

	/**
	 * プロジェクトを実行する.
	 *
	 * @param project プロジェクト
	 * @param agent 学習エージェント
	 * @param evaluator 報酬評価器
	 * @param exploring 探索学習モード
	 * @param learning	 学習させる
	 *
	 * @return 学習収束度パラメータ
	 */
	private double performProject(final ProjectModel project,
			final LearningAgent agent, final RewardEvaluator evaluator,
			final Boolean exploring, final Boolean learning) {

		double learningIndex = 0.0e0D;

		ProjectState postState;
		do {

			// 行動前の状態を観測する
			ProjectState preState = project.observe();

			// エージェントに行動を決定させる
			ProjectManagementAction action = agent.decideAction(preState,
					exploring);

			// 環境に対して行動を行う
			project.perform(action);

			// 行動後の状態を観測する
			postState = project.observe();

			// 報酬を決定する
			double reward = evaluator.evaluate(postState);

			if (learning) {
				// エージェントに学習させる
				learningIndex += agent.learn(preState, action, reward,
						postState);
			}

		} while (!postState.isComplete());

		// ※ここで action, postState, reward を使ってログを作成 ...

		return learningIndex / (double) postState.getSimTime();
	}
}
