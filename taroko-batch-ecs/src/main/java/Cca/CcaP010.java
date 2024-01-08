/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  109-07-03  V1.00.00   shiyuqi       updated for project coding standard   *
 *  109-07-22  V1.00.01   yanghan       修改了字段名称                                                                        *
 *  109-09-18  V1.00.02   Alex          排除沖銷交易                                                                            *
 *  109-10-19  V1.00.07   shiyuqi       updated for project coding standard   *
 *  109-11-06  V1.00.08   Alex		   reset Amt fix						 *
 *  109-11-09  V1.00.09   Alex          cca_auth_txlog 無資料時不 update 最後一筆       *
 *  109-11-11  V1.00.10   Alex		   reset Amt fix						 *
 *  109-11-11  V1.00.11   Alex          營業日不-1								 *
 *  109-11-12  V1.00.12   Alex          增加判斷此卡號是否存在於系統					 *
 *  109-11-16  V1.00.13   Alex          總計個繳消費額度異動到總繳戶					 *
 *  110-01-21  V1.00.14   Alex          原始交易識別碼欄位變更						 *
 *  110-02-03  V1.00.15   Alex          UserExpireDate 欄位修正					 *
 *  110-03-04  v1.00.16   Alex		        程式執行完畢日期更正至系統參數				 *
 *  110-03-08  v1.00.17   Alex		        新增預借現金判斷條件						 *
 *  110-03-17  v1.00.18   Alex          排除退貨交易								 *
 *  110-11-23  v1.00.19   Alex          updateSysParm 位置變動                                                *
 *  111/02/14  V1.00.20   Ryan         big5 to MS950                                           *
 *  111/03/22  V1.00.21   Alex          update cca_consume 前先 check           *
 *  111/03/25  V1.00.22   Alex          取消 update 前 check , 改為 update不到不失敗僅顯示訊息 *
 *  111/03/25  V1.00.23   Alex          逐筆 commit                             *
 *  111/03/31  V1.00.24   Alex          雙幣卡放清算金額                                                                     *
 *  111/04/01  V1.00.25   Alex          沖正時補讀 curr_code                      *
 *  111/05/03  V1.00.26   Alex          預先授權資料處理                                                                     *
 *  2023-1110  V1.00.27    JH          check ptr_sys_parm.CCAP010 is run
 *  112/12/05  V1.00.28   JeffKung      排除批次授權
 *****************************************************************************/
package Cca;

import java.text.NumberFormat;

import com.CommCrd;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.BaseBatch;
import com.CommString;

public class CcaP010 extends BaseBatch {

private final String progname = "產生每日授權交易記錄檔 2023-1205  V1.00.28";
CommFunction comm = new CommFunction();
CommCrd comc = new CommCrd();
CommString commString = new CommString();
CommFTP commFTP = null;
CommRoutine comr = null;
private int ilFileNum;
String prgmId = "CcaP010";
String fileName = "";
String temstr = "";
String hCallBatchSeqno = "";
String hCardNo = "";
String hUserExpireDate = "";
String hTxDate = "";
String hTxTime = "";
String hTxGmtDate = "";
String hTxGmtTime = "";
Double hNtAmt = 0.0;
Double hOriAmt = 0.0;
String hTxCurrency = "";
String hBankCountry = "";
String hBankCountryNum = "";
String hStandIn = "";
String hMchtNo = "";
String hMccCode = "";
String hAuthStatusCode = "";
String hAuthNo = "";
String hTraceNo = "";
String hRefNo = "";
String hPoMode = "";
String hAuthSource = "";
String hTxnIdf = "";
String hVCardNo = "";
String hIsoRespCode = "";
String hChgDate = "";
String hChgTime = "";
String hAuthType = "";
String hLastCard = "";
Double hSumAmt = 0.0;
Double hSumCash = 0.0;
String hAcnoPSeqno = "";
String hDebitFlag = "";
String hVdcardFlag = "";
String hLastDebit = "";
String hProcCode = "";
String hTransCode = "";
Double hCardAcctIdx = 0.0;
String hTransType = "";
String hOriAuthSeqno = "";
String hReversalFlag = "";
String hCacuAmount = "";
String hCacuCash = "";
String hCardCurr = "";
Double hCurrNtAmt = 0.0;
//--
String hCorpPSeqno = "";
String hAcctType = "";
String hCorpAcnoPSeqno = "";
Double hCorpCardAcctIdx = 0.0;
String hLastAcctType = "";
String hLastCorpPSeqno = "";
String hOriTxnIdf = "";
boolean ibReversal = false;

public static void main(String[] args) {
   CcaP010 proc = new CcaP010();
   // proc.debug = true;
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);
   dateTime();
   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : Ccap010 [business_date]");
      okExit(0);
   }

   dbConnect();
   if (liArg == 1) {
      this.setBusiDate(args[0]);
   }

   //ptr_sys_parm.(SYSPARM,ROLLBACK_P2).文字1=Y--
   int liRC=checkIsRun();
   if (liRC !=0) {
      printf("--此作業指定停止執行, ptr_sys_parm.(SYSPARM,ROLLBACK_P2).文字1=N");
      okExit(0);
   }

   resetAmt1Cash1();
   sqlCommit();
   fileName = "AUTHLOG_"+hBusiDate+".TXT";
   checkOpen();
   selectAuthTxlog();
   selectAuthTxlog2();

   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   //--更新程式完成日期
   updateSysParm();
   sqlCommit();
   endProgram();
}
//---
int checkIsRun() throws Exception {
   //文字值1=N 表示P3不執行--
   sqlCmd ="select count(*) as run_cnt"
       +" from ptr_sys_parm"
       +" where wf_parm ='SYSPARM'"
       +" and wf_key ='ROLLBACK_P2' "
       +" and wf_value ='N' "  //stop
       ;

   sqlSelect();
   if (sqlNrow >0 && colInt("run_cnt")>0) {
      return 1;
   }
   return 0;
}

