/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 110-03-18  V1.00.01  tanwei       表拆分修改增刪改查邏輯   
* 111-08-17  V1.00.01  machao        測試結果調整                                                                    *   
******************************************************************************/
package cmsm03;
/** 2019-0614:  JH    p_xxx >>acno_pxxx
 * */
import busi.FuncAction;

public class Cmsm4120Func extends FuncAction {

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
    msgOK();
    if (eqIgno(varsStr("action"), "A")) {
      insertCard();
    } else if (eqIgno(varsStr("action"), "U")) {
      updateCard();
    }
    return rc;
  }

  public int insertCard() {
    msgOK();
    /*
     * strSql = "insert into mkt_contri_card (" + " cost_month ," + " card_no ," + " id_p_seqno ," +
     * " major_id_p_seqno ," + " p_seqno ," + " group_code ," + " adv_bill_code ," +
     * " adv_airshutt_cnt ," + " adv_airpark_cnt ," + " adv_favor_room_cnt ," +
     * " adv_moore_room_cnt ," + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno " +
     * " ) values (" + " :cost_month ," + " :card_no ," + " :id_p_seqno ," + " :major_id_p_seqno ,"
     * + " :p_seqno ," + " :group_code ," + " :adv_bill_code ," + " :adv_airshutt_cnt ," +
     * " :adv_airpark_cnt ," + " :adv_favor_room_cnt ," + " :adv_moore_room_cnt ," + " :mod_user ,"
     * + " sysdate ," + " :mod_pgm ," + " '1' " + " )";
     */
    strSql = "insert into mkt_contri_card (" + " cost_month ," + " card_no ," + " id_p_seqno ,"
        + " major_id_p_seqno ," + " p_seqno ," + " group_code ," + " adv_bill_code ,"
        + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno "
        + " ) values (" + " :cost_month ," + " :card_no ," + " :id_p_seqno ,"
        + " :major_id_p_seqno ," + " :p_seqno ," + " :group_code ," + " :adv_bill_code ,"
        + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " '1' " + " )";
    
    var2ParmStr("cost_month");
    var2ParmStr("card_no");
    var2ParmStr("id_p_seqno");
    var2ParmStr("major_id_p_seqno");
    var2ParmStr("p_seqno");
    var2ParmStr("group_code");
    var2ParmNum("adv_bill_code");
    /*
     * var2ParmNum("adv_airshutt_cnt"); var2ParmNum("adv_airpark_cnt");
     * var2ParmNum("adv_favor_room_cnt"); var2ParmNum("adv_moore_room_cnt");
     */
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsm4120");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert mkt_contri_card error !");
    }
    insertCardDtl();
    return rc;
  }
  
  public int insertCardDtl() {
    int itemNo = 8;
    for(int i= 0;i < 4;i++) {
      strSql = "insert into mkt_contri_card_dtl (" + " cost_month ," + " card_no ," + " item_no ,"
          + " amount ," + " cnt ," + " day_cnt ,"
          + " adv_cnt ," + " day_amt ," + " mod_user ," + " mod_time ," + " mod_pgm " 
          + " ) values (" + " :cost_month ," + " :card_no ," + " :item_no ,"
          + " :amount ," + " :cnt ," + " :day_cnt ,"
          + " :adv_cnt ," + " :day_amt ," + " :mod_user ," + "sysdate ," + " :mod_pgm )";
      var2ParmStr("cost_month");
      var2ParmStr("card_no");
      var2ParmStr("item_no");
      setString2("card", wp.itemStr("card_no"));
      setNumber("amount", 0);
      setString2("cnt", "0");
      setString2("day_cnt", "0");
      setString2("day_amt", "0");
      if(itemNo == 8) {
        setString2("item_no", "08");
        setString2("adv_cnt", wp.itemStr("adv_airshutt_cnt"));
      }
      if(itemNo == 9) {
        setString2("item_no", "09");
        setString2("adv_cnt", wp.itemStr("adv_airpark_cnt"));
      }
      if(itemNo == 10) {
        setString2("item_no", "10");
        setString2("adv_cnt", wp.itemStr("adv_moore_room_cnt"));
      }
      if(itemNo == 11) {
        setString2("item_no", "11");
        setString2("adv_cnt", wp.itemStr("adv_favor_room_cnt"));
      }
      itemNo++;
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", "cmsm4120");
      sqlExec(strSql);
      if (sqlRowNum <= 0) {
        errmsg("insert mkt_contri_card_dtl error !");
      }
      }
    return rc;
  }

  public int updateCard() {
    msgOK();
    /*
     * strSql = " update mkt_contri_card set " + " adv_bill_code =:adv_bill_code ," +
     * " adv_airshutt_cnt =:adv_airshutt_cnt ," + " adv_airpark_cnt =:adv_airpark_cnt ," +
     * " adv_favor_room_cnt =:adv_favor_room_cnt ," + " adv_moore_room_cnt =:adv_moore_room_cnt ," +
     * " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ," +
     * " mod_seqno =nvl(mod_seqno,0)+1 " + " where cost_month =:cost_month " +
     * " and card_no =:card_no ";
     */
    strSql = " update mkt_contri_card set " + " adv_bill_code =:adv_bill_code ,"
        + " mod_user =:mod_user ,"
        + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 "
        + " where cost_month =:cost_month " + " and card_no = :card_no ";
    var2ParmNum("adv_bill_code");
    /*
     * var2ParmNum("adv_airshutt_cnt"); var2ParmNum("adv_airpark_cnt");
     * var2ParmNum("adv_favor_room_cnt"); var2ParmNum("adv_moore_room_cnt");
     */
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsm4120");
    var2ParmStr("cost_month");
    var2ParmStr("card_no");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update mkt_contri_card error !");
      return rc;
    }
    updateCardDtl("08");
    updateCardDtl("09");
    updateCardDtl("10");
    updateCardDtl("11");
    return rc;
   
  }
  
  public int updateCardDtl(String itemNo) {
    strSql = " update mkt_contri_card_dtl set " + " cost_month =:cost_month ,"
        + " card_no =:card_no ," + " item_no =:item_no ," + " amount =:amount ,"
        + " cnt =:cnt ," + " day_cnt =:day_cnt ," + " adv_cnt =:adv_cnt ," 
        + " day_amt =:day_amt ," + " mod_user =:mod_user ,"
        + " mod_time =sysdate ," + " mod_pgm =:mod_pgm "
        + " where cost_month =:cost_month " + " and card_no =:card_no "+ " and item_no =:item_no";
    var2ParmStr("cost_month");
    var2ParmStr("card_no");
    if("08".equals(itemNo)) {
    setString2("item_no", "08");
    setString2("adv_cnt", varsStr("adv_airshutt_cnt"));
    }
    if("09".equals(itemNo)) {
      setString2("item_no", "09");
      setString2("adv_cnt", varsStr("adv_airpark_cnt"));
      }
    if("10".equals(itemNo)) {
      setString2("item_no", "10");
      setString2("adv_cnt", varsStr("adv_moore_room_cnt"));
      }
    if("11".equals(itemNo)) {
      setString2("item_no", "11");
      setString2("adv_cnt", varsStr("adv_favor_room_cnt"));
      }
//    var2ParmStr("card_no");
    setNumber("amount", 0);
    setString2("cnt", "0");
    setString2("day_cnt", "0");
    setString2("day_amt", "0");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsm4120");
    sqlExec(strSql);
    if (sqlRowNum <= 0)
      errmsg("update mkt_contri_card_dtl error !");
    return rc;
  }

}
