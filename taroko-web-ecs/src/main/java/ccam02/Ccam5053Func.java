package ccam02;
/* 2019-0509:  JH    get new.class_code
   2018-0827:	JH		modify
 *2020-0420  V1.00.01 yanghan 修改了變量名稱和方法名稱
 * */

import busi.FuncAction;

public class Ccam5053Func extends FuncAction {
  String riskLevel = "", riskType = "", cardNote = "";
  int rows = 0;
  private int type = 0;

  @Override
  public void dataCheck() {

  }

  @Override
  public int dbInsert() {

    return rc;
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

    return rc;
  }

  public int deleteTemp() {
    msgOK();
    strSql = " delete cca_risk_consume_parm_t"
        + " where card_note =? and risk_type =? and risk_level =? ";
    setString2(1, varsStr("card_note"));
    setString(varsStr("risk_type"));
    setString(varsStr("risk_level"));

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete cca_risk_consume_parm_t error !");
    }

    return rc;
  }

  public int reCrtTemp() {
    msgOK();

    strSql = " insert into cca_risk_consume_parm_t ( " + " card_note , " + " risk_type , "
        + " risk_level , " + " add_tot_amt , " + " area_type , " + " lmt_amt_month_pct , "
        + " lmt_amt_time_pct , " + " lmt_cnt_day , " + " lmt_cnt_month , " + " rsp_code_1 , "
        + " rsp_code_2 , " + " rsp_code_3 , " + " rsp_code_4 , " + " crt_date , " + " crt_user , "
        + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno " + " ) values ( "
        + " :card_note , " + " :risk_type , " + " :risk_level , " + " 0 , " + " 'T' , " + " 0 , "
        + " 0 , " + " 0 , " + " 0 , " + " '' , " + " '' , " + " '' , " + " '' , "
        + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , " + " :mod_user , " + " sysdate , "
        + " :mod_pgm , " + " 1 " + " ) ";
    var2ParmStr("card_note");
    var2ParmStr("risk_type");
    var2ParmStr("risk_level");
    setString("crt_user", modUser);
    setString("mod_user", modUser);
    setString("mod_pgm", modPgm);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("重新產生卡人等級失敗");
    }

    return rc;
  }

  public int deleteData() {
    msgOK();
    strSql =
        " delete cca_risk_consume_parm" + " where card_note =? and risk_type =? and risk_level =?";
    setString2(1, varsStr("card_note"));
    setString(varsStr("risk_type"));
    setString(varsStr("risk_level"));

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("delete cca_risk_consume_parm error ");
    }

    return rc;
  }

  public int insertTemp() {
    msgOK();

    strSql = " insert into cca_risk_consume_parm_t ( " + " card_note , " + " risk_type , "
        + " risk_level , " + " add_tot_amt , " + " area_type , " + " lmt_amt_month_pct , "
        + " lmt_amt_time_pct , " + " lmt_cnt_day , " + " lmt_cnt_month , " + " rsp_code_1 , "
        + " rsp_code_2 , " + " rsp_code_3 , " + " rsp_code_4 , " + " crt_date , " + " crt_user , "
        + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno " + " ) values ( "
        + " :card_note , " + " :risk_type , " + " :risk_level , " + " :add_tot_amt , " + " 'T' , "
        + " :lmt_amt_month_pct , " + " :lmt_amt_time_pct , " + " :lmt_cnt_day , "
        + " :lmt_cnt_month , " + " :rsp_code_1 , " + " :rsp_code_2 , " + " :rsp_code_3 , "
        + " :rsp_code_4 , " + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , " + " :mod_user , "
        + " sysdate , " + " :mod_pgm , " + " 1 " + " ) ";

    var2ParmStr("card_note");
    var2ParmStr("risk_type");
    var2ParmStr("risk_level");
    var2ParmNum("add_tot_amt");
    var2ParmNum("lmt_amt_month_pct");
    var2ParmNum("lmt_amt_time_pct");
    var2ParmNum("lmt_cnt_day");
    var2ParmNum("lmt_cnt_month");
    var2ParmStr("rsp_code_1");
    var2ParmStr("rsp_code_2");
    var2ParmStr("rsp_code_3");
    var2ParmStr("rsp_code_4");
    setString("crt_user", modUser);
    setString("mod_user", modUser);
    setString("mod_pgm", modPgm);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert cca_risk_consume_parm_t error !");
    }
    return rc;
  }

  void dataCheckAdd() {

    String lsCardNote = "", lsRiskType = "";
    lsCardNote = wp.itemStr("ex_card_note");
    lsRiskType = wp.itemStr("ex_risk_type");

    if (empty(lsCardNote)) {
      errmsg("卡片等級: 不可空白");
      return;
    }

    if (empty(lsRiskType)) {
      errmsg("風險類別: 不可空白");
      return;
    }


    // --檢核是否已存在
    String sql1 =
        " select count(*) as db_cnt from cca_risk_consume_parm where 1=1 and area_type ='T' and card_note = ? and risk_type = ? ";
    sqlSelect(sql1, new Object[] {lsCardNote, lsRiskType});

    if (sqlRowNum < 0 || colNum("db_cnt") > 0) {
      errmsg("風險類別已存在 不可新增");
      return;
    }

    String sql2 =
        " select count(*) as db_cnt2 from cca_risk_consume_parm_t where 1=1 and area_type ='T' and card_note = ? and risk_type = ? ";
    sqlSelect(sql2, new Object[] {lsCardNote, lsRiskType});

    if (sqlRowNum < 0 || colNum("db_cnt2") > 0) {
      errmsg("風險類別已存在 不可新增");
      return;
    }
  }

  public int procFuncAdd() {
    msgOK();
    dataCheckAdd();
    if (rc != 1)
      return rc;
    int selectCount = 0;

    String lsCardNote = "", lsRiskType = "";
    lsCardNote = wp.itemStr("ex_card_note");
    lsRiskType = wp.itemStr("ex_risk_type");

    String sql1 = " select * from cca_risk_consume_parm where card_note = '*' and risk_type ='P' ";
    sqlSelect(sql1);

    selectCount = sqlRowNum;

    strSql = " insert into cca_risk_consume_parm_t ( " + " card_note , " + " risk_type , "
        + " risk_level , " + " add_tot_amt , " + " area_type , " + " lmt_amt_month_pct , "
        + " lmt_amt_time_pct , " + " lmt_cnt_day , " + " lmt_cnt_month , " + " rsp_code_1 , "
        + " rsp_code_2 , " + " rsp_code_3 , " + " rsp_code_4 , " + " crt_date , " + " crt_user , "
        + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno " + " ) values ( "
        + " :card_note , " + " :risk_type , " + " :risk_level , " + " :add_tot_amt , "
        + " :area_type , " + " :lmt_amt_month_pct , " + " :lmt_amt_time_pct , " + " :lmt_cnt_day , "
        + " :lmt_cnt_month , " + " :rsp_code_1 , " + " :rsp_code_2 , " + " :rsp_code_3 , "
        + " :rsp_code_4 , " + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , " + " :mod_user , "
        + " sysdate , " + " :mod_pgm , " + " 1 " + " ) ";

    for (int ii = 0; ii < selectCount; ii++) {
      setString("card_note", lsCardNote);
      setString("risk_type", lsRiskType);
      setString("risk_level", colStr(ii, "risk_level"));
      setDouble("add_tot_amt", colNum(ii, "add_tot_amt"));
      setString("area_type", colStr(ii, "area_type"));
      setDouble("lmt_amt_month_pct", colNum(ii, "lmt_amt_month_pct"));
      setDouble("lmt_amt_time_pct", colNum(ii, "lmt_amt_time_pct"));
      setDouble("lmt_cnt_day", colNum(ii, "lmt_cnt_day"));
      setDouble("lmt_cnt_month", colNum(ii, "lmt_cnt_month"));
      setString("rsp_code_1", colStr(ii, "rsp_code_1"));
      setString("rsp_code_2", colStr(ii, "rsp_code_2"));
      setString("rsp_code_3", colStr(ii, "rsp_code_3"));
      setString("rsp_code_4", colStr(ii, "rsp_code_4"));
      setString("crt_user", wp.loginUser);
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", "ccam5053");
      sqlExec(strSql);

      if (sqlRowNum <= 0) {
        errmsg("insert cca_risk_consume_parm_t error !");
        return rc;
      }

    }

    return rc;
  }

}
