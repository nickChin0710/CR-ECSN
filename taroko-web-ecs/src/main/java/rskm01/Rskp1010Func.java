package rskm01;
/**
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 */

import busi.FuncAction;

public class Rskp1010Func extends FuncAction {
String isReferNo = "";

@Override
public void dataCheck() {
   // TODO Auto-generated method stub

}

@Override
public int dbInsert() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbUpdate() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbDelete() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

void selectDbbBill() throws Exception  {
   daoTid = "dbb-";
   strSql = " select "
         + " bill_type ,"
         + " contract_no ,"
         + " txn_code ,"
         + " reference_no_original as reference_no_ori ,"
         + " contract_no ,"
         + " install_curr_term as inst_curr_term , "
         + " uf_idno_name2(id_p_seqno,'Y') as idno_name , "
         + " uf_idno_id2(id_p_seqno,'Y') as major_idno "
         + " from dbb_bill"
         + " where reference_no =?";
   setString(1, isReferNo);
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("查無帳單資料, ref-no=" + isReferNo);
   }


   return;
}

public int reptSelect() throws Exception  {
   isReferNo = varsStr("reference_no");
   if (empty(isReferNo)) {
      errmsg("帳單參考號: 不可空白");
      return -1;
   }
   selectDbbBill();
   log("0:" + rc);
   if (rc != 1)
      return rc;
   if (select_check() != 1)
      return rc;

   wp.colSet("reference_no_ori", colStr("dbb-reference_no_ori"));
   wp.colSet("contract_no", colStr("dbb-contract_no"));


   return rc;
}

int select_check() {
   String ss = colStr("dbb-bill_type").substring(0, 2);
   if (pos("|OK|OS|I1|I2", ss) > 0) {
      errmsg("請選擇正確的帳單類別碼！！");
      return 0;
   }
   if (colEq("dbb-bill_type", "NCFC")) {
      errmsg("國外手續費: 不可 列問交,調單,扣款");
      return -1;
   }

   ss = colStr("dbb-txn_code");
   if (pos("06|25|27|28|29|RI", ss) > 0) {
      errmsg("請選擇正確的交易碼！！" + ss);
      return 0;
   }

   return 1;
}

public int chgbSelect() throws Exception  {
   isReferNo = varsStr("reference_no");
   if (empty(isReferNo)) {
      errmsg("帳單參考號: 不可空白");
      return -1;
   }
   selectDbbBill();
   if (rc != 1)
      return rc;
   if (select_check() != 1)
      return rc;

   wp.colSet("reference_no_ori", colStr("dbb-reference_no_ori"));
   wp.colSet("contract_no", colStr("dbb-contract_no"));
   wp.colSet("idno_name", colStr("dbb-idno_name"));
   wp.colSet("major_idno", colStr("dbb-major_idno"));
   return rc;
}

public int arbitSelect() throws Exception  {
   isReferNo = varsStr("reference_no");
   if (empty(isReferNo)) {
      errmsg("帳單參考號: 不可空白");
      return -1;
   }
   selectDbbBill();
   if (rc != 1)
      return rc;

   wp.colSet("reference_no_ori", colStr("dbb-reference_no_ori"));
   wp.colSet("contract_no", colStr("dbb-contract_no"));
   return rc;
}

public int complSelect() throws Exception  {
   isReferNo = varsStr("reference_no");
   if (empty(isReferNo)) {
      errmsg("帳單參考號: 不可空白");
      return -1;
   }
   selectDbbBill();
   if (rc != 1)
      return rc;

   wp.colSet("reference_no_ori", colStr("dbb-reference_no_ori"));
   wp.colSet("contract_no", colStr("dbb-contract_no"));
   return rc;
}

}
