package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 *
 * プロジェクト状態.
 *
 * @author K.Okada T.Hayashi
 */
class ProjectState {

	// =======================================================================
	/**
	 * シミュレーション時刻.
	 */
	private int simTime;

	/**
	 * シミュレーション時刻を取得する.
	 *
	 * @return シミュレーション時刻
	 */
	final int getSimTime() {
		return this.simTime;
	}

	/**
	 * シミュレーション時刻を設定する.
	 *
	 * @param simulationTime シミュレーション時刻
	 */
	final void setSimTime(final int simulationTime) {
		this.simTime = simulationTime;
		return;
	}

	// =======================================================================
	/**
	 * プロジェクト完了フラグ.
	 */
	private boolean completionFlag;

	/**
	 * プロジェクト完了かを取得する.
	 *
	 * @return プロジェクト完了か否か
	 */
	boolean isComplete() {
		return this.completionFlag;
	}

	// =======================================================================
	/**
	 * 進捗率.
	 */
	private double progressRate;

	/**
	 * 進捗率を取得する.
	 *
	 * @return 進捗率
	 */
	final double getProgressRate() {

		return this.progressRate;
	}

	/**
	 * 進捗率および完了フラグを設定する.
	 *
	 * @param progress 進捗率
	 * @param cmpFlag 完了フラグ
	 */
	final void setProgressRate(final double progress, final boolean cmpFlag) {
		this.progressRate = progress;
		this.completionFlag = cmpFlag;
		return;
	}

	// =======================================================================
	/**
	 * スケジュール遅延量.
	 */
	private double scheduleDelay;

	/**
	 * スケジュール遅延量を取得する.
	 *
	 * @return スケジュール遅延量
	 */
	final double getScheduleDelay() {
		return this.scheduleDelay;
	}

	/**
	 * スケジュール遅延量および遅延率を設定する.
	 *
	 * @param schDelay スケジュール遅延量
	 * @param schDelayRate スケジュール遅延率
	 */
	final void setScheduleDelay(final double schDelay,
			final double schDelayRate) {
		this.scheduleDelay = schDelay;
		this.scheduleDelayRate = schDelayRate;
		return;
	}

	// =======================================================================
	/**
	 * スケジュール遅延率.
	 */
	private double scheduleDelayRate;

	/**
	 * スケジュール遅延率を取得する.
	 *
	 * @return スケジュール遅延率
	 */
	final double getScheduleDelayRate() {
		return this.scheduleDelayRate;
	}

	// =======================================================================
	/**
	 * コスト超過量.
	 */
	private double costOverrun;

	/**
	 * コスト超過量を取得する.
	 *
	 * @return コスト超過量
	 */
	final double getCostOverrun() {
		return this.costOverrun;
	}

	/**
	 * コスト超過量および超過率を設定する.
	 *
	 * @param cstOverrun コスト超過量
	 * @param cstOverrunRate コスト超過率
	 */
	final void setCostOverrun(final double cstOverrun,
			final double cstOverrunRate) {
		this.costOverrun = cstOverrun;
		this.costOverrunRate = cstOverrunRate;
		return;
	}

	// =======================================================================
	/**
	 * コスト超過率.
	 */
	private double costOverrunRate;

	/**
	 * コスト超過率を取得する.
	 *
	 * @return コスト超過率
	 */
	final double getCostOverrunRate() {
		return this.costOverrunRate;
	}

	// =======================================================================
	/**
	 * Planned Value.
	 */
	private double pv;

	/**
	 * PVを取得する.
	 *
	 * @return PVの値
	 */
	final double getPV() {
		return this.pv;
	}

	// =======================================================================
	/**
	 * Earned Value.
	 */
	private double ev;

	/**
	 * EVを取得する.
	 *
	 * @return EVの値
	 */
	final double getEV() {
		return this.ev;
	}

	// =======================================================================
	/**
	 * Actual Cost.
	 */
	private double ac;

