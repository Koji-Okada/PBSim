package jp.ac.tcu.okadak.project_reinforcement_learning;

import java.util.Random;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 *
 * @author K.Okada
 */
public class QNet {

	/**
	 *
	 */
	private MultiLayerNetwork qNet;
	private int[] inNodeLevels; // 入力ノードの分解能の定義
	private int[] inNodeEncodings; // エンコード仕様の定義

	// ビット演算可能な形式
	static int NORMAL = 0;
	static int TOP_BOUNDARY_PLUS = 1;
	static int BOTTOM_BOUNDARY_PLUS = 2;
	static int DIFFERENTIAL = 4;

	private int nInNodes; // 入力ノード数(内部)
	private Random rdm = new Random();
	MultiLayerConfiguration nnConf;

	private int initDataSize = 256 * 8;

	// エポック数
//	private int nEpochsInitialize = 256;
//	private int nEpochsUpdate = 256;
	private int nEpochsUpdate = 1024 * 2;

	/**
	 *
	 *
	 * @param args
	 * @author K.Okada
	 */
	public static void main(final String[] args) {

		System.out.println("Start QNet ...");

		QNet qNetObj = new QNet();

		int[] nodeLevels = { 10, 4, 4, 4, 4, 4, 4, 4, 4 };
//		int[] nodeLevels = { 1, 1, 1, 1, 1, 1, 1, 1, 1 };

		int tb = TOP_BOUNDARY_PLUS;
		int nr = NORMAL;
		int[] encordings = { tb, nr, nr, nr, nr, nr, nr, nr, nr };

		qNetObj.generate(nodeLevels, encordings); // ニューラルネットを生成する

		qNetObj.initialize(); // 初期化する

		System.out.println("... Fin.");

		return;
	}

	/**
	 * Q-Netを生成する.
	 *
	 * @return QNet本体
	 */

	/**
	 * Q-Netを生成する.
	 * 
	 * @param inNodes 入力ノードの分解能の定義
	 */
	void generate(int[] inNodes, int[] encodings) {

		this.inNodeLevels = inNodes;
		this.inNodeEncodings = encodings;

		// ニューラルネット構成を定義する
		this.nnConf = nnConfiguration();

		// ニューラルネットを生成する
		this.qNet = new MultiLayerNetwork(nnConf);
		qNet.init();
//		qNet.setListeners(new ScoreIterationListener(1));

		return;
	}

	/**
	 * 
	 * @param dimensions
	 * @return
	 */
	double initialize() {

		int cMax = inNodeLevels.length;
		int rMax = initDataSize;

		// 入力側データを設定する
		float[][] data = new float[rMax][cMax];
		for (int i = 0; i < rMax; i++) {
			for (int j = 0; j < cMax; j++) {
				data[i][j] = sampleX();
			}
		}
		INDArray inData = Nd4j.create(data);

		// 出力側データを設定する
		float[][] res = new float[rMax][1];
		for (int i = 0; i < rMax; i++) {
			res[i][0] = 0.0e0F;
		}
		INDArray outData = Nd4j.create(res);

		// 初期化用データを使って学習する
		double score = update(inData, outData);

		return score;
	}

