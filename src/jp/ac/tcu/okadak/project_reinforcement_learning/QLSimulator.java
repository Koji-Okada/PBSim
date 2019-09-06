package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 * シミュレータ本体.
 *
 * @author K.Okada T.Hayashi
 */
public final class QLSimulator {

    /**
     * 全体の反復回数.
     */
    private static final int ITERATION_ALL = 100;

    /**
     * 探索学習モードでの反復回数.
     */
    private static final int ITERATION_WITH_EXPLORING = 8000;

    /**
     * 収束学習モードでの反復回数.
     */
    private static final int ITERATION_WITHOUT_EXPLORING = 2000;

    /**
     * 学習結果評価用の反復数.
     */
    private static final int LAST_EVALUATIONS = 1;

    /**
     *
     */
    private static final int PROJECT_SIZE = 1000;
    private static final int PROJECT_HR = 20;



    /**
     * 見積りの安全率.
     */
    //	private double safetyRate = 1.0e0d;

    /**
     * コンストラクタ. (プライベート化)
     */
    private QLSimulator() {
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
        for (int i = 0; i < 15; i++) {
            // エージェント15体で実行する
            QLSimulator simulator = new QLSimulator();
            simulator.qLearning(i);
        }
        System.out.println("... Fin.");

        return;
    }

