package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 * 報酬評価器.
 *
 * @author K.Okada T.Hayashi
 */
public class RewardEvaluator {

	private static double RWD_FN_SCH = 0.0e3D; // 1.0e3D 0.5e3D
	private static double RWD_FN_CST = 1.0e3D; // 1.0e3D 0.5e3D
	// 製品魅力の重み係数
	private static double RWD_FN_CPW = 1.0e0D; // 1.0e3D 2.0e3D 4.0e3D
	// 投資回収結果の重み係数
	private static double RWD_FN_Business = 1.0e0D;

	private static double RWD_OG_SCH = 0.0e0D; // 1.0e0D 0.50e0D
	private static double RWD_OG_CST = 1.0e0D; // 1.0e0D 0.50e0D
	// 製品魅力の重み係数
	// private static double RWD_OG_CPW= 1.0e0D; // 1.0e3D

	// 投資回収計画 とりあえず～～～～～～～～～～～～～～～～～～～～～～～～
	private static double Q = 1.00e3D; //
	private static double D = 10.00e1D; // 5.00e1D 10.00e1D
	private static double P = 0.6e0D; //
	private static double CD = 25.00e3D;//

	/**
	 * 報酬を評価する.
	 *
	 * @param state
	 *            状態
	 * @return 報酬値
	 */
	final double evaluate(final ProjectState state) {

		double reward;

		if (state.isComplete()) {
			// プロジェクト終了時

			reward = (1.0e0D - state.getScheduleDelayRate()) * RWD_FN_SCH
					+ (1.0e0D - state.getCostOverrunRate()) * RWD_FN_CST
					+ (-state.getCompromiseWorksRate()) * RWD_FN_CPW;

			// 計画通りが最大(林シミュレーションの報酬)
			// reward =
			// (Math.min(state.getScheduleDelayRate(), 1.0e0D /
			// state.getScheduleDelayRate())- 1.0e0D) * RWD_FN_SCH
			// +(Math.min(state. getCostOverrunRate (), 1.0e0D /
			// state.getCostOverrunRate ())- 1.0e0D) * RWD_FN_CST
			// + ( - state.getCompromiseWorksRate()) * RWD_FN_CPW;

			// 新方式（ビジネス重視 F'-F
//			reward = (((Q * (1.0e0D - state.getCompromiseWorksRate() * 1.0e0D))
//					* (D - state.getScheduleDelay()) * P
//					- (CD + (state.getCostOverrun() * 25.0e0D))) // F'
//					- ((Q * P * D) - CD)) * RWD_FN_Business; // F
			// 新方式（ビジネス重視 F'
			// reward = ((Q * (1.0e0D - state.getCompromiseWorksRate() * 1)) *
			// (D - state.getScheduleDelay()) * P - (CD +
			// (state.getCostOverrun()*25)) )* RWD_FN_Business; //F

		} else {
			// プロジェクト進行時

			reward = (1.0e0D
					- Math.max(state.getSPI(), 1.0e0D / state.getSPI()))
					* RWD_OG_SCH
					+ (1.0e0D
							- Math.max(state.getCPI(), 1.0e0D / state.getCPI()))
							* RWD_OG_CST;
		}

		return reward;
	}
}
