package jp.ac.tcu.okadak.project_attributes_generator;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;

/**
 *
 * プロジェクト属性生成器.
 *
 * @author K.Okada
 */
public class ProjectAttributesGenerator {

	/**
	 * 成果物規模の確率分布.
	 */
	private LogNormalDistribution distributionProduuctSize = null;

	/**
	 * 期間変動の確率分布.
	 */
	private NormalDistribution distributionDuration = null;

	/**
	 * フェーズ毎工数比率の確立分布.
	 *
	 * 再現性確保のためランダムシードを固定
	 * (ただし他のランダムシードと同じ値にしないこと！)
	 */
	private NormalDistribution distributionPhaseEffortRate = new NormalDistribution(
			new JDKRandomGenerator(3), 0.0e0D, 0.05e0D);

	//
	/**
	 * フェーズ毎期間比率の確立分布.
	 *
	 * 再現性確保のためランダムシードを固定
	 * (ただし他のランダムシードと同じ値にしないこと！)
	 */
	NormalDistribution distributionePhaseDurationRate = new NormalDistribution(
			new JDKRandomGenerator(4), 0.0e0D, 0.05e0D);

	/**
	 * 能力レベルの確立分布.
	 *
	 * 再現性確保のためランダムシードを固定
	 * (ただし他のランダムシードと同じ値にしないこと！)
	 */
	NormalDistribution distributionCapabilityLevel = new NormalDistribution(
			new JDKRandomGenerator(5), 0.0e0D, 0.05e0D);

	/**
	 * モチベーションレベルの確立分布.
	 *
	 * 再現性確保のためランダムシードを固定
	 * (ただし他のランダムシードと同じ値にしないこと！)
	 */
	NormalDistribution distributionMotivationLevel = new NormalDistribution(
			new JDKRandomGenerator(6), 0.0e0D, 0.05e0D);

	/**
	 * レビュー方針(レビュー作業工数比率)の確立分布.
	 *
	 * 再現性確保のためランダムシードを固定
	 * (ただし他のランダムシードと同じ値にしないこと！)
	 */
	NormalDistribution distributionReviewPolicy = new NormalDistribution(
			new JDKRandomGenerator(7), 0.0e0D, 0.05e0D);

	/**
	 * コミュニケーション方針(コミュニケーション作業工数比率)の確立分布.
	 *
	 * 再現性確保のためランダムシードを固定
	 * (ただし他のランダムシードと同じ値にしないこと！)
	 */
	NormalDistribution distributionCommunicationPolicy = new NormalDistribution(
			new JDKRandomGenerator(8), 0.0e0D, 0.05e0D);

	/**
	 * コミュニケーションエラー率の確立分布.
	 *
	 * 再現性確保のためランダムシードを固定
	 * (ただし他のランダムシードと同じ値にしないこと！)
	 */
	NormalDistribution distributionCommunicationErrorRate = new NormalDistribution(
			new JDKRandomGenerator(9), 0.0e0D, 0.05e0D);

	/**
	 * 工数見積係数.
	 * (デフォルトは 1.0)
	 */
	private double effortEstimationParameter = 1.0e0D;

	/**
	 * 理想的工数見積係数.
	 * (デフォルトは 1.0)
	 */
	private double idealEffortEstimationParameter = 1.0e0D;

