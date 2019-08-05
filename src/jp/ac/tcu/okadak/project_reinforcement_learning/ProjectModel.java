package jp.ac.tcu.okadak.project_reinforcement_learning;

//import jp.ac.tcu.okadak.project_attributes_generator.ProjectAttributes;

/**
 * 超簡易版プロジェクト挙動モデル.
 *
 * @author K.Okada T.Hayashi
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

	// 成果物規模
	private double productSize;

	// 理想モデル
	private double idealTotalWork;
	private double idealRemainingWork;
	private double idealCompleteWork;
	private double effortInUST;
	private int idealLastTime;
	private double startOfTestPhase;
	private double idealTotalEfforts;

	// 手戻りモデル
	private double remainingWorks;
	private double completeWorks;
	private double efforts;
	private double latentReworks;
	private double totalEffort;
	private double compromiseWork;

	private double completeThreshold = 2.0e0D;

	// プロジェクト状態
	private double idealProgressRate;
	private double progressRate = 0.0e0D;
	private double pv;
	private double ev;
	private double ac;

	// PM行動の影響を受ける可変パラメータ
	private double efficiency;
	private double defectInjectionRate;
	private double defectDetectionRate;
    //スコープ調整の可変パラメータ
	private double scopeAdjustRate;

	// PM行動の累積
	private int accumlatedApplyingPressure = 0;
	private int accumlatedIncreasingEfforts = 0;
	private int accumlatedScopeAdjust = 0;


	/**
	 * コンストラクタ (直接指定).
	 *
	 * @param size
	 * @param hr
	 * @param est
	 * @param idealEst
	 */
	ProjectModel(final double size, final double hr, final double est,
			final double ideal) {
		super();

		this.simTime = 0;
		this.completionFlag = false;

		// 成果物規模を設定する
		this.productSize = size;

		// 理想モデルを設定する
		this.idealTotalWork = size * est;
		this.idealRemainingWork = this.productSize * est;
		this.idealCompleteWork = 0.0e0D;
		this.effortInUST = hr;
		this.startOfTestPhase = (int) (this.idealRemainingWork
				/ this.effortInUST * 0.6e0D);
		this. idealTotalEfforts= 0.0e0d;

		this.idealLastTime = (int) Math
				.ceil(this.idealRemainingWork / this.effortInUST);

		// 現実モデルを設定する
		this.remainingWorks = this.productSize * ideal;
		this.completeWorks = 0.0e0D;
		this.latentReworks = 0.0e0D;

		return;
	}

	/**
	 *
	 * プロジェクトマネジメント行動を実行する.
	 *
	 * @param action
	 *            プロジェクトマネジメント行動
	 *
	 */
	void perform(final ProjectManagementAction action) {

		simTime++;
		this.accumlatedApplyingPressure += action.getApplyingPressure();
		this.accumlatedIncreasingEfforts += action.getIncreasingEffort();
		this.accumlatedScopeAdjust += action.getScopeAdjust();

		// 行動によるパラメータ変更
		switch (action.getApplyingPressure()) {
			// プレッシャーを掛けるほど
			// ・効率は高い (手抜き)
			// ・高欠陥混入率は高い
			// ・欠陥発見率は低い

			case -1 :
				this.efficiency = 0.90e0D;
				this.defectInjectionRate = 0.15e0D;
				this.defectDetectionRate = 0.35e0D;
				break;
			case 0 :
				this.efficiency = 0.95e0D;
				this.defectInjectionRate = 0.20e0D;
				this.defectDetectionRate = 0.30e0D;
				break;
			case 1 :
				this.efficiency = 1.00e0D;
				this.defectInjectionRate = 0.25e0D;
				this.defectDetectionRate = 0.25e0D;
				break;
			case 2 :
				this.efficiency = 1.05e0D;
				this.defectInjectionRate = 0.30e0D;
				this.defectDetectionRate = 0.20e0D;
				break;
			case 99 :	// 理想モデルとの一致確認用
				this.efficiency = 1.00e0D;
				this.defectInjectionRate = 0.00e0D;
				this.defectDetectionRate = 0.00e0D;
				break;
			default :
				System.out.println("Illegal PM operation.");
		}

		switch (action.getIncreasingEffort()) {
			// 人員工数を増やすほど
			// ・工数は大きい
			// ・欠陥混入率は微増
			case -1 :
				this.efforts = this.effortInUST * 0.90e0D;
				this.defectInjectionRate += 0.00e0D;
				break;
			case 0 :
				this.efforts = this.effortInUST * 1.00e0D;
				this.defectInjectionRate += 0.00e0D;
				break;
			case 1 :
				this.efforts = this.effortInUST * 1.10e0D;
				this.defectInjectionRate += 0.01e0D;
				break;
			case 2 :
				this.efforts = this.effortInUST * 1.20e0D;
				this.defectInjectionRate += 0.02e0D;
				break;
			case 99 :	// 理想モデルとの一致確認用
				this.efforts = this.effortInUST * 1.00e0D;
				this.defectInjectionRate += 0.00e0D;
				break;
			default :
				System.out.println("Illegal PM operation.");
		}

		switch (action.getScopeAdjust()) {
			//スコープ調整を行うと製品仕様目標の引き上げ下げを行う
			case -1 :
				//削減なし
				this. scopeAdjustRate = 0.000e0D;
				break;
			case 0 :
				//0.1%削減
				this. scopeAdjustRate = 0.001e0D;
				break;
			case 1 :
				//0.2%削減
				this. scopeAdjustRate = 0.002e0D;
				break;
			case 2 :
				//0.3%削減
				this. scopeAdjustRate = 0.003e0D;
				break;
			case 99 :	// 理想モデルとの一致確認用
				this.scopeAdjustRate = 0.00e0D;
				break;
			default :
				System.out.println("Illegal PM operation.");
		}

//		★スコープ調整	上限設定	if(this.compromiseWork / this.idealTotalWork )
//		★if (this.compromiseWork / this.idealTotalWork <= 0.3e0D)
		{
			//理想のスコープ調整
			this.idealRemainingWork = this.idealRemainingWork * (1.0e0D - scopeAdjustRate);
			this.idealCompleteWork = this.idealCompleteWork * (1.0e0D - scopeAdjustRate);
			//スコープ調整
			this.compromiseWork += this.remainingWorks * scopeAdjustRate;
			this.remainingWorks = this.remainingWorks * (1.0e0D - scopeAdjustRate);
//			this.completeWorks = this.completeWorks * (1.0e0D - scopeAdjustRate);

			}

//		 理想モデルの状態変化
		if (this.idealRemainingWork >= this.effortInUST) {
			this.idealCompleteWork += this.effortInUST;
			this.idealTotalEfforts += this.effortInUST;
			this.idealRemainingWork -= this.effortInUST;
		} else {
			this.idealCompleteWork += this.idealRemainingWork;
			this.idealRemainingWork = 0.0e0D;
		}

		// 現実モデルの状態変化
		double effectiveEfforts = this.efforts * this.efficiency;

		// 作業の実施
		if (this.simTime <= startOfTestPhase) {
			// 制作フェーズの場合の処理

			// 作業実施に伴う作業量の移動
			// (制作フェーズ(前半)なので残作業量が負値になることはない)
			this.totalEffort += this.efforts;
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
				this.totalEffort += this.efforts;
				this.completeWorks += effectiveEfforts;
				this.remainingWorks -= effectiveEfforts;

				// 潜在欠陥量に応じて欠陥検出量を算出
				double detectedReworks = Math.min(
						effectiveEfforts * defectDetectionRate,
						this.latentReworks);

				// 検出された欠陥作業量を残作業に追加
				this.latentReworks -= detectedReworks;
				this.remainingWorks += detectedReworks;

			} else {
				// 残テスト作業量が少ない場合の処理
				// 終結処理
				this.totalEffort += this.remainingWorks;
				this.completeWorks += this.remainingWorks;
				this.remainingWorks = 0.0e0D;

				this.completionFlag = true;

			}

		}


		// 進捗率の算出
		this.idealProgressRate = this.idealCompleteWork
				/ (this.idealRemainingWork + this.idealCompleteWork);
		this.progressRate = this.completeWorks
				/ (this.remainingWorks + this.completeWorks);
		this.pv = this.idealTotalEfforts;
		this.ev = (this.idealRemainingWork + this.idealCompleteWork)
				* this.progressRate;
		this.ac = this.totalEffort;

		return;
	}

	/**
	 * プロジェクト状態を返す. Q-Net学習への拡張を考慮し、状態量は離散化せず連続値のまま返す．
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
		double sd = 0.0e0D;
		double sdr = 1.0e0D;
		if (0 != this.simTime) {
			sd = (double) ((this.simTime - 1)  - this.idealLastTime);
			sdr = (double) (this.simTime - 1) / (double) this.idealLastTime;
		}
		state.setScheduleDelay(sd, sdr);

		// コスト超過量と超過率を設定する
		double co = 0.0e0D;
		double cor = 1.0e0D;
		if (0 != this.simTime) {
			double base = Math.max(this.pv, this.ev);
			// PVよりもEVが先行する場合を考慮
			co = this.ac - base;
			cor = this.ac / base;
		}
		state.setCostOverrun(co, cor);


		//仕様妥協量と仕様妥協率　
		double cp = 0.0e0D;
		double cpr = 1.0e0D;
		if (0 != this.simTime) {
			cp  = (double)(this.compromiseWork);
			cpr = (double)this.compromiseWork / ((double)this.idealTotalWork);
		//新方式　
//		if (0 != this.simTime) {
//			cp  = (double)(this.compromiseWorks);
//			cpr = (double)this.compromiseWorks / 理想プロセスの総作業量);
		}
		state.setCompromiseWorks(cp, cpr);

		// EVM指標を設定する
		state.setEVM(this.pv, this.ev, this.ac);

		// 内部状態を使用しない場合用に初期化する
		double avAP = 0.0e0D;
		double avIE = 0.0e0D;
		double avSA = 0.0e0D;

		// 内部状態量を使用する場合
		if (0 != this.simTime) {
			avAP = (double) this.accumlatedApplyingPressure
					/ (double) this.simTime;
			avIE = (double) this.accumlatedIncreasingEfforts
					/ (double) this.simTime;
			avSA = (double) this.accumlatedScopeAdjust
					/ (double) this.simTime;
		}

		state.setAverageAPIESA(avAP, avIE, avSA);

		return state;
	}
}
