package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 * 超簡易版プロジェクト挙動モデル.
 *
 * @author K.Okada
 */
class ProjectModel {

	/**
	 * シミュレーション時刻.
	 */
	private int simTime;

	/**
	 * プロジェクト完了フラグ.
	 */
	private boolean completionFlag;

	// 理想モデル
	private double idealRemainingWorks;
	private double idealCompleteWorks;
	private double idealEfforts;
	private int idealLastTime;			// 進行中を反映し、完了１回前で停止している点に注意！
	private int startOfTestPhase;

	// 手戻りモデル
	private double remainingWorks;
	private double completeWorks;
	private double efforts;
	private double latentReworks;
	private double totalEfforts;

	private double completeThreshold = 2.0e0d;

	// プロジェクト状態
	private double idealProgressRate;
	private double progressRate;
	private double pv;
	private double ev;
	private double ac;

	// PM行動の影響を受ける可変パラメータ
	private double efficiency;
	private double defectInjectionRate;
	private double defectDetectionRate;

	// PM行動の累積
	private int accumlatedApplyingPressure;
	private int accumlatedIncreasingEfforts;

	/**
	 *  コンストラクタ.
	 */
	ProjectModel() {
		super();

		this.simTime = 0;

		this.completionFlag = false;

		// 理想モデルの設定
		this.idealRemainingWorks = 1000.0e0D;
		this.idealCompleteWorks = 0.0e0D;
		this.idealEfforts = 20.0e0D;
		this.startOfTestPhase = (int) (this.idealRemainingWorks
				/ this.idealEfforts * 0.6e0D);

		// 現実モデルの設定
		this.remainingWorks = this.idealRemainingWorks;
		this.completeWorks = this.idealCompleteWorks;
		//		this.efforts = this.idealEfforts;
		this.latentReworks = 0.0e0D;

		return;
	}

