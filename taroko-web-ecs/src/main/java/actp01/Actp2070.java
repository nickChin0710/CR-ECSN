/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 107-01-11  V1.00.01  yash       program initial                            *
 * 111/10/23  V1.00.02  jiangyigndong  updated for project coding standard    *
 ******************************************************************************/

package actp01;


import java.util.Arrays;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Actp2070 extends BaseProc {


  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
      //insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      //updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      //deleteFunc();
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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 執行 */
      strAction = "S2";
      dataProcess();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    //wp.whereStr = " where 1=1   ";
    wp.whereStr = " where 1=1  and act_acaj.adjust_type = 'AI01' and act_acaj.p_seqno = act_acno.p_seqno "
            + " and act_acno.acno_flag != 'Y' ";

    if(empty(wp.itemStr("ex_ackey"))==false){
      wp.whereStr  += " and  act_acno.acct_key like :ex_ackey ";
      setString("ex_ackey", wp.itemStr("ex_ackey")+"%");

    }

    if(empty(wp.itemStr("ex_confirm"))==false){

      if(wp.itemStr("ex_confirm").equals("1")){

        wp.whereStr  += " and  act_acaj.apr_flag <> 'Y' ";

      }else if(wp.itemStr("ex_confirm").equals("2")){

        wp.whereStr  += " and  act_acaj.apr_flag = 'Y' ";

      }
    }

    if(empty(wp.itemStr("ex_crtuser"))==false){
      wp.whereStr  += " and  act_acaj.crt_user = :ex_crtuser ";
      setString("ex_crtuser", wp.itemStr("ex_crtuser"));

    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if(!getWhereStr()){
      return;
    };
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(act_acaj.rowid) as rowid,act_acaj.mod_seqno ,"
            + "act_acaj.p_seqno, "
            + "act_acaj.acct_type, "
            + "act_acno.acct_key, "
            + "act_acaj.acct_type||'-'||act_acno.acct_key as wk_acctkey, "
            + "decode(curr_code,'','901',curr_code) curr_code,"
            + "uf_dc_amt(curr_code,orginal_amt,dc_orginal_amt) orginal_amt, "
            + "uf_dc_amt(curr_code,bef_amt,dc_bef_amt) bef_amt, "
            + "uf_dc_amt(curr_code,bef_d_amt,dc_bef_d_amt) bef_d_amt,  "
            + "uf_dc_amt(curr_code,aft_amt,dc_aft_amt) aft_amt, "
            + "uf_dc_amt(curr_code,aft_d_amt,dc_aft_d_amt) aft_d_amt,"
            + "uf_dc_amt(curr_code,dr_amt,dc_dr_amt) dr_amt, "
            + "uf_dc_amt(curr_code,cr_amt,dc_cr_amt) cr_amt, "
            + "act_acaj.post_date,  "
            + "act_acaj.crt_user,  "
            + "act_acaj.crt_date,  "
            + "act_acaj.apr_flag,"
            + "act_acaj.jrnl_date,"
            + "act_acaj.jrnl_time,"
            + "act_acaj.adjust_type,"
            + "act_acaj.crt_time,"
            + "lpad(' ', 20) trans_cname,"
            + "uf_acno_name(act_acaj.p_seqno) cname";

    wp.daoTable = "act_acaj,act_acno  ";
    wp.whereOrder = "order by act_acaj.p_seqno ";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
    apprDisabled("crt_user");
  }

  void listWkdata() throws Exception {

  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_pp_card_no = wp.item_ss("pp_card_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {

    String[] aaOpt = wp.itemBuff("opt");
    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] aaPSeqno = wp.itemBuff("p_seqno");
    String[] aaAprFlag = wp.itemBuff("apr_flag");

    wp.listCount[0] = aaPSeqno.length;
    int llOk = 0, llErr = 0;


    //-check duplication-
    for (int ll = 0; ll < aaPSeqno.length; ll++) {
      wp.colSet(ll, "ok_flag", "");

      //-option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {

        if (ll == Arrays.asList(aaPSeqno).indexOf(aaPSeqno[ll])) {
          if(ll==0){
            continue;
          }
        }

      }

    }

    if (llErr > 0) {
      alertErr("請勾選 同帳號之所有相關調整");
      return;
    }


    String lsConf ="";
    //-update-
    for (int ll = 0; ll < aaPSeqno.length; ll++) {
      if (checkBoxOptOn(ll, aaOpt)) {
        if(aaAprFlag[ll].equals("Y")){
          lsConf="N";
        }else{
          lsConf="Y";
        }

        String sqlUpdate = " Update act_acaj set "
                + " apr_flag = :apr_flag "
                + " ,mod_user =:mod_user "
                + " ,mod_time =sysdate "
                + " ,mod_pgm =:mod_pgm "
                + " ,mod_seqno =nvl(mod_seqno,0)+1 "
                + " where hex(rowid) = :rowid  and mod_seqno=:mod_seqno ";
        setString("apr_flag", lsConf);
        setString("mod_user", wp.loginUser);
        setString("mod_pgm", wp.itemStr("mod_pgm"));
        setString("rowid", aaRowid[ll]);
        setString("mod_seqno", aaModSeqno[ll]);
        sqlExec(sqlUpdate);
        if (sqlRowNum <= 0) {
          sqlCommit(0);
          llErr++;
          wp.colSet(ll, "ok_flag", "!");
        }else{
          sqlCommit(1);
          llOk++;
          wp.colSet(ll, "ok_flag", "V");
        }

      }


    }



    //alert_msg("處理: 成功筆數=" + ll_ok + "; 失敗筆數=" + ll_err + ";" );
    alertMsg("執行完成, 成功:"+llOk+" 失敗:"+llErr);

  }


  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {

      wp.initOption="--";
      wp.optionKey = wp.itemStr("ex_crtuser");
      this.dddwList("dddw_sec_user", "sec_user", "usr_id", "usr_cname", "where 1=1 order by usr_id");
    } catch (Exception ex) {
    }
  }



}