	/**
	 * 単体機能確認用メインルーチン.
	 *
	 * @param arg デフォルト
	 */
	public static void main(final String[] arg) {

		// プロジェクト属性生成器を生成する
		ProjectAttributesGenerator pg = new ProjectAttributesGenerator();

		// IPA-SECのデータから求めた総工数(人日)の平均値と標準偏差
		pg.setProductSizeDistibution(2843.371e0D, 4711.928e0D);

		// 期間変動の確立分布を設定する
		//		pg.setDurationDistribution(0.0e0D, 0.45e0D);
		pg.setDurationDistribution(0.0e0D, 0.45e0D);

		// 工数見積係数を設定する
		pg.setEffortEstimationParameter(1.0e0D);

		// 理想的工数見積係数を設定する
		pg.setIdealEffortEstimationParameter(1.0e0D / 1.2e0D); // 手戻り率 0.2

		// プロジェクト属性記録器を生成する
		String path = "C:/Users/Okada/Documents/Okada/Research/PjCsDtGen/";
		String fileName = "pjAtr.csv";
		ProjectAttributesLogger pal = new ProjectAttributesLogger(path
				+ fileName);
		pal.open();

		// ヘッダー行を書込む
		pal.header();

		for (int i = 0; i < 1000; i++) {
			// プロジェクト属性を生成する.
			ProjectAttributes pjAtr = pg.generateProjectAttribute(true);
			pjAtr.caseName = String.format("case%04d", i);

			// プロジェクト事例名称
			System.out.printf("%s\t", pjAtr.caseName);

			// 成果物規模
			//			System.out.printf("%12.4f\t", pjAtr.productSize);
			// 見積成果物規模
			//			System.out.printf("%12.4f\t", pjAtr.estimatedProductSize);

			// 理想総工数
			//			System.out.printf("%12.4f\t", pjAtr.idealTotalEffort);
			// 理想認識総工数
			//			System.out.printf("%12.4f\t", pjAtr.idealRecognizedTotalEffort);

			// 見積総工数
			//			System.out.printf("%12.4f\t", pjAtr.plannedTotalEffort);
			// 計画期間
			//			System.out.printf("%12.4f\t", pjAtr.plannedTotalDuration);
			// 計画人員数
			//			System.out.printf("%10.2f\t", pjAtr.plannedNumberOfHumanResources);

			// 要件定義を除く工程フェーズの期間を出力する
			//			double ph5 = 0.0e0d;
			//			for (int p = 1; p < ProbesSD.NUM_OF_PHASES; p++) {
			//				ph5 += pjAtr.plannedDurationInPhase[p];
			//			}
			//			System.out.printf("%10.2f\t", ph5);

			// フェーズ毎の計画工数
			//						for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {
			//							System.out.printf("%12.4f\t", pjAtr.plannedEffortInPhase[ph]);
			//						}

			// フェーズ毎の計画期間
			//			for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {
			//				System.out.printf("%12.4f\t", pjAtr.plannedDurationInPhase[ph]);
			//			}

			// フェーズ毎・職種毎の計画人員数
			//			System.out.println();
			//			System.out.printf("*SE\t");
			//			for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {
			//				System.out.printf("%10.2f\t",
			//						pjAtr.plannedNumberOfProfessionInPhase[ph][ProbesSD.SE]);
			//			}
			//			System.out.println();
			//			System.out.printf(" PG\t");
			//			for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {
			//				System.out.printf("%10.2f\t",
			//						pjAtr.plannedNumberOfProfessionInPhase[ph][ProbesSD.PG]);
			//			}

			// フェーズ毎の理想認識工数
			//			for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {
			//				System.out.printf("%12.4f\t",
			//						pjAtr.idealRecognizedEffortInPhase[ph]);
			//			}

			// レビュー方針
			System.out.printf("%8.5f\t", pjAtr.reviewPolicy);

			// コミュニケーション方針
			System.out.printf("%8.5f\t", pjAtr.communicationPolicy);

			// コミュニケーションエラー率
			System.out.printf("%8.5f\t", pjAtr.communicationErrorRate);

			// 能力レベル
			//			for (int prf = 0; prf < ProbesSD.NUM_OF_PROFESSIONS; prf++) {
			//				System.out.printf("%8.5f\t", pjAtr.capabilityLevel[prf]);
			//			}
			//
			//			// モチベーションレベル
			//			for (int prf = 0; prf < ProbesSD.NUM_OF_PROFESSIONS; prf++) {
			//				System.out.printf("%8.5f\t", pjAtr.motivationLevel[prf]);
			//			}

			System.out.println();

			// プロジェクト属性の値を記録する
			pal.log(pjAtr);
		}

		// プロジェクト属性記録器を閉じる
		pal.close();
	}

