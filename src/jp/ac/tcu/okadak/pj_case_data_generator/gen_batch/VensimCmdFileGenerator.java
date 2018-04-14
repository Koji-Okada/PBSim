package jp.ac.tcu.okadak.pj_case_data_generator.gen_batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * コマンドファイル生成器.
 *
 *
 * @author K.Okada
 *
 */
public class VensimCmdFileGenerator {

	/**
	 *
	 * コマンドファイルを生成する.
	 *
	 * @param caseName
	 * @param varName
	 * @param value
	 */
	void generateVensimCmd(String caseName, String[] varName, String[] value) {

		String str;

		String fileName = "C:\\Users\\Okada\\Documents\\Okada\\Research\\PjCsDtGen\\"
				+ caseName + ".cmd";

		try {
			File file = new File(fileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));

			// Vensimコマンドファイル前半部分の書込み
			bw.write("SPECIAL>NOINTERACTION");
			bw.newLine();
			bw.write(
					"SPECIAL>LOADMODEL|Model\\Probes_SD_v0.2-20180119-gen.mdl");
			bw.newLine();

			// Vensimコマンドファイル変数設定部分の書込み
			for (int i = 0; i < varName.length; i++) {
				if ('!' != varName[i].charAt(0)) {
					// 一文字目が "!" の変数は対象外
					str = "SIMULATE>SETVAL|" + varName[i] + "=" + value[i];
					bw.write(str);
					bw.newLine();
				}
			}

			// Vensimコマンドファイル後半部分の書込み
			bw.write("SIMULATE>RUNNAME|" + caseName);
			bw.newLine();
			bw.write("MENU>RUN|O");
			bw.newLine();
			str = "MENU>VDF2CSV|" + caseName + ".vdf|" + caseName
					+ ".csv|Output_20180119.lst|+*";
			bw.write(str);
			bw.newLine();
			bw.write("MENU>EXIT");
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			System.out.println(e);
		}

		return;
	}
}
