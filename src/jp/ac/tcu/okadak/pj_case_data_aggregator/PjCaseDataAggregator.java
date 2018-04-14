package jp.ac.tcu.okadak.pj_case_data_aggregator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import jp.ac.tcu.okadak.CSVTokenizer;

/**
 *
 * 仮想データの集約器.
 *
 * @author K.Okada
 *
 */
public class PjCaseDataAggregator {

	/**
	 *
	 * 生成された仮想事例データを集約する.
	 *
	 * @param args	デフォルト
	 */
	public static void main(final String[] args) {

		PjCaseDataAggregator da = new PjCaseDataAggregator();
		da.aggregate();

		return;
	}

	/**
	 *
	 * プロジェクト属性(入力)とプロジェクト実績(シミュレーション結果)を集約する.
	 *
	 */
	private void aggregate() {
		String basePath = "C:/Users/Okada/Documents/Okada/Research/PjCsDtGen/";
		String inputName = "pjAtr.csv";
		String summaryName = "summaryPjCase.csv";
		String resPath = basePath + "Model/";

		try {
			File inputFile = new File(basePath + inputName);
			File summaryFile = new File(basePath + summaryName);

			// 入力ファイルと出力ファイルを開く
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
					summaryFile)));

			// 1行目を処理する
			Boolean initFlag = true;
			String header = br.readLine();

			String line;
			while (null != (line = br.readLine())) {

				// 仮想事例を求める
				CSVTokenizer csv = new CSVTokenizer(line);
				String caseName = csv.nextToken();
				System.out.println(caseName);

				String caseFile = resPath + caseName + ".csv";

				// シミュレーション結果データを結合する
				if (initFlag) {
					// 最初の事例の場合
					// ヘッダーを出力する
					pw.println(header + "," + header(caseFile));
				}

				String simResData = joint(caseFile);
				pw.println(line + "," + simResData);

				initFlag = false;
			}

			// 入力ファイルと出力ファイルを閉じる
			br.close();
			pw.close();

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * シミュレーション結果ファイルのヘッダー文字列を返す.
	 *
	 * @param caseFile	事例ファイル名
	 * @return	ヘッダー文字列
	 */
	private String header(final String caseFile) {

		String line = "";
		try {
			// シミュレーション結果ファイルを開く
			BufferedReader br = new BufferedReader(new FileReader(caseFile));

			line = br.readLine();	// ヘッダーを読み込む
			br.close();
		} catch (IOException e) {
			System.out.println(e);
		}
		return line;
	}

	/**
	 *
	 * @param caseName
	 */

	/**
	 * シミュレーション結果ファイルから完了時データの文字列を返す.
	 *
	 * @param caseFile	事例ファイル名
	 * @return	シミュレーション結果の完了時データ示す文字列
	 */
	private String joint(final String caseFile) {

		String line = "";
		try {
			// シミュレーション結果ファイルを開く
			BufferedReader br = new BufferedReader(new FileReader(caseFile));

			br.readLine(); // ヘッダー部分を空読み

			while (null != (line = br.readLine())) {
				// シミュレーション結果を読み込む

				CSVTokenizer csv = new CSVTokenizer(line);
				csv.nextToken();
				String pjCmpFlag = csv.nextToken();
				if (pjCmpFlag.equals("1")) {
					// 完了フラグが「1」になった場合にデータを示す文字列を返す
					break;
				}
			}
			br.close();
		} catch (IOException e) {
			System.out.println(e);
		}

		return line;
	}
}
