package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 * 報酬評価器.
 *
 * @author K.Okada T.Hayashi
 */
public class RewardEvaluator {

    private static double RWD_FN_SCH = 0.00e3D; // 1.0e3D 0.2e3D 0.0e3D
    private static double RWD_FN_CST = 1.00e3D; // 1.0e3D 0.8e3D 0.0e3D
    private static double RWD_FN_CPW = 0.0e3D; // 1.0e3D 0.0e3D

    private static double RWD_OG_SCH = 0.00e0D; // 1.0e0D 0.2e0D 0.0e0D
    private static double RWD_OG_CST = 1.00e0D; // 1.0e0D 0.8e0D 0.0e0D

    // 投資回収結果の重み係数
    private static double RWD_FN_BIZ = 1.0e-3D; // 投資回収利益は桁が大き過ぎるので


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

            double rrcw = 1.0e0 - rcw;

            // スケジュール・コストは計画通りが最良
            reward = (Math.min(rsd, 1.0e0D / rsd) - 1.0e0D) * RWD_FN_SCH
                    + (Math.min(rco, 1.0e0D / rco) - 1.0e0D) * RWD_FN_CST
 //                   - rcw * RWD_FN_CPW;
//                      - Math.abs(rcw) * RWD_FN_CPW;
            + (Math.min(rrcw, 1.0e0D / rrcw) - 1.0e0D) * RWD_FN_CPW;


            // 投資回収性の評価
//            double sd = state.getScheduleDelay();
//            double co = state.getCostOverrun();
//
//            ServiceModel sm = new ServiceModel();
//            double bizRes = sm.perform(sd, co, rcw);
//
//            reward = bizRes * RWD_FN_BIZ;

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
