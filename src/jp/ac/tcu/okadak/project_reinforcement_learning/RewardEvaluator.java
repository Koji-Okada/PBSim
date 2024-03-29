package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 * 報酬評価器.
 *
 * @author K.Okada T.Hayashi
 */
public class RewardEvaluator {

	private static double RWD_FN_SCH = 0.20e3D; // 0.99e3D 0.20e3D 0.01e3D
	private static double RWD_FN_CST = 0.80e3D; // 0.99e3D 0.80e3D 0.01e3D
	private static double RWD_FN_CPW = 1.00e3D; // 1.0e3D 0.01e3D

	private static double RWD_OG_SCH = 0.20e1D; // 0.99e1D 0.20e1D 0.01e1D
	private static double RWD_OG_CST = 0.80e1D; // 0.99e1D 0.80e1D 0.01e1D

	// 投資回収結果の重み係数
	private static double RWD_FN_BIZ = 1.00e-0D; // 投資回収利益は桁が大き過ぎるので

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

			double rsd = state.getScheduleDelayRate();
			double rco = state.getCostOverrunRate();
			double rsc = state.getScopeChangeRate();

			// スケジュール・コストは計画通りが最良
			reward = (Math.min(rsd, 1.0e0D / rsd) - 1.0e0D) * RWD_FN_SCH
					+ (Math.min(rco, 1.0e0D / rco) - 1.0e0D) * RWD_FN_CST
					+ (Math.min(rsc, 1.0e0D / rsc) - 1.0e0D) * RWD_FN_CPW;

			// 投資回収性の評価 (事例により、サービスモデルを切替えること)
//			 double sd = state.getScheduleDelay();
//			 double co = state.getCostOverrun();
//			 ServiceModel sm = new ServiceModel(100.0e0D, 1.0e0D);
//			 double bizRes = sm.perform(sd, co, rsc);
//			 reward = bizRes * RWD_FN_BIZ;
		} else {
			// プロジェクト進行時

			double spi = state.getSPI();
			double cpi = state.getCPI();
			reward = (1.0e0D - Math.max(spi, 1.0e0D / spi)) * RWD_OG_SCH
					+ (1.0e0D - Math.max(cpi, 1.0e0D / cpi)) * RWD_OG_CST;
		}

		return reward;
	}
}