void checkOpen() throws Exception {
   temstr = String.format("%s/media/cca/%s", comc.getECSHOME(), fileName);
   ilFileNum = openOutputText(temstr, "MS950");
   if (ilFileNum < 0) {
      printf("開啟檔案失敗 ! ");
      errExit(1);
   }
}


void selectAuthTxlog() throws Exception {

   //--不含沖正當日授權交易 , 若當日授權交易有沖正時則在原交易的更正日期時間有值 , 沖正交易不寫入只寫入原交易
   //--含沖正非當日授權交易 , 資料內容同原交易僅更正日期時間不同 , 授權來源碼為 R
   //--logic_del=B 退貨交易不寫授權交易記錄檔 , 等請款資料來時才還額

   sqlCmd = " select ";
   sqlCmd += " A.card_no , A.user_expire_date , A.tx_date , A.tx_time , ";
   sqlCmd += " to_char(A.tx_datetime,'yyyymmdd') as tx_gmt_date , to_char(A.tx_datetime,'hh24miss') as tx_gmt_time , ";
   sqlCmd += " decode(A.chg_date,'','00000000',A.chg_date) as chg_date , decode(A.chg_time,'','000000',A.chg_time) as chg_time ,";
   sqlCmd += " A.nt_amt , A.ori_amt , A.tx_currency , A.bank_country , A.stand_in , A.mcht_no , A.mcc_code , ";
   sqlCmd += " A.auth_status_code , A.auth_no , A.trace_no , A.ref_no , A.pos_mode , A.auth_source , A.ori_auth_seqno , ";
   sqlCmd += " A.txn_idf , A.v_card_no , A.iso_resp_code , A.auth_type , A.vdcard_flag , A.proc_code , A.trans_code , A.trans_type , A.reversal_flag , ";
   sqlCmd += " A.cacu_amount , A.cacu_cash , A.corp_p_seqno , A.acct_type , A.ori_txn_idf , A.curr_nt_amt , B.curr_code ";
   sqlCmd += " from cca_auth_txlog A left join crd_card B on A.card_no = B.card_no where 1=1 and A.tx_date = ? and A.logic_del <> 'B' and A.trans_code <> 'BA'";  //20231205 排除批次授權
   sqlCmd += " order by A.card_no ";

   setString(1, hBusiDate);

   openCursor();

   while (fetchTable()) {
      initData();
      totalCnt++;
      hCardNo = getValue("card_no");
      hUserExpireDate = getValue("user_expire_date");
      hTxDate = getValue("tx_date");
      hTxTime = getValue("tx_time");
      hTxGmtDate = getValue("tx_gmt_date");
      hTxGmtTime = getValue("tx_gmt_time");
      hChgDate = getValue("chg_date");
      hChgTime = getValue("chg_time");
      hNtAmt = getValueDouble("nt_amt");
      hOriAmt = getValueDouble("ori_amt");
      hTxCurrency = getValue("tx_currency");
      hBankCountry = getValue("bank_country");
      hStandIn = getValue("stand_in");
      hMchtNo = getValue("mcht_no");
      hMccCode = getValue("mcc_code");
      hAuthStatusCode = getValue("auth_status_code");
      hIsoRespCode = getValue("iso_resp_code");
      hAuthNo = getValue("auth_no");
      hTraceNo = getValue("trace_no");
      hRefNo = getValue("ref_no");
      hPoMode = getValue("pos_mode");
      hAuthSource = getValue("auth_source");
      hTxnIdf = getValue("txn_idf");
      hVCardNo = getValue("v_card_no");
      hAuthType = getValue("auth_type");
      hVdcardFlag = getValue("vdcard_flag");
      hProcCode = getValue("proc_code");
      hTransCode = getValue("trans_code");
      hTransType = getValue("trans_type");
      hOriAuthSeqno = getValue("ori_auth_seqno");
      hReversalFlag = getValue("reversal_flag");
      hCacuAmount = getValue("cacu_amount");
      hCacuCash = getValue("cacu_cash");
      hCorpPSeqno = getValue("corp_p_seqno");
      hAcctType = getValue("acct_type");
      hOriTxnIdf = getValue("ori_txn_idf");
      hCardCurr = getValue("curr_code");
      if (hCardCurr.isEmpty())
         hCardCurr = "901";
      hCurrNtAmt = getValueDouble("curr_nt_amt");
      if (eqIgno(hVdcardFlag, "D")) {
         hDebitFlag = "Y";
      } else {
         hDebitFlag = "N";
      }
      //--trans_type = 0420 為沖正交易
      if (hTransType.equals("0420") && selectOriTxn() == false) continue;

      hBankCountryNum = getBankCountryNum(hBankCountry);
      writeText();
      if (checkCardNo(hCardNo) == false) continue;
      // --update cca_card_acct
      if (empty(hLastCard)) {
         hLastCard = hCardNo;
         hLastDebit = hDebitFlag;
         hLastCorpPSeqno = hCorpPSeqno;
         hLastAcctType = hAcctType;
         hCorpAcnoPSeqno = "";
         hAcnoPSeqno = "";
         hSumAmt = 0.0;
         hSumCash = 0.0;
         hCardAcctIdx = 0.0;
         hCorpCardAcctIdx = 0.0;
      }

      if (eqIgno(hLastCard, hCardNo)) {
         // --待確認
         if (hIsoRespCode.equals("00")) {
            if (ibReversal) {
               if (commString.ssIn(hTransCode, "CA|CO|AC|MA")) hSumCash -= hNtAmt;
               hSumAmt -= hNtAmt;
            } else {
               if (commString.ssIn(hTransCode, "CA|CO|AC|MA") && hCacuCash.equals("Y")) hSumCash += hNtAmt;
               if (hCacuAmount.equals("Y")) hSumAmt += hNtAmt;
            }
         }
      } else {
         //--update 上一筆的 cca_consume
         hAcnoPSeqno = selectActAcno(hLastCard, hLastDebit);
         hCardAcctIdx = selectCardAcctIdx(hAcnoPSeqno, hLastDebit);
         updateCcaConsume(hCardAcctIdx);
         //--公司戶要 Update cca_consume
         if (hLastCorpPSeqno.isEmpty() == false) {
            hCorpAcnoPSeqno = selectActAcnoForCorp(hLastCorpPSeqno, hLastAcctType);
            hCorpCardAcctIdx = selectCardAcctIdx(hCorpAcnoPSeqno, hLastDebit);
            updateCcaConsume(hCorpCardAcctIdx);
         }
         //--逐筆commit
         sqlCommit();
         // --update 後記錄現在這筆資料
         hLastCard = hCardNo;
         hLastDebit = hDebitFlag;
         hLastCorpPSeqno = hCorpPSeqno;
         hLastAcctType = hAcctType;
         hAcnoPSeqno = "";
         hCorpAcnoPSeqno = "";
         if (hIsoRespCode.equals("00") == false) {
            hSumCash = 0.0;
            hSumAmt = 0.0;
         } else {
            hSumCash = 0.0;
            hSumAmt = 0.0;
            if (ibReversal) {
               if (commString.ssIn(hTransCode, "CA|CO|AC")) hSumCash -= hNtAmt;
               hSumAmt -= hNtAmt;
            } else {
               if (commString.ssIn(hTransCode, "CA|CO|AC") && hCacuCash.equals("Y")) hSumCash += hNtAmt;
               if (hCacuAmount.equals("Y")) hSumAmt += hNtAmt;
            }
         }
      }

   }
   closeCursor();
//		closeOutputText(ilFileNum);

   if (hLastCard.isEmpty()) return;
   //--補做最後一筆
   hAcnoPSeqno = selectActAcno(hLastCard, hLastDebit);
   hCardAcctIdx = selectCardAcctIdx(hAcnoPSeqno, hLastDebit);
   updateCcaConsume(hCardAcctIdx);
   if (hLastCorpPSeqno.isEmpty() == false) {
      hCorpAcnoPSeqno = selectActAcnoForCorp(hLastCorpPSeqno, hLastAcctType);
      hCorpCardAcctIdx = selectCardAcctIdx(hCorpAcnoPSeqno, hLastDebit);
      updateCcaConsume(hCorpCardAcctIdx);
   }
   //--最後一筆 commit
   sqlCommit();
}

