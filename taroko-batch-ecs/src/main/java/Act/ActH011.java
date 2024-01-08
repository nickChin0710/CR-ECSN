/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  108/06/19  V1.08.01  黃繼民      RECS-s1070827-074 ACH新規格:cfee, noteb   *
 *  109/02/07  V1.08.01    Brian     update to V1.08.01                        *
 *  111/10/12  V1.08.07  jiangyigndong  updated for project coding standard    *
 *  112/05/23  V1.08.08  Simon       clone from R6 dir:/crdataupload/          *
 *  112/06/05  V1.08.09  Simon       1.copy from R6 dir:/crdataupload/ & delete it*
 *                                   2.acct_bank not in ('017','700') changed into *
 *                                     substr(acct_bank,1,3) not in ('006','700')*
 *  112/06/24  V1.08.10  mHung3      簡訊修改                                  *
 *  112/07/12  V1.08.11  Simon       簡訊修改                                  *
 *  112/07/13  V1.08.12  Simon       簡訊紀錄(sms_msg_dtl)內容修正             *
 *  112/09/01  V1.08.13  Simon       1.批次自動覆核                            *
 *                                   2.若只回覆首、尾筆時，以營業日為扣款日讀取扣款查核檔*
 *  112/12/02  V1.08.14  Simon       1.accept consecutive-flag                 *
 *                                   2.input file non-consecutiive layout error handle*
 *                                   3.readfile() 可能已有 insert act_pay_detail，*
 *                                     selectSucess() 不可 set nSerialNo=0     *
 *  113/01/05  V1.08.15  Simon       接收無頭、尾筆空檔處理                    *
 ******************************************************************************/

package Act;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

/*ACH接收他行自動扣繳檔程式*/
public class ActH011 extends AccessDAO {

  private final String PROGNAME = "ACH接收他行自動扣繳檔程式 "
                                + "113/01/05  V1.08.15";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
	CommString comStr = new CommString();
  CommCrdRoutine comcr = null;

  String prgmId = "ActH011";
  String rptName1 = "";
  List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
  String errCode = "";
  String errDesc = "";
  String procDesc = "";
  int rptSeq1 = 0;
  int errCnt = 0;
  String ErrMsg = "";
  String buf = "";
  String szTmp = "";
  String stderr = "";
  long hModSeqno = 0;
  String ecsServer = "";
  String hModUser = "";
  String hModTime = "";
  String hModPgm = "";
  String hCallBatchSeqno = "";
  String iFileName = "";
  String iPostDate = "";

//String hTempUser = "";
  String hBusiBusinessDate = "";
  String hConsecutive = "";

//String hSystemDate = "";
  String hAoayAutopayAcctNo = "";
  String hAoayPSeqno = "";
  String hAoayAcctType = "";
  String hAoayAcctKey = "";
  String hAoayFromMark = "";
  String hAoayStmtCycle = "";
  double hAoayTransactionAmt = 0;
  String hAoayAutopayId = "";
  String hTempStatusCode = "";
  String hAoayIdPSeqno = "";
  String hAoayId = "";
  String hAoayIdCode = "";
  String hAoayChiName = "";
  String hAoayRowid = "";
  String hB021AcctBank = "";
  String hAoayEnterAcctDate = "";
  String hAoayStatusCode = "";
  String hApbtBatchNo = "";
  double hApdlPayAmt = 0;
  String hAoayAutopayIdCode = "";
  String hB021ErrType = "";
  String hIdnoCellarPhone = "";
  String hApdlSerialNo = "";
  String hApdlPayDate = "";
  String hAperErrorReason = "";
  String hAperErrorRemark = "";
  String hApbtModUser = "";
  String hApbtModPgm = "";
  long hTempBatchNoSeq = 0;
  String hTempBatchNo = "";
  int hApbtBatchTotCnt = 0;
  double hApbtBatchTotAmt = 0;
  String hApbtModSeqno = "";
  String hWdayThisLastpayDate = "";
  String hIdnoChiName = "";
  String hIdnoBirthday = "";
  String hIdnoHomeAreaCode1 = "";
  String hIdnoHomeTelNo1 = "";
  String hIdnoHomeTelExt1 = "";
  String hSmidMsgId = "";
  String hSmidMsgDept = "";
  String hSmidMsgSendFlag = "";
  String hSmidMsgSelAcctType = "";
  String hSmidMsgAcctType = "";
  String hSmidMsgUserid = "";
  String hSmdlCellphoneCheckFlag = "";
  String hSmdlVoiceFlag = "";
  String hWdayThisCloseDate = "";
  String hPbcdBcAbname = "";
  String hBatchnoExist = "";
  String temstr0 = "";
  String temstr1 = "";
  int nSerialNo = 0;
  int hBofEofCnt = 0;
  int totCnt = 0;
  int rightNo = 0;
  int errorNo = 0;
  int errorNoR1 = 0;
  String str600 = "";
  String wsNotFound = "";
  String hCallRProgramCode = "";
  String hCallErrorDesc = "";
  String hSmidAcctTypeCond = "";
  int smsTag = 0;

  buf1 detailSt = new buf1();

  public int mainProcess(String[] args) {

    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + PROGNAME);
      // =====================================
      //if (args.length > 2) {
      //    comc.errExit("Usage : ActH011 [proc_date] [batch_seq]", "");
      //}

      // 固定要做的

      if (!connectDataBase()) {
          comc.errExit("connect DataBase error", "");
      }

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

