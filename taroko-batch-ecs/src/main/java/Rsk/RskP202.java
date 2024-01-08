/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112-06-06  V1.00.01  Alex        新增他行強停處理                                                                             *
 *  112-06-08  V1.00.02  Alex        新增支票拒往處理                                                                             *
 *  112-06-13  V1.00.03  Alex        新增轉催強停處理                                                                             *
 *  112-08-24  V1.00.04  Alex        商務卡連動停用/暫停負責人的一般卡
 *  112/09/09 V1.00.05   jh          ++id_no, corp_no
 *  112/11/02 V1.00.06   Wilson      add insert crd_jcic                      *
 *  113/01/03 V1.00.07   JH          ppStmtCrt.id重複
 *****************************************************************************/
package Rsk;

import Cca.CcaOutGoing;
import com.BaseBatch;
import com.CommCrdRoutine;

public class RskP202 extends BaseBatch {
private final String progname = "風管不良紀錄處理 113/01/03 V1.00.07";
Hhdata hh = new Hhdata();
SmsData sms = new SmsData();
CcaOutGoing ccaOutGoing = null;
CommCrdRoutine comcr = null;

String prgmId = "RskP202";
String pgmName = "RskP202";

String noProcReasonRefund = "";
String crtDate = "";
String fromType = "";
String idNo = "";
String idPSeqno = "";
String corpNo = "";
String corpPSeqno = "";
String majorIdPSeqno = "";
String chiName = "";
String hasSupFlag = "";
String blockReason4 = "";
String specStatus = "";
String annouType = "";
String rowid = "";
String chargeId = "";
boolean writeLog = true;

public static void main(String[] args) {
   RskP202 proc = new RskP202();
   // proc.debug = true;
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : RskP202 [business_date]");
      okExit(0);
   }

   if (liArg == 1) {
      this.setBusiDate(args[0]);
   }
   dbConnect();

   ccaOutGoing = new CcaOutGoing(getDBconnect(), getDBalias());
   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   //--取得退票不覆蓋凍結碼參數
   selectRefundParm();

   procData();

   sqlCommit();
   endProgram();
}

void selectRefundParm() throws Exception {

   String sql1 = "select wf_id from ptr_sys_idtab where wf_type ='REFUND' ";
   sqlSelect(sql1);

   if (sqlNrow <= 0)
      return;

   for (int ii = 0; ii < sqlNrow; ii++) {
      if (ii == 0)
         noProcReasonRefund = colSs(ii, "wf_id");
      else
         noProcReasonRefund += ","+colSs(ii, "wf_id");
   }

}

void procData() throws Exception {

   sqlCmd = " select crt_date, from_type, annou_type "
       +", id_no , id_p_seqno , corp_no , corp_p_seqno "
       +", major_id_p_seqno , chi_name "
       +", has_sup_flag , block_reason4 , spec_status "
       +", hex(rowid) as rowid "
       +", decode(corp_p_seqno ,'','' "
       +", (select charge_id from crd_corp where corp_p_seqno = rsk_bad_annou.corp_p_seqno)"
       +" ) as charge_id "
       +" from rsk_bad_annou "
       +" where proc_flag in ('','N') ";

   openCursor();

   while (fetchTable()) {
      totalCnt++;
      initData();

      crtDate = colSs("crt_date");
      fromType = colSs("from_type");
      idNo = colSs("id_no");
      idPSeqno = colSs("id_p_seqno");
      corpNo = colSs("corp_no");
      corpPSeqno = colSs("corp_p_seqno");
      majorIdPSeqno = colSs("major_id_p_seqno");
      chiName = colSs("chi_name");
      hasSupFlag = colSs("has_sup_flag");
      blockReason4 = colSs("block_reason4");
      specStatus = colSs("spec_status");
      rowid = colSs("rowid");
      annouType = colSs("annou_type");
      chargeId = colSs("charge_id");

      if ("6".equals(annouType)) {
         //--退票個人
         if (empty(idPSeqno) == false) {
            procType6Person();
         }

         //--退票公司
         if (empty(corpPSeqno) == false) {
            procType6Corp();
            if (writeLog) {
               insertRskBadAnnouLogCorp();
            }
            procCharge6();
         }
      } else if ("1".equals(annouType)) {
         //--同業強停
         procType1Person();

      } else if ("2".equals(annouType)) {
         //--支票拒往
         if (empty(idPSeqno) == false) {
            procType2Person();
         }

         //--支票拒往公司
         if (empty(corpPSeqno) == false) {
            procType2Corp();
            if (writeLog) {
               insertRskBadAnnouLogCorp();
            }
            procCharge2();
         }

      } else if ("9".equals(annouType)) {
         procType9Person();
      }

      //--處理簡訊
      procSmsSend();

      updateRskBadAnnou();
   }

   closeCursor();
}
//----------
void procSmsSend() throws Exception {
   if ("1".equals(annouType) == false && "6".equals(annouType) == false)
      return;

   if (!empty(chargeId)) {
      //--送簡訊給公司負責人
      sms.smsIdNo = chargeId;
   } else if (!empty(idNo) == false) {
      //--送簡訊給持卡人
      sms.smsIdNo = idNo;
   } else {
      return;
   }

   //--取得電話
   getCellarPhone();

   //--無法取得手機號碼則不發送
   if (empty(sms.smsCellarPhone))
      return;

   if ("1".equals(annouType))
      sms.smsMsgPgm = "RSKR0550-1";
   else if ("6".equals(annouType))
      sms.smsMsgPgm = "RSKR0550-6";

   //--取得簡訊相關資訊
   getSmsId();

   //--簡訊未設定不發送
   if (empty(sms.smsMsgId))
      return;

   //--取得簡訊流水號
   getSmsSeqno();

   sms.msgDesc = sms.msgUserId+","+sms.smsMsgId+","+sms.smsCellarPhone;

   //--寫入 sms_msg_dtl
   insertSmsMsgDtl();
}

void insertSmsMsgDtl() throws Exception {
   sqlCmd="";

   daoTable = "sms_msg_dtl";
   setValue("msg_seqno", sms.msgSeqno);
   setValue("msg_dept", sms.msgDept);
   setValue("msg_userid", sms.msgUserId);
   setValue("msg_pgm", sms.smsMsgPgm);
   setValue("id_p_seqno", idPSeqno);
   setValue("id_no", sms.smsIdNo);
   setValue("msg_id", sms.smsMsgId);
   setValue("msg_desc", sms.msgDesc);
   setValue("cellar_phone", sms.smsCellarPhone);
   setValue("cellphone_check_flag", sms.cellphoneCheckFlag);
   setValue("chi_name", sms.smsChiName);
   setValue("add_mode", "B");
   setValue("send_flag", "Y");
   setValue("crt_date", sysDate);
   setValue("crt_user", "ecs");
   setValue("apr_date", sysDate);
   setValue("apr_user", "ecs");
   setValue("apr_flag", "Y");

   insertTable();
}

int tiCrdCard = -1;
int tiCrdIdno = -1;
int tiActAcno2 = -1;

