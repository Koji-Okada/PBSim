package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 *
 *
 * @author K.Okada
 */
public class ServiceModel3 {

	private double mspq0 = 6.0e6D; // 週当たりの利益 (M￥/週)

	private double msd0; // 想定サービス期間 (週 100 vs 50)
	private double pdc0 = 40.0e6D; // 計画開発費 (M￥)
	private double beta; // スコープ変化率の影響

	/**
	 *
	 *
	 * @param duration
	 *            想定サービス期間
	 * @param b
	 *            スコープ変化率の影響
	 */
	public ServiceModel3(double duration, double b) {
		this.msd0 = duration;
		this.beta = b;

		return;
	}


	/**
	 * テスト用メイン
	 *
	 * @param args
	 */
	public static void main(String args[]) {

		ServiceModel3 sm3 = new ServiceModel3(100.0e0D, 1.0e0D);
		sm3.perform(5.0e0D, 0.0e0D, 0.90e0D);

		return;
	}



	/**
	 * サービスモデルプロジェクトを実施する.
	 *
	 * @param sd
	 *            スケジュール遅延量
	 * @param co
	 *            コスト超過量
	 * @param rsc
	 *            スコープ変化率
	 * @return 投資回収利益
	 */
	double perform(double sd2, double co2, double rsc2) {

		double imp2 = 0.90; // 欠陥混入率の基準変化
		double beta2 = 1.00;

		double imp = (imp2 - 1.0e0D) * Math.pow(rsc2, beta2);

		// 基準の製品開発プロジェクトを生成する
		// 改善効果のメカニズムを加えた ProjectModel3を使用
		ProjectModel3 prDevPj = new ProjectModel3(1000.0e0D, 20.0e0D, 1.0e0D,
				1.0e0D);

		// プロジェクトマネジャーの行動は固定で与える
		// 改善代が必要なので、1,1,1 としている
		ProjectManagementAction action = new ProjectManagementAction(1, 1, 1);

		boolean improved = false;	// 改善後か否か
	    ProjectState preState;
	    ProjectState postState;
		do {
			postState = prDevPj.observe();
			int st = postState.getSimTime();

			if ((!improved) && (st >= (int)sd2)) {
				improved = true;
				prDevPj.improveEffect = imp;	// 改善効果の反映
//				System.out.println(" -> " + imp);
			}

//			System.out.println(st + "\t\t" + prDevPj.improveEffect);

			prDevPj.perform(action);
			// 行動後の状態を観測する
			postState = prDevPj.observe();
		} while (!postState.isComplete());

		// 製品開発プロジェクトのスケジュール遅延・コスト超過・スコープ変化
		double sd0 = postState.getScheduleDelay();
		double co0 = postState.getCostOverrun();
		double rsc0 = postState.getScopeChangeRate();

//		System.out.printf("%6.3f\t%6.3f\t%6.3f\n",sd0, co0, rsc0);

		// ==============================================================
		// ここから量産販売プロジェクト

		double pc2 = 35.0e6D;	// 製品開発力向上プロジェクト費用
		double unit = 0.25e6D;	// 1人週 ⇒ 費用

		// サービスモデルを作成して、期間他を与える
		ServiceModel sv = new ServiceModel(this.msd0, this.beta);


		double fv = sv.perform(sd0, co0, rsc0) - (pc2 + co2 * unit);

		return fv;
	}
}