      /***
      hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

      hTempUser = comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);
      if (hTempUser.length() == 0) {
          hModUser = comc.commGetUserID();
          hTempUser = hModUser;
      }
      ***/

      if (args.length > 2)
      {
        showLogMessage("I","","請輸入參數:");
        showLogMessage("I","","PARM 1 : [BUSINESS_DATE]/['c']");
        showLogMessage("I","","PARM 2 : ['c']");
        return(0);
      }

      if ( args.length == 1 ) {
      	if (args[0].length()==8) {
          hBusiBusinessDate = args[0]; 
      	} else if (args[0].equalsIgnoreCase("c")) {
          hConsecutive  = "C";
        }
      } else if ( args.length == 2 ) {
      	if (args[0].length()==8) {
          hBusiBusinessDate = args[0]; 
      	} 
      	if (args[1].equalsIgnoreCase("c")) {
          hConsecutive  = "C";
        } else {
          showLogMessage("I","","請輸入參數:");
          showLogMessage("I","","PARM 1 : [BUSINESS_DATE]/['c']");
          showLogMessage("I","","PARM 2 : ['c']");
          return(0);
        }
      }
      
      selectPtrBusinday();
    //if (args.length > 0 && args[0].length() == 8)
    //  hBusiBusinessDate = args[0];

      iFileName = "ACHR01.TXT";

      hModUser = comc.commGetUserID();
      hApbtModUser = hModUser;
      hApbtModPgm = javaProgram;

      /*-- read 簡訊參數檔 --*/
      selectSmsMsgId();

      //select_act_ach_bank(); /* mike For Message */

      checkFopen();

      readFile();
      showLogMessage("I","","=== hBofEofCnt : "+hBofEofCnt);
      if ( hBofEofCnt > 0 ) {
        selectSucess();
      }

      comcr.hCallErrorDesc = String.format("程式執行結束,總筆數=[%d]", rightNo + errorNoR1);
      showLogMessage("I", "", String.format("%s", hCallErrorDesc));

      String fs = String.format("%s/media/act/%s", 
      comc.getECSHOME(), iFileName);
      String ft = String.format("%s/media/act/backup/%s.%s", 
      comc.getECSHOME(), iFileName, sysDate+sysTime);
      if (comc.fileRename(fs, ft) == false) {
          comcr.errRtn(String.format("無法搬移檔案[%s]", fs), "", hCallBatchSeqno);
      }
    	showLogMessage("I", "", "檔案 [" + temstr0 + "] 已備份至 [" + ft + "]");

    	if (comc.fileDelete(temstr0) == false) {
    		showLogMessage("I", "", "來源檔案 [" + temstr0 + "] 刪除失敗!");
  	  }

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
  public int  selectPtrBusinday() throws Exception
  {
    daoTable    = "ptr_businday";
    extendField = "busi.";
    selectSQL   = "decode( cast(? as varchar(8)) ,'',business_date, ? ) "
                + "as business_date ";
    setString(1, hBusiBusinessDate);
    setString(2, hBusiBusinessDate);
    whereStr    = " fetch first 1 rows only ";
    int n = selectTable();
    if ( n == 0 )
       { showLogMessage("E","","select_ptr_businday ERROR "); exitProgram(3); }
    hBusiBusinessDate = getValue("busi.business_date");
    return n;
  }
	
