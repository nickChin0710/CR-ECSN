package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.TextStyle;


import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.HpeUtil;


public class CcaMchtBaseDao extends AuthBatchDbHandler{

	static String sG_MchtName="", sG_MchtNo="", sG_ZipCode="";
	static String sG_City="", sG_Address="", sG_TelNo="",sG_ContractType="",sG_BankNo="";
	static String sG_PosFlag="",sG_EdcFlag="",sG_IcFlag="";
	static String sG_NccContrStartDate="",sG_NccContrEndDate="", sG_NccRiskLevel="";
	static String sG_VisContrStartDate="",sG_VisContrEndDate="", sG_VisRiskLevel="";
	static String sG_MstContrStartDate="",sG_MstContrEndDate="", sG_MstRiskLevel="";
	static String sG_JcbContrStartDate="",sG_JcbContrEndDate="", sG_JcbRiskLevel="";
	static String sG_MccCode="", sG_CurrentCode="";
	static String sG_AcqBankId="493817";
	static int nG_NccCFloorLimit=0,nG_NccGFloorLimit=0;
	static int nG_VisCFloorLimit=0,nG_VisGFloorLimit=0;
	static int nG_MstCFloorLimit=0,nG_MstGFloorLimit=0;
	static int nG_JcbCFloorLimit=0,nG_JcbGFloorLimit=0;

	static String sG_InsertCmd = "insert into CCA_MCHT_BASE(MCHT_NO,MCHT_NAME,ZIP_CODE,ZIP_CITY,MCHT_ADDR,TEL_NO,CONTR_TYPE,"
						+"BANK_NO,POS_FLAG,EDC_FLAG,IC_FLAG,NCC_CONTR_DATE,NCC_CONTR_END_DATE,NCC_RISK_LEVEL,VIS_CONTR_DATE,"
						+"VIS_CONTR_END_DATE,VIS_RISK_LEVEL,MST_CONTR_DATE,MST_CONTR_END_DATE,MST_RISK_LEVEL,JCB_CONTR_DATE,"
						+"JCB_CONTR_END_DATE,JCB_RISK_LEVEL,MCC_CODE,CURRENT_CODE,ACQ_BANK_ID,NCC_CFLOOR_LIMIT,NCC_GFLOOR_LIMIT,"
						+"VIS_CFLOOR_LIMIT,VIS_GFLOOR_LIMIT,MST_CFLOOR_LIMIT,MST_GFLOOR_LIMIT,JCB_CFLOOR_LIMIT,"
						+"JCB_GFLOOR_LIMIT,CRT_DATE,CRT_USER)" 
						+"values( ? , ? , ? , ? , ? , ? , ? , ? ,"
							+" ? , ? , ? , "
							+" ? , ? , ? ,"
							+": ? , ? , ? , ? ,: ? ,"
							+" ? , ? , ? , ? ,"
							+" ? , ? , ? ,"
							+" ? , ? , ? , ? , ? ,"
							+": ? , ? , ? , ? , ?  )"; //共36個欄位
	
	static String sG_UpdateCmd = "update CCA_MCHT_BASE set "
							+" MCHT_NAME= ? ,ZIP_CODE= ? ,ZIP_CITY= ? ,MCHT_ADDR= ? ,TEL_NO= ? ,CONTR_TYPE= ? ,BANK_NO= ? ,"
							+"POS_FLAG= ? ,EDC_FLAG= ? ,IC_FLAG= ? ,NCC_CONTR_DATE= ? ,NCC_CONTR_END_DATE= ? ,NCC_RISK_LEVEL= ? ,"
							+"VIS_CONTR_DATE= ? ,VIS_CONTR_END_DATE= ? ,VIS_RISK_LEVEL= ? ,MST_CONTR_DATE= ? ,MST_CONTR_END_DATE= ? ,"
							+"MST_RISK_LEVEL= ? ,JCB_CONTR_DATE= ? ,JCB_CONTR_END_DATE= ? ,JCB_RISK_LEVEL= ? ,MCC_CODE= ? ,CURRENT_CODE= ? ,"
							+"NCC_CFLOOR_LIMIT= ? ,NCC_GFLOOR_LIMIT= ? ,VIS_CFLOOR_LIMIT= ? ,VIS_GFLOOR_LIMIT= ? ,MST_CFLOOR_LIMIT= ? ,"
							+"MST_GFLOOR_LIMIT=:pMstGFloorLimit,JCB_CFLOOR_LIMIT=:pJcbCFloorLimit,JCB_GFLOOR_LIMIT=:pJcbGFloorLimit "
							+"where ACQ_BANK_ID= ?  and MCHT_NO= ?  " ;
	
