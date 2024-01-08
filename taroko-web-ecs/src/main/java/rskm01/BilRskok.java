package rskm01;
/** 問交結案重啟帳單公用程式: comgl3
 * */

import taroko.base.Parm2Sql;

public class BilRskok extends busi.FuncBase {
busi.CommBusi commBusi = new busi.CommBusi();
Parm2Sql tt = new Parm2Sql();

private String rskCtrlSeqno = "";

private String referenceNo = "";
private String referenceNoNew = "";
private String cardNo = "";
private String rskOrgCardno = "";
private double rskAmt = 0;
private double rskOrgAmt = 0;
private String rskType = "";
private String rskMark = "";
private String stdVouchCd = "";
private String postFlag = "";
private double issueFee = 0;
private double issueFeeTax = 0;
private String currCode = "";
private double dcRskAmt = 0;
private double dcRskOrgAmt = 0;
private final String contractNo = "";
public boolean ibDebit=false;

public void setrskCtrlSeqno(String s1) {
   rskCtrlSeqno =s1;
}
private void tt_insert() {
   sqlExec(tt.getSql(), tt.getParms());
   if (sqlRowNum <= 0) {
      errmsg("insert BIL_RSKOK error");
   }
}
private void set_cardNo() {
   String ls_cardNo =wp.itemStr("A.card_no");
   String ls_org_cardNo =wp.itemStr("A.org_card_no");

   cardNo =ls_cardNo;
   rskOrgCardno =cardNo;
   //--
   if (notEmpty(ls_org_cardNo)) {
      rskOrgCardno =ls_cardNo;
      cardNo =ls_org_cardNo;
   }
}
public int rskP0030Insert(String a_std_cd) {
   data_Init();

//   set_Ctrl_seqno("801");  //第一次結案, 802.二次結案
   set_cardNo();
   if (empty(cardNo)) {
      errmsg("bil_rskok.card_no 不可空白");
      return rc;
   }

   issueFee = wp.num("A.mcht_close_fee");
   issueFeeTax = Math.rint(issueFee * 0.05);
   issueFee = issueFee - issueFeeTax;
   if (wp.itemEq("A.oversea_flag", "Y")) {
	   issueFee = 0;
	   issueFeeTax = 0;
   }

   referenceNo =wp.itemStr("A.reference_no");
   tt.insert("bil_rskok");
   tt.parmSet("reference_no", referenceNo);
//   tt.parmSet("card_no", wp.itemStr("A.card_no"));
//   tt.parmSet("rskOrgCardno", wp.itemStr("A.card_no"));
   tt.parmSet("card_no", cardNo);
   tt.parmSet("rsk_org_cardno", rskOrgCardno);
   tt.parmSet("std_vouch_cd", a_std_cd);
   tt.parmSet("rsk_amt", wp.num("A.mcht_repay"));  //ldc_repay
   tt.parmSet("rsk_org_amt", wp.num("A.prb_amount"));  //ldc_pblamt
   tt.parmSet("dc_rsk_amt", wp.num("A.dc_mcht_repay"));
   tt.parmSet("dc_rsk_org_amt", wp.num("A.dc_prb_amount"));
   tt.parmSet("curr_code", wp.itemStr("A.curr_code"));
   tt.parmSet("issue_fee", issueFee);
   tt.parmSet("issue_fee_tax", issueFeeTax);
   tt.parmSet("rsk_type", rskType);
   tt.parmSet("rsk_ctrl_seqno", rskCtrlSeqno);
   tt.modxxxSet(modUser, modPgm);

   tt_insert();
   return rc;
}

public int rskP0030Comgl3(String std_cd, int switch1, int switch2) {
   data_Init();

   //switch:1=Y, 0=N
   referenceNo = wp.itemStr("A.reference_no");
//   if (switch1 == 0) {
//      card_no = wp.itemStr("A.card_no");
//      rskOrgCardno = wp.itemStr("A.card_no");
//   }
//   else {
//      card_no = wp.itemStr("A.org_card_no");
//      rskOrgCardno = wp.itemStr("A.card_no");
//   }
   currCode = wp.itemStr("A.curr_code");
   if (switch2 == 0) {
      rskAmt = wp.num("A.prb_amount");
      dcRskAmt = wp.num("A.dc_prb_amount");
   }
   else {
      rskAmt = wp.num("A.mcht_repay");
      rskOrgAmt = wp.num("A.prb_amount");
      dcRskAmt = wp.num("A.dc_mcht_repay");
      dcRskOrgAmt = wp.num("A.dc_prb_amount");
   }
   stdVouchCd = std_cd;
//   set_Ctrl_seqno("801");

   set_cardNo();

   dbInsert();
   return rc;
}

public int rskp0030_I1(String a_vouch_cd) throws Exception  {

//   set_Ctrl_seqno("801");
   set_cardNo();

   tt.insert("bil_rskok");
   tt.parmSet("reference_no", wp.itemStr("A.reference_no"));
//   tt.parmSet("card_no", wp.itemStr("A.card_no"));
//   tt.parmSet("rskOrgCardno", wp.itemStr("A.card_no"));
   tt.parmSet("card_no", cardNo);
   tt.parmSet("rsk_org_cardno", rskOrgCardno);
   tt.parmSet("curr_code", wp.itemStr("A.curr_code"));
   tt.parmSet("rsk_amt", wp.num("A.prb_amount"));
   tt.parmSet("dc_rsk_amt", wp.num("A.dc_prb_amount"));
   tt.parmSet("std_vouch_cd", a_vouch_cd); //"P-13");
   tt.parmSet("contract_no", wp.itemStr("contract_no"));
   tt.parmSet("rsk_ctrl_seqno", rskCtrlSeqno);
   if (ibDebit) {
      tt.parmSet("rsk_type","D");
   }
   tt.modxxxSet(modUser, modPgm);

   tt_insert();
   return rc;
}

public int rskP0030R024() {
   data_Init();

   referenceNo = wp.itemStr("A.reference_no");
   cardNo = wp.itemStr("A.card_no");
   stdVouchCd = "";
   rskAmt = wp.itemNum("A.mcht_repay");
   rskOrgAmt = wp.itemNum("A.prb_amount");
   issueFee = wp.itemNum("A.mcht_close_fee");
   issueFeeTax = Math.rint(issueFee * 0.05);
   issueFee = issueFee - issueFeeTax;
   if (wp.itemEq("A.oversea_flag", "Y")) {
	   issueFee = 0;
	   issueFeeTax = 0;
   }
   currCode = wp.itemStr("A.curr_code");
   dcRskAmt = wp.itemNum("A.dc_mcht_repay");
   dcRskOrgAmt = wp.itemNum("A.dc_prb_amount");

//   set_Ctrl_seqno("801");
   set_cardNo();

   dbInsert();
   return rc;
}

public int rskP0030RD7() {
   data_Init();

   referenceNo = wp.itemStr("A.reference_no");
   //card_no = wp.itemStr("A.card_no");
   stdVouchCd = "R-D7";
   rskAmt = wp.itemNum("A.mcht_repay");
   rskOrgAmt = wp.itemNum("A.prb_amount");
   issueFee = wp.itemNum("A.mcht_close_fee");
   issueFeeTax = Math.rint(issueFee * 0.05);
   issueFee = issueFee - issueFeeTax;
   if (wp.itemEq("A.oversea_flag", "Y")) {
	   issueFee = 0;
	   issueFeeTax = 0;
   }

   //set_Ctrl_seqno("801");
   set_cardNo();

   dbInsert();
   return rc;
}

public int dbDelete(String a_refer_no) {
   String sql1 = "delete bil_rskok"
         + " where 1=1"  //rsk_ctrl_seqno =?"
           +" and reference_no =?"
         + " and post_flag <>'Y'";
   setString(1, a_refer_no);
   sqlExec(sql1);
   if (sqlRowNum < 0) {
      errmsg("delete bil_rskok error, kk=" + rskCtrlSeqno);
      return -1;
   }
   return sqlRowNum;
}

//void set_Ctrl_seqno(String rsk_status) throws Exception  {
//   rsk_ctrl_seqno = zzComm.rsk_ctrlSeqno_Prbl(
//         wp.itemStr("A.ctrl_seqno"),
//         wp.itemStr("A.bin_type"), rsk_status);
//}

void data_Init() {
	referenceNo = "";
   referenceNoNew = "";
   cardNo = "";
   rskOrgCardno = "";
   rskAmt = 0;
   rskOrgAmt = 0;
   rskType = "";
   rskMark = "";
   stdVouchCd = "";
   postFlag = "N";
   issueFee = 0;
   issueFeeTax = 0;
   currCode = "";
   dcRskAmt = 0;
   dcRskOrgAmt = 0;
   //rsk_ctrl_seqno = "";

   if (ibDebit) rskType="D";
}


void dbInsert() {
   if (empty(cardNo)) {
      errmsg("bil_rskok.card_no 不可空白");
      return;
   }

   Parm2Sql spp=new Parm2Sql();
   spp.insert("bil_rskok");

   spp.parmSet("reference_no", referenceNo);
   spp.parmSet("reference_no_new", referenceNoNew);
   spp.parmSet("card_no", cardNo);
   spp.parmSet("rsk_org_cardno", rskOrgCardno);
   spp.parmSet("rsk_amt", rskAmt);
   spp.parmSet("rsk_org_amt", rskOrgAmt);
   spp.parmSet("rsk_type", rskType);
   spp.parmSet("rsk_mark", rskMark);
   spp.parmSet("std_vouch_cd", stdVouchCd);
   spp.parmSet("post_flag", postFlag);
   spp.parmSet("issue_fee", issueFee);
   spp.parmSet("issue_fee_tax", issueFeeTax);
   spp.parmSet("curr_code", currCode);
   spp.parmSet("dc_rsk_amt", dcRskAmt);
   spp.parmSet("dc_rsk_org_amt", dcRskOrgAmt);
   spp.parmSet("rsk_ctrl_seqno", rskCtrlSeqno);
   spp.modxxxSet(modUser, modPgm);

   sqlExec(spp.getSql(), spp.getParms());
   if (sqlRowNum <= 0) {
      errmsg("insert bil_rskok error, kk=" + referenceNo);
   }
}

}
