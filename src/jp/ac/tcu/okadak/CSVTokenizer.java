package jp.ac.tcu.okadak;


import java.util.StringTokenizer;

/**
 *
 * CSV専用のトークナイザ.
 *
 * @author Koji-Okada
 * @version 2017.11.08
 *
 */
public class CSVTokenizer extends StringTokenizer {

	/**
	 *
	 * コンストラクタ.
	 *
	 * @author K.Okada
	 * @version 2016.01.14
	 *
	 * @param str			入力文字列
	 */
	public CSVTokenizer(final String str) {
		super(str + "," , ",", true);
	}

	/**
	 *
	 * 次のトークンを返す.
	 *
	 * @author Koji-Okada
	 * @version 2016.01.17
	 *
	 * @return			分割されたトークン
	 */
	public final String nextToken() {

		String str = super.nextToken();

		if (',' == str.charAt(0)) {
			return "";
		}

		if ('\"' == str.charAt(0)) {
			// ダブルクォート部分の始まり.

			if ('\"' != str.charAt(str.length() - 1) || (str.length() == 1)) {
				// ダブルクォートが閉じる前にカンマが出現する場合.
				String nxt;
				boolean clFlag; // 閉フラグ
				do {
					super.nextToken();
					nxt = super.nextToken();

					int nLen = nxt.length();
					clFlag = (nxt.charAt(nLen - 1) == '\"');
					if (clFlag) {
						// 最後の文字がダブルクォートの場合.
						if (1 < nLen) {
							clFlag = !(nxt.charAt(nLen - 2) == '\\');
							if (!clFlag) {
								// エスケープされている場合.
								nxt = nxt.substring(0, nLen - 2) + "\"";
							}
						}
					}
					str = str + "," + nxt;
					// ダブルクォート部分の終わりまで.
				} while (!clFlag);
			}

			// 前後のダブルクォートを削除.
			str = str.substring(1, str.length() - 1);
		}

		if (super.hasMoreTokens()) {
			super.nextToken();
		}

		return str;
	}
}
