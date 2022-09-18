package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 * 報酬評価器.
 *
 * @author K.Okada T.Hayashi
 */
public class RewardEvaluator {

	private static double RWD_FN_SCH = 0.99e3D; // 0.99e3D 0.50e3D 0.01e3D
	private static double RWD_FN_CST = 0.01e3D; // 0.99e3D 0.50e3D 0.01e3D
	private static double RWD_FN_CPW = 1.00e4D; // 1.0e4D 0.01e4D

	private static double RWD_OG_SCH = 0.99e1D; // 0.99e1D 0.50e1D 0.01e1D
	private static double RWD_OG_CST = 0.01e1D; // 0.99e1D 0.50e1D 0.01e1D

	// 投資回収結果の重み係数
//	private static double RWD_FN_BIZ = 1.00e-0D; // 投資回収利益は桁が大き過ぎるので

	/**
	 * 報酬を評価する.
	 *
	 * @param state
	 *            状態
	 * @return 報酬値
	 */
	double evaluate(final ProjectState state) {

		double reward;

		if (state.isComplete()) {
			// プロジェクト完了時
			reward = evaluateAtCompletion(state);
		} else {
			// プロジェクト進行時
			reward = evaluateInProgress(state);
		}
		return reward;
	}
	
	/**
	 * プロジェクト完了時の報酬を評価する.
	 * 
	 * @param state	プロジェクト状態
	 * @return 報酬値
	 */
	private double evaluateAtCompletion(final ProjectState state) {
		
		double reward;
		
		double rsd = state.getScheduleDelayRate();
		double rco = state.getCostOverrunRate();
		double rsc = state.getScopeChangeRate();

		// スケジュール・コストは計画通りが最良
//		reward = (Math.min(rsd, 1.0e0D / rsd) - 1.0e0D) * RWD_FN_SCH
//				+ (Math.min(rco, 1.0e0D / rco) - 1.0e0D) * RWD_FN_CST
//				+ (Math.min(rsc, 1.0e0D / rsc) - 1.0e0D) * RWD_FN_CPW;

		reward = - Math.pow(Math.log(rsd), 2) * RWD_FN_SCH
				 - Math.pow(Math.log(rco), 2) * RWD_FN_CST
				 - Math.pow(Math.log(rsc), 2) * RWD_FN_CPW	;
		
		
		return reward;
	}		
	
	/**
	 * プロジェクト進行中の報酬を評価する.
	 * 
	 * @param state	プロジェクト状態
	 * @return 報酬値
	 */
	private double evaluateInProgress(final ProjectState state) {
		
		double reward;
		
		double spi = state.getSPI();
		double cpi = state.getCPI();
//		reward = (1.0e0D - Math.max(spi, 1.0e0D / spi)) * RWD_OG_SCH
//				+ (1.0e0D - Math.max(cpi, 1.0e0D / cpi)) * RWD_OG_CST;

		reward = - Math.pow(Math.log(spi), 2) * RWD_OG_SCH
				 - Math.pow(Math.log(cpi), 2) * RWD_OG_CST;

		return reward;
	}	
}