void selectAuthTxlog2() throws Exception {
   initData2();
   //--處理預先授權資料 , 排除退貨和沖正資料只抓 chg_date = busi_date
   sqlCmd = " select ";
   sqlCmd += " A.card_no , A.user_expire_date , A.tx_date , A.tx_time , ";
   sqlCmd += " to_char(A.tx_datetime,'yyyymmdd') as tx_gmt_date , to_char(A.tx_datetime,'hh24miss') as tx_gmt_time , ";
   sqlCmd += " decode(A.chg_date,'','00000000',A.chg_date) as chg_date , decode(A.chg_time,'','000000',A.chg_time) as chg_time ,";
   sqlCmd += " A.nt_amt , A.ori_amt , A.tx_currency , A.bank_country , A.stand_in , A.mcht_no , A.mcc_code , ";
   sqlCmd += " A.auth_status_code , A.auth_no , A.trace_no , A.ref_no , A.pos_mode , A.auth_source , A.ori_auth_seqno , ";
   sqlCmd += " A.txn_idf , A.v_card_no , A.iso_resp_code , A.auth_type , A.vdcard_flag , A.proc_code , A.trans_code , A.trans_type , A.reversal_flag , ";
   sqlCmd += " A.cacu_amount , A.cacu_cash , A.corp_p_seqno , A.acct_type , A.ori_txn_idf , A.curr_nt_amt , B.curr_code ";
   sqlCmd += " from cca_auth_txlog A left join crd_card B on A.card_no = B.card_no where 1=1 and A.chg_date = ? and A.logic_del <> 'B' and A.reversal_flag <> 'Y' and A.trans_type ='0100' and vdcard_flag <> 'D' ";
   sqlCmd += " and A.trans_code <> 'BA' ";  //20231205 排除批次授權"
   sqlCmd += " order by A.card_no ";

   setString(1, hBusiDate);

   openCursor();

   while (fetchTable()) {
      initData();
      totalCnt++;
      hCardNo = getValue("card_no");
      hUserExpireDate = getValue("user_expire_date");
      hTxDate = getValue("tx_date");
      hTxTime = getValue("tx_time");
      hTxGmtDate = getValue("tx_gmt_date");
      hTxGmtTime = getValue("tx_gmt_time");
      hChgDate = getValue("chg_date");
      hChgTime = getValue("chg_time");
      hNtAmt = getValueDouble("nt_amt");
      hOriAmt = getValueDouble("ori_amt");
      hTxCurrency = getValue("tx_currency");
      hBankCountry = getValue("bank_country");
      hStandIn = getValue("stand_in");
      hMchtNo = getValue("mcht_no");
      hMccCode = getValue("mcc_code");
      hAuthStatusCode = getValue("auth_status_code");
      hIsoRespCode = getValue("iso_resp_code");
      hAuthNo = getValue("auth_no");
      hTraceNo = getValue("trace_no");
      hRefNo = getValue("ref_no");
      hPoMode = getValue("pos_mode");
      hTxnIdf = getValue("txn_idf");
      hVCardNo = getValue("v_card_no");
      hAuthType = getValue("auth_type");
      hVdcardFlag = getValue("vdcard_flag");
      hProcCode = getValue("proc_code");
      hTransCode = getValue("trans_code");
      hTransType = getValue("trans_type");
      hOriAuthSeqno = getValue("ori_auth_seqno");
      hReversalFlag = getValue("reversal_flag");
      hCacuAmount = getValue("cacu_amount");
      hCacuCash = getValue("cacu_cash");
      hCorpPSeqno = getValue("corp_p_seqno");
      hAcctType = getValue("acct_type");
      hOriTxnIdf = getValue("ori_txn_idf");
      hCardCurr = getValue("curr_code");
      if (hCardCurr.isEmpty())
         hCardCurr = "901";
      hCurrNtAmt = getValueDouble("curr_nt_amt");
      if (eqIgno(hVdcardFlag, "D")) {
         hDebitFlag = "Y";
      } else {
         hDebitFlag = "N";
      }
      hAuthSource = "R";
      //--tx_date = busi_date 表示當日完成之預授權交易不再送沖正給 CardLink
      if (hTxDate.equals(hBusiDate))
         continue;

      hBankCountryNum = getBankCountryNum(hBankCountry);
      writeText();
      if (checkCardNo(hCardNo) == false) continue;

      // --update cca_card_acct
      if (empty(hLastCard)) {
         hLastCard = hCardNo;
         hLastDebit = hDebitFlag;
         hLastCorpPSeqno = hCorpPSeqno;
         hLastAcctType = hAcctType;
         hCorpAcnoPSeqno = "";
         hAcnoPSeqno = "";
         hSumAmt = 0.0;
         hSumCash = 0.0;
         hCardAcctIdx = 0.0;
         hCorpCardAcctIdx = 0.0;
      }

      if (eqIgno(hLastCard, hCardNo)) {
         // --此段專門處理預先授權交易完成後要沖正原先預先授權交易所以全部為還額
         if (hIsoRespCode.equals("00")) {
            if (commString.ssIn(hTransCode, "CA|CO|AC|MA")) hSumCash -= hNtAmt;
            hSumAmt -= hNtAmt;
         }
      } else {
         //--update 上一筆的 cca_consume
         hAcnoPSeqno = selectActAcno(hLastCard, hLastDebit);
         hCardAcctIdx = selectCardAcctIdx(hAcnoPSeqno, hLastDebit);
         updateCcaConsume(hCardAcctIdx);
         //--公司戶要 Update cca_consume
         if (hLastCorpPSeqno.isEmpty() == false) {
            hCorpAcnoPSeqno = selectActAcnoForCorp(hLastCorpPSeqno, hLastAcctType);
            hCorpCardAcctIdx = selectCardAcctIdx(hCorpAcnoPSeqno, hLastDebit);
            updateCcaConsume(hCorpCardAcctIdx);
         }
         //--逐筆commit
         sqlCommit();
         // --update 後記錄現在這筆資料
         hLastCard = hCardNo;
         hLastDebit = hDebitFlag;
         hLastCorpPSeqno = hCorpPSeqno;
         hLastAcctType = hAcctType;
         hAcnoPSeqno = "";
         hCorpAcnoPSeqno = "";
         if (hIsoRespCode.equals("00") == false) {
            hSumCash = 0.0;
            hSumAmt = 0.0;
         } else {
            hSumCash = 0.0;
            hSumAmt = 0.0;
            if (commString.ssIn(hTransCode, "CA|CO|AC")) hSumCash -= hNtAmt;
            hSumAmt -= hNtAmt;
         }
      }

   }

   closeCursor();
   closeOutputText(ilFileNum);

   if (hLastCard.isEmpty()) return;
   //--補做最後一筆
   hAcnoPSeqno = selectActAcno(hLastCard, hLastDebit);
   hCardAcctIdx = selectCardAcctIdx(hAcnoPSeqno, hLastDebit);
   updateCcaConsume(hCardAcctIdx);
   if (hLastCorpPSeqno.isEmpty() == false) {
      hCorpAcnoPSeqno = selectActAcnoForCorp(hLastCorpPSeqno, hLastAcctType);
      hCorpCardAcctIdx = selectCardAcctIdx(hCorpAcnoPSeqno, hLastDebit);
      updateCcaConsume(hCorpCardAcctIdx);
   }
   //--最後一筆 commit
   sqlCommit();

}

