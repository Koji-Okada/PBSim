package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 * シミュレータ本体.
 *
 * @author K.Okada
 */
public final class QLSimulator {

	/**
	 * 全体の反復回数.
	 */
	private static final int ITERATION_ALL = 500;

	/**
	 * 探索学習モードでの反復回数.
	 */
	private static final int ITERATION_WITH_EXPLORING = 3000;

	/**
	 * 収束学習モードでの反復回数.
	 */
	private static final int ITERATION_WITHOUT_EXPLORING = 1000;

	/**
	 * 学習結果算出用の反復数.
	 */
	private static final int LAST_EVALUATIOMS = 10;

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

		// 学習エージェントを生成する
		LearningAgent agent = new LearningAgent();

		// 報酬評価器を生成する
		RewardEvaluator evaluator = new RewardEvaluator();

		// 学習収束度パラメータを初期化する
		double sumLearningIndex1 = 0.0e0D;
		double sumLearningIndex2 = 0.0e0D;

		for (int j = 0; j < ITERATION_ALL; j++) {

			// プロジェクト反復のループ (探索学習モード(ε-Greedyオン))
			for (int i = 0; i < ITERATION_WITH_EXPLORING; i++) {
				ProjectModel project = new ProjectModel();
				sumLearningIndex1 += performProject(project, agent, evaluator,
						true);
			}

			// プロジェクト反復のループ (収束学習モード(ε-Greedyオフ))
			double sumDelay = 0.0e0D;
			double sumCostOverrun = 0.0e0D;
			for (int i = 0; i < ITERATION_WITHOUT_EXPLORING; i++) {
				ProjectModel project = new ProjectModel();
				sumLearningIndex2 += performProject(project, agent, evaluator,
						false);

				// 学習結果の評価を行う
				if ((ITERATION_WITHOUT_EXPLORING - i) <= LAST_EVALUATIOMS) {
					sumDelay += project.observe().getScheduleDelay();
					sumCostOverrun += project.observe().getCostOverrun();
				}
			}

			System.out.print(sumLearningIndex1 / (double) ITERATION_WITH_EXPLORING + "\t");
			System.out.print(sumLearningIndex2 / (double) ITERATION_WITHOUT_EXPLORING + "\t");
			System.out.println(sumDelay / (double) LAST_EVALUATIOMS + "\t"
					+ sumCostOverrun / (double) LAST_EVALUATIOMS);
		}

		return;
	}


	/**
	 * プロジェクトを実行する.
	 *
	 * @param project プロジェクト
	 * @param agent 学習エージェント
	 * @param evaluator 報酬評価器
	 * @param exploring 探索学習モード
	 *
	 * @return 学習収束度パラメータ
	 */
	private double performProject(final ProjectModel project,
			final LearningAgent agent, final RewardEvaluator evaluator,
			final Boolean exploring) {

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

			// エージェントに学習させる
			learningIndex += agent.learn(preState, action, reward, postState);

		} while (!postState.isComplete());

		return learningIndex / (double) postState.getSimTime();
	}
}
