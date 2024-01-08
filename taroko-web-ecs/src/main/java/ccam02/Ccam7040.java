package ccam02;
/**
 * 2019-1210:  Alex  add initButton
 * 2019-1017   JH    callBatch: CrdC010
 * 2019-0612:    JH    p_xxx >>acno_p_xxx
 * 2020-0326：   	YH     update new_card_no(select)
 * 2020-0409： Wilson  post_flag = 'Y'
 * 2020-0420  V1.00.01 yanghan 修改了變量名稱和方法名稱
 * 2020-0730  V1.00.02 tanwei 緊急替代卡卡號驗證
 * 2020-0806  V1.00.03 tanwei callBatch方法添加邏輯判斷
 * 2020-1022  V1.00.04 Wilson 取消callBatch，改在shell執行
 */

import busi.SqlPrepare;
import busi.ecs.CommFunction;
import ofcapp.BaseAction;
public class Ccam7040 extends BaseAction {
	
@Override
public void userAction() throws Exception {
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
   } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
   } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
   } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
   } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
   } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
   } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
   } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
   }

}

@Override
public void dddwSelect() {
	// TODO Auto-generated method stub
	
}
	//動態查詢可用亂碼
	public void selectCardno() {
		// String ls_card_no = wp.col_ss("card_no").substring(0,6);
		String lsGroupCode = wp.colStr("group_code");
		String lsCardType = wp.colStr("card_type");
		CommFunction comm = new CommFunction();
			if (eqIgno(wp.respHtml, "ccam7040")) {
				if (empty(lsGroupCode) || empty(lsCardType))
					return;
				}
	}

	//動態添加Option
	public void addOption(String[] newCardNo, int total) {
		StringBuilder sbOption = new StringBuilder();
		for (int i = 0; i < total; i++) {
			sbOption.append("<option value='").append(newCardNo[i]).append("'>").append(newCardNo[i])
					.append("</option> ");
		}
		// 寫入
		String lsOption = sbOption.toString();
		wp.setValue("new_card_no", lsOption, 0);
	}

	@Override
public void queryFunc() throws Exception {

//   if (checkCurrentCode() == false) {
//      err_alert("此卡尚未停掛，不能作業");
//      return;
//   }
//
//   String ls_where = " where 1=1 and A.acno_p_seqno=C.acno_p_seqno and B.id_p_seqno=C.id_p_seqno "
//         + sql_col(wp.item_ss("ex_card_no"), "C.card_no");
//
//
//   wp.whereStr = ls_where;
//   wp.queryWhere = wp.whereStr;
//   wp.setQueryMode();
//
//   queryRead();

}

boolean checkCurrentCode() {
   String sql1 = " select "
         + " count(*) as db_cnt "
         + " from crd_card "
         + " where card_no = ? "
         + " and current_code = '0' ";

   sqlSelect(sql1, new Object[]{wp.itemStr("ex_card_no")});

   if (sqlNum("db_cnt") > 0) return false;

   return true;
}

@Override
public void queryRead() throws Exception {
//   wp.pageControl();
//
//   wp.selectSQL = ""
//         + " C.card_no , "
//         + " C.card_type , "
//         + " C.acct_type , "
//         + " B.chi_name , "
//         + " C.eng_name , "
//         + " B.id_no , "
//         + " B.birthday , "
//         + " A.bill_sending_zip||A.bill_sending_addr1||A.bill_sending_addr2||A.bill_sending_addr3||A.bill_sending_addr4||A.bill_sending_addr5 as bill_address , "
//         + " B.home_area_code1||'-'||B.home_tel_no1||'-'||B.home_tel_ext1 as tel_no_h , "
//         + " B.office_area_code1||'-'||B.office_tel_no1||'-'||B.office_tel_ext1 as tel_no_o , "
//         + " B.home_area_code2||'-'||B.home_tel_no2||'-'||B.home_tel_ext2 as tel_no_h2 , "
//         + " B.office_area_code2||'-'||B.office_tel_no2||'-'||B.office_tel_ext2 as tel_no_o2 , "
//         + " B.cellar_phone , "
//         + " C.new_beg_date , "
//         + " C.new_end_date , "
//         + " C.current_code , "
//         + " C.oppost_date , "
//         + " C.id_p_seqno , "
//         + " C.acno_p_seqno , "
//         + " C.corp_p_seqno ,"
//         + " C.group_code "
//   ;
//   wp.daoTable = "crd_idno B, act_acno A, crd_card C";
//   pageQuery();
//
//   if (sql_nrow <= 0) {
//      err_alert("此條件查無資料");
//      return;
//   }
//   queryAfter();
}