void updateSysParm() throws Exception {
   daoTable = "ptr_sys_parm";
   updateSQL = "  wf_desc = ? ";
   whereStr = " where wf_parm = 'SYSPARM' and wf_key = 'TXLOGAMT_DATE' ";
   setString(1, hBusiDate);

   int updateCnt = updateTable();

   if (updateCnt == 0) {
      errmsg("update ptr_sys_parm error, kk=[TXLOGAMT_DATE]");
      errExit(1);
   }
}

boolean checkCardNo(String lsCardNo) throws Exception {

   String sql1 = "select count(*) as dbCnt from cca_card_base where 1=1 and card_no = ? ";

   sqlSelect(sql1, new Object[]{lsCardNo});

   if (sqlNrow > 0 && colNum("dbCnt") > 0) return true;

   return false;
}

boolean selectOriTxn() throws Exception {
   //--reversal_flag = 'Y' 才有比對到
   if (hReversalFlag.equals("Y") == false) return false;
   if (hOriAuthSeqno.isEmpty()) return false;

   //--不含沖正當日授權交易 , 若當日授權交易有沖正時則在原交易的更正日期時間有值 , 沖正交易不寫入只寫入原交易
   //--含沖正非當日授權交易 , 資料內容同原交易僅更正日期時間不同 , 授權來源碼為 R

   String sql1 = "";
   sql1 = " select ";
   sql1 += " A.card_no , A.user_expire_date , A.tx_date , A.tx_time , ";
   sql1 += " to_char(A.tx_datetime,'yyyymmdd') as tx_gmt_date , to_char(A.tx_datetime,'hh24miss') as tx_gmt_time , ";
   sql1 += " decode(A.chg_date,'','00000000',A.chg_date) as chg_date , decode(A.chg_time,'','000000',A.chg_time) as chg_time ,";
   sql1 += " A.nt_amt , A.ori_amt , A.tx_currency , A.bank_country , A.stand_in , A.mcht_no , A.mcc_code , ";
   sql1 += " A.auth_status_code , A.auth_no , A.trace_no , A.ref_no , A.pos_mode , A.auth_source , A.ori_auth_seqno , ";
   sql1 += " A.txn_idf , A.v_card_no , A.iso_resp_code , A.auth_type , A.vdcard_flag , A.proc_code , A.trans_code , A.trans_type ,";
   sql1 += " A.curr_nt_amt , B.curr_code ";
   sql1 += " from cca_auth_txlog A left join crd_card B on A.card_no = B.card_no where 1=1 and A.auth_seqno = ? ";

   sqlSelect(sql1, new Object[]{hOriAuthSeqno});

   if (sqlNrow <= 0) return false;

   if (colEq("tx_date", hTxDate)) return false;

   hCardNo = colSs("card_no");
   hUserExpireDate = colSs("user_expire_date");
   hTxDate = colSs("tx_date");
   hTxTime = colSs("tx_time");
   hTxGmtDate = colSs("tx_gmt_date");
   hTxGmtTime = colSs("tx_gmt_time");
   hChgDate = colSs("chg_date");
   hChgTime = colSs("chg_time");
   hNtAmt = colNum("nt_amt");
   hOriAmt = colNum("ori_amt");
   hTxCurrency = colSs("tx_currency");
   hBankCountry = colSs("bank_country");
   hStandIn = colSs("stand_in");
   hMchtNo = colSs("mcht_no");
   hMccCode = colSs("mcc_code");
   hAuthStatusCode = colSs("auth_status_code");
   hIsoRespCode = colSs("iso_resp_code");
   hAuthNo = colSs("auth_no");
   hTraceNo = colSs("trace_no");
   hRefNo = colSs("ref_no");
   hPoMode = colSs("pos_mode");
   hAuthSource = "R";
   hTxnIdf = colSs("txn_idf");
   hVCardNo = colSs("v_card_no");
   hAuthType = colSs("auth_type");
   hVdcardFlag = colSs("vdcard_flag");
   hProcCode = colSs("proc_code");
   hTransCode = colSs("trans_code");
   hTransType = colSs("trans_type");
   hOriAuthSeqno = colSs("ori_auth_seqno");
   hCardCurr = colNvl("curr_code", "901");
   hCurrNtAmt = colNum("curr_nt_amt");
   ibReversal = true;

   return true;
}