	/**
	 * 成果物規模の確率分布を設定する.
	 *
	 * @param mean	平均
	 * @param sd	標準偏差
	 **/
	public final void setProductSizeDistibution(final double mean,
			final double sd) {

		// 平均・標準偏差となるような対数正規関数のパラメータを算出する
		double p0 = Math.log((sd * sd) / (mean * mean) + 1.0e0D);
		double p2 = Math.sqrt(p0);
		double p1 = Math.log(mean) - p0 / 2.0e0D;

		// 再現性確保のためランダムシードを固定
		// (ただし他のランダムシードと同じ値にしないこと！)
		JDKRandomGenerator rg = new JDKRandomGenerator(1);
		this.distributionProduuctSize = new LogNormalDistribution(rg, p1, p2);
		//		this.distributionProduuctSize = new LogNormalDistribution(p1, p2);

		return;
	}

	/**
	 * 期間変動の確率分布を設定する.
	 *
	 * @param mean	平均
	 * @param sd	標準偏差
	 */
	public final void setDurationDistribution(final double mean,
			final double sd) {

		// 再現性確保のためランダムシードを固定
		// (ただし他のランダムシードと同じ値にしないこと！)
		JDKRandomGenerator rg = new JDKRandomGenerator(2);

		this.distributionDuration = new NormalDistribution(rg, mean, sd);
		//		this.distributionDuration = new NormalDistribution(mean, sd);
		return;
	}

	/**
	 * 工数見積係数を設定する.
	 *
	 * @param estimationParameter	工数見積係数値
	 */
	public final void setEffortEstimationParameter(
			final double estimationParameter) {

		this.effortEstimationParameter = estimationParameter;
		return;
	}

	/**
	 * 工数見積係数を設定する.
	 *
	 * @param estimationParameter	工数見積係数値
	 */
	public final void setIdealEffortEstimationParameter(
			final double estimationParameter) {

		this.idealEffortEstimationParameter = estimationParameter;
		return;
	}

	/** =======================================================================
	 *
	 * プロジェクト属性を生成する.
	 *
	 * @param modeProbesSD	Probes-SDモードの場合 True
	 * @return	プロジェクト属性
	 */
	public final ProjectAttributes generateProjectAttribute(
			final Boolean modeProbesSD) {

		// プロジェクト属性オブジェクトを生成する
		ProjectAttributes pjAtr = new ProjectAttributes();

		// 成果物規模を見積る
		estimateProductSize(pjAtr);

		// 総工数を見積る
		planTotalEffort(pjAtr);

		// 理想総工数・理想認識総工数を算定する
		decideIdealTotalEffort(pjAtr);

		// 総期間を計画する
		planTotalDuration(pjAtr);

		// 人員数を計画する
		planNumOfHumanResouce(pjAtr);

		if (modeProbesSD) { // Probes-SDモードの場合

			// ==== 計画モデル側の設定パラメータを算出する
			// 各フェーズに計画工数を配分する
			planEffortInPhase(pjAtr);

			// 各フェーズに計画期間を配分する
			planDurationInPhase(pjAtr);

			// 各フェーズの人的資源を計画する
			planNumOfHumanResouceInPhase(pjAtr);

			// ==== レビュー方針・コミュニケーション方針(作業工数比率)を算出する
			//  手戻りメカニズム再現プロセス側の算出よりも先に！
			setReviewPolicy(pjAtr);
			setCommunicationPolicy(pjAtr);

			// ==== 手戻りメカニズム再現プロセス側の設定パラメータを算出する
			// 各フェーズに理想認識総工数を配分する
			decideIdealRecognizedEffortInPhase(pjAtr);

			// ==== 能力レベル・モチベーションレベルの設定パラメータを算出する
			setCapabilityLevel(pjAtr);
			setMotivationLevel(pjAtr);

			// ==== コミュニケーションエラー率の設定パラメータを算出する
			setCommunicationErrorRate(pjAtr);
		}

		return pjAtr;
	}

