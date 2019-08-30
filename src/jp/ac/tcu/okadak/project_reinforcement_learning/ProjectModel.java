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

	// 成果物規模
	private double productSize;

	// 理想モデル
	private boolean idealCompletionFlag; // 完了フラグ
	private double idealInitialWork; // 初期作業量
	private double idealRemainingWork; // 残作業量
	private double idealCompleteWork; // 完了作業
	private double idealEffort; // 作業工数
	private double idealCompromisedWork; // 妥協作業量 (EV算出に必要)
	private int idealLastTime; // 計画完了日 (PVの有効範囲)

	// 手戻りモデル
	private boolean completionFlag; // 完了フラグ
	private double initialWork; // 初期作業量
	private double remainingWork; // 残作業量
	private double completeWork; // 完了作業
	private double effort; // 作業工数
	private double latentRework; // 潜在手戻り作業
	private double compromisedWork; // 妥協作業量
	private double totalEffort; // 総投入工数 (AC算出に必要)
	private boolean testFlag; // テスト開始フラグ

	private double completeThreshold = 2.0e0D; // 完了条件の閾値

	// プロジェクト状態
	private double idealProgressRate;
	private double progressRate;
	private double pv;
	private double ev;
	private double ac;

	// PM行動の影響を受ける可変パラメータ
	private double efficiency; // 表面的な効率向上
	private double defectInjectionRate; // 欠陥混入率
	private double defectDetectionRate; // 欠陥検出率
	private double scopeAdjustRate; // スコープ調整率

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
			final double idealEst) {
		super();

		this.simTime = 0;

		// 成果物規模を設定する
		this.productSize = size;

		// 理想モデルを設定する
		this.idealInitialWork = this.productSize * est;
		this.idealRemainingWork = this.idealInitialWork;
		this.idealCompleteWork = 0.0e0D;
		this.idealProgressRate = 0.0e0D;
		this.idealCompletionFlag = false;
		this.idealCompromisedWork = 0.0e0D;
		this.idealEffort = hr;

		this.idealLastTime = (int) Math
				.ceil(this.idealInitialWork / this.idealEffort);

		// 現実モデルを設定する
		this.initialWork = this.productSize * idealEst;
		this.remainingWork = this.initialWork;
		this.completeWork = 0.0e0D;
		this.latentRework = 0.0e0D;
		this.progressRate = 0.0e0D;
		this.completionFlag = false;
		this.compromisedWork = 0.0e0D;
		this.testFlag = false;

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

		// シミュレーション時刻を更新する
		simTime++;

		// プロジェクトマネジメント行動の累積値を求める
		this.accumlatedApplyingPressure += action.getApplyingPressure();
		this.accumlatedIncreasingEfforts += action.getIncreasingEffort();
		this.accumlatedScopeAdjust += action.getScopeAdjust();

		// プロジェクトマネジメント行動により可変パラメータを設定する
		setParameters(action);

		// スコープ調整量を算出する
		double idealScopeAdjust = this.idealInitialWork * this.scopeAdjustRate;
		double scopeAdjust = this.initialWork * this.scopeAdjustRate;

		// ===================================================================
		// スコープ調整に伴う理想モデルの状態変化
		// ※EV算出用に仮想的に使用妥協量を算出しているのみなので
		// idealRemainingWork に影響を与えていない点に注意！
		// PVに関しては、仕様妥協の影響を受けない！
		this.idealCompromisedWork += idealScopeAdjust;

		// ===================================================================
		// スコープ調整に伴う現実モデルの状態変化
		if (this.remainingWork > scopeAdjust) {
			// 残作業がゼロにはならない場合
			this.compromisedWork += scopeAdjust;
			this.remainingWork -= scopeAdjust;
		} else {
			// 残作業がゼロとなる場合
			this.compromisedWork += this.remainingWork;
			this.remainingWork = 0.0e0D;
		}

		// ===================================================================
		// 作業実施に伴う理想モデルの状態変化
		if (this.idealRemainingWork >= this.idealEffort) {
			// まだ完了しない場合
			this.idealCompleteWork += this.idealEffort;
			this.idealRemainingWork -= this.idealEffort;
		} else {
			// 完了する場合
			this.idealCompleteWork += this.idealRemainingWork;
			this.idealRemainingWork = 0.0e0D;
			this.idealCompletionFlag = true; // 完了フラグ
		}

		// ===================================================================
		// 作業実施に伴う現実モデルの状態変化

		// 表面的な効率を算出
		double effectiveEffort = this.effort * this.efficiency;

		// 作業の実施
		if (!this.testFlag) {
			// 制作フェーズの場合の処理

			// 作業実施に伴う作業量の移動
			// (制作フェーズ(前半)なので残作業量が負値になることはないと想定)
			this.totalEffort += this.effort; // ACを算出する
			this.completeWork += effectiveEffort;
			this.remainingWork -= effectiveEffort;

			// 欠陥の混入
			this.latentRework += effectiveEffort * this.defectInjectionRate;

		} else {
			// テストフェーズの場合の処理

			// 追加テストの決定
			if ((this.remainingWork < effectiveEffort)
					&& (this.latentRework > this.completeThreshold)) {
				this.remainingWork += effectiveEffort;
//                this.remainingWork += this.effort;
			}

			if (this.remainingWork >= effectiveEffort) {
				// 残テスト作業量が多い場合の処理
				this.totalEffort += this.effort; // ACを算出する
				this.completeWork += effectiveEffort;
				this.remainingWork -= effectiveEffort;

				// 潜在欠陥量に応じて欠陥検出量を算出
				double detectedReworks = Math.min(
						effectiveEffort * defectDetectionRate,
						this.latentRework);

				// 検出された欠陥作業量を残作業に追加
				this.latentRework -= detectedReworks;
				this.remainingWork += detectedReworks;

			} else {
				// 残テスト作業量が少ない場合の処理
				// 終結処理
				this.totalEffort += this.remainingWork; // ACを算出する
				this.completeWork += this.remainingWork;
				this.remainingWork = 0.0e0D;

				this.completionFlag = true; // 完了状態
			}
		}

		// 進捗率の算出
		this.idealProgressRate = this.idealCompleteWork
				/ (this.idealRemainingWork + this.idealCompleteWork);

		this.progressRate = this.completeWork
				/ (this.remainingWork + this.completeWork);
		if (this.progressRate >= 0.60e0D) {
			// 進捗率が 60% 以上の場合
			this.testFlag = true; // テスト開始とする
		}

		this.pv = this.idealCompleteWork;
		this.ev = (this.idealInitialWork - this.idealCompromisedWork)
				* this.progressRate;
		this.ac = this.totalEffort;

		return;
	}

	/**
	 * プロジェクトマネジメント指示により可変パラメータを設定する.
	 *
	 * @param action
	 */
	private void setParameters(ProjectManagementAction action) {

		// 行動によりパラメータを変更する
		switch (action.getApplyingPressure()) {
			// プレッシャーを掛けるほど
			// ・効率は高くなる (手抜き) 少し圧力を掛けた状態で標準値
			// ・高欠陥混入率は高くなる
			// ・欠陥発見率は低くなる

			case -1 :
				this.efficiency = 0.90e0D;
				this.defectInjectionRate = 0.15e0D;
				this.defectDetectionRate = 0.30e0D;
				break;
			case 0 :
				this.efficiency = 0.95e0D;
				this.defectInjectionRate = 0.20e0D;
				this.defectDetectionRate = 0.25e0D;
				break;
			case 1 :
				this.efficiency = 1.00e0D;
				this.defectInjectionRate = 0.25e0D;
				this.defectDetectionRate = 0.20e0D;
				break;
			case 2 :
				this.efficiency = 1.05e0D;
				this.defectInjectionRate = 0.30e0D;
				this.defectDetectionRate = 0.15e0D;
				break;
			case 99 : // 理想モデルとの一致確認用
				this.efficiency = 1.00e0D;
				this.defectInjectionRate = 0.00e0D;
				this.defectDetectionRate = 0.00e0D;
				break;
			default :
				System.out.println("Illegal PM operation.");
		}

		switch (action.getIncreasingEffort()) {
			// 投入工数を増やすほど
			// ・投入工数は大きくなる
			// ・余りに投入工数が多くなると欠陥混入率が増加する
			case -1 :
				this.effort = this.idealEffort * 0.90e0D;
				this.defectInjectionRate += 0.00e0D;
				break;
			case 0 :
				this.effort = this.idealEffort * 1.00e0D;
				this.defectInjectionRate += 0.00e0D;
				break;
			case 1 :
				this.effort = this.idealEffort * 1.10e0D;
				this.defectInjectionRate += 0.01e0D;
				break;
			case 2 :
				this.effort = this.idealEffort * 1.20e0D;
				this.defectInjectionRate += 0.02e0D;
				break;
			case 99 : // 理想モデルとの一致確認用
				this.effort = this.idealEffort * 1.00e0D;
				this.defectInjectionRate += 0.00e0D;
				break;
			default :
				System.out.println("Illegal PM operation.");
		}

		switch (action.getScopeAdjust()) {
			// スコープ調整
			case -1 :
				// 0.1%追加
				this.scopeAdjustRate = -1.0e-3D;
				break;
			case 0 :
				// 増減なし
				this.scopeAdjustRate = 0.0e-3D;
				break;
			case 1 :
				// 0.1%削減
				this.scopeAdjustRate = 1.0e-3D;
				break;
			case 2 :
				// 0.2%削減
				this.scopeAdjustRate = 2.0e-3D;
				break;
			case 99 : // 理想モデルとの一致確認用
				this.scopeAdjustRate = 0.000e0D;
				break;
			default :
				System.out.println("Illegal PM operation.");
		}

		return;
	}

	/**
	 * プロジェクト状態を返す. Q-Net学習への拡張を考慮し、 状態量は離散化せず連続値のまま返す．
	 *
	 * @return プロジェクト状態.
	 */
	ProjectState observe() {

		// 状態量を格納する器を生成する
		ProjectState state = new ProjectState();

		// シミュレーション時刻を設定する
		state.setSimTime(this.simTime);

		// プロジェクト進捗率と完了フラグを設定する
		state.setProgressRate(this.progressRate, this.completionFlag);

		// スケジュール遅延量と遅延率を設定する
		double sd = 0.0e0D;
		double sdr = 1.0e0D;
		if (0 != this.simTime) {
			sd = (double) ((this.simTime - 1) - this.idealLastTime);
			sdr = (double) (this.simTime - 1) / (double) this.idealLastTime;
		}
		state.setScheduleDelay(sd, sdr);

		// コスト超過量と超過率を設定する
		double co = 0.0e0D;
		double cor = 1.0e0D;
		if (0 != this.simTime) {
			co = this.ac - this.idealInitialWork;
			cor = this.ac / this.idealInitialWork;
		}
		state.setCostOverrun(co, cor);

		// 仕様妥協量と仕様妥協率
		double cp = 0.0e0D;
		double cpr = 1.0e0D;
		if (0 != this.simTime) {
			cp = (double) (this.compromisedWork);
			cpr = (double) this.compromisedWork / ((double) this.initialWork);
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
			avSA = (double) this.accumlatedScopeAdjust / (double) this.simTime;
		}
		state.setAverageAPIESA(avAP, avIE, avSA);

		return state;
	}
}
