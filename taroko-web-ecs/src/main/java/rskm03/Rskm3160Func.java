package rskm03;
/**
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import busi.FuncEdit;
import taroko.base.Parm2Sql;
import taroko.com.TarokoCommon;

public class Rskm3160Func extends FuncEdit {
String kk1 = "", is_case_seqno = "";
Parm2Sql tt=new Parm2Sql();

public Rskm3160Func(TarokoCommon wr) {
   wp = wr;
   this.conn = wp.getConn();
   modPgm =wp.modPgm();
   modUser =wp.loginUser;
}

@Override
public int querySelect() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dataSelect() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public void dataCheck()  {
   kk1 = wp.itemStr("card_no");
   if (this.ibAdd) {
      if (empty(wp.itemStr("card_no"))) {
         errmsg("卡號：不可空白");
         return;
      }
   }
   if (this.isAdd()) {
      selectSeqno();
      return;
   }

   if (isDelete()) {
      if (checkDelete() == false) {
         errmsg("已有交易紀錄不可刪除");
         return;
      }
/*		
		if(!wp.item_eq("case_date", this.get_sysDate())){
			errmsg("僅能刪除當日案件");
			return ;
		}
*/
   }

   sqlWhere = " where 1=1"
         + " and card_no='" + kk1 + "'"
         + " and nvl(mod_seqno,0) =" + wp.modSeqno();
   if (this.isOtherModify("RSK_CTFI_CASE", sqlWhere)) {
      return;
   }
}

boolean checkDelete()  {

   String sql1 = " select count(*) as db_cnt from rsk_ctfi_txn where card_no = ? ";
   sqlSelect(sql1, new Object[]{kk1});

   return !(colNum("db_cnt") > 0);
}

void selectSeqno()  {
//   wp.dddSql_log = true;
   String ls_date = "";
   if (!empty(wp.itemStr("case_date"))) {
      ls_date = commString.mid(wp.itemStr("case_date"), 0, 6);
   }
   else {
      ls_date = commString.mid(sysDate, 0, 6);
   }


   String sql1 = " select "
         + " max(case_seqno) as case_seqno "
         + " from rsk_ctfi_case "
         + " where substr(case_date,1,6) = ? ";
   sqlSelect(sql1, new Object[]{ls_date});

   if (colInt("case_seqno") <= 0) {
      is_case_seqno = ls_date.substring(2, 6) + "001";
   }
   else {
      is_case_seqno = commString.intToStr(colInt("case_seqno") + 1);
   }
   log("case_seqno:" + is_case_seqno);
}

@Override
public int dbInsert()  {
   actionInit("A");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   tt.insert("rsk_ctfi_case");
   tt.parmSet("card_no", kk1);
   tt.parmSet("id_p_seqno", wp.itemStr("id_p_seqno"));
   tt.parmSet("case_date", wp.itemStr("case_date"));
   tt.parmSet("case_seqno", is_case_seqno);
   tt.parmSet("case_user", wp.itemStr("case_user"));
   tt.parmSet("group_code", wp.itemStr("group_code"));
   tt.parmSet("new_end_date", wp.itemStr("new_end_date"));
   tt.parmSet("case_type", wp.itemStr("case_type"));
   tt.parmSet("case_diffi", wp.num("case_diffi"));
   tt.parmSet("case_source", wp.itemStr("case_source"));
   tt.parmSet("fraud_ok_cnt", wp.num("fraud_ok_cnt"));
   tt.parmSet("fraud_ok_amt", wp.num("fraud_ok_amt"));
   tt.parmSet("survey_user", wp.itemStr("survey_user"));
   tt.parmSet("tel_no1", wp.itemStr("tel_no1"));
   tt.parmSet("tel_no2", wp.itemStr("tel_no2"));
   tt.parmSet("tel_no3", wp.itemStr("tel_no3"));
   tt.parmSet("bill_addr", wp.itemStr("bill_addr"));
   tt.parmSet("ctfg_seqno", wp.num("ctfg_seqno"));
   tt.parmSet("vd_flag", wp.itemStr("vd_flag"));
   tt.modxxxSet(modUser,modPgm);

   sqlExec(tt.getSql(),tt.getParms());
   if (sqlRowNum <= 0) {
      errmsg("卡號已立案 不可重復立案");
   }
   return rc;
}

@Override
public int dbUpdate()  {
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   tt.update("rsk_ctfi_case");
   tt.parmSet("case_date", wp.itemStr("case_date"));
   tt.parmSet("case_user", wp.itemStr("case_user"));
   tt.parmSet("case_type", wp.itemStr("case_type"));
   tt.parmSet("case_diffi", wp.num("case_diffi"));
   tt.parmSet("case_source", wp.itemStr("case_source"));
   tt.parmSet("fraud_ok_cnt", wp.num("fraud_ok_cnt"));
   tt.parmSet("fraud_ok_amt", wp.num("fraud_ok_amt"));
   tt.parmSet("survey_user", wp.itemStr("survey_user"));
   tt.modxxxSet(modUser,modPgm);
   tt.whereParm(" where card_no =?",kk1);

   sqlExec(tt.getSql(), tt.getParms());
   if (sqlRowNum <= 0) {
      errmsg("update RSK_CTFI_CASE error: " + this.sqlErrtext);
   }
   return rc;
}

@Override
public int dbDelete()  {
   actionInit("D");
   dataCheck();
   if (rc != 1) {
      return rc;
   }
   strSql = "delete rsk_ctfi_case " + sqlWhere;
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
   }
   return rc;
}

}