void procCharge6() throws Exception {
   String tmpIdPSeqno = "";
   if (empty(chargeId))
      return;

   if (tiCrdIdno <= 0) {
      sqlCmd = "select id_p_seqno from crd_idno where id_no = ? ";
      tiCrdIdno = ppStmtCrt("tiCrdIdno", "");
   }
   setString(1, chargeId);
   sqlSelect(tiCrdIdno);
   if (sqlNrow <= 0) {
      return;
   }

   tmpIdPSeqno = colSs("id_p_seqno");
   checkCardPerson(tmpIdPSeqno);
   if (tiActAcno2 <= 0) {
      sqlCmd = " select A.acct_type , A.id_p_seqno, A.corp_p_seqno "
          +", A.acno_p_seqno, A.acno_flag , A.int_rate_mcode "
          +", B.block_reason1 , B.block_reason2 "
          +", B.block_reason3 , B.block_reason4 , B.block_reason5 , "
          +" uf_spec_status(B.spec_status,B.spec_del_date) as spec_status "
          +", B.card_acct_idx "
          +" from act_acno A join cca_card_acct B "
          +"   on A.acno_p_seqno = B.acno_p_seqno "
          +" where 1=1 and A.id_p_seqno = ? and A.stop_status <> 'Y' "
          +" and A.acct_type ='01' ";
      tiActAcno2 = ppStmtCrt("tiActAcno2", "");
   }

   setString(1, tmpIdPSeqno);

   sqlSelect(tiActAcno2);
   if (sqlNrow <= 0) {
      return;
   }

   hh.hhInitData();
   hh.acctType = colSs("acct_type");
   hh.idPSeqno = colSs("id_p_seqno");
   hh.corpPSeqno =colSs("corp_p_seqno");
   hh.acnoPSeqno = colSs("acno_p_seqno");
   hh.blockReason1 = colSs("block_reason1");
   hh.blockReason2 = colSs("block_reason2");
   hh.blockReason3 = colSs("block_reason3");
   hh.blockReason4 = colSs("block_reason4");
   hh.blockReason5 = colSs("block_reason5");
   hh.specStatus = colSs("spec_status");
   hh.cardAcctIdx = colNum("card_acct_idx");
   hh.acnoFlag =colNvl("acno_flag","1");
   hh.kindFlag = "A";
   hh.mCode = colInt("int_rate_mcode");
   isNotProcReason();
   String allBlockCode = hh.blockReason1+hh.blockReason2+hh.blockReason3+hh.blockReason4+hh.blockReason5;
   if (empty(allBlockCode))
      hh.blockFlag = "Y";

   getCardNum1();

   if (hh.lbProc) {
      //--進行凍結
      updateCcaCardAcct();
      insertRskAcnoLog();
      selectCrdCard();
   }

   if (writeLog) {
      insertRskBadAnnouLog();
   }

}

int tiCrdIdno2 = -1;
int tiActAcno4 = -1;

void procCharge2() throws Exception {
   String tmpIdPSeqno = "";
   if (empty(chargeId))
      return;

   if (tiCrdIdno2 <= 0) {
      sqlCmd = "select id_p_seqno from crd_idno where id_no = ? ";
      tiCrdIdno2 = ppStmtCrt("tiCrdIdno2", "");
   }
   setString(1, chargeId);
   sqlSelect(tiCrdIdno2);
   if (sqlNrow <= 0) {
      return;
   }

   tmpIdPSeqno = colSs("id_p_seqno");
   checkCardPerson(tmpIdPSeqno);
   if (tiActAcno4 <= 0) {
      sqlCmd = " select A.acct_type , A.id_p_seqno, A.corp_p_seqno "
          +", A.acno_p_seqno, A.acno_flag, A.int_rate_mcode "
          +", B.block_reason1 , B.block_reason2 "
          +", B.block_reason3 , B.block_reason4 , B.block_reason5 , "
          +" uf_spec_status(B.spec_status,B.spec_del_date) as spec_status "
          +", B.card_acct_idx, A.rc_use_indicator "
          +" from act_acno A join cca_card_acct B "
          +"   on A.acno_p_seqno = B.acno_p_seqno "
          +" where 1=1 and A.id_p_seqno = ? and A.stop_status <> 'Y' "
          +" and A.acct_type ='01' ";
      tiActAcno4 = ppStmtCrt("tiActAcno4", "");
   }

   setString(1, tmpIdPSeqno);

   sqlSelect(tiActAcno4);
   if (sqlNrow <= 0) {
      return;
   }

   hh.hhInitData();
   hh.acctType = colSs("acct_type");
   hh.idPSeqno = colSs("id_p_seqno");
   hh.corpPSeqno =colSs("corp_p_seqno");
   hh.acnoPSeqno = colSs("acno_p_seqno");
   hh.blockReason1 = colSs("block_reason1");
   hh.blockReason2 = colSs("block_reason2");
   hh.blockReason3 = colSs("block_reason3");
   hh.blockReason4 = colSs("block_reason4");
   hh.blockReason5 = colSs("block_reason5");
   hh.specStatus = colSs("spec_status");
   hh.cardAcctIdx = colNum("card_acct_idx");
   hh.acnoFlag =colNvl("acno_flag","1");
   hh.kindFlag = "A";
   hh.mCode = colInt("int_rate_mcode");
   hh.rcUseIndicator = colSs("rc_use_indicator");
   isNotProcReason();
   String allBlockCode = hh.blockReason1+hh.blockReason2+hh.blockReason3+hh.blockReason4+hh.blockReason5;
   if (empty(allBlockCode))
      hh.blockFlag = "Y";

   isBadLoan();
   getCardNum1();

   selectCrdCardType2();
   if (writeLog)
      insertRskBadAnnouLog();
}

int tiSmsSeqno = -1;

void getSmsSeqno() throws Exception {
   if (tiSmsSeqno <= 0) {
      sqlCmd = "select lpad(to_char(ecs_modseq.nextval),10,'0') as sms_seqno from dual ";
      tiSmsSeqno = ppStmtCrt("tiSmsSeqno", "");
   }

   sqlSelect(tiSmsSeqno);
   if (sqlNrow <= 0) {
      return;
   }

   sms.msgSeqno = colSs("sms_seqno");
}

int tiSmsId = -1;

void getSmsId() throws Exception {
   if (tiSmsId <= 0) {
      sqlCmd = "select msg_dept , msg_userid , msg_id from sms_msg_id where msg_pgm = ? ";
      tiSmsId = ppStmtCrt("tiSmsId", "");
   }

   setString(1, sms.smsMsgPgm);

   sqlSelect(tiSmsId);
   if (sqlNrow <= 0) {
      return;
   }

   sms.msgDept = colSs("msg_dept");
   sms.msgUserId = colSs("msg_userid");
   sms.smsMsgId = colSs("msg_id");
}


int tiPhone = -1;

void getCellarPhone() throws Exception {
   if (tiPhone <= 0) {
      sqlCmd = "select cellar_phone , chi_name from crd_idno where id_no = ? union select cellar_phone , chi_name from dbc_idno where id_no = ? ";
      ;
      tiPhone = ppStmtCrt("tiPhone", "");
   }

   setString(1, sms.smsIdNo);
   setString(2, sms.smsIdNo);

   sqlSelect(tiPhone);
   if (sqlNrow <= 0) {
      return;
   }

   sms.smsCellarPhone = colSs("cellar_phone");
   sms.smsChiName = colSs("chi_name");

   if (sms.smsCellarPhone.length() == 10 && colNum("cellar_phone") > 0)
      sms.cellphoneCheckFlag = "Y";
   else
      sms.cellphoneCheckFlag = "N";
}

