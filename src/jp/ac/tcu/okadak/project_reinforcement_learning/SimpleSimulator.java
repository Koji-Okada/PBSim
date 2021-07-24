package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 *
 * 学習無しのシミュレーション.
 *
 * @author K.Okada T.Hayashi
 *
 */
public final class SimpleSimulator {

    /**
     * コンストラクタ. (プライベート化)
     */
    private SimpleSimulator() {
        super();
        return;
    }

    /**
     * メインルーチン.
     *
     * @param args
     *            デフォルトの引数指定
     */
    public static void main(final String[] args) {

        System.out.println("Start ...");

        ProjectState postState;
        for (int applyingPressure = ProjectManagementAction.MIN_ACTION_AP; applyingPressure < ProjectManagementAction.MAX_ACTION_AP; applyingPressure++) {
            for (int increasingEffort = ProjectManagementAction.MIN_ACTION_IE; increasingEffort < ProjectManagementAction.MAX_ACTION_IE; increasingEffort++) {
//             for (int scopeAdjust = ProjectManagementAction.MIN_ACTION_SA; scopeAdjust < ProjectManagementAction.MAX_ACTION_SA; scopeAdjust++) {
                {
//                    int scopeAdjust = -1;
                    int scopeAdjust = 0;


                    // 基準プロジェクトを生成する
                    ProjectModel project = new ProjectModel(1000.0e0, 20.0e0,
                            1.0e0, 1.0e0);
                    do {
                        ProjectManagementAction action = new ProjectManagementAction(
                                applyingPressure, increasingEffort,
                                scopeAdjust);
                        project.perform(action);
                        // 行動後の状態を観測する
                        postState = project.observe();

                    } while (!postState.isComplete());

                    double scheduleDelay = postState.getScheduleDelay();
                    double costOverrun = postState.getCostOverrun();
                    double scopeChangeRate = postState.getScopeChangeRate();

                    ServiceModel sModel0 = new ServiceModel(100.0e0D, 1.0e0D);
                    ServiceModel sModel1 = new ServiceModel(50.0e0D, 1.0e0D);
                    ServiceModel sModel2 = new ServiceModel(100.0e0D, 1.5e0D);

                    double bizRes0 = sModel1.perform(scheduleDelay, costOverrun, scopeChangeRate);
                    double bizRes1 = sModel1.perform(scheduleDelay, costOverrun, scopeChangeRate);
                    double bizRes2 = sModel2.perform(scheduleDelay, costOverrun, scopeChangeRate);


                    System.out.println("-終了結果-" + "\t" + applyingPressure + "\t"
                            + increasingEffort + "\t" + scopeAdjust + "\t"
                            + scheduleDelay + "\t" + costOverrun + "\t" + scopeChangeRate
                            + "\t" + (bizRes0 / 1.0e6D) + "\t" + (bizRes1 / 1.0e6D) + "\t" + (bizRes2 / 1.0e6D));
                }
            }
        }
        System.out.println("... Fin.");

        // 理想ケースとの一致をテストする
        // 基準プロジェクトを生成する
        ProjectModel project = new ProjectModel(1000.0e0, 20.0e0, 1.0e0, 1.0e0);
        do {
            ProjectManagementAction action = new ProjectManagementAction(99, 99,
                    99);

            project.perform(action);

            // 行動後の状態を観測する
            postState = project.observe();

        } while (!postState.isComplete());

         System.out.printf("理想モデルとの一致\t");
         System.out.printf("%6.2f\t", postState.getScheduleDelay());
         System.out.printf("%6.2f\t", postState.getCostOverrun());
         System.out.printf("%6.2f\t", postState.getScheduleDelayRate());
         System.out.printf("%6.2f\t", postState.getCostOverrunRate());
         System.out.println();

        System.out.println("... Fin.");
    }
}
