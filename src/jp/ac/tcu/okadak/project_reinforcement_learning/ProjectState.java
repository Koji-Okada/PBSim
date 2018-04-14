package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 *
 * プロジェクト状態.
 *
 * @author K.Okada
 *
 */
class ProjectState {

	boolean completionFlag;
	int simTime;
	double delay;
	double costOverrun;

	double progressRate;
	double spi;
	double cpi;

	double pv;
	double ev;
	double ac;

	double averageApplyingPressure;
	double averageIncreasingEfforts;
}