@Override
public void querySelect() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void dataRead() throws Exception {
   if (checkCurrentCode() == false) {
      alertErr2("此卡尚未停掛，不能作業");
      return;
   }

   wp.sqlCmd = "select "
         + " C.card_no , "
         + " C.card_type , "
         + " C.acct_type , "
         + " B.chi_name , "
         + " C.eng_name , "
         + " B.id_no , "
         + " B.birthday , "
         + " A.bill_sending_zip||A.bill_sending_addr1||A.bill_sending_addr2||A.bill_sending_addr3||A.bill_sending_addr4||A.bill_sending_addr5 as bill_address , "
         + " B.home_area_code1||'-'||B.home_tel_no1||'-'||B.home_tel_ext1 as tel_no_h , "
         + " B.office_area_code1||'-'||B.office_tel_no1||'-'||B.office_tel_ext1 as tel_no_o , "
         + " B.home_area_code2||'-'||B.home_tel_no2||'-'||B.home_tel_ext2 as tel_no_h2 , "
         + " B.office_area_code2||'-'||B.office_tel_no2||'-'||B.office_tel_ext2 as tel_no_o2 , "
         + " B.cellar_phone , "
         + " C.new_beg_date , "
         + " C.new_end_date , "
         + " C.current_code , "
         + " C.oppost_date , "
         + " C.id_p_seqno , "
         + " C.acno_p_seqno , "
         + " C.corp_p_seqno ,"
         + " C.group_code, "
         + "C.unit_code"
         +" from crd_card C join act_acno A on A.acno_p_seqno=C.acno_p_seqno"
         +" join crd_idno B on B.id_p_seqno=C.id_p_seqno"
         +" where C.card_no =?"
         ;
   setString2(1,wp.itemStr2("ex_card_no"));
   pageSelect();
   if (sqlRowNum <=0) {
      alertErr2("此條件查無資料");
      return;
   }
//   dddw_select();
   selectCardno();
}

@Override
public void saveFunc() throws Exception {
   Ccam7040Func func = new Ccam7040Func();
   func.setConn(wp); 
   rc = func.dbSave(strAction); 
   if (rc != 1) { 
     errmsg(func.getMsg()); 
     return; 
   } 
   sqlCommit(rc);
   this.saveAfter(false);
    
//   callBatch();
}

private void callBatch() throws Exception {
   ecsfunc.EcsCallbatch ooCall =new ecsfunc.EcsCallbatch(wp);
   rc =ooCall.callBatch("CrdC010");
   if (rc ==1) {
      rc = 1;
      alertMsg("callBatch OK;CrdC010執行成功，請至ptrm0777畫面查詢執行結果，Batch-seqno="+ooCall.batchSeqno());
   }else {
	  alertErr2("callBatch error; "+ooCall.getMesg()+";請至secp1010 (線上執行批次處理) 重新執行CrdC010");
   }
}
@Override
public void procFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void initButton() {
   btnModeAud("XX");
   boolean aa = wp.autApprove();
   if(wp.autUpdate()){
   	if(wp.colEmpty("card_no")==false){
   		this.btnOnAud(true, false, false);
   	}	else	{
   		this.btnOnAud(false, false, false);
   	}
   }

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}


}
