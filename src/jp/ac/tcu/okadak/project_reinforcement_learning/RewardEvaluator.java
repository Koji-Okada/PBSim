package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 * 報酬評価器.
 *
 * @author K.Okada
 */
public class RewardEvaluator {

	private static double RWD_FN_SCH = 1.0e3D;	// 1.0e3D
	private static double RWD_FN_CST = 0.0e0D;	// 1.0e3D
	private static double RWD_OG_SCH = 1.0e0D;	// 1.0e3D	1.0e0D
	private static double RWD_OG_CST = 0.0e0D;	// 1.0e3D	1.0e0D

	/**
	 * 報酬を評価する.
	 *
	 * @param state 状態
	 * @return 報酬値
	 */
	final double evaluate(final ProjectState state) {

		double reward;

		if (state.isComplete()) {
			// プロジェクト終了時

			// 絶対量を用いる場合
			//			reward = (-state.getScheduleDelay()) / 100.0e0D * RWD_FN_SCH
			//					+ (-state.getCostOverrun()) / 1000.0e0D * RWD_FN_CST;

			// 比率を用いる場合
			reward = (1.0e0D - state.getScheduleDelayRate()) * RWD_FN_SCH
					+ (1.0e0D - state.getCostOverrunRate()) * RWD_FN_CST;

		} else {
			// プロジェクト進行時
			//			reward =
			//					(state.getSPI() - 1.0e0D) * RWD_ST_SCH
			//					+ (state.getCPI() - 1.0e0D) * RWD_ST_CST;

			reward = (Math.min(state.getSPI(), 1 / state.getSPI()) - 1.0e0D)
					* RWD_OG_SCH + (Math.min(state.getCPI(), 1 / state.getCPI())
							- 1.0e0D) * RWD_OG_CST;
		}

		return reward;
	}
}
