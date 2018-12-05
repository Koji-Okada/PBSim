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

				// 基準プロジェクトを生成する
				ProjectModel project = new ProjectModel(1000.0e0, 20.0e0, 1.0e0,
						1.0e0);
				do {
					ProjectManagementAction action = new ProjectManagementAction(
							applyingPressure, increasingEffort);

					project.perform(action);

					// 行動後の状態を観測する
					postState = project.observe();

				} while (!postState.isComplete());

				System.out.println(applyingPressure + "\t" + increasingEffort
						+ "\t" + postState.getScheduleDelay() + "\t"
						+ postState.getCostOverrun() + "\t"
						+ postState.getAverageAP() + "\t"
						+ postState.getAverageIE());
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

		System.out.println(99 + "\t" + 99 + "\t"
				+ postState.getScheduleDelay() + "\t"
				+ postState.getCostOverrun() + "\t"
				+ postState.getAverageAP() + "\t"
				+ postState.getAverageIE());

		System.out.println("... Fin.");
	}
}