void updateRskBadAnnou() throws Exception {
   sqlCmd="";

   daoTable = "rsk_bad_annou";
   updateSQL = "proc_flag ='Y' , proc_date = ? , mod_pgm = ? , mod_user = ? , mod_time = sysdate , mod_seqno = nvl(mod_seqno,0)+1 ";
   whereStr = " where rowid = ? ";
   setString(1, hBusiDate);
   setString(2, "RskP202");
   setString(3, "ecs");
   setRowId(4, rowid);
   updateTable();
}

int tiAcnoCorp = -1;

void procType6Corp() throws Exception {
   checkCardCorp();
   if (tiAcnoCorp <= 0) {
      sqlCmd = " select A.acno_flag , A.acct_type "
          +", A.id_p_seqno ,A.acno_p_seqno "
          +", B.card_acct_idx "
          +", B.block_reason1, B.block_reason2, B.block_reason3 "
          +", B.block_reason4, B.block_reason5 "
          +", uf_spec_status(B.spec_status,B.spec_del_date) as spec_status "
          +" from act_acno A join cca_card_acct B "
          +"   on A.acno_p_seqno = B.acno_p_seqno "
          +" where A.corp_p_seqno = ? "
          +" and A.stop_status <> 'Y' "
          +" and A.acct_type in ('03','06') "
      ;
      tiAcnoCorp = ppStmtCrt("tiAcnoCorp", "");
   }

   setString(1, corpPSeqno);

   sqlSelect(tiAcnoCorp);
   if (sqlNrow <= 0) {
      return;
   }

   int corpCnt = sqlNrow;

   for (int ii = 0; ii < corpCnt; ii++) {
      hh.hhInitData();
      hh.acctType = colSs(ii, "acct_type");
      hh.idPSeqno = colSs(ii, "id_p_seqno");
      hh.acnoPSeqno = colSs(ii, "acno_p_seqno");
      hh.blockReason1 = colSs(ii, "block_reason1");
      hh.blockReason2 = colSs(ii, "block_reason2");
      hh.blockReason3 = colSs(ii, "block_reason3");
      hh.blockReason4 = colSs(ii, "block_reason4");
      hh.blockReason5 = colSs(ii, "block_reason5");
      hh.specStatus = colSs(ii, "spec_status");
      hh.cardAcctIdx = colNum(ii, "card_acct_idx");
      hh.kindFlag = "A";
      hh.acnoFlag = colSs(ii, "acno_flag");
      hh.corpPSeqno = corpPSeqno;
      isNotProcReason();
      getCorpCardNo();
      String allBlockCode = hh.blockReason1+hh.blockReason2+hh.blockReason3+hh.blockReason4+hh.blockReason5;
      if (empty(allBlockCode))
         hh.blockFlag = "Y";

      if ("2".equals(hh.acnoFlag))
         getCardNum3();
      else
         getCardNum2();

      if (hh.lbProc) {
         //--進行凍結
         updateCcaCardAcct();
         insertRskAcnoLog();
         selectCrdCard();
      }

//			insertRskBadAnnouLog();
   }
}

int tiAcnoCorp2 = -1;

void procType2Corp() throws Exception {
   checkCardCorp();
   if (tiAcnoCorp2 <= 0) {
      sqlCmd = " select A.acno_flag , A.acct_type , A.id_p_seqno ,A.acno_p_seqno , B.block_reason1 , B.block_reason2 , B.block_reason3 , B.block_reason4 , B.block_reason5 , "
          +" uf_spec_status(B.spec_status,B.spec_del_date) as spec_status , B.card_acct_idx, A.rc_use_indicator "
          +" from act_acno A join cca_card_acct B on A.acno_p_seqno = B.acno_p_seqno "
          +" where 1=1 and A.corp_p_seqno = ? and A.stop_status <> 'Y' "
          +" and A.acct_type in ('03','06') "
//		    	   + " and A.acno_flag not in ('2','Y') "
      ;
      tiAcnoCorp2 = ppStmtCrt("tiAcnoCorp2", "");
   }

   setString(1, corpPSeqno);

   sqlSelect(tiAcnoCorp2);
   if (sqlNrow <= 0) {
      return;
   }

   int corpCnt = sqlNrow;

   for (int ii = 0; ii < corpCnt; ii++) {
      hh.hhInitData();
      hh.acctType = colSs(ii, "acct_type");
      hh.idPSeqno = colSs(ii, "id_p_seqno");
      hh.acnoPSeqno = colSs(ii, "acno_p_seqno");
      hh.blockReason1 = colSs(ii, "block_reason1");
      hh.blockReason2 = colSs(ii, "block_reason2");
      hh.blockReason3 = colSs(ii, "block_reason3");
      hh.blockReason4 = colSs(ii, "block_reason4");
      hh.blockReason5 = colSs(ii, "block_reason5");
      hh.specStatus = colSs(ii, "spec_status");
      hh.cardAcctIdx = colNum(ii, "card_acct_idx");
      hh.kindFlag = "A";
      hh.acnoFlag = colSs(ii, "acno_flag");
      hh.corpPSeqno = corpPSeqno;
      hh.rcUseIndicator = colSs(ii, "rc_use_indicator");
      isNotProcReason();
      getCorpCardNo();
      String allBlockCode = hh.blockReason1+hh.blockReason2+hh.blockReason3+hh.blockReason4+hh.blockReason5;
      if (empty(allBlockCode))
         hh.blockFlag = "Y";

      if ("2".equals(hh.acnoFlag))
         getCardNum3();
      else
         getCardNum2();

      selectCrdCardType2Corp();

//			//--商務卡無有效卡帳戶卡不寫進rsk_bad_annoulog			
//			if(selectCrdCardType2Corp() == false && "2".equals(hh.acnoFlag) == false)
//				continue ;
//			insertRskBadAnnouLog();
   }
}

void getCorpCardNo() throws Exception {
   String sql1 = "select card_no from crd_card where acno_p_seqno = ? and current_code ='0' ";
   setString(1, hh.acnoPSeqno);
   sqlSelect(sql1);
   if (sqlNrow > 0) {
      hh.cardNo = colSs("card_no");
      return;
   }


   String sql2 = "select card_no from crd_card where acno_p_seqno = ? order by oppost_date Desc "+commSqlStr.rownum(1);
   setString(1, hh.acnoPSeqno);
   sqlSelect(sql2);
   if (sqlNrow > 0)
      hh.cardNo = colSs("card_no");
}

int tiAcno = -1;

