package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 *
 *
 * @author K.Okada
 */
public class ServiceModel2 {

    private double mspq0 = 6.0e6D;  //  週当たりの利益 (M￥/週)
    private double imp0 = 1.20;  // 利益改善 (倍)

    private double msd0;    //  想定サービス期間 (週 100 vs 50)
    private double pdc0 = 40.0e6D;   //  計画開発費 (M￥)
    private double beta;    //  スコープ変化率の影響

    /**
     *
     *
     * @param duration 想定サービス期間
     * @param b	スコープ変化率の影響
     */
    public ServiceModel2(double duration, double b) {
    	this.msd0 = duration;
    	this.beta = b;

    	return;
    }

    /**
     * サービスモデルプロジェクトを実施する.
     *
     * @param sd    スケジュール遅延量
     * @param co    コスト超過量
     * @param rsc   スコープ変化率
     * @return      投資回収利益
     */
    double perform(double sd, double co, double rsc) {

        double unit = 0.25e6D; // コスト超過量 (人週→金額)

        double imp = (imp0 - 1.0e0D) * Math.pow(rsc, beta);
        double msd = msd0 - sd;

        double msf = mspq0 * imp * msd - (pdc0 + co * unit);

        return msf;
    }
}
