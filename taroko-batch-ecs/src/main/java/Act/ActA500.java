/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/21  V1.00.01    SUP       error correction                          *
 *  110/05/20  V1.00.07    Simon     fix2 vulnerability Absolute Path Traversal
 *  111/10/12  V1.00.02    Machao      sync from mega & updated for project coding standard*
 *  112/08/08  V1.00.03    Ryan      配合TCB調整公會協商繳款檔(ACUT.TXT)的處理 *
 *  112/09/20  V1.00.04    sunny      檔案不存在，不跳ERROR                        *
 *  112/10/31  V1.00.05    Ryan      tranAmt 改為11取7 ，調整error訊息                          *
 ******************************************************************************/

package Act;

import java.io.UnsupportedEncodingException;

import java.util.ArrayList;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;

/*接收債務協商清分資料檔回灌入帳作業*/
public class ActA500 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private final String PROGNAME = "接收債務協商清分資料檔回灌入帳作業  112/10/31  V1.00.05";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommDate comDate = new CommDate();
    CommCrdRoutine comcr = null;

    String prgmId = "ActA500";
    String hModUser = "";
    String hCallBatchSeqno = "";
    String hModPgm = "";
    String hCallRProgramCode = "";
    String iFileName = "";

    String hTempUser = "";
    String hBusinssDate = "";
    String hSystemDate = "";
    String hSystemDatef = "";
    int cntRealIdno = 0;
    int inputId = 0;
    String hAcnoAcctKey = "";
    String hAcnoPSeqno = "";
    String hIdnoIdCode = "";
    String hIdnoChiName = "";
    int inputAcctKey = 0;
    int mediaCnt = 0;
    double totAmt = 0;
    int totCnt = 0;
    String hApbtBatchNo = "";
    String hApdlSerialNo = "";
    String hAoayPSeqno = "";
    String hAoayAcctType = "";
    String hAoayAcctKey = "";
    double hApdlPayAmt = 0;
    String hApdlPayDate = "";
    String hErrorRemark = "";
    String hAperErrorReason = "";
    String hAperErrorRemark = "";
    String hApbtModUser = "";
    String hTempBranch = "";
    String tId = "";
    String hApbtModPgm = "";
    String hIdnoIdPSeqno = "";
    String hAoayIdPSeqno = "";
    String hAoayId = "";
    String hAoayIdCode = "";
    long hTempBatchNoSeq = 0;
    String hTempBatchNo = "";
    int hApbtBatchTotCnt = 0;
    double hApbtBatchTotAmt = 0;
    long hApbtModSeqno = 0;
    String hBusiBusinessDate = "";
    String tIssueId = "";
    String tAcqId = "";
    String hAoayEnterAcctDate = "";
    String tAcctNo = "";
    double tmpAmt = 0;
    double txAmt = 0;
    String hAcnoAcctType = "";
    String tErrRsn = "";
    String hAoayStmtCycle = "";
    int tempInt = 0;
    String iAcctMonth = "";
    String hTtttAcctBank = "";
    String hAoayAutopayAcctNo = "";
    String hAoayStatusCode = "";
    String hAoayAutopayId = "";
    String tUninNo = "";
    String tTranAmtRem = "";
    String tTranAmt = "";
    String tTranDate = "";
    String tTranKind = "";

    int ret = 0;
    int nSerialNo = 0;
    String temstr0 = "";
    String ascStr = "";
    String tFunCd = "";
    String swCol = "";
    int rtn = 0;
    int currAccum = 0;
    int currentIdx = 0;
    int cntIdno = 0;
    int cntIdnoDebt = 0;
    double allEndBal = 0;
    int rightNo = 0;
    int errorNo = 0;
    long tempLong = 0;

    String hClbrRateFlag = "";
    String hClbrRateType = "";
    String hClbrLiabSeqno = "";
    String hAcctKey = "";
    double hClbrRepayRate = 0;
    double hEndBal = 0;
    int payErrorIndex = 0;

    ArrayList<String> aClbrRateFlag  = new ArrayList<String>();
    ArrayList<String> aClbrRateType  = new ArrayList<String>();
    ArrayList<String> aClbrLiabSeqno = new ArrayList<String>();
    ArrayList<String> aAcctKey        = new ArrayList<String>();
    ArrayList<Double> aClbrRepayRate = new ArrayList<Double>();
    ArrayList<Double> aEndBal         = new ArrayList<Double> ();
    ArrayList<String> fileDataList        = new ArrayList<String> ();

    buf1 newBuf = new buf1();

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            //if (args.length < 3 || args.length > 4) {
            if (args.length < 2) {
               // comc.errExit("Usage : ActA500 [txt_file] [acct_month] [Y/N] [callbatch_seqno]", "");
            	 comc.errExit("Usage : ActA500 [txt_file](必填) [Y/N](必填) [businday] [callbatch_seqno]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTempUser = comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            if (hTempUser.length() == 0) {
                hModUser = comc.commGetUserID();
                hTempUser = hModUser;
            }

            ret = 0;

          //if (args[0].matches("\\w+") == false) {
            if (args[0].length() == 0) {
                comc.errExit(String.format("ActA500 args[0] i_file_name[%s]不符合格式", args[0]), "");
            } else {
//                args[0] = sanitizeArg2(args[0]);
                iFileName = args[0];
            }
            
// TCB CANCEL
//            if (args[1].chars().allMatch(Character::isDigit) == false) {
//                comc.errExit(String.format("ActA500 args[1] i_acct_month[%s]不符合格式", args[1]), "");
//            } else {
////                args[1] = sanitizeArg2(args[1]);
//                iAcctMonth = args[1];
//            }

            showLogMessage("I", "", String.format("Process File=[%s]", iFileName));          
            
            hModUser = comc.commGetUserID();
            hApbtModUser = hModUser;
            hApbtModPgm = javaProgram;

            commonRtn(); //取得營業日日期
                       
            //處理強迫轉入參數
            if (args[1].length() > 0) {
            showLogMessage("I", "", String.format("強 迫 轉 入 =[%s]", args[1]));
            }  
            
            //以程式執行參數日期為主
            if (args.length > 2 && args[2].length() == 8) {
                hBusiBusinessDate = args[2];
            }
            
            showLogMessage("I", "", String.format("BusinessDate=[%s]", hBusiBusinessDate));       

            checkFopenR();

            readFile();
             
            if (args[1].equals("N"))/* 強迫註記 */          
            	chkBilMediactl();            

            if (hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 0);

            if (rightNo + errorNo > 0) {
                String fs = String.format("%s/media/act/%s", comc.getECSHOME(), iFileName);
              //fs = Normalizer.normalize(fs, java.text.Normalizer.Form.NFKD);
                String ft = String.format("%s/media/act/backup/%s_%s", comc.getECSHOME(), iFileName,sysDate+sysTime);
              //ft = Normalizer.normalize(ft, java.text.Normalizer.Form.NFKD);
                if (comc.fileRename(fs, ft) == false)
                    showLogMessage("I", "", String.format("無法搬移=[%s]", fs));
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
    void commonRtn() throws Exception {
        sqlCmd = "select business_date ";
        sqlCmd += "  from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusinssDate = getValue("business_date");
        }

        hBusiBusinessDate = hBusinssDate;

        sqlCmd = "select to_char(sysdate,'yyyymmdd')         h_system_date,";
        sqlCmd += " to_char(sysdate,'yyyymmddhh24miss') h_system_date_f ";
        sqlCmd += "  from dual ";
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dual not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
            hSystemDatef = getValue("h_system_date_f");
        }
    }

    /***********************************************************************/
    void checkFopenR() throws Exception {

        temstr0 = String.format("%s/media/act/%s", comc.getECSHOME(), iFileName);
      //temstr0 = Normalizer.normalize(temstr0, java.text.Normalizer.Form.NFKD);
        return;
    }
    
	void checkTotCntAmt(int br) throws Exception {
		String str600 = "";
		int totCnt = 0;
		long totAmt = 0;
		while (true) {
			str600 = readTextFile(br);
			if (endFile[br].equals("Y")) break;
			fileDataList.add(str600);
			if (str600.length() < 7)
				continue;
			newBuf.splitBuf1(str600);
			if (newBuf.funCd.equals("2")) {
				totCnt++;
				totAmt += comc.str2long(newBuf.tranAmt);
			}
			if (newBuf.funCd.equals("3")) {
				if (totCnt != comc.str2int(newBuf.dataCnt)) {
					exceptExit = 0;
					comcr.errRtn(String.format("「明細筆數%s檔尾筆數%s，檢核不一致，程式不予執行", totCnt, comc.str2long(newBuf.dataCnt)), "", "");
				}
				if (totAmt != comc.str2long(newBuf.totAmt)) {
					exceptExit = 0;
					comcr.errRtn(String.format("「明細金額%s檔尾總金額%s，檢核不一致，程式不予執行", totAmt, comc.str2long(newBuf.totAmt)), "", "");
				}
			}
		}
		closeInputText(br);
	}

    /***********************************************************************/
    void readFile() throws Exception {
        int i = 0;
//        String str600 = "";
        totCnt = 0;
        totAmt = 0;
        nSerialNo = 0;

        selectActPayBatch();
        hApbtBatchTotCnt = 0;
        hApbtBatchTotAmt = 0;

        int br = openInputText(temstr0, "MS950");
        if (br == -1) {
        	exceptExit = 0;
            comcr.errRtn("檔案不存在：" + temstr0, "", hCallBatchSeqno);
        }
        
        //檢核明細筆數,金額與檔尾總筆數與金額是否一致,不一致程式當掉
        checkTotCntAmt(br);

//        while (true) {
        for(String str600 : fileDataList) {
//            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;
            if (str600.length() < 7)
                continue;

            newBuf.splitBuf1(str600);
//            ascStr = newBuf.allText();

            moveRtn();
                   
            if (!tFunCd.equals("2")) {
                continue;
            }

            totCnt++;
            totAmt = totAmt + txAmt;

            if (totCnt % 100 == 0 || totCnt == 1) {
                showLogMessage("I", "", String.format("Process record=[%d]", totCnt));
            }

            /*
             * if(strcmp(h_aoay_status_code.arr , "99") == 0) {
             * sprintf(ErrMsg,"此批資料 狀態錯誤 (status='99') [%s]",t_id);
             * err_rtn(ErrMsg,"" ); }
             */

            aClbrRateFlag.clear();
            aClbrRateType.clear();
            aClbrLiabSeqno.clear();
            aAcctKey.clear();
            aClbrRepayRate.clear();
            aEndBal.clear();

            hClbrRateFlag = "";
            hClbrRateType = "";
            hClbrLiabSeqno = "";
            hAoayPSeqno = "";
            hAoayIdPSeqno = "";
            hAoayStmtCycle = "";
            hAoayAcctType = "";
            hAoayAcctKey = "";
            hAcnoAcctType = "";

            hIdnoIdPSeqno = "";
            currentIdx = 0;
//            rtn = chkColLiab(tId);

            currAccum = 0;
            swCol = "Y";
            hAcnoPSeqno = "";

            if (currentIdx == 0) { /* 沒有分配比率參數 */
                swCol = "N";
                rtn = chkCrdIdno(tId); /* get acct_key */
                if (rtn != 0 || cntIdno == 0) { /* 無 acno */
                    tmpAmt = txAmt;
                	tmpAmt = 0;
                    if (tErrRsn.length() == 0)
                        tErrRsn = "0";
                    hModPgm = "ActA500-1";
                    //補寫----Start
                    hAperErrorRemark = "AUT5 分配分行 ";
                    hErrorRemark = "AUT5";
                    setPayDateRtn(); 
                    //補寫----End
                      insertActA500r1();
                      insertActPayError();
                    //insertColLiabPayErr();
                    continue;
                }


                if (cntRealIdno > 1 && cntIdnoDebt == 0) { /* 重號(id_no_code兩個以上，且無欠款 */
                } else {
                    rtn = chkActDebt(hAcnoAcctKey); /* 依欠款比率分配 */
                    for (i = 0; i < currentIdx; i++) {
                        aAcctKey.add( hAcnoAcctKey);
                        aClbrRepayRate.add(aEndBal.get(i) / allEndBal);                       
                    }
                }
            }
            

            if (currentIdx == 0) { /* 無欠款者 */
                hAperErrorRemark = "AUT5 分配分行 ";
                hErrorRemark = "AUT5";
              //no_debt_rtn(t_id);              
                noDebtRtn(hIdnoIdPSeqno);
                continue;
            }
            for (i = 0; i < currentIdx; i++) { /* 有分配比率參數且有欠款者 */

                hAcnoAcctKey = aAcctKey.size() > i ? aAcctKey.get(i) : "";
                hTempBranch = "";
                hErrorRemark = "AUT4";
                hAperErrorRemark = "AUT4 分配分行 ";
                hClbrRateFlag = aClbrRateFlag.size() > i ? aClbrRateFlag.get(i) : "";
                hClbrRateType = aClbrRateType.size() > i ? aClbrRateType.get(i) : "";
                hClbrLiabSeqno = aClbrLiabSeqno.size() > i ? aClbrLiabSeqno.get(i) : "";

                if ((aClbrRateFlag.size() > i ? aClbrRateFlag.get(i) : "").equals("2")) { /* 1:acct_type 2:分行 */
                    hTempBranch = aClbrRateType.get(i);
                    if (aClbrRateType.get(0).length() > 2) {
                        hAoayPSeqno = "";
                        hAoayIdPSeqno = "";
                        hAoayStmtCycle = "";
                        hAoayAcctType = "";
                        hAoayAcctKey = "";
                        hAcnoAcctType = "";
                    } else {
                        /* 第一個type */
                        hAcnoAcctType = aClbrRateType.get(0);
                        selectActAcno();
                    }
                    tempLong = (long) (txAmt * aClbrRepayRate.get(i) + 0.5);
                    if ((i == currentIdx - 1) && currentIdx != 1)
                        tempLong = (long) (txAmt - currAccum);
                    currAccum = (int) (currAccum + tempLong);
                    hApdlPayAmt = tempLong;
                    hAperErrorReason = "302";
                    selectActAcno();
                    selectCrdIdno();
                    setPayDateRtn();
                    insertActPayError();

                    /*
                     * insert_col_liab_pay_err();
                     */
                    continue;
                }
                hAcnoAcctType = aClbrRateType.get(i);
                tempLong = (long) (txAmt * aClbrRepayRate.get(i) + 0.5);
                if ((i == currentIdx - 1) && currentIdx != 1)
                    tempLong = (long) (txAmt - currAccum);
                currAccum = (int) (currAccum + tempLong);

                selectActAcno();
                selectCrdIdno();
                setPayDateRtn();

                if (swCol.substring(0, 1).equals("N")) {
                    hAperErrorRemark = "AUT5 分配分行 ";
                    hErrorRemark = "AUT5";
                    tErrRsn = "1";
                    tmpAmt = tempLong;
                    hModPgm = "ActA500-2";
                    insertActA500r1();
//                    insertActPayError();
//                    hApbtBatchTotCnt++;
                    //insertColLiabPayErr();
                }
                hApdlPayAmt = tempLong;
                hModPgm = "ActA500-1";
                insertActPayDetail();

                hApbtBatchTotCnt++;
                hApbtBatchTotAmt = hApbtBatchTotAmt + hApdlPayAmt;
            }
        }
//        closeInputText(br);
        showLogMessage("I", "", String.format("處理入帳日=[%s]", hAoayEnterAcctDate));
        
        if (hApbtBatchTotCnt != 0)
            insertActPayBatch();
   
        if(totCnt != payErrorIndex)
        	saveActPayError();
    }

    /***********************************************************************/
    void selectActPayBatch() throws Exception {
        String tempstr = "";

        tempstr = String.format("%s1005%3.3s%c", hBusiBusinessDate, "000", '%');
        hTempBatchNo = tempstr;

        sqlCmd = "select to_number(substr(max(batch_no),16,1))+1 h_temp_batch_no_seq ";
        sqlCmd += "  from act_pay_batch  ";
        sqlCmd += " where batch_no like ? ";
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
    void moveRtn() throws Exception {

        String tmpStr = "";
        hTtttAcctBank = "";
        hAoayAutopayAcctNo = "";
        hApdlPayAmt = 0;
        hAoayStatusCode = "";
        hAoayAutopayId = "";
        hAoayPSeqno = "";
//        hAoayEnterAcctDate = "";

        tIssueId = "";
        tAcqId = "";
//        hAoayEnterAcctDate = "";
        tId = "";
        hIdnoChiName = "";
        tAcctNo = "";
        hAcnoAcctType = "";
        hAcnoAcctKey = "";
        tErrRsn = "";

        tFunCd = newBuf.funCd;
//        tIssueId = newBuf.issueId;

//        tmpStr = newBuf.acqId;
//        tAcqId = tmpStr;
//        hTtttAcctBank = tmpStr;

//        tTranKind = newBuf.tranKind;
        if("1".equals(tFunCd)) {
        	tTranDate = newBuf.tranDate;
        	if(tTranDate.length() == 7) {
            	tTranDate = comDate.tw2adDate(tTranDate);
            }
        }
        	
//        tTranDate = String.format("%8.0f", comcr.str2double(tmpStr) + 19110000);

        hAoayEnterAcctDate = tTranDate;
       
        if(DEBUG_MODE)
        showLogMessage("I", "", String.format(" EnterAcctDate[%s]", hAoayEnterAcctDate ));
        
//        tAcctNo = newBuf.acctNo.substring(2);
//        hAoayAutopayAcctNo = tAcctNo;
        tTranAmt = newBuf.tranAmt;
//        tTranAmtRem = newBuf.tranAmtRem;
//        txAmt = comcr.str2long(tTranAmt) + comcr.str2long(tTranAmtRem) / 100;
        txAmt = comcr.str2long(tTranAmt);
        hApdlPayAmt = txAmt;
//        tUninNo = newBuf.uninNo;
        /*
         * erase_space(tmp_str, new_buf.status_code ,sizeof(new_buf.status_code
         * )); strcpy(t_status_code , tmp_str); str2var(h_aoay_status_code
         * ,tmp_str );
         */
        hAoayStatusCode = "00";

        tmpStr = newBuf.id;
        tId = tmpStr;
        hAoayAutopayId = tmpStr;

    }

    /***********************************************************************/
    int chkColLiab(String inputId) throws Exception {

        sqlCmd = "select id_p_seqno ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_no = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, inputId);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hIdnoIdPSeqno = getValue("id_p_seqno");
        }

        /* 以下先讀 2碼 (acct_type, a.rate_flag='1') 再讀 3碼 (branch, a.rate_flag='2') */

        aClbrRateFlag  .clear();
        aClbrRateType  .clear();
        aClbrLiabSeqno .clear();
        aAcctKey        .clear();
        aClbrRepayRate .clear();
        
        sqlCmd = "select rate_flag,";
        sqlCmd += " rate_type,";
        sqlCmd += " liab_seqno,";
        sqlCmd += " b.acct_key,";
      //sqlCmd += " repay_rate/100 ";
        sqlCmd += " repay_rate/100 as real_repay_rate";
        sqlCmd += "  from col_liab_rate a ";
        sqlCmd += "  left outer join act_acno b";
        sqlCmd += "    on a.p_seqno    = b.acno_p_seqno ";
      //sqlCmd += " where a.id_p_seqno = ?  "; // a.id
        sqlCmd += " where a.id_no = ?  "; // a.id
        sqlCmd += "   and a.apr_flag   = 'Y' ";
        sqlCmd += " order by rate_flag ,rate_type ";
        setString(1, inputId);
      //setString(1, h_idno_id_p_seqno);
        recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            aClbrRateFlag.add(getValue("rate_flag", i));
            aClbrRateType.add(getValue("rate_type", i));
            aClbrLiabSeqno.add(getValue("liab_seqno", i));
            aAcctKey.add(getValue("acct_key", i));
          //a_clbr_repay_rate.add(getValueDouble("repay_rate", i));
            aClbrRepayRate.add(getValueDouble("real_repay_rate", i));
        }

        currentIdx = recordCnt;

        return (0);
    }

    /***********************************************************************/
    int chkCrdIdno(String inputId) throws Exception {
        String tmpAcctKey = "";
        String tmpChiName = "";
        extendField = "chkidno.";
        sqlCmd = "select id_p_seqno ";
        sqlCmd += "  from crd_idno  ";
        sqlCmd += " where id_no = ? ";
        setString(1, inputId);
        int recordCnt = selectTable();
        cntRealIdno = recordCnt;
        if (recordCnt > 0) {
            hIdnoIdPSeqno = getValue("chkidno.id_p_seqno");
        }

        if (cntRealIdno == 0) {
            tErrRsn = "2"; /* ECS id_no 不存在 */
            return (1);
        }

        cntIdno = 0;
        cntIdnoDebt = 0;
        tmpAcctKey = "";
        tmpChiName = "";

        fetchExtend = "chkacno.";
        sqlCmd = "select b.acct_key,";
        sqlCmd += " b.p_seqno,";
        sqlCmd += " a.id_no_code,"; // id_code
        sqlCmd += " a.chi_name ";
        sqlCmd += "  from crd_idno a,act_acno b ";
        sqlCmd += " where a.id_p_seqno  = b.id_p_seqno ";
        sqlCmd += "   and decode(b.corp_act_flag,'','N',b.corp_act_flag) <> 'Y' ";
        sqlCmd += "   and b.corp_p_seqno     = '' ";// corp_no
        sqlCmd += "   and a.id_no = ? ";
        sqlCmd += " group by ";
        sqlCmd += " b.acct_key, ";
        sqlCmd += " a.id_no_code, ";
        sqlCmd += " b.p_seqno, ";
        sqlCmd += " a.chi_name ";
        setString(1, inputId);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAcnoAcctKey = getValue("chkacno.acct_key");
            hAcnoPSeqno = getValue("chkacno.p_seqno");
            hIdnoIdCode = getValue("chkacno.id_no_code");
            hIdnoChiName = getValue("chkacno.chi_name");

            cntIdno++;

            rtn = chkActDebt(hAcnoAcctKey);

            if (cntIdno == 1) {
                tmpAcctKey = hAcnoAcctKey;
                tmpChiName = hIdnoChiName;
            }
            if (allEndBal > 0) {
                tmpAcctKey = hAcnoAcctKey;
                tmpChiName = hIdnoChiName;
                cntIdnoDebt++;
            }
        }
        closeCursor(cursorIndex);

        /* ECS 不存在 ID重號且同時均欠款 */

        if (cntRealIdno > 1 && cntIdnoDebt > 1) {
            tErrRsn = "3"; /* ID重號且同時均欠款 */
            return (1);
        }

        /*
         * if(cnt_real_idno == 1 && cnt_idno_debt > 1) 此種 狀況 只 有一個 acct_key
         * (acct_type 不同)
         */

        /* 只有一 acct_key 欠款 或 只有一 cnt_idno == 1 */
        hAcnoAcctKey = tmpAcctKey;
        hIdnoChiName = tmpChiName;

        return (0);
    }

    /***********************************************************************/
    int chkActDebt(String inputAcctKey) throws Exception {
        aClbrRateType.clear();
        aEndBal.clear();
        extendField = "chkdebt.";
        sqlCmd = "select a.acct_type,";
        sqlCmd += " sum(a.end_bal) h_end_bal ";
        sqlCmd += "  from act_debt a, act_acno b ";
        sqlCmd += " where a.end_bal > 0 ";
        sqlCmd += "   and b.acno_p_seqno = a.p_seqno ";
        sqlCmd += "   and b.acct_key = ? ";// acct_key
        sqlCmd += "   and a.acct_type='01'"; //TCB只有01
        sqlCmd += "group by a.acct_type ";
        setString(1, inputAcctKey);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            aClbrRateType.add(getValue("chkdebt.acct_type", i));
            aEndBal.add(getValueDouble("chkdebt.h_end_bal", i));
        }

        currentIdx = recordCnt;

        allEndBal = 0;
        for (int i = 0; i < currentIdx; i++) {
            allEndBal =  (allEndBal + aEndBal.get(i));
        }

        return (0);
    }

    /***********************************************************************/
  //void no_debt_rtn(String input_id) throws Exception {
    void noDebtRtn(String inputIdPSeqno) throws Exception {
        /* 無欠款者，挑選任一有效卡的ACCT_TYPE(除商務卡總繳)入帳 */
        /* 無欠款者，且無有效卡者，挑選任一ACCT_TYPE(除商務卡總繳)入帳 */

        tempInt = 0;
        sqlCmd = "select count(*) temp_int ";
        sqlCmd += "  from crd_card a, act_acno b  ";
      //sqlCmd += " where b.p_seqno = a.p_seqno and b.acct_key = ?  ";// acct_key
        sqlCmd += " where b.acno_p_seqno = a.acno_p_seqno and a.major_id_p_seqno = ?  ";
        sqlCmd += "   and b.acct_type='01' "; //TCB只允許01一般卡入帳
        sqlCmd += "   and a.current_code = '0' ";
      //setString(1, input_id);
        setString(1, inputIdPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_card not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }

        if (tempInt > 0) {
            sqlCmd = "select min(a.acct_type) h_acno_acct_type ";
            sqlCmd += "  from crd_card a, act_acno b  ";
          //sqlCmd += " where b.p_seqno = a.p_seqno and b.acct_key = ?  "; // acct_key
            sqlCmd += " where b.acno_p_seqno = a.acno_p_seqno and a.major_id_p_seqno = ?  ";
          //sqlCmd += "   and (    a.acct_type  != '02' " + "or (    a.acct_type = '02'  " + "and a.p_seqno   = a.GP_NO))  "; // acct_p_seqno
//            sqlCmd += "   and (    a.acct_type  != '02' " + "or (    a.acct_type = '02'  " + "and a.acno_p_seqno   = a.p_seqno))  "; // acct_p_seqno
            sqlCmd += "   and b.acct_type = '01' "; //TCB只允許01一般卡入帳
            sqlCmd += "   and a.current_code = '0' ";
          //setString(1, input_id);
            setString(1, inputIdPSeqno);
            int recordCnt2 = selectTable();
            if (recordCnt2 > 0) {
                hAcnoAcctType = getValue("h_acno_acct_type");
            }
        } 
//TCB取消本段判斷，固定取01
//        else {
//            sqlCmd = "select min(a.acct_type) h_acno_acct_type ";
//            sqlCmd += "  from crd_card a, act_acno b  ";
//          //sqlCmd += " where b.p_seqno = a.p_seqno and b.acct_key = ?  ";// acct_key
//            sqlCmd += " where b.acno_p_seqno = a.acno_p_seqno and a.major_id_p_seqno = ?  ";// acct_key
//          //sqlCmd += "   and (   a.acct_type != '02' " + "or (    a.acct_type = '02' " + "and a.p_seqno   = a.GP_NO)) ";// acct_p_seqno
//            sqlCmd += "   and (   a.acct_type != '02' " + "or (    a.acct_type = '02' " + "and a.acno_p_seqno   = a.p_seqno)) ";// acct_p_seqno
//          //setString(1, h_acno_acct_key);
//            setString(1, inputIdPSeqno);
//            int recordCnt2 = selectTable();
//            if (recordCnt2 > 0) {
//                hAcnoAcctType = getValue("h_acno_acct_type");
//            }
//        }
        
        hApdlPayAmt = txAmt;
        /*
         * str2var(h_error_remark , "AUT4"); str2var(h_aper_error_remark ,
         * "AUT4 分配分行 ");
         */
        selectActAcno();
        selectCrdIdno();
        setPayDateRtn();
        hModPgm = "ActA500-2";
        insertActPayDetail();
        hApbtBatchTotCnt++;
        hApbtBatchTotAmt = hApbtBatchTotAmt + hApdlPayAmt;

        tErrRsn = "1";
        tmpAmt = txAmt;
        hModPgm = "ActA500-3";
        hClbrRateType = hAcnoAcctType;
        insertActA500r1();
//        insertActPayError();
//        hApbtBatchTotCnt++;
        //insertColLiabPayErr();
    }

    /***********************************************************************/
    void insertActPayError() throws Exception {
        String tmpstr = "";

        nSerialNo++;
        tmpstr = String.format("%05d", nSerialNo);
        hApdlSerialNo = tmpstr;
      //n_serial_no++;
        errorNo++;
     
        
        daoTable = "act_pay_error";
        extendField = daoTable + ".";
        setValue(extendField+"batch_no", hApbtBatchNo,payErrorIndex);
        setValue(extendField+"serial_no", hApdlSerialNo,payErrorIndex);
        setValue(extendField+"p_seqno", hAoayPSeqno,payErrorIndex);
        setValue(extendField+"acno_p_seqno", hAoayPSeqno,payErrorIndex);
        setValue(extendField+"acct_type", hAoayAcctType,payErrorIndex);
        setValue(extendField+"acct_key" , hAoayAcctKey,payErrorIndex);
        setValueDouble(extendField+"pay_amt", hApdlPayAmt,payErrorIndex);
        setValue(extendField+"pay_date", hApdlPayDate,payErrorIndex);
        setValue(extendField+"payment_type", hErrorRemark,payErrorIndex);
        setValue(extendField+"error_reason", hAperErrorReason,payErrorIndex);
        setValue(extendField+"error_remark", hAperErrorRemark,payErrorIndex);
        setValue(extendField+"crt_user", hApbtModUser,payErrorIndex);
        setValue(extendField+"crt_date", sysDate,payErrorIndex);
        setValue(extendField+"crt_time", sysTime,payErrorIndex);
        setValue(extendField+"branch", hTempBranch,payErrorIndex);
        setValue(extendField+"id_no", tId,payErrorIndex); // id
        setValue(extendField+"mod_user", hApbtModUser,payErrorIndex);
        setValue(extendField+"mod_time", sysDate + sysTime,payErrorIndex);
        //setValue(extendField+"mod_pgm", hApbtModPgm); //sunny test
        setValue(extendField+"mod_pgm", hModPgm,payErrorIndex);
//        insertTable();
//        if (dupRecord.equals("Y")) {
//            comcr.errRtn("insert_act_pay_error duplicate!", "", hCallBatchSeqno);
//        }
        payErrorIndex ++;
        if(DEBUG_MODE)
        showLogMessage("I", "", String.format(" insert act_pay_error ! [%s],[%s],[%s],[%s]", hAoayAcctKey, hApdlPayDate,hErrorRemark,tErrRsn ));

    }
    
    void saveActPayError() throws Exception {
    	showLogMessage("I", "", String.format("資料正在寫入act_pay_error,筆數[%d]", payErrorIndex));
        for(int index =0 ;index < payErrorIndex ;index++) {
            daoTable = "act_pay_error";
            extendField = "error.";
            setValue("error.batch_no", getValue("act_pay_error.batch_no",index));
            setValue("error.serial_no", getValue("act_pay_error.serial_no",index));
            setValue("error.p_seqno", getValue("act_pay_error.p_seqno",index));
            setValue("error.acno_p_seqno",getValue("act_pay_error.acno_p_seqno",index));
            setValue("error.acct_type", getValue("act_pay_error.acct_type",index));
            setValue("error.acct_key" , getValue("act_pay_error.acct_key",index));
            setValueDouble("error.pay_amt", getValueDouble("act_pay_error.pay_amt",index));
            setValue("error.pay_date", getValue("act_pay_error.pay_date",index));
            setValue("error.payment_type", getValue("act_pay_error.payment_type",index));
            setValue("error.error_reason", getValue("act_pay_error.error_reason",index));
            setValue("error.error_remark", getValue("act_pay_error.error_remark",index));
            setValue("error.crt_user", getValue("act_pay_error.crt_user",index));
            setValue("error.crt_date", getValue("act_pay_error.crt_date",index));
            setValue("error.crt_time", getValue("act_pay_error.crt_time",index));
            setValue("error.branch", getValue("act_pay_error.branch",index));
            setValue("error.id_no", getValue("act_pay_error.id_no",index)); // id
            setValue("error.mod_user", getValue("act_pay_error.mod_user",index));
            setValue("error.mod_time", getValue("act_pay_error.mod_time",index));
            setValue("error.mod_pgm", getValue("act_pay_error.mod_pgm",index));
            insertTable();
            if (dupRecord.equals("Y")) {
                comcr.errRtn("insert_act_pay_error duplicate!", "", hCallBatchSeqno);
            }	
        }

    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        hAoayPSeqno = "";
        hAoayIdPSeqno = "";
        hAoayStmtCycle = "";
        if (hAcnoAcctType.length() == 0 || hAcnoAcctKey.length() == 0) {
        	extendField = "accttype.";
            sqlCmd = "select b.acno_p_seqno,";
            sqlCmd += " b.id_p_seqno,";
            sqlCmd += " b.acct_type,";
            sqlCmd += " b.acct_key,";
            sqlCmd += " b.stmt_cycle ";
            sqlCmd += "  from ptr_acct_type a , act_acno b  ";
            sqlCmd += " where b.id_p_seqno = ?  "; 
            sqlCmd += "   and a.acct_type      = b.acct_type  ";
            sqlCmd += "   and b.card_indicator = '1'  ";
            sqlCmd += " fetch first 1 rows only ";
          //setString(1, t_id);
            setString(1, hIdnoIdPSeqno);
            int recordCnt = selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("select_act_acno not found!", "", hCallBatchSeqno);
            }
            if (recordCnt > 0) {
                hAoayPSeqno = getValue("accttype.acno_p_seqno");
                hAcnoPSeqno = getValue("accttype.acno_p_seqno");
                hAoayIdPSeqno = getValue("accttype.id_p_seqno");
                hAcnoAcctType = getValue("accttype.acct_type");
                hAcnoAcctKey = getValue("accttype.acct_key");
                hAoayStmtCycle = getValue("accttype.stmt_cycle");
            }
        } else {
         	extendField = "actacno.";
            sqlCmd = "select acno_p_seqno,";
            sqlCmd += " id_p_seqno,";
            sqlCmd += " stmt_cycle ";
            sqlCmd += "  from act_acno  ";
            sqlCmd += " where acct_type = ? ";
            sqlCmd += "   and acct_key  = ? ";
            setString(1, hAcnoAcctType);
            setString(2, hAcnoAcctKey);
            int recordCnt = selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("select_act_acno not found!", "", hCallBatchSeqno);
            }
            if (recordCnt > 0) {
                hAoayPSeqno = getValue("actacno.acno_p_seqno");
                hAcnoPSeqno = getValue("actacno.acno_p_seqno");
                hAoayIdPSeqno = getValue("actacno.id_p_seqno");
                hAoayStmtCycle = getValue("actacno.stmt_cycle");
            }
        }
        hAoayAcctType = hAcnoAcctType;
        hAoayAcctKey = hAcnoAcctKey;
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {

        hAoayIdPSeqno = "";
        hAoayId = "";
        hAoayIdCode = "";
        hIdnoChiName = "";
    	extendField = "crdidno.";
        sqlCmd = "select a.id_p_seqno,";
        sqlCmd += " a.chi_name,";
        sqlCmd += " a.id_no,";
        sqlCmd += " a.id_no_code ";
        sqlCmd += "  from crd_idno a, act_acno b  ";
        sqlCmd += " where b.acct_type  = ?  ";
        sqlCmd += "   and b.acct_key   = ?  ";
        sqlCmd += "   and a.id_p_seqno = b.id_p_seqno ";
        setString(1, hAcnoAcctType);
        setString(2, hAcnoAcctKey);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAoayIdPSeqno = getValue("crdidno.id_p_seqno");
            hIdnoChiName = getValue("crdidno.chi_name");
            hAoayId = getValue("crdidno.id_no");
            hAoayIdCode = getValue("crdidno.id_no_code");
        }
    }

    /***********************************************************************/
    void setPayDateRtn() throws Exception {

        /*
         * str2var(h_wday_this_close_date , ECS_NULLSTR);
         * str2var(h_wday_this_acct_month , ECS_NULLSTR); EXEC SQL SELECT
         * this_lastpay_date, this_acct_month INTO :h_wday_this_lastpay_date:di,
         * :h_wday_this_acct_month:di FROM ptr_workday WHERE stmt_cycle =
         * :h_aoay_stmt_cycle;
         * 
         * if(sqlca.sqlcode!=0) {
         * sprintf(ErrMsg,"select ptr_workday    error ");
         * err_rtn(ErrMsg,h_aoay_stmt_cycle.arr); }
         */

        /* str2var(h_apdl_pay_date, h_wday_this_lastpay_date.arr); */

        hApdlPayDate = hAoayEnterAcctDate;
        if(DEBUG_MODE)
        showLogMessage("I", "", String.format(" hApdlPayDate[%s]", hApdlPayDate ));

    }

    /***********************************************************************/
    void insertActA500r1() throws Exception {

        String a500r1IdNo = "";
      //if (h_acno_acct_key.length() == 0) {
      //    h_acno_acct_key = t_id;
      //}
        if (tErrRsn.equals("2")) { /* ECS id_no 不存在 */
            hIdnoChiName = "";
            a500r1IdNo = tId;
            tmpAmt=0;
            showLogMessage("I", "", String.format(" insert act_a500r1 error(ID不存在) ! [%s],[%s],[%s]", tErrRsn, tId, hIdnoChiName));
        }
        else {
        	  a500r1IdNo = hIdnoIdPSeqno;
        }
 
        daoTable = "act_a500r1";
        extendField = daoTable + ".";
        setValue(extendField+"tx_date", hBusiBusinessDate);
        setValue(extendField+"issue_id", tIssueId);
        setValue(extendField+"acq_id", tAcqId);
        setValue(extendField+"enter_acct_date", hAoayEnterAcctDate);
      //setValue(extendField+"id_no", t_id); // id
        setValue(extendField+"id_p_seqno", a500r1IdNo); // id
        setValue(extendField+"chi_name", hIdnoChiName);
        setValue(extendField+"acct_no", tAcctNo);
        setValueDouble(extendField+"tx_amt", tmpAmt); //入帳金額
        setValueDouble(extendField+"tot_amt", txAmt); //原始金額
        setValue(extendField+"acct_type", hAcnoAcctType);
      //setValue(extendField+"acct_key", h_acno_acct_key);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"err_rsn", tErrRsn);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hModPgm);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_a500r1 duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertColLiabPayErr() throws Exception {
        daoTable = "col_liab_pay_err";
        extendField = daoTable + ".";
        setValue(extendField+"id_no", tId); // id
        setValue(extendField+"rate_flag", hClbrRateFlag);
        setValue(extendField+"rate_type", hClbrRateType);
        setValue(extendField+"liab_seqno", hClbrLiabSeqno);
        setValue(extendField+"p_seqno", hAoayPSeqno);
        setValue(extendField+"id_p_seqno", hAoayIdPSeqno);
        setValue(extendField+"stmt_cycle", hAoayStmtCycle);
        setValueDouble(extendField+"pay_amt", tmpAmt);
        setValue(extendField+"crt_date", hBusiBusinessDate);
        setValue(extendField+"acct_month", iAcctMonth);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hModPgm);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liab_pay_err duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActPayDetail() throws Exception {

        nSerialNo++;
        hApdlSerialNo = String.format("%05d", nSerialNo);
      //n_serial_no++;
        rightNo++;

        daoTable = "act_pay_detail";
        extendField = daoTable + ".";
        setValue(extendField+"batch_no", hApbtBatchNo);
        setValue(extendField+"serial_no", hApdlSerialNo);
        setValue(extendField+"p_seqno", hAoayPSeqno);
        setValue(extendField+"acno_p_seqno", hAoayPSeqno);
        setValue(extendField+"acct_type", hAoayAcctType);
        // setValue ("acct_key" , h_aoay_acct_key);
        setValue(extendField+"id_p_seqno", hAoayIdPSeqno);
        // setValue ("id" , h_aoay_id);
        // setValue ("id_code" , h_aoay_id_code);
        setValueDouble(extendField+"pay_amt", hApdlPayAmt);
        setValue(extendField+"pay_date", hApdlPayDate);
        setValue(extendField+"payment_type", hErrorRemark);
        setValue(extendField+"crt_user", hApbtModUser);// update_user
        setValue(extendField+"crt_date", sysDate); // update_date
        setValue(extendField+"crt_time", sysTime); // update_time
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_user", hApbtModUser);
        setValue(extendField+"mod_pgm", hModPgm);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_detail duplicate!", "", hCallBatchSeqno);
        }
  
