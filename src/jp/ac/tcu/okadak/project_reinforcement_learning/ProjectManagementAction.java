package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 *
 * プロジェクトマネジメント行動.
 *
 * @author K.Okada
 */
public class ProjectManagementAction {

	/**
	 * 計画遵守圧力指示.
	 */
	private int applyingPressure;

	/**
	 * 計画遵守圧力指示の最大値.
	 */
	static final int MAX_ACTION_AP = 3;

	/**
	 * 計画遵守圧力指示の最小値.
	 */
	static final int MIN_ACTION_AP = -1;

	/**
	 * 計画遵守圧力指示の値を取得する.
	 *
	 * @return 計画遵守圧力指示の値
	 */
	final int getApplyingPressure() {
		return this.applyingPressure;
	}

	/**
	 * 工数増加指示.
	 */
	private int increasingEffort;

	/**
	 * 工数増加指示の最大値.
	 */
	static final int MAX_ACTION_IE = 3;

	/**
	 * 工数増加指示の最小値.
	 */
	static final int MIN_ACTION_IE = -1;

	/**
	 * 工数増加指示の値を取得する.
	 *
	 * @return 工数増加指示の値
	 */
	final int getIncreasingEffort() {
		return this.increasingEffort;
	}

	/**
	 * コンストラクタ.
	 *
	 * @param aPressure 計画遵守圧力指示(-1,0,1,2)
	 * @param iEfforts 工数増加指示(-1,0,1,2)
	 */
	public ProjectManagementAction(final int aPressure,
			final int iEfforts) {
		super();

		this.applyingPressure = aPressure;
		this.increasingEffort = iEfforts;

		return;
	}
}
