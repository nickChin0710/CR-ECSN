/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 106-06-23  V1.00.01   David FU      Initial                              *
* 109/11/24  V1.00.01   shiyuqi       updated for project coding standard  *
* 111/09/22  V1.00.02   JeffKung      updated for TCB.                     *
* 112/03/08  V1.00.04    yingdong  Erroneous String Compare Issue            *
***************************************************************************/
package Bil;

import com.AccessDAO;
import com.CommCrd;
import com.CommFunction;
import com.CommCrdRoutine;
import com.CommRoutine;

public class BilA023 extends AccessDAO {
    private String progname = "合格化回饋處理程式 111/09/22  V1.00.02 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;

    String prgmId = "BilA023";
    String hBusinessDate = "";
    String hSystemDate = "";
    String hVouchDate = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";

    String hCallErrorDesc = "";
    String hCallRProgramCode = "";
    String hTempUser = "";
    long totalCnt = 0;
    String hSystemDateFull = "";
    String hRskoReferenceNo = "";
    String hRskoCardNo = "";
    String hRskoReferenceNoNew = "";
    String hRskoRskOrgCardno = "";
    double hRskoRskAmt = 0;
    double hRskoRskOrgAmt = 0;
    double hRskoIssueFee = 0;
    int hRskoIssueFeeTax = 0;
    String hRskoRskType = "";
    String hRskoRskMark = "";
    String hRskoStdVouchCd = "";
    String hRskoModUser = "";
    String hRskoModTime = "";
    String hRskoCurrCode = "";
    String hRskoRowid = "";
    String hCurpId = "";
    String swPrint = "";
    double totalAmt = 0;
    double tempDoubleO = 0;
    double tempDouble = 0;
    double tempBal = 0;
    double feeAmt = 0;
    String hCurpReferenceNoFeeF = "";
    String hCurpReferenceNo = "";

    String hBinType = "";
    String hTransactionCode = "";
    String hCurpCardNo = "";
    String hCardMajorCardNo = "";
    String hCardAcctPSeqno = "";
    String hCardCardType = "";
    String hCurpMajorCardNo = "";
    String hCurpCurrentCode = "";
    String hCurpIssueDate = "";
    String hCurpOppostDate = "";
    String hCurpPromoteDept = "";
    String hCurpProdNo = "";
    String hCurpGroupCode = "";
    String hCurpSourceCode = "";

    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctStatus = "";
    String hAcnoStmtCycle = "";
    String hAcnoBlockStatus = "";
    String hAcnoBlockDate = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoAutopayAcctNo = "";
    String hCurpCardSw = "";
    String hCurpPSeqno = "";
    String hCurpAcctType = "";
    String hCurpAcctKey = "";
    String hCurpAcctStatus = "";
    String hCurpStmtCycle = "";
    String hCurpBlockStatus = "";
    String hCurpBlockDate = "";
    String hCurpPayByStageFlag = "";
    String hCurpAutopayAcctNo = "";

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        BilA023 proc = new BilA023();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            // 連線資料庫
            if (!connectDataBase()) {
                comc.errExit("Connect DB error !!", "");
            }

            swPrint = "N";

            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.lpar.clear();

            // 報表名稱
            comcr.hGsvhModWs = "BIL_A023R0";

            // ========================================================
            commonRtn();
            hModPgm = prgmId;
            // ========================================================

            // 程式處理開始
            selectBilRskok();