void procType6Person() throws Exception {
   checkCardPerson(idPSeqno);
   if (tiAcno <= 0) {
      sqlCmd = " select A.acct_type , A.id_p_seqno, A.corp_p_seqno "
          +", A.acno_p_seqno, A.acno_flag "
          +", A.int_rate_mcode , B.block_reason1 , B.block_reason2 "
          +", B.block_reason3 , B.block_reason4 , B.block_reason5 , "
          +" uf_spec_status(B.spec_status,B.spec_del_date) as spec_status "
          +", B.card_acct_idx "
          +" from act_acno A join cca_card_acct B "
          +"   on A.acno_p_seqno = B.acno_p_seqno "
          +" where 1=1 and A.id_p_seqno = ? and A.stop_status <> 'Y' "
          +" and A.acct_type ='01' ";
      tiAcno = ppStmtCrt("tiAcno", "");
   }

   setString(1, idPSeqno);

   sqlSelect(tiAcno);
   if (sqlNrow <= 0) {
      return;
   }

   hh.hhInitData();
   hh.acctType = colSs("acct_type");
   hh.idPSeqno = colSs("id_p_seqno");
   hh.corpPSeqno =colSs("corp_p_seqno");
   hh.acnoPSeqno = colSs("acno_p_seqno");
   hh.blockReason1 = colSs("block_reason1");
   hh.blockReason2 = colSs("block_reason2");
   hh.blockReason3 = colSs("block_reason3");
   hh.blockReason4 = colSs("block_reason4");
   hh.blockReason5 = colSs("block_reason5");
   hh.specStatus = colSs("spec_status");
   hh.cardAcctIdx = colNum("card_acct_idx");
   hh.acnoFlag =colNvl("acno_flag","1");
   hh.kindFlag = "A";
   hh.mCode = colInt("int_rate_mcode");
   isNotProcReason();
   String allBlockCode = hh.blockReason1+hh.blockReason2+hh.blockReason3+hh.blockReason4+hh.blockReason5;
   if (empty(allBlockCode))
      hh.blockFlag = "Y";

   getCardNum1();

   if (hh.lbProc) {
      //--進行凍結
      updateCcaCardAcct();
      insertRskAcnoLog();
      selectCrdCard();
   }
   isBadLoan();
   if (writeLog)
      insertRskBadAnnouLog();
}

int tiCard = -1;

void procType1Person() throws Exception {
   checkCardPerson(idPSeqno);
   if (tiCard <= 0) {
      sqlCmd = " select A.acct_type , A.id_p_seqno, A.corp_p_seqno "
          +", A.acno_p_seqno, A.acno_flag "
          +", A.int_rate_mcode , B.block_reason1 , B.block_reason2 "
          +", B.block_reason3 , B.block_reason4 , B.block_reason5 , "
          +" uf_spec_status(B.spec_status,B.spec_del_date) as spec_status "
          +", B.card_acct_idx, A.rc_use_indicator "
          +" from act_acno A join cca_card_acct B "
          +"   on A.acno_p_seqno = B.acno_p_seqno "
          +" where 1=1 and A.id_p_seqno = ? and A.stop_status <> 'Y' "
          +" and A.acct_type ='01' ";
      tiCard = ppStmtCrt("tiCard", "");
   }

   setString(1, idPSeqno);

   sqlSelect(tiCard);
   if (sqlNrow <= 0) {
      return;
   }

   hh.hhInitData();
   hh.acctType = colSs("acct_type");
   hh.idPSeqno = colSs("id_p_seqno");
   hh.corpPSeqno =colSs("corp_p_seqno");
   hh.acnoPSeqno = colSs("acno_p_seqno");
   hh.blockReason1 = colSs("block_reason1");
   hh.blockReason2 = colSs("block_reason2");
   hh.blockReason3 = colSs("block_reason3");
   hh.blockReason4 = colSs("block_reason4");
   hh.blockReason5 = colSs("block_reason5");
   hh.specStatus = colSs("spec_status");
   hh.cardAcctIdx = colNum("card_acct_idx");
   hh.acnoFlag =colNvl("acno_flag","1");
   hh.kindFlag = "A";
   hh.mCode = colInt("int_rate_mcode");
   hh.rcUseIndicator = colSs("rc_use_indicator");

   getCardNum1();

   selectCrdCardType1();

   String allBlockCode = hh.blockReason1+hh.blockReason2+hh.blockReason3+hh.blockReason4+hh.blockReason5;
   if (empty(allBlockCode))
      hh.blockFlag = "Y";
   isBadLoan();
   if (writeLog)
      insertRskBadAnnouLog();
}

int tiCard9 = -1;

void procType9Person() throws Exception {
   if (tiCard9 <= 0) {
      sqlCmd = " select A.acct_type , A.id_p_seqno, A.corp_p_seqno, A.acno_flag "
          +", A.acno_p_seqno , A.int_rate_mcode "
          +", B.block_reason1 , B.block_reason2 , B.block_reason3 "
          +", B.block_reason4 , B.block_reason5 , "
          +" uf_spec_status(B.spec_status,B.spec_del_date) as spec_status , B.card_acct_idx, A.rc_use_indicator "
          +" from act_acno A join cca_card_acct B on A.acno_p_seqno = B.acno_p_seqno where 1=1 and A.id_p_seqno = ? and A.stop_status <> 'Y' "
          +" and A.acct_type ='01' ";
      tiCard9 = ppStmtCrt("tiCard9", "");
   }

   setString(1, idPSeqno);

   sqlSelect(tiCard9);
   if (sqlNrow <= 0) {
      return;
   }

   hh.hhInitData();
   hh.acctType = colSs("acct_type");
   hh.idPSeqno = colSs("id_p_seqno");
   hh.corpPSeqno =colSs("corp_p_seqno");
   hh.acnoPSeqno = colSs("acno_p_seqno");
   hh.blockReason1 = colSs("block_reason1");
   hh.blockReason2 = colSs("block_reason2");
   hh.blockReason3 = colSs("block_reason3");
   hh.blockReason4 = colSs("block_reason4");
   hh.blockReason5 = colSs("block_reason5");
   hh.specStatus = colSs("spec_status");
   hh.cardAcctIdx = colNum("card_acct_idx");
   hh.acnoFlag =colNvl("acno_flag","1");
   hh.kindFlag = "A";
   hh.mCode = colInt("int_rate_mcode");
   hh.rcUseIndicator = colSs("rc_use_indicator");

   getCardNum1();

   selectCrdCardType9();

   String allBlockCode = hh.blockReason1+hh.blockReason2+hh.blockReason3+hh.blockReason4+hh.blockReason5;
   if (empty(allBlockCode))
      hh.blockFlag = "Y";
   isBadLoan();

   insertRskBadAnnouLog();
}

int tiAcno2 = -1;

