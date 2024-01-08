/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-02  V1.00.01  yash       program initial                            *
* * 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 110-01-05  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package mktp02;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Mktp4210 extends BaseProc {


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
      // insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      // updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
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
      /* 執行 */
      strAction = "S2";
      dataProcess();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {

    // if(wp.item_ss("ex_item_no").equals("06")){
    // if(empty(wp.item_ss("ex_group_code"))){
    // alert_err("團體代號不可空白");
    // return false;
    // }
    // }else{
    // if(empty(wp.item_ss("ex_mcht_no"))){
    // alert_err("特店代號不可空白");
    // return false;
    // }
    // }


    wp.whereStr = " where 1=1  and item_no in ('06','07','08','09','10','11','12','13') ";

    if (empty(wp.itemStr("ex_item_no")) == false) {
      wp.whereStr += " and  item_no = :ex_item_no ";
      setString("ex_item_no", wp.itemStr("ex_item_no"));

    }

    if (empty(wp.itemStr("ex_group_code")) == false) {
      wp.whereStr += " and  key_data = :ex_group_code ";
      setString("ex_group_code", wp.itemStr("ex_group_code"));

    }

    if (empty(wp.itemStr("ex_mcht_no")) == false) {
      wp.whereStr += " and  substr(key_data,1,15) like :ex_mcht_no ";
      setString("ex_mcht_no", wp.itemStr("ex_mcht_no") + "%");

    }

    if (empty(wp.itemStr("ex_crt_date1")) == false) {
      wp.whereStr += " and  crt_date >= :ex_crt_date1 ";
      setString("ex_crt_date1", wp.itemStr("ex_crt_date1"));

    }

    if (empty(wp.itemStr("ex_crt_date2")) == false) {
      wp.whereStr += " and  crt_date <= :ex_crt_date2 ";
      setString("ex_crt_date2", wp.itemStr("ex_crt_date2"));

    }

    if (empty(wp.itemStr("ex_user")) == false) {
      wp.whereStr += " and  crt_user = :ex_user ";
      setString("ex_user", wp.itemStr("ex_user"));

    }



    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (!getWhereStr()) {
      return;
    } ;
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(rowid) as rowid,mod_seqno ," + "item_no, " + "key_type, " + "key_data, "
    // + "decode(item_no,'06',key_data,'') as geoup_code ,"
    // + "decode(item_no,'08',substr(key_data,1,15),key_data) as mcht_no, "
    // + "decode(item_no,'08',substr(key_data,16),key_data) as prod_no, "
        + "trim(decode(key_type,'1', key_data,'')) as geoup_code ,"
        + "trim(decode(key_type,'3', key_data,'4', rtrim(substrb(key_data,1,15)),'')) as mcht_no, "
        + "trim(decode(key_type,'4', rtrim(substrb(key_data,16,8)),'')) as prod_no, "
        + "cost_month, " + "cost_month2, " + "cost_month||'~'||cost_month2 as month ,"
        + "cost_amt, " + "purch_mm, " + "service_amt, " + "crt_user, " + "crt_date ";

    wp.daoTable = "mkt_contri_parm_t ";
    wp.whereOrder = " order by item_no";
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


    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] aaItemNo = wp.itemBuff("item_no");
    String[] aaKeyData = wp.itemBuff("key_data");
    String[] aaKeyType = wp.itemBuff("key_type");
    String[] aaCostAmt = wp.itemBuff("cost_amt");
    String[] opt = wp.itemBuff("opt");
    String[] aaPurchMm = wp.itemBuff("purch_mm");
    String[] aaServiceAmt = wp.itemBuff("service_amt");
    String[] aaCostMonth = wp.itemBuff("cost_month");
    String[] aaCostMonth2 = wp.itemBuff("cost_month2");

    wp.listCount[0] = aaRowid.length;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
    SimpleDateFormat sdfm = new SimpleDateFormat("MM");

    // check
    int rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }


    }

    // save
    // -update-
    rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      wp.colSet(rr, "ok_flag", "");


      // delete mkt_contri_parm
      String lds_sql2 = " delete  mkt_contri_parm " + " where  item_no=:item_no "
          + "  and   key_data=:key_data" + "  and   key_type=:key_type";

      setString("item_no", aaItemNo[rr]);
      setString("key_data", aaKeyData[rr]);
      setString("key_type", aaKeyType[rr]);

      sqlExec(lds_sql2);


      Date date = sdf.parse(aaCostMonth[rr]);
      Date date2 = sdf.parse(aaCostMonth2[rr]);
      int mon = Integer.parseInt(sdfm.format(date2.getTime() - date.getTime()));


      // insert
      for (int cnt = 0; cnt < mon; cnt++) {

        String purchMm = "0";
        String serviceAmt = "0";
        String keyType = "3";

        if (aaItemNo[rr].equals("06")) {
          purchMm = aaPurchMm[rr];
          keyType = "1";
        } else if (aaItemNo[rr].equals("12")) {
          serviceAmt = aaServiceAmt[rr];
        } else if (aaItemNo[rr].equals("07")) {
          keyType = "0";
        } else if (aaItemNo[rr].equals("08")) {
          keyType = "4";
        }

        // mon+1
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, cnt);


        String insSql = " insert into mkt_contri_parm ( " + "   item_no " + " , key_data"
            + " , key_type" + " , cost_month" + " , cost_amt" + " , purch_mm" + " , service_amt"
            + " , crt_user" + " , crt_date" + " , mod_user" + " , mod_time" + " , mod_pgm"
            + " , mod_seqno" + " ) values (" + "  ?,?,?,?,?,?,?,?,?,?" + ",sysdate,?,1)";
        Object[] param = new Object[] {aaItemNo[rr], aaKeyData[rr], keyType,
            sdf.format(calendar.getTime()), aaCostAmt[rr], purchMm, serviceAmt, wp.loginUser,
            getSysDate(), wp.loginUser, wp.itemStr("mod_pgm")};
        sqlExec(insSql, param);
        if (sqlRowNum <= 0) {
          sqlCommit(0);
          wp.colSet(rr, "ok_flag", "!");
          alertErr("ERROR: insert  mkt_contri_parm  ");
          return;
        }

      }


      // delete t
      String ldsSql =
          " delete  mkt_contri_parm_t  where hex(rowid) = :rowid  and  mod_seqno = :mod_seqno ";
      setString("rowid", aaRowid[rr]);
      setString("mod_seqno", aaModSeqno[rr]);
      sqlExec(ldsSql);
      if (sqlRowNum <= 0) {
        sqlCommit(0);
        wp.colSet(rr, "ok_flag", "!");
        alertErr("ERROR: delete  mkt_contri_parm ");
        return;
      } else {
        wp.colSet(rr, "ok_flag", "V");
      }

    }

    sqlCommit(1);
    alertMsg("處理完成");


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

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_user");
      this.dddwList("dddw_sec_user", "sec_user", "usr_id", "usr_cname",
          "where 1=1 order by usr_id");
    } catch (Exception ex) {
    }
  }



}