	/**
	 * ネットワーク構成を定義する.
	 *
	 * @return ネット構成の定義.
	 */
	private MultiLayerConfiguration nnConfiguration() {

		// 入力分解能を考慮する
		nInNodes = 0;
		for (int i = 0; i < inNodeLevels.length; i++) {
			int en = inNodeEncodings[i];
			nInNodes += inNodeLevels[i];

			if (DIFFERENTIAL == (DIFFERENTIAL & en)) {
				// 微分型(2-Hot)の場合
				nInNodes++;
			}
			if (TOP_BOUNDARY_PLUS == (TOP_BOUNDARY_PLUS & en)) {
				// 上限境界ノードを置く場合
				nInNodes++;
			}
			if (BOTTOM_BOUNDARY_PLUS == (BOTTOM_BOUNDARY_PLUS & en)) {
				// 下限境界ノードを置くの場合
				nInNodes++;
			}

		}

		System.out.println("Input nodes = " + nInNodes);

		int nMidNodes = nInNodes;
//		int nMidNodes = nInNodes % 2 + 1; // 半分に絞る
//		int nMidNodes = nInNodes * 2; // 倍に拡げる

		int nOutNodes = 1;

		// 中間層を定義する
		DenseLayer.Builder ly1Bldr = new DenseLayer.Builder();
		ly1Bldr.nIn(nInNodes);
		ly1Bldr.nOut(nMidNodes);
		ly1Bldr.activation(Activation.TANH);
//		ly1Bldr.activation(Activation.LEAKYRELU);
		DenseLayer layer1 = ly1Bldr.build();

		// 出力層を定義する
		OutputLayer.Builder ly2Bldr = new OutputLayer.Builder();
		ly2Bldr.nIn(nMidNodes);
		ly2Bldr.nOut(nOutNodes);
		ly2Bldr.activation(Activation.IDENTITY);
		ly2Bldr.lossFunction(LossFunctions.LossFunction.MSE);

		OutputLayer layer2 = ly2Bldr.build();

		// ニューラルネットワーク構成を定義する
		NeuralNetConfiguration.Builder nBldr = new NeuralNetConfiguration.Builder();
		nBldr.seed(0);
//		nBldr.weightInit(WeightInit.RELU);
		nBldr.weightInit(WeightInit.XAVIER);
		nBldr.updater(new Adam());

		MultiLayerConfiguration nnConf = nBldr.list().layer(layer1).layer(layer2).build();

		return nnConf;
	}

	/**
	 * サンプル値(スカラー)を取得する.
	 *
	 * @param ext
	 * @return
	 */
	float sampleX() {
		return rdm.nextFloat();
	}

	/**
	 * データを与え Q-Netを更新する.
	 *
	 * @param data
	 * @return
	 */
	double update(INDArray in, INDArray out) {

		qNet = new MultiLayerNetwork(nnConf); // init()が効いていないようなので、作り直している
		qNet.init();

		INDArray transIn = transInData(in);
		INDArray transOut = transOutData(out);
		double score = normarizedUpdate(transIn, transOut);

		return score;
	}

	/**
	 * 
	 * @param in
	 * @param out
	 * @return
	 */
	private double normarizedUpdate(INDArray in, INDArray out) {

		int nEpochs = nEpochsUpdate;
		DataSet data = new DataSet(in, out);

		double score = 1.0e3D; // 大きめの値から
		double preScore = 2.0e3D;
		while (score > 1.0e-5D) {

			for (int i = 0; i < nEpochs; i++) {
				data.shuffle();
				qNet.fit(data); // 学習する
			}
			preScore = score;
			score = qNet.score(data); // テストする

			System.out.println("sc = " + score);

			if ((preScore - score) / score < 1.0e-3D) {
				System.out.println("..Break");
				break;
			}
		}

		return score;
	}

	/**
	 * Q関数を使って値を求める.
	 *
	 * @param in 入力値
	 * @return 出力値
	 */
	INDArray getValues(INDArray in) {

		INDArray transIn = transInData(in);
		INDArray out = qNet.output(transIn);
		INDArray inv = invOutData(out);
		return inv;
	}

	/**
	 * 
	 * @param in
	 * @return
	 */
	private INDArray transInData(INDArray in) {

		int nSamples = (int) in.size(0);
		float[][] transData = new float[nSamples][nInNodes];

//		System.out.println("[ " + nInNodes + " : " + nSamples + " ]");

		for (int i = 0; i < nSamples; i++) {
			int cnt = 0;
			for (int j = 0; j < inNodeLevels.length; j++) {
				float v = in.getFloat(i, j);
				int sep = inNodeLevels[j];
				int encoding = inNodeEncodings[j];

				float[] trans = transInValue(sep, encoding, v);

				for (int k = 0; k < trans.length; k++, cnt++) {
					transData[i][cnt] = trans[k];
				}
			}
		}

		INDArray transIn = Nd4j.create(transData);

		return transIn;
	}

