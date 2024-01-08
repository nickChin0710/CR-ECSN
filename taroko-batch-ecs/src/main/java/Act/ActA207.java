/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/01/09  V1.00.00    Ryan     program initial                            *
 *  112/06/29  V1.00.01    Ryan     modify                                     *
 *  112/09/04  V1.00.02    Ryan     修改updateActChkautopay                    *
 *  112/09/27  V1.00.03    Simon    1.調整自扣回覆相關日期                     *
 *                                  2.open & read file 方式調整                *
 ******************************************************************************/

package Act;

import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

/*自行自動扣繳媒體回饋處理程式*/
public class ActA207 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private final String PROGNAME = "產生花農卡自動扣繳回覆檔  112/09/27 V1.00.03";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommString comStr = new CommString();
    String fileName = "HH001_yyyymmdd_60000_001";
    String prgmId = "ActA207";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hmCurpModPgm = "";
    String hmCurpModTime = "";
    String hmCurpModUser = "";
    long hmCurpModSeqno = 0;
    int br  = -1;

    String hBusiBusinessDate = "";
    String hCkapAcctType = "";
    String hCkapAcctKey = "";
    String hCkapFromMark = "";
    String hCkapSendUnit = "";
    String hCkapCommerceType = "";
    String hCkapDataType = "";
    String hCkapDescCode = "";
    double hCkapTransactionAmt = 0;
    String hTempOrignStatusCode = "";
    String hCkapIdPSeqno = "";
    String hCkapChiName = "";
    String hCkapCreateDate = "";
    String hAgnnAutopayDeductDays = "";
    String hCkapRowid = "";
    String hIdnoChiName = "";
    String hIdnoCellarPhone = "";
    String hIdnoHomeAreaCode1 = "";
    String hIdnoHomeTelNo1 = "";
    String hIdnoHomeTelExt1 = "";
    String hApdlPayDate = "";
    String hCkapPSeqno = "";
    String hCkapStatusCode = "";
    double hApdlPayAmt = 0;
    int hTempSmsFlag = 0;
    String hCkapAutopayAcctNo = "";
    String hCkapAutopayId = "";
    String hCkapAutopayIdCode = "";
    String hA051ErrType = "";
    String hApbtBatchNo = "";
    String hApdlSerialNo = "";
    String hAperErrorReason = "";
    String hAperErrorRemark = "";
    long hTempBatchNoSeq = 0;
    String hTempBatchNo = "";
    int hApbtBatchTotCnt = 0;
    double hApbtBatchTotAmt = 0;
    String hSmidMsgId = "";
    String hSmidMsgDept = "";
    String hSmidMsgSendFlag = "";
    String hSmidMsgSelAcctType = "";
    String hSmidMsgUserid = "";
    String hApbtModSeqno = "";
    String hSmdlCellphoneCheckFlag = "";
    String hSmdlVoiceFlag = "";
    String hSmdlMsgDesc = "";
    String hWdayThisLastpayDate = "";
    String hAcnoStmtCycle = "";
    String hAcnoAcctStatus = "";
    String hSmidMsgSelAmt01 = "";
    String hCkapEnterAcctDate = "";
    String hCardno = "";
    int hAgnnSmsDeductDays = 0;
    String str600 = "";
    int nSerialNo = 0;
    int totalCnt = 0;
    String temstr0 = "";
    String temstr1 = "";
    String hEnterAcctDate = "";
    String hAutopayCounts = "";
    String hThisLastpayDate = "";
    String hComfRowid = "";
    String chargeBackFlag = "";
    
    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length > 2) {
                comc.errExit("Usage : ActA207 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            hBusiBusinessDate = "";
            if ((args.length == 1) && (args[0].length() == 8)) {
                String sGArgs0 = "";
                sGArgs0 = args[0];
                sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
                hBusiBusinessDate = sGArgs0;
            }
            selectPtrBusinday();
            if(selectActAutoComf() != 0 ) {
                showLogMessage("I", "", "ACT_AUTO_COMF 查無資料 ");
                return 0;
            }
            fileName = fileName.replace("yyyymmdd", hBusiBusinessDate);
            
            selectSmsMsgId();

            checkFopen();
            readFile();
            renameFile();
            if (hApbtBatchTotCnt != 0)
                insertActPayBatch();
            updateActAutoComf();
            showLogMessage("I", "", String.format("      本日處理扣款總筆數 [%d]", totalCnt));
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        } finally {
			finalProcess();
		}
    }

    /***********************************************************************/
    void checkFopen() throws Exception {

   		temstr0 = String.format("/crdataupload/%s",fileName);
   		temstr0 = Normalizer.normalize(temstr0,java.text.Normalizer.Form.NFKD);

      br = openInputText(temstr0, "MS950");
      if (br == -1) {
        exceptExit = 0;
        comc.errExit(String.format("***　Input檔案不存在[/crdataupload/%s], br=[%d]",
        fileName,br), "");
      } 
      closeInputText(br);

    	temstr1 = String.format("%s/media/act/%s", comc.getECSHOME(), fileName);
    	temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
      if (comc.fileMove(String.format("/crdataupload/%s",fileName), temstr1) == false) {
//        comcr.errRtn(String.format("檔案不存在 [/crdataupload/%s]",fileName), "", hCallBatchSeqno);
      }
      return;
    }
    
    void renameFile() throws Exception {
    	String tmpstr1 = String.format("%s/media/act/%s", comc.getECSHOME(), fileName);
    	String tmpstr2 = String.format("%s/media/act/backup/%s.%s", comc.getECSHOME(), fileName,sysDate+sysTime);

    	if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
    		showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
    		return;
    	}
    	showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select decode( cast(? as varchar(8)) , '',business_date, ? ) h_busi_business_date";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
        }

    }
    
    int selectActAutoComf() throws Exception {
    	extendField = "COMF.";
    	sqlCmd = "SELECT ROWID AS ROWID ,AUTOPAY_COUNTS,ENTER_ACCT_DATE,THIS_LASTPAY_DATE FROM ACT_AUTO_COMF ";
    	sqlCmd += " WHERE THIS_LASTPAY_DATE = (SELECT THIS_LASTPAY_DATE FROM PTR_WORKDAY WHERE STMT_CYCLE='01') ";
    	sqlCmd += " AND FILE_TYPE='4' AND (AUTOPAY_COUNTS = 1 OR AUTOPAY_COUNTS = 2)";
    	int n = selectTable();
    	if(n > 0) {
    		hAutopayCounts = getValue("COMF.AUTOPAY_COUNTS");
    		hEnterAcctDate = getValue("COMF.ENTER_ACCT_DATE");
    		hThisLastpayDate = getValue("COMF.THIS_LASTPAY_DATE");
    		hComfRowid = getValue("COMF.ROWID");
    		return 0;
    	}
    	return 1;
    }

    /***********************************************************************/
    void selectSmsMsgId() throws Exception {
        hSmidMsgId = "";
        hSmidMsgDept = "";
        hSmidMsgSendFlag = "";
        hSmidMsgSelAcctType = "";
        hSmidMsgUserid = "";
        hSmidMsgSelAmt01 = "";
        hSmidMsgSendFlag = "N";

        sqlCmd = "select msg_id,";
        sqlCmd += " msg_dept,";
        sqlCmd += " msg_send_flag,";
        sqlCmd += " decode(ACCT_TYPE_SEL,'','0',ACCT_TYPE_SEL) h_smid_msg_sel_acct_type,"; // msg_sel_acct_type
        sqlCmd += " msg_userid ";
        sqlCmd += "  from sms_msg_id  ";
        sqlCmd += " where msg_pgm = 'ACT_A205'  ";
        sqlCmd += "   and decode(msg_send_flag,'','N',msg_send_flag) ='Y' ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hSmidMsgId = getValue("msg_id");
            hSmidMsgDept = getValue("msg_dept");
            hSmidMsgSendFlag = getValue("msg_send_flag");
            hSmidMsgSelAcctType = getValue("h_smid_msg_sel_acct_type");
            hSmidMsgUserid = getValue("msg_userid");
        } else {
            showLogMessage("I", "", "--簡訊暫停發送 (SMS_P010)");
            hSmidMsgSendFlag = "";
        }

    }

    /***********************************************************************/
    void insertActPayBatch() throws Exception {
        daoTable = "act_pay_batch";
        setValue("batch_no", hApbtBatchNo);
        setValueInt("batch_tot_cnt", hApbtBatchTotCnt);
        setValueDouble("batch_tot_amt", hApbtBatchTotAmt);
        setValue("crt_user", "AIX");
        setValue("crt_date", hBusiBusinessDate);
        setValue("crt_time", sysTime);
        setValue("trial_user", "AIX");
        setValue("trial_date", hBusiBusinessDate);
        setValue("trial_time", sysTime);
        setValue("confirm_user", "AIX");
        setValue("confirm_date", hBusiBusinessDate);
        setValue("confirm_time", sysTime);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_user", "AIX");
        setValue("mod_pgm", prgmId);
        insertTable();
      //if (dupRecord.equals("Y")) { //★ modified on 2019/07/15
        if (!dupRecord.equals("Y")) {
            return;
        }

        daoTable = "act_pay_batch";
        updateSQL = " batch_tot_cnt = batch_tot_cnt + ?,";
        updateSQL += " batch_tot_amt = batch_tot_amt + ?,";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " mod_pgm       = ? ";
        whereStr = "where batch_no = ? ";
        setInt(1, hApbtBatchTotCnt);
        setDouble(2, hApbtBatchTotAmt);
        setString(3, prgmId);
        setString(4, hApbtBatchNo);
        updateTable();

    }

    /***********************************************************************/
    void readFile() throws Exception {
        String temp1Date = "";
        nSerialNo = 0;

        selectActPayBatch();
        hApbtBatchTotCnt = 0;
        hApbtBatchTotAmt = 0;

      //if (openBinaryInput(temstr1) == false) {
      //    comcr.errRtn("檔案不存在：" + temstr1, "", hCallBatchSeqno);
      //}

        br = openInputText(temstr1, "MS950");
        if (br == -1) {
          exceptExit = 0;
          comc.errExit(String.format("***　Input檔案[%s]讀取錯誤, br=[%d]",
          temstr1,br), "");
        } 

      //int readlen = 0;
      //byte[] bytes = new byte[81];
      //while ((readlen = readBinFile(bytes)) > 0) {
        //str600 = new String(bytes, 0, readlen, "MS950");
        //str600 = comc.rtrim(str600);
        while (true) {
          str600 = readTextFile(br);
          if (endFile[br].equals("Y")) break;

        //if (str600.length() < 80)
          if (str600.length() < 10)
              continue;

          if (nSerialNo == 0) {
              nSerialNo++;
              continue;//跳過第1筆 header
          } 

          totalCnt++;
          if ((totalCnt % 5000) == 0) {
              showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
          }

          getField();
          
          if(selectCrdCard() == 1) {
          	continue;
          } 

          if (selectActChkautopay() == 1) {
              hA051ErrType = "0";
              insertActA005r1();
              if ("Y".equals(chargeBackFlag)) {
                  hAperErrorReason = "102";
                  hAperErrorRemark = "AUT3 act_chkautopay not found";
                  insertActPayError();
              }
              continue;
          } else {
              if (!hTempOrignStatusCode.substring(0, 2).equals("99")) {
                  hA051ErrType = "1";
                  insertActA005r1();

                  if ("Y".equals(chargeBackFlag)) {
                      hAperErrorReason = "101";
                      hAperErrorRemark = "AUT3 duplicated update act_chkautopay";
                      insertActPayError();
                  }
                  continue;
              }
          }
          hTempSmsFlag = 0;
          if ((!"Y".equals(chargeBackFlag))
                  || (("Y".equals(chargeBackFlag)) && (hCkapTransactionAmt > hApdlPayAmt))) {
              if (hCkapAcctType.equals("02")) {
                  insertActCorpAutopay();
              } else if (hSmidMsgSendFlag.equals("Y")) {
                  selectPtrWorkday();
                  temp1Date = comcr.increaseDays(hWdayThisLastpayDate, -1);
                  hWdayThisLastpayDate = temp1Date;
                  temp1Date = comcr.increaseDays(hWdayThisLastpayDate, hAgnnSmsDeductDays);
                  if ((hBusiBusinessDate.equals(temp1Date)) && (hAcnoAcctStatus.compareTo("3") < 0))
                      procSmsMsgDtl();
              }
          }
          updateActChkautopay();
          if (hTempSmsFlag == 0)
              hIdnoCellarPhone = "";
          if (!"Y".equals(chargeBackFlag)) {
              hA051ErrType = "2";
              insertActA005r1();
              continue;
          }
          /*
           * 不能扣 , act_e004 處理 if (h_apdl_pay_amt>0) { update_act_acct();
           * update_act_acct_curr(); }
           */
          insertActPayDetail();
        //insert_onbat();  新系統可用餘額直接抓 act_pay_detail 判斷
          hApbtBatchTotCnt++;
          hApbtBatchTotAmt = hApbtBatchTotAmt + hApdlPayAmt;
      } /*- while -*/
    //closeBinaryInput();
      closeInputText(br);
    }
       
    int selectCrdCard() throws Exception {
    	hCkapPSeqno = "";
    	sqlCmd = "select p_seqno from crd_card where card_no = ? and curr_code = '901' ";
    	setString(1,hCardno);
    	int recordCnt = selectTable();
    	if(recordCnt==0) {
    		 showLogMessage("I", "", String.format("selectCrdCard not found ,card_no = [%s]", hCardno));
    		 return 1;
    	}
    	hCkapPSeqno = getValue("p_seqno");
    	return 0;
    }

    /***********************************************************************/
    void selectActPayBatch() throws Exception {
        String tempstr = "";
        hTempBatchNo = String.format("%s9002%c", hBusiBusinessDate, '%');

        sqlCmd = "select to_number(substr(max(batch_no),13,4))+1 h_temp_batch_no_seq ";
        sqlCmd += " from act_pay_batch  ";
        sqlCmd += "where batch_no like ? ";
        setString(1, hTempBatchNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempBatchNoSeq = getValueLong("h_temp_batch_no_seq");
        }

        if (hTempBatchNoSeq != 0)
            tempstr = String.format("%12.12s%04d", hTempBatchNo, hTempBatchNoSeq);
        else
            tempstr = String.format("%12.12s0001", hTempBatchNo);

        hApbtBatchNo = tempstr;
    }
    
    /***********************************************************************/
    @SuppressWarnings("unused")
    void getField() throws Exception {
        String stra = "";
        String strb = "";
        int int1;
        double doublea;

        stra = comStr.mid(str600, 38,7);
        strb = String.format("%08d", comcr.str2long(stra) + 19110000);

        if (comcr.str2Date(strb) == null) {
            comcr.errRtn(String.format("入扣帳日期錯誤[%s] !", stra), "", hCallBatchSeqno);
        }
        hApdlPayDate = strb;

      //hCardno = comc.rtrim(comStr.bbMid(str600,0, 16));
        hCardno = comc.rtrim(comStr.mid(str600,0, 16));
        stra = comc.rtrim(comStr.mid(str600,25, 13));
        hApdlPayAmt = comcr.str2double(stra);
        hApdlPayAmt = hApdlPayAmt / 100;
        
        hCkapStatusCode = "0000";
        
        if (hApdlPayAmt > 0) {
        	chargeBackFlag = "Y";
        } else {
            chargeBackFlag = "N";
        }
    }

    /***********************************************************************/
    int selectActChkautopay() throws Exception {
        hCkapAcctType = "";
        hCkapAcctKey = "";
        hCkapFromMark = "";
        hCkapSendUnit = "";
        hCkapCommerceType = "";
        hCkapDataType = "";
        hCkapDescCode = "";
        hCkapTransactionAmt = 0;
        hTempOrignStatusCode = "";
        hCkapIdPSeqno = "";
        hCkapChiName = "";
        hCkapCreateDate = "";
        hAgnnSmsDeductDays = 0;
        hCkapRowid = "";
        hIdnoChiName = "";
        hIdnoCellarPhone = "";
        hIdnoHomeAreaCode1 = "";
        hIdnoHomeTelNo1 = "";
        hIdnoHomeTelExt1 = "";

        sqlCmd = "select a.acct_type,";
        sqlCmd += " d.acct_key,";
        sqlCmd += " d.autopay_id,";
        sqlCmd += " d.autopay_acct_no,";
        sqlCmd += " a.from_mark,";
        sqlCmd += " a.send_unit,";
        sqlCmd += " a.commerce_type,";
        sqlCmd += " a.data_type,";
        sqlCmd += " a.desc_code,";
        sqlCmd += " a.transaction_amt,";
        sqlCmd += " a.status_code,";
        sqlCmd += " a.id_p_seqno,";
        sqlCmd += " a.chi_name,";
        sqlCmd += " a.crt_date,";
        sqlCmd += " b.autopay_deduct_days,";
        sqlCmd += " decode(b.sms_deduct_days, 0, b.autopay_deduct_days, b.sms_deduct_days) h_agnn_sms_deduct_days,";
        sqlCmd += " a.rowid rowid,";
        sqlCmd += " c.chi_name,";
        sqlCmd += " c.cellar_phone,";
        sqlCmd += " decode(c.home_tel_no1, '', c.home_area_code2, c.home_area_code1) h_idno_home_area_code1,";
        sqlCmd += " decode(c.home_tel_no1, '', c.home_tel_no2   , c.home_tel_no1)    h_idno_home_tel_no1,";
        sqlCmd += " decode(c.home_tel_no1, '', c.home_tel_ext2  , c.home_tel_ext1)   h_idno_home_tel_ext1 ";
        sqlCmd += "  from ptr_actgeneral_n b, act_chkautopay a";
        sqlCmd += "  left outer join crd_idno c";
        sqlCmd += "    on c.id_p_seqno = a.id_p_seqno ";
        sqlCmd += "  left join act_acno d";
        sqlCmd += "    on d.acno_p_seqno = a.p_seqno";
        sqlCmd += " where a.enter_acct_date  = ?  ";
        sqlCmd += "   and decode(a.curr_code,'','901',a.curr_code) = '901'  ";
        sqlCmd += "   and a.p_seqno   = ?  ";
        sqlCmd += "   and a.acct_type = b.acct_type  ";
        sqlCmd += "   and a.from_mark = '02' ";
        setString(1, hEnterAcctDate);
        setString(2, hCkapPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCkapAcctType = getValue("acct_type");
            hCkapAcctKey = getValue ("acct_key");
            hCkapAutopayId = getValue ("autopay_id");
            hCkapAutopayAcctNo = getValue ("autopay_acct_no");
            hCkapFromMark = getValue("from_mark");
            hCkapSendUnit = getValue("send_unit");
            hCkapCommerceType = getValue("commerce_type");
            hCkapDataType = getValue("data_type");
            hCkapDescCode = getValue("desc_code");
            hCkapTransactionAmt = getValueDouble("transaction_amt");
            hTempOrignStatusCode = getValue("status_code");
            hCkapIdPSeqno = getValue("id_p_seqno");
            hCkapChiName = getValue("chi_name");
            hCkapCreateDate = getValue("crt_date");
            hAgnnAutopayDeductDays = getValue("autopay_deduct_days");
            hAgnnSmsDeductDays = getValueInt("h_agnn_sms_deduct_days");
            hCkapRowid = getValue("rowid");
            hIdnoChiName = getValue("chi_name");
            hIdnoCellarPhone = getValue("cellar_phone");
            hIdnoHomeAreaCode1 = getValue("h_idno_home_area_code1");
            hIdnoHomeTelNo1 = getValue("h_idno_home_tel_no1");
            hIdnoHomeTelExt1 = getValue("h_idno_home_tel_ext1");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void insertActPayError() throws Exception {
        hApdlSerialNo = String.format("%07d", nSerialNo);
        nSerialNo++;

        daoTable = "act_pay_error";
        setValue("batch_no", hApbtBatchNo);
        setValue("serial_no", hApdlSerialNo);
        setValue("p_seqno", hCkapPSeqno);
        setValue("acno_p_seqno", hCkapPSeqno);
        setValue("acct_type", hCkapAcctType);
        setValue("acct_key", hCkapAcctKey);
        setValueDouble("pay_amt", hApdlPayAmt);
        setValue("pay_date", hApdlPayDate);
        setValue("payment_type", "AUT3");
        setValue("error_reason", hAperErrorReason);
        setValue("error_remark", hAperErrorRemark);
        setValue("crt_user", "AIX");
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("mod_user", "AIX");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", prgmId);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_error duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActCorpAutopay() throws Exception {
        daoTable = "act_corp_autopay";
        setValue("business_date", hBusiBusinessDate);
        setValue("p_seqno", hCkapPSeqno);
        setValue("acct_type", hCkapAcctType);
        // setValue ("acct_key" , h_ckap_acct_key);
        setValueDouble("autopay_amt", hCkapTransactionAmt);
        setValueDouble("autopay_amt_bal", hCkapTransactionAmt - hApdlPayAmt);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", prgmId);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_corp_autopay duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectPtrWorkday() throws Exception {
        hWdayThisLastpayDate = "";
        hAcnoStmtCycle = "";
        hAcnoAcctStatus = "";

        sqlCmd = "select a.this_lastpay_date,";
        sqlCmd += " b.stmt_cycle,";
        sqlCmd += " b.acct_status ";
        sqlCmd += "  from act_acno b, ptr_workday a  ";
        sqlCmd += " where b.acno_p_seqno =  ? ";
        sqlCmd += "   and a.stmt_cycle   = b.stmt_cycle ";
        setString(1, hCkapPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_workday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hWdayThisLastpayDate = getValue("this_lastpay_date");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoAcctStatus = getValue("acct_status");
        }

    }

    /***********************************************************************/
    void procSmsMsgDtl() throws Exception {
    	if (checkAcctType() != 0) {
            return;
		    }
    	
        int int1 = 0;
        String tmpstr1 = "";
        String[] dataCode = { "0001", "0002", "0003", "0004", "0005", "2002", "2005", "1001", "1002", "1003", "1004",
                "1005", "1006", "1007", "1008", "1009", "1010" };
        String[] dataMsg = { "存款不足(1)" /* 扣款金額小於應繳金額大於最低應繳金額 */
                , "存款不足(2)" /* 扣款金額小於最低應繳金額但不同意兌換 */
                , "存款不足(3)" /* 經兌換扣足最低應繳金額，如金額有繳足,則不發簡訊 */
                , "存款不足(4)" /* 經兌換仍不足最低應繳金額 */
                , "存款不足(5)" /* 扣款金額小於最低應繳金額且兌換不成功 */
                , "扣款金額為0不同意兌換", "扣款金額為0兌換失敗", "身分證號空白", "生日不為數字或為零", "無此外幣帳號" /* 幣別錯誤或為空白 */
                , "無此外幣帳號" /* 外幣扣帳帳號空白或 APCODE 不是 05,53,57,58 */
                , "應繳金額不為數字或為零", "最低應繳金額不為數字或為零", "台幣帳號有誤", "扣款日期錯誤", "外幣帳號統編與持卡人不同", "台幣帳號統編與持卡人不同" };

        hSmdlCellphoneCheckFlag = "Y";
        hSmdlVoiceFlag = "";

        if (hIdnoCellarPhone.length() != 10)
            hSmdlCellphoneCheckFlag = "N";
        else {
        for (int1 = 0; int1 < 10; int1++)
            if ((hIdnoCellarPhone.toCharArray()[int1] < '0') || (hIdnoCellarPhone.toCharArray()[int1] > '9')) {
                hSmdlCellphoneCheckFlag = "N";
                break;
            }
        }

        tmpstr1 = String.format("其他");
        for (int1 = 0; int1 < dataCode.length; int1++)
            if (hCkapStatusCode.equals(dataCode[int1])) {
                tmpstr1 = String.format("%s", dataMsg[int1]);
                break;
            }

        if (hSmdlCellphoneCheckFlag.equals("N")) {
            /*
             * if (h_idno_home_tel_no1.len!=0) { remark
             */
            hSmdlVoiceFlag = "Y";
            insertActAutopayVoice();
            /* } */
        }

        hSmdlMsgDesc = String.format("%s,%s,%s,%s,合庫商銀,%s", hSmidMsgUserid, hSmidMsgId, hIdnoCellarPhone,
                hIdnoChiName, tmpstr1);
        hTempSmsFlag = 1;
        insertSmsMsgDtl();
    }

    /***********************************************************************/
    int checkAcctType() throws Exception {

        if (hSmidMsgSelAcctType.equals("0"))
            return (0);

        if (hSmidMsgSelAcctType.equals("1")) {
            sqlCmd = "select data_code " + "from sms_dtl_data " + "where table_name='SMS_MSG_ID' " + "and data_key = ? "
                    + "and data_type='1'";
            setString(1, "ACT_A205");
            int recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                String dataCode = getValue("data_code", i);
                if (dataCode.equals(hCkapAcctType)) {
                    return 0;
                }
            }
        }

        return (1);
    }

    /***********************************************************************/
    void insertActAutopayVoice() throws Exception {
        daoTable = "act_autopay_voice";
        setValue("business_date", hBusiBusinessDate);
        setValue("p_seqno", hCkapPSeqno);
        setValue("acct_type", hCkapAcctType);
        // setValue("acct_key" , h_ckap_acct_key);
        setValue("chi_name", hIdnoChiName);
        setValue("home_area_code", hIdnoHomeAreaCode1);
        setValue("home_tel_no", hIdnoHomeTelNo1);
        setValue("home_tel_ext", hIdnoHomeTelExt1);
        setValue("ftp_date", "");
        setValue("ftp_flag", "N");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", prgmId);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_autopay_voice duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertSmsMsgDtl() throws Exception {
        hModSeqno = comcr.getModSeq();

        daoTable = "sms_msg_dtl";
//        setValue("msg_seqno", hApbtModSeqno);
        setValue("msg_seqno", hModSeqno + "" );
        setValue("msg_pgm", "ACT_A205");
        setValue("msg_dept", hSmidMsgDept);
        setValue("msg_userid", hSmidMsgUserid);
        setValue("msg_id", hSmidMsgId);
        setValue("cellar_phone", hIdnoCellarPhone);
        setValue("cellphone_check_flag", hSmdlCellphoneCheckFlag);
        setValue("chi_name" , hIdnoChiName);
        setValue("msg_desc", hSmdlMsgDesc);
        setValue("p_seqno", hCkapPSeqno); // acct_p_seqno
        setValue("acct_type", hCkapAcctType);
        setValue("id_p_seqno", hCkapIdPSeqno);
        setValue("add_mode", "B");
        setValue("crt_date", sysDate); // add_date
        setValue("crt_time", sysTime); // add_time
        setValue("CRT_USER", "AIX"); // add_user_id
        setValue("apr_date", sysDate); // conf_date      
        setValue("APR_USER", "AIX"); // conf_user_id
        setValue("apr_flag", "Y");
        setValue("SEND_FLAG", "N"); // msg_status
        setValue("proc_flag", "N"); //
        setValue("mod_user", "AIX");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_sms_msg_dtl duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActChkautopay() throws Exception {
    	int i = 1;
        daoTable = "act_chkautopay";
        updateSQL = " sms_add_date  = decode(cast(? as varchar(8)), 0, sms_add_date, to_char(sysdate,'yyyymmdd')),";
        setInt(i++, hTempSmsFlag);
        if("1".equals(hAutopayCounts)) {
        	updateSQL += "status_code = ?,";
            updateSQL += " transaction_amt = ?,";
        	updateSQL += " DEDUCT_AMT_1 = ?,";
        	updateSQL += " DEDUCT_AMT_2 = ?,";
        	setString(i++, hCkapStatusCode);
            setDouble(i++, hApdlPayAmt);
    		setDouble(i++, hApdlPayAmt);
			setDouble(i++, 0);
        }else if("2".equals(hAutopayCounts)) {
        	updateSQL += "status_code = ?,";
            updateSQL += " transaction_amt = transaction_amt + ?,";
           	updateSQL += " DEDUCT_AMT_2 = ?,";
            setString(i++, hCkapStatusCode);
            setDouble(i++, hApdlPayAmt);
            setDouble(i++, hApdlPayAmt);
        }
        updateSQL += " mod_time        = sysdate,";
        updateSQL += " mod_pgm         = ?";
        whereStr = "where rowid      = ? ";
        setString(i++, javaProgram);
        setRowId(i++, hCkapRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_chkautopay not found!", "", hCallBatchSeqno);
        }

    }
    
    /**
     * @throws Exception *********************************************************************/
    void updateActAutoComf() throws Exception {
    	  daoTable = "ACT_AUTO_COMF";
          updateSQL = "AUTOPAY_COUNTS = AUTOPAY_COUNTS + 1 ";
          whereStr = " WHERE ROWID = ? ";
          setRowId(1,hComfRowid);
          updateTable();
          if (notFound.equals("Y")) {
              comcr.errRtn("update act_auto_comf not found!", "", "");
          }
    }

    /***********************************************************************/
    void insertActA005r1() throws Exception {
        daoTable = "act_a005r1";
        setValue("print_date", hBusiBusinessDate);
        setValue("enter_acct_date", hApdlPayDate);
        setValue("autopay_acct_no", hCkapAutopayAcctNo);
        setValue("from_mark", "02");
        setValue("send_unit", hCkapSendUnit);
        setValue("commerce_type", hCkapCommerceType);
        setValue("data_type", hCkapDataType);
        setValue("desc_code", hCkapDescCode);
        setValueDouble("transaction_amt", hApdlPayAmt);
        setValue("autopay_id", hCkapAutopayId);
        setValue("autopay_id_code", hCkapAutopayIdCode);
        setValue("p_seqno", hCkapPSeqno);
        setValue("acct_type", hCkapAcctType);
        setValue("id_p_seqno", hCkapIdPSeqno);
        setValue("chi_name", hCkapChiName);
        setValue("status_code", hCkapStatusCode);
        setValue("err_type", hA051ErrType);
        setValue("cellphone_no", hIdnoCellarPhone);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_a005r1 duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActPayDetail() throws Exception {
        hApdlSerialNo = String.format("%07d", nSerialNo);
        nSerialNo++;

        daoTable = "act_pay_detail";
        setValue("batch_no", hApbtBatchNo);
        setValue("serial_no", hApdlSerialNo);
        setValue("p_seqno", hCkapPSeqno);
        setValue("acno_p_seqno", hCkapPSeqno);
        setValue("acct_type", hCkapAcctType);
        setValue("id_p_seqno", hCkapIdPSeqno);
        setValueDouble("pay_amt", hApdlPayAmt);
        setValue("pay_date", hApdlPayDate);
        setValue("payment_type", "AUT3");
        setValue("crt_user", "AIX"); // update_user
        setValue("crt_date", sysDate); // update_date
        setValue("crt_time", sysTime); // update_time
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_user", "AIX");
        setValue("mod_pgm", prgmId);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_detail duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertOnbat() throws Exception {
        daoTable = "onbat_2ccas";
        setValue("trans_type", "16");
        setValueInt("to_which", 2);
        setValue("dog", sysDate);
        setValue("proc_mode", "B");
        setValueInt("proc_status", 0);
      //setValue("card_indicator", h_ckap_acct_type.equals("01") ? "1" : "2");
        setValue("card_catalog", hCkapAcctType.equals("01") ? "1" : "2");
        setValue("payment_type", hCkapIdPSeqno.equals("") ? "2" : "1");
        setValue("acct_type", hCkapAcctType);
        setValue("acno_p_seqno ", hCkapPSeqno);
        setValue("id_p_seqno ", hCkapIdPSeqno);// uf_idno_pseqno(card_hldr_id)
        setValue("trans_date", hApdlPayDate);
        setValueDouble("trans_amt", hApdlPayAmt);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_onbat_2ccas duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActA207 proc = new ActA207();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
