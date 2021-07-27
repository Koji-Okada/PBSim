package jp.ac.tcu.okadak.project_reinforcement_learning;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class QNet {

	/**
	 * 
	 * 
	 * @param args
	 * @author K.Okada
	 */
	public static void main(final String[] args) {

		QNet tr = new QNet();
		tr.perform();
		
		return;
	}

	/**
	 * 
	 */
	Random rdm = new Random();

	/**
	 * 
	 */
	void perform() {
		System.out.println("Trial start ...");

		// ニューラルネット構成を定義する
		MultiLayerConfiguration nnConf = nnConfiguration();

		// ニューラルネットを生成する
		MultiLayerNetwork nn = new MultiLayerNetwork(nnConf);
		nn.init();
		nn.setListeners(new ScoreIterationListener(1));
		
		// データセットを生成する
		List<DataSet> list = generateData();

		// 学習させる
		int nEpochs = 10;
		DataSetIterator it = new ListDataSetIterator<DataSet>(list);
		for (int i = 0; i < nEpochs; i++) {
			it.reset();
			nn.fit(it);

			System.out.println("---- " + i);

			// 学習結果を確認する
			INDArray smpl = generateX();
//			List<INDArray> y = nn.feedForward(smpl);
			INDArray y = nn.output(smpl);

			System.out.println(y);
		}
		System.out.println("... Fin.");
	}

	/**
	 * 
	 * @return
	 */
	private MultiLayerConfiguration nnConfiguration() {
		double learningRate = 0.01e0D;
		double momentum = 0.90e0D;

		// 中間層を定義する
		DenseLayer.Builder ly1Bldr = new DenseLayer.Builder();
		ly1Bldr.nIn(9);
		ly1Bldr.nOut(5);
		ly1Bldr.activation(Activation.TANH);
		DenseLayer layer1 = ly1Bldr.build();

		// 出力層を定義する
		OutputLayer.Builder ly2Bldr = new OutputLayer.Builder();
		ly2Bldr.nIn(5);
		ly2Bldr.nOut(1);
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
	 * データセットの生成.
	 */
	private List<DataSet> generateData() {

		int cMax = 9;
		int rMax = 1024;

		// 入力側データを設定する
		double[][] data = new double[rMax][cMax];
		for (int i = 0; i < rMax; i++) {
			for (int j = 0; j < cMax; j++) {
				data[i][j] = (double) rdm.nextDouble();
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
		List<DataSet> list = allData.asList();
		Collections.shuffle(list, rdm);

		return list;
	}

	/**
	 * 
	 * @return
	 */
	private INDArray generateX() {

		int cMax = 9;
		int rMax = 16;

		// 入力側データを設定する
		double[][] data = new double[rMax][cMax];
		for (int i = 0; i < rMax; i++) {
			for (int j = 0; j < cMax; j++) {
				data[i][j] = (double) rdm.nextDouble();
			}
		}
		INDArray in = Nd4j.create(data);

		return in;
	}
}
