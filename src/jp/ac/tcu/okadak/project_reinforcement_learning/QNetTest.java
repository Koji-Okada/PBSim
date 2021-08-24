package jp.ac.tcu.okadak.project_reinforcement_learning;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class QNetTest {

	public static void main(String[] args) {

		System.out.println("Start QNetTest...");

		QNet qNet = new QNet();
		
//		int[] nodeLevels = { 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		int[] nodeLevels = { 10, 4, 4, 4, 4, 4, 4, 4, 4 };

		int tb = QNet.TOP_BOUNDARY_PLUS | QNet.DIFFERENTIAL;
//		int nr = QNet.NORMAL;
		int nr = QNet.NORMAL | QNet.DIFFERENTIAL;
		int[] nodeEncodings = {tb, nr, nr, nr, nr, nr, nr, nr, nr};
		
		int nSample = 1024 * 2;
		int n = 16;
		int nParam = nodeLevels.length;

		float[][] in = new float[nSample][nParam];
		float[][] out = new float[nSample][1];

		qNet.generate(nodeLevels, nodeEncodings); // ニューラルネットの生成

		int cnt = 0;
		for (int i = 0; i < n; i++, cnt++) {
			in[cnt][0] = 1.00e0F;
			for (int j = 1; j < nParam; j++) {
				in[cnt][j] = qNet.sampleX();
			}
			float d = in[cnt][1] - 0.50e0F;
			out[cnt][0] = -100.0e0F - (d * d * 100.0e0F);
		}
		for (int i = 0; i < nSample - n; i++, cnt++) {
			in[cnt][0] = qNet.sampleX() * 0.95e0F;
			for (int j = 1; j < nParam; j++) {
				in[cnt][j] = qNet.sampleX();
			}
			out[cnt][0] = 0.0e0F;
		}
		
		INDArray inData = Nd4j.create(in);
		INDArray outData = Nd4j.create(out);
		
		qNet.update(inData, outData);

		
		// 結果の表示
		// =============================
		{
			System.out.println("-- x0 ");
			int steps = 100;
			float[][] test = new float[steps + 1][nParam];
			for (int i = 0; i <= steps; i++) {
				test[i][0] = (float) i / (float) steps;
				for (int j = 1; j < nParam; j++) {
					test[i][j] = 0.5e0F;
				}
			}
			INDArray testData = Nd4j.create(test);
			INDArray res = qNet.getValues(testData);
			for (int i = 0; i <= steps; i++) {
				float s = testData.getFloat(i, 0);
				float v = res.getFloat(i, 0);
				System.out.println(s + "\t" + v);
			}
		}

		// =============================
		{
			System.out.println("-- x1 ");
			int steps = 10;
			float[][] test = new float[steps + 1][nParam];
			for (int i = 0; i <= steps; i++) {
				test[i][0] = 1.0e0F;
//				test[i][0] = 0.8e0F;
				test[i][1] = (float) i / (float) steps;
				for (int j = 1; j < nParam; j++) {
					if (j == 1) {
						test[i][j] = (float) i / (float) steps;						
					} else {
						test[i][j] = 0.5e0F;
					}
				}
			}
			INDArray testData = Nd4j.create(test);
			INDArray res = qNet.getValues(testData);

			for (int i = 0; i <= steps; i++) {
				float s = testData.getFloat(i, 1);
				float v = res.getFloat(i, 0);
				System.out.println(s + "\t" + v);
			}
		}
		
		// =============================
		{
			System.out.println("-- x2 ");
			int steps = 10;
			float[][] test = new float[steps + 1][nParam];
			for (int i = 0; i <= steps; i++) {
				test[i][0] = 1.0e0F;
//				test[i][0] = 0.8e0F;
				for (int j = 1; j < nParam; j++) {
					if (j == 2) {
						test[i][j] = (float) i / (float) steps;						
					} else {
						test[i][j] = 0.5e0F;
					}
				}
			}
			INDArray testData = Nd4j.create(test);
			INDArray res = qNet.getValues(testData);
			for (int i = 0; i <= steps; i++) {
				float s = testData.getFloat(i, 2);
				float v = res.getFloat(i, 0);
				System.out.println(s + "\t" + v);
			}
		}
		
		System.out.println("... Fin.");

		return;
	}
}