    /**
     * 一連の Q学習を行う.
     */
    private void qLearning(int aID) {

        // 最良エージェント
        LearningAgent bestAgent = null;

        // 学習エージェントを生成する
        LearningAgent agent = new LearningAgent();
        agent.SetRandomSeed(aID); // 再現性確保のため乱数種を固定する

        // 報酬評価器を生成する
        RewardEvaluator evaluator = new RewardEvaluator();

        //		// プロジェクト属性生成器を生成する
        //		ProjectAttributesGenerator pjAtrGen = new ProjectAttributesGenerator();
        //		pjAtrGen.setRandomSeed(0);

        // 成果物規模の確率分布を設定する
        // pjAtrGen.setProductSizeDistibution(1000.0e0D * 5.0e0, 100.0e0D);
        //		pjAtrGen.setProductSizeDistibution(2843.371e0D, 4711.928e0D);
        //
        //		// 期間変動の確立分布を設定する
        //		// pjAtrGen.setDurationDistribution(0.0e0D, 0.10e0D);
        //		pjAtrGen.setDurationDistribution(0.0e0D, 0.45e0D);
        //
        //		// 工数見積係数を設定する
        //		pjAtrGen.setEffortEstimationParameter(safetyRate); // 1 + 手戻り工数率
        //
        //		// 理想的工数見積係数を設定する
        //		pjAtrGen.setIdealEffortEstimationParameter(1.0e0D);

        for (int j = 0; j < ITERATION_ALL; j++) {
            // 全体の反復ループ

            // 学習収束度パラメータを初期化する
            double sumLearningIndex1 = 0.0e0D;
            for (int i = 0; i < ITERATION_WITH_EXPLORING; i++) {
                // プロジェクト反復のループ (探索学習モード(ε-Greedyオン))

                // プロジェクトを生成する
                //				ProjectAttributes pjAtr = pjAtrGen
                //						.generateProjectAttribute(false);
                ProjectModel project = new ProjectModel(PROJECT_SIZE, PROJECT_HR, 1.0e0, 1.0e0);

                // プロジェクトを実施する
                sumLearningIndex1 += performProject(project, agent, evaluator,
                        true, true);
            }

            // 学習収束度パラメータを初期化する
            double sumLearningIndex2 = 0.0e0D;
            for (int i = 0; i < ITERATION_WITHOUT_EXPLORING; i++) {
                // プロジェクト反復のループ (収束学習モード(ε-Greedyオフ))

                // プロジェクトを生成する
                //				ProjectAttributes pjAtr = pjAtrGen
                //						.generateProjectAttribute(false);
                ProjectModel project = new ProjectModel(PROJECT_SIZE, PROJECT_HR, 1.0e0, 1.0e0);

                // プロジェクトを実施する
                sumLearningIndex2 += performProject(project, agent, evaluator,
                        false, true);
            }

            // 学習速度のコンソール出力
            // System.out
            // .printf("%8.4f\t", sumLearningIndex1
            // / (double) ITERATION_WITH_EXPLORING);
            // System.out.printf("%10.4f\t",
            // sumLearningIndex2 / (double) ITERATION_WITHOUT_EXPLORING);
            // System.out.println();

            double sumDelayRate = 0.0e0D;
            double sumCostOverrunRate = 0.0e0D;
            double sumReward = 0.0e0D;
            double sumDelayRate0 = 0.0e0D;
            double sumCostOverrunRate0 = 0.0e0D;
            double sumReward0 = 0.0e0D;
            double sumProductivity0 = 0.0e0D;

            // 学習結果の評価
            for (int i = 0; i < LAST_EVALUATIONS; i++) {

                //				// プロジェクト属性を生成する
                //				ProjectAttributes pjAtr = pjAtrGen
                //						.generateProjectAttribute(false);

                // プロジェクトを生成する
                ProjectModel project = new ProjectModel(PROJECT_SIZE, PROJECT_HR, 1.0e0, 1.0e0);

                // プロジェクトを実施する
                performProject(project, agent, evaluator, false, false);

                // 学習結果の評価を行う
                sumReward += evaluator.evaluate(project.observe());
                sumDelayRate += project.observe().getScheduleDelayRate();
                sumCostOverrunRate += project.observe().getCostOverrunRate();

                // 最良学習エージェントにもプロジェクトを実行させる
                if (null == bestAgent) {
                    bestAgent = agent.agentClone();
                }

                // プロジェクトを生成する(プロジェクト属性は同一)
                ProjectModel project0 = new ProjectModel(PROJECT_SIZE, PROJECT_HR, 1.0e0, 1.0e0);

                // プロジェクトを実施する
                performProject(project0, bestAgent, evaluator, false, false);

                // 学習結果の評価を行う
                sumReward0 += evaluator.evaluate(project0.observe());
                sumDelayRate0 += project0.observe().getScheduleDelayRate();
                sumCostOverrunRate0 += project0.observe().getCostOverrunRate();
            }

            // 評価結果のコンソール出力============================================================
            //			System.out.print("\t");
            //			System.out.printf("%10.4f\t",
            //					sumDelayRate / (double) LAST_EVALUATIONS);
            //			System.out.printf("%10.4f\t",
            //					sumCostOverrunRate / (double) LAST_EVALUATIONS);
            //			System.out.printf("%10.4f\t",
            //					sumReward / (double) LAST_EVALUATIONS);

            // 最良学習エージェントと結果を比較し淘汰する
            if (sumReward0 < sumReward) {
                // 現行エージェントの方が評価が高い場合
                // 最良学習エージェントを置換する
                bestAgent = agent.agentClone();
                //				System.out.print("*\t");
            } else {
                // 現行エージェントの方が評価が低い場合
                // 最良学習エージェントの結果に置換する
                sumReward = sumReward0;
                sumDelayRate = sumDelayRate0;
                sumCostOverrunRate = sumCostOverrunRate0;
                //				System.out.print("\t");
            }

            // 評価結果のコンソール出力==============================================================
            //			System.out.printf("%10.4f\t",
            //					sumDelayRate / (double) LAST_EVALUATIONS);
            //			System.out.printf("%10.4f\t",
            //					sumCostOverrunRate / (double) LAST_EVALUATIONS);
            //			System.out.printf("%10.4f\t",
            //					sumReward / (double) LAST_EVALUATIONS);
            //			System.out.println();
        }

        for (int p = 0; p < 1; p++) {

            //			// プロジェクト属性生成器を、再度、生成する
            //			pjAtrGen = new ProjectAttributesGenerator();
            //			pjAtrGen.setRandomSeed(p); // 再現性確保のため
            //
            //			// 成果物規模の確率分布を設定する
            //			// pjAtrGen.setProductSizeDistibution(1000.0e0D * 5.0e0, 100.0e0D);
            //			pjAtrGen.setProductSizeDistibution(2843.371e0D, 4711.928e0D);
            //
            //			// 期間変動の確立分布を設定する
            //			// pjAtrGen.setDurationDistribution(0.0e0D, 0.10e0D);
            //			pjAtrGen.setDurationDistribution(0.0e0D, 0.45e0D);
            //
            //			// 工数見積係数を設定する
            //			pjAtrGen.setEffortEstimationParameter(safetyRate); // 1 + 手戻り工数率
            //
            //			// 理想的工数見積係数を設定する
            //			pjAtrGen.setIdealEffortEstimationParameter(1.0e0D);
            //
            //
            //			// プロジェクトを生成する
            //			ProjectAttributes pjAtr = pjAtrGen.generateProjectAttribute(false);
            //★ProjectModel project = new ProjectModel(pjAtr);
            ProjectModel project = new ProjectModel(PROJECT_SIZE, PROJECT_HR, 1.0e0, 1.0e0);
            //			double idealCost = pjAtr.getIdealTotalEffort() / 5.0e0d;
            //			double plannedCost = pjAtr.getEstimatedTotalEffort() / 5.0e0d;

            // プロジェクトを実施する
            // bestAgent.setRecordAction(true);
            //			System.out.println();
            //			System.out.println("---- Project " + p);

            performProject(project, bestAgent, evaluator, false, false);
            // System.out.println();

            // プロジェクト状態を表示する
            ProjectState st = project.observe();
            showProjectState(st);
        }

        return;
    }