void procType2Person() throws Exception {
   checkCardPerson(idPSeqno);
   if (tiAcno2 <= 0) {
      sqlCmd = " select A.acct_type , A.id_p_seqno , A.acno_p_seqno, A.acno_flag "
          +", A.corp_p_seqno "
          +", A.int_rate_mcode , B.block_reason1 , B.block_reason2 "
          +", B.block_reason3 , B.block_reason4 , B.block_reason5 , "
          +" uf_spec_status(B.spec_status,B.spec_del_date) as spec_status "
          +", B.card_acct_idx, A.rc_use_indicator "
          +" from act_acno A join cca_card_acct B "
          +"   on A.acno_p_seqno = B.acno_p_seqno "
          +" where 1=1 and A.id_p_seqno = ? "
          +" and A.stop_status <> 'Y' "
          +" and A.acct_type ='01' ";
      tiAcno2 = ppStmtCrt("tiAcno2", "");
   }

   setString(1, idPSeqno);

   sqlSelect(tiAcno2);
   if (sqlNrow <= 0) {
      return;
   }

   hh.hhInitData();
   hh.acctType       = colSs("acct_type");
   hh.idPSeqno       = colSs("id_p_seqno");
   hh.corpPSeqno     = colSs("corp_p_seqno");
   hh.acnoPSeqno     = colSs("acno_p_seqno");
   hh.blockReason1   = colSs("block_reason1");
   hh.blockReason2   = colSs("block_reason2");
   hh.blockReason3   = colSs("block_reason3");
   hh.blockReason4   = colSs("block_reason4");
   hh.blockReason5   = colSs("block_reason5");
   hh.specStatus     = colSs("spec_status");
   hh.cardAcctIdx    = colNum("card_acct_idx");
   hh.acnoFlag       =colNvl("acno_flag","1");
   hh.kindFlag = "A";
   hh.mCode = colInt("int_rate_mcode");
   hh.rcUseIndicator = colSs("rc_use_indicator");
   isNotProcReason();
   String allBlockCode = hh.blockReason1+hh.blockReason2+hh.blockReason3
       +hh.blockReason4+hh.blockReason5;
   if (empty(allBlockCode))
      hh.blockFlag = "Y";

   isBadLoan();
   getCardNum1();

   selectCrdCardType2();
   if (writeLog)
      insertRskBadAnnouLog();
}

void isNotProcReason() throws Exception {
   if (commString.ssIn(hh.blockReason1, noProcReasonRefund)) {
      hh.lbProc = false;
   }

   if (commString.ssIn(hh.blockReason2, noProcReasonRefund)) {
      hh.lbProc = false;
   }
   if (commString.ssIn(hh.blockReason3, noProcReasonRefund)) {
      hh.lbProc = false;
   }

   if (commString.ssIn(hh.blockReason4, noProcReasonRefund)) {
      hh.lbProc = false;
   }

   if (commString.ssIn(hh.blockReason5, noProcReasonRefund)) {
      hh.lbProc = false;
   }

   if ("2".equals(hh.acnoFlag) || "Y".equals(hh.acnoFlag)) {
      hh.lbProc = false;
   }
}

void isBadLoan() {

   String badLoanReason = "0N,61,62,63,64";

   if (commString.ssIn(hh.blockReason1, badLoanReason))
      hh.badLoanFlag = "Y";
   if (commString.ssIn(hh.blockReason2, badLoanReason))
      hh.badLoanFlag = "Y";
   if (commString.ssIn(hh.blockReason3, badLoanReason))
      hh.badLoanFlag = "Y";
   if (commString.ssIn(hh.blockReason4, badLoanReason))
      hh.badLoanFlag = "Y";
   if (commString.ssIn(hh.blockReason5, badLoanReason))
      hh.badLoanFlag = "Y";
}

int tiCardNum1 = -1;

void getCardNum1() throws Exception {
   if (tiCardNum1 <= 0) {
      sqlCmd = " select sum(decode(A.sup_flag,'1',1,0)) as h_sup1_card_num , "
          +" sum(decode(A.sup_flag,'1',0,decode(A.acno_flag,'Y',0,1))) as h_sup0_card_num , "
          +" sum(decode(A.sup_flag,'1',0,decode(A.acno_flag,'Y',1,0))) as h_corp_card_num "
          +" from crd_card A "
          +" where acno_p_seqno = ? "
          +" and A.current_code ='0' ";
      tiCardNum1 = ppStmtCrt("tiCardNum1", "");
   }

   setString(1, hh.acnoPSeqno);
   sqlSelect(tiCardNum1);

   if (sqlNrow <= 0)
      return;

   hh.sup1CardNum = colInt("h_sup1_card_num");
   hh.sup0CardNum = colInt("h_sup0_card_num");
   hh.corpCardNum = colInt("h_corp_card_num");

}

int tiCardNum2 = -1;

void getCardNum2() throws Exception {
   if (tiCardNum2 <= 0) {
      sqlCmd = " select sum(decode(A.sup_flag,'1',1,0)) as h_sup1_card_num , "
          +" sum(decode(A.sup_flag,'1',0,decode(A.acno_flag,'Y',0,1))) as h_sup0_card_num , "
          +" sum(decode(A.sup_flag,'1',0,decode(A.acno_flag,'Y',1,0))) as h_corp_card_num "
          +" from crd_card A "
          +" where A.corp_p_seqno = ? "
          +" and A.acno_p_seqno = ? "
          +" and A.current_code ='0' ";
      daoTable = "tiCardNum2";
      tiCardNum2 = ppStmtCrt("tiCardNum2", "");
   }

   setString(1, hh.corpPSeqno);
   setString(2, hh.acnoPSeqno);
   sqlSelect(tiCardNum2);

   if (sqlNrow <= 0)
      return;

   hh.sup1CardNum = colInt("h_sup1_card_num");
   hh.sup0CardNum = colInt("h_sup0_card_num");
   hh.corpCardNum = colInt("h_corp_card_num");

}

int tiCardNum3 = -1;

