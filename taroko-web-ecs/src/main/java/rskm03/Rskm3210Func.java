package rskm03;
/**
 * 2019-0816   JH    dataSelect
 */

import busi.FuncEdit;

public class Rskm3210Func extends FuncEdit {
String kk1 = "";

//	public Rskm3210_func(TarokoCommon wr) {
//		wp = wr;
//		this.conn = wp.getConn();
//	}
@Override
public int querySelect() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dataSelect()  {
   msgOK();

   String kk1 = wp.itemStr("card_no");
   if (empty(kk1)) {
      errmsg("卡號: 不可空白");
      return rc;
   }
   strSql = "select rsk_ctfi_case.*" +
         ", to_char(mod_time,'yyyymmdd') as mod_date" +
         ", hex(rowid) as rowid" +
         " from rsk_ctfi_case" +
         " where card_no =?";
   setParm(1, kk1);
   sqlSelectWp(strSql);
   if (sqlRowNum <= 0) {
      errmsg("查無卡片資料; card_no[%s]", kk1);
      return rc;
   }
   
   if(wp.colEmpty("id_p_seqno")) {
   	String sql1 = " select id_p_seqno from crd_card where card_no = ? union select id_p_seqno from dbc_card where card_no = ? ";
   	sqlSelect(sql1,new Object[]{kk1,kk1});
   	if(sqlRowNum >0) {
   		wp.colSet("id_p_seqno", colStr("id_p_seqno"));
   	}
   }
   
   return rc;
}

@Override
public void dataCheck()  {
   kk1 = wp.itemStr("card_no");

   sqlWhere = " where 1=1 and card_no = ? and nvl(mod_seqno,0) = ? ";
   Object[] parms = new Object[] {kk1, wp.itemNum("mod_seqno")};   
   if (this.isOtherModify("rsk_ctfi_case", sqlWhere , parms)) {
      return;
   }
}

@Override
public int dbInsert() {
   return rc;
}

@Override
public int dbUpdate()  {
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }
   strSql = "update rsk_ctfi_case set "
         + " card_no = :card_no, "
         + " id_p_seqno = :id_p_seqno, "
         + " case_seqno = :case_seqno, "
         + " case_type = :case_type, "
         + " fraud_ok_amt = :fraud_ok_amt, "
         + " vd_flag = :vd_flag, "
         + " fraud_area = :fraud_area, "
         + " fraud_cntry_code =:fraud_cntry_code , "
         + " fraud_area_code =:fraud_area_code , "
//	         + " disput_sign = :disput_sign, "
         + " conf_case_type = :conf_case_type, "
         + " modus_oper = :modus_oper, "
         + " friend_fraud_flag = :friend_fraud_flag, "
         + " ch_moral_flag = :ch_moral_flag, "
         + " no_fraud_flag = :no_fraud_flag, "
         + " survey_result = :survey_result, "
         + " survey_result2 =:survey_result2 ,"
         + " survey_result3 =:survey_result3 ,"
         + " survey_result4 =:survey_result4 ,"
         + " survey_result5 =:survey_result5 ,"
         + " survey_result6 =:survey_result6 ,"
         + " survey_result7 =:survey_result7 ,"
         + " survey_result8 =:survey_result8 ,"
         + " survey_result9 =:survey_result9 ,"
         + " survey_result10 =:survey_result10 ,"
         + " survey_result_remark = :survey_result_remark, "
         + " case_file_flag = :case_file_flag, "
         + " adj_limit_code = :adj_limit_code, "
         + " adj_limit = :adj_limit, "
         + " un_adj_reason = :un_adj_reason, "
         + " reissue_card_flag =:reissue_card_flag , "
         + " mod_user = :mod_user, "
         + " mod_time = sysdate, "
         + " mod_pgm = :mod_pgm, "
         + " mod_seqno =nvl(mod_seqno,0)+1"
         + " where card_no ='" + kk1 + "'"
         + " and nvl(mod_seqno,0) ='" + wp.modSeqno() + "'"
   ;

