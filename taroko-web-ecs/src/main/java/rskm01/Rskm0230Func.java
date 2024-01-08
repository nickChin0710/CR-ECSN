package rskm01;
/** 扣款撥款登錄
 * 2021-0804   JH    dc_dest_amt=decode(901,dest_amt,dc_dest_amt)
 */

public class Rskm0230Func extends busi.FuncAction {
busi.CommCurr zzCurr = new busi.CommCurr();
String kk1 = "";
double imDisbDcamt = 0, imDisbAmt = 0;

@Override
public int querySelect() {
   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

@Override
public void dataCheck() {
   kk1 = varsStr("rowid");
   selectRskChgback(kk1);
   if (rc != 1) {
      return;
   }

   imDisbDcamt = varsNum("disb_dc_amt");
   imDisbDcamt = zzCurr.resetAmt(imDisbDcamt, colStr("A.curr_code"));

   if (imDisbDcamt < 0) {
      errmsg("撥款結算金額  不可小於0");
      return;
   }

   if (pos(",1,3", colStr("A.chg_stage")) <= 0 || colNeq("A.sub_stage", "30")) {
      errmsg("扣款狀態: 不是[一扣,二扣; 待覆核], 不可修改");
      return;
   }

   if (colEmpty("A.fst_disb_apr_date") == false) {
      errmsg("扣款[撥款]已覆核, 不可修改");
      return;
   }
   //-OK-
   imDisbAmt =imDisbDcamt;
   if (!zzCurr.isTw(colStr("A.curr_code"))) {
      imDisbAmt = zzCurr.dc2twAmt(colNum("A.dest_amt"), colNum("A.dc_dest_amt"), imDisbDcamt);
   }

/*
	if io_curr.f_chk_decimal(dw_data.item_ss(L,'curr_code'),dw_data.item_num(L,'disb_dc_amt'))<>1 then
		dw_data.selectrow(L,True)
		//f_errmsg("撥款結算金額  小數輸入錯誤~")
		Return -1
	end if	
	*/
}

void selectRskChgback(String aRowid) {
   strSql = "select chg_stage, sub_stage, dest_amt, dc_dest_amt, curr_code"
         + ", fst_disb_apr_date"
         + ", mod_seqno"
         + " from rsk_chgback"
         + " where rowid =?"
   ;
	this.setRowId(1, aRowid);
   daoTid = "A.";
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("資料己不存在, kk[%s]", aRowid);
      return;
   }

//   if (col_num("A.mod_seqno") != vars_num("mod_seqno")) {
//      errmsg("err_otherModify");
//      return;
//   }

}

@Override
public int dataProc() {

   return rc;
}

@Override
public int dbInsert() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbUpdate() {
   msgOK();
//	wp.ddd("rowid[%s], mod_seq[%s], amt[%s]",varsStr("rowid"),varsStr("mod_seqno"),vars_num("disb_dc_amt"));
   dataCheck();
   if (rc != 1)
      return rc;

   //--
   strSql = "update rsk_chgback set"
         + " fst_disb_add_date =?,"
         + " fst_disb_add_user =?,"
         + " fst_disb_dc_amt =?,"
         + " fst_disb_amt =?,"
         + " fst_disb_yn =?,"
           +" dc_dest_amt =decode(curr_code,'901',dest_amt,dc_dest_amt), "
         + commSqlStr.setModxxx(modUser, modPgm)
         + " where 1=1"
         + " and rowid = ? ";
   
   if (imDisbDcamt == 0) {
      setString("");
      setString("");
      setDouble(0);
      setDouble(0);
      setString("N");
   }
   else {
	   setString(this.getSysDate());
	   setString(modUser);
	   setDouble(imDisbDcamt);
	   setDouble(imDisbAmt);
	   setString("Y");
   }
   
   setRowId(kk1);
   
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_chgback error; err[%s]", sqlErrtext);
   }
   return rc;
}

@Override
public int dbDelete() {
   // TODO Auto-generated method stub
   return 0;
}

}
