/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-05-30  V1.00.00  machao      Initial       
* 112-06-14  V1.00.01  machao      新增bug调整*                                                                                  *    
******************************************************************************/
package mktm02;

import busi.FuncAction;

public class Mktm1017Func extends FuncAction {
	String cardNo = "";
	String idPseqNo = "";
	String idNo ="";
	String chiName = "";
	String acctMonth = "";
	
  @Override
  public void dataCheck() {
	 cardNo = wp.itemStr("ex_card_no");
	 idPseqNo = selectIdNo(wp.itemStr("major_id"));
	 if(empty(idPseqNo)) {
		 errmsg("ID 不存在!");
		 return;
	 }
	 
	 String majorIdPseqno = selectmajor(idPseqNo,cardNo);
    if(empty(majorIdPseqno)) {
    	errmsg("資料錯誤!!非正卡ID");
		 return;
    }else if( !majorIdPseqno.equals(idPseqNo)){
    	errmsg("資料錯誤!!非正卡ID");
		 return;
    }
    
    // 依据card_no查出current_code ,若current_code为空，则对应的card_no也不存在，二者一一对应
    String currentCode = selectCurrCode(cardNo);
    if(empty(currentCode)) {
    	errmsg("卡號不存在!!");
		 return;
    }else {
    	if(!currentCode.equals("0")) {
    		errmsg("非有效卡,不可存檔");
    		return;
    	}
    }
    
    strSql = " select count(*) as tot_cnt from bil_dodo_dtl_temp "
    		+ " where create_date = ? and aud_type = ? and major_id = ? and card_no = ? ";
    Object[] param1 = new Object[] {getSysDate(),wp.itemStr("ex_aud_type"),wp.itemStr("major_id"),cardNo};
    sqlSelect(strSql, param1);
    if (colNum("tot_cnt") > 0) {
      errmsg("本日異動檔交易別、ID、卡號己存在,不可重覆存檔!");
      return;
    } 
    
    acctMonth = getSysDate();
    String actY = acctMonth.substring(0, 4);
    String actM = acctMonth.substring(4, 6);
    String actD = acctMonth.substring(6, 8);
    int mm = Integer.parseInt(actM);
    int dd = Integer.parseInt(actD);
    if(dd<=5) {
    	mm = mm-1;
        actM = String.valueOf(mm);
        acctMonth = actY + "0" + actM;
    }else {
    	acctMonth = actY + actM;
    }
    
  }

 public String selectCurrCode(String cardNo2) {
	  strSql = "select current_code from crd_card where card_no = ?";
		Object[] param1 = new Object[] {cardNo2};
	    sqlSelect(strSql, param1);
	    return colStr("current_code");
}

public String selectmajor(String idPseqNo, String cardNo) {
	strSql = "select major_id_p_seqno from crd_card where id_p_seqno = ? and card_no = ?";
	Object[] param1 = new Object[] {idPseqNo,cardNo};
    sqlSelect(strSql, param1);
    return colStr("major_id_p_seqno");
}

public String selectIdNo( String majorNo) {
	  strSql = "select id_p_seqno, id_no, chi_name from crd_idno where id_no = ? ";
      Object[] param1 = new Object[] {majorNo};
      sqlSelect(strSql, param1);
      idNo = colStr("id_no");
      chiName = colStr("chi_name");
      return colStr("id_p_seqno");
}

@Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {

    return rc;
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

  public int deleteClass() {
    msgOK();
    strSql = "delete bil_dodo_dtl_temp where 1=1 and card_no = ? " ;
    setString(varsStr("card_no"));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete bil_dodo_dtl_temp error !");
    }

    return rc;
  }

  public int insertClass() {
    msgOK();

    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into bil_dodo_dtl_temp ( " + " create_date , " + " create_time , "
        + " mod_time , " + " send_mcht , " + " id_no , " + " chi_name , " + " major_id , "
        + " aud_type , " + " data_from , " + " send_date , " + " id_p_seqno , " + " card_no , "
        + " crt_user , " + " mod_user , " + " mod_pgm , " + " use_month , "  + " acct_month "  + " )"
        + " values ( " + " to_char(sysdate,'yyyymmdd') , " + " to_char(sysdate,'yyyymm') , " + " sysdate , "
        + " 'D' , " + " :id_no , " + " :chi_name , " + " :major_id , "
        + " :aud_type , " + " '1' , " + "  '' , " + " :id_p_seqno , " + " :card_no , "
        + " :crt_user , " + " :mod_user , " + " :mod_pgm , " + " to_char(sysdate,'yyyymm') , " + " :acct_month "  + " ) ";

    setString("id_no",idNo);
    setString("chi_name",chiName);
    setString("major_id", wp.itemStr("major_id"));
    setString("aud_type", wp.itemStr("ex_aud_type"));
    setString("id_p_seqno", idPseqNo);
    setString("card_no", wp.itemStr("ex_card_no"));
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "mktm1017");
    setString("acct_month", acctMonth);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert bil_dodo_dtl_temp error !");
    }
    return rc;
  }

}