	/**
	 *
	 * プロジェクトマネジメント行動を実行する.
	 *
	 * @param action プロジェクトマネジメント行動
	 *
	 */
	void perform(final ProjectManagementAction action) {

		simTime++;
		this.accumlatedApplyingPressure += action.getApplyingPressure();
		this.accumlatedIncreasingEfforts += action.getIncreasingEffort();

		// 行動によるパラメータ変更
		switch (action.getApplyingPressure()) {
		// プレッシャーを掛けるほど
		// ・効率は高い (手抜き)
		// ・高欠陥混入率は高い
		// ・欠陥発見率は低い

		case -1:
			this.efficiency = 0.90e0D;
			this.defectInjectionRate = 0.15e0D;
			this.defectDetectionRate = 0.35e0D;
			break;
		case 0:
			this.efficiency = 0.95e0D;
			this.defectInjectionRate = 0.20e0D;
			this.defectDetectionRate = 0.30e0D;
			break;
		case 1:
			this.efficiency = 1.00e0D;
			this.defectInjectionRate = 0.25e0D;
			this.defectDetectionRate = 0.25e0D;
			break;
		case 2:
			this.efficiency = 1.05e0D;
			this.defectInjectionRate = 0.30e0D;
			this.defectDetectionRate = 0.20e0D;
			break;
		default:
			System.out.println("Illegal PM operation.");
		}

		switch (action.getIncreasingEffort()) {
		// 人員工数を増やすほど
		// ・工数は大きい
		// ・欠陥混入率は微増
		case -1:
			this.efforts = this.idealEfforts * 0.90e0D;
			this.defectInjectionRate += 0.00e0D;
			break;
		case 0:
			this.efforts = this.idealEfforts * 1.00e0D;
			this.defectInjectionRate += 0.00e0D;
			break;
		case 1:
			this.efforts = this.idealEfforts * 1.10e0D;
			this.defectInjectionRate += 0.01e0D;
			break;
		case 2:
			this.efforts = this.idealEfforts * 1.20e0D;
			this.defectInjectionRate += 0.02e0D;
			break;
		default:
			System.out.println("Illegal PM operation.");
		}

		// 理想モデルの状態変化
		if (this.idealRemainingWorks >= this.idealEfforts) {
			this.idealCompleteWorks += this.idealEfforts;
			this.idealRemainingWorks -= this.idealEfforts;
			this.idealLastTime = this.simTime;
		} else {
			this.idealCompleteWorks += this.idealRemainingWorks;
			this.idealRemainingWorks = 0.0e0D;
		}

		// 現実モデルの状態変化
		double effectiveEfforts = this.efforts * this.efficiency;

		// 作業の実施
		if (this.simTime <= startOfTestPhase) {
			// 制作フェーズの場合の処理

			// 作業実施に伴う作業量の移動
			this.totalEfforts += this.efforts;
			this.completeWorks += effectiveEfforts;
			this.remainingWorks -= effectiveEfforts;

			// 欠陥の混入
			this.latentReworks += effectiveEfforts * this.defectInjectionRate;

		} else {
			// テストフェーズの場合の処理

			// 追加テストの決定
			if ((this.remainingWorks < effectiveEfforts)
					&& (this.latentReworks > this.completeThreshold)) {
				this.remainingWorks += this.efforts;
			}

			if (this.remainingWorks >= effectiveEfforts) {
				// 残テスト作業量が多い場合の処理
				this.totalEfforts += this.efforts;
				this.completeWorks += effectiveEfforts;
				this.remainingWorks -= effectiveEfforts;

				// 潜在欠陥量に応じて欠陥検出量を算出
				double detectedReworks = Math.min(effectiveEfforts
						* defectDetectionRate, this.latentReworks);

				// 検出された欠陥作業量を残作業に追加
				this.latentReworks -= detectedReworks;
				this.remainingWorks += detectedReworks;

			} else {
				// 残テスト作業量が少ない場合の処理

				// 終結処理
				this.totalEfforts += this.remainingWorks;
				this.completeWorks += this.remainingWorks;
				this.remainingWorks = 0.0e0d;

				this.completionFlag = true;
			}
		}

		// 進捗率の算出
		this.idealProgressRate = this.idealCompleteWorks
				/ (this.idealRemainingWorks + this.idealCompleteWorks);
		this.progressRate = this.completeWorks / (this.remainingWorks
				+ this.completeWorks);
		this.pv = this.idealCompleteWorks;
		this.ev = (this.idealRemainingWorks + this.idealCompleteWorks)
				* this.progressRate;
		this.ac = this.totalEfforts;

		return;

	}

	/**
	 * プロジェクト状態を返す.
	 * Q-Net学習への拡張を考慮し、状態量は離散化せず連続値のまま返す．
	 *
	 * @return プロジェクト状態.
	 */
	ProjectState observe() {

		// 状態の器を生成する
		ProjectState state = new ProjectState();

		// シミュレーション時刻を設定する
		state.setSimTime(this.simTime);

		// プロジェクト進捗率と完了フラグを設定する
		state.setProgressRate(this.progressRate, this.completionFlag);

		// スケジュール遅延量と遅延率を設定する
		state.setScheduleDelay((double) (this.simTime - (this.idealLastTime
				+ 1)), (double) this.simTime / (double) (this.idealLastTime
						+ 1));

		// コスト超過量と超過率を設定する
		state.setCostOverrun(this.ac - this.pv, this.ac / this.pv);

		// EVM指標を設定する
		state.setEVM(this.pv, this.ev, this.ac);

		// 内部状態を使用しない場合用に初期化する
		state.setAverageAPIE(0.0e0D, 0.0e0D);

		// 内部状態量を使用する場合
		state.setAverageAPIE((double) this.accumlatedApplyingPressure
				/ (double) this.simTime,
				(double) this.accumlatedIncreasingEfforts
						/ (double) this.simTime);

		return state;
	}
}
