package bank.authbatch.main;

public class BatchControler {

	
	public BatchControler() {
		// TODO Auto-generated constructor stub
	}

	
	private void preProcess(Object P_TargetObj) {
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//System.out.println("---" + args[0] + "===");

		
		Thread t1 = new Thread(new Runnable() {
			public void run() {
			
				if ("050".equals(args[0])) {
					AuthBatch050 L_050;
					try {
						L_050 = new AuthBatch050();
						L_050.startProcess(args);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}
				else if ("060OVER30".equals(args[0].toUpperCase())) {
					AuthBatch060Over30 L_060Over30;
					try {
						L_060Over30 = new AuthBatch060Over30();
						L_060Over30.startProcess(args);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}

				else if ("080".equals(args[0])) {
					AuthBatch080 L_080;
					try {
						L_080 = new AuthBatch080();
						L_080.startProcess(args);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
						
				}
				else if ("120".equals(args[0])) {
					AuthBatch120 L_120;
					try {
						L_120 = new AuthBatch120();
						
						
						L_120.startProcess(args);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
						
				}

				else if ("500".equals(args[0])) {
					AuthBatch500 L_500;
					try {
						L_500 = new AuthBatch500();
						L_500.startProcess(args);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
							
							
					
				}
				
				else if ("100".equals(args[0])) {
					AuthBatch100 L_100;
					try {
						L_100 = new AuthBatch100();
						L_100.startProcess(args);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}
				else if ("004".equals(args[0])) {
					AuthBatch004 L_004;
					try {
						L_004 = new AuthBatch004();
						L_004.startProcess(args);
						//L_004.testInsert();
						/*
						String sL_MsgHeader="MH"; 
						String sL_MsgType="3333";
						String sL_CardNo = "12345";
						L_004.testUpdate(sL_MsgHeader, sL_MsgType, sL_CardNo);
						*/
						/*
						String sL_CardNo = "12345";
						//L_004.testSelect(sL_CardNo);
						//L_004.testDelete(sL_CardNo);
						
						*/
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}
				else if ("AUI070".equals(args[0])) {
					AuthBatchAui070 L_Aui070;
					try {
						L_Aui070 = new AuthBatchAui070();
						L_Aui070.startProcess(args);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
						
				}    	
				else if ("AUO030".equals(args[0])) {
					AuthBatchAuo030 L_Auo030;
					try {
						L_Auo030 = new AuthBatchAuo030();
						L_Auo030.startProcess(args);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
						
				}

				else if ("AUICLEARDB".equals(args[0])) {
					AuthBatchAuiClearDb L_AuiClearDb;
					try {
						L_AuiClearDb = new AuthBatchAuiClearDb();
						L_AuiClearDb.startProcess(args);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
						
				}    	
				    	
				    	
			}});  
			t1.start();
				    
				    
				    
			
	}

}
