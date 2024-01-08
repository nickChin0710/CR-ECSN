/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-16  V1.00.00  yash       program initial                            *
* 108-12-17  V1.00.01  ryan		  update : ptr_group_card==>crd_item_unit    *
*  109-04-22  V1.00.02  yanghan  修改了變量名稱和方法名稱*                        
******************************************************************************/

package dbcm01;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import busi.SqlPrepare;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;


public class Dbcm0085 extends BaseProc {


  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

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
      /* 存檔 */
      strAction = "S2";
      dataProcess();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {


    wp.whereStr = " where 1=1 " + " and d.current_code = '0'  ";

    if (empty(wp.itemStr("ex_card")) == false) {
      wp.whereStr += " and  d.card_no like :ex_card ";
      setString("ex_card", wp.itemStr("ex_card") + "%");
    }

    if (empty(wp.itemStr("ex_id")) == false) {
      wp.whereStr += " and  b.id_no = :ex_id ";
      setString("ex_id", wp.itemStr("ex_id"));
    }

    if (empty(wp.itemStr("ex_corp")) == false) {
      wp.whereStr += " and  d.corp_no = :ex_corp ";
      setString("ex_corp", wp.itemStr("ex_corp"));
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    if (empty(wp.itemStr("ex_card")) && empty(wp.itemStr("ex_id"))
        && empty(wp.itemStr("ex_corp"))) {
      alertErr("請輸入查詢之鍵值");
      return;
    }
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " '' as db_optcode  " + ", lpad(' ',6,' ') as db_appr "
        + ", lpad(' ',8,' ') as db_appr_date " + ", lpad(' ',20,' ') as db_old_process  "
        + ", lpad(' ',2,' ') as db_optcode1 " + ", d.card_no " + ", b.id_no as id "
        + ", d.id_p_seqno " + ", d.p_seqno " + ", d.corp_no " + ", b.chi_name "
        + ", '' as db_expire_chg " + ", '' as db_expire_chg_desc " + ", '' as db_expire_reason "
        + ", decode(d.change_reason,'1','系統續卡','2','提前續卡','3','人工續卡',d.change_reason) as reason_desc "
        + ", d.change_reason" + ", d.change_status "
        + ", decode(d.change_status,'1','續卡待製卡中','2','續卡製卡中','3','續卡完成','4','製卡失敗',d.change_status) as status_desc "
        + ", d.expire_chg_date " + ", d.new_end_date " + ", d.sup_flag " + ", d.major_card_no "
        + ", d.group_code " + ", d.unit_code " + ", d.card_type " + ", d.ic_flag "
        + ", d.reissue_status " + ", d.expire_chg_flag " + ",d.mod_user" + ",d.mod_time"
        + ",d.mod_pgm" + ",d.mod_seqno" + ",hex(d.rowid) as rowid";

    wp.daoTable = "dbc_card d left join crd_idno b on d.id_p_seqno = b.id_p_seqno ";
    wp.whereOrder = " order by d.card_no";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

    ofc_retrieve();

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

    String[] rowid = wp.itemBuff("rowid");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] aaSerNum = wp.itemBuff("ser_num");
    String[] aaTxCode = wp.itemBuff("tx_code");
    String[] aaDbOptcode = wp.itemBuff("db_optcode");
    String[] aaDbOptcode1 = wp.itemBuff("db_optcode1");
    String[] aaDbOptcode2 = wp.itemBuff("db_optcode2");
    String[] aaDbAppr = wp.itemBuff("db_appr");
    String[] aaDbExpireChg = wp.itemBuff("db_expire_chg");
    String[] aaChangeStatus = wp.itemBuff("change_status");
    String[] aaReissueStatus = wp.itemBuff("reissue_status");
    String[] aaExpireChgFlag = wp.itemBuff("expire_chg_flag");
    String[] aaCardNo = wp.itemBuff("card_no");
    String[] aaIdpSeqno = wp.itemBuff("id_p_seqno");
    String[] aaCorpNo = wp.itemBuff("corp_no");
    String[] aaDbExpireReason = wp.itemBuff("db_expire_reason");
    String[] aaExpireChgDate = wp.itemBuff("expire_chg_date");
    String[] aaNewEndDate = wp.itemBuff("new_end_date");
    String[] aaSupFlag = wp.itemBuff("sup_flag");
    String[] aaMajorCardNo = wp.itemBuff("major_card_no");
    String[] aapSeqno = wp.itemBuff("p_seqno");
    String[] aaGroupCode = wp.itemBuff("group_code");
    String[] aaCardType = wp.itemBuff("card_type");
    String[] aaExpireReason = wp.itemBuff("expire_reason");
    String[] aaUnitCode = wp.itemBuff("unit_code");

    wp.listCount[0] = aaTxCode.length;

    // check
    int isOk = 0, err = 0;
    // save
    // -update-
    for (int ll = 0; ll < aaSerNum.length; ll++) {

      if (empty(aaDbOptcode[ll])) {
        continue;
      }

      aaDbExpireChg[ll] = aaDbOptcode[ll];


      if (aaDbOptcode[ll].equals("1") && empty(aaDbOptcode1[ll])) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "ls_errmsg", "請輸入預約原因 !!");
        err++;
        continue;
      }

