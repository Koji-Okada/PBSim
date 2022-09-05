package jp.ac.tcu.okadak.project_reinforcement_learning;
import com.vensim.Vensim;

/**
 * Vensim DSS シミュレータとの接続
 * 
 * @author K.Okada
 */
public class VensimLink {

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Start ...");

		VensimLink obj = new VensimLink();
		
		obj.prepare("InterfaceModel.vpmx", "base");	// 実行準備
		obj.process(13.0e0d, 24.0e0d, 20000000e0d, 0.8e0d);	// 実行
		obj.evaluate(60);
		
		System.out.println("... Fin.");
		return;
	}

	private Vensim vensim = new Vensim("vendll64") ; /* vendml64 for the minimal dll */
	private String simCaseName;

	/**
	 * 
	 * 
	 */
	void prepare(String modelName, String caseName) {
		int result;

		String vensimModelPath = "VensimModel/" + modelName;
		
		// SDモデルの読込み
		result = Vensim.command("SPECIAL>LOADMODEL|" + vensimModelPath);
		if (0 == result) {
			System.out.println("  Vensim model loading was failed!");
		}
		
		// シミュレーションケース名の設定
		result = Vensim.command("SIMULATE>RUNNAME|" + this.simCaseName);
		if (0 == result) {
			System.out.println("  Simulation Case Setting was failed!");
		}

		return;
	}
		
	/**
	 * 
	 * 
	 */
	void process(double pjStartTime, double pjCompTime, double pjTotalCost, double scopeChangeRate) {
		
		String cmdStr;
		int result;

		// インタフェース変数値の設定
		cmdStr = String.format("SIMULATE>SETVAL|ProjectCompletionTime = %5.2f", pjCompTime);
//		System.out.println(cmdStr);
		result = Vensim.command(cmdStr);
//		System.out.println("res:"+result);

		cmdStr = String.format("SIMULATE>SETVAL|TotalProjectCost =  %12.0f", pjTotalCost);
//		System.out.println(cmdStr);
		result = Vensim.command(cmdStr);
//		System.out.println("res:"+result);
		
		cmdStr = String.format("SIMULATE>SETVAL|ScopeChangeRate = %6.5f", scopeChangeRate);
//		System.out.println(cmdStr);
		result = Vensim.command(cmdStr);
//		System.out.println("res:"+result);

		// シミュレーションの実行
		result = Vensim.command("MENU>RUN|O");
//		System.out.println("  simulation run. " + result);

		return;
	}
	
	/**
	 * 
	 * @return
	 */
	double evaluate(int evaluationTime) {
		
		int tPoints = 1024;
		float val[] = new float[tPoints];
		float tval[]  = new float[tPoints];

		int result;

		// シミュレーション結果の確認
		result = Vensim.get_data(simCaseName + ".vdfx", "EvaluatedReward", "Time", val, tval, tPoints);

//		System.out.println("  simulation results. " + result );
//		for (int i = 0; i <= 100 ; i++) {
//			System.out.println(i + " : " + val[i] + " : " + tval[i]);
//		}
		
//		System.out.println("reward = " + val[evaluationTime] + " : " + tval[evaluationTime]);
		
		return (double)val[evaluationTime];
	}
}
