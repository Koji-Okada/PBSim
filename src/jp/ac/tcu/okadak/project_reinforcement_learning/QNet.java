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
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
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
	private Random rdm = new Random();

	
	/**
	 *
	 *
	 * @param args
	 * @author K.Okada
	 */
	public static void main(final String[] args) {

		System.out.println("Start QNet ...");

		QNet qNetObj = new QNet();
		qNetObj.init(9);

		double[][] tmp0 = {{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}};
		double[][] tmp5 = {{0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}};
		double[][] tmp10 = {{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}};

		INDArray index0 = Nd4j.create(tmp0);
		double ans0 = qNetObj.getValue(index0);

		INDArray index5 = Nd4j.create(tmp5);
		double ans5 = qNetObj.getValue(index5);

		INDArray index10 = Nd4j.create(tmp10);
		double ans10 = qNetObj.getValue(index10);
		
		System.out.println("ans0  = " + ans0);
		System.out.println("ans5  = " + ans5);
		System.out.println("ans10 = " + ans10);

		System.out.println("... Fin.");

		return;
	}
	
	
	/**
	 * Q-Netを生成する.
	 *
	 * @return QNet本体
	 */
	double init(int dimensions) {

		// ニューラルネット構成を定義する
		MultiLayerConfiguration nnConf = nnConfiguration(dimensions);

		// ニューラルネットを生成する
		qNet = new MultiLayerNetwork(nnConf);
		qNet.init();
//		qNet.setListeners(new ScoreIterationListener(1));

		// データセットを生成する
		DataSet allData = initialData(dimensions);

		// 学習する
		int nEpochs = 256;
		double score = 0.0e0D;
		for (int i = 0; i < nEpochs; i++) {

			// 訓練用データとテスト用データに分割する
//			SplitTestAndTrain splitData = allData.splitTestAndTrain(256 * 7, rdm);
			SplitTestAndTrain splitData = allData.splitTestAndTrain(0.8e0D);
			DataSet trainData = splitData.getTrain();
			DataSet testData = splitData.getTest();

			qNet.fit(trainData);	// 学習する
			score = qNet.score(testData);	// テストする

			System.out.println("---- " + i + " :\t" + score);
		}
		return score;
	}

	/**
	 *
	 * ネットトポロジーの定義.
	 *
	 * @return ネットトポロジー.
	 */
	private MultiLayerConfiguration nnConfiguration(int dimensions) {
		double learningRate = 0.01e0D;
		double momentum = 0.90e0D;
		int nInNodes = dimensions;
		int nMidNodes = nInNodes % 2 + 1;	// 半分に絞る
		int nOutNodes = 1;
		
		// 中間層を定義する
		DenseLayer.Builder ly1Bldr = new DenseLayer.Builder();
		ly1Bldr.nIn(nInNodes);
		ly1Bldr.nOut(nMidNodes);
		ly1Bldr.activation(Activation.TANH);
		DenseLayer layer1 = ly1Bldr.build();

		// 出力層を定義する
		OutputLayer.Builder ly2Bldr = new OutputLayer.Builder();
		ly2Bldr.nIn(nMidNodes);
		ly2Bldr.nOut(nOutNodes);
		ly2Bldr.activation(Activation.IDENTITY);
		ly2Bldr.lossFunction(LossFunctions.LossFunction.MSE);
		OutputLayer layer2 = ly2Bldr.build();

		// ニューラルネット構成を定義する
		NeuralNetConfiguration.Builder nBldr = new NeuralNetConfiguration.Builder();
		nBldr.seed(0);
//		nBldr.weightInit(WeightInit.RELU);
		nBldr.weightInit(WeightInit.XAVIER);
		nBldr.updater(new Nesterovs(learningRate, momentum));
//		nBldr.updater(new Adam());

		MultiLayerConfiguration nnConf = nBldr.list().layer(layer1).layer(layer2).build();

		return nnConf;
	}


	/**
	 *
	 * データセットの生成.
	 *
	 * @return データセット
	 */
	private DataSet initialData(int dimensions) {

		int cMax = dimensions;
		int rMax = 256 * 8;

		// 入力側データを設定する
		double[][] data = new double[rMax][cMax];
		for (int i = 0; i < rMax; i++) {
			for (int j = 0; j < cMax; j++) {
				data[i][j] = (double) rdm.nextDouble() * 2.0e0d - 0.5e0d;	// 端部の精度を確保するため、領域を広めに取る
			}
		}
		INDArray in = Nd4j.create(data);

		// 出力側データを設定する
		double[][] res = new double[rMax][1];
		for (int i = 0; i < rMax; i++) {
			res[i][0] = 0.0e0D;
		}
		INDArray out = Nd4j.create(res);

		// データセットとして整える
		DataSet allData = new DataSet(in, out);

		return allData;
	}
	
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	double update(DataSet data) {
				
		// 学習する
		int nEpochs = 256;
		double score = 1.0e3D;
		for (int i = 0; i < nEpochs; i++) {

			// 訓練用データとテスト用データに分割する
			SplitTestAndTrain splitData = data.splitTestAndTrain(0.8e0D);
			DataSet trainData = splitData.getTrain();
			DataSet testData = splitData.getTest();

			qNet.fit(trainData);	// 学習する
			score = qNet.score(testData);	// テストする

			System.out.println("---- " + i + " :\t" + score);
		}

		
		return score;
	}
	
	
	/**
	 * Q関数を使って値を求める.
	 * 
	 * @param in 入力値
	 * @return 出力値
	 */
	double getValue(INDArray in) {
				
		INDArray out = qNet.output(in);
		return out.getDouble(0);
	}
}