	/**
	 * 	成果物規模を見積る.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void estimateProductSize(final ProjectAttributes pjAtr) {

		// 成果物規模を求める
		double productSize;
		if (null != this.distributionProduuctSize) {
			productSize = this.distributionProduuctSize.sample();
		} else {
			productSize = 5000.0e0D; // とりあえず
		}
		pjAtr.productSize = productSize;

		// 見積成果物規模を求める
		pjAtr.estimatedProductSize = productSize; // 要件見逃しは未考慮

		return;
	}

	/**
	 *	総工数を見積る.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void planTotalEffort(final ProjectAttributes pjAtr) {

		// 見積総工数を求める
		double estimatedTotalEffort = pjAtr.estimatedProductSize
				* this.effortEstimationParameter;
		pjAtr.plannedTotalEffort = estimatedTotalEffort;

		return;
	}

	/**
	 *	理想総工数と理想認識総工数を算定する.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void decideIdealTotalEffort(final ProjectAttributes pjAtr) {

		// 理想総工数を求める
		double idealTotalEffort = pjAtr.productSize
				* this.idealEffortEstimationParameter;
		pjAtr.idealTotalEffort = idealTotalEffort;

		// 理想認識総工数を求める
		double idealRecognizedTotalEffort = pjAtr.estimatedProductSize
				* this.idealEffortEstimationParameter;
		pjAtr.idealRecognizedTotalEffort = idealRecognizedTotalEffort;

		return;
	}

	/**
	 *	総期間を計画する.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void planTotalDuration(final ProjectAttributes pjAtr) {

		// IPE-SECのデータから求めた総工数と総期間の関係式を用いる
		double p1 = 17.084e0D;
		double p2 = 0.3175e0D;
		double plannedTotalDuration = p1 * Math.pow(pjAtr.plannedTotalEffort,
				p2);

		// 期間の変動を混入する
		double amp;
		if (null != this.distributionDuration) {
			amp = Math.exp(this.distributionDuration.sample());
		} else {
			amp = 1.0e0D;
		}

		pjAtr.plannedTotalDuration = plannedTotalDuration * amp;

		return;
	}

	/**
	 * 人員数を計画する.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void planNumOfHumanResouce(final ProjectAttributes pjAtr) {

		// 人員数を計画する (0.5人単位で丸める)
		double numOfHR = adjustNumOfHumanResource(pjAtr.plannedTotalEffort
				/ pjAtr.plannedTotalDuration);
		if (0.5e0D > numOfHR) {
			// 人員数が 0 の場合、0.5人を割当て
			numOfHR = 0.5e0D;
		}

		pjAtr.plannedNumberOfHumanResources = numOfHR;

		return;
	}

	/**
	 *	各フェーズに工数を配分する.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void planEffortInPhase(final ProjectAttributes pjAtr) {

		double[] ratioOfEffortInPhase = { // IPE-SECデータから算出した比率を使用
				0.0980e0D, // RDフェーズの工数比率
				0.1484e0D, // CDフェーズの工数比率
				0.1565e0D, // DDフェーズの工数比率
				0.3174e0D * 0.6e0D, // IMフェーズの工数比率
				0.3174e0D * 0.4e0D, // UTフェーズの工数比率
				0.1610e0D, // ITフェーズの工数比率
				0.1188e0D // STフェーズの工数比率
		};

		for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {

			double delta = ratioOfEffortInPhase[ph] * (Math.exp(
					distributionPhaseEffortRate.sample()) - 1.0e0D);

			//			delta = 0.0e0D;

			pjAtr.plannedEffortInPhase[ph] = pjAtr.plannedTotalEffort
					* ((ratioOfEffortInPhase[ph] + delta) / (1.0e0D + delta));
		}

		return;
	}

	/**
	 *	各フェーズに期間を配分する.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void planDurationInPhase(final ProjectAttributes pjAtr) {

		double[] ratioOfDurationInPhase = { // IPE-SECデータから算出した比率を使用
				0.1817e0D, // RDフェーズの期間比率
				0.1766e0D, // CDフェーズの期間比率
				0.1543e0D, // DDフェーズの期間比率
				0.2143e0D * 0.6e0D, // IMフェーズの期間比率
				0.2143e0D * 0.4e0D, // UTフェーズの期間比率
				0.1403e0D, // ITフェーズの期間比率
				0.1328e0D // STフェーズの期間比率
		};

		for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {

			double delta = ratioOfDurationInPhase[ph] * (Math.exp(
					distributionePhaseDurationRate.sample()) - 1.0e0D);

			//			delta = 0.0e0D;

			pjAtr.plannedDurationInPhase[ph] = pjAtr.plannedTotalDuration
					* ((ratioOfDurationInPhase[ph] + delta) / (1.0e0D + delta));
		}

		return;
	}

	/**
	 *	各フェーズの人的資源を計画する.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void planNumOfHumanResouceInPhase(final ProjectAttributes pjAtr) {

		double[][] ratioOfProfessionInPhase = { // とりあえず
				{ 1.00e0D, 0.00e0D }, // RDフェーズの SE/PG 比率
				{ 0.50e0D, 0.50e0D }, // CDフェーズの SE/PG 比率
				{ 0.20e0D, 0.80e0D }, // DDフェーズの SE/PG 比率
				{ 0.10e0D, 0.90e0D }, // IDフェーズの SE/PG 比率
				{ 0.10e0D, 0.90e0D }, // UTフェーズの SE/PG 比率
				{ 0.40e0D, 0.60e0D }, // ITフェーズの SE/PG 比率
				{ 0.40e0D, 0.60e0D } // STフェーズの SE/PG 比率
		};

		for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {

			double numInPhase = pjAtr.plannedEffortInPhase[ph]
					/ pjAtr.plannedDurationInPhase[ph];

			double numSE = adjustNumOfHumanResource(numInPhase
					* ratioOfProfessionInPhase[ph][ProbesSD.SE]);
			double numPG = adjustNumOfHumanResource(numInPhase
					* ratioOfProfessionInPhase[ph][ProbesSD.PG]);

			// 欠陥修正用の人員の確保
			if (numSE < 0.5e0D) {
					numSE = 0.5e0D;
			}
			if (numPG < 0.5e0D) {
				if (ProbesSD.RD != ph) {
					numPG = 0.5e0D;
				}
			}

			pjAtr.plannedNumberOfProfessionInPhase[ph][ProbesSD.SE] = numSE;
			pjAtr.plannedNumberOfProfessionInPhase[ph][ProbesSD.PG] = numPG;
		}

		return;
	}

	/**
	 * 人員数の端数を調整する.
	 *
	 * @param num	人員数
	 * @return		調整済み人員数
	 */
	private double adjustNumOfHumanResource(double num) {

		// 単位期間当工数を計画する (0.5人単位で丸める)
		double adjust = Math.round(num * 2.0e0D) / 2.0e0D;

		return adjust;
	}

