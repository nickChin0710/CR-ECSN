/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 107-01-15  V1.00.00  ryan       program initial                            *
 * 109-04-06  V2.00.01  ryan       add f_auth_query()                         *
 * 109-08-12  V2.00.02  Amber      Update Mantis:0003922						 *
 * 111/10/23  V2.00.03  jiangyigndong  updated for project coding standard    *
 ******************************************************************************/
package actp01;


import busi.func.ColFunc;
import ecsfunc.EcsCallbatch;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Actp7000 extends BaseProc {
  int rr = -1;
  String msg = "";
  String kk1 = "",kk2="";
  int ilOk = 0;
  int ilErr = 0;
  String mProgName = "actp7000";
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
      // } else if (itemStr(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (itemStr(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (itemStr(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
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
      /*call_batch */
      fCallBatch();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {

  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.itemStr("ex_actype");
      dddwList("dddw_ex_actype","ptr_acct_type"
              ,"acct_type","chin_name","where 1=1 order by acct_type");

    } catch (Exception ex) {
    }
  }

  //for query use only
  private boolean getWhereStr() throws Exception {

    wp.whereStr = " where 1=1 and ecs_simp_main.p_seqno = ecs_act_acno.p_seqno "
    //+ " and ecs_act_acno.acno_flag != 'Y' "
    ;

    if (empty(wp.itemStr("ex_actype")) == false) {
      wp.whereStr += " and ecs_simp_main.acct_type = :ex_actype ";
      setString("ex_actype", wp.itemStr("ex_actype"));
    }
    if (empty(wp.itemStr("ex_key")) == false) {
      wp.whereStr += " and ecs_act_acno.acct_key = :ex_key ";
      setString("ex_key", wp.itemStr("ex_key"));
    }

    //查詢權限檢查，參考【f_auth_query】
    String ls_id = "";
    ls_id = wp.itemStr("ex_key");

    ColFunc func =new ColFunc();
    func.setConn(wp);
    if (func.fAuthQuery(mProgName, ls_id)!=1)
    { alertErr2(func.getMsg()); return false;}

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.sqlCmd = "select hex(a.rowid) as rowid "
            +" ,a.p_seqno "
            +" ,a.acct_type "
            +" ,b.acct_key "
            +" ,a.id_p_seqno "
            +" ,UF_IDNO_ID(a.id_p_seqno) as id_no"
            +" ,a.out_of_months "
            +" ,a.settle_months "
            +" ,a.proc_date "
            +" ,a.proc_mark "
            +" ,a.return_date "
            +" ,a.apr_user as apr_user1 "
            +" ,a.apr_date "
            +" ,a.crt_user "
            +" ,a.crt_date "
            +" ,a.mod_user "
            +" ,a.mod_time "
            +" ,a.mod_pgm "
            +" ,a.mod_seqno "
            +" ,a.simp_date "
            +" ,a.return_flag "
            +" ,'N' db_return_mark "
            + "from ecs_simp_main a,ecs_act_acno b "
            + "where 1=1 "
            + "and a.p_seqno = b.p_seqno "
    ;
    if (empty(wp.itemStr("ex_actype")) == false) {
      wp.sqlCmd += " and a.acct_type = :ex_actype ";
      setString("ex_actype", wp.itemStr("ex_actype"));
    }
    if (empty(wp.itemStr("ex_key")) == false) {
      wp.sqlCmd += " and b.acct_key = :ex_key ";
      setString("ex_key", wp.itemStr("ex_key"));
    }
    wp.sqlCmd += "union "
            + " select hex(c.rowid) as rowid "
            +" ,c.p_seqno "
            +" ,c.acct_type "
            +" ,d.acct_key "
            +" ,c.id_p_seqno "
            +" ,UF_IDNO_ID(c.id_p_seqno) as id_no"
            +" ,c.out_of_months "
            +" ,c.settle_months "
            +" ,c.proc_date "
            +" ,c.proc_mark "
            +" ,c.return_date "
            +" ,c.apr_user as apr_user1 "
            +" ,c.apr_date "
            +" ,c.crt_user "
            +" ,c.crt_date "
            +" ,c.mod_user "
            +" ,c.mod_time "
            +" ,c.mod_pgm "
            +" ,c.mod_seqno "
            +" ,c.simp_date "
            +" ,c.return_flag "
            +" ,'N' db_return_mark "
            + "from ecs_simp_main c,act_acno d "
            + "where 1=1 "
            + "and c.p_seqno = d.p_seqno ";
    if (empty(wp.itemStr("ex_actype")) == false) {
      wp.sqlCmd += " and c.acct_type = :ex_actype ";
      setString("ex_actype", wp.itemStr("ex_actype"));
    }
    if (empty(wp.itemStr("ex_key")) == false) {
      wp.sqlCmd += " and d.acct_key = :ex_key ";
      setString("ex_key", wp.itemStr("ex_key"));
    }

    if(getWhereStr()==false)
      return;

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    //wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata(wp.selectCnt);

  }

  void listWkdata(int selectCnt) throws Exception {
    String ls_return_flag = "";
    int ll_dtl=0;
    for (int ii = 0; ii < selectCnt; ii++) {
      String sql_select=" select count(*) ll_cnt "
              + " from ecs_simp_return "
              + " where simp_date = :ls_simp_date "
              + " and	p_seqno = :ls_p_seqno "
              + " fetch first 1 rows only ";
      setString("ls_simp_date",wp.colStr(ii,"simp_date"));
      setString("ls_p_seqno",wp.colStr(ii,"p_seqno"));
      sqlSelect(sql_select);
      if(this.toNum(sqlStr("ll_cnt"))>0){
        wp.colSet(ii,"db_return_mark", "Y");
        wp.colSet(ii,"disabled", "disabled");
      }else{
        wp.colSet(ii,"disabled", "");
      }
      ls_return_flag = wp.colStr(ii,"return_flag");
      if(ls_return_flag.equals("Y")){
        wp.colSet(ii,"disabled", "disabled");
        continue;
      }else{
        wp.colSet(ii,"disabled", "");
      }
      if(ll_dtl ==0){
        ll_dtl = ii;
      }
    }
    if( ll_dtl ==0){
      return;
    }
    String ls_p_seqno = wp.colStr(ll_dtl,"p_seqno");
    if(empty(ls_p_seqno)){
      alertErr("無主檔資料 !!");
      return;
    }
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {

    //String[] aa_rowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    String[] aa_mod_seqno = wp.itemBuff("mod_seqno");
    String[] aa_return_flag = wp.itemBuff("return_flag");
    String[] aa_acct_type = wp.itemBuff("acct_type");
    String[] aa_db_return_mark = wp.itemBuff("db_return_mark");
    String[] aa_p_seqno = wp.itemBuff("p_seqno");
    String[] aa_acct_key = wp.itemBuff("acct_key");
    String[] aa_id_p_seqno = wp.itemBuff("id_p_seqno");
    String[] aa_id_no = wp.itemBuff("id_no");
    String[] aa_simp_date = wp.itemBuff("simp_date");
    wp.listCount[0] = aa_acct_type.length;

    //-check approve-
    if (!checkApprove(wp.itemStr("zz_apr_user"),wp.itemStr("zz_apr_passwd")))
    {
      return;
    }

    // -update-
    for (int rr = 0; rr < aa_acct_type.length; rr++) {
      if (!(checkBoxOptOn(rr, opt))) {
        continue;
      }
      if(aa_return_flag[rr].equals("Y")){
        wp.colSet(rr,"ok_flag", "移回旗標=Y,無法處理");
        ilErr++;
        continue;
      }
      //-已登錄移回, 未處理-
      if(aa_db_return_mark[rr].equals("Y")){
        wp.colSet("il_ok", "1");
        wp.colSet(rr,"ok_flag", "V");
        ilOk++;
        continue;
      }
      //-未登錄移回-
      String sql_update=" update ecs_simp_main set "
              + " crt_user = ? "
              + " ,crt_date = to_char(sysdate,'YYYYMMDD') "
              + " ,apr_user = ? "
              + " ,apr_date = to_char(sysdate,'YYYYMMDD') "
//							+ " ,apr_flag = 'Y' "	//2020/08/12 SQL_err 拿掉
              + " ,mod_user = ? "
              + " ,mod_time = sysdate "
              + " ,mod_pgm = 'actp7000' "
              + " ,mod_seqno = mod_seqno + 1 "
              + " where p_seqno = ? "
              + " and simp_date = ? "
              + " and mod_seqno = ? ";

      Object[] param1 = new Object[] {
              wp.loginUser
              ,wp.itemStr("zz_apr_user")
              ,wp.loginUser
              ,aa_p_seqno[rr]
              ,aa_simp_date[rr]
              ,aa_mod_seqno[rr]
      };
      sqlExec(sql_update,param1);
      if(sqlCode==-1){
        sqlCommit(0);
        wp.colSet(rr,"ok_flag", "寫入瘦身資料回移檔 錯誤,update ecs_simp_main err");
        ilErr++;
        continue;
      }

      //-Insert ecs_simp_return-
      String sql_insert=" insert into ecs_simp_return( "
              + " p_seqno "
              + " ,acct_type "
              + " ,acct_key "
              + " ,id_p_seqno "
              + " ,id "
              + " ,simp_date "
              + " ,from_mark "
              + " ,crt_user "
              + " ,crt_date "
              + " ,apr_flag "
              + " ,apr_user "
              + " ,apr_date "
              + " ,mod_user "
              + " ,mod_time "
              + " ,mod_pgm "
              + " ,mod_seqno "
              + " ,return_flag "
              + " )values( "
              + " ?,?,?,?,?,?,'1',?,?,'Y',?,? "
              + " ,?,sysdate,'actp7000',1,'N') "
              ;
      Object[] param2 = new Object[] {
              aa_p_seqno[rr]
              ,aa_acct_type[rr]
              ,aa_acct_key[rr]
              ,aa_id_p_seqno[rr]
              ,aa_id_no[rr]
              ,aa_simp_date[rr]
              ,wp.loginUser
              ,getSysDate()
              ,wp.itemStr("zz_apr_user")
              ,getSysDate()
              ,wp.loginUser
      };
      sqlExec(sql_insert,param2);
      if(sqlCode==-1){
        sqlCommit(0);
        wp.colSet(rr,"ok_flag", "寫入瘦身資料回移檔 錯誤,insert ecs_simp_return err");
        ilErr++;
        continue;
      }
      sqlCommit(1);
      ilOk++;
      wp.colSet(rr,"ok_flag", "V");
    }
    errmsg("覆核處理,成功="+ ilOk +" ,失敗="+ ilErr);
    wp.colSet("il_ok", "1");
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }



  int fCallBatch() throws Exception{
    String[] aa_acct_type = wp.itemBuff("acct_type");

    wp.listCount[0] = aa_acct_type.length;

    EcsCallbatch batch = new EcsCallbatch(wp);
    rc = batch.callBatch("EcsS004");
    if (rc != 1) {
      alertErr("EcsS004  處理: callbatch 失敗,"+batch.getMesg());
      return -1;
    }
    alertMsg("EcsS004   批次程式執行完畢，請重新讀取，若「移回旗標」為「N」，請通知資訊部");

    return 1;
  }

}