	/**
	 * ACを取得する.
	 *
	 * @return ACの値
	 */
	final double getAC() {
		return this.ac;
	}

	// =======================================================================
	/**
	 * EVMの値を設定する.
	 *
	 * @param evmPv EVMのPV
	 * @param evmEv EVMのEV
	 * @param evmAc EVMのAC
	 */
	final void setEVM(final double evmPv, final double evmEv,
			final double evmAc) {
		this.pv = evmPv;
		this.ev = evmEv;
		this.ac = evmAc;
		if (1.0e-6D < evmPv) {
			this.spi = evmEv / evmPv;
		} else {
			this.spi = 1.0e0D;
		}
		if (1.0e-6D < evmAc) {
			this.cpi = evmEv / evmAc;
		} else {
			this.cpi = 1.0e0D;
		}

		return;
	}

	/**
	 * スケジュール効率指数.
	 */
	private double spi;

	/**
	 * SPIの値を取得する.
	 *
	 * @return SPIの値
	 */
	final double getSPI() {
		return this.spi;
	}

	/**
	 * コスト効率指数.
	 */
	private double cpi;

	/**
	 * CPIの値を取得する.
	 *
	 * @return CPIの値
	 */
	final double getCPI() {
		return this.cpi;
	}

	// =======================================================================
	/**
	 * 計画遵守圧力指示の過去平均値.
	 */
	private double averageApplyingPressure;

	/**
	 * 計画遵守圧力指示の過去平均値を取得する.
	 *
	 * @return 計画遵守圧力指示の過去平均値
	 */
	final double getAverageAP() {
		return this.averageApplyingPressure;
	}

	// =======================================================================
	/**
	 * 工数増加指示の過去平均値.
	 */
	private double averageIncreasingEffort;

	/**
	 * 工数増加指示の過去平均値を取得する.
	 *
	 * @return 工数増加指示の過去平均値
	 */
	final double getAverageIE() {
		return this.averageIncreasingEffort;
	}

	// =======================================================================
	/**
	 * スコープ調整指示の過去平均値.
	 */
	private double averageScopeAdjust;

	/**
	 * スコープ調整指示の過去平均値を取得する.
	 *
	 * @return スコープ調整指示の過去平均値
	 */
	final double getAverageSA() {
		return this.averageScopeAdjust;
	}

	// =======================================================================
	/**
	 * 仕様妥協量.
	 */
	private double compromiseWorks;

	/**
	 * 仕様妥協量を取得する.
	 *
	 * @return スケジュール遅延量
	 */
	final double getCompromiseWorks() {
		return this.compromiseWorks;
	}

	/**
	 * 仕様妥協量および仕様妥協率を設定する.
	 *
	 * @param cpWorks 仕様妥協量
	 * @param cpWorksRate 仕様妥協率
	 */
	final void setCompromiseWorks(final double cpWorks,
			final double cpWorksRate) {
		this.compromiseWorks = cpWorks;
		this.compromiseWorksRate = cpWorksRate;
		return;
	}

	/**
	 * 仕様妥協率.
	 */
	private double compromiseWorksRate;

	/**
	 * 仕様妥協率を取得する.
	 *
	 * @return 仕様妥協率
	 */
	final double getCompromiseWorksRate() {
		return this.compromiseWorksRate;
	}

	// =======================================================================
	/**
	 * 指示の過去平均値を設定する.
	 *
	 * @param avgAP 計画遵守圧力の過去平均値
	 * @param avgIE 工数増加指示の過去平均値
	 * @param avgSA スコープ調整の過去平均値
	 */
	final void setAverageAPIESA(final double avgAP, final double avgIE, final double avgSA) {
		this.averageApplyingPressure = avgAP;
		this.averageIncreasingEffort = avgIE;
		this.averageIncreasingEffort = avgSA;
		// 内部歪み推定を行わない場合
//		this.averageApplyingPressure = 0.0e0D;
//		this.averageIncreasingEffort = 0.0e0D;
//		this.averageScopeAdjust = 0.0e0D;

		return;
	}
}