Double selectCardAcctIdx(String AcnoPSeqno, String lsDebitFlag) throws Exception {

   String sql1 = "select card_acct_idx from cca_card_acct where acno_p_seqno = ? ";
   if (eqIgno(lsDebitFlag, "Y")) {
      sql1 += " and debit_flag ='Y' ";
   } else {
      sql1 += " and debit_flag <>'Y' ";
   }
   sqlSelect(sql1, new Object[]{AcnoPSeqno});
   if (sqlNrow > 0) return colNum("card_acct_idx");

   return 0.0;
}

String getBankCountryNum(String BankCode) throws Exception {
   if (BankCode.isEmpty())
      return "";
   String sql1 = "select country_no from cca_country where ? in (country_code,bin_country) ";
   sqlSelect(sql1, new Object[]{BankCode});

   if (sqlNrow > 0) {
      return colSs("country_no");
   }

   return BankCode;
}

String selectActAcno(String lsCardNo, String lsDebitFlag) throws Exception {
   String sql1 = "";

   if (eqIgno(lsDebitFlag, "Y")) {
      sql1 = "select p_seqno as acno_p_seqno from dbc_card where card_no = ? ";
   } else {
      sql1 = "select acno_p_seqno from crd_card where card_no = ? ";
   }

   sqlSelect(sql1, new Object[]{lsCardNo});

   if (sqlNrow > 0) {
      return colSs("acno_p_seqno");
   }

   return "";
}