void getCardNum3() throws Exception {
   //--acno_flag ='2' 計算公司下多少卡
   if (tiCardNum3 <= 0) {
      sqlCmd = " select sum(decode(A.sup_flag,'1',1,0)) as h_sup1_card_num , "
          +" sum(decode(A.sup_flag,'1',0,decode(A.acno_flag,'Y',0,1))) as h_sup0_card_num , "
          +" sum(decode(A.sup_flag,'1',0,decode(A.acno_flag,'Y',1,0))) as h_corp_card_num "
          +" from crd_card A "
          +" where A.corp_p_seqno = ? "
          +" and A.current_code ='0' ";
      daoTable = "tiCardNum3";
      tiCardNum3 = ppStmtCrt("tiCardNum3", "");
   }

   setString(1, hh.corpPSeqno);
//		setString(2,hh.acnoPSeqno);
   sqlSelect(tiCardNum3);

   if (sqlNrow <= 0)
      return;

   hh.sup1CardNum = colInt("h_sup1_card_num");
   hh.sup0CardNum = colInt("h_sup0_card_num");
   hh.corpCardNum = colInt("h_corp_card_num");

}
//----------
com.Parm2sql ttAbadlg2=null;
void insertRskBadAnnouLogCorp() throws Exception {
   String tmpAcnoPSeqno = "", tmpAcctType = "", tmpBlockReason1 = "", tmpBlockReason2 = "";
   String tmpBlockReason3 = "", tmpBlockReason4 = "", tmpBlockReason5 = "", tmpSpecStatus = "";
   int mCode = 0, tmpSup0CardNum = 0, tmpSup1CardNum = 0, tmpCorpCardNum = 0;
   String tmpBlockFlag = "";

   String sql1 = " select A.acno_p_seqno , A.acct_type , B.block_reason1 , B.block_reason2 , "
       +" B.block_reason3 , B.block_reason4 , B.block_reason5 , A.int_rate_mcode , "
       +" B.spec_status "
       +" from act_acno A join cca_card_acct B on A.acno_p_seqno = B.acno_p_seqno "
       +" where A.corp_p_seqno = ? and A.acno_flag ='2' ";

   sqlSelect(sql1, new Object[]{corpPSeqno});
   if (sqlNrow <= 0)
      return;

   tmpAcnoPSeqno = colSs("acno_p_seqno");
   tmpAcctType = colSs("acct_type");
   tmpBlockReason1 = colSs("block_reason1");
   tmpBlockReason2 = colSs("block_reason2");
   tmpBlockReason3 = colSs("block_reason3");
   tmpBlockReason4 = colSs("block_reason4");
   tmpBlockReason5 = colSs("block_reason5");
   mCode = colInt("int_rate_mcode");
   tmpSpecStatus = colSs("spec_status");

   if (empty(tmpBlockReason1+tmpBlockReason2+tmpBlockReason3+tmpBlockReason4+tmpBlockReason5))
      tmpBlockFlag = "Y";

   String sql2 = " select sum(decode(A.sup_flag,'1',1,0)) as h_sup1_card_num , "
       +" sum(decode(A.sup_flag,'1',0,decode(A.acno_flag,'Y',0,1))) as h_sup0_card_num , "
       +" sum(decode(A.sup_flag,'1',0,decode(A.acno_flag,'Y',1,0))) as h_corp_card_num "
       +" from crd_card A "
       +" where A.corp_p_seqno = ? "
       +" and A.current_code ='0' ";

   sqlSelect(sql2, new Object[]{corpPSeqno});

   tmpSup1CardNum = colInt("h_sup1_card_num");
   tmpSup0CardNum = colInt("h_sup0_card_num");
   tmpCorpCardNum = colInt("h_corp_card_num");

   if (ttAbadlg2 ==null) {
      ttAbadlg2 =new com.Parm2sql();
      ttAbadlg2.insert("rsk_bad_annou_log");
   }
   ttAbadlg2.aaa("crt_date"                 , crtDate);    //-匯入登錄日期--
   ttAbadlg2.aaa("from_type"                , fromType);    //-來源管道--
   ttAbadlg2.aaa("annou_type"               , annouType);
   ttAbadlg2.aaa("id_p_seqno"               , "");    //-帳戶流水號碼--
   ttAbadlg2.aaa("corp_p_seqno"             , corpPSeqno);    //-統編流水號--
   ttAbadlg2.aaa("major_id_p_seqno"         , "");    //-正卡身分證流水號--
   ttAbadlg2.aaa("kind_flag"                , "A");    //-帳戶卡片--
   ttAbadlg2.aaa("acno_p_seqno"             , tmpAcnoPSeqno);    //-帳戶流水號--
   ttAbadlg2.aaa("acct_type"                , tmpAcctType);    //-帳戶帳號類別碼--
   ttAbadlg2.aaa("card_no"                  , "");    //-卡號--
   ttAbadlg2.aaa("block_reason"             , tmpBlockReason1);    //-凍結原因(欠費)--
   ttAbadlg2.aaa("block_reason2"            , tmpBlockReason2);   //-凍結原因--
   ttAbadlg2.aaa("block_reason3"            , tmpBlockReason3);    //-禁超原因3--
   ttAbadlg2.aaa("block_reason4"            , tmpBlockReason4);    //-禁超原因4--
   ttAbadlg2.aaa("block_reason5"            , tmpBlockReason5);    //-禁超原因5--
   ttAbadlg2.aaa("proc_reason"              , blockReason4);    //-執行凍結碼--
   ttAbadlg2.aaa("spec_status"              , tmpSpecStatus);    //-戶特指--
   ttAbadlg2.aaa("sup_card_flag"            , nvl(hasSupFlag, "N"));    //-因附卡凍結正卡旗標--
   ttAbadlg2.aaa("bad_loan_flag"            , "N");    //-不良債權通報旗標--
   ttAbadlg2.aaa("m_code"                   , mCode);   //-M_code--
   ttAbadlg2.aaa("block_flag"               , nvl(tmpBlockFlag, "N"));    //-凍結旗標--
   ttAbadlg2.aaa("sup0_card_num"            , tmpSup0CardNum);    //-流通正卡數--
   ttAbadlg2.aaa("sup1_card_num"            , tmpSup1CardNum);    //-流通附卡數--
   ttAbadlg2.aaa("corp_card_num"            , tmpCorpCardNum);    //-流通商務卡數--
   ttAbadlg2.aaa("proc_date"                , hBusiDate);    //-處理日期--
   ttAbadlg2.aaaDtime("mod_time");    //-異動時間--
   ttAbadlg2.aaa("mod_pgm"                  , hModPgm);    //-異動程式--
   ttAbadlg2.aaa("print_flag"               , "N");
   ttAbadlg2.aaa("id_no"                    , idNo);    //-身分證號碼--
   ttAbadlg2.aaa("corp_no"                  , corpNo);    //-法人戶統一編號--

   if (ttAbadlg2.ti <=0) {
      ttAbadlg2.ti =ppStmtCrt("ttAbadlg2", ttAbadlg2.getSql());
   }

   sqlExec(ttAbadlg2.ti, ttAbadlg2.getParms());
   if (sqlNrow <=0) {
      sqlerr("insert rsk_bad_annou_log.CORP error, kk[%s,%s]", idNo,corpNo);
      errExit(1);
   }

}
//--------
com.Parm2sql ttAbadlg=null;
void insertRskBadAnnouLog() throws Exception {
   if (ttAbadlg ==null) {
      ttAbadlg =new com.Parm2sql();
      ttAbadlg.insert("rsk_bad_annou_log");
   }

//   String ls_idPseqno=idPSeqno;
//   if (empty(ls_idPseqno)) {
//      ls_idPseqno =hh.idPSeqno;
//   }

   ttAbadlg.aaa("crt_date"                 , crtDate);    //-匯入登錄日期--
   ttAbadlg.aaa("from_type"                , fromType);    //-來源管道--
   ttAbadlg.aaa("annou_type"               , annouType);
   ttAbadlg.aaa("id_p_seqno"               , hh.idPSeqno);    //-帳戶流水號碼--
   ttAbadlg.aaa("corp_p_seqno"             , hh.corpPSeqno);    //-統編流水號--
   ttAbadlg.aaa("major_id_p_seqno"         , majorIdPSeqno);    //-正卡身分證流水號--
   ttAbadlg.aaa("kind_flag"                , hh.kindFlag);    //-帳戶卡片--
   ttAbadlg.aaa("acno_p_seqno"             , hh.acnoPSeqno);    //-帳戶流水號--
   ttAbadlg.aaa("acct_type"                , hh.acctType);   //-帳戶帳號類別碼--
   ttAbadlg.aaa("card_no"                  , hh.cardNo);    //-卡號--
   ttAbadlg.aaa("block_reason"             , hh.blockReason1);    //-凍結原因(欠費)--
   ttAbadlg.aaa("block_reason2"            , hh.blockReason2);    //-凍結原因--
   ttAbadlg.aaa("block_reason3"            , hh.blockReason3);    //-禁超原因3--
   ttAbadlg.aaa("block_reason4"            , hh.blockReason4);    //-禁超原因4--
   ttAbadlg.aaa("block_reason5"            , hh.blockReason5);    //-禁超原因5--
   ttAbadlg.aaa("proc_reason"              , blockReason4);    //-執行凍結碼--
   ttAbadlg.aaa("spec_status"              , hh.specStatus);    //-戶特指--
   ttAbadlg.aaa("sup_card_flag"            , nvl(hasSupFlag, "N"));    //-因附卡凍結正卡旗標--
   ttAbadlg.aaa("bad_loan_flag"            , nvl(hh.badLoanFlag, "N"));    //-不良債權通報旗標--
   ttAbadlg.aaa("m_code"                   , hh.mCode);    //-M_code--
   ttAbadlg.aaa("block_flag"               , nvl(hh.blockFlag, "N"));    //-凍結旗標--
   ttAbadlg.aaa("sup0_card_num"            , hh.sup0CardNum);    //-流通正卡數--
   ttAbadlg.aaa("sup1_card_num"            , hh.sup1CardNum);    //-流通附卡數--
   ttAbadlg.aaa("corp_card_num"            , hh.corpCardNum);    //-流通商務卡數--
   ttAbadlg.aaa("proc_date"                , hBusiDate);    //-處理日期--
   ttAbadlg.aaa("print_flag"               , "N");
   ttAbadlg.aaa("id_no"                    , idNo);    //-身分證號碼--
   ttAbadlg.aaa("corp_no"                  , corpNo);    //-法人戶統一編號--
   ttAbadlg.aaaDtime("mod_time");    //-異動時間--
   ttAbadlg.aaa("mod_pgm"                  , hModPgm);    //-異動程式--

   if (ttAbadlg.ti <=0) {
      ttAbadlg.ti =ppStmtCrt("ttAbadlg", ttAbadlg.getSql());
   }

   sqlExec(ttAbadlg.ti, ttAbadlg.getParms());
   if (sqlNrow <=0) {
      sqlerr("insert rsk_bad_annou_log error, kk[%s,%s]",idNo,corpNo);
      errExit(1);
   }
}

