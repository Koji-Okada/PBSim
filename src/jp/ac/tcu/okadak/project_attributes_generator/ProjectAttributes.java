package jp.ac.tcu.okadak.project_attributes_generator;

/**
 *
 * プロジェクト属性.
 *
 * @author K.Okada
 */
public class ProjectAttributes {

	/** =======================================================================
	 * プロジェクト事例名称.
	 */
	String caseName;


	/** =======================================================================
	 * (本来の)成果物規模.
	 */
	double productSize;

	/**
	 * 本来の成果物規模を返す.
	 *
	 * @return (本来の)成果物規模
	 */
	public double getProductSize() {
		return this.productSize;
	}

	/** =======================================================================
	 * 見積成果物規模.
	 */
	double estimatedProductSize;

	/** =======================================================================
	 * 理想総工数.
	 * 　見逃された要件も含む
	 * 　手戻り作業は含まない
	 * 　レビュー・コミュニケーション等含む
	 */
	double idealTotalEffort;

	/** =======================================================================
	 * 理想認識総工数.
	 */
	double idealRecognizedTotalEffort;

	/**
	 * 理想認識総工数を返す.
	 * 　見逃された要件も含まない
	 * 　手戻り作業は含まない
	 * 　レビュー・コミュニケーション等含む
	 *
	 * @return	理想総工数
	 */
	public double getIdealTotalEffort() {
		return this.idealTotalEffort;
	}

	/** =======================================================================
	 * 見積総工数.
	 */
	double plannedTotalEffort;

	/**
	 * 見積総工数を返す.
	 *
	 * @return	見積総工数
	 */
	public final double getEstimatedTotalEffort() {
		return this.plannedTotalEffort;
	}

	/** =======================================================================
	 * 計画期間.
	 */
	double plannedTotalDuration;

	/** =======================================================================
	 * 計画人員数.
	 */
	double plannedNumberOfHumanResources;

	/**
	 * 計画人員数を返す.
	 *
	 * @return	計画人員数
	 */
	public double getPlannedNumberOfHumanResources() {
		return this.plannedNumberOfHumanResources;
	}

	/** =======================================================================
	 * フェーズ毎の計画工数.
	 * (Probes-SD 対応)
	 */
	double[] plannedEffortInPhase = new double[ProbesSD.NUM_OF_PHASES];

	/** =======================================================================
	 * フェーズ毎の計画期間.
	 * (Probes-SD 対応)
	 */
	double[] plannedDurationInPhase = new double[ProbesSD.NUM_OF_PHASES];

	/** =======================================================================
	 * フェーズ毎・職種毎の計画人員数.
	 * (Probes-SD 対応)
	 */
	double[][] plannedNumberOfProfessionInPhase = new double[ProbesSD.NUM_OF_PHASES][ProbesSD.NUM_OF_PROFESSIONS];

	/** =======================================================================
	 * フェーズ毎の理想認識工数.
	 * (Probes-SD 対応)
	 *
	 * 　見逃された要件も含まない
	 * 　手戻り作業は含まない
	 * 　レビュー・コミュニケーション等含まない
	 *
	 */
	double[] idealRecognizedEffortInPhase = new double[ProbesSD.NUM_OF_PHASES];


	/** =======================================================================
	 * レビュー方針(レビュー作業工数比率)
	 * (Probes-SD 対応)
	 */
	double reviewPolicy;

	/** =======================================================================
	 * コミュニケーション方針(コミュニケーション作業工数比率)
	 * (Probes-SD 対応)
	 */
	double communicationPolicy;

	/** =======================================================================
	 * コミュニケーションエラー率
	 * (Probes-SD 対応)
	 */
	double communicationErrorRate;


	/** =======================================================================
	 * 人員の能力レベル
	 * (Probes-SD 対応)
	 */
	double[] capabilityLevel = new double[ProbesSD.NUM_OF_PROFESSIONS];

	/** =======================================================================
	 * 人員のモチベーションレベル
	 * (Probes-SD 対応)
	 */
	double[] motivationLevel = new double[ProbesSD.NUM_OF_PROFESSIONS];

}
