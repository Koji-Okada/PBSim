package jp.ac.tcu.okadak.project_reinforcement_learning;

/**
 *
 *
 * @author K.Okada
 */
public class ServiceModel {

    private double mspq0 = 8.0e6D;  //  週当たりの利益 (8.0 M\/週)
    private double msd0 =50.0;    //  想定サービス期間 (100週 / 50週)
    private double pdc0 = 250.0e6D;   //  計画開発費
    private double beta = 0.8e0;    //  仕様妥協の影響

    /**
     *
     * @param duration
     */
    public void setDuration(double duration) {
        this.msd0 = duration;
        return;
    }

    /**
     * サービスモデルプロジェクトを実施する.
     *
     * @param sd    スケジュール遅延量
     * @param co    コスト超過量
     * @param rcw   仕様妥協率
     * @return      投資回収利益
     */
    double perform(double sd, double co, double rcw) {

        double unit = 0.25e6D; // コスト超過量 (人週→金額)

        double mspq = mspq0 * (1.0e0 - rcw * beta);
        double msd = msd0 - sd;

        double msf = mspq * msd - (pdc0 + co * unit);

        return msf;
    }
}