      if (aaDbOptcode[ll].equals("4") && empty(aaDbOptcode2[ll])) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "ls_errmsg", "請輸入人工原因 !!");
        err++;
        continue;
      }

      if (aaDbOptcode[ll].equals("0") && !aaDbAppr[ll].equals("N")) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "ls_errmsg", "請執行【取消不續卡(放行後)】!!");
        err++;
        continue;
      }

      if (aaDbOptcode[ll].equals("2") && !aaDbAppr[ll].equals("Y")
          && aaDbAppr[ll].length() != 0) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "ls_errmsg", "請執行【取消不續卡(放行前)】!!");
        err++;
        continue;
      }


      // wf_chk_expire_chg_flag
      if (aaDbOptcode[ll].equals("1") || aaDbOptcode[ll].equals("4")) {

        if (empty(aaExpireChgFlag[ll]) == false) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "此卡片已在不續卡狀態下");
          err++;
          continue;
        }

        if (empty(aaDbExpireChg[ll])) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "請輸入預約不續卡註記");
          err++;
          continue;
        }

        if (aaChangeStatus[ll].equals("1") || aaChangeStatus[ll].equals("2")) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "此卡片在續卡狀態下,不可做預約不續卡");
          err++;
          continue;
        }

        if (aaReissueStatus[ll].equals("1") || aaReissueStatus[ll].equals("2")) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "此卡片在重製卡狀態下,不可做預約不續卡");
          err++;
          continue;
        }

      }

      if (aaDbOptcode[ll].equals("2")) {
        if (empty(aaExpireChgFlag[ll])) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "此卡片未在不續卡狀態下,不可做取消預約不續卡");
          err++;
          continue;
        }
      }



      if (aaChangeStatus[ll].equals("1") || aaChangeStatus[ll].equals("2")) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "ls_errmsg", "此卡片在續卡狀態下,不可做預約不續卡");
        err++;
        continue;
      }

      if (aaReissueStatus[ll].equals("1") || aaReissueStatus[ll].equals("2")) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "ls_errmsg", "此卡片在重製卡狀態下,不可做預約不續卡");
        err++;
        continue;
      }

      if (aaDbOptcode[ll].equals("2")) {
        if (empty(aaDbExpireChg[ll])) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "此卡片未在不續卡狀態下,不可做取消預約不續卡");
          err++;
          continue;
        }

      }



      if (aaDbOptcode[ll].equals("0")) {
        // wf_delete_dbc_card_tmp
        String lsDel = " delete  dbc_card_tmp   where card_no  = :card_no and kind_type = '080' ";
        setString("card_no", aaCardNo[ll]);
        sqlExec(lsDel);
        if (sqlRowNum <= 0) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", " wf_delete_dbc_card_tmp err");
          err++;
          sqlCommit(0);
          continue;
        } else {
          isOk++;
          wp.colSet(ll, "ok_flag", "V");
          sqlCommit(1);
          continue;
        }


      }

      if (aaDbOptcode[ll].equals("1") || aaDbOptcode[ll].equals("4")) {

        String lsDel = " delete  dbc_card_tmp   where card_no  = :card_no and kind_type = '080' ";
        setString("card_no", aaCardNo[ll]);
        sqlExec(lsDel);

        String lsExpireReason = "";
        // wf_upd_dbc_card
        if (aaDbOptcode[ll].equals("1")) {
          lsExpireReason = aaDbOptcode1[ll];
        }
        if (aaDbOptcode[ll].equals("4")) {
          lsExpireReason = aaDbOptcode2[ll];
        }

        if (aaChangeStatus[ll].equals("2")) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", " 此卡片已送製卡,不可作任何異動");
          err++;
          sqlCommit(0);
          continue;
        }


        // wf_insert_dbc_card_tmp
        String lsSql =
            " select count(*) as cnt from dbc_card_tmp where card_no =:card_no and kind_type = '080' ";
        setString("card_no", aaCardNo[ll]);
        sqlSelect(lsSql);

        // String ls_cnt = sql_ss("cnt");
        if (sqlNum("cnt") > 0) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", " 已經處理提前續卡過,請先查詢資料(dbc_card_tmp) !!");
          err++;
          sqlCommit(0);
          continue;
        }

        busi.SqlPrepare spi = new SqlPrepare();
        spi.sql2Insert("dbc_card_tmp");
        spi.ppstr("card_no", aaCardNo[ll]);
        spi.ppstr("id_p_seqno", aaIdpSeqno[ll]);
        spi.ppstr("kind_type", "080");
        spi.ppstr("corp_no", aaCorpNo[ll]);
        spi.ppstr("process_kind", aaDbOptcode[ll]);
        spi.ppstr("expire_reason", lsExpireReason);
        spi.ppstr("expire_chg_flag", aaDbExpireChg[ll]);
        spi.ppstr("expire_chg_date", getSysDate());
        spi.ppstr("expire_reason_old", aaExpireReason[ll]);
        spi.ppstr("expire_chg_flag_old", aaReissueStatus[ll]);
        spi.ppstr("expire_chg_date_old", aaExpireChgDate[ll]);
        spi.ppstr("cur_end_date", aaNewEndDate[ll]);
        spi.ppstr("old_end_date", aaNewEndDate[ll]);
        // spi.ppss("cur_beg_date","");
        spi.ppstr("crt_user", wp.loginUser);
        spi.ppstr("crt_date", getSysDate());
        spi.ppstr("mod_pgm", wp.itemStr("mod_pgm"));
        spi.ppstr("mod_user", wp.loginUser);
        spi.ppnum("mod_seqno", 1);
        spi.addsql(", mod_time ", ", sysdate ");
        sqlExec(spi.sqlStmt(), spi.sqlParm());
        if (sqlRowNum <= 0) {

          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", " 寫入卡片暫存檔錯誤~");
          err++;
          sqlCommit(0);
          continue;
        } else {
          isOk++;
          wp.colSet(ll, "ok_flag", "V");
          sqlCommit(1);
          continue;
        }


      }

      if (aaDbOptcode[ll].equals("2")) {

        String lsDel = " delete  dbc_card_tmp   where card_no  = :card_no and kind_type = '080' ";
        setString("card_no", aaCardNo[ll]);
        sqlExec(lsDel);

        // wf_cancel_expire
        if (aaChangeStatus[ll].equals("2")) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "此卡片已送製卡,不可作任何異動");
          err++;
          sqlCommit(0);
          continue;
        }

        if (empty(aaDbExpireChg[ll])) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "此筆資料本身並無不續卡註記");
          err++;
          sqlCommit(0);
          continue;
        }

        String ls_expire_reason = "";

        if (aaDbOptcode[ll].equals("1")) {
          ls_expire_reason = aaDbOptcode1[ll];
        }
        if (aaDbOptcode[ll].equals("4")) {
          ls_expire_reason = aaDbOptcode2[ll];
        }
        // wf_insert_dbc_card_tmp
        String ls_sql =
            " select count(*) as cnt from dbc_card_tmp where card_no =:card_no and kind_type = '080' ";
        setString("card_no", aaCardNo[ll]);
        sqlSelect(ls_sql);
        // String ls_cnt = sql_ss("cnt");
        if (sqlNum("cnt") > 0) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", " 已經處理提前續卡過,請先查詢資料(dbc_card_tmp) !!");
          err++;
          sqlCommit(0);
          continue;
        }
        busi.SqlPrepare spi2 = new SqlPrepare();
        spi2.sql2Insert("dbc_card_tmp");
        spi2.ppstr("card_no", aaCardNo[ll]);
        spi2.ppstr("id_p_seqno", aaIdpSeqno[ll]);
        spi2.ppstr("kind_type", "080");
        spi2.ppstr("corp_no", aaCorpNo[ll]);
        spi2.ppstr("process_kind", aaDbOptcode[ll]);
        spi2.ppstr("expire_reason", ls_expire_reason);
        spi2.ppstr("expire_chg_flag", aaDbExpireChg[ll]);
        spi2.ppstr("expire_chg_date", getSysDate());
        spi2.ppstr("expire_reason_old", aaExpireReason[ll]);
        spi2.ppstr("expire_chg_flag_old", aaReissueStatus[ll]);
        spi2.ppstr("expire_chg_date_old", aaExpireChgDate[ll]);
        spi2.ppstr("cur_end_date", aaNewEndDate[ll]);
        spi2.ppstr("old_end_date", aaNewEndDate[ll]);
        // spi.ppss("cur_beg_date","");
        spi2.ppstr("crt_user", wp.loginUser);
        spi2.ppstr("crt_date", getSysDate());
        spi2.addsql(", mod_time ", ", sysdate ");
        spi2.ppstr("mod_pgm", wp.itemStr("mod_pgm"));
        spi2.ppstr("mod_user", wp.loginUser);
        spi2.ppnum("mod_seqno", 1);

        sqlExec(spi2.sqlStmt(), spi2.sqlParm());
        if (sqlRowNum <= 0) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", " 寫入卡片暫存檔錯誤~");
          err++;
          sqlCommit(0);
          continue;
        } else {
          isOk++;
          wp.colSet(ll, "ok_flag", "V");
          sqlCommit(1);
          continue;
        }

      }

      if (aaDbOptcode[ll].equals("3")) {
        String ls_del = " delete  dbc_card_tmp   where card_no  = :card_no and kind_type = '080' ";
        setString("card_no", aaCardNo[ll]);
        sqlExec(ls_del);

        // wf_move_emboss_tmp
        if (aaChangeStatus.equals("1")) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "續卡製卡中，不可改為不續卡");
          err++;
          sqlCommit(0);
          continue;
        }

        if (aaChangeStatus.equals("2")) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "此卡片送製卡中,不可再做提前續卡");
          err++;
          sqlCommit(0);
          continue;
        }


        String ls_sql = " select count(*) as cnt from dbc_emboss_tmp where old_card_no =:card_no";
        setString("card_no", aaCardNo[ll]);
        sqlSelect(ls_sql);

        if (sqlNum("cnt") > 0) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "續卡製卡中，不可改為不續卡");
          err++;
          sqlCommit(0);
          continue;
        }

        if (aaSupFlag[ll].equals("1")) {

          String ls_sql2 = " select current_code from dbc_card where card_no =:card_no";
          setString("card_no", aaMajorCardNo[ll]);
          sqlSelect(ls_sql2);

          if (sqlRowNum <= 0) {
            wp.colSet(ll, "ok_flag", "X");
            wp.colSet(ll, "ls_errmsg", "找取不到正卡資料");
            err++;
            sqlCommit(0);
            continue;
          }
          String ls_major_current_code = sqlStr("current_code");
          if (!ls_major_current_code.equals("0")) {
            wp.colSet(ll, "ok_flag", "X");
            wp.colSet(ll, "ls_errmsg", "正卡不為正常卡,不可做線上續卡");
            err++;
            sqlCommit(0);
            continue;
          }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String str1 = getSysDate();
        String str2 = aaNewEndDate[ll];
        Calendar bef = Calendar.getInstance();
        Calendar aft = Calendar.getInstance();
        bef.setTime(sdf.parse(str1));
        aft.setTime(sdf.parse(str2));
        int resultYear = (aft.get(Calendar.YEAR) - bef.get(Calendar.YEAR)) * 12;
        int resultMonth = aft.get(Calendar.MONTH) - bef.get(Calendar.MONTH);

        if ((resultYear + resultMonth) > 6) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "效期需在系統日六個月內");
          err++;
          sqlCommit(0);
          continue;
        }

        String lsSql3 =
            " select line_of_credit_amt,chg_addr_date,decode(stat_send_paper,'','N',stat_send_paper) as stat_send_paper, decode(stat_send_internet,'','N',stat_send_internet) as stat_send_internet from dba_acno where p_seqno=:p_seqno ";
        setString("p_seqno", aapSeqno[ll]);
        sqlSelect(lsSql3);
        if (sqlRowNum <= 0) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "select dba_acno err ! ");
          err++;
          sqlCommit(0);
          continue;
        }

        String lsSendPaper = sqlStr("stat_send_paper");
        String lsStatSendInternet = sqlStr("stat_send_internet");
        String batchno = "";
        String recno = "";


        String batchno1 = strMid(getSysDate(), 3, 6);

        if (lsSendPaper.equals("N") && lsStatSendInternet.equals("N")) {
          String lsSql4 =
              "	select max(to_number(nvl(substr(batchno,7,2),0)))+1 as ls_batchno ,max(recno)+1 as li_recno  from dbc_emboss_tmp  where substr(batchno,1,6) = :ls_batchno1 and substr(batchno,7,2) >= '85'";
          setString("ls_batchno1", batchno1);
          sqlSelect(lsSql4);
          if (empty(sqlStr("ls_batchno"))) {
            batchno = batchno1 + "85";
          }
          recno = sqlStr("li_recno");
        } else {
          String lsSql5 =
              "	select max(batchno) as ls_batchno from dbc_emboss_tmp  where substr(batchno,1,6) = :ls_batchno1 ";
          setString("ls_batchno1", batchno1);
          sqlSelect(lsSql5);
          batchno = sqlStr("ls_batchno");
          if (empty(sqlStr("ls_batchno"))) {
            batchno = batchno1 + "01";
          } else {
            String lsSql6 =
                "	select max(recno)+1 as li_recno from  dbc_emboss_tmp  where batchno = :ls_batchno ";
            setString("ls_batchno", batchno);
            sqlSelect(lsSql6);
            recno = sqlStr("li_recno");
          }
        }

        if (empty(recno) || toInt(recno) == 0) {
          recno = "1";
        }

        String lsSql7 =
            "	select chi_name, birthday from dbc_idno  where id_p_seqno = :id_p_seqno ";
        setString("id_p_seqno", aaIdpSeqno[ll]);
        sqlSelect(lsSql7);
        if (sqlRowNum <= 0) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "抓取卡人檔失敗 ! ");
          err++;
          sqlCommit(0);
          continue;
        }
        // wf_get_extn_year
        String lsSql8 =
            "	select  extn_year from crd_item_unit  where unit_code = :unit_code and card_type=:card_type ";
        setString("unit_code", empty(aaUnitCode[ll]) ? "0000" : aaUnitCode[ll]);
        setString("card_type", aaCardType[ll]);
        sqlSelect(lsSql8);
        if (sqlRowNum <= 0) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "抓取不到展期年~ ");
          err++;
          sqlCommit(0);
          continue;
        }

        // wf_insert_dbc_card_tmp
        String ls_expire_reason = "";

        if (aaDbOptcode[ll].equals("1")) {
          ls_expire_reason = aaDbOptcode1[ll];
        }
        if (aaDbOptcode[ll].equals("4")) {
          ls_expire_reason = aaDbOptcode2[ll];
        }


        String lsSql9 =
            " select count(*) as cnt from dbc_card_tmp where card_no =:card_no and kind_type = '080' ";
        setString("card_no", aaCardNo[ll]);
        sqlSelect(lsSql9);
        // String ls_cnt = sql_ss("cnt");
        if (sqlNum("cnt") > 0) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", " 已經處理提前續卡過,請先查詢資料(dbc_card_tmp) !!");
          err++;
          sqlCommit(0);
          continue;
        }

        busi.SqlPrepare spi = new SqlPrepare();
        spi.sql2Insert("dbc_card_tmp");
        spi.ppstr("card_no", aaCardNo[ll]);
        spi.ppstr("id_p_seqno", aaIdpSeqno[ll]);
        spi.ppstr("kind_type", "080");
        spi.ppstr("corp_no", aaCorpNo[ll]);
        spi.ppstr("process_kind", aaDbOptcode[ll]);
        spi.ppstr("expire_reason", ls_expire_reason);
        spi.ppstr("expire_chg_flag", aaDbExpireChg[ll]);
        spi.ppstr("expire_chg_date", getSysDate());
        spi.ppstr("expire_reason_old", aaExpireReason[ll]);
        spi.ppstr("expire_chg_flag_old", aaReissueStatus[ll]);
        spi.ppstr("expire_chg_date_old", aaExpireChgDate[ll]);
        spi.ppstr("cur_end_date", aaNewEndDate[ll]);
        spi.ppstr("old_end_date", aaNewEndDate[ll]);
        // spi.ppss("cur_beg_date","");
        spi.ppstr("crt_user", wp.loginUser);
        spi.ppstr("crt_date", getSysDate());
        spi.ppstr("mod_pgm", wp.itemStr("mod_pgm"));
        spi.ppstr("mod_user", wp.loginUser);
        spi.ppnum("mod_seqno", 1);
        spi.addsql(", mod_time ", ", sysdate ");
        sqlExec(spi.sqlStmt(), spi.sqlParm());
        if (sqlRowNum <= 0) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", " 寫入卡片暫存檔錯誤~");
          err++;
          sqlCommit(0);
          continue;
        } else {
          isOk++;
          wp.colSet(ll, "ok_flag", "V");
          sqlCommit(1);
          continue;
        }


      }

    }
    wp.initOption = "--";
    this.dddwList("dddw_optcode1", "ptr_sys_idtab", "wf_id", "wf_desc",
        "where 1=1 and wf_type='NOTCHG_VD_O' order by wf_id");
    wp.initOption = "--";
    this.dddwList("dddw_optcode2", "ptr_sys_idtab", "wf_id", "wf_desc",
        "where 1=1 and wf_type='NOTCHG_VD_M' order by wf_id");
    alertMsg("處理: 成功筆數=" + isOk + "; 失敗筆數=" + err + ";");
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

      for (int ii = 0; ii < wp.selectCnt; ii++) {

        wp.initOption = "--";
        wp.optionKey = wp.itemStr(ii, "db_optcode1");
        this.dddwList(ii, "dddw_optcode1", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where 1=1 and wf_type='NOTCHG_VD_O' order by wf_id");

        wp.initOption = "--";
        wp.optionKey = wp.itemStr(ii, "db_optcode2");
        this.dddwList(ii, "dddw_optcode2", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where 1=1 and wf_type='NOTCHG_VD_M' order by wf_id");


      }


    } catch (Exception ex) {
    }
  }

  void ofc_retrieve() {
    int ls_cnt = wp.selectCnt;
    String lsIdpSeqno = "";
    String lscardno = "";
    String lspseqno = "";
    String lsAppr = "";
    String lsApprDate = "";
    String lsProcessKind = "";
    String lsExpireChgFlag = "";
    String lsExpireReason = "", lsExpireChgDate = "", lsDbExpireChgDesc = "";
    for (int ll = 0; ll < ls_cnt; ll++) {
      lsIdpSeqno = wp.colStr(ll, "id_p_seqno");
      lscardno = wp.colStr(ll, "card_no");
      lspseqno = wp.colStr(ll, "p_seqno");

      String ls_sql = "select risk_bank_no from dba_acno where p_seqno =:p_seqno  ";
      setString("p_seqno", lspseqno);
      sqlSelect(ls_sql);
      if (sqlRowNum <= 0) {
        alertErr("無法抓取到此卡號帳戶資料");
        return;
      }
      // String ls_risk_bank_no = sql_ss("risk_bank_no");

      String lsSql2 =
          "select process_kind,expire_reason,expire_chg_flag,expire_chg_date,apr_date  from dbc_card_tmp where card_no =:card_no  and kind_type = '080'  ";
      setString("card_no", lscardno);
      sqlSelect(lsSql2);

      if (sqlRowNum > 0) {
        lsProcessKind = sqlStr("process_kind");
        lsExpireReason = sqlStr("expire_reason");
        lsExpireChgDate = sqlStr("expire_chg_date");
        lsExpireChgFlag = sqlStr("expire_chg_flag");

        wp.colSet(ll, "db_old_process", lsProcessKind);
        wp.colSet(ll, "expire_reason", lsExpireReason);
        wp.colSet(ll, "expire_chg_date", lsExpireChgDate);
        wp.colSet(ll, "expire_chg_flag", lsExpireChgFlag);


        if (lsExpireChgFlag.equals("1")) {
          String ls_sql3 = "select wf_desc " + " from ptr_sys_idtab "
              + " where wf_type='NOTCHG_VD_O' and wf_id=:wf_id ";
          setString("wf_id", lsExpireReason);
          sqlSelect(ls_sql3);
          wp.colSet(ll, "db_expire_reason", lsExpireReason + "-" + sqlStr("wf_desc"));
          wp.colSet(ll, "expire_reason", lsExpireReason);
        }

        if (lsExpireChgFlag.equals("4")) {
          String ls_sql4 = "select wf_desc " + " from ptr_sys_idtab "
              + " where wf_type='NOTCHG_VD_M' and wf_id=:wf_id ";
          setString("wf_id", lsExpireReason);
          sqlSelect(ls_sql4);
          wp.colSet(ll, "db_expire_reason", lsExpireReason + "-" + sqlStr("wf_desc"));
          wp.colSet(ll, "expire_reason", lsExpireReason);
        }

        wp.colSet(ll, "db_appr", "N");
        // wp.col_set(ll,"db_expire_chg", wp.col_ss(ll, "expire_chg_flag"));

        switch (wp.colStr(ll, "expire_chg_flag")) {
          case "0":
            lsDbExpireChgDesc = "取消不續卡(放行前)";
            break;
          case "1":
            lsDbExpireChgDesc = "預約不續卡";
            break;
          case "4":
            lsDbExpireChgDesc = "人工不續卡";
            break;
          case "2":
            lsDbExpireChgDesc = "取消不續卡(放行後)";
            break;
          case "3":
            lsDbExpireChgDesc = "系統不續卡改續卡";
            break;
          case "5":
            lsDbExpireChgDesc = "系統不續卡";
            break;

          default:
            break;
        }

        wp.colSet(ll, "db_expire_chg", lsDbExpireChgDesc);

      } else {
        String lsSql5 =
            "select process_kind,expire_reason,expire_chg_flag,expire_chg_date,apr_date  from dbc_card_tmp_h where card_no =:card_no  and kind_type = '080'  "
                + " and apr_date in ( select max(apr_date) from dbc_card_tmp_h   where  card_no  = :ls_cardno2 and kind_type = '080')";
        setString("card_no", lscardno);
        setString("ls_cardno2", lscardno);
        sqlSelect(lsSql5);


        if (sqlRowNum > 0) {
          wp.colSet(ll, "db_appr_date", sqlStr("apr_date"));
          wp.colSet(ll, "db_appr", "Y");

          String lsSql6 = "select expire_reason,"
              + " decode(expire_chg_flag,'1','5','2','1','3','4') as expire_chg_flag , expire_chg_date  from dbc_card   where card_no   = :ls_cardno ";
          setString("ls_cardno", lscardno);
          sqlSelect(lsSql6);

          if (sqlRowNum > 0) {
            lsExpireReason = sqlStr("expire_reason");
            lsExpireChgDate = sqlStr("expire_chg_date");
            lsExpireChgFlag = sqlStr("expire_chg_flag");


            wp.colSet(ll, "expire_reason", lsExpireReason);
            wp.colSet(ll, "expire_chg_date", lsExpireChgDate);
            wp.colSet(ll, "expire_chg_flag", lsExpireChgFlag);

            if (lsExpireChgFlag.equals("1")) {
              String lsSql7 = "select wf_desc " + " from ptr_sys_idtab "
                  + " where wf_type='NOTCHG_VD_O' and wf_id=:wf_id ";
              setString("wf_id", lsExpireReason);
              sqlSelect(lsSql7);
              wp.colSet(ll, "db_expire_reason", lsExpireReason + "-" + sqlStr("wf_desc"));
              wp.colSet(ll, "expire_reason", lsExpireReason);
            }

            if (lsExpireChgFlag.equals("4")) {
              String lsSql8 = "select wf_desc " + " from ptr_sys_idtab "
                  + " where wf_type='NOTCHG_VD_M' and wf_id=:wf_id ";
              setString("wf_id", lsExpireReason);
              sqlSelect(lsSql8);
              wp.colSet(ll, "db_expire_reason", lsExpireReason + "-" + sqlStr("wf_desc"));
              wp.colSet(ll, "expire_reason", lsExpireReason);
            }

            switch (wp.colStr(ll, "expire_chg_flag")) {
              case "0":
                lsDbExpireChgDesc = "取消不續卡(放行前)";
                break;
              case "1":
                lsDbExpireChgDesc = "預約不續卡";
                break;
              case "4":
                lsDbExpireChgDesc = "人工不續卡";
                break;
              case "2":
                lsDbExpireChgDesc = "取消不續卡(放行後)";
                break;
              case "3":
                lsDbExpireChgDesc = "系統不續卡改續卡";
                break;
              case "5":
                lsDbExpireChgDesc = "系統不續卡";
                break;

              default:
                break;
            }

            wp.colSet(ll, "db_expire_chg", lsDbExpireChgDesc);


          }
        }


      }

      String[] cde = new String[] {"0", "1", "4", "2", "3"};
      String[] txt = new String[] {"取消不續卡(放行前)", "預約不續卡", "人工不續卡", "取消不續卡(放行後)", "系統不續卡改續卡"};
      wp.colSet(ll, "db_old_process", commString.decode(lsProcessKind, cde, txt));



    }

  }



}
