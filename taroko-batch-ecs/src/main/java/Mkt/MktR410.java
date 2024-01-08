package Mkt;
/**
 * 2023-0828 V1.00.00   JH    initial
 * */

import java.text.Normalizer;

@SuppressWarnings({"unchecked", "deprecation"})
public class MktR410 extends com.BaseBatch {
private final String PROGNAME = "聯名機構主機入帳處理-回饋企業  2023-0828 V1.00.00";
HH hh=new HH();
//-----------
class HH {
   double rew_amount=0;
   String member_corp_no="";
   String member_acct_no="";

   void initData() {
      rew_amount=0;
      member_corp_no="";
      member_acct_no="";
   }
}
//--
String is_rewYYMM="";
//=*****************************************************************************
public static void main(String[] args) {
   MktR410 proc = new MktR410();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 2) {
      printf("Usage : MktR410 [busi_date]");
      okExit(0);
   }

   if (args.length >= 1) {
      if (args[0].length()==8) {
         String sG_Args0 = args[0];
         hBusiDate= Normalizer.normalize(sG_Args0, java.text.Normalizer.Form.NFKD);
      }
   }

   dbConnect();
   //-第二營業日-
   String lsbusiDate2=commString.left(hBusiDate,6)+"01";
   int libusiDay=0;
   for (int ii = 0; ii <31 ; ii++) {
      if (checkWorkDate(lsbusiDate2)) continue;
      libusiDay++;
      if (libusiDay >=2) break;
      lsbusiDate2 =commDate.dateAdd(lsbusiDate2,0,0,1);
   }
   if (!eq(hBusiDate,lsbusiDate2)) {
      printf(" 此作業須在本月第2營業日[%s]才可執行", lsbusiDate2);
      okExit(0);
   }

   is_rewYYMM =commString.left(hBusiDate,6);

   selectMkt_re_group();

   sqlCommit();
   endProgram();
}
//=======================
void selectMkt_re_group() throws Exception {
   sqlCmd ="SELECT sum(A.rew_amount) AS rew_amount " +
            ", B.member_corp_no " +
            " FROM  mkt_re_group A " +
            " JOIN ptr_group_code B ON A.group_code=B.group_code AND B.member_flag='Y' " +
            " WHERE 1=1 " +
            " AND A.reward_type ='2' " +
            " AND A.rew_yyyymm =? " +
            " AND A.rew_amount>0 " +
            " GROUP BY B.member_corp_no"
    ;
   ppp(1, is_rewYYMM);
   int liCrsr=openCursor();
   while (fetchTable(liCrsr)) {
      totalCnt++;
      hh.initData();
//      hh.group_code =colSs("group_code");
      hh.rew_amount =colNum("rew_amount");
//      hh.group_name =colSs("group_name");
      hh.member_corp_no =colSs("member_corp_no");

      selectMkt_member();

      deleteMkt_in_reward();
      insertMkt_in_reward();
   }

   closeCursor(liCrsr);
}
//===============
int tiMemb=-1;
void selectMkt_member() throws Exception {
   if (tiMemb <=0) {
      sqlCmd ="select member_name,acct_no"
               +" from mkt_member"
               +" where member_corp_no =?"+
      commSqlStr.rownum(1)
      ;
      tiMemb =ppStmtCrt("tiMemb","");
   }

   ppp(1, hh.member_corp_no);

   String CC=daoTid="memb.";
   sqlSelect(tiMemb);
   if (sqlNrow <=0) return;
//   hh.member_name =colSs(CC+"member_name");
   String lsAcctNo =colSs(CC+"acct_no");
   hh.member_acct_no =lsAcctNo.replaceAll("-","").replaceAll("_","");
}
//================
int tiDrewd=-1;
void deleteMkt_in_reward() throws Exception {
   if (tiDrewd <=0) {
      sqlCmd ="delete mkt_in_reward"+
               " where in_month =? "+
               " and in_account =? "+
               " and in_type ='1' "+
               " and in_date =? "
       ;
      tiDrewd =ppStmtCrt("tiDrewd","");
   }
   String lsAcctNo =commString.left(hh.member_acct_no,13);

   ppp(1, is_rewYYMM);
   ppp(lsAcctNo);
   ppp(hBusiDate);

//   debug =true;
//   dddSql(tiDrewd);
//   debug =false;

   sqlExec(tiDrewd);
   if (sqlNrow <0) {
      sqlerr(" delete mkt_in_rewadr, kk[%s]", hh.member_acct_no);
      okExit(0);
   }
}
//---------------------
com.Parm2sql ttArewd=null;
void insertMkt_in_reward() throws Exception {
   if (ttArewd ==null) {
      ttArewd =new com.Parm2sql();
      ttArewd.insert("mkt_in_reward");
   }

   String lsAcctNo =commString.left(hh.member_acct_no,13);

   ttArewd.aaa("in_month"                  , is_rewYYMM);
   ttArewd.aaa("corp_no"                   , hh.member_corp_no);
   ttArewd.aaa("in_account"                , lsAcctNo);
   ttArewd.aaa("in_type"                   , "1");
   ttArewd.aaa("in_date"                   , hBusiDate);
   ttArewd.aaa("in_amount"                 , hh.rew_amount);
   ttArewd.aaa("real_amount"               , hh.rew_amount);
   ttArewd.aaa("charge_amount"             , 0);
   ttArewd.aaa("acct_bank_no"              , "");
   ttArewd.aaa("post_date"                 , hBusiDate);
   ttArewd.aaa("post_seqno"                , "");
//   ttArewd.aaa("execute_flag"              , hh.execute_flag);
   ttArewd.aaa("crt_date"                  , sysDate);
   ttArewd.aaa("mod_date"                  , sysDate);
   ttArewd.aaa("mod_user"                  , hModPgm);
   ttArewd.aaaDtime("mod_time");
   ttArewd.aaa("mod_pgm"                   , hModPgm);

   if (ttArewd.ti <=0) {
      ttArewd.ti =ppStmtCrt("ttArewd", ttArewd.getSql());
   }

   sqlExec(ttArewd.ti, ttArewd.getParms());
   if (sqlNrow <=0) {
      sqlerr("insert mkt_in_reward error");
      errExit(1);
   }

}

//=======
}
