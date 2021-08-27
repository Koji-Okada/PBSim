package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 *
 * シミュレータ本体.
 *
 * @author K.Okada
 */
public final class QNetLSimulator {

	/**
	 * 全体の反復回数.
	 */
	private static final int ITERATION_ALL = 100;

	/**
	 * 探索学習モードでの反復回数.
	 */
	private static final int ITERATION_WITH_EXPLORING = 800;

	/**
	 * 収束学習モードでの反復回数.
	 */
	private static final int ITERATION_WITHOUT_EXPLORING = 200;

	/**
	 * 学習結果評価用の反復数.
	 */
	private static final int LAST_EVALUATIONS = 1;

	/**
	 * 見積りの安全率.
	 */
	private double safetyRate = 1.0e0d;

	/**
	 * エージェントID (乱数種の値).
	 */
	private int agentID = 0;
	/**
	 * コンストラクタ. (プライベート化)
	 */
	private QNetLSimulator() {
		super();
		return;
	}

	/**
	 * メインルーチン.
	 *
	 * @param args
	 *            デフォルトの引数指定
	 */
	public static void main(final String[] args) {

		System.out.println("Start ...");
		QNetLSimulator simulator = new QNetLSimulator();
		simulator.qLearning();
		System.out.println("... Fin.");

		return;
	}

	/**
	 * 一連の Q学習を行う.
	 */
	private void qLearning() {

		// 学習エージェントを生成する
		QNetLearningAgent agent = new QNetLearningAgent(agentID); // 再現性確保のため乱数種を固定する


		// 報酬評価器を生成する
		RewardEvaluator evaluator = new RewardEvaluator();

		for (int j = 0; j < ITERATION_ALL; j++) {
			// 全体の反復ループ

			// 学習収束度パラメータを初期化する
			double sumLearningIndex1 = 0.0e0D;
			for (int i = 0; i < ITERATION_WITH_EXPLORING; i++) {
				// プロジェクト反復のループ (探索学習モード(ε-Greedyオン))

				// プロジェクトを生成する
				ProjectModel project = new ProjectModel(1000.0e0D, 20.0e0D,
						1.0e0D * this.safetyRate, 1.0e0D);

				// プロジェクトを実施する
				sumLearningIndex1 += performProject(project, agent, evaluator,
						true, true);

				System.out.println(" Project " + j + "L-"+ i + " completed.");
			}

			// 学習収束度パラメータを初期化する
			double sumLearningIndex2 = 0.0e0D;
			for (int i = 0; i < ITERATION_WITHOUT_EXPLORING; i++) {
				// プロジェクト反復のループ (収束学習モード(ε-Greedyオフ))

				ProjectModel project = new ProjectModel(1000.0e0D, 20.0e0D,
						1.0e0D * this.safetyRate, 1.0e0D);

				// プロジェクトを実施する
				sumLearningIndex2 += performProject(project, agent, evaluator,
						false, true);

				System.out.println(" Project " + j + "E-"+ i + " completed.");
			}

			// 学習速度のコンソール出力
			System.out.printf("*%3d %8.4f\t", j, sumLearningIndex1 / (double) ITERATION_WITH_EXPLORING);
			System.out.printf("%10.4f\t", sumLearningIndex2 / (double) ITERATION_WITHOUT_EXPLORING);
			System.out.printf(":\t");

			double sumDelayRate = 0.0e0D;
			double sumCostOverrunRate = 0.0e0D;
			double sumDelay = 0.0e0D;
			double sumCostOverrun = 0.0e0D;
			double sumReward = 0.0e0D;
//			double sumDelayRate0 = 0.0e0D;
//			double sumCostOverrunRate0 = 0.0e0D;
//			double sumDelay0 = 0.0e0D;
//			double sumCostOverrun0 = 0.0e0D;
//			double sumReward0 = 0.0e0D;

			// 学習結果の評価
			for (int i = 0; i < LAST_EVALUATIONS; i++) {

				// プロジェクトを生成する
				ProjectModel project = new ProjectModel(1000.0e0D, 20.0e0D,
						1.0e0D * this.safetyRate, 1.0e0D);

				// プロジェクトを実施する
				performProject(project, agent, evaluator, false, false);

				// 学習結果の評価を行う
				sumReward += evaluator.evaluate(project.observe());
				sumDelayRate += project.observe().getScheduleDelayRate();
				sumCostOverrunRate += project.observe().getCostOverrunRate();
				sumDelay += project.observe().getScheduleDelay();
				sumCostOverrun += project.observe().getCostOverrun();
			}

			// 評価結果のコンソール出力
			System.out.print("\t");
			System.out.printf("%10.4f\t",
					sumDelayRate / (double) LAST_EVALUATIONS);
			System.out.printf("%10.4f\t",
					sumCostOverrunRate / (double) LAST_EVALUATIONS);
			System.out.printf("%10.4f\t", sumDelay / (double) LAST_EVALUATIONS);
			System.out.printf("%10.4f\t",
					sumCostOverrun / (double) LAST_EVALUATIONS);
			System.out.printf("%10.4f\t",
					sumReward / (double) LAST_EVALUATIONS);
			System.out.println();
			
			agent.checkQ();
		}
		return;
	}

	/**
	 * プロジェクトを実行する.
	 *
	 * @param project
	 *            プロジェクト
	 * @param agent
	 *            学習エージェント
	 * @param evaluator
	 *            報酬評価器
	 * @param exploring
	 *            探索学習モード
	 * @param learning
	 *            学習させる
	 *
	 * @return 学習収束度パラメータ
	 */
	private double performProject(final ProjectModel project,
			final QNetLearningAgent agent, final RewardEvaluator evaluator,
			final boolean exploring, final boolean learning) {

		double learningIndex = 0.0e0D;

		ProjectState postState;
		do {

			// 行動前の状態を観測する
			ProjectState preState = project.observe();

			// エージェントに行動を決定させる
			ProjectManagementAction action = agent.decideAction(preState,
					exploring);

//			if ((!exploring) && (!learning)) {
//			System.out.println(" act = " +
//			action.getApplyingPressure() + ":" + action.getIncreasingEffort() + ":" + action.getScopeAdjust());
//			}

			// 環境に対して行動を行う
			project.perform(action);

			// 行動後の状態を観測する
			postState = project.observe();

			// 報酬を決定する
			double reward = evaluator.evaluate(postState);

			if (learning) {
				// エージェントに学習させる
				learningIndex += agent.qLearn(preState, action, reward,
						postState);
			}

		} while (!postState.isComplete());

		return learningIndex / (double) postState.getSimTime();
	}
}
