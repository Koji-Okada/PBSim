package jp.ac.tcu.okadak.project_attributes_generator;

/**
 * Probes-SD 関連の定数値.
 *
 * @author K.Okada
 */
public class ProbesSD {

	/**
	 * コンストラクタの内部化.
	 */
	private ProbesSD() {
		super();
	}

	/**
	 * フェーズ数.
	 */
	public static final int NUM_OF_PHASES = 7;

	/**
	 * 要件定義フェーズ.
	 */
	public static final int RD = 0;

	/**
	 * 基本設計フェーズ.
	 */
	public static final int CD = 1;

	/**
	 * 詳細設計フェーズ.
	 */
	public static final int DD = 2;

	/**
	 * 実装フェーズ.
	 */
	public static final int IM = 3;

	/**
	 * 単体テストフェーズ.
	 */
	public static final int UT = 4;

	/**
	 * 統合テストフェーズ.
	 */
	public static final int IT = 5;

	/**
	 * システムテストフェーズ.
	 */
	public static final int ST = 6;

	/**
	 * 職種数.
	 */
	public static final int NUM_OF_PROFESSIONS = 2;

	/**
	 * システムエンジニア.
	 */
	public static final int SE = 0;

	/**
	 * プログラマ.
	 */
	public static final int PG = 1;
}