void selectCrdCard() throws Exception {

   String sql1 = "select card_no , current_code from crd_card where current_code ='0' and acno_p_seqno = ? ";
   setString(1, hh.acnoPSeqno);

   sqlSelect(sql1);

   if (sqlNrow <= 0) {
      return;
   }


   String aCardNo = "", aCurrentCode = "";
   for (int ii = 0; ii < sqlNrow; ii++) {
      aCardNo = "";
      aCurrentCode = "";
      aCardNo = colSs(ii, "card_no");
      aCurrentCode = colSs(ii, "current_code");
      updateCrdCard(aCardNo);
      ccaOutGoing.InsertCcaOutGoingBlock(aCardNo, aCurrentCode, hBusiDate, blockReason4);
   }

}

void selectCrdCardType1() throws Exception {

   String sql1 = "select card_no , current_code from crd_card where current_code ='0' and acno_p_seqno = ? ";
   setString(1, hh.acnoPSeqno);

   sqlSelect(sql1);

   if (sqlNrow <= 0)
      return;

   String aCardNo = "", aCurrentCode = "";
   for (int ii = 0; ii < sqlNrow; ii++) {
      aCardNo = "";
      aCurrentCode = "";
      aCardNo = colSs(ii, "card_no");
      aCurrentCode = "4";
      updateCrdCardType1(aCardNo);
      ccaOutGoing.InsertCcaOutGoing(aCardNo, aCurrentCode, hBusiDate, blockReason4);
      insertCrdJcic(aCardNo, aCurrentCode);
   }

}

void selectCrdCardType9() throws Exception {

   String sql1 = "select card_no , current_code from crd_card where 1=1 and acno_p_seqno = ? and corp_p_seqno = '' ";
   setString(1, hh.acnoPSeqno);

   sqlSelect(sql1);

   if (sqlNrow <= 0)
      return;

   String aCardNo = "", aCurrentCode = "";
   for (int ii = 0; ii < sqlNrow; ii++) {
      aCardNo = "";
      aCurrentCode = "";
      aCardNo = colSs(ii, "card_no");
      aCurrentCode = "3";
      updateCrdCardType9(aCardNo);
      ccaOutGoing.InsertCcaOutGoing(aCardNo, aCurrentCode, hBusiDate, blockReason4);
      insertCrdJcic(aCardNo, aCurrentCode);
   }

}

void selectCrdCardType2() throws Exception {

   String sql1 = "select card_no , current_code from crd_card where current_code ='0' and acno_p_seqno = ? and group_code not in ('3750','3751','3752') ";
   setString(1, hh.acnoPSeqno);

   sqlSelect(sql1);

   if (sqlNrow <= 0)
      return;

   String aCardNo = "", aCurrentCode = "";
   for (int ii = 0; ii < sqlNrow; ii++) {
      aCardNo = "";
      aCurrentCode = "";
      aCardNo = colSs(ii, "card_no");
      aCurrentCode = "4";
      updateCrdCardType1(aCardNo);
      ccaOutGoing.InsertCcaOutGoing(aCardNo, aCurrentCode, hBusiDate, blockReason4);
      insertCrdJcic(aCardNo, aCurrentCode);
   }

}

boolean selectCrdCardType2Corp() throws Exception {

   String sql1 = "select card_no , current_code from crd_card where current_code ='0' and acno_p_seqno = ? and group_code not in ('3750','3751','3752') ";
   setString(1, hh.acnoPSeqno);

   sqlSelect(sql1);

   if (sqlNrow <= 0)
      return false;

   String aCardNo = "", aCurrentCode = "";
   for (int ii = 0; ii < sqlNrow; ii++) {
      aCardNo = "";
      aCurrentCode = "";
      aCardNo = colSs(ii, "card_no");
      aCurrentCode = "4";
      updateCrdCardType1(aCardNo);
      ccaOutGoing.InsertCcaOutGoing(aCardNo, aCurrentCode, hBusiDate, blockReason4);
      insertCrdJcic(aCardNo, aCurrentCode);
   }
   return true;
}

void updateCrdCard(String aCardNo) throws Exception {
   sqlCmd="";

   daoTable = "crd_card";
   updateSQL = "block_code = ? , block_date = ? , mod_pgm = ? , mod_user = ? , mod_time = sysdate , mod_seqno = nvl(mod_seqno,0)+1 ";
   whereStr = "where card_no = ? ";

   setString(1, hh.blockReason1+hh.blockReason2+blockReason4+hh.blockReason4+hh.blockReason5);
   setString(2, hBusiDate);
   setString(3, "RskP202");
   setString(4, "ecs");
   setString(5, aCardNo);

   updateTable();
}

void updateCcaCardAcct() throws Exception {
   sqlCmd="";
   daoTable = "cca_card_acct";
   updateSQL = "block_reason3 = ? , block_date = to_char(sysdate,'yyyymmdd') , block_status = 'Y' ";
   whereStr = " where card_acct_idx = ? ";
   setString(1, blockReason4);
   setDouble(2, hh.cardAcctIdx);
   updateTable();
}

void updateCrdCardType1(String aCardNo) throws Exception {
   sqlCmd="";
   daoTable = "crd_card";
   updateSQL = "current_code = '4' , oppost_date = to_char(sysdate,'yyyymmdd') , oppost_reason =? , lost_fee_code = 'N' , "
       +"mod_user = 'ecs' , mod_pgm = 'RskP202' , mod_time = sysdate , mod_seqno = nvl(mod_seqno,0)+1 "
   ;
   whereStr = " where card_no = ? ";
   setString(1, blockReason4);
   setString(2, aCardNo);
   updateTable();
}

