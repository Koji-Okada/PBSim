package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 *
 * 学習無しのシミュレーション.
 *
 * @author K.Okada
 *
 */
public class SimpleSimulation {

	public static void main(String[] args) {

		System.out.println("Start ...");

		ProjectState postState;
		Boolean completionFlag;


		for (int applyingPressure = -1; applyingPressure < 3; applyingPressure++) {
//		{ int applyingPressure = 0;
		for (int increasingEfforts = -1; increasingEfforts < 3; increasingEfforts++) {
//			{ int increasingEfforts = 0;
				ProjectModel project = new ProjectModel();
				do {
					ProjectManagementAction action = new ProjectManagementAction(
							applyingPressure, increasingEfforts);

					project.perform(action);

					// 行動後の状態を観測する
					postState = project.observe();
					completionFlag = postState.completionFlag;

				} while (!completionFlag);

				System.out.println(applyingPressure + "\t" + increasingEfforts
						+ "\t" + postState.delay + "\t"
						+ postState.costOverrun);
			}
		}
		System.out.println("... Fin.");
	}
}