String selectActAcnoForCorp(String lsCorpPseqno, String lsAcctType) throws Exception {
   String sql1 = "";
   sql1 = "select acno_p_seqno as corp_acno_p_seqno from act_acno where corp_p_seqno = ? and acct_type = ? and acno_flag = '2' ";
   sqlSelect(sql1, new Object[]{lsCorpPseqno, lsAcctType});
   if (sqlNrow > 0) {
      return colSs("corp_acno_p_seqno");
   }
   return "";
}

void resetAmt1Cash1() throws Exception {
   daoTable = "cca_consume";
   updateSQL += " auth_txlog_amt_1 = 0 , ";
   updateSQL += " auth_txlog_amt_2 = auth_txlog_amt_1 , ";
   updateSQL += " auth_txlog_amt_cash_1  = 0 , ";
   updateSQL += " auth_txlog_amt_cash_2  = auth_txlog_amt_cash_1 , ";
   updateSQL += " mod_user = 'SYSTEM' , mod_time = sysdate , mod_pgm = 'CcaP010' , mod_seqno = nvl(mod_seqno,0)+1 ";
   whereStr = " where 1=1 and (auth_txlog_amt_1 <> 0 or auth_txlog_amt_cash_1 <> 0 ";
   whereStr += " or auth_txlog_amt_2 <> 0 or auth_txlog_amt_cash_2 <> 0 ) ";
   int updateCnt = updateTable();

   if (updateCnt < 0) {
      showLogMessage("I", "", "update all cca_consume error");
//			errmsg("update cca_consume error");
//			errExit(1);
   }
}

