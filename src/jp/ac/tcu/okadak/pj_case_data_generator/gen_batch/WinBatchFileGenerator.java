package jp.ac.tcu.okadak.pj_case_data_generator.gen_batch;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * 仮想事例データ生成用のファイル群の生成器.
 *
 * @author K.Okada
 *
 */
public class WinBatchFileGenerator {

	/**
	 *
	 * 仮想事例データ生成用のファイル群を生成する.
	 *
	 * @param args	デフォルト
	 */
	public static void main(final String[] args) {

		System.out.println("Start ... ");

		// 各可変パラメータの値を入力ファイルから読込む

		String filePath = "C:/Users/Okada/Documents/Okada/Research/PjCsDtGen/";
		String fileName = "pjAtr.csv";

		Loader loader = new Loader(filePath + fileName);
		int numOfParameters = loader.getNumOfParameters();
		int numOfCases = loader.getNumOfCases();

		System.out.println("P = " + numOfParameters);
		System.out.println("C = " + numOfCases);

		// 配列領域の確保
		String[] caseName = new String[numOfCases];
		String[] paramName = new String[numOfParameters];
		String[][] value = new String[numOfCases][numOfParameters];

		// データをロードする
		loader.load(caseName, paramName, value);

		WinBatchFileGenerator wbGen = new WinBatchFileGenerator();
		wbGen.generateWinBatch(caseName, paramName, value);

		System.out.println("... Complete.");
	}

	/**
	 *
	 * バッチファイルを生成する.
	 *	 *
	 * @param caseName	事例データ名
	 * @param varName	可変パラメータ名
	 * @param value		可変パラメータの設定値
	 */
	private void generateWinBatch(final String[] caseName,
			final String[] varName, final String[][] value) {

		String filePath = "C:/Users/Okada/Documents/Okada/Research/PjCsDtGen/";

		// 拡張子は安全のため bat ではなく txt とする
		String fileName = "vensimWinBatch.txt";

		VensimCmdFileGenerator vcGen = new VensimCmdFileGenerator();

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filePath
					+ fileName));

			for (int i = 0; i < caseName.length; i++) {

				// Windowsバッチファイルへの書き込み
				bw.write("\"C:\\Program Files (x86)\\Vensim\\vensim\" ");
				bw.write(
						"\"C:\\Users\\Okada\\Documents\\Okada\\Research\\PjCsDtGen\\");

				System.out.println("\t" + i + ":" + caseName[i]);
				bw.write(caseName[i]);
				bw.write(".cmd\"");
				bw.newLine();

				vcGen.generateVensimCmd(caseName[i], varName, value[i]);
			}

			bw.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
