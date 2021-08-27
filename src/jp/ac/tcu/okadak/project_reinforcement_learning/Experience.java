package jp.ac.tcu.okadak.project_reinforcement_learning;

public class Experience {

	private ProjectState preState;
	private ProjectManagementAction action;
	private ProjectState postState;
	private double reward;
		
	public Experience(ProjectState pre, ProjectManagementAction act, ProjectState post, double r) {
		
		preState = pre;
		action = act;
		postState = post;
		reward = r;

		return;
	}
	
	ProjectState getPreState() {
		return preState;
	}
	
	ProjectState getPostState() {
		return postState;
	}

	ProjectManagementAction getAction() {
		return action;
	}

	double getReward() {
		return reward;
	}
	
	
}