	static String sG_SelectFields = " MCHT_NAME,ZIP_CODE,ZIP_CITY,MCHT_ADDR,TEL_NO,CONTR_TYPE,BANK_NO,"
								+"POS_FLAG,EDC_FLAG,IC_FLAG,NCC_CONTR_DATE,NCC_CONTR_END_DATE,NCC_RISK_LEVEL,"
								+"VIS_CONTR_DATE,VIS_CONTR_END_DATE,VIS_RISK_LEVEL,MST_CONTR_DATE,MST_CONTR_END_DATE,"
								+"MST_RISK_LEVEL,JCB_CONTR_DATE,JCB_CONTR_END_DATE,JCB_RISK_LEVEL,MCC_CODE,CURRENT_CODE,"
								+"NCC_CFLOOR_LIMIT,NCC_GFLOOR_LIMIT,VIS_CFLOOR_LIMIT,VIS_GFLOOR_LIMIT,MST_CFLOOR_LIMIT,"
								+"MST_GFLOOR_LIMIT,JCB_CFLOOR_LIMIT,JCB_GFLOOR_LIMIT ";

	static String sL_SelectCmd = "select " + sG_SelectFields + " from CCA_MCHT_BASE where ACQ_BANK_ID= ? and MCHT_NO= ? "; 
	
	static PreparedStatement G_SelectPs = null;
	static PreparedStatement G_InsertPs = null;
	static PreparedStatement G_UpdatePs = null;
	
	public CcaMchtBaseDao() throws Exception{
		// TODO Auto-generated constructor stub
	}

