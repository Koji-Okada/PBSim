package jp.ac.tcu.okadak.project_reinforcement_learning;

public class UniSim {

    public static void main(final String[] args) {
        System.out.println("Start ...");

        ProjectModel project = new ProjectModel(1000.0e0, 20.0e0, 1.0e0, 1.0e0);
        ProjectModel0 project0 = new ProjectModel0(1000.0e0, 20.0e0, 1.0e0, 1.0e0);

        ProjectState postState;
        ProjectState postState0;

        int applyingPressure = 2;
        int increasingEffort = 2;

        do {
            ProjectManagementAction action = new ProjectManagementAction(
                    applyingPressure, increasingEffort, -1);
            project.perform(action);
            project0.perform(action);

            // 行動後の状態を観測する
            postState = project.observe();
            postState0 = project0.observe();

//            System.out.printf("%6.2f\t%6.2f\n", postState.getAC(), postState2.getAC());

        } while (!postState.isComplete() || !postState0.isComplete());

        System.out.println(" ... Fin");
    }
}