            //改成線上報表
            //String filename = comc.getECSHOME() + "/reports/BIL_A023R0_" + hSystemDateFull;
            //comc.writeReport(filename, comcr.lpar);
            //comcr.insertPtrBatchRpt(comcr.lpar);
            
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "]");

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    } // End of mainProcess
    // ************************************************************************

    private void selectBilRskok() throws Exception {
        sqlCmd = "SELECT reference_no,";
        sqlCmd += "reference_no_new,";
        sqlCmd += "card_no,";
        sqlCmd += "rsk_org_cardno,";
        sqlCmd += "decode(decode(curr_code, '', '901', curr_code),'901',rsk_amt    ,dc_rsk_amt) as rsk_amt,";
        sqlCmd += "decode(decode(curr_code, '', '901', curr_code),'901',rsk_org_amt,dc_rsk_org_amt) as rsk_org_amt,";
        sqlCmd += "issue_fee,";
        sqlCmd += "issue_fee_tax,";
        sqlCmd += "rsk_type,";
        sqlCmd += "rsk_mark,";
        sqlCmd += "std_vouch_cd,";
        sqlCmd += "mod_user,";
        sqlCmd += "to_char(mod_time,'yyyymmdd') as mod_time,";
        sqlCmd += "decode(curr_code, '', '901', curr_code) as curr_code,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += "  from bil_rskok ";
        sqlCmd += " where post_flag != 'Y' and decode(rsk_type,'','N',rsk_type) != 'D'";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hRskoReferenceNo = getValue("reference_no", i);
            hRskoReferenceNoNew = getValue("reference_no_new", i);
            hRskoCardNo = getValue("card_no", i);
            hRskoRskOrgCardno = getValue("rsk_org_cardno", i);
            hRskoRskAmt = getValueDouble("rsk_amt", i);
            hRskoRskOrgAmt = getValueDouble("rsk_org_amt", i);
            hRskoIssueFee = getValueDouble("issue_fee", i);
            hRskoIssueFeeTax = getValueInt("issue_fee_tax", i);
            hRskoRskType = getValue("rsk_type", i);
            hRskoRskMark = getValue("rsk_mark", i);
            hRskoStdVouchCd = getValue("std_vouch_cd", i);
            hRskoModUser = getValue("mod_user", i);
            hRskoModTime = getValue("mod_time", i);
            hRskoCurrCode = getValue("curr_code", i);
            hRskoRowid = getValue("rowid", i);

            totalCnt++;
            comcr.hGsvhCurr = selectPtrCurrcode();

            /* debug
                showLogMessage("D","","8888 Beg  id=[" + hRskoCardNo + "]");
                showLogMessage("D","","        amt =[" + hRskoRskAmt + "]" + hRskoRskOrgAmt);
                showLogMessage("D","","        ref =[" + hRskoReferenceNo + "]");
                showLogMessage("D","","       curr =[" + comcr.hGsvhCurr + "]" + hRskoStdVouchCd);
            */
            
            selectBilCurpost();
            
            /*不起帳
            if (hRskoStdVouchCd.length() > 0) {
                //vouchRtn();
            	;
            } else {
                selectBilCurpost();
            }
            */

            daoTable   = "bil_rskok";
            updateSQL  = "post_flag         = 'Y',";
            updateSQL += "mod_pgm           = 'BilA023',";
            updateSQL += "mod_time          = sysdate  ,";
            updateSQL += "reference_no_new  = ?   ";
            whereStr = "where rowid   = ?   ";
            setString(1, hCurpReferenceNo);
            setRowId(2, hRskoRowid);

            updateTable();

        }
    }

    // ************************************************************************
    private String selectPtrCurrcode() throws Exception {
        sqlCmd = "select curr_code_gl ";
        sqlCmd += " from ptr_currcode where curr_code = ?";
        setString(1, hRskoCurrCode);
        if (selectTable() > 0) {
            return getValue("curr_code_gl");
        }
        return "";
    }

    // ************************************************************************
    private void vouchRtn() throws Exception {
        String hCurpIdHide = "";
        String hBusinssChiDate = "";
        String hVouchChiDate = "";
        String hVouchCdKind = "";
        String hTempType = "";
        double tempAmt = 0;
        String hTAcNo = "";
        int hTSeqno = 0;
        String hTMemo3Kind = "";
        String hTMemo3Flag = "";
        String hTDbcr = "";
        String hTCrFlag = "";
        String hTDrFlag = "";
        String hTMemo3 = "";
        String tMemo3 = "";
        int vouchCnt = 0;

        hCurpId = "";
        sqlCmd = "select crd_idno.id_no as id, ";
        sqlCmd += "uf_hi_idno(crd_idno.id_no) as id1 ";
        sqlCmd += " from crd_card ";
        sqlCmd += " left join crd_idno on crd_idno.ID_P_SEQNO = crd_card.MAJOR_ID_P_SEQNO";
        sqlCmd += " where  card_no  = ?";
        setString(1, hRskoCardNo);
        if (selectTable() > 0) {
            hCurpId = getValue("id");
            hCurpIdHide = getValue("id1");
        }

        sqlCmd = "select substr(to_char(to_number(business_date)- 19110000,'0000000'),2,7) as businss_chi_date, ";
        sqlCmd += "substr(to_char(to_number(vouch_date)- 19110000,'0000000'),2,7) as vouch_chi_date";
        sqlCmd += " from ptr_businday";

        if (selectTable() > 0) {
            hBusinssChiDate = getValue("businss_chi_date");
            hVouchChiDate = getValue("vouch_chi_date");
        }
        
        /* debug
            showLogMessage("D", "", "8888 CHI   D=[" + hVouchChiDate + "]");
        */

        hVouchCdKind = hRskoStdVouchCd;

        selectBilCurpost();

        comcr.hGsvhModPgm = prgmId;
        /** OP --- > 1 **/

        sqlCmd = "select b.gl_code from sec_user  a, ptr_dept_code b ";
        sqlCmd += " where a.usr_id     = ? ";
        sqlCmd += "   and a.usr_deptno = b.dept_code ";
        setString(1, hRskoModUser);

        if (selectTable() > 0) {
            hTempType = getValue("gl_code");
        } else {
            hTempType = "1";
        }

        comcr.hVoucSysRem = hVouchCdKind;

        comcr.startVouch(hTempType, hVouchCdKind);

        swPrint = "Y";

        sqlCmd = "SELECT ";
        sqlCmd += "gen_sys_vouch.ac_no, ";
        sqlCmd += "gen_sys_vouch.dbcr_seq,";
        sqlCmd += "gen_sys_vouch.dbcr,";
        sqlCmd += "gen_acct_m.memo3_kind,";
        sqlCmd += "decode(gen_acct_m.memo3_flag,'','N',gen_acct_m.memo3_flag) as memo3_flag, ";
        sqlCmd += "decode(gen_acct_m.dr_flag   ,'','N',gen_acct_m.dr_flag) as dr_flag, ";
        sqlCmd += "decode(gen_acct_m.cr_flag   ,'','N',gen_acct_m.cr_flag) as cr_flag ";
        sqlCmd += "  FROM gen_sys_vouch,gen_acct_m ";
        sqlCmd += " where std_vouch_cd        = ? ";
        sqlCmd += "   and gen_sys_vouch.ac_no = gen_acct_m.ac_no ";
        sqlCmd += " order by gen_sys_vouch.dbcr_seq,decode(dbcr,'D','A',dbcr)";
        setString(1, hVouchCdKind);

        int recordCnt = selectTable();

        for (int i = 0; i < recordCnt; i++) {
            hTSeqno = 0;
            hTAcNo      = getValue("ac_no", i);
            hTSeqno      = getValueInt("dbcr_seq", i);
            hTDbcr       = getValue("dbcr", i);
            hTMemo3Kind = getValue("memo3_kind", i);
            hTMemo3Flag = getValue("memo3_flag", i);
            hTDrFlag    = getValue("dr_flag", i);
            hTCrFlag    = getValue("cr_flag", i);

            totalAmt = hRskoRskAmt;
            tempAmt  = hRskoRskAmt - hRskoRskOrgAmt + (hRskoIssueFee + hRskoIssueFeeTax);

            comcr.hVoucIdNo = "";
            comcr.hGsvhMemo1 = "";
            comcr.hGsvhMemo3 = "";
            tMemo3 = comcr.formatDate(hBusinssChiDate, 6);
            if (hVouchCdKind.equals("R271") || hVouchCdKind.equals("R261")) {
                tMemo3 = String.format("%7.7s-%s", hBusinssChiDate, hCurpIdHide);
                if (hVouchCdKind.equals("R261") && hTSeqno == 2) {
                    comcr.hGsvhMemo1 = hRskoCardNo;
                }
            }

            comcr.hGsvhMemo2 = tMemo3;
            if (hVouchCdKind.equals("R024") || hVouchCdKind.equals("R042")) {
                switch (hTSeqno) {
                    case 1:
                        totalAmt = hRskoRskAmt;
                        break;
                    case 2: // 借方
                        if (hRskoRskOrgAmt == 0) {
                            totalAmt = 0;
                        } else {
                            totalAmt = tempAmt < 0 ? tempAmt * (-1) : 0;
                        }
                        break;
                    case 3: // 貸方
                        totalAmt = hRskoRskOrgAmt > 0 ? hRskoRskOrgAmt : hRskoRskAmt;
                        break;
                    case 4: // 貸方
                        if (hRskoRskOrgAmt == 0) {
                            totalAmt = 0;
                        } else {
                            totalAmt = tempAmt > 0 ? tempAmt : 0;
                        }
                        tempDoubleO = totalAmt;
                        tempDouble = totalAmt / 1.05;
                        tempBal = comcr.commCurrAmt(hRskoCurrCode, tempDouble, 0);
                        totalAmt = tempBal;
                        break;
                    case 5: // 貸方
                        totalAmt = tempDoubleO - tempBal;
                        break;
                    case 6: // 借方
                        comcr.hGsvhMemo1 = hRskoCardNo;
                        totalAmt = hRskoIssueFee;
                        break;
                    case 7: // 借方
                        comcr.hGsvhMemo1 = hRskoCardNo;
                        totalAmt = hRskoIssueFeeTax;
                        break;
                    default:
                        totalAmt = 0;
                        break;
                }
            } else if (hVouchCdKind.equals("R055")) {
                totalAmt = hRskoRskAmt;
                if (hTSeqno == 1) {
                    comcr.hGsvhMemo2 = hRskoCardNo;
                }
                if (feeAmt > 0) {
                    if ((hBinType.equals("V") &&
                            (hTransactionCode.equals("25") || hTransactionCode.equals("26") ||
                                    hTransactionCode.equals("27"))) ||
                            (hBinType.equals("M") &&
                                    (hTransactionCode.equals("25") || hTransactionCode.equals("06") ||
                                            hTransactionCode.equals("27")))) {
                        switch (hTSeqno) {
                            case 3:
                                comcr.hGsvhMemo2 = hRskoCardNo;
                                tempDouble = feeAmt / 1.05;
                                tempBal = comcr.commCurrAmt(hRskoCurrCode, tempDouble, 0);
                                totalAmt = tempBal;
                                break;
                            case 4:
                                comcr.hGsvhMemo2 = hRskoCardNo;
                                totalAmt = feeAmt - tempBal;
                                break;
                            case 5:
                                totalAmt = feeAmt;
                                break;
                        } // switch(h_t_seqno)
                    } else {
                        if (hTSeqno == 3 || hTSeqno == 4 || hTSeqno == 5) {
                            totalAmt = 0;
                        }
                    }
                } else {
                    if (hTSeqno == 3 || hTSeqno == 4 || hTSeqno == 5) {
                        totalAmt = 0;
                    }
                }
            } else if (hVouchCdKind.equals("R026") || hVouchCdKind.equals("R027") ||
                    hVouchCdKind.equals("R043") || hVouchCdKind.equals("R261") ||
                    hVouchCdKind.equals("R271")) {
                totalAmt = hRskoRskAmt;
            } else if (hVouchCdKind.equals("P-13")) {
                if (hTSeqno == 13)
                    totalAmt = hRskoRskAmt;
                else
                    totalAmt = 0;
            }

            /* debug
                showLogMessage("D", "", "8888 memo3 f=[" + hTMemo3Flag + "]" + hTDbcr);
            */
            
            if (hTMemo3Flag.equals("Y")) {
                if ((hTDbcr.equals("D") && hTCrFlag.equals("Y")) ||
                        (hTDbcr.equals("C") && hTDrFlag.equals("Y"))) {
                    vouchCnt++;
                }

                comcr.hGsvhMemo3 = "";
                tMemo3 = "";

                if (hTMemo3Kind.equals("1")) {
                    comcr.hGsvhMemo3 = hRskoCardNo;
                } else {
                    if (hTMemo3Kind.equals("2")) {
                        tMemo3 = String.format("%4.4s%-12.12s", "ID :", hCurpId);
                        comcr.hVoucIdNo = hCurpId;
                        comcr.hGsvhMemo3 = tMemo3;
                    } else {
                        if (hTMemo3Kind.equals("3")) {
                            tMemo3 = String.format("%6.6s%6.6s%02d", hVouchChiDate.substring(1), comcr.hVoucRefno,
                                    vouchCnt);
                            comcr.hGsvhMemo3 = tMemo3;
                        }
                    }
                }
            } // end of if(h_t_memo3_flag == "Y")
            
            /* debug
                showLogMessage("D", "", "8888 memo3  =[" + comcr.hGsvhMemo3 + "]");
            */

            /* 銷帳 */
            String hTempMemo3 = "";
            sqlCmd  = "select memo3 from gen_memo3 ";
            sqlCmd += " where ref_no = ? ";
            sqlCmd += "   and amt    = ? ";
            sqlCmd += "   and ac_no  = ?";
            setString(1, hRskoReferenceNo);
            setDouble(2, hRskoRskAmt);
            setString(3, hTAcNo);
            if (selectTable() > 0) {
                hTempMemo3 = getValue("memo3");
            }

            if (hTempMemo3.length() > 0) {
                comcr.hGsvhMemo3 = hTempMemo3;
            }

            comcr.hGsvhModUser = hRskoReferenceNo;

            if (totalAmt > 0) {
                if (comcr.detailVouch(hTAcNo, hTSeqno, totalAmt, hRskoCurrCode) != 0)
                    comc.errExit("detail_vouch error", "");
            }
        } // end of for loop
    }

    // ************************************************************************
    private void selectBilCurpost() throws Exception {
        String hCurpRowid = "";
        String hCurpRskType = "";
        hCurpReferenceNoFeeF = "";
        hTransactionCode = "";
        double hCurpDcExchangeRate = 0;

        sqlCmd = "SELECT rowid  as rowid, ";
        sqlCmd += "rsk_type, ";
        sqlCmd += "TXN_CODE, ";
        sqlCmd += "reference_no_fee_f, ";
        sqlCmd += "dc_exchange_rate ";
        sqlCmd += "  FROM bil_curpost ";
        sqlCmd += " where reference_no = ?";
        setString(1, hRskoReferenceNo);

        if (selectTable() > 0) {
            hCurpRowid = getValue("rowid");
            hCurpRskType = getValue("rsk_type");
            hTransactionCode = getValue("TXN_CODE");
            hCurpReferenceNoFeeF = getValue("reference_no_fee_f");
            hCurpDcExchangeRate = getValueDouble("dc_exchange_rate");
        } else {
            String err1 = "select_bil_curpost 0 error!" + hRskoReferenceNo;
            String err2 = "";
            comc.errExit(err1, err2);
        }

        feeAmt = 0;
        if (hCurpReferenceNoFeeF.length() > 0) {
            selectBilCurpostFee();
        }

        String hTempX08 = "";
        sqlCmd = "select substr(to_char(bil_postseq.nextval,'0000000000'),4,8) as h_temp_x08 ";
        sqlCmd += " from dual";
        if (selectTable() > 0) {
            hTempX08 = getValue("h_temp_x08");
        }
        /* %2.2s 強制 補 2 碼 */
        String tempX10 = String.format("%2.2s%s", hBusinessDate.substring(2), hTempX08);
        hCurpReferenceNo = tempX10;
        
        /* debug
            showLogMessage("D", "", "8888 New ref=[" + hCurpReferenceNo + "]" + hRskoCurrCode);
		*/
        
        double hCurpDcAmount = 0;
        double hCurpDestinationAmt = 0;
        long tempLong = 0;
        if (hRskoCurrCode.equals("901"))
            hCurpDestinationAmt = hRskoRskAmt;
        else {
            hCurpDcAmount = hRskoRskAmt;
            tempLong = (long) (hCurpDcExchangeRate * hRskoRskAmt + 0.5);
            hCurpDestinationAmt = tempLong;
        }
        hCurpCardNo = hRskoCardNo;

        if ((hRskoCardNo.equals(hRskoRskOrgCardno) == false) || ("1".equals(hCurpRskType))) {
            chkCrdCard();
            daoTable = "bil_curpost";
            updateSQL = "major_card_no= ? , ";
            updateSQL += "valid_flag   = 'N' ,";
            updateSQL += "issue_date   = ? , ";
            updateSQL += "promote_dept = ? , ";
            updateSQL += "prod_no      = ? , ";
            updateSQL += "group_code   = ? ,";
            updateSQL += "acno_p_seqno = ? ,";
            updateSQL += "source_code  = ? ,";
            updateSQL += "acct_type    = ? ,";
            updateSQL += "stmt_cycle   = ? ,";
            updateSQL += "rsk_type           = '',";
            updateSQL += "duplicated_flag    = '',";
            updateSQL += "doubt_type         = '',";
            updateSQL += "contract_flag      = 'N',";
            updateSQL += "err_chk_ok_flag    = 'N',";
            updateSQL += "double_chk_ok_flag = 'N',";
            updateSQL += "format_chk_ok_flag = 'N',";
            updateSQL += "curr_post_flag     = 'N',";
            updateSQL += "tx_convt_flag      = 'R' ";
            whereStr = " where rowid   = ?";
            setString(1, hCurpMajorCardNo);
            setString(2, hCurpIssueDate);
            setString(3, hCurpPromoteDept);
            setString(4, hCurpProdNo);
            setString(5, hCurpGroupCode);
            setString(6, hCurpPSeqno);
            setString(7, hCurpSourceCode);
            setString(8, hCurpAcctType);
            setString(9, hCurpStmtCycle);
            setRowId(10, hCurpRowid);
            updateTable();
        }

        daoTable = "bil_curpost";
        updateSQL = "rsk_type           = '',";
        updateSQL += "duplicated_flag    = '',";
        updateSQL += "doubt_type         = '',";
        updateSQL += "contract_flag      = 'N',";
        updateSQL += "format_chk_ok_flag = 'N',";
        updateSQL += "double_chk_ok_flag = 'N',";
        updateSQL += "err_chk_ok_flag    = 'N',";
        updateSQL += "curr_post_flag     = 'N',";
        updateSQL += "card_no            = ?,";
        updateSQL += "dest_amt           = ?,";
        updateSQL += "dc_exchange_rate   = ?,";
        updateSQL += "tx_convt_flag      = 'R' ,";
        updateSQL += "mod_time           = timestamp_format(?,'YYYYMMDDHH24MISS'),";
        updateSQL += "reference_no       = ? ";
        whereStr = " where rowid     = ? ";
        setString(1, hCurpCardNo);
        setDouble(2, hCurpDestinationAmt);
        setDouble(3, hCurpDcExchangeRate);
        setString(4, sysDate + sysTime);
        setString(5, hCurpReferenceNo);
        setRowId(6, hCurpRowid);
        updateTable();
        if (notFound.equals("Y")) {
            String err1 = "update_bil_curpost 1 error!=" + hCurpCardNo;
            String err2 = "";
            comc.errExit(err1, err2);
        }
        
        /* debug
            showLogMessage("D", "", "8888 upadte =[" + hCurpReferenceNo + "]" + hCurpCardNo);
        */

        if (hCurpReferenceNoFeeF.length() > 0) {
            if ((hRskoCardNo.equals(hRskoRskOrgCardno) == false) || ("1".equals(hCurpRskType))) {
                daoTable   = "bil_curpost";
                updateSQL  = "major_card_no= ? , ";
                updateSQL += "valid_flag   = 'N',";
                updateSQL += "curr_code    = ? , ";
                updateSQL += "issue_date   = ? , ";
                updateSQL += "promote_dept = ? , ";
                updateSQL += "prod_no      = ? , ";
                updateSQL += "group_code   = ? ,";
                updateSQL += "acno_p_seqno = ? ,";
                updateSQL += "source_code  = ? ,";
                updateSQL += "acct_type    = ? ,";
                updateSQL += "stmt_cycle   = ? ,";
                updateSQL += "rsk_type           = '',";
                updateSQL += "duplicated_flag    = '',";
                updateSQL += "doubt_type         = '',";
                updateSQL += "err_chk_ok_flag    = 'N',";
                updateSQL += "double_chk_ok_flag = 'N',";
                updateSQL += "format_chk_ok_flag = 'N',";
                updateSQL += "curr_post_flag     = 'N',";
                updateSQL += "tx_convt_flag      = 'R' ";
                whereStr = " where reference_no  = ?";
                setString(1, hCurpMajorCardNo);
                setString(2, hRskoCurrCode);
                setString(3, hCurpIssueDate);
                setString(4, hCurpPromoteDept);
                setString(5, hCurpProdNo);
                setString(6, hCurpGroupCode);
                setString(7, hCurpPSeqno);
                setString(8, hCurpSourceCode);
                setString(9, hCurpAcctType);
                setString(10, hCurpStmtCycle);
                setString(11, hCurpReferenceNoFeeF);
                updateTable();
            }

            hTempX08 = "";
            sqlCmd = "select substr(to_char(bil_postseq.nextval,'0000000000'),4,8) as h_temp_x08 ";
            sqlCmd += "from dual";
            if (selectTable() > 0) {
                hTempX08 = getValue("h_temp_x08");
            }

            /* %2.2s 強制 補 2 碼 */
            tempX10 = String.format("%2.2s%s", hBusinessDate.substring(2), hTempX08);
            
            /* debug
                showLogMessage("D", "", "8888   x10  =[" + tempX10 + "]" + hCurpCardNo);
            */
            
            daoTable   = "bil_curpost";
            updateSQL  = "rsk_type             = '',";
            updateSQL += "duplicated_flag      = '',";
            updateSQL += "doubt_type           = '',";
            updateSQL += "contract_flag        = 'N',";
            updateSQL += "format_chk_ok_flag   = 'N',";
            updateSQL += "double_chk_ok_flag   = 'N',";
            updateSQL += "err_chk_ok_flag      = 'N',";
            updateSQL += "curr_post_flag       = 'N',";
            updateSQL += "card_no              = ?,";
            updateSQL += "reference_no         = ?,";
            updateSQL += "reference_no_original= ?,";
            updateSQL += "tx_convt_flag        = 'R' ";
            whereStr   = " where reference_no  = ?";
            setString(1, hCurpCardNo);
            setString(2, tempX10);
            setString(3, hCurpReferenceNo);
            setString(4, hCurpReferenceNoFeeF);
            updateTable();

            daoTable  = "bil_curpost";
            updateSQL = " reference_no_fee_f  = ? ";
            whereStr  = " where reference_no  = ?";
            setString(1, tempX10);
            setString(2, hCurpReferenceNo);
            updateTable();
        }
    }

    // ************************************************************************
    private void selectBilCurpostFee() throws Exception {
        feeAmt = 0;
        hBinType = "";
        sqlCmd = "SELECT decode(decode(curr_code, '','901', curr_code),'901',dest_amt,dc_amount) as fee_amt, ";
        sqlCmd += " bin_type ";
        sqlCmd += " FROM bil_curpost ";
        sqlCmd += " where reference_no = ?";
        setString(1, hCurpReferenceNoFeeF);
        if (selectTable() > 0) {
            feeAmt = getValueDouble("fee_amt");
            hBinType = getValue("bin_type");
        }
    }

    // ************************************************************************
    private void chkCrdCard() throws Exception {

        String hPbtbBinType = "";
        initCrdCard();

        sqlCmd = "select major_card_no, ";
        sqlCmd += "current_code, ";
        sqlCmd += "issue_date, ";
        sqlCmd += "oppost_date, ";
        sqlCmd += "promote_dept, ";
        sqlCmd += "prod_no, ";
        sqlCmd += "group_code, ";
        sqlCmd += "source_code, ";
        sqlCmd += "card_type, ";
        sqlCmd += "p_seqno,";
        sqlCmd += "bin_type,";
        sqlCmd += "crd_idno.id_no";
        sqlCmd += " from crd_card ";
        sqlCmd += " left join crd_idno on crd_idno.ID_P_SEQNO = crd_card.MAJOR_ID_P_SEQNO";
        sqlCmd += " where card_no =  ?";
        setString(1, hCurpCardNo);
        if (selectTable() > 0) {
            hCurpMajorCardNo = getValue("major_card_no");
            hCurpCurrentCode = getValue("current_code");
            hCurpIssueDate = getValue("issue_date");
            hCurpOppostDate = getValue("oppost_date");
            hCurpPromoteDept = getValue("promote_dept");
            hCurpProdNo = getValue("prod_no");
            hCurpGroupCode = getValue("group_code");
            hCurpSourceCode = getValue("source_code");
            hCardCardType = getValue("card_type");
            hCardAcctPSeqno = getValue("p_seqno");
            hPbtbBinType = getValue("bin_type");
            hCurpId = getValue("ID_NO");
        }

        hCurpCardSw = hPbtbBinType;

        hAcnoAcctType = "";
        hAcnoAcctKey = "";
        hAcnoAcctStatus = "";
        hAcnoStmtCycle = "";
        hAcnoBlockStatus = "";
        hAcnoBlockDate = "";
        hAcnoPayByStageFlag = "";
        hAcnoAutopayAcctNo = "";

        sqlCmd = "select acct_type, ";
        sqlCmd += "acct_key, ";
        sqlCmd += "acct_status, ";
        sqlCmd += "stmt_cycle, ";
        sqlCmd += "pay_by_stage_flag, ";
        sqlCmd += "autopay_acct_no ";
        sqlCmd += " from act_acno where acno_p_seqno = ?";
        setString(1, hCardAcctPSeqno);
        if (selectTable() > 0) {
            hAcnoAcctType = getValue("acct_type");
            hAcnoAcctKey = getValue("acct_key");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
            hAcnoAutopayAcctNo = getValue("autopay_acct_no");
        }

        hCurpPSeqno = hCardAcctPSeqno;
        hCurpAcctType = hAcnoAcctType;
        hCurpAcctKey = hAcnoAcctKey;
        hCurpAcctStatus = hAcnoAcctStatus;
        hCurpStmtCycle = hAcnoStmtCycle;
        hCurpPayByStageFlag = hAcnoPayByStageFlag;
        hCurpAutopayAcctNo = hAcnoAutopayAcctNo;
    }

    // ************************************************************************
    private void initCrdCard() {
        hCardMajorCardNo = "";
        hCardAcctPSeqno = "";
        hCardCardType = "";
    }

    // ************************************************************************
    private void commonRtn() throws Exception {
        sqlCmd = "select business_date,vouch_date ";
        sqlCmd += " from ptr_businday";

        if (selectTable() > 0) {
            hBusinessDate = getValue("business_date");
            hVouchDate = getValue("vouch_date");
        }

        // =============================
        sqlCmd = "select to_char(sysdate,'yyyymmdd')         date1,";
        sqlCmd += "      to_char(sysdate,'yyyymmddhh24miss') date2 ";
        sqlCmd += " from dual";

        if (selectTable() > 0) {
            hSystemDate = getValue("date1");
            hSystemDateFull = getValue("date2");
        }

        hModSeqno = comcr.getModSeq();
        hModUser = comc.commGetUserID();
        hModTime = hSystemDate;
        showLogMessage("I", "", "本日營業日 : [" + hBusinessDate + "] [" + hVouchDate + "]");
    }
    // ************************************************************************
}
