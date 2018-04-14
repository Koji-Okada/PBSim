package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 *
 * シミュレーション本体.
 *
 * @author K.Okada
 *
 */
public class QLSimulator {

	private int iteration0 = 500;
	private int iteration1 = 3000;
	private int iteration2 = 1000;
	private int last = 10;

	public static void main(String[] args) {

		QLSimulator simulator = new QLSimulator();

		System.out.println("Start ...");
		simulator.qLearning();
		System.out.println("... Fin.");

		return;
	}

	/**
	 *
	 * 一連のＱ学習を行う
	 *
	 */
	private void qLearning() {

		LearningAgent agent = new LearningAgent();
		RewardEvaluator evaluator = new RewardEvaluator();
		double sumLearningIndex1 = 0.0e0D;
		double sumLearningIndex2 = 0.0e0D;

		for (int j = 0; j < this.iteration0; j++) {

			// プロジェクト反復のループ (ε-Greedyオンで探索＆学習)
			for (int i = 0; i < this.iteration1; i++) {
				sumLearningIndex1 += performProject(agent, evaluator, false);
			}
			sumLearningIndex1 /= this.iteration1;

			// プロジェクト反復のループ (ε-Greedyオフで一旦収束させる)
			for (int i = 0; i < this.iteration2 - this.last; i++) {
				sumLearningIndex2 += performProject(agent, evaluator, true);
			}
			sumLearningIndex2 /= (this.iteration2 - this.last);

			System.out.print(sumLearningIndex1 + "\t" + sumLearningIndex2
					+ "\t");

			//			System.out.println("--");
			evaluateLearning(agent, evaluator);
			//			System.out.println("--");
		}

		return;
	}

	/**
	 *
	 *
	 *
	 */
	private double performProject(LearningAgent agent,
			RewardEvaluator evaluator, Boolean afterLearning) {

		ProjectModel project = new ProjectModel();
		double learningIndex = 0.0e0D;

		ProjectState postState;
		Boolean completionFlag;
		do {

			// 行動前の状態を観測する
			ProjectState preState = project.observe();

			// エージェントに行動を決定させる
			ProjectManagementAction action = agent.decideAction(preState,
					afterLearning);
					//	ProjectManagementAction action = new ProjectManagementAction(0, 0);

			// 環境に対して行動を行う
			project.perform(action);

			// 行動後の状態を観測する
			postState = project.observe();
			completionFlag = postState.completionFlag;

			// 報酬を求める
			double reward = evaluator.evaluate(postState);

			//			System.out.println(" R:" + reward);
			// エージェントに学習させる
			learningIndex += agent.learn(preState, action, reward, postState);

		} while (!completionFlag);

		return learningIndex / (double) postState.simTime;
	}

	/**
	 *
	 * 学習結果を評価する
	 *
	 */
	private void evaluateLearning(LearningAgent agent,
			RewardEvaluator evaluator) {

		double sumDelay = 0.0e0D;
		double sumCostOverrun = 0.0e0D;

		for (int i = 0; i < this.last; i++) {

			ProjectModel project = new ProjectModel();
			ProjectState postState;

			Boolean completionFlag;
			do {

				// 行動前の状態を観測する
				ProjectState preState = project.observe();

				// エージェントに行動を決定させる
				ProjectManagementAction action = agent.decideAction(preState,
						true);

				// 環境に対して行動を行う
				project.perform(action);

				// 行動後の状態を観測する
				postState = project.observe();

				completionFlag = postState.completionFlag;

				// 報酬を求める
				double reward = evaluator.evaluate(postState);

				// エージェントに学習させる
				agent.learn(preState, action, reward, postState);

			} while (!completionFlag);

			sumDelay += postState.delay;
			sumCostOverrun += postState.costOverrun;
		}

		System.out.println(sumDelay / (double) this.last + "\t" + sumCostOverrun
				/ (double) this.last);

		return;
	}

}
