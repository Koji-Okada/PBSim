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
		obj.process();	// 実行
		obj.evaluate();
		
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
		System.out.println(result);
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
	void process() {
		
		int result;
				
		// インタフェース変数値の設定
		result = Vensim.command("SIMULATE>SETVAL|ProjectCompletionTime = 10");
		System.out.println("  set val <ProjectCompletionTime>. " + result);
		result = Vensim.command("SIMULATE>SETVAL|TotalProjectCost = 20000000");
		System.out.println("  set val <TotalProjectCost>. " + result);
		result = Vensim.command("SIMULATE>SETVAL|ScopeChangeRate = 0.8");
		System.out.println("  set val <ScopeChangeRate>. " + result);

		
		// シミュレーションの実行
		result = Vensim.command("MENU>RUN|O");
		System.out.println("  simulation run. " + result);

	}
	
	private double evaluate() {
		
		int tPoints = 1024;
		float val[] = new float[tPoints];
		float tval[]  = new float[tPoints];

		int result;

		// シミュレーション結果の確認
//		result = Vensim.get_data(simCaseName + ".vdfx", "Capability", "Time", val, tval, 101);
//		result = Vensim.get_data(simCaseName + ".vdfx", "EvaluatedReward", "Time", val, tval, 101);
		result = Vensim.get_data(simCaseName + ".vdfx", "CapabilityImprovement", "Time", val, tval, 101);

		System.out.println("  simulation results. " + result );
		for (int i = 0; i <= 100 ; i++) {
			System.out.println(i + " : " + val[i] + " : " + tval[i]);
		}
		
		return 0.0e0d;
	}
}
