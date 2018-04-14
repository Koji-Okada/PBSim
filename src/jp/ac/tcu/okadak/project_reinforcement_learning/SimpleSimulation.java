package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 *
 * 学習無しのシミュレーション.
 *
 * @author K.Okada
 *
 */
public final class SimpleSimulation {

	/**
	 * コンストラクタ. (プライベート化)
	 */
	private SimpleSimulation() {
		super();
		return;
	}

	/**
	 * メインルーチン.
	 *
	 * @param args デフォルトの引数指定
	 */
	public static void main(final String[] args) {

		System.out.println("Start ...");

		ProjectState postState;

		for (int applyingPressure = ProjectManagementAction.MIN_ACTION_AP; applyingPressure < ProjectManagementAction.MAX_ACTION_AP; applyingPressure++) {
			for (int increasingEffort = ProjectManagementAction.MIN_ACTION_IE; increasingEffort < ProjectManagementAction.MAX_ACTION_IE; increasingEffort++) {
				ProjectModel project = new ProjectModel();
				do {
					ProjectManagementAction action = new ProjectManagementAction(
							applyingPressure, increasingEffort);

					project.perform(action);

					// 行動後の状態を観測する
					postState = project.observe();

				} while (!postState.isComplete());

				System.out.println(applyingPressure + "\t" + increasingEffort
						+ "\t" + postState.getScheduleDelay() + "\t"
						+ postState.getCostOverrun());
			}
		}
		System.out.println("... Fin.");
	}
}
