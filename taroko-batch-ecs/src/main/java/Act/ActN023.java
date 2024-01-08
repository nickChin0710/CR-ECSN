/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/28  V1.00.01    Brian     error correction                          *
 *  110/01/20  V1.00.02    Brian     mantis#5602 移除產出檔案檔尾多出換行      *
 *  110/04/21  V1.00.04    Simon     fix vulnerability Absolute Path Traversal *
 *  110/05/20  V1.00.05    Simon     fix2 vulnerability Absolute Path Traversal*
 *  111/10/13  V1.00.07  jiangyigndong  updated for project coding standard    *
 *  112/04/25  V1.00.08    Simon     1."017" changed to "006"                  *
 *                                   2.read ptr_sys_parm for contact value1    *
 *                                   3.add procFTP()                           *
 *                                   4.comc.errExit() 取代 comcr.errRtn() 顯示"本日無KK8資料可產生"*
 *  112/05/01  V1.00.09    Simon     1.add fetch's daoTable                    *
 *                                   2.fileName format error fixed             *
 *  112/06/11  V1.00.10    Simon     1.換日後跑，檔案日期減一天                *
 *                                   2.procFTP1() to folder-JCIC               *
 *                                   3.procFTP2() to folder-CRDATACREA         *
 ******************************************************************************/

package Act;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*JCIC界面-產生KK8帳戶欠款餘額媒體檔處理程式*/
public class ActN023 extends AccessDAO {

    private final String PROGNAME = 
    "JCIC界面-產生KK8帳戶欠款餘額媒體檔處理程式  112/06/11  V1.00.10";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;
    CommFTP commFTP = null;

    String prgmId = "ActN023";
    String rptName1 = "";
    String rptName2 = "";
    int recordCnt = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq1 = 0;
    int rptSeq2 = 0;
    int errCnt = 0;
    String ErrMsg = "";
    String buf = "";
    String szTmp = "";
    String stderr = "";
    long hModSeqno = 0;
    String ECS_SERVER = "";
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    // String h_mod_ws = "";
    // String h_mod_log = "";
    String hCallBatchSeqno = "";
    String iPostDate = "";

    String hBusiBusinessDate = "";
    String hTempSysdate = "";
    String hAjblId = "";
    String hAjblIdCode = "";
    String hAjblBillTypeFlag = "";
    double hAjblStmtThisTtlAmt = 0;
    double hAjblUnpostInstFee = 0;
    double hAjblUnpostCardFee = 0;
    double hAjblUnpostInstStageFee = 0;
    String hAjblRowid = "";
    String hPrintName = "";
    String hRptName = "";
    String hTempBusinessDate = "";
    int hCnt = 0;
    String hAjblLogType = "";
    String hChgiChiName = "";
    String hChgiCreateUser = "";
    String hChgiApprovUser = "";
    String hChgiId = "";
    String hChgiIdCode = "";
    String hChgiPostJcicFlag = "";
    String hJcicContValue1 = "";
    String hJcicContValue2 = "";
    String jcicFileName = "";

