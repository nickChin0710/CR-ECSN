package rskm01;
/**
 * 2020-1117   JH    prb_status>=30, no-change
 * 2019-1210   JH    UAT
 */
public class Rskm0010Func extends busi.FuncEdit {
busi.CommCurr zzCurr = new busi.CommCurr();

String isCtrlSeqno = "", isReferNo = "";   //ctrl_seqno,bin_type
boolean ibDebitFlag = false;

@Override
public int querySelect() {
   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

@Override
public int dataSelect() {
   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

@Override
public void dataCheck() {
   ibDebitFlag = eqAny(wp.itemStr("debit_flag"), "Y");
   isReferNo = wp.itemStr("reference_no");
   if (empty(isReferNo)) {
      errmsg("帳單參考序號: 不可空白");
      return;
   }
   
   if(ibDebitFlag == false && wp.itemEq("back_flag", "Y")) {
	   errmsg("信用卡不可勾選回存");
	   return ;
   }
   
   isCtrlSeqno = wp.colStr("ctrl_seqno");
//	kk2 =wp.colStr("bin_type");
   if (isAdd())
      checkInsert();
   else if (isUpdate())
      checkUpdate();
   else if (isDelete())
      checkDelete();
   else errmsg("Action 不是A,U,D");
   return;

}

void checkInsert() {
   selectBilBill();
   if (rc != 1) {
      return;
   }

   //wp.ddd("prb_amt=%s, dc_desc_amt=%s",wp.itemStr("dc_prb_amount"),col_ss("B.dc_dest_amt"));
   if (wp.itemNum("dc_prb_amount") > colNum("B.dc_dest_amt")) {
      errmsg("結算問題金額不合理");
      return;
   }

   //-OK-
   double lm_prb_amt = zzCurr.dc2twAmt(colNum("B.dest_amt"), colNum("B.dc_dest_amt"), wp.itemNum("dc_prb_amount"));
   wp.itemSet("prb_amount", "" + lm_prb_amt);
}

void checkUpdate() {
   if (empty(isCtrlSeqno)) {
      errmsg("控制流水號號: 不可空白");
      return;
   }
   sqlWhere = " where 1=1" ;
   
   int ii=0;
   if(empty(isCtrlSeqno) == false) {
	   ii++;
	   sqlWhere += " and ctrl_seqno = ? ";
	   setString(ii,isCtrlSeqno);
   }
   
   if(empty(isReferNo) == false) {
	   ii++;
	   sqlWhere += " and reference_no = ? ";
	   setString(ii,isReferNo);
   }
   
   if(wp.itemEmpty("mod_seqno") == false) {
	   ii++;
	   sqlWhere += " and nvl(mod_seqno,0) = ? ";
	   setString(ii,wp.itemStr("mod_seqno"));
   }
      
   //-已被異動-
   if (isOtherModify("rsk_problem", sqlWhere))
      return;

   selectBilBill();
   if (rc != 1) {
      return;
   }
   
   selectRskProblem();
   if (rc != 1) {
      return;
   }

   int liStatus =colInt("A.prb_status");
   if (liStatus>=30) {
      errmsg("此狀態下不可修改!!! [%s]", liStatus);
      return;
   }
//   if (col_ss("A.prb_status").compareTo("60") >= 0) {
//      errmsg("此狀態下不可修改");
//      return;
//   }

   if (wp.itemNum("dc_prb_amount") > colNum("B.dc_dest_amt")) {
      errmsg("結算問題金額不合理");
      return;
   }
   
   if(wp.itemEmpty("back_date") == false && wp.itemEq("back_flag", "Y")) {
	   errmsg("已執行回存 , 不可取消");
	   return ;
   }
   
   //-OK-
   double lmPrbAmt = zzCurr.dc2twAmt(colNum("B.dest_amt"), colNum("B.dc_dest_amt"), wp.itemNum("dc_prb_amount"));
   wp.itemSet("prb_amount", "" + lmPrbAmt);      
   
   updateBefore();
}

void checkDelete() {
   if (empty(isCtrlSeqno)) {
      errmsg("控制流水號號: 不可空白");
      return;
   }
   sqlWhere = " where 1=1";
   
   if(empty(isCtrlSeqno) == false) {
	   sqlWhere += " and ctrl_seqno = ? ";
	   setString(isCtrlSeqno);
   }
   
   if(empty(isReferNo) == false) {
	   sqlWhere += " and reference_no = ? ";
	   setString(isReferNo);
   }
   
   if(wp.itemEmpty("mod_seqno") == false) {
	   sqlWhere += " and nvl(mod_seqno,0) = ? ";
	   setString(wp.itemStr("mod_seqno"));
   }
   
   if (isOtherModify("rsk_problem", sqlWhere))
      return;

   selectBilBill();
   if (rc != 1) {
      return;
   }
   selectRskProblem();
   if (rc != 1) {
      return;
   }

   if (colEq("A.prb_status", "10") == false) {
      errmsg("狀態[10]才可刪除");
      return;
   }

	/*
IF dw_data.object.ctrl_seqno[1]='' THEN
   MessageBox("Msg","請輸入控制流水號？！")
   return -1
END IF
//-JH(9309)-check 可D數, ACT_ACAJ-------------------------------------------------
if is_action='D' then Return 1

//-JH(941003)-all check 可D數-----------------
if wf_chk_d_validable_amt()<>1 then Return -1

Return 1	
	 */
   //-OK-
   updateBefore();

}

void selectBilBill() {
   daoTid = "B.";
   if (ibDebitFlag) {
      strSql = "select rsk_ctrl_seqno  "
            + ", dest_amt as dest_amt  "
            + ", dest_amt as dc_dest_amt  "
            + " from dbb_bill B"
            + " where B.reference_no =?";
   }
   else {
      strSql = "select rsk_ctrl_seqno "
            + ", dest_amt "
            + ", decode(dc_dest_amt,0,dest_amt,dc_dest_amt) as dc_dest_amt"
            + " from bil_bill"
            + " where reference_no =?";
   }

   sqlSelect(strSql, new Object[]{
         wp.itemStr("reference_no")
   });
   if (sqlRowNum <= 0) {
      errmsg("select BIL_BILL no-find");
   }

}

void selectRskProblem() {
   daoTid = "A.";
   strSql = "select prb_status"
         + " from rsk_problem"
         + " where ctrl_seqno =?"
         + " and reference_no =?"
   ;

   sqlSelect(strSql, new Object[]{
         isCtrlSeqno, isReferNo
   });
   if (sqlRowNum <= 0) {
      errmsg("rsk_problem not find");
   }

}

void updateBefore() {
   colSet("prb_status", colStr("A.prb_status"));
   if (isUpdate()) {
      if (this.colPos("|30|50", "A.prb_status") > 0) {
         wp.itemSet("prb_status", "40");
      }
   }
   else if (isAdd()) {
      wp.itemSet("prb_status", "10");
   }
   else if (isDelete()) {
      wp.itemSet("prb_status", "00");
   }
   wp.itemSet("rsk_problem1_mark", wp.itemStr("prb_mark") + colStr("prb_status"));

}

@Override
public int dbInsert() {
   actionInit("A");
   isCtrlSeqno = wp.itemStr("ctrl_seqno");

   dataCheck();
   if (rc != 1) {
      return rc;
   }

   RskProblem ooprbl = new RskProblem();
   ooprbl.setConn(wp);
   rc = ooprbl.rskm0010Insert();
   if (rc != 1) {
      errmsg(ooprbl.getMsg());
      return rc;
   }

   //-update bil_bill-
   isCtrlSeqno = ooprbl.isCtrlSeqno;
   updateBilBill();
   if (rc != 1)
      return rc;

   wp.colSet("ctrl_seqno", ooprbl.isCtrlSeqno);
   return rc;
}

void updateBilBill() {
   if (ibDebitFlag) {
      strSql = "update dbb_bill set"
            + " rsk_post =decode(rsk_post,'','O',rsk_post)"
            + ", rsk_ctrl_seqno =?  "
            + " where reference_no =?"
      ;
   }
   else {
      strSql = "update bil_bill set"
            + " rsk_post =decode(rsk_post,'','O',rsk_post)"
            + ", rsk_ctrl_seqno =?  "
            + " where reference_no =?"
      ;
   }
   this.sqlExec(strSql, new Object[]{isCtrlSeqno, wp.itemStr("reference_no")});
   if (sqlRowNum != 1) {
      errmsg("update bil[dbb]_bill error; " + sqlErrtext);
   }
}

@Override
public int dbUpdate() {
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   RskProblem ooprbl = new RskProblem();
   ooprbl.setConn(wp);
   if (ooprbl.rskm0010Update() != 1) {
      errmsg(ooprbl.getMsg());
      return rc;
   }

   return rc;
}

@Override
public int dbDelete() {
   actionInit("D");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   RskProblem ooprbl = new RskProblem();
   ooprbl.setConn(wp);
   if (ooprbl.rskm0010Delete() != 1) {
      errmsg(ooprbl.getMsg());
      return rc;
   }

   return rc;
}

}
