package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 * 報酬評価器.
 *
 * @author K.Okada T.Hayashi
 */
public class RewardEvaluator {

    private static double RWD_FN_SCH = 1.0e3D; // 1.0e3D 0.4e3D 0.0e3D
    private static double RWD_FN_CST = 0.0e3D; // 1.0e3D 0.6e3D 0.0e3D
    private static double RWD_FN_CPW = 1.0e4D; // 1.0e4D 1.0e2D

    private static double RWD_OG_SCH = 1.0e0D; // 1.0e0D 0.50e0D
    private static double RWD_OG_CST = 0.0e0D; // 1.0e0D 0.50e0D

    // 投資回収結果の重み係数
    private static double RWD_FN_Business = 1.0e0D;

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

            double rsd = state.getScheduleDelayRate();
            double rco = state.getCostOverrunRate();
            double rcw = state.getCompromiseWorksRate();

            // スケジュール・コストは最小が最良
//            reward = (1.0e0D - rsd) * RWD_FN_SCH
//                    + (1.0e0D - rco) * RWD_FN_CST
//                    - rcw * RWD_FN_CPW;

            // 計画通りが最良(林シミュレーションの報酬)
            reward = (Math.min(rsd, 1.0e0D / rsd) - 1.0e0D) * RWD_FN_SCH
                    + (Math.min(rco, 1.0e0D / rco) - 1.0e0D) * RWD_FN_CST
                    - rcw * RWD_FN_CPW;

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

            double spi = state.getSPI();
            double cpi = state.getCPI();
            reward = (1.0e0D - Math.max(spi, 1.0e0D / spi)) * RWD_OG_SCH
                    + (1.0e0D - Math.max(cpi, 1.0e0D / cpi)) * RWD_OG_CST;
        }

        return reward;
    }
}