	/**
	 *	手戻りメカニズム再現プロセス側において各フェーズに工数を配分する.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void decideIdealRecognizedEffortInPhase(
			final ProjectAttributes pjAtr) {

		double[] ratioOfEffortInPhase = { // IPE-SECデータから算出した比率を使用
				0.0980e0D, // RDフェーズの工数比率
				0.1484e0D, // CDフェーズの工数比率
				0.1565e0D, // DDフェーズの工数比率
				0.3174e0D * 0.6e0D, // IMフェーズの工数比率
				0.3174e0D * 0.4e0D, // UTフェーズの工数比率
				0.1610e0D, // ITフェーズの工数比率
				0.1188e0D // STフェーズの工数比率
		};

		double[] overheadRate = { // Probes-SDの基本値
				pjAtr.reviewPolicy + pjAtr.communicationPolicy, // RDフェーズ
				pjAtr.reviewPolicy + pjAtr.communicationPolicy, // CDフェーズ
				pjAtr.reviewPolicy + pjAtr.communicationPolicy, // DDフェーズ
				pjAtr.reviewPolicy + pjAtr.communicationPolicy, // IMフェーズ
				pjAtr.communicationPolicy, // UTフェーズ
				pjAtr.communicationPolicy, // ITフェーズ
				pjAtr.communicationPolicy // STフェーズ
		};

		// ※※※ オーバーヘッド分の考慮が未だ
		for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {
			pjAtr.idealRecognizedEffortInPhase[ph] = pjAtr.idealRecognizedTotalEffort
					* ratioOfEffortInPhase[ph] * (1.0e0D - overheadRate[ph]);
		}

		return;
	}

	/**
	 *	能力レベルを設定する.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void setCapabilityLevel(final ProjectAttributes pjAtr) {

		double[] baseCapabliltyLevel = { // Probes-SDの基本値
				0.7e0D, // SE
				0.7e0D, // PG
		};

		double[] base = new double[ProbesSD.NUM_OF_PROFESSIONS];

		for (int prf = 0; prf < ProbesSD.NUM_OF_PROFESSIONS; prf++) {

			// シグモイド関数の入力値にバラツキを混入させる
			base[prf] = -Math.log(1.0e0D / baseCapabliltyLevel[prf] - 1.0e0D);
			double delta = distributionCapabilityLevel.sample();
			double x = base[prf] + delta;
			double y = 1.0e0D / (1.0e0D + Math.exp(-x));

			pjAtr.capabilityLevel[prf] = y;
		}

		return;
	}

	/**
	 *	モチベーションレベルを設定する.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void setMotivationLevel(final ProjectAttributes pjAtr) {

		double[] baseMotivationLevel = { // Probes-SDの基本値
				0.8e0D, // SE
				0.8e0D, // PG
		};

		double[] base = new double[ProbesSD.NUM_OF_PROFESSIONS];

		for (int prf = 0; prf < ProbesSD.NUM_OF_PROFESSIONS; prf++) {

			// シグモイド関数の入力値にバラツキを混入させる
			base[prf] = -Math.log(1.0e0D / baseMotivationLevel[prf] - 1.0e0D);
			double delta = distributionMotivationLevel.sample();
			double x = base[prf] + delta;
			double y = 1.0e0D / (1.0e0D + Math.exp(-x));

			pjAtr.motivationLevel[prf] = y;
		}

		return;
	}

	/**
	 *	レビュー方針(レビュー作業工数比率)を設定する.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void setReviewPolicy(final ProjectAttributes pjAtr) {

		double baseReviewPolicy = 0.15e0D; // Probes-SDの基本値

		// シグモイド関数の入力値にバラツキを混入させる
		double base = -Math.log(1.0e0D / baseReviewPolicy - 1.0e0D);
		double delta = distributionReviewPolicy.sample();
		double x = base + delta;
		double y = 1.0e0D / (1.0e0D + Math.exp(-x));

		pjAtr.reviewPolicy = y;

		return;
	}

	/**
	 *	コミュニケーション方針(コミュニケーション作業工数比率)を設定する.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void setCommunicationPolicy(final ProjectAttributes pjAtr) {

		double baseCommunicationPolicy = 0.15e0D; // Probes-SDの基本値

		// シグモイド関数の入力値にバラツキを混入させる
		double base = -Math.log(1.0e0D / baseCommunicationPolicy - 1.0e0D);
		double delta = distributionCommunicationPolicy.sample();
		double x = base + delta;
		double y = 1.0e0D / (1.0e0D + Math.exp(-x));

		pjAtr.communicationPolicy = y;

		return;
	}

	/**
	 * コミュニケーションエラー率を設定する.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	private void setCommunicationErrorRate(final ProjectAttributes pjAtr) {

		double baseCommunicationErrorRate = 0.010e0D; // Probes-SDの基本値

		// バラツキを混入させる
		pjAtr.communicationErrorRate = baseCommunicationErrorRate * Math.exp(
				distributionCommunicationErrorRate.sample());

		return;
	}

}
