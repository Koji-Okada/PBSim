package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 * 
 * @author K.Okada
 *
 */
public class QAction {

	double qValue;
	ProjectManagementAction action;
	
	public QAction (double q, int a0, int a1, int a2) {
	
		action = new ProjectManagementAction(a0, a1, a2);
		qValue = q;
		
		return;
	}
}