   setString("card_no", kk1);
   item2ParmStr("id_p_seqno");
   item2ParmStr("case_seqno");
   item2ParmStr("case_type");
   item2ParmNum("fraud_ok_amt");
   item2ParmStr("vd_flag");
   item2ParmStr("fraud_area");
   item2ParmStr("fraud_cntry_code");
   item2ParmStr("fraud_area_code");
//				item2ParmStr("disput_sign");
   item2ParmStr("conf_case_type");
   item2ParmStr("modus_oper");
   item2ParmNvl("friend_fraud_flag", "N");
   item2ParmNvl("ch_moral_flag", "N");
   item2ParmNvl("no_fraud_flag", "N");
   item2ParmNvl("case_file_flag", "N");
   item2ParmNvl("adj_limit_code", "N");
   item2ParmNvl("case_file_flag", "N");
   item2ParmStr("un_adj_reason");
   item2ParmStr("adj_limit");
   item2ParmStr("survey_result");
   item2ParmStr("survey_result2");
   item2ParmStr("survey_result3");
   item2ParmStr("survey_result4");
   item2ParmStr("survey_result5");
   item2ParmStr("survey_result6");
   item2ParmStr("survey_result7");
   item2ParmStr("survey_result8");
   item2ParmStr("survey_result9");
   item2ParmStr("survey_result10");
   item2ParmStr("survey_result_remark");
   item2ParmNvl("reissue_card_flag", "N");
   setString("mod_user", wp.loginUser);
   item2ParmStr("mod_pgm");
   item2ParmNum("mod_seqno");


   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_ctfi_case error: " + this.sqlErrtext);
   }
   return rc;
}


@Override
public int dbDelete() {
   // TODO Auto-generated method stub
   return 0;
}

public int dbInsert_dtl()  {
   msgOK();

   strSql = "insert into RSK_CTFI_FRMAN ("
         + " card_no, "      //1
         + " frman_name, "
         + " frman_idno, "
         + " frman_birdate,"
         + " fr_remark, "
         + " no_fraud_man, "
         + " mod_user, mod_time, mod_pgm,mod_seqno "
         + " ) values ("
         + " :card_no, "      //1
         + " :frman_name, "
         + " :frman_idno, "
         + " :frman_birdate,"
         + " :fr_remark, "
         + " :no_fraud_man, "
         + " :mod_user, sysdate, :mod_pgm,1 "
         + " )";
   setString("card_no", wp.itemStr("card_no"));
   var2ParmStr("frman_name");
   var2ParmStr("frman_idno");
   var2ParmStr("frman_birdate");
   var2ParmStr("fr_remark");
   var2ParmStr("no_fraud_man");
   var2ParmStr("mod_user");
   var2ParmStr("mod_pgm");
   this.sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("Insert RSK_CTFI_FRMAN error; " + getMsg());
   }
   return rc;
}

public int dbDelete_dtl()  {
   msgOK();
   strSql = "Delete RSK_CTFI_FRMAN"
         + " where rowid =x'" + this.varsStr("rowid") + "'"
   ;
   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("Delete RSK_CTFI_FRMAN err; " + getMsg());
      rc = -1;
   }
   else rc = 1;
   return rc;
}

public int insert_dtl()  {
   msgOK();

   strSql = "insert into RSK_CTFI_FRMAN ("
         + " card_no, "      //1
         + " frman_name, "
         + " frman_idno, "
         + " frman_birdate,"
         + " fr_remark, "
         + " no_fraud_man, "
         + " mod_user, mod_time, mod_pgm,mod_seqno "
         + " ) values ("
         + " :card_no, "      //1
         + " :frman_name, "
         + " :frman_idno, "
         + " :frman_birdate,"
         + " :fr_remark, "
         + " 'N', "
         + " :mod_user, sysdate, :mod_pgm,1 "
         + " )"
   ;

   item2ParmStr("card_no");
   item2ParmStr("frman_name", "ex_frman_name");
   item2ParmStr("frman_idno", "ex_frman_idno");
   item2ParmStr("frman_birdate", "ex_frman_birdate");
   item2ParmStr("fr_remark", "ex_fr_remark");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());

   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("insert RSK_CTFI_FRMAN error ");
   }

   return rc;
}

}