	public static void initPs() {
		try {
			if (null == G_SelectPs)
				//G_SelectPs = new NamedParamStatement(Db2Connection, sL_SelectCmd);
				G_SelectPs = getPreparedStatement(sL_SelectCmd, true);

			if (null == G_InsertPs)
				//G_InsertPs = new NamedParamStatement(Db2Connection, sG_InsertCmd);
				G_InsertPs = getPreparedStatement(sG_InsertCmd, true);

			
			if (null == G_UpdatePs)
				//G_UpdatePs = new NamedParamStatement(Db2Connection, sG_UpdateCmd);
				G_UpdatePs = getPreparedStatement(sG_UpdateCmd,false);

		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	public static void closePs() {
		try {
			if (null != G_SelectPs)
				G_SelectPs.close();

			if (null != G_InsertPs)
				G_InsertPs.close();

			
			if (null != G_UpdatePs)
				G_UpdatePs.close();

		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	private static String getData(String sP_SrcData, int nP_BeginIndx, int nP_EndIndex) {
		String sL_Result =sP_SrcData.substring(nP_BeginIndx, nP_EndIndex);
		sL_Result = sL_Result.replace("　", "");
		
		return sL_Result;
	}
	public static boolean hasData() {
		boolean bL_Reault = false;
		try {
			G_SelectPs.setString(1,sG_AcqBankId);
			G_SelectPs.setString(2,sG_MchtNo);
		
			
			ResultSet L_ResultSet = G_SelectPs.executeQuery();
			
			while (L_ResultSet.next()) {
				bL_Reault = true;
			}
			
			L_ResultSet.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Reault = false;
		}
		
		return bL_Reault;
	}

	public static boolean updateData() {
		boolean bL_Result = true;
		try {
			G_UpdatePs.setString(1, sG_MchtName);
			G_UpdatePs.setString(2, sG_ZipCode);
			G_UpdatePs.setString(3, sG_City);
			G_UpdatePs.setString(4, sG_Address);
			G_UpdatePs.setString(5, sG_TelNo);
			G_UpdatePs.setString(6, sG_ContractType);
			G_UpdatePs.setString(7, sG_BankNo);

			G_UpdatePs.setString(8, sG_PosFlag);
			G_UpdatePs.setString(9, sG_EdcFlag);
			G_UpdatePs.setString(10, sG_IcFlag);
			G_UpdatePs.setString(11, sG_NccContrStartDate);
			G_UpdatePs.setString(12, sG_NccContrEndDate);
			G_UpdatePs.setString(13, sG_NccRiskLevel);
			
			G_UpdatePs.setString(14, sG_VisContrStartDate);
			G_UpdatePs.setString(15, sG_VisContrEndDate);
			G_UpdatePs.setString(16, sG_VisRiskLevel);
			G_UpdatePs.setString(17, sG_MstContrStartDate);
			G_UpdatePs.setString(18, sG_MstContrEndDate);
			G_UpdatePs.setString(19, sG_MstRiskLevel);
			
			G_UpdatePs.setString(20, sG_JcbContrStartDate);
			G_UpdatePs.setString(21, sG_JcbContrEndDate);
			G_UpdatePs.setString(22, sG_JcbRiskLevel);

			G_UpdatePs.setString(23, sG_MccCode);
			G_UpdatePs.setString(24, sG_CurrentCode);
			
			G_UpdatePs.setInt(25, nG_NccCFloorLimit);
			G_UpdatePs.setInt(26, nG_NccGFloorLimit);
			G_UpdatePs.setInt(27, nG_VisCFloorLimit);
			G_UpdatePs.setInt(28, nG_VisGFloorLimit);
			G_UpdatePs.setInt(29, nG_MstCFloorLimit);
			G_UpdatePs.setInt(30, nG_MstGFloorLimit);
			
			G_UpdatePs.setInt(31, nG_JcbCFloorLimit);
			G_UpdatePs.setInt(32, nG_JcbGFloorLimit);


			G_UpdatePs.setString(33,sG_AcqBankId);
			G_UpdatePs.setString(34,sG_MchtNo);
			
			G_UpdatePs.executeUpdate();

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
			
			//System.out.println("updateData exception=>" + e.getMessage() + "--" + "AcqBankId=>" + sG_AcqBankId + "--MchtNo=>" + sG_MchtNo);
		}
		return bL_Result;
	}
	
	public static boolean insertData(String sP_CurDate, String sP_ProgId) {
		boolean bL_Result = true;
		try {
			G_InsertPs.setString(1, sG_MchtName);
			G_InsertPs.setString(2, sG_ZipCode);
			G_InsertPs.setString(3, sG_City);
			G_InsertPs.setString(4, sG_Address);
			G_InsertPs.setString(5, sG_TelNo);
			G_InsertPs.setString(6, sG_ContractType);
			G_InsertPs.setString(7, sG_BankNo);

			G_InsertPs.setString(8, sG_PosFlag);
			G_InsertPs.setString(9, sG_EdcFlag);
			G_InsertPs.setString(10, sG_IcFlag);
			G_InsertPs.setString(11, sG_NccContrStartDate);
			G_InsertPs.setString(12, sG_NccContrEndDate);
			G_InsertPs.setString(13, sG_NccRiskLevel);
			
			G_InsertPs.setString(14, sG_VisContrStartDate);
			G_InsertPs.setString(15, sG_VisContrEndDate);
			G_InsertPs.setString(16, sG_VisRiskLevel);
			G_InsertPs.setString(17, sG_MstContrStartDate);
			G_InsertPs.setString(18, sG_MstContrEndDate);
			G_InsertPs.setString(19, sG_MstRiskLevel);
			
			G_InsertPs.setString(20, sG_JcbContrStartDate);
			G_InsertPs.setString(21, sG_JcbContrEndDate);
			G_InsertPs.setString(22, sG_JcbRiskLevel);

			G_InsertPs.setString(23, sG_MccCode);
			G_InsertPs.setString(24, sG_CurrentCode);
			
			G_InsertPs.setInt(25, nG_NccCFloorLimit);
			G_InsertPs.setInt(26, nG_NccGFloorLimit);
			G_InsertPs.setInt(27, nG_VisCFloorLimit);
			G_InsertPs.setInt(28, nG_VisGFloorLimit);
			G_InsertPs.setInt(29, nG_MstCFloorLimit);
			G_InsertPs.setInt(30, nG_MstGFloorLimit);
			
			G_InsertPs.setInt(31, nG_JcbCFloorLimit);
			G_InsertPs.setInt(32, nG_JcbGFloorLimit);


			G_InsertPs.setString(33,sG_AcqBankId);
			G_InsertPs.setString(34,sG_MchtNo);

			
			G_InsertPs.setString(35,sP_CurDate);
			G_InsertPs.setString(36,sP_ProgId);
			G_InsertPs.executeUpdate();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
			//System.out.println("insertData exception=>" + e.getMessage() + "--" + "AcqBankId=>" + sG_AcqBankId + "--MchtNo=>" + sG_MchtNo);
		}
		return bL_Result;
	}

	public static void parseSrcData(String sP_SrcData) {
		sG_MchtNo= getData(sP_SrcData,0, 10);
		sG_MchtName = getData(sP_SrcData,10, 35);
		sG_ZipCode = getData(sP_SrcData,35, 38);
		sG_City = getData(sP_SrcData,38, 45);
		sG_Address = getData(sP_SrcData,45, 73);
		sG_TelNo = getData(sP_SrcData,73, 83);
		
		sG_ContractType = getData(sP_SrcData,83, 84);
		sG_BankNo = getData(sP_SrcData,84, 86);
		////System.out.println("=="+sG_MchtNo + "=="+sG_MchtName+"=="+sG_ZipCode+"--" + sG_City);
		////System.out.println("=="+sG_Address + "--" + sG_TelNo + "--" + sG_ContractType + "^^"+ sG_BankNo);
		
		 
				
		sG_PosFlag = getData(sP_SrcData,86, 87);
		sG_EdcFlag = getData(sP_SrcData,87, 88);
		sG_IcFlag = getData(sP_SrcData,88, 89);
		
		
		
		sG_NccContrStartDate = getData(sP_SrcData,89, 95);
		sG_NccContrEndDate = getData(sP_SrcData,95, 101);
		sG_NccRiskLevel = getData(sP_SrcData,101, 103);
		
		////System.out.println("=="+sG_PosFlag+"==" + sG_EdcFlag+"=="+sG_IcFlag +"=="+sG_NccContrStartDate+"=="+sG_NccContrEndDate+"=="+ sG_NccRiskLevel);
		
		sG_VisContrStartDate = getData(sP_SrcData,103, 109);
		sG_VisContrEndDate = getData(sP_SrcData,109, 115);
		sG_VisRiskLevel = getData(sP_SrcData,115, 117);

		////System.out.println(sG_VisContrStartDate+"=="+sG_VisContrEndDate+"=="+ sG_VisRiskLevel);
		
		sG_MstContrStartDate = getData(sP_SrcData,117, 123);
		sG_MstContrEndDate = getData(sP_SrcData,123, 129);
		sG_MstRiskLevel = getData(sP_SrcData,129, 131);
		////System.out.println(sG_MstContrStartDate+"=="+sG_MstContrEndDate+"=="+ sG_MstRiskLevel);
		
		
		sG_JcbContrStartDate = getData(sP_SrcData,131, 137);
		sG_JcbContrEndDate = getData(sP_SrcData,137, 143);
		sG_JcbRiskLevel = getData(sP_SrcData,143, 145);

		
		////System.out.println(sG_JcbContrStartDate+"=="+sG_JcbContrEndDate+"=="+ sG_JcbRiskLevel);
		
		/**** mcc_code,but 第一碼為'0'(應從146-150) ****/
		sG_MccCode = getData(sP_SrcData,146, 150);
		
		sG_CurrentCode = getData(sP_SrcData,150, 151);
	
	}
}
