/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 110-03-07  V1.00.0     Ryan     initial                                    *
* 110-04-14  V1.00.1     Ryan     增加處理  proc_code = 'X'                      *
*****************************************************************************/

package Cca;

import com.CommCrd;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.net.URL;
import java.text.Normalizer;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import com.BaseBatch;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.tcb.ap4.tool.Decryptor;

import Dxc.Util.SecurityUtil;
import bank.Auth.HpeUtil;
import net.sf.json.JSONObject;

public class CcaB003 extends BaseBatch {
	private String PROGNAME = "VD沖正線上處理 111/04/14 V.00.1";
	CommFunction   comm  = new CommFunction();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommDate zzdate = new CommDate();
	JSONObject jasonToString = new JSONObject();
	private String crtDate = "";
	private String rowid = "";
	private String isIdPSeqno2 = "", isUrlToken = "", isUrlTxn = "";
	private String isUserName1 = "", isUserName2 = "", isUserPd1 = "", isUserPd2 = "";
	private String isApplJson1 = "", isApplJson2 = "", isToken = "";
	private String lsReponseCode = "" ,lsOriReponseCode; 

	// =****************************************************************************
	public static void main(String[] args) {
		CcaB003 proc = new CcaB003();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

	// =****************************************************************************
	public int mainProcess(String[] args) {
		try {
			long startTime = System.currentTimeMillis() ;
			dateTime();
			
			setConsoleMode("N");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME + "," + args.length);
			if (comm.isAppActive(javaProgram)) {
				showLogMessage("I", "", "本程式已有另依程序啟動中, 不執行..");
				return (0);
			}

			int liArg = args.length;
			if (liArg > 1) {
				return (0);
			}

			crtDate = sysDate;
			
			if (liArg == 1) {
				crtDate = args[0];
			}

			dbConnect();
			
			getImsParm();
			selectCcaImsLog();

			printf("Total process record:[%s]", totalCnt);

			commitDataBase();

			long endTime = durationTime(startTime+"")-1;
			if(endTime<30) {
				showLogMessage("I", "", "sleep "+(30-endTime)+" sec");
				TimeUnit.SECONDS.sleep(30-endTime);
			}
			
			endProgram();

			finalProcess();
			return (0);
		}

		catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}
	
	// =****************************************************************************
	@Override
	protected void dataProcess(String[] args) throws Exception {

	}
	
