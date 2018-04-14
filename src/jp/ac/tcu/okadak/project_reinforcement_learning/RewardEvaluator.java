package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 *
 * 報酬評価器.
 *
 * @author K.Okada
 *
 */
public class RewardEvaluator {

	private static double RWD_ST_SCH = 1.0e-6D; // 1.0e-6D; 重視: 1000.0e0D
	private static double RWD_ST_CST = 1.0e-6D; // 1.0e-6D; 重視: 1000.0e0D
	private static double RWD_FN_SCH = 1000.0e0D; // 1000.0e0D;
	private static double RWD_FN_CST = 1000.0e0D; // 1000.0e0D

	/**
	 *
	 * 報酬を評価する.
	 *
	 *
	 */
	double evaluate(ProjectState state) {

		double reward;

		if (state.completionFlag) {
			// プロジェクト終了時
			reward = (double) (-state.delay) * RWD_FN_SCH
					+ (double) (-state.costOverrun) / 10 * RWD_FN_CST;

		} else {
			// プロジェクト進行時
			reward = (state.spi) * RWD_ST_SCH + (state.cpi) * RWD_ST_CST;
		}

		return reward;
	}
}