//TCB取消      
//        if (hClbrLiabSeqno.length() > 0)
//            insertColLiabPay();
//        else {
//            /*
//             * if(DEBUG) insert_col_liab_pay();
//             */
//        }
       
    }

    /***********************************************************************/
    void insertColLiabPay() throws Exception {

        daoTable = "col_liab_pay";
        extendField = daoTable + ".";
        setValue(extendField+"id_no", tId); // id
        setValue(extendField+"rate_flag", hClbrRateFlag);
        setValue(extendField+"rate_type", hClbrRateType);
       // setValue(extendField+"liab_seqno", hClbrLiabSeqno);
        setValue(extendField+"p_seqno", hAoayPSeqno);
        setValue(extendField+"id_p_seqno", hAoayIdPSeqno);
        setValue(extendField+"stmt_cycle", hAoayStmtCycle);
        /* :tmp_amt, */
        setValueDouble(extendField+"pay_amt", hApdlPayAmt);
        setValue(extendField+"crt_date", hBusiBusinessDate);
        setValue(extendField+"acct_month", iAcctMonth);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", "ActA500");
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liab_pay duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertActPayBatch() throws Exception {

        daoTable = "act_pay_batch";
        extendField = daoTable + ".";
        setValue(extendField+"batch_no", hApbtBatchNo);
        setValueInt(extendField+"batch_tot_cnt", hApbtBatchTotCnt);
        setValueDouble(extendField+"batch_tot_amt", hApbtBatchTotAmt);
        setValue(extendField+"crt_user", hApbtModUser);
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"trial_user", hApbtModUser);
        setValue(extendField+"trial_date", sysDate);
        setValue(extendField+"trial_time", sysTime);
        setValue(extendField+"confirm_user", hApbtModUser);
        setValue(extendField+"confirm_date", sysDate);
        setValue(extendField+"confirm_time", sysTime);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_user", hApbtModUser);
        setValue(extendField+"mod_pgm", hApbtModPgm);
        insertTable();
        if (!dupRecord.equals("Y")) {
            return;
        }

        hApbtModSeqno = comcr.getModSeq();

        daoTable = "act_pay_batch";
        updateSQL = "batch_tot_cnt  = batch_tot_cnt + ?,";
        updateSQL += " batch_tot_amt = batch_tot_amt + ?,";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " mod_user      = ?,";
        updateSQL += " mod_pgm       = ?,";
        updateSQL += " mod_seqno     = ?";
        whereStr = "where batch_no = ? ";
        setInt(1, hApbtBatchTotCnt);
        setDouble(2, hApbtBatchTotAmt);
        setString(3, hApbtModUser);
        setString(4, hApbtModPgm);
        setLong(5, hApbtModSeqno);
        setString(6, hApbtBatchNo);
        updateTable();

    }

    /***********************************************************************/
    void chkBilMediactl() throws Exception {
        int mediaCnt = 0;

        sqlCmd = "select count(*) media_cnt ";
        sqlCmd += "  from bil_mediactl  ";
        sqlCmd += " where bill_type    = 'ACT'  ";
        sqlCmd += "   and prog_name    = 'ActA500'  ";
        sqlCmd += "   and total_amount = ? ";
        sqlCmd += "   and total_record = ? ";
       // sqlCmd += "   and tx_date = ? ";
        setDouble(1, totAmt);
        setInt(2, totCnt);
//        setString(3, hBusiBusinessDate); //因TCB上傳檔名固定相同，增加判斷日期。
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            mediaCnt = getValueInt("media_cnt");
        }

        if (mediaCnt < 1) {
            insertBilMediactl();
        } else {
            exceptExit = 0;
            comcr.errRtn(String.format("錯誤 : 此批資料已經執行處理過 cnt=[%d] amt=[%s]", totCnt,totAmt), "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertBilMediactl() throws Exception {
        daoTable = "bil_mediactl";
        extendField = daoTable + ".";
        setValue(extendField+"bill_type", "ACT");
        setValueDouble(extendField+"total_amount", totAmt);
        setValueInt(extendField+"total_record", totCnt);
        setValue(extendField+"prog_name", "ActA500");
        setValue(extendField+"media_name", iFileName);
        setValueInt(extendField+"media_yyyy", comc.str2int(hSystemDate.substring(0, 4)));
        setValue(extendField+"tx_date", hBusinssDate);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", "ActA500");
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_mediactl duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActA500 proc = new ActA500();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class buf1 {
        String funCd = "";
//        String issueId;
//        String acqId;
        String tranDate = "";
//        String tranKind;
//        String seqNo;
        String tranAmt = ""; /* 50 */
        String totAmt = "";
//        String tranAmtRem;
//        String uninNo;
//        String bankNo;
//        String acctNo;
        String id = "";
        String dataCnt = "";
//        String keyNo; /* 109 */
//        String filler1;
//        String filler2; /* 150 */
//        String endFlag;

//        String allText() throws UnsupportedEncodingException {
//            String rtn = "";
//            rtn += fixLeft(funCd, 1);
//            rtn += fixLeft(issueId, 8);
//            rtn += fixLeft(acqId, 8);
////            rtn += fixLeft(tranDate, 7);
//            rtn += fixLeft(tranKind, 5);
//            rtn += fixLeft(seqNo, 10);
//            rtn += fixLeft(tranAmt, 11);
////            rtn += fixLeft(tranAmtRem, 2);
//            rtn += fixLeft(uninNo, 8);
//            rtn += fixLeft(bankNo, 7);
//            rtn += fixLeft(acctNo, 16);
//            rtn += fixLeft(id, 10);
//            rtn += fixLeft(keyNo, 16);
//            rtn += fixLeft(filler1, 20);
//            rtn += fixLeft(filler2, 21);
//            rtn += fixLeft(endFlag, 1);
//            return rtn;
//        }

        void splitBuf1(String str) throws UnsupportedEncodingException {
            byte[] bytes = str.getBytes("MS950");
			funCd = comc.subMS950String(bytes, 0, 1);
			if ("1".equals(funCd)) {
				tranDate = comc.subMS950String(bytes, 1, 7);
				
				if(DEBUG_MODE)  
				showLogMessage("I", "", String.format(" tranDate[%s]", tranDate ));
			}
			if ("2".equals(funCd)) {
				id = comc.subMS950String(bytes, 1, 10);
				tranAmt = comc.subMS950String(bytes, 11, 7);
			}
			if ("3".equals(funCd)) {
				dataCnt = comc.subMS950String(bytes, 1, 8);
				totAmt = comc.subMS950String(bytes, 9, 10);
			}
         
//            issueId = comc.subMS950String(bytes, 1, 8);
//            acqId = comc.subMS950String(bytes, 9, 8);
//            tranDate = comc.subMS950String(bytes, 17, 7);
//            tranKind = comc.subMS950String(bytes, 24, 5);
//            seqNo = comc.subMS950String(bytes, 29, 10);
//            tranAmt = comc.subMS950String(bytes, 39, 11);
//            tranAmtRem = comc.subMS950String(bytes, 50, 2);
//            uninNo = comc.subMS950String(bytes, 52, 8);
//            bankNo = comc.subMS950String(bytes, 60, 7);
//            acctNo = comc.subMS950String(bytes, 67, 16);
//            id = comc.subMS950String(bytes, 83, 10);
//            keyNo = comc.subMS950String(bytes, 93, 16);
//            filler1 = comc.subMS950String(bytes, 109, 20);
//            filler2 = comc.subMS950String(bytes, 129, 21);
//            endFlag = comc.subMS950String(bytes, 150, 1);
        }

    }

    String fixLeft(String str, int len) throws UnsupportedEncodingException {
        int size = (Math.floorDiv(len, 100) + 1) * 100;
        String spc = "";
        for (int i = 0; i < size; i++)
            spc += " ";
        if (str == null)
            str = "";
        str = str + spc;
        byte[] bytes = str.getBytes("MS950");
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, 0, vResult, 0, len);

        return new String(vResult, "MS950");
    }

}
