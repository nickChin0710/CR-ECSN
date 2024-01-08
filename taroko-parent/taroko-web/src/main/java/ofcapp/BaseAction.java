/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei      coding standard      *
*  110-01-08  V1.00.02  tanwei        修改意義不明確變量                                                                          *  
*  110-01-08  V1.00.03  Zuwei      add method item2Col                                                                          *   
******************************************************************************/
package ofcapp;
/** UI版公用程式
 * 2019-1023   JH    getBusi_date()
 * 2019-1014   JH    sql_between()
 * 2019-0826   JH    err_alert2()
 * 2019-0711:  jh    ++itemall_empty([])
 * 2019-0617:  jh    ++sql_between
 * 2019-0612:  jh    getPseqno(ss)
 * 2019-0520:  jh    alert_msg()
   2019-0422:  JH    opt_okflag()
 *
 * */
import taroko.com.TarokoCommon;

@SuppressWarnings({"unchecked", "deprecation"})
public abstract class BaseAction extends BasePage {

  public abstract void userAction() throws Exception;

  public abstract void dddwSelect();

  public abstract void queryFunc() throws Exception;

  public abstract void queryRead() throws Exception;

  public abstract void querySelect() throws Exception;

  public abstract void dataRead() throws Exception;

  public abstract void saveFunc() throws Exception;

  public abstract void procFunc() throws Exception;

  public abstract void initButton();

  public abstract void initPage();

  /* 維護--基本method */
  public void showScreen(TarokoCommon wr) throws Exception {
    wp = wr;
    wp.respHtml = wp.requHtml;
    initPage();
    dddwSelect();
    initButton();
  }

  public void actionFunction(TarokoCommon wr) throws Exception {
    wp = wr;
    rc = 1;
    wp.logActive();
    wp.loginDeptNo = userDeptNo();

    strAction = wp.buttonCode;
    userAction();
    if (pos(strAction.toUpperCase(), "AJAX") >= 0) {
      return;
    }

    dddwSelect();
    initButton();
  }

  // @override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
    queryModeClear();