	void getImsParm() throws Exception {
		String sql1 = "", sql2 = "", sql3 = "", lsTokenDate = "", updateSql = "";
		sql1 = " select wf_value , wf_value2 , wf_value3 , wf_value4 from ptr_sys_parm "
				+ " where wf_parm = 'IMS_TOKEN' and wf_key = 'GET_TOKEN' ";

		sql2 = " select wf_value , wf_value2 , wf_value3 , wf_value4 from ptr_sys_parm "
				+ " where wf_parm = 'IMS_TXN' and wf_key = 'TXN' ";

		sql3 = " select wf_value , wf_value2||wf_value3||wf_value4 as token from ptr_sys_parm where wf_parm = 'IMS_TOKEN' and wf_key = 'TOKEN' ";

		sqlSelect(sql1);
		if (sqlNrow > 0) {
			isUrlToken = colSs("wf_value");
			isApplJson1 = colSs("wf_value2");
			isUserName1 = colSs("wf_value3");
		}

		sqlSelect(sql2);
		if (sqlNrow > 0) {
			isUrlTxn = colSs("wf_value");
			isApplJson2 = colSs("wf_value2");
			isUserName2 = colSs("wf_value3");	
		}

		sqlSelect(sql3);
		if (sqlNrow > 0) {
			lsTokenDate = colSs("wf_value");
			isToken = colSs("token");
		}
		try {

			if (eqIgno(lsTokenDate, sysDate) == false) {
				// --從檔案中取密碼
				String confFile = SecurityUtil.verifyPath(comc.getECSHOME() + "/conf/ecsAcdp.properties");
				confFile = Normalizer.normalize(confFile, Normalizer.Form.NFKC);
				Properties props = new Properties();
				try (FileInputStream fis = new FileInputStream(confFile);) {
					props.load(fis);
					fis.close();
				}

				isUserPd1 = props.getProperty("cr.ims").trim();

				// --解密
				Decryptor decrptor = new Decryptor();
				isUserPd1 = decrptor.doDecrypt(isUserPd1);

				// --取Token
				JSONObject jsonObjectUserPw = new JSONObject();
				jsonObjectUserPw.put("password", isUserPd1);
				jsonObjectUserPw.put("username", isUserName1);
//			printf("password = " + "[ " +isUserPd1+ " ]" );
//			printf("username = " + "[ " +isUserName1+ " ]" );
				String userPass = jsonObjectUserPw.toString();
				HpeUtil hpeUtil = new HpeUtil();
				// --取 Token
				String lsTemp = "";
				printf("isUrlToken = " + "[ " + isUrlToken + " ]");
//			printf("isApplJson1 = " + "[ " +isApplJson1+ " ]" );
//			printf("userPass = " + "[ " +userPass+ " ]" );
				lsTemp = hpeUtil.curlToken(isUrlToken, isApplJson1, "", userPass);
				isToken = parsingJson(lsTemp, 1);
				lsTokenDate = sysDate;
				updateSql = "update ptr_sys_parm set wf_value = ? , wf_value2 = ? , wf_value3 = ? , wf_value4 = ? where wf_parm = 'IMS_TOKEN' and wf_key = 'TOKEN'";

				setString(1, lsTokenDate);
				setString(2, commString.mid(isToken, 0, 100));
				setString(3, commString.mid(isToken, 100, 100));
				setString(4, commString.mid(isToken, 200, isToken.length()));
				sqlExec(updateSql);
				sqlCommit(1);
			}
		}catch(Exception ex) {
			showLogMessage("W", "", ex.toString()); 
        }

	}

	void selectCcaImsLog() throws Exception {
		String imsReversalData = "";
		int endIndex = 0;
		sqlCmd = " select hex(rowid) as rowid , ims_reversal_data , ims_resp_code from cca_ims_log ";
		sqlCmd += "  where crt_date = ? and send_date = '' and proc_code = 'Y' ";
		setString(1, crtDate);
		this.openCursor();

		while (fetchTable()) {
			rowid = colSs("rowid");
			imsReversalData = colSs("ims_reversal_data");
			lsOriReponseCode = colSs("ims_resp_code");
			if(empty(commString.mid(imsReversalData, 0,1))) {
				imsReversalData = imsReversalData.replaceFirst("\\s", "");
			}
			if(commString.pos(",2440,2442", commString.mid(imsReversalData, 4,4)) <= 0) {
				continue;
			}
			imsReversalData = " 0202" + commString.mid(imsReversalData, 4);

			int procResult = sendReversalData(imsReversalData);
			if( procResult == 1) {
				updateReversalLog1();
				totalCnt++;
			}
			if( procResult == 2) {
				updateReversalLog2();
				totalCnt++;
			}
			if( procResult == 3) {
				updateReversalLog3();
				totalCnt++;
			}
		}
		this.closeCursor();
	}

	void updateReversalLog1() throws Exception{
		sqlCmd = " update cca_ims_log set ims_resp_code = ? ,send_date = to_char(sysdate,'yyyymmdd') ,mod_pgm = 'CcaB003' ,mod_time = sysdate where rowid = ? ";
		sqlExec(new Object[] {lsReponseCode,hexStrToByteArr(rowid) });
		if (sqlNrow < 0) {
			printf("update cca_ims_log error");
			errExit(sqlNrow);
		}
	}
	