void updateCcaConsume(Double cardAcctIdx) throws Exception {

//		if(checkConsume(cardAcctIdx) == false)
//			return ;

   daoTable = "cca_consume";
   updateSQL += " auth_txlog_amt_1 = auth_txlog_amt_1 + ? , ";
   updateSQL += " auth_txlog_amt_cash_1  = auth_txlog_amt_cash_1 + ? , ";
   updateSQL += " mod_user = 'SYSTEM' , mod_time = sysdate , mod_pgm = 'CcaP010' , mod_seqno = nvl(mod_seqno,0)+1 ";
   whereStr = " where 1=1 and card_acct_idx = ? ";
   setDouble(1, hSumAmt);
   setDouble(2, hSumCash);
   setDouble(3, cardAcctIdx);

   int updateCnt = updateTable();

   if (updateCnt == 0) {
      showLogMessage("I", "", "card_acct_idx = ["+cardAcctIdx+"] 不存在 cca_consume");
//			errmsg("update cca_consume error, kk=[%s]", hCardAcctIdx);
//			errExit(1);
   }

}

void writeText() throws Exception {
   StringBuffer tempBuf = new StringBuffer();
   String tempStr = "", newLine = "\r\n";
   tempBuf.append(comc.fixLeft(hCardNo, 19)); // --含預留銀聯卡卡號長度 共19
   if (hUserExpireDate.length() == 4) { // --目前資料庫此欄位有 4 碼 和 6 碼 , 檔案需要 MMYY
      tempBuf.append(comc.fixLeft(commString.bbMid(hUserExpireDate, 2, 2), 2));
      tempBuf.append(comc.fixLeft(commString.bbMid(hUserExpireDate, 0, 2), 2));
//      tempBuf.append(comc.fixLeft(hUserExpireDate,4));
   } else if (hUserExpireDate.length() == 6) {
      tempBuf.append(comc.fixLeft(commString.bbMid(hUserExpireDate, 4, 2), 2));
      tempBuf.append(comc.fixLeft(commString.bbMid(hUserExpireDate, 2, 2), 2));
//      tempBuf.append(comc.fixLeft(commString.bbMid(hUserExpireDate, 2, 4),4));
   } else {
      tempBuf.append(comc.fixLeft(" ", 4));
   }
   tempBuf.append(comc.fixLeft(hTxDate, 8));
   tempBuf.append(comc.fixLeft(hTxTime, 6));
   tempBuf.append(comc.fixLeft(commString.bbMid(hTxGmtDate, 2, 6), 6));
   tempBuf.append(comc.fixLeft(hTxGmtTime, 6));
   //--雙幣卡
   if ("901".equals(hCardCurr) == false && "901".equals(hTxCurrency) == false) {
      tempStr = double2String(hCurrNtAmt * 100);
      tempBuf.append(comc.fixLeft(commString.lpad(tempStr, 12, "0"), 12));
   } else {
      tempStr = double2String(hNtAmt * 100);
      tempBuf.append(comc.fixLeft(commString.lpad(tempStr, 12, "0"), 12));
   }


   // tempBuf.append(commString.bb_fixlen(tempStr, 10));
   tempStr = "";
   tempStr = double2String(hOriAmt * 100);
   tempBuf.append(comc.fixLeft(commString.lpad(tempStr, 12, "0"), 12));
   // tempBuf.append(commString.bb_fixlen(tempStr, 10));
   tempStr = "";
   tempBuf.append(comc.fixLeft(hTxCurrency, 3));
   tempBuf.append(comc.fixLeft(hBankCountryNum, 3));
   tempBuf.append(comc.fixLeft(hStandIn, 11));
   tempBuf.append(comc.fixLeft(hMchtNo, 15));
   tempBuf.append(comc.fixLeft(hMccCode, 4));
   tempBuf.append(comc.fixLeft("00"+hIsoRespCode, 4));
//    tempBuf.append(comc.fixLeft(hIsoRespCode + hAuthStatusCode, 4));
   tempBuf.append(comc.fixLeft(hAuthNo, 6));
   tempBuf.append(comc.fixLeft(hTraceNo, 6));
   tempBuf.append(comc.fixLeft(hRefNo, 12));
   if (hAuthType.equals("Z")) {
      // --Auth_type :Z 為人工授權交易
      tempBuf.append(comc.fixLeft("Y", 1));
   } else {
      tempBuf.append(comc.fixLeft(" ", 1));
   }
//		tempBuf.append(comc.fixLeft(hChgDate, 4));
   tempBuf.append(comc.fixLeft(commString.bbMid(hChgDate, 4, 4), 4));
   tempBuf.append(comc.fixLeft(hChgTime, 6));
   tempBuf.append(comc.fixLeft(hPoMode, 2));
   tempBuf.append(comc.fixLeft(hAuthSource, 1));
   tempBuf.append(comc.fixLeft(hTxnIdf, 15)); // --交易識別碼 , 此欄位待確認
   tempBuf.append(comc.fixLeft(hVCardNo, 19)); // --對應卡片號碼 , 此欄位待確認
   tempBuf.append(comc.fixLeft(" ", 88)); // --保留欄位
   tempBuf.append(comc.fixLeft(hOriTxnIdf, 15)); //--原始交易識別碼
   tempBuf.append(comc.fixLeft(hVCardNo, 19));
   tempBuf.append(comc.fixLeft(" ", 87)); // --保留欄位
   tempBuf.append(newLine);
   if (!writeTextFile(ilFileNum, tempBuf.toString())) {
      printf("寫入檔案失敗 !");
      errExit(1);
   }
}