	/**
	 * 
	 * 
	 * @param n
	 * @param topSideFlag
	 * @param in
	 * @return
	 */
	float[] transInValue(int n, int encoding, float in) {

		float th = 0.999999e0F; // 閾値
		int size = n;

		if (DIFFERENTIAL == (DIFFERENTIAL & encoding)) {
			// 微分型(2-Hot)の場合
			size++;
		}
		if (TOP_BOUNDARY_PLUS == (TOP_BOUNDARY_PLUS & encoding)) {
			// 上限境界ノードを置く場合
			size++;
		}
		if (BOTTOM_BOUNDARY_PLUS == (BOTTOM_BOUNDARY_PLUS & encoding)) {
			// 下限境界ノードを置くの場合
			size++;
		}

		float[] out = new float[size];

		// 入力値を上下限値で制限する
		float limitedIn = in;
		if (in < 0.0e0F) {
			limitedIn = 0.0e0F;
		} else if (in > 1.0e0F) {
			limitedIn = 1.0e0F;
		}

		float m = (limitedIn * (float) n);
		int k = (int) m;

		int cnt = 0;
		if (BOTTOM_BOUNDARY_PLUS == (BOTTOM_BOUNDARY_PLUS & encoding)) {
			cnt++;
		}
		if (DIFFERENTIAL == (DIFFERENTIAL & encoding)) {
			out[cnt++] = 1.0e0F;
		}

		for (int i = 0; i < n; i++, cnt++) {
			float x;
			if (i < k) {
				x = 1.0e0F;
			} else if (i > k) {
				x = 0.0e0F;
			} else {
				x = m - (float) k;
			}
			out[cnt] = x;

			if (DIFFERENTIAL == (DIFFERENTIAL & encoding)) {
				out[cnt - 1] -= x;
			}
		}

		if (TOP_BOUNDARY_PLUS == (TOP_BOUNDARY_PLUS & encoding)) {
			if (out[cnt - 1] > th) {
				out[cnt] = 1.0e0F;
			}
		}
		if (BOTTOM_BOUNDARY_PLUS == (BOTTOM_BOUNDARY_PLUS & encoding)) {
			if (out[1] > th) {
				out[0] = 1.0e0F;
			}
		}

		if (true) {
			System.out.printf("%6.5f => ", in);
			for (int i = 0; i < out.length; i++) {
				System.out.printf("%5.4f, ", out[i]);
			}
			System.out.println();
		}

		return out;
	}

	private float transRange;
	private float transShift;

	/*
	 * 
	 */
	private INDArray transOutData(INDArray out) {

		float max = out.max(0).getFloat(0, 0);
		float min = out.min(0).getFloat(0, 0);

		transRange = (max - min) / 2.0e0F;
		transShift = (max + min) / 2.0e0F;

		int nSamples = (int) out.size(0);
		float[][] transData = new float[nSamples][1];
		for (int i = 0; i < nSamples; i++) {
			float x = out.getFloat(i, 0);
			if (transRange >= 1.0e-6F) {
				transData[i][0] = (x - transShift) / transRange;
			} else {
				transData[i][0] = 0.5e0F;
			}
		}
		INDArray transOut = Nd4j.create(transData);

		return transOut;
	}

	/*
	 * 
	 */
	private INDArray invOutData(INDArray out) {

		int nSamples = (int) out.size(0);
		float[][] invData = new float[nSamples][1];

		for (int i = 0; i < nSamples; i++) {
			float z = out.getFloat(i, 0);
			if (transRange >= 1.0e-6F) {
				invData[i][0] = z * transRange + transShift;
			} else {
				invData[i][0] = transShift;
			}
		}
		INDArray invOut = Nd4j.create(invData);

		return invOut;
	}
}
