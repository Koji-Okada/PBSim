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
	private int[] inNodeLevels; // 入力データの次元数
	int nInNodes; // 入力ノード数(内部)
	private Random rdm = new Random();

	private int initDataSize = 256 * 8;

	// 入力値の広さ
//	private double extSpace = 2.0e0D;
	private double extSpace = 1.0e0D;

	// エポック数
//	private int nEpochsInitialize = 256;
//	private int nEpochsUpdate = 256;
	private int nEpochsUpdate = 1024;

	/**
	 *
	 *
	 * @param args
	 * @author K.Okada
	 */
	public static void main(final String[] args) {

		System.out.println("Start QNet ...");

		QNet qNetObj = new QNet();
//		int[] inNodeLevels = { 10, 4, 4, 4, 4, 4, 4, 4, 4 };
		int[] inNodeLevels = { 1, 1, 1, 1, 1, 1, 1, 1, 1 };

		
		qNetObj.generate(inNodeLevels); // ニューラルネットの生成
		
		
		qNetObj.initializeAndTest(); // 初期化

		System.out.println("... Fin.");

		return;
	}

	/**
	 * Q-Netを生成する.
	 *
	 * @return QNet本体
	 */
	void generate(int[] levels) {

		inNodeLevels = levels;

		// ニューラルネット構成を定義する
		MultiLayerConfiguration nnConf = nnConfiguration(levels);

		// ニューラルネットを生成する
		qNet = new MultiLayerNetwork(nnConf);
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
		double[][] data = new double[rMax][cMax];
		for (int i = 0; i < rMax; i++) {
			for (int j = 0; j < cMax; j++) {
				data[i][j] = sampleX();
			}
		}
		INDArray inData = Nd4j.create(data);

		// 出力側データを設定する
		double[][] res = new double[rMax][1];
		for (int i = 0; i < rMax; i++) {
			res[i][0] = 0.0e0D;
		}
		INDArray outData = Nd4j.create(res);
		
		// 初期化用データを使って学習する
		double score = update(inData, outData);
		
		return score;
	}

	/**
	 * 
	 * @param dimensions
	 * @return
	 */
	double initializeAndTest() {

		int cMax = inNodeLevels.length;
		int rMax = initDataSize;

		// 入力側データを設定する
		double[][] data = new double[rMax][cMax];
		for (int i = 0; i < rMax; i++) {
			for (int j = 0; j < cMax; j++) {
				data[i][j] = sampleX();
			}
		}
		INDArray inData = Nd4j.create(data);

		// 出力側データを設定する
		double[][] res = new double[rMax][1];
		for (int i = 0; i < rMax; i++) {
			res[i][0] = 0.0e0D;
		}
//		INDArray outData = Nd4j.create(res);
		
		// テスト用
		int num = 12;
		int num2 = 3;
		double[][] test = new double[num][9];
		for (int i = 0; i < num; i++) {
			for (int j = 0; j < 9; j++) {
				test[i][j] = sampleX();
				if (i < num2) {
					test[i][j] = data[i][j];
				}
			}
			if (i < num2) {
				res[i][0] = -500.0e0;
			}
		}
		INDArray outData = Nd4j.create(res);

		
		// 初期化用データを使って学習する
		double score = update(inData, outData);

		INDArray testData = Nd4j.create(test);
		INDArray confirm = getValues(testData);
		for (int i = 0; i < num; i++) {
			System.out.println(" -- " + confirm.getDouble(i, 0));
		}
		
		return score;
	}
	
	
	/**
	 * ネットワーク構成を定義する.
	 *
	 * @return ネット構成の定義.
	 */
	private MultiLayerConfiguration nnConfiguration(int[] levels) {
//		double learningRate = 0.01e0D;
//		double momentum = 0.90e0D;

		// 入力分解レベルを考慮する
		nInNodes = 0;
		for (int i = 0; i < levels.length; i++) {
			nInNodes += levels[i];
		}

		System.out.println("Input nodes = " + nInNodes);

		int nMidNodes = nInNodes % 2 + 1; // 半分に絞る
		int nOutNodes = 1;

		// 中間層を定義する
		DenseLayer.Builder ly1Bldr = new DenseLayer.Builder();
		ly1Bldr.nIn(nInNodes);
		ly1Bldr.nOut(nMidNodes);
		ly1Bldr.activation(Activation.TANH);
//		ly1Bldr.activation(Activation.RELU);
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
//		nBldr.updater(new Nesterovs(learningRate, momentum));
		nBldr.updater(new Adam());

		MultiLayerConfiguration nnConf = nBldr.list().layer(layer1).layer(layer2).build();

		return nnConf;
	}

	/**
	 * 広がりを考慮してサンプル値(スカラー)を取得する.
	 *
	 * @param ext
	 * @return
	 */
	double sampleX() {

		return rdm.nextDouble() * extSpace - ((extSpace - 1.0e0D) / 2.0e0D);

	}

	/**
	 * データを与え Q-Netを更新する.
	 *
	 * @param data
	 * @return
	 */
	double update(INDArray in, INDArray out) {

		qNet.init();
		
		INDArray transIn = transInData(in);
		double score = planeUpdate(transIn, out);

		return score;
	}

	/**
	 * 
	 * @param in
	 * @param out
	 * @return
	 */
	private double planeUpdate(INDArray in, INDArray out) {
		DataSet data = new DataSet(in, out);

		// 学習する
		int nEpochs = nEpochsUpdate;

		double score = 1.0e3D; // 大きめの値から
		double preScore = 1.2e3D;
		while ((score > 1.0e0D) && (preScore - score > 0.01e0D)) {
			for (int i = 0; i < nEpochs; i++) {
				data.shuffle();
				qNet.fit(data); // 学習する
			}
			preScore = score;
			score = qNet.score(data); // テストする
			System.out.println(score);
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
		return out;
	}
	
	/**
	 * 
	 * @param in
	 * @return
	 */
	private INDArray transInData(INDArray in) {
	
		int nSamples = (int) in.size(0);
		double[][] transData = new double[nSamples][nInNodes];

//		System.out.println("[ " + nInNodes + " : " + nSamples + " ]");

		int cnt;
		for (int i = 0; i < nSamples; i++) {
			cnt = 0;
			for (int j = 0; j < inNodeLevels.length; j++) {
				double v = in.getDouble(i, j);
				int sep = inNodeLevels[j];
//				System.out.printf("%5.4f: ", v);
				
				// エンコーディング
				double st = 1.0e0D / (double)sep;
				for (int k = 0; k < sep ; k++) {
					double tr;
					double s0 = (double)k * st;
					double s1 = (double)(k+1) * st; 
					if (v < s0) {
						tr = 0.0e0D;
					} else if (v > s1) {
						tr = 1.0e0D;
					} else {
						tr = (v - s0) * (double)sep;
					}
					transData[i][cnt++] = tr;
					
//					System.out.printf("%5.4f, ", tr);
				}
//				System.out.println();
			}
		}

		INDArray transIn = Nd4j.create(transData);
		
		return transIn;
		
	}
	
	
}
