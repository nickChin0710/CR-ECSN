/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 110-03-18  V1.00.01  tanwei       表拆分修改增刪改查邏輯                                                                       *   
******************************************************************************/
package cmsm03;
/** 
 * 2019-1205:  Alex  add initButton
 * 2019-0614:  JH    p_xxx >>acno_pxxx
 * 109-04-20   shiyuqi       updated for project coding standard     
 * 111-08-17   machao        測試結果調整*
 * */
import ofcapp.BaseAction;

public class Cmsm4120 extends BaseAction {
	String current_code;

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

  @Override
  public void queryFunc() throws Exception {
    if (checkIdno() == false) {
      alertErr2("持卡無流通卡");
      return;
    }

    String lsWhere =
        " where 1=1 " + " and B.major_id_p_seqno in (select id_p_seqno from crd_idno where id_no ='"
            + wp.itemStr("ex_idno") + "')"

    ;

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  boolean checkIdno() {
    String sql1 =
        " select count(*) as db_cnt from crd_card where major_id_p_seqno in (select id_p_seqno from crd_idno where id_no = ? )";
    sqlSelect(sql1, new Object[] {wp.itemStr("ex_idno")});
    if (sqlNum("db_cnt") <= 0)
      return false;
    return true;
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    
    wp.selectSQL = "" + " distinct B.card_no ," + " 0 as adv_bill_code ," 
        + " 0 as adv_airshutt_cnt ," + " 0 as adv_airpark_cnt ," + " 0 as adv_favor_room_cnt ,"
        + " 0 as adv_moore_room_cnt ," + " to_char(sysdate,'yyyymm') cost_month ," 
        + " B.id_p_seqno ," + " B.major_id_p_seqno ," + " B.p_seqno, B.acno_p_seqno," 
        + " B.group_code ," + " uf_tt_group_code(B.group_code) as group_name ," 
        + " uf_idno_name(B.card_no) as chi_name, "
        + " current_code ";
        wp.daoTable = "crd_card B left join mkt_contri_card A on B.card_no =A.card_no "; 
        wp.whereOrder = " order by B.card_no ";

    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    current_code = wp.getValue("current_code");
    queryAfter();
    wp.setPageValue();

  }

  void queryAfter() {
    int tlBill = 0;
    int tlAirshutt = 0;
    int tlAirpark = 0;
    int tlFavorRoom = 0;
    int tlMooreRoom = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_group", wp.colStr(ii, "group_code") + " " + wp.colStr(ii, "group_name"));

      String sql1 =
          /*
           * " select " + " count(*) as db_cnt , " + " sum(nvl(adv_bill_code,0)) as lm_amt , " +
           * " sum(nvl(adv_airshutt_cnt,0)) as lm_amt2 , " +
           * " sum(nvl(adv_airpark_cnt,0)) as lm_amt3 , " +
           * " sum(nvl(adv_favor_room_cnt,0)) as lm_amt4 , " +
           * " sum(nvl(adv_moore_room_cnt,0)) as lm_amt5 " + " from mkt_contri_card " +
           * " where card_no = ? " + " and cost_month = to_char(sysdate,'yyyy')||'00' ";
           */
          " select " + " count(*) as db_cnt , " 
           + " sum(nvl(a.adv_bill_code,0))/4 as lm_amt , "
//          + " a.adv_bill_code as lm_amt , "
          + " sum(decode(b.item_no, '08', nvl(b.adv_cnt,0), 0)) as lm_amt2 , "
          + " sum(decode(b.item_no, '09', nvl(b.adv_cnt,0), 0)) as lm_amt3 , "
          + " sum(decode(b.item_no, '11', nvl(b.adv_cnt,0), 0)) as lm_amt4 , "
          + " sum(decode(b.item_no, '10', nvl(b.adv_cnt,0), 0)) as lm_amt5 "
          + " from mkt_contri_card a, mkt_contri_card_dtl b"
          + " where a.card_no = b.card_no and a.cost_month = b.cost_month  and a.card_no = ? " 
          + " and a.cost_month >= to_char(sysdate,'yyyy')||'01' and a.cost_month <= to_char(sysdate,'yyyy')||'12'"
          + " and b.item_no in('08','09','10','11')";
      
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "card_no")});
      if (sqlNum("db_cnt") == 0) {
        wp.colSet(ii, "action", "A");
        continue;
      }
      wp.colSet(ii, "action", "U");
      wp.colSet(ii, "adv_bill_code", sqlStr("lm_amt"));
      wp.colSet(ii, "adv_airshutt_cnt", sqlStr("lm_amt2"));
      wp.colSet(ii, "adv_airpark_cnt", sqlStr("lm_amt3"));
      wp.colSet(ii, "adv_favor_room_cnt", sqlStr("lm_amt4"));
      wp.colSet(ii, "adv_moore_room_cnt", sqlStr("lm_amt5"));
      tlBill += sqlNum("lm_amt");
      tlAirshutt += sqlNum("lm_amt2");
      tlAirpark += sqlNum("lm_amt3");
      tlFavorRoom += sqlNum("lm_amt4");
      tlMooreRoom += sqlNum("lm_amt5");
    }
    wp.colSet("tl_bill", "" + tlBill);
    wp.colSet("tl_airshutt", "" + tlAirshutt);
    wp.colSet("tl_airpark", "" + tlAirpark);
    wp.colSet("tl_favor_room", "" + tlFavorRoom);
    wp.colSet("tl_moore_room", "" + tlMooreRoom);
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
	if(!wp.getValue("current_code").equals('0')) {	
		alertMsg("唯讀(不可改)");
		return;
	} 	  
    int llOK = 0;
    int llErr = 0;
    cmsm03.Cmsm4120Func func = new cmsm03.Cmsm4120Func();
    func.setConn(wp);
    String[] lsCostMonth = wp.itemBuff("cost_month");
    String[] lsCardNo = wp.itemBuff("card_no");
    String[] lsIdPSeqno = wp.itemBuff("id_p_seqno");
    String[] lsMajorIdPSeqno = wp.itemBuff("major_id_p_seqno");
    String[] lsPSeqno = wp.itemBuff("p_seqno");
    String[] lsAcnoPSeqno = wp.itemBuff("acno_p_seqno");
    String[] lsGroupCode = wp.itemBuff("group_code");
    String[] lsAdvBillCode = wp.itemBuff("adv_bill_code");
    String[] lsAdvAirshuttCnt = wp.itemBuff("adv_airshutt_cnt");
    String[] lsAdvAirparkCnt = wp.itemBuff("adv_airpark_cnt");
    String[] lsAdvFavorRoomCnt = wp.itemBuff("adv_favor_room_cnt");
    String[] lsAdvMooreRoomCnt = wp.itemBuff("adv_moore_room_cnt");
    String[] lsAction = wp.itemBuff("action");

    wp.listCount[0] = lsCardNo.length;
    for (int ii = 0; ii < lsCardNo.length; ii++) {
      func.varsSet("cost_month", lsCostMonth[ii]);
//      String a = lsCardNo[ii];
      func.varsSet("card_no", lsCardNo[ii]);
      func.varsSet("id_p_seqno", lsIdPSeqno[ii]);
      func.varsSet("major_id_p_seqno", lsMajorIdPSeqno[ii]);
      func.varsSet("p_seqno", lsPSeqno[ii]);
      func.varsSet("acno_p_seqno", lsAcnoPSeqno[ii]);
      func.varsSet("group_code", lsGroupCode[ii]);
      func.varsSet("adv_bill_code", lsAdvBillCode[ii]);
//      String b = lsAdvAirshuttCnt[ii];
      func.varsSet("adv_airshutt_cnt", lsAdvAirshuttCnt[ii]);
//      String c = lsAdvAirparkCnt[ii];
      func.varsSet("adv_airpark_cnt", lsAdvAirparkCnt[ii]);
      func.varsSet("adv_favor_room_cnt", lsAdvFavorRoomCnt[ii]);
      func.varsSet("adv_moore_room_cnt", lsAdvMooreRoomCnt[ii]);
      func.varsSet("action", lsAction[ii]);
      if (func.dataProc() == 1)
        llOK++;
      else
        llErr++;
      
      if (llOK > 0)
          sqlCommit(1);
        alertMsg("存檔完成 ; ");
    }


  }

  @Override
  public void initButton() {
    btnModeAud("XX");

  }

  @Override
  public void initPage() {
    wp.colSet("ex_date1", this.getSysDate().substring(0, 4));

  }

}