    String[] tempDate = new String[9];
    int totalCount = 0;
    int totalAll = 0;
    String fileName = "";
    String temstr = "";
    String temstr1 = "";
    String cDate = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            exceptExit = 1;

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            if ((args.length == 1) && (args[0].length() == 8)) {
              if (args[0].chars().allMatch( Character::isDigit ) ) {
//              args[0] = sanitizeArg2(args[0]);
                hBusiBusinessDate = args[0];
                hCallBatchSeqno = "";
              } else {
              //h_busi_business_date will use the value get from select_ptr_businday()
              }
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            commFTP = new CommFTP(getDBconnect(), getDBalias());
          	comr = new CommRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            if (args.length > 2) {
                comcr.errRtn("Usage : ActN023 [business_date]", "", hCallBatchSeqno);
            }

            selectPtrBusinday();

            if (selectActJcicBal0() != 0) {
                exceptExit = 0;
              //comcr.errRtn(String.format("本日無KK8資料可產生", hBusiBusinessDate), "", hCallBatchSeqno);
                comc.errExit(String.format("本日無KK8資料可產生", hBusiBusinessDate), 
                hCallBatchSeqno);
            }

   			    getContactParm();
            checkOpen();//open in comc.writeReport()

            selectActJcicBal();
            buf = String.format("TRLR%08d", totalCount);
            lpar1.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));
            temstr = String.format("%s/media/act/006%4.4s1.kk8", comc.getECSHOME(), 
            comc.getSubString(hTempBusinessDate, 4));
            //temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
            comc.writeReport(temstr, lpar1);

            printReport();
            temstr1 = String.format("%s/reports/act_n023_%sR_%s", comc.getECSHOME(), hAjblLogType,
                    hTempBusinessDate);
          //temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
          //comc.writeReport(temstr1, lpar1);

          //lprtn("act_n023");

            jcicFileName = String.format("006%4.4s1.kk8",
            comc.getSubString(hTempBusinessDate, 4));
            procFTP1();//to folder-JCIC
            procFTP2();//to folder-CRDATACREA
            renameFile(jcicFileName);

            showLogMessage("I", "", String.format("累計處理筆數 [%d]", totalAll));

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select decode( cast(? as varchar(8)) ,'',business_date, ? ) h_busi_business_date,";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_temp_sysdate,";
        sqlCmd += " to_char(to_date(decode(  cast(? as varchar(8)) ,'',business_date, ? ),'yyyymmdd')-1 days,'yyyymmdd') h_temp_business_date ";
        sqlCmd += " from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
            hTempBusinessDate = getValue("h_temp_business_date");
            hTempSysdate = getValue("h_temp_sysdate");
        }

    }

    /***********************************************************************/
    int selectActJcicBal0() throws Exception {
        hAjblLogType = "";
        sqlCmd = "select 1 cnt,";
        sqlCmd += " log_type ";
        sqlCmd += " from act_jcic_bal  ";
        sqlCmd += "where crt_date = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCnt = getValueInt("cnt");
            hAjblLogType = getValue("log_type");
        } else
            return (1);
        return (0);
    }

  /***********************************************************************/
	void getContactParm() throws Exception {
		
	  hJcicContValue1 = " ";
	  hJcicContValue2 = " ";

		selectSQL = "wf_value , wf_value2 ";
		daoTable = "ptr_sys_parm";
		whereStr = "where wf_parm ='JCIC_FILE' and wf_key = 'JCIC_KK8' ";
		selectTable();
		
		if("Y".equals(notFound)) {
			return ;
		}	else {
	  	hJcicContValue1 = getValue("wf_value");
	    hJcicContValue2 = getValue("wf_value2");
		}
  	return ;
		
	}
	
    /***********************************************************************/
    void checkOpen() throws Exception {
        buf = String.format("%-18.18s006%5.5s%07d01%10.10s%-18.18s%-78.78s", "JCIC-DAT-KK08-V01-", " ",
                //comcr.str2long(h_busi_business_date) - 19110000, " ", "02-89822222#1880", " ");
                comcr.str2long(hTempSysdate) - 19110000, " ", hJcicContValue1, hJcicContValue2);
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

    }

    /***********************************************************************/
    void selectActJcicBal() throws Exception {

      daoTable = "act_jcic_bal a, crd_idno c";
      sqlCmd = "select ";
      sqlCmd += " decode(c.id_no,'',corp_no,c.id_no) h_ajbl_id,";
      // sqlCmd += " id_code,"; //notfound
      sqlCmd += " bill_type_flag,";
      sqlCmd += " stmt_this_ttl_amt,";
      sqlCmd += " unpost_inst_fee,";
      sqlCmd += " unpost_card_fee,";
      sqlCmd += " unpost_inst_stage_fee,";
      sqlCmd += " a.rowid rowid ";
      sqlCmd += " from act_jcic_bal a, crd_idno c ";
      sqlCmd += "where a.crt_date = ? ";
      sqlCmd += "  and a.id_p_seqno = c.id_p_seqno ";
    //sqlCmd += "  and acct_type not in ('02','03') ";//ActN022已篩選非商務卡
      setString(1, hBusiBusinessDate);
      int cursorIndex = openCursor();
      while (fetchTable(cursorIndex)) {
          hAjblId = getValue("h_ajbl_id");
          hAjblIdCode = getValue("id_code");
          hAjblBillTypeFlag = getValue("bill_type_flag");
          hAjblStmtThisTtlAmt = getValueDouble("stmt_this_ttl_amt");
          hAjblUnpostInstFee = getValueDouble("unpost_inst_fee");
          hAjblUnpostCardFee = getValueDouble("unpost_card_fee");
          hAjblUnpostInstStageFee = getValueDouble(
                  "unpost_inst_stage_fee"); /* 增設「未到期分期償還代墊帳單帳款餘額」 */
          hAjblRowid = getValue("rowid");

          totalAll++;
          selectCrdChgId();
          if (hChgiPostJcicFlag.equals("N")) {
              insertCrdNopassJcic();
              continue;
          }

          for (int int1a = 0; int1a < 9; int1a++)
              tempDate[int1a] = "";
          //temp_date[0] = String.format("%07d", comcr.str2long(h_busi_business_date) - 19110000);
          tempDate[0] = String.format("%07d", comcr.str2long(hTempSysdate) - 19110000);

          buf = String.format("81A006%7.7s%-10.10s%2.2s%011.0f%011.0f%011.0f%011.0f", tempDate[0], hAjblId,
                  hAjblBillTypeFlag, hAjblStmtThisTtlAmt, hAjblUnpostInstFee, hAjblUnpostCardFee,
                  hAjblUnpostInstStageFee);
          lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
          /* 增加「未到期分期償還代墊帳單帳款餘額」 */

          totalCount++;
          if (totalCount % 25000 == 0)
              showLogMessage("I", "", String.format("    處理筆數 [%d]", totalCount));
      }
      closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void selectCrdChgId() throws Exception {
        hChgiCreateUser = "";
        hChgiApprovUser = "";
        hChgiChiName = "";
        hChgiId = "";
        hChgiIdCode = "";
        hChgiPostJcicFlag = "";

        sqlCmd = "select chi_name,";
        sqlCmd += " crt_user,";
        sqlCmd += " apr_user,";
        sqlCmd += " id_no,";
        sqlCmd += " id_no_code,";
        sqlCmd += " decode(post_jcic_flag,'','N',post_jcic_flag) h_chgi_post_jcic_flag ";
        sqlCmd += " from crd_chg_id  ";
        sqlCmd += "where old_id_no  = ?  ";
        sqlCmd += "  and old_id_no_code = ? ";
        setString(1, hAjblId);
        setString(2, hAjblIdCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hChgiChiName = getValue("chi_name");
            hChgiCreateUser = getValue("crt_user");
            hChgiApprovUser = getValue("approv_user");
            hChgiId = getValue("id");
            hChgiIdCode = getValue("id_code");
            hChgiPostJcicFlag = getValue("h_chgi_post_jcic_flag");
        }

    }

    /***********************************************************************/
    void insertCrdNopassJcic() throws Exception {
        setValue("old_id", hAjblId);
        setValue("old_id_code", hAjblIdCode);
        setValue("chi_name", hChgiChiName);
        setValue("post_kind", "kk8");
        setValue("post_jcic_date", sysDate);
        setValue("card_no", "");
        setValue("oppost_reason", "");
        setValue("oppost_date", "");
        setValue("mod_user", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "crd_nopass_jcic";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_nopass_jcic duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void printReport() throws Exception {

        buf = "";
        buf = comcr.insertStr(buf, " " + comcr.bankName + " ", 26);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "報表名稱:ACT_N023", 1);

        buf = comcr.insertStrCenter(buf, "報送新增JCIC信用卡戶每週帳款餘額資料總數報表", 80);

        buf = comcr.insertStr(buf, "頁次:", 68);
        temstr = String.format("%04d", 1);
        buf = comcr.insertStr(buf, temstr, 73);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        temstr = String.format("%4.4s", hTempBusinessDate);

        buf = "";
        buf = comcr.insertStr(buf, "單    位:", 1);
        buf = comcr.insertStr(buf, "109", 11);
        buf = comcr.insertStr(buf, "交易日期:", 58);
        cDate = String.format("%03d年%2.2s月%2.2s日", comcr.str2long(temstr), hTempBusinessDate.substring(4),
                hTempBusinessDate.substring(6));
        buf = comcr.insertStr(buf, cDate, 68);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        szTmp = comcr.commFormat("3z,3z,3z", totalCount);
        szTmp = String.format("產生資料 : %2.2s月    筆 數  : %s 筆", hTempBusinessDate.substring(4), szTmp);
        buf = comcr.insertStr(buf, szTmp, 1);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = comcr.insertStr(buf, "備註 1 : 每週三或四且非關帳日產生報送資料", 1);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = comcr.insertStr(buf, "備註 2 : 資料格式依據JCIC民國99年2月版本", 1);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

    }

  //************************************************************************	
  void procFTP1() throws Exception {
  	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
  	commFTP.hEflgSystemId = "JCIC"; /* 區分不同類的 FTP 檔案-大類 (必要) */
  	commFTP.hEriaLocalDir = String.format("%s/media/act", comc.getECSHOME());
  	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
  	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
  	commFTP.hEflgModPgm = javaProgram;

  //showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
	//ecs_ref_ip_code.ref_ip_code="JCIC"，其ftp_type="0"，在COMMFTP處理只是fileCopy
  	int errCode = commFTP.ftplogName("JCIC", "mput " + jcicFileName);

  	if (errCode != 0) {
  		showLogMessage("I", "", "ERROR:無法傳送 " + jcicFileName + " 資料" + " errcode:" + errCode);
  		insertEcsNotifyLog(jcicFileName);
  	}
  }

  //************************************************************************	
  void procFTP2() throws Exception {
  	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
  	commFTP.hEflgSystemId = "CRDATACREA"; /* 區分不同類的 FTP 檔案-大類 (必要) */
  	commFTP.hEriaLocalDir = String.format("%s/media/act", comc.getECSHOME());
  	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
  	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
  	commFTP.hEflgModPgm = javaProgram;

  //showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
	//ecs_ref_ip_code.ref_ip_code="JCIC"，其ftp_type="0"，在COMMFTP處理只是fileCopy
  	int errCode = commFTP.ftplogName("CRDATACREA", "mput " + jcicFileName);

  	if (errCode != 0) {
  		showLogMessage("I", "", "ERROR:無法傳送 " + jcicFileName + " 資料" + " errcode:" + errCode);
  		insertEcsNotifyLog(jcicFileName);
  	}
  }

  //************************************************************************		  
	public int insertEcsNotifyLog(String fileName) throws Exception {
	  setValue("crt_date", sysDate);
	  setValue("crt_time", sysTime);
	  setValue("unit_code", comr.getObjectOwner("3", javaProgram));
	  setValue("obj_type", "3");
	  setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
	  setValue("notify_name", "媒體檔名:" + fileName);
	  setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
	  setValue("notify_desc2", "");
	  setValue("trans_seqno", commFTP.hEflgTransSeqno);
	  setValue("mod_time", sysDate + sysTime);
	  setValue("mod_pgm", javaProgram);
	  daoTable = "ecs_notify_log";

	  insertTable();

	  return (0);
	}

  //************************************************************************	
    void renameFile(String fileName) throws Exception {
    	String tmpstr1 = String.format("%s/media/act/%s", comc.getECSHOME(), fileName);
    	String tmpstr2 = String.format("%s/media/act/backup/%s.%s", comc.getECSHOME(), 
    	fileName,sysDate+sysTime);

    	if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
    		showLogMessage("I", "", "ERROR : 檔案[" + tmpstr1 + "]更名失敗!");
    		return;
    	}
    	showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已移至 [" + tmpstr2 + "]");
    }
    
    /*****************************************************************************/
    void lpRtn(String lstr) throws Exception {
        String hPrintName = "";
        String hRptName = "";
        String lpStr = "";

        hRptName = lstr;

        sqlCmd = "select print_name ";
        sqlCmd += "  from bil_rpt_prt";
        sqlCmd += " where report_name like ? || '%'";
        sqlCmd += " fetch first 1 rows only";
        setString(1, hRptName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_bil_rpt_prt not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPrintName = getValue("print_name");
        }

        if (hPrintName.length() > 0) {
            lpStr = String.format("lp -d %s %s", hPrintName, temstr1);
          //comc.systemCmd(lp_str);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActN023 proc = new ActN023();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