    initPage();
  }

  public void saveAfter(boolean bRetrieve) throws Exception {

    if (rc != 1) {
      return;
    }
    if (isAdd()) {
      if (bRetrieve)
        dataRead();
      else
        clearFunc();
    } else if (isUpdate()) {
      if (bRetrieve) {
        dataRead();
      } else {
        modSeqnoAdd();
      }
    } else
      clearFunc();
  }

  public void modSeqnoAdd() {
    if (rc == 1) {
      int liSeqno = (int) wp.itemNum("mod_seqno") + 1;
      wp.colSet("mod_seqno", "" + liSeqno);
    }
  }

	public void item2Col(String col) {
		wp.colSet(col,wp.itemStr(col));
	}

  public String itemkk(String col) {
    String ss = wp.itemStr("kk_" + col);
    if (empty(ss) == false) {
      return ss;
    }

    return wp.itemStr(col);
  }

  // protected boolean itemall_empty(String ss) {
  // return itemall_empty(ss.split(","));
  // }
  protected boolean itemAllempty(String col[]) {
    for (int ii = 0; ii < col.length; ii++) {
      if (!wp.itemEmpty(col[ii]))
        return false;
    }
    alertErr2("查詢條件不可空白");
    return true;
  }

  protected boolean itemallEmpty(String strName) {
    return itemallEmpty(strName.split(","));
  }

  protected boolean itemallEmpty(String col[]) {
    for (int ii = 0; ii < col.length; ii++) {
      if (!wp.itemEmpty(col[ii]))
        return false;
    }
    return true;
  }

  protected boolean itemStrend(String col1, String col2) {
    return chkStrend(wp.itemStr2(col1), wp.itemStr2(col2));
  }

  // public void alert_msg(String msg1) {
  // if (rc != 1) {
  // err_alert(msg1);
  // return;
  // }
  //
  // if (isEmpty(msg1)) {
  // if (isAdd()) {
  // wp.respMesg = "資料新增成功";
  // } else if (isUpdate()) {
  // wp.respMesg = "資料存檔成功";
  // } else if (isDelete()) {
  // wp.respMesg = "資料刪除成功";
  // }
  // } else {
  // wp.respMesg = msg1;
  // }
  // }

  public boolean isAdd() {
    return eqIgno(strAction, "A");
  }

  public boolean isUpdate() {
    return eqIgno(strAction, "U");
  }

  public boolean isDelete() {
    return eqIgno(strAction, "D");
  }

  public boolean logQueryIdno(String aDebit, String asKey) {
    if (empty(asKey)) {
      return true;
    }
    ofcapp.QueryLog qlog = new ofcapp.QueryLog(wp);
    if (eqIgno(aDebit, "Y")) {
      if (qlog.authQueryVd(asKey))
        return true;
    } else {
      if (qlog.authQuery(asKey))
        return true;
    }

    errmsg(qlog.getMesg());

    return true;

  }

  public boolean logQueryIdno(String asKey) {
    return logQueryIdno("", asKey);
  }

  protected void optOkflag(int rr) {
    wp.colSet(rr, "ok_flag", "-");
  }

  protected boolean checkAprUser(int num, String col) {
    // -主管,經辦: 不可同一人-
    return wp.itemEq(num, col, wp.loginUser);
  }

  public void keepListOption(int rr, String optCol) {
    for (int ii = 0; ii < rr; ii++) {
      wp.colSet(ii, optCol, wp.itemStr(optCol + "-" + ii));
    }
  }

  public String xxVipColor(String asIdCard) {
    String lsColor = "";
    if (empty(asIdCard)) {
      errmsg("身分證ID, 卡號: 不可空白");
      return lsColor;
    }
    int liLen = asIdCard.trim().length();
    if (liLen != 10 && liLen != 15 && liLen != 16) {
      errmsg("身分證ID, 卡號: 輸入錯誤");
      return lsColor;
    }


    /*
     * //--先恢復背景值--------- backcolor=12632256
     * 
     * 
     * //配合需求單ECS-s930219-014--新增VIP判斷 string ls_id , ls_vip
     * 
     * CHOOSE CASE Len(csm_id)
     * 
     * CASE 10 //--IDNO ls_id = csm_id + '%'
     * 
     * select MAX(decode(vip_code,'6S','4','5S','3','WW','2','4S','1','0')) into :ls_vip from
     * act_acno where acct_key like :ls_id and (vip_code like '%V%' or vip_code like '%W%' or
     * vip_code like '%S%'); IF ls_vip = '0' THEN this.backcolor = RGB(0,208,0) MessageBox("請注意",
     * '此卡友為4S_VIP客戶(V0-V8、WV)',Exclamation!) END IF IF ls_vip = '1' THEN this.backcolor =
     * RGB(0,208,0) MessageBox("請注意", '此卡友為4S_VIP(4S)客戶',Exclamation!) END IF IF ls_vip = '2' THEN
     * this.backcolor = RGB(185,115,255) MessageBox("請注意", '此卡友為5S_VIP(WW)客戶',Exclamation!) END IF
     * IF ls_vip = '3' THEN this.backcolor = RGB(185,115,255) MessageBox("請注意",
     * '此卡友為5S_VIP(5S)客戶',Exclamation!) END IF IF ls_vip = '4' THEN this.backcolor = RGB(255,255,0)
     * MessageBox("請注意", '此卡友為6S_VIP(6S)客戶',Exclamation!) END IF return 1
     * 
     * CASE 16 //--cardno ls_id = csm_id
     * 
     * select MAX(decode(vip_code,'6S','4','5S','3','WW','2','4S','1','0')) into :ls_vip from
     * act_acno where (vip_code like '%V%' or vip_code like '%W%' or vip_code like '%S%') and
     * acct_key in (select acct_key from crd_card where card_no like :ls_id); IF ls_vip = '0' THEN
     * this.backcolor = RGB(0,208,0) MessageBox("請注意", '此卡友為4S_VIP客戶(V0-V8、WV)',Exclamation!) END IF
     * IF ls_vip = '1' THEN this.backcolor = RGB(0,208,0) MessageBox("請注意",
     * '此卡友為4S_VIP(4S)客戶',Exclamation!) END IF IF ls_vip = '2' THEN this.backcolor =
     * RGB(185,115,255) MessageBox("請注意", '此卡友為5S_VIP(WW)客戶',Exclamation!) END IF IF ls_vip = '3'
     * THEN this.backcolor = RGB(185,115,255) MessageBox("請注意", '此卡友為5S_VIP(5S)客戶',Exclamation!) END
     * IF IF ls_vip = '4' THEN this.backcolor = RGB(255,255,0) MessageBox("請注意",
     * '此卡友為6S_VIP(6S)客戶',Exclamation!) END IF return 1
     * 
     * CASE 15 //--cardno for AE ls_id = csm_id
     * 
     * select MAX(decode(vip_code,'6S','4','5S','3','WW','2','4S','1','0')) into :ls_vip from
     * act_acno where (vip_code like '%V%' or vip_code like '%W%' or vip_code like '%S%') and
     * acct_key in (select acct_key from crd_card where card_no like :ls_id); IF ls_vip = '0' THEN
     * this.backcolor = RGB(0,208,0) MessageBox("請注意", '此卡友為4S_VIP客戶(V0-V8、WV)',Exclamation!) END IF
     * IF ls_vip = '1' THEN this.backcolor = RGB(0,208,0) MessageBox("請注意",
     * '此卡友為4S_VIP(4S)客戶',Exclamation!) END IF IF ls_vip = '2' THEN this.backcolor =
     * RGB(185,115,255) MessageBox("請注意", '此卡友為5S_VIP(WW)客戶',Exclamation!) END IF IF ls_vip = '3'
     * THEN this.backcolor = RGB(185,115,255) MessageBox("請注意", '此卡友為5S_VIP(5S)客戶',Exclamation!) END
     * IF IF ls_vip = '4' THEN this.backcolor = RGB(255,255,0) MessageBox("請注意",
     * '此卡友為6S_VIP(6S)客戶',Exclamation!) END IF return 1
     * 
     * CASE ELSE
     * 
     * MessageBox("對不起", '卡號 or 身份證字號 輸入錯誤~',Exclamation!) Return -1
     * 
     * END CHOOSE
     */
    return "";
  }

  protected String busiDate() {
    String sql1 = "select business_date from ptr_businday" + " where 1=1" + commSqlStr.rownum(1);
    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      return "";
    }
    return sqlStr("business_date");
  }

  protected String acctPseqno(String akk) {
    if (empty(akk) || (akk.length() != 10 && akk.length() != 16)) {
      return "";
    }
    String sql1 = "select p_seqno from act_acno where acno_p_seqno =?";

    if (akk.length() == 16) {
      sql1 = "select p_seqno from crd_card where crd_card =?";
    }
    this.sqlSelect(sql1, new Object[] {akk});
    return this.sqlStr("p_seqno");
  }

  protected String idPseqno(String lsIdno) {
    if (empty(lsIdno))
      return "";
    String sql1 = "select id_p_seqno from crd_idno" + " where id_no =?" + sqlRownum(1);
    this.sqlSelect(sql1, new Object[] {lsIdno});
    return this.sqlStr("id_p_seqno");
  }

  protected String idPseqnoVd(String lsIdno) {
    if (empty(lsIdno))
      return "";
    String sql1 = "select id_p_seqno from dbc_idno" + " where id_no =?" + sqlRownum(1);
    this.sqlSelect(sql1, new Object[] {lsIdno});
    return this.sqlStr("id_p_seqno");
  }

  protected void okMsg(String msg1) {
    rc = 1;
    wp.respMesg = msg1;

  }

  protected void alertErr2(String msg1, Object... objs) {
    alertErr2(commString.formatSqlString(msg1, objs));
  }

  protected void okAlert(String msg1) {
    rc = 1;
    wp.respMesg = msg1;
    wp.alertMesg(msg1);
  }

  protected void alertMsg(String msg1) {
    if (rc == 1) {
      if (empty(msg1)) {
        if (isAdd()) {
          wp.respMesg = "資料新增成功";
        } else if (isUpdate()) {
          wp.respMesg = "資料存檔成功";
        } else if (isDelete()) {
          wp.respMesg = "資料刪除成功";
        }
      } else {
        wp.respMesg = msg1;
      }
      // wp.alertMesg(wp.respMesg);
    } else {
      alertErr2(msg1);
    }
  }

  protected void alertMsg(String msg1, boolean bShow) {
    if (rc == 1) {
      if (empty(msg1)) {
        if (isAdd()) {
          wp.respMesg = "資料新增成功";
        } else if (isUpdate()) {
          wp.respMesg = "資料存檔成功";
        } else if (isDelete()) {
          wp.respMesg = "資料刪除成功";
        }
      } else {
        wp.respMesg = msg1;
      }
      if (bShow)
        wp.alertMesg(wp.respMesg);
    } else {
      alertErr2(msg1);
    }
  }

  protected String sqlBetween(String parm1, String parm2, String col) {
    if (eqAny(wp.itemStr2(parm1), wp.itemStr2(parm2))) {
      return sqlCol(wp.itemStr2(parm1), col);
    }

    return sqlCol(wp.itemStr2(parm1), col, ">=") + sqlCol(wp.itemStr2(parm2), col, "<=");
  }

  protected String sqlLocate(String col, String s2) {
    // , locate(spec_case,',01,08') as aaa1
    if (empty(col) || empty(s2))
      return "";
    return " and locate(" + col + ",'|" + s2 + "')>1 and " + col + "<>''";
  }
  // protected String sql_inString(String col,String s1) {
  // //, locate_in_string(',01,08',spec_case) as aaa_2
  // if (empty(col) || empty(s1))
  // return "";
  // return " and locate_in_string('"+s1+"',"+col+")>1 and "+col+"<>''";
  // }
  
  protected void defaultAction() throws Exception{
		switch (wp.buttonCode) {
			case "X": //轉換顯示畫面
				strAction = "new";
				clearFunc();
				break;
			case "Q": //查詢功能
				queryFunc();
				break;
			case "R": // -資料讀取-
				dataRead();
				break;
			case "A": //新增功能
			case "U": //更新功能
			case "D": //刪除功能
				saveFunc(); break;
			case "M": //瀏覽功能 :skip-page-
				queryRead();
				break;
			case "S": //動態查詢--
				querySelect();
				break;
			case "L": //清畫面--
				strAction = "";
				clearFunc();
				break;
			case "C": // -資料處理-
				procFunc();
				break;
		}
	}
  
}