    /**
     * プロジェクトを実行する.
     *
     * @param project
     *            プロジェクト
     * @param agent
     *            学習エージェント
     * @param evaluator
     *            報酬評価器
     * @param exploring
     *            探索学習モード
     * @param learning
     *            学習させる
     *
     * @return 学習収束度パラメータ
     */
    private double performProject(final ProjectModel project,
            final LearningAgent agent, final RewardEvaluator evaluator,
            final Boolean exploring, final Boolean learning) {

        double learningIndex = 0.0e0D;

        ProjectState postState;
        do {

            // 行動前の状態を観測する
            ProjectState preState = project.observe();

            // エージェントに行動を決定させる
            ProjectManagementAction action = agent.decideAction(preState,
                    exploring);

            // 環境に対して行動を行う
            project.perform(action);

            // 行動後の状態を観測する
            postState = project.observe();

            // 報酬を決定する
            double reward = evaluator.evaluate(postState);

            if (learning) {
                // エージェントに学習させる
                learningIndex += agent.learn(preState, action, reward,
                        postState);
            }

        } while (!postState.isComplete());

        // ※ここで action, postState, reward を使ってログを作成 ...

        return learningIndex / (double) postState.getSimTime();
    }

    /**
     * プロジェクト状態を表示する.
     *
     */
    private void showProjectState(final ProjectState st) {

        double performedDuration = (double) st.getSimTime();
        double performedCost = st.getAC();
        double scheduleDelay = st.getScheduleDelay();
        double scheduleDelayRate = st.getScheduleDelayRate();
        double costOverrun = st.getCostOverrun();
        double costOverrunRate = st.getCostOverrunRate();
        double plannedDuration = performedDuration - scheduleDelay;
        double compromiseWorks = st.getCompromiseWorks();
        double scopeChangeRate = st.getScopeChangeRate();

        //          System.out.printf("pl_duration = \t%4.0f\t", plannedDuration);
        //          System.out.println();
        //          System.out.printf("pf_duration = \t%4.0f", performedDuration);
        //          System.out.println();
        //          System.out.printf("id_cost     = \t%8.3f", idealCost);
        //          System.out.println();
        //          System.out.printf("pl_cost     = \t%8.3f", plannedCost);
        //          System.out.println();
        //          System.out.printf("pf_cost     = \t%8.3f", performedCost);
        //          System.out.println();
        System.out.printf("schedule deley = \t%4.0f\t", scheduleDelay);
        //          System.out.println();
        //          System.out.printf("schedule deley rate = \t%8.3f", scheduleDelayRate );
        //          System.out.println();
        System.out.printf("cost overrun   = \t%8.3f\t", costOverrun);
        //          System.out.println();
        //          System.out.printf("cost overrun rate = \t%8.3f\t",costOverrunRate );
        //          System.out.println();
        //仕様妥協量と仕様妥協率
        //          System.out.printf("compromiseWorks   = \t%8.3f", compromiseWorks);
        //          System.out.println();
        System.out.printf("compromiseWorks rate = \t%8.3f\t%8.3f", scopeChangeRate, 1.0e0D - scopeChangeRate);


        //          System.out.printf("pv = \t%8.3f", st.getPV());
        //          System.out.println();
        //          System.out.printf("ev = \t%8.3f", st.getEV());
        //          System.out.println();
        //          System.out.printf("ac = \t%8.3f", st.getAC());
        //          System.out.println();

        ServiceModel sm0 = new ServiceModel(100.0e0D, 1.0e0D);
        ServiceModel sm1 = new ServiceModel(50.0e0D, 1.0e0D);
        ServiceModel sm2 = new ServiceModel(100.0e0D, 1.2e0D);

        double bizRes0 = sm0.perform(scheduleDelay, costOverrun, scopeChangeRate);
        double bizRes1 = sm1.perform(scheduleDelay, costOverrun, scopeChangeRate);
        double bizRes2 = sm2.perform(scheduleDelay, costOverrun, scopeChangeRate);

        System.out.printf("\tBiz Result = \t%8.3f", bizRes0 / 1.0e6D);
        System.out.printf("\t%8.3f", bizRes1 / 1.0e6D);
        System.out.printf("\t%8.3f", bizRes2 / 1.0e6D);
        System.out.println();

        return;
    }
}