void updateCrdCardType9(String aCardNo) throws Exception {
   sqlCmd="";
   daoTable = "crd_card";
   updateSQL = "current_code = '3' , oppost_date = to_char(sysdate,'yyyymmdd') , oppost_reason =? , lost_fee_code = 'N' , "
       +"mod_user = 'ecs' , mod_pgm = 'RskP202' , mod_time = sysdate , mod_seqno = nvl(mod_seqno,0)+1 "
   ;
   whereStr = " where card_no = ? ";
   setString(1, blockReason4);
   setString(2, aCardNo);
   updateTable();
}

void insertCrdJcic(String aCardNo,String aCurrentCode) throws Exception {
    String hRowid = "";
    String hPaymentDate = "";
    hRowid = "";
    hPaymentDate = "" ;
    
    sqlCmd = "select rowid  as rowid ";
    sqlCmd += " from crd_jcic  ";
    sqlCmd += "where card_no  = ?  ";
    sqlCmd += "and trans_type = 'C'  ";
    sqlCmd += "and to_jcic_date =''  ";
    sqlCmd += "fetch first 1 rows only ";
    setString(1, aCardNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
        hRowid = getValue("rowid");

        sqlCmd="";

        daoTable   = "crd_jcic";
        updateSQL  = " current_code  = ?,";
        updateSQL += " oppost_reason = ?,";
        updateSQL += " oppost_date   = ?,";
        updateSQL += " payment_date  = ?,";
        updateSQL += " mod_user      = ?,";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " mod_pgm       = ?";
        whereStr   = "where rowid    = ? ";
        setString(1, aCurrentCode);
        setString(2, blockReason4);
        setString(3, sysDate);
        setString(4, hPaymentDate);
        setString(5, pgmName);
        setString(6, prgmId);
        setRowId(7, hRowid);
        updateTable();

        return;
    }

    setValue("card_no"      , aCardNo);
    setValue("crt_date"     , sysDate);
    setValue("crt_user"     , pgmName);
    setValue("trans_type"   , "C");
    setValue("current_code" , aCurrentCode);
    setValue("oppost_reason", blockReason4);
    setValue("oppost_date"  , sysDate);
    setValue("payment_date" , hPaymentDate);
    setValue("is_rc"        , hh.rcUseIndicator);
    setValue("mod_user"     , pgmName);
    setValue("mod_time"     , sysDate + sysTime);
    setValue("mod_pgm"      , prgmId);
    daoTable = "crd_jcic";
    insertTable();
    if (dupRecord.equals("Y")) {
        comcr.errRtn("insert_crd_jcic duplicate!", "", comcr.hCallBatchSeqno);
    }

}

void insertRskAcnoLog() throws Exception {
   sqlCmd="";

   daoTable = "rsk_acnolog";
   setValue("kind_flag", hh.kindFlag);
   setValue("acno_p_seqno", hh.acnoPSeqno);
   setValue("acct_type", hh.acctType);
   setValue("id_p_seqno", idPSeqno);
   setValue("corp_p_seqno", hh.corpPSeqno);
   setValue("log_date", hBusiDate);
   setValue("log_mode", "1");
   setValue("log_type", "3");
   setValue("log_reason", "");
   setValue("log_not_reason", "");
   setValue("block_reason", hh.blockReason1);
   setValue("block_reason2", hh.blockReason2);
   setValue("block_reason3", blockReason4);
   setValue("block_reason4", hh.blockReason4);
   setValue("block_reason5", hh.blockReason5);
   setValue("spec_status", hh.specStatus);
   setValue("fit_cond", "ECS");
   setValue("apr_flag", "Y");
   setValue("apr_date", hBusiDate);
   setValue("apr_user", "ECS");
   setValue("mod_user", "ecs");
   setValue("mod_time", sysDate+sysTime);
   setValue("mod_pgm", "RskP202");
   setValueDouble("mod_seqno", 1);
   insertTable();
}

int tiCheckCardP = -1;

void checkCardPerson(String aIdPSeqno) throws Exception {
   if (tiCheckCardP <= 0) {
      sqlCmd = "select count(*) as db_cnt from crd_card "
          +" where id_p_seqno = ? "
          +" and current_code = '0' "
          +" and acct_type ='01' ";
      tiCheckCardP = ppStmtCrt("tiCheckCardP", "");
   }

   setString(1, aIdPSeqno);
   sqlSelect(tiCheckCardP);

   if (sqlNrow <= 0)
      return;

   if (colNum("db_cnt") > 0)
      writeLog = true;
   else
      writeLog = false;

}

int tiCheckCardC = -1;

void checkCardCorp() throws Exception {
   if (tiCheckCardC <= 0) {
      sqlCmd = "select count(*) as db_cnt "
          +" from crd_card "
          +" where corp_p_seqno = ? "
          +" and current_code = '0' ";
      tiCheckCardC = ppStmtCrt("tiCheckCardC", "");
   }

   setString(1, corpPSeqno);
   sqlSelect(tiCheckCardC);

   if (sqlNrow <= 0)
      return;

   if (colNum("db_cnt") > 0)
      writeLog = true;
   else
      writeLog = false;

}

void initData() {
   crtDate = "";
   fromType = "";
   idNo = "";
   idPSeqno = "";
   corpNo = "";
   corpPSeqno = "";
   majorIdPSeqno = "";
   chiName = "";
   hasSupFlag = "";
   blockReason4 = "";
   specStatus = "";
   rowid = "";
   annouType = "";
   chargeId = "";
   writeLog = true;
}

class SmsData {

   String smsIdNo = "";
   String smsCellarPhone = "";
   String smsMsgPgm = "";
   String smsMsgId = "";
   String smsChiName = "";
   String msgDept = "";
   String msgUserId = "";
   String msgSeqno = "";
   String msgDesc = "";
   String cellphoneCheckFlag = "";

   void smsInitData() {
      smsIdNo = "";
      smsCellarPhone = "";
      smsMsgPgm = "";
      smsMsgId = "";
      smsChiName = "";
      msgDept = "";
      msgUserId = "";
      msgSeqno = "";
      msgDesc = "";
      cellphoneCheckFlag = "";
   }

}

class Hhdata {

   String acctType = "";
   String acnoPSeqno = "";
   String corpPSeqno = "";
   String blockReason1 = "";
   String blockReason2 = "";
   String blockReason3 = "";
   String blockReason4 = "";
   String blockReason5 = "";
   String specStatus = "";
   String kindFlag = "";
   String cardNo = "";
   String blockFlag = "";
   String badLoanFlag = "";
   String idPSeqno = "";
   String acnoFlag = "";
   String rcUseIndicator = "";
   int mCode = 0;
   double cardAcctIdx = 0;
   int sup1CardNum = 0;
   int sup0CardNum = 0;
   int corpCardNum = 0;
   boolean lbProc = true;

   void hhInitData() {
      acctType = "";
      acnoPSeqno = "";
      corpPSeqno = "";
      blockReason1 = "";
      blockReason2 = "";
      blockReason3 = "";
      blockReason4 = "";
      blockReason5 = "";
      specStatus = "";
      kindFlag = "";
      cardNo = "";
      blockFlag = "";
      badLoanFlag = "";
      cardAcctIdx = 0;
      sup1CardNum = 0;
      sup0CardNum = 0;
      corpCardNum = 0;
      mCode = 0;
      lbProc = true;
      idPSeqno = "";
      acnoFlag = "";
      rcUseIndicator = "";
   }

}

}