	void updateReversalLog2() throws Exception{
		sqlCmd = " update cca_ims_log set ims_resp_code = ? ,proc_code = 'X' ,mod_pgm = 'CcaB003' ,mod_time = sysdate where rowid = ? ";
		sqlExec(new Object[] {lsReponseCode,hexStrToByteArr(rowid) });
		if (sqlNrow < 0) {
			printf("update cca_ims_log error");
			errExit(sqlNrow);
		}
	}
	
	void updateReversalLog3() throws Exception{
		sqlCmd = " update cca_ims_log set ims_resp_code = ? ,mod_pgm = 'CcaB003' ,mod_time = sysdate where rowid = ? ";
		sqlExec(new Object[] {lsReponseCode,hexStrToByteArr(rowid) });
		if (sqlNrow < 0) {
			printf("update cca_ims_log error");
			errExit(sqlNrow);
		}
	}

	
	int sendReversalData(String imsReversalData) throws Exception {
		try {
			JSONObject jsonObjectSent = new JSONObject();
			HpeUtil hpeUtil = new HpeUtil();
			String lsReceiveData = "", lsSentData = "", lsTempReceiveData = "", backSeqNo = "", seqNo = "";

			seqNo = commString.left(imsReversalData, 23).replaceFirst("\\s", "");

			lsSentData = hpeUtil.encoded2Base64(hpeUtil.transByCode(imsReversalData, "Cp1047"));
//		printf("lsSentData = [" + hpeUtil.ebcdic2Str(hpeUtil.decodedString2(lsSentData))+ "]");

			jsonObjectSent.put("message", lsSentData);
			jsonObjectSent.put("seqNo", seqNo);
			lsSentData = jsonObjectSent.toString();
//		log(lsSentData);

			lsTempReceiveData = hpeUtil.curlToken(isUrlTxn, isApplJson2, isToken, lsSentData);

			lsTempReceiveData = parsingJson(lsTempReceiveData, 2);
			if (empty(lsTempReceiveData)) {
				// --電文沒有回應
				printf("電文沒有回應");
				return -1;
			}
			lsReceiveData = hpeUtil.ebcdic2Str(hpeUtil.decodedString2(lsTempReceiveData));
			backSeqNo = commString.mid(lsReceiveData, 21, 10);
			printf("seqNo = [" + seqNo + "]");

			if (backSeqNo.equals(commString.right(seqNo, 10)) == false) {
				// --電文回覆序號錯誤
				printf("電文回覆序號錯誤");
				printf("backSeqNo = [" + backSeqNo + "]");
//			printf("lsReceiveData = ");
//			printf(lsReceiveData);
				return -1;
			}
			// --66~69 reponse code 0000 表示成功 ,1002重複交易
			lsReponseCode = commString.mid(lsReceiveData, 61, 4);

			printf("lsReponseCode = [" + lsReponseCode + "]");
			printf("lsOriReponseCode = [" + lsOriReponseCode + "]");

			if (empty(lsReponseCode)) {
				return 0;
			}
			
			if(lsReponseCode.equals("0000")) {
				return 1;
			}

			if(lsOriReponseCode.equals(lsReponseCode)) {
				return 2; //X
			}
			
			if(!lsOriReponseCode.equals(lsReponseCode)) {
				return 3;
			}
			
		}catch(Exception ex) {
			showLogMessage("W", "", ex.toString()); 
			return -1;
		}
		return 1;
	}

	String parsingJson(String fromObject, int infoType) {
		String lsReponseData = "";
		jasonToString = JSONObject.fromObject(fromObject);
		// --1:Token , 2:Txn
		if (infoType == 1) {
			lsReponseData = jasonToString.getString("token");
		} else if (infoType == 2) {
			lsReponseData = jasonToString.getString("message");
		} else if (infoType == 3) {
			lsReponseData = jasonToString.getString("seqNo");
		}

		return lsReponseData;
	}
	
}