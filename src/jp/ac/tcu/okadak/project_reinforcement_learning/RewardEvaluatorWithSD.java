package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 * 報酬評価器.
 *
 * @author K.Okada
 */
public class RewardEvaluatorWithSD extends RewardEvaluator {

	private static double RWD_FN_SCH = 0.99e3D; // 0.99e3D 0.20e3D 0.01e3D
	private static double RWD_FN_CST = 0.01e3D; // 0.99e3D 0.80e3D 0.01e3D
	private static double RWD_FN_CPW = 1.00e3D; // 1.0e3D 0.01e3D

	private static double RWD_OG_SCH = 0.01e1D; // 0.99e1D 0.20e1D 0.01e1D
	private static double RWD_OG_CST = 0.99e1D; // 0.99e1D 0.80e1D 0.01e1D

	// 投資回収結果の重み係数
	private static double RWD_FN_BIZ = 1.00e-0D; // 投資回収利益は桁が大き過ぎるので

	VensimLink vensim;
	
	/**
	 * 
	 */
	public RewardEvaluatorWithSD() {
		super();
		
		vensim = new VensimLink();
		vensim.prepare("InterfaceModel.vpmx", "base");	// 実行準備
	}

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

		double simTime = state.getSimTime() / 4.0e0d;	// 週→月の単位変換
		double ac = state.getAC() * 250000.0e0D;		// 人週→円への単位変換 
		
		double pjStartTime = 13;
		double pjCompletionTime = pjStartTime + simTime;
		
		vensim.process(pjStartTime, pjCompletionTime, ac, rsc);		// 実行
//		System.out.printf("R ");
		
		reward = vensim.evaluate(36+12+12);
//		System.out.printf("E\t");

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
		reward = (1.0e0D - Math.max(spi, 1.0e0D / spi)) * RWD_OG_SCH
				+ (1.0e0D - Math.max(cpi, 1.0e0D / cpi)) * RWD_OG_CST;
		
		return reward;
	}	
}