void initData() {
   hCardNo = "";
   hUserExpireDate = "";
   hTxDate = "";
   hTxTime = "";
   hTxGmtDate = "";
   hTxGmtTime = "";
   hNtAmt = 0.0;
   hOriAmt = 0.0;
   hTxCurrency = "";
   hBankCountry = "";
   hStandIn = "";
   hMchtNo = "";
   hMccCode = "";
   hAuthStatusCode = "";
   hAuthNo = "";
   hTraceNo = "";
   hRefNo = "";
   hPoMode = "";
   hAuthSource = "";
   hTxnIdf = "";
   hVCardNo = "";
   hIsoRespCode = "";
   hChgDate = "";
   hChgTime = "";
   hAuthType = "";
   hDebitFlag = "";
   hVdcardFlag = "";
   hProcCode = "";
   hBankCountryNum = "";
   hCardAcctIdx = 0.0;
   hTransCode = "";
   hTransType = "";
   hOriAuthSeqno = "";
   hReversalFlag = "";
   ibReversal = false;
   hCorpPSeqno = "";
   hAcctType = "";
   hCardCurr = "";
   hCurrNtAmt = 0.0;
}

String double2String(Double ldAmount) {

   Double doubleObj = new Double(ldAmount);
   NumberFormat nf = NumberFormat.getInstance();
   nf.setGroupingUsed(false);
   String doubleString = nf.format(doubleObj);

   return doubleString;
}

void procFTP() throws Exception {
   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEriaLocalDir = String.format("%s/media/cca", comc.getECSHOME());
   commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEflgModPgm = javaProgram;

   // System.setProperty("user.dir",commFTP.h_eria_local_dir);
   showLogMessage("I", "", "mput "+fileName+" 開始傳送....");
   int errCode = commFTP.ftplogName("NCR2TCB", "mput "+fileName);

   if (errCode != 0) {
      showLogMessage("I", "", "ERROR:無法傳送 "+fileName+" 資料"+" errcode:"+errCode);
      insertEcsNotifyLog(fileName);
   }
}

public int insertEcsNotifyLog(String fileName) throws Exception {
   setValue("crt_date", sysDate);
   setValue("crt_time", sysTime);
   setValue("unit_code", comr.getObjectOwner("3", javaProgram));
   setValue("obj_type", "3");
   setValue("notify_head", "無法 FTP 傳送 "+fileName+" 資料");
   setValue("notify_name", "媒體檔名:"+fileName);
   setValue("notify_desc1", "程式 "+javaProgram+" 無法 FTP 傳送 "+fileName+" 資料");
   setValue("notify_desc2", "");
   setValue("trans_seqno", commFTP.hEflgTransSeqno);
   setValue("mod_time", sysDate+sysTime);
   setValue("mod_pgm", javaProgram);
   daoTable = "ecs_notify_log";

   insertTable();

   return (0);
}

boolean checkConsume(Double checkCardAcctIdx) throws Exception {

   String sql1 = "select count(*) as db_cnt from cca_consume where card_acct_idx = ? ";
   sqlSelect(sql1, new Object[]{checkCardAcctIdx});
   if (colNum("db_cnt") <= 0) {
      showLogMessage("I", "", "card_acct_idx = ["+checkCardAcctIdx+"] 不存在 cca_consume");
      return false;
   }

   return true;
}

void initData2() {
   hLastCard = "";
   hLastDebit = "";
   hLastCorpPSeqno = "";
   hLastAcctType = "";
   hCorpAcnoPSeqno = "";
   hAcnoPSeqno = "";
   hSumAmt = 0.0;
   hSumCash = 0.0;
   hCardAcctIdx = 0.0;
   hCorpCardAcctIdx = 0.0;
}

}
