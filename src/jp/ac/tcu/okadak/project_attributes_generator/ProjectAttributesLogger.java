package jp.ac.tcu.okadak.project_attributes_generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 *
 * 生成されたプロジェクト属性をファイルに記録する.
 *
 * @author K.Okada
 *
 */
public class ProjectAttributesLogger {

	/**
	 * 出力ファイル名 (フルパス).
	 */
	private String filePath;

	/**
	 * 出力ファイル.
	 */
	private PrintWriter pWriter;

	/**
	 * コンストラクタ.
	 *
	 * @param name	出力ファイル名 (フルパス)
	 */
	ProjectAttributesLogger(final String name) {
		super();
		this.filePath = name;
		return;
	}

	/**
	 * 出力ファイルを開く.
	 */
	final void open() {

		try {
			this.pWriter = new PrintWriter(new BufferedWriter(new FileWriter(
					filePath)));
		} catch (Exception e) {
			System.out.println(e);
		}

		return;
	}

	/**
	 * 出力ファイルを閉じる.
	 */
	final void close() {

		try {
			this.pWriter.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		return;
	}

	/**
	 * プロジェクト属性データのヘッダーを書込む.
	 */
	final void header() {

		this.pWriter.print(",");

		this.pWriter.printf("!Planned Effort" + ",");
		this.pWriter.printf("!Planned Duration" + ",");
		this.pWriter.printf("!Planned Duration of RD Ph" + ",");
		this.pWriter.printf("!Planned Duration of CD Ph" + ",");
		this.pWriter.printf("!Planned Duration of DD Ph" + ",");
		this.pWriter.printf("!Planned Duration of IM Ph" + ",");
		this.pWriter.printf("!Planned Duration of UT Ph" + ",");
		this.pWriter.printf("!Planned Duration of IT Ph" + ",");
		this.pWriter.printf("!Planned Duration of ST Ph" + ",");


		// フェーズ毎の理想認識工数
		this.pWriter.print("Estimated Works in RD Ph" + ",");
		this.pWriter.print("Estimated Works in CD Ph" + ",");
		this.pWriter.print("Estimated Works in DD Ph" + ",");
		this.pWriter.print("Estimated Works in IM Ph" + ",");
		this.pWriter.print("Estimated Works in UT Ph" + ",");
		this.pWriter.print("Estimated Works in IT Ph" + ",");
		this.pWriter.print("Estimated Works in ST Ph" + ",");

		// フェーズ毎の計画工数
		this.pWriter.print("Estimated Total Works in RD Ph" + ",");
		this.pWriter.print("Estimated Total Works in CD Ph" + ",");
		this.pWriter.print("Estimated Total Works in DD Ph" + ",");
		this.pWriter.print("Estimated Total Works in IM Ph" + ",");
		this.pWriter.print("Estimated Total Works in UT Ph" + ",");
		this.pWriter.print("Estimated Total Works in IT Ph" + ",");
		this.pWriter.print("Estimated Total Works in ST Ph" + ",");

		// フェーズ毎・職種毎の計画人員数
		this.pWriter.print("Planned Number of SEs in RD Ph" + ",");
		this.pWriter.print("Planned Number of SEs in CD Ph" + ",");
		this.pWriter.print("Planned Number of SEs in DD Ph" + ",");
		this.pWriter.print("Planned Number of SEs in IM Ph" + ",");
		this.pWriter.print("Planned Number of SEs in UT Ph" + ",");
		this.pWriter.print("Planned Number of SEs in IT Ph" + ",");
		this.pWriter.print("Planned Number of SEs in ST Ph" + ",");
		this.pWriter.print("Planned Number of PGs in RD Ph" + ",");
		this.pWriter.print("Planned Number of PGs in CD Ph" + ",");
		this.pWriter.print("Planned Number of PGs in DD Ph" + ",");
		this.pWriter.print("Planned Number of PGs in IM Ph" + ",");
		this.pWriter.print("Planned Number of PGs in UT Ph" + ",");
		this.pWriter.print("Planned Number of PGs in IT Ph" + ",");
		this.pWriter.print("Planned Number of PGs in ST Ph" + ",");

		// 人材能力レベル
		this.pWriter.print("Initial Capability Level of SEs" + ",");
		this.pWriter.print("Initial Capability Level of PGs" + ",");

		// 人材モチベーションレベル
		this.pWriter.print("Initial Motivation Level of SEs" + ",");
		this.pWriter.print("Initial Motivation Level of PGs" + ",");

		// レビュー方針(レビュー作業工数比率)
		this.pWriter.print("Review Policy" + ",");

		// コミュニケーション方針(コミュニケーション作業工数比率)
		this.pWriter.print("Communication Policy" + ",");

		// コミュニケーションエラー率
		this.pWriter.print("Base Rate of Communication Error");


		this.pWriter.println();

		return;
	}

	/**
	 * プロジェクト属性データを書込む.
	 *
	 * @param pjAtr	プロジェクト属性
	 */
	final void log(final ProjectAttributes pjAtr) {

		// プロジェクト事例名称
		this.pWriter.print(pjAtr.caseName);

		// 基本情報
		this.pWriter.printf(", %12.3f", pjAtr.plannedTotalEffort);
		this.pWriter.printf(", %12.3f", pjAtr.plannedTotalDuration);
		for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {
			this.pWriter.printf(", %12.3f",
					pjAtr.plannedDurationInPhase[ph]);
		}


		// フェーズ毎の理想認識工数
		for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {
			this.pWriter.printf(", %12.3f",
					pjAtr.idealRecognizedEffortInPhase[ph]);
		}

		// フェーズ毎の計画工数
		for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {
			this.pWriter.printf(", %12.3f", pjAtr.plannedEffortInPhase[ph]);
		}

		// フェーズ毎・職種毎の計画人員数
		for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {
			this.pWriter.printf(", %8.1f",
					pjAtr.plannedNumberOfProfessionInPhase[ph][ProbesSD.SE]);
		}
		for (int ph = 0; ph < ProbesSD.NUM_OF_PHASES; ph++) {
			this.pWriter.printf(", %8.1f",
					pjAtr.plannedNumberOfProfessionInPhase[ph][ProbesSD.PG]);
		}

		// 人材能力レベル
		for (int prf = 0; prf < ProbesSD.NUM_OF_PROFESSIONS; prf++) {
			this.pWriter.printf(", %8.5f", pjAtr.capabilityLevel[prf]);
		}

		// 人材モチベーションレベル
		for (int prf = 0; prf < ProbesSD.NUM_OF_PROFESSIONS; prf++) {
			this.pWriter.printf(", %8.5f", pjAtr.motivationLevel[prf]);
		}

		// レビュー方針(レビュー作業工数比率)
		this.pWriter.printf(", %8.5f", pjAtr.reviewPolicy);

		// コミュニケーション方針(コミュニケーション作業工数比率)
		this.pWriter.printf(", %8.5f", pjAtr.communicationPolicy);

		// コミュニケーションエラー率
		this.pWriter.printf(", %8.5f", pjAtr.communicationErrorRate);

		this.pWriter.println();

		return;
	}
}
