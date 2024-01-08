package Cca;

import java.text.Normalizer;

/**
 * 2023-1106 V1.00.00   JH    initial
 * */
@SuppressWarnings({"unchecked", "deprecation"})
public class CcaE065 extends com.BaseBatch {
private final String PROGNAME = "逾期未銷帳處理  2023-1106 V1.00.00";
//--------------
String hh_card_no="";
String hh_rowid="";
String is_sysDate="";

//=*****************************************************************************
public static void main(String[] args) {
   CcaE065 proc = new CcaE065();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 2) {
      printf("Usage : CcaE065 [busi_date]");
      okExit(0);
   }

   if (args.length >= 1) {
      setBusiDate(args[0]);
//      if (args[0].length()==8) {
//         String sG_Args0 = args[0];
//         hBusiDate= Normalizer.normalize(sG_Args0, java.text.Normalizer.Form.NFKD);
//      }
      is_sysDate =hBusiDate;
   }

   dbConnect();
   if (empty(is_sysDate)) {
      is_sysDate =sysDate;
   }
   printf("-- 處理日期[%s] ", is_sysDate);

   selectCca_cca_sys_parm1(is_sysDate);
   //逾期未銷帳處理程式
   selectCca_auth_txlog();

   sqlCommit();
   endProgram();
}
//============
void selectCca_auth_txlog() throws Exception {
   //MCC_CODE[9311]:稅金--
   sqlCmd ="select"
       +" card_no, hex(rowid) as rowid"
       +" from cca_auth_txlog"
       +" where 1=1"
       +" and tx_date < case when MCC_CODE ='9311' AND CONSUME_COUNTRY ='TW' THEN cast(? as varchar(8)) else cast(? as varchar(8)) end "
       +" and tx_date < ?"
       +" and cacu_amount  = 'Y' "
       +" and mtch_flag  not in ('Y','U') "
   +" order by tx_date"
//   +" limit 111"
   ;
   daoTable  = " cca_auth_txlog ";

   ppp(1, is_uDate_tax);
   ppp(2, is_uDate);
   if (ii_uBound_tax >=ii_uBound) ppp(3, is_uDate);
   else ppp(3, is_uDate_tax);

   openCursor();
   printf(" process data start.....");
   int li_dspCnt=10000;
   while (fetchTable()) {
      totalCnt++;
      dspProcRow(li_dspCnt);

      hh_card_no    = colSs("card_no");
      hh_rowid      = colSs("rowid");

      updateCca_auth_txlog();
      if ((totalCnt % li_dspCnt)==0) {
         sqlCommit();
      }
   }
   closeCursor();
}
//--------
com.Parm2sql ttUtxlog=null;
void updateCca_auth_txlog() throws Exception {
   if (ttUtxlog ==null) {
      ttUtxlog =new com.Parm2sql();
      ttUtxlog.update("cca_auth_txlog");
   }
   ttUtxlog.aaaYmd("mtch_date");
   ttUtxlog.aaa("mtch_flag", "U");
   ttUtxlog.aaa("mod_pgm", hModPgm);
   ttUtxlog.aaaDtime("mod_time");
   //-kk.rowid-
   ttUtxlog.aaaWhere(" where rowid =CAST(HEXTORAW(?) AS VARCHAR(2000) FOR BIT DATA)", hh_rowid);

   if (ttUtxlog.ti <=0) {
      ttUtxlog.ti =ppStmtCrt("ttUtxlog", ttUtxlog.getSql());
   }

   sqlExec(ttUtxlog.ti, ttUtxlog.getConvParm());
   if (sqlNrow <=0) {
      errmsg("update cca_auth_txlog N-find, kk[%s]", hh_card_no);
      okExit(0);
   }
}
//-------
int ii_uBound=0, ii_uBound_tax=0;
String is_uDate="", is_uDate_tax="";
void selectCca_cca_sys_parm1(String a_sysDate) throws Exception {
   sqlCmd ="select sys_data1 as  ubound "
       +", sys_data2 as ubound_tax"
       +" from cca_sys_parm1"
       +" where sys_id ='REPORT'"
       +" and sys_key ='LOGIC_DAY'"
       ;
//       + " to_char(sysdate - to_number(nvl(sys_data1,'30')),'yyyymmdd') as udate , "
//       + " to_number(nvl(sys_data2,'75'))                               as ubound_tax , "
//       + " to_char(sysdate - to_number(nvl(sys_data2,'75')),'yyyymmdd') as udate_tax ";
//   daoTable  = "cca_sys_parm1";
//   whereStr  = "WHERE sys_id     =  ? "
//       + "  and sys_key    =  ? ";
//   setString(1, "REPORT");
//   setString(2, "LOGIC_DAY");

   sqlSelect();
   if (sqlNrow <=0) {
      errmsg("select_cca_cca_sys_parm1  error, kk[%s,%s]", "REPORT","LOGIC_DAY");
      okExit(1);
   }

   ii_uBound     =colInt("ubound");
   if (ii_uBound <=0) ii_uBound=30;
   ii_uBound_tax =colInt("ubound_tax");
   if (ii_uBound_tax <=0) ii_uBound_tax=75;
   is_uDate      =commDate.dateAdd(a_sysDate,0,0,0 - ii_uBound);
   is_uDate_tax  =commDate.dateAdd(a_sysDate,0,0,0 - ii_uBound_tax);

   printf("一般交易未銷帳: sysDate[%s], 天數[%s], 日期[%s]", a_sysDate, ii_uBound, is_uDate);
   printf("卡繳稅交易未銷帳: sysDate[%s], 天數[%s], 日期[%s]", a_sysDate, ii_uBound_tax, is_uDate_tax);
}
}
