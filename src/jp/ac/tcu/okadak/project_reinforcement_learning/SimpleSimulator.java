package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 *
 * 学習無しのシミュレーション.
 *
 * @author K.Okada
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

				 System.out.printf("%4d\t", applyingPressure);
				 System.out.printf("%4d\t", increasingEffort);

				// 基準プロジェクトを生成する
				ProjectModel project = new ProjectModel(1000.0e0, 20.0e0, 1.0e0,
						1.0e0);

				int i = 0;
				do {
					int ap = applyingPressure;
					int ie = increasingEffort;

					// 40回以上は、[0,0] に
//					if (40 <= i) {
//						ap = 0;
//						ie = 0;
//					}

					ProjectManagementAction action = new ProjectManagementAction(
							ap, ie);

					project.perform(action);

					// 行動後の状態を観測する
					postState = project.observe();

					System.out.printf("%6.2f\t", postState.getSPI());
					System.out.printf("%6.2f\t", postState.getCPI());
					System.out.printf("%6.2f\t", postState.getProgressRate());

					i++;

				} while (!postState.isComplete());
				System.out.println();

//				 System.out.printf("%4d\t", applyingPressure);
//				 System.out.printf("%4d\t", increasingEffort);
//				 System.out.printf("%6.2f\t", postState.getScheduleDelay());
//				 System.out.printf("%6.2f\t", postState.getCostOverrun());
//				 System.out.printf("%6.2f\t", postState.getScheduleDelayRate());
//				 System.out.printf("%6.2f\t", postState.getCostOverrunRate());
//				 System.out.println();
			}
		}

		// 理想ケースとの一致をテストする
		// 基準プロジェクトを生成する
		ProjectModel project = new ProjectModel(1000.0e0, 20.0e0, 1.0e0, 1.0e0);
		do {
			ProjectManagementAction action = new ProjectManagementAction(99,
					99);

			project.perform(action);

			// 行動後の状態を観測する
			postState = project.observe();

		} while (!postState.isComplete());

		// System.out.printf("%4d\t", 99);
		// System.out.printf("%4d\t", 99);
		// System.out.printf("%6.2f\t", postState.getScheduleDelay());
		// System.out.printf("%6.2f\t", postState.getCostOverrun());
		// System.out.printf("%6.2f\t", postState.getScheduleDelayRate());
		// System.out.printf("%6.2f\t", postState.getCostOverrunRate());
		// System.out.println();

		System.out.println("... Fin.");
	}
}
