package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.ExecutionTimer;


public class OppTypeReasonDao  extends AuthBatchDbHandler{

	public OppTypeReasonDao() throws Exception {
		// TODO Auto-generated constructor stub
	}

	public static ResultSet getOppTypeReason(String sP_OppType, String sP_OppStatus, String sP_NccOppTypeDefValue, String sP_NegOppReasonDefValue) {
		//用在 ECS004
		
		ResultSet L_ResultSet = null;
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		String sL_Sql = "select NVL(NCC_OPP_TYPE, ? ) as NccOppType ,NVL(NEG_OPP_REASON, ? ) as NegOppReason, "
                		+"NVL(VIS_EXCEP_CODE,'  ') as VisExcepCode,NVL(MST_AUTH_CODE,'O') as MstAuthCode, "
                		+"NVL(JCB_EXCP_CODE,'  ') as JcbExcpCode from CCA_OPP_TYPE_REASON " //,NVL(JOCS_CODE,'  ') =>新系統無此欄位
                		//+"NVL(AE_EXCP_CODE,' ')"  =>新系統無此欄位
						+ " WHERE ONUS_OPP_TYPE = ? "
						+ "AND OPP_STATUS = ? ";
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_NccOppTypeDefValue);  //"3"
			ps.setString(2, sP_NegOppReasonDefValue);			
			ps.setString(3, sP_OppType);
			ps.setString(4, sP_OppStatus);



			
			
		
			
			L_ResultSet = ps.executeQuery();
			
			

			
		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet = null;
		}
		L_Timer.end();
		System.out.println("Timer : " + L_Timer.duration());
		
		return L_ResultSet;
	}


}
