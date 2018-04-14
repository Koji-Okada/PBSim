package jp.ac.tcu.okadak.pj_case_data_generator.gen_batch;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import jp.ac.tcu.okadak.CSVTokenizer;

/**
 *
 * データローダー.
 *
 * @author K.Okada
 *
 */
public class Loader {


	/**
	 *
	 */
	private String fileName;

	/**
	 * ファイル名をフルパスで指定する.
	 *
	 * @param fileName
	 */
	Loader(String fileName) {
		super();

		this.fileName = fileName;

		return;
	}


	/**
	 *
	 * 可変パラメータ数を求める.
	 *
	 * @return
	 */
	int getNumOfParameters() {

		int num = 0;

		// 可変パラメータ数を求める
		try {
			File file = new File(this.fileName);
			BufferedReader br = new BufferedReader(new FileReader(file));

			String str = br.readLine();
			CSVTokenizer csv = new CSVTokenizer(str);

			// 1列目を空読みする
			csv.nextToken();
			while (csv.hasMoreTokens()) {
				csv.nextToken();
				num++;
			}

			br.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		return num;
	}


	/**
	 *
	 * 仮想事例数を求める,
	 *
	 * @return
	 */
	int getNumOfCases() {

		int num = 0;

		try {
			File file = new File(this.fileName);
			BufferedReader br = new BufferedReader(new FileReader(file));

			// 1行目を空読みする
			br.readLine();

			// 仮想事例を求める
			while (null != br.readLine()) {
				num++;
			}

			br.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		return num;
	}


	/**
	 *
	 * 仮想事例名、可変パラメータ名、可変パラメータ値を読込む.
	 *
	 * @param caseName
	 * @param paramName
	 * @param value
	 */
	void load(String[] caseName, String[] paramName, String[][] value) {

		int countParameters = 0;
		int countCases = 0;

		//
		try {
			File file = new File(this.fileName);
			BufferedReader br = new BufferedReader(new FileReader(file));

			String str;
			CSVTokenizer csv;

			// パラメータ名を読込む
			str = br.readLine();
			csv = new CSVTokenizer(str);
			csv.nextToken();	// 1列空読み
			while (csv.hasMoreTokens()) {
				paramName[countParameters++] = csv.nextToken();
			}


//			str = br.readLine();
//			csv = new CSVTokenizer(str);

			// データを読込む
			countCases = 0;
			while (null != (str = br.readLine())) {
				csv = new CSVTokenizer(str);

//				csv.nextToken();	// 1列空読み

				countParameters = 0;
				caseName[countCases] = csv.nextToken();
				while (csv.hasMoreTokens()) {
//					System.out.println("\t" + countCases + "\t" + countParameters);
					value[countCases][countParameters++] = csv.nextToken();

				}
				countCases++;
			}

			br.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		return;
	}
}