    /***********************************************************************/
    void selectSmsMsgId() throws Exception {
        hSmidMsgId = "";
        hSmidMsgDept = "";
        hSmidMsgSendFlag = "N";
        hSmidAcctTypeCond = "";
        hSmidMsgUserid = "";

        sqlCmd = "select msg_id,";
        sqlCmd += "msg_dept,";
        sqlCmd += "msg_send_flag,";
        sqlCmd += "decode(ACCT_TYPE_SEL,'','0',ACCT_TYPE_SEL) h_smid_acct_type_cond,";
        sqlCmd += "msg_userid ";
        sqlCmd += " from sms_msg_id  ";
      //sqlCmd += "where msg_pgm ='ActA205'  ";
        sqlCmd += "where msg_pgm ='ActB102'  ";
        sqlCmd += "and decode(msg_send_flag,'','N',msg_send_flag) ='Y' ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hSmidMsgId = getValue("msg_id");
            hSmidMsgDept = getValue("msg_dept");
            hSmidMsgSendFlag = getValue("msg_send_flag");
            hSmidAcctTypeCond = getValue("h_smid_acct_type_cond");
            hSmidMsgUserid = getValue("msg_userid");
        } else {
            showLogMessage("I", "", String.format("--簡訊暫停發送 (SMS_P010)"));
            smsTag = 1;
        }

    }

    /***********************************************************************/
    void readFile() throws Exception {
        nSerialNo = 0;

        selectActPayBatch();
        hApbtBatchTotCnt = 0;
        hApbtBatchTotAmt = 0;

        int br = 0;

        int readlen = 0;
        byte[] bytes = new byte[250];

      //openBinaryInput(temstr1);
        if (hConsecutive.equals("C")) {
          openBinaryInput(temstr1);
        } else {
          br = openInputText(temstr1, "MS950");
        }

      //while ((readlen = readBinFile(bytes)) > 0) {
      //    str600 = new String(bytes, 0, readlen, "MS950");
        if (hConsecutive.equals("C")) {
          while ((readlen = readBinFile(bytes)) > 0) {
            str600 = new String(bytes, 0, readlen, "MS950");

            if (str600.length() < 10)
                continue;
            if (str600.substring(0, 3).equals("BOF") || str600.substring(0, 3).equals("EOF")) { 
                hBofEofCnt++;
                continue;
            }
            splitBuf1(str600);
            getField();
            wsNotFound = "N";
            selectActOtherApay();
            hIdnoCellarPhone = "";
            if (wsNotFound.equals("Y"))
                continue;
            if (!hAoayStatusCode.equals("00")) {
                hB021ErrType = "2";
                /* --AAA--簡訊--AAA-- */
                if (!hAoayStatusCode.equals("XX")) {
                  //if (hAoayAcctType.equals("02")) {
                    if (hAoayIdPSeqno.length() == 0) {
                        insertActCorpAutopay();
                    } else if (hSmidMsgSendFlag.equals("Y")) {
                        selectCrdIdno();
                        selectActAchBank();

                      //if (checkAcctType() == 0) {
                        szTmp = "";
                        szTmp = String.format("%s,%s,%s,%s,%s,代扣失敗", hSmidMsgUserid, hSmidMsgId,
                                hIdnoCellarPhone, hIdnoChiName, hPbcdBcAbname);
                        insertSmsMsgDtl();
                      //} 
                    }
                }
                /* --VVV--mike For Message--VVV-- */
                insertActB002R1();
            } else {
                setPayDateRtn();
                insertActPayDetail();
                hApbtBatchTotCnt++;
                hApbtBatchTotAmt = hApbtBatchTotAmt + hApdlPayAmt;
            }
          }
        } else {
          while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            if (str600.length() < 10)
                continue;
            if (str600.length() > 250) {
              comcr.errRtn("input file layout error! comStr.mid(str600, 0, 1000) = "+
              comStr.mid(str600, 0, 1000),"","");
            }
            if (str600.substring(0, 3).equals("BOF") || str600.substring(0, 3).equals("EOF")) {
                hBofEofCnt++;
                continue;
            }
            splitBuf1(str600);
            getField();
            wsNotFound = "N";
            selectActOtherApay();
            hIdnoCellarPhone = "";
            if (wsNotFound.equals("Y"))
                continue;
            if (!hAoayStatusCode.equals("00")) {
                hB021ErrType = "2";
                /* --AAA--簡訊--AAA-- */
                if (!hAoayStatusCode.equals("XX")) {
                  //if (hAoayAcctType.equals("02")) {
                    if (hAoayIdPSeqno.length() == 0) {
                        insertActCorpAutopay();
                    } else if (hSmidMsgSendFlag.equals("Y")) {
                        selectCrdIdno();
                        selectActAchBank();

                      //if (checkAcctType() == 0) {
                        szTmp = "";
                        szTmp = String.format("%s,%s,%s,%s,%s,代扣失敗", hSmidMsgUserid, hSmidMsgId,
                                hIdnoCellarPhone, hIdnoChiName, hPbcdBcAbname);
                        insertSmsMsgDtl();
                      //} 
                    }
                }
                /* --VVV--mike For Message--VVV-- */
                insertActB002R1();
            } else {
                setPayDateRtn();
                insertActPayDetail();
                hApbtBatchTotCnt++;
                hApbtBatchTotAmt = hApbtBatchTotAmt + hApdlPayAmt;
            }
          }
        }
        //closeBinaryInput();
        if (hConsecutive.equals("C")) {
          closeBinaryInput();
        } else {
          closeInputText(br);
        }

        if (hApbtBatchTotCnt != 0)
            insertActPayBatch();
    }

    /***********************************************************************/
    void selectActPayBatch() throws Exception {
        String tempstr = "";

        tempstr = String.format("%s1002%3.3s%c", hBusiBusinessDate, "000", '%');
        hTempBatchNo = tempstr;

        sqlCmd = "select to_number(substr(max(batch_no),16,1))+1 h_temp_batch_no_seq ";
        sqlCmd += " from act_pay_batch  ";
        sqlCmd += "where batch_no like ? ";
        setString(1, hTempBatchNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempBatchNoSeq = getValueLong("h_temp_batch_no_seq");
        }

        if (hTempBatchNoSeq != 0) {
            tempstr = String.format("%15.15s%01d", hTempBatchNo, hTempBatchNoSeq);
        } else {
            tempstr = String.format("%15.15s1", hTempBatchNo);
        }
        hApbtBatchNo = tempstr;

        showLogMessage("I", "", String.format("處 理 批 號 =[%s]", hApbtBatchNo));

    }

    /***********************************************************************/
    void getField() throws Exception {

        hB021AcctBank = "";
        hAoayAutopayAcctNo = "";
        hApdlPayAmt = 0;
        hAoayStatusCode = "";
        hAoayAutopayId = "";
        hAoayPSeqno = "";
        hAoayEnterAcctDate = "";

        String tmpStr = comc.eraseSpace(detailSt.pRclno);
        hAoayAutopayAcctNo = tmpStr;

        tmpStr = comc.eraseSpace(detailSt.pAmt);
        hApdlPayAmt = comcr.str2double(tmpStr);

        tmpStr = comc.eraseSpace(detailSt.pRbank);
        hB021AcctBank = tmpStr;

        tmpStr = comc.eraseSpace(detailSt.pRcode);
        hAoayStatusCode = tmpStr;

        if (hAoayStatusCode.equals("90")) {
            comcr.errRtn(String.format("%s", "ERROR 資訊處回覆status_code = 90，票交所未回覆結果!!!"), "", hCallBatchSeqno);
        }

        tmpStr = comc.eraseSpace(detailSt.pPid);
        hAoayAutopayId = tmpStr;

        tmpStr = comc.eraseSpace(detailSt.pCno);
        hAoayPSeqno = tmpStr;
        sqlCmd = "select autopay_acct_no ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acno_p_seqno = ? ";
        setString(1, hAoayPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        //showLogMessage("I", "", "select_act_acno not found! p_seqno = "+hAoayPSeqno);
          comcr.errRtn("select_act_acno not found! p_seqno = ["+
          hAoayPSeqno+"]", "", "");
        }
        if (recordCnt > 0) {
            hAoayAutopayAcctNo = getValue("autopay_acct_no");
        }

        tmpStr = comc.eraseSpace(detailSt.pNote);
        hAoayEnterAcctDate = tmpStr;

    }

    /***********************************************************************/
    void selectActOtherApay() throws Exception {
        hAoayAcctType = "";
        hAoayAcctKey = "";
        hAoayFromMark = "";
        hAoayStmtCycle = "";
        hAoayTransactionAmt = 0;
        hAoayAutopayId = "";
        hTempStatusCode = "";
        hAoayIdPSeqno = "";
        hAoayId = "";
        hAoayIdCode = "";
        hAoayChiName = "";
        hAoayRowid = "";

        sqlCmd = "select a.acct_type,";
        sqlCmd += " b.acct_key,";
        sqlCmd += " a.from_mark,";
        sqlCmd += " a.stmt_cycle,";
        sqlCmd += " a.transaction_amt,";
        sqlCmd += " a.autopay_id,";
        sqlCmd += " a.status_code,";
        sqlCmd += " a.id_p_seqno,";
        sqlCmd += " c.id_no, "; // act_other_apay.id not found
        sqlCmd += " c.id_no_code, "; // act_other_apay.id_code not found
        sqlCmd += " a.chi_name,";
        sqlCmd += " a.rowid rowid ";
        sqlCmd += " from act_other_apay a, act_acno b ";
        sqlCmd += " left join crd_idno c on a.id_p_seqno = c.id_p_seqno ";
        sqlCmd += "where a.acct_bank       = ?  ";
        sqlCmd += " and a.enter_acct_date = ?  ";
        sqlCmd += " and a.p_seqno         = ?  ";
        sqlCmd += " and a.from_mark       = '01' ";
        sqlCmd += " and a.p_seqno         =  b.acno_p_seqno ";
        setString(1, hB021AcctBank);
        setString(2, hAoayEnterAcctDate);
        setString(3, hAoayPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAoayAcctType = getValue("acct_type");
            hAoayAcctKey = getValue("acct_key");
            hAoayFromMark = getValue("from_mark");
            hAoayStmtCycle = getValue("stmt_cycle");
            hAoayTransactionAmt = getValueDouble("transaction_amt");
            hAoayAutopayId = getValue("autopay_id");
            hTempStatusCode = getValue("status_code");
            hAoayIdPSeqno = getValue("id_p_seqno");
            hAoayId = getValue("id_no");
            hAoayIdCode = getValue("id_no_code");
            hAoayChiName = getValue("chi_name");
            hAoayRowid = getValue("rowid");
        }

        if (notFound.equals("Y")) {
            wsNotFound = "Y";
            hB021ErrType = "0";
            insertActB002R1();
            if (hAoayStatusCode.equals("00")) {
                hAperErrorReason = "302";
                hAperErrorRemark = "ACH1 act_other_apay not found";
                insertActPayError();
            }
        } else {
            if (!hTempStatusCode.equals("XX")) {
                wsNotFound = "Y";
                hB021ErrType = "1";
                insertActB002R1();
                if (hAoayStatusCode.equals("00")) {
                    hAperErrorReason = "301";
                    hAperErrorRemark = "ACH1 duplicated update act_other_apay";
                    setPayDateRtn();
                    insertActPayError();
                }
            }
            if (hAoayTransactionAmt != hApdlPayAmt) {
                wsNotFound = "Y";
                hB021ErrType = "0";
                insertActB002R1();
                if (hAoayStatusCode.equals("00")) {
                    hAperErrorReason = "303";
                    hAperErrorRemark = "ACH1 transaction amt not match";
                    setPayDateRtn();
                    insertActPayError();
                }
            } else {
                updateActOtherApay();
            }
        }
    }

    /***********************************************************************/
    void insertActPayError() throws Exception {
        String tmpstr = "";

        nSerialNo++;
        tmpstr = String.format("%07", nSerialNo);
        hApdlSerialNo = tmpstr;
        //n_serial_no++;
        errorNo++;

        daoTable = "act_pay_error";
        extendField = daoTable + ".";
        setValue(extendField + "batch_no", hApbtBatchNo);
        setValue(extendField + "serial_no", hApdlSerialNo);
        setValue(extendField + "p_seqno", hAoayPSeqno);
        setValue(extendField + "acno_p_seqno", hAoayPSeqno);
        setValue(extendField + "acct_type", hAoayAcctType);
        setValueDouble(extendField + "pay_amt", hApdlPayAmt);
        setValue(extendField + "pay_date", hApdlPayDate);
        setValue(extendField + "payment_type", "ACH1");
        setValue(extendField + "error_reason", hAperErrorReason);
        setValue(extendField + "error_remark", hAperErrorRemark);
        setValue(extendField + "crt_user", hApbtModUser);
        setValue(extendField + "crt_date", sysDate);
        setValue(extendField + "crt_time", sysTime);
        setValue(extendField + "mod_user", hApbtModUser);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", hApbtModPgm);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_error duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActOtherApay() throws Exception {
        daoTable = "act_other_apay";
        updateSQL = "status_code = ?,";
        updateSQL += " batch_no  = ?,";
        updateSQL += " mod_pgm   = 'ActH011'";
        whereStr = "where rowid  = ? ";
        setString(1, hAoayStatusCode);
        setString(2, hApbtBatchNo);
        setRowId(3, hAoayRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_other_apay not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActCorpAutopay() throws Exception {
        daoTable = "act_corp_autopay";
        extendField = daoTable + ".";
        setValue(extendField + "business_date", hBusiBusinessDate);
        setValue(extendField + "p_seqno", hAoayPSeqno);
        setValue(extendField + "acct_type", hAoayAcctType);
        setValueDouble(extendField + "autopay_amt", hAoayTransactionAmt);
        setValueDouble(extendField + "autopay_amt_bal", hAoayTransactionAmt - hApdlPayAmt);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", javaProgram);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_corp_autopay duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hIdnoChiName = "";
        hIdnoBirthday = "";
        hIdnoCellarPhone = "";
        hIdnoHomeAreaCode1 = "";
        hIdnoHomeTelNo1 = "";
        hIdnoHomeTelExt1 = "";

        sqlCmd = "select chi_name,";
        sqlCmd += " birthday,";
        sqlCmd += " cellar_phone,";
        sqlCmd += " decode(c.home_tel_no1, '', c.home_area_code2, c.home_area_code1) h_idno_home_area_code1,";
        sqlCmd += " decode(c.home_tel_no1, '', c.home_tel_no2,    c.home_tel_no1)    h_idno_home_tel_no1,";
        sqlCmd += " decode(c.home_tel_no1, '', c.home_tel_ext2,   c.home_tel_ext1)   h_idno_home_tel_ext1 ";
        sqlCmd += " from crd_idno c  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hAoayIdPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hIdnoChiName = getValue("chi_name");
            hIdnoBirthday = getValue("birthday");
            hIdnoCellarPhone = getValue("cellar_phone");
            hIdnoHomeAreaCode1 = getValue("h_idno_home_area_code1");
            hIdnoHomeTelNo1 = getValue("h_idno_home_tel_no1)");
            hIdnoHomeTelExt1 = getValue("h_idno_home_tel_ext1)");
        }
    }

    /***********************************************************************/
    void selectActAchBank() throws Exception {

      hPbcdBcAbname = "";
      sqlCmd = "select bank_name ";
      sqlCmd += " from act_ach_bank  ";
      sqlCmd += "where bank_no like ? || '%'  ";
      sqlCmd += "fetch first 1 rows only ";
      setString(1, hB021AcctBank);
      int recordCnt = selectTable();
      if (recordCnt > 0) {
          hPbcdBcAbname = getValue("bank_name");
      }

    }

    /***********************************************************************/
    int checkAcctType() throws Exception {

        if (hSmidAcctTypeCond.equals("0"))
            return (0);

        if (hSmidAcctTypeCond.equals("1")) {
            sqlCmd = "select data_code " + "from sms_dtl_data " + "where table_name='SMS_MSG_ID' " + "and data_key = ? "
                    + "and data_type='1'";
            setString(1, "ACT_B002");
            int recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                String dataCode = getValue("data_code", i);
                if (dataCode.equals(hAoayAcctType)) {
                    return 0;
                }
            }
        }

        return (1);
    }

    /***********************************************************************/
    void insertSmsMsgDtl() throws Exception {

        /*--cellphone format check--*/
        hSmdlCellphoneCheckFlag = "Y";
        hSmdlVoiceFlag = "";
        if (checkPhoneNo(hIdnoCellarPhone) != 0) {
            hSmdlCellphoneCheckFlag = "N";

            hSmdlVoiceFlag = "Y";
            insertActAutopayVoice();

        }

        hModSeqno = comcr.getModSeq();

        daoTable = "sms_msg_dtl";
        extendField = daoTable + ".";
        //setValue(extendField + "msg_seqno", h_apbt_mod_seqno);
        setValue(extendField + "msg_seqno", hModSeqno + "");
      //setValue(extendField + "msg_pgm", "ACT_B002");
        setValue(extendField + "msg_pgm", "ActH011");
        setValue(extendField + "msg_dept", hSmidMsgDept);
        setValue(extendField + "msg_userid", hSmidMsgUserid);
        setValue(extendField + "msg_id", hSmidMsgId);
        setValue(extendField + "cellar_phone", hIdnoCellarPhone);
        setValue(extendField + "cellphone_check_flag", hSmdlCellphoneCheckFlag);
        setValue(extendField + "voice_flag", hSmdlVoiceFlag);
      //setValue(extendField + "holder_name", hIdnoChiName);
        setValue(extendField + "CHI_NAME", hIdnoChiName); // holder_name
        setValue(extendField + "msg_desc", szTmp);
        setValue(extendField + "acct_type", hAoayAcctType);
        setValue(extendField + "p_seqno", hAoayPSeqno);
        setValue(extendField + "id_p_seqno", hAoayIdPSeqno);
        setValue(extendField + "id_no", hAoayId);
        setValue(extendField + "add_mode", "B");
        setValue(extendField + "crt_date", sysDate);
        setValue(extendField + "crt_time", sysTime);
        setValue(extendField + "crt_user", hApbtModUser);
        setValue(extendField + "apr_date", sysDate);
        setValue(extendField + "apr_user", hApbtModUser);
        setValue(extendField + "apr_flag", "Y");
        setValue(extendField + "SEND_FLAG", "N"); // msg_status
        setValue(extendField + "proc_flag", "N"); //
        setValue(extendField + "mod_user", hApbtModUser);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", hApbtModPgm);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_sms_msg_dtl duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int checkPhoneNo(String pno) throws Exception {
        int i;
        String str10 = "";

        if (pno.length() != 10)
            return (1);

        str10 = pno;

        for (i = 0; i < 10; i++) {
            if ((str10.toCharArray()[i] < '0') || (str10.toCharArray()[i] > '9'))
                return (1);
        }

        return (0);
    }

    /***********************************************************************/
    void insertActAutopayVoice() throws Exception {
        daoTable = "act_autopay_voice";
        extendField = daoTable + ".";
        setValue(extendField + "business_date", hBusiBusinessDate);
        setValue(extendField + "p_seqno", hAoayPSeqno);
        setValue(extendField + "acct_type", hAoayAcctType);
        setValue(extendField + "chi_name", hIdnoChiName);
        setValue(extendField + "home_area_code", hIdnoHomeAreaCode1);
        setValue(extendField + "home_tel_no", hIdnoHomeTelNo1);
        setValue(extendField + "home_tel_ext", hIdnoHomeTelExt1);
        setValue(extendField + "ftp_date", "");
        setValue(extendField + "ftp_flag", "N");
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", javaProgram);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_autopay_voice duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActB002R1() throws Exception {
        errorNoR1++;
        daoTable = "act_b002r1";
        extendField = daoTable + ".";
        setValue(extendField + "batch_no", hApbtBatchNo);
        setValue(extendField + "print_date", hBusiBusinessDate);
        setValue(extendField + "enter_acct_date", hAoayEnterAcctDate);
        setValue(extendField + "acct_bank", hB021AcctBank);
        setValue(extendField + "autopay_acct_no", hAoayAutopayAcctNo);
        setValue(extendField + "from_mark", "01");
        setValue(extendField + "stmt_cycle", hAoayStmtCycle);
        setValueDouble(extendField + "transaction_amt", hApdlPayAmt);
        setValue(extendField + "autopay_id", hAoayAutopayId);
        setValue(extendField + "autopay_id_code", hAoayAutopayIdCode);
        setValue(extendField + "p_seqno", hAoayPSeqno);
        setValue(extendField + "acct_type", hAoayAcctType);
        //setValue(extendField + "acct_key", h_aoay_acct_key);
        setValue(extendField + "id_p_seqno", hAoayIdPSeqno);
        //setValue(extendField + "id_no", h_aoay_id);
        //setValue(extendField + "id_code", h_aoay_id_code);
        setValue(extendField + "chi_name", hAoayChiName);
        setValue(extendField + "status_code", hAoayStatusCode);
        setValue(extendField + "err_type", hB021ErrType);
        setValue(extendField + "cellphone_no", hIdnoCellarPhone);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_b002r1 duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActPayBatch() throws Exception {
        daoTable = "act_pay_batch";
        extendField = daoTable + ".";
        setValue(extendField + "batch_no", hApbtBatchNo);
        setValueInt(extendField + "batch_tot_cnt", hApbtBatchTotCnt);
        setValueDouble(extendField + "batch_tot_amt", hApbtBatchTotAmt);
        setValue(extendField + "crt_user", hApbtModUser);
      //setValue(extendField + "crt_date", sysDate);
        setValue(extendField + "crt_date", hBusiBusinessDate);
        setValue(extendField + "crt_time", sysTime);
        setValue(extendField + "trial_user", hApbtModUser);
      //setValue(extendField + "trial_date", sysDate);
        setValue(extendField + "trial_date", hBusiBusinessDate);
        setValue(extendField + "trial_time", sysTime);
        setValue(extendField + "confirm_user", hApbtModUser);
        setValue(extendField + "confirm_date", hBusiBusinessDate);
        setValue(extendField + "confirm_time", sysTime);
        setValue(extendField + "curr_code", "901");
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_user", hApbtModUser);
        setValue(extendField + "mod_pgm", hApbtModPgm);

        insertTable();
        if (!dupRecord.equals("Y"))
            return;

        hModSeqno = comcr.getModSeq();

        daoTable = "act_pay_batch";
        updateSQL = " batch_tot_cnt = batch_tot_cnt + ?,";
        updateSQL += " batch_tot_amt = batch_tot_amt + ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_seqno  = mod_seqno + 1 ";
        whereStr = " where batch_no = ? ";
        setInt(1, hApbtBatchTotCnt);
        setDouble(2, hApbtBatchTotAmt);
        setString(3, hApbtModUser);
        setString(4, hApbtModPgm);
        //setString(5, h_mod_seqno + "");
        setString(5, hApbtBatchNo);
        updateTable();

    }

    /***********************************************************************/
    void updateActPayBatch() throws Exception {
        hModSeqno = comcr.getModSeq();

        daoTable = "act_pay_batch";
        updateSQL = " batch_tot_cnt = batch_tot_cnt + ?,";
        updateSQL += " batch_tot_amt = batch_tot_amt + ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_seqno  = ?";
        whereStr = " where batch_no = ? ";
        setInt(1, hApbtBatchTotCnt);
        setDouble(2, hApbtBatchTotAmt);
        setString(3, hApbtModUser);
        setString(4, hApbtModPgm);
        //setString(5, h_apbt_mod_seqno);
        setString(5, hModSeqno + "");
        setString(6, hApbtBatchNo);
        updateTable();

    }

    /***********************************************************************/
    void selectSucess() throws Exception {
      //nSerialNo = 0; readfile() 可能已有 insert act_pay_detail
        totCnt = 0;
        hApbtBatchTotAmt = 0;

        hApbtBatchTotCnt = 0;
        hApbtBatchTotAmt = 0;
        if (hAoayEnterAcctDate.length() == 0) {
            hAoayEnterAcctDate = hBusiBusinessDate;
        } 

        /* select_act_pay_batch(); */

        sqlCmd = "select ";
        sqlCmd += " a.p_seqno,";
        sqlCmd += " a.acct_type,";
        sqlCmd += " a.from_mark, ";
        sqlCmd += " a.stmt_cycle, ";
        sqlCmd += " a.transaction_amt, ";
        sqlCmd += " a.autopay_id, ";
        sqlCmd += " a.status_code, ";
        sqlCmd += " a.id_p_seqno, ";
        sqlCmd += " c.id_no, "; // act_other_apay.id not found
        sqlCmd += " c.id_no_code, "; // act_other_apay.id_code not found
        sqlCmd += " a.chi_name, ";
        sqlCmd += " a.stmt_cycle, ";
        sqlCmd += " a.rowid rowid ";
        sqlCmd += "from act_other_apay a ";
        sqlCmd += "left join crd_idno c on a.id_p_seqno = c.id_p_seqno ";
        sqlCmd += "where enter_acct_date = ? ";
        sqlCmd += " and status_code     = 'XX' ";
        sqlCmd += " and substr(acct_bank,1,3) not in ('006','700') ";
        sqlCmd += " and from_mark       = '01' ";

        setString(1, hAoayEnterAcctDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAoayPSeqno = getValue("p_seqno");
            hAoayAcctType = getValue("acct_type");
            hAoayFromMark = getValue("from_mark");
            hAoayStmtCycle = getValue("stmt_cycle");
            hApdlPayAmt = getValueDouble("transaction_amt");
            hAoayAutopayId = getValue("autopay_id");
            hAoayStatusCode = getValue("status_code");
            hAoayIdPSeqno = getValue("id_p_seqno");
            hAoayId = getValue("id_no");
            hAoayIdCode = getValue("id_code");
            hAoayChiName = getValue("chi_name");
            hAoayStmtCycle = getValue("stmt_cycle");
            hAoayRowid = getValue("rowid");

            totCnt++;

            setPayDateRtn();
            insertActPayDetail();
            updateActOtherApay1();

            hApbtBatchTotCnt++;
            hApbtBatchTotAmt = hApbtBatchTotAmt + hApdlPayAmt;

            if (totCnt % 1000 == 0 || totCnt == 1000)
                showLogMessage("I", "", String.format("Process record=[%d]", totCnt));

            /* ECSprintf(stderr,"\n%s\n","1141"); */
        }
        closeCursor(cursorIndex);

        //insert 一筆資料後未 commit 前再insert 同一筆資料時，可能會發生 deadlock ?!
        //insert 前先 select 已存在就不 insert, 改用 update。
        hBatchnoExist = "N";
        checkActPayBatch();

        if (hApbtBatchTotCnt != 0) {
            if (hBatchnoExist.equals("N")) {
                insertActPayBatch();
            } else {
                updateActPayBatch();
            }
        }
    }

    /***********************************************************************/
    void checkActPayBatch() throws Exception {
        int cntInt = 0;

        sqlCmd = "select count(*) cnt_int ";
        sqlCmd += "  from act_pay_batch  ";
        sqlCmd += " where batch_no = ? ";

        setString(1, hApbtBatchNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            cntInt = getValueInt("cnt_int");
        }

        if (cntInt > 0) {
            hBatchnoExist = "Y";
        }

    }

    /***********************************************************************/
    void setPayDateRtn() throws Exception {

        hWdayThisCloseDate = "";
        sqlCmd = "select this_lastpay_date ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where stmt_cycle = ? ";
        setString(1, hAoayStmtCycle);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_workday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hWdayThisLastpayDate = getValue("this_lastpay_date");
        }

        hApdlPayDate = hWdayThisLastpayDate;
    }

    /***********************************************************************/
    void insertActPayDetail() throws Exception {
        String tmpstr = "";

        nSerialNo++;
        tmpstr = String.format("%07d", nSerialNo);
        hApdlSerialNo = tmpstr;
        //n_serial_no++;
        rightNo++;

        daoTable = "act_pay_detail";
        extendField = daoTable + ".";
        setValue(extendField + "batch_no", hApbtBatchNo);
        setValue(extendField + "serial_no", hApdlSerialNo);
        setValue(extendField + "p_seqno", hAoayPSeqno);
        setValue(extendField + "acno_p_seqno", hAoayPSeqno);
        setValue(extendField + "acct_type", hAoayAcctType);
        setValue(extendField + "id_p_seqno", hAoayIdPSeqno);
        setValueDouble(extendField + "pay_amt", hApdlPayAmt);
        setValue(extendField + "pay_date", hApdlPayDate);
        setValue(extendField + "payment_type", "ACH1");
        setValue(extendField + "crt_user", hApbtModUser);
      //setValue(extendField + "crt_date", sysDate);
        setValue(extendField + "crt_date", hBusiBusinessDate);
        setValue(extendField + "crt_time", sysTime);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_user", hApbtModUser);
        setValue(extendField + "mod_pgm", hApbtModPgm);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_detail duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActOtherApay1() throws Exception {
        daoTable = "act_other_apay";
        updateSQL = " status_code = '00',";
        updateSQL += " batch_no    = ?,";
        updateSQL += " mod_pgm     = 'ActH011'";
        whereStr = "where rowid    = ? ";
        setString(1, hApbtBatchNo);
        setRowId(2, hAoayRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_other_apay not found!", "", hCallBatchSeqno);
        }

    }

  /***********************************************************************/
  void checkFopen() throws Exception {

    temstr0 = String.format("/crdataupload/%s", iFileName);
    temstr0 = Normalizer.normalize(temstr0, java.text.Normalizer.Form.NFKD);
    int br = openInputText(temstr0, "MS950");
    if (br == -1) {
      exceptExit = 0;
      comc.errExit(String.format("本日[%s]無檔案[/crdataupload/%s]",hBusiBusinessDate,iFileName),
      hCallBatchSeqno);
    } else {
      closeInputText(br);
    }

    temstr1 = String.format("%s/media/act/%s", comc.getECSHOME(), iFileName);
    temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
    if (comc.fileCopy(temstr0, temstr1) == false) {
      comcr.errRtn(String.format("複製檔案錯誤[%s] to [%s]",temstr0,temstr1),
      "", hCallBatchSeqno);
    }

    showLogMessage("I", "", String.format("input file name:[%s]", temstr1));
    int br1 = openInputText(temstr1, "MS950");
    if (br1 == -1) {
        comcr.errRtn("檔案開啟錯誤：" + temstr1, "", hCallBatchSeqno);
    }
    closeInputText(br1);

    return;
  }

    /***********************************************************************/
    class buf1 {
        String pType;
        String pTxtype;
        String rTxid;
        String pSeq;
        String pPbank;
        String pPclno;
        String pRbank;
        String pRclno;
        String pAmt;
        String pRcode;
        String pSchd;
        String pCid;
        String pPid;
        String pSid;
        String pPdate;
        String pPseq;
        String pPschd;
        String pCno;
        String pNote;
        String pMemo;
        String pCfee;
        String pNoteb;
        String pFiller1;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixRight(pType, 1);
            rtn += fixRight(pTxtype, 2);
            rtn += fixRight(rTxid, 3);
            rtn += fixRight(pSeq, 8);
            rtn += fixRight(pPbank, 7);
            rtn += fixRight(pPclno, 16);
            rtn += fixRight(pRbank, 7);
            rtn += fixRight(pRclno, 16);
            rtn += fixRight(pAmt, 10);
            rtn += fixRight(pRcode, 2);
            rtn += fixRight(pSchd, 1);
            rtn += fixRight(pCid, 10);
            rtn += fixRight(pPid, 10);
            rtn += fixRight(pSid, 6);
            rtn += fixRight(pPdate, 8);
            rtn += fixRight(pPseq, 8);
            rtn += fixRight(pPschd, 1);
            rtn += fixRight(pCno, 20);
            rtn += fixRight(pNote, 40);
            rtn += fixRight(pMemo, 10);
            rtn += fixRight(pCfee, 5);
            rtn += fixRight(pNoteb, 20);
            rtn += fixRight(pFiller1, 39);
            return rtn;
        }

        String fixRight(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = spc + str;
            byte[] bytes = str.getBytes("MS950");
            int offset = bytes.length - len;
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, offset, vResult, 0, len);
            return new String(vResult, "MS950");
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        detailSt.pType = comc.subMS950String(bytes, 0, 1);
        detailSt.pTxtype = comc.subMS950String(bytes, 1, 2);
        detailSt.rTxid = comc.subMS950String(bytes, 3, 3);
        detailSt.pSeq = comc.subMS950String(bytes, 6, 8);
        detailSt.pPbank = comc.subMS950String(bytes, 14, 7);
        detailSt.pPclno = comc.subMS950String(bytes, 21, 16);
        detailSt.pRbank = comc.subMS950String(bytes, 37, 7);
        detailSt.pRclno = comc.subMS950String(bytes, 44, 16);
        detailSt.pAmt = comc.subMS950String(bytes, 60, 10);
        detailSt.pRcode = comc.subMS950String(bytes, 70, 2);
        detailSt.pSchd = comc.subMS950String(bytes, 72, 1);
        detailSt.pCid = comc.subMS950String(bytes, 73, 10);
        detailSt.pPid = comc.subMS950String(bytes, 83, 10);
        detailSt.pSid = comc.subMS950String(bytes, 93, 6);
        detailSt.pPdate = comc.subMS950String(bytes, 99, 8);
        detailSt.pPseq = comc.subMS950String(bytes, 107, 8);
        detailSt.pPschd = comc.subMS950String(bytes, 115, 1);
        detailSt.pCno = comc.subMS950String(bytes, 116, 20);
        detailSt.pNote = comc.subMS950String(bytes, 136, 40);
        detailSt.pMemo = comc.subMS950String(bytes, 176, 10);
        detailSt.pCfee = comc.subMS950String(bytes, 186, 5);
        detailSt.pNoteb = comc.subMS950String(bytes, 191, 20);
        detailSt.pFiller1 = comc.subMS950String(bytes, 211, 39);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActH011 proc = new ActH011();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
