/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-15  V1.00.00  Andy            program initial                            *
* 106-12-14  V1.00.01  Andy		        update : ucStr==>zzStr                     *
* 108-10-15  V1.00.02  Amber         update                                     *	
* 109-01-03  V1.00.03  Justin Wu    updated for archit.  change
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
* 111-11-25  V1.00.04  Zuwei      sync from mega                             *
* 111-12-02  V1.00.05  Zuwei      fix 執行 bug                             *
* 111-12-02  V1.00.06  Zuwei      fix 執行 bug                             *
******************************************************************************/

package genp01;

import org.apache.commons.lang3.StringUtils;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Genp0210 extends BaseProc {
  String mExAcNo = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      // case "A":
      // /* 新增功能 */
      // saveFunc();
      // break;
      // case "U":
      // /* 更新功能 */
      // saveFunc();
      // break;
      // case "D":
      // /* 刪除功能 */
      // saveFunc();
      // break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "S2":
        // 存檔
        strAction = "S2";
        dataProcess();
        break;
      case "AJAX":
        // AJAX 20200102 updated for archit. change
        /* TEST */
        strAction = "AJAX";
        processAjaxOption();
        break;
      case "ItemChange":
        /* TEST */
        strAction = "ItemChange";
        break;
      default:
        break;
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    String exAcNo = wp.itemStr("ex_ac_no");

    wp.whereStr = " where 1=1  ";
    if (empty(exAcNo) == false) {
      wp.whereStr += sqlCol(exAcNo, "ac_no");
    }
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    getWhereStr();
    // -page control-
//    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno,"
            + "ac_no, "
            + "ac_full_name, "
            + "ac_brief_name, "
            + "memo3_flag, "
            + "memo3_kind, "
            + "dr_flag, "
            + "cr_flag, "
            + "brn_rpt_flag, "
            + "online_update_flag, "
            + "write_off_flag, "
            + "count_flag, "
            + "neuter_flag, "
            + "asset_p_flag, "
            + "crt_date, "
            + "crt_user, "
            + "mod_user, "
            + "mod_time, "
            + "mod_pgm, "
            + "mod_seqno, "
            + "'0' db_optcode   ";
    wp.daoTable = " gen_acct_m_t ";
    wp.whereOrder = " order by ac_no ";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
	apprDisabled("mod_user");// 20200826 add
    // list_wkdata();
  }

  void listWkdata() throws Exception {

  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_mcht_no = wp.item_ss("mcht_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    // 本功能未做Detl頁面
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    // try {
    // wp.initOption = "--";
    // wp.optionKey = wp.item_ss("ex_ac_no");
    // this.dddw_list("dddw_ac_no", "gen_acct_m", "ac_no", "ac_full_name",
    // " where 1=1 order by ac_no FETCH FIRST 6000 ROWS ONLY ");
    // } catch (Exception ex) {
    // }
  }

  public int deleteGenAcctMT(String acNo) throws Exception {
    String dsSql = "delete gen_acct_m_t where 1=1 ";
    dsSql += sqlCol(acNo, "ac_no");
    sqlExec(dsSql);
    if (sqlRowNum <= 0) {
      return -1;
    } else {
      return 1;
    }
  }

  @Override
  public void dataProcess() throws Exception {

    String[] opt = wp.itemBuff("opt");

    String[] aaAcNo = wp.itemBuff("ac_no");
    String[] aaAcFullName = wp.itemBuff("ac_full_name");
    String[] aaAcBriefName = wp.itemBuff("ac_brief_name");
    String[] aaMemo3Flag = wp.itemBuff("memo3_flag");
    String[] aaMemoKind = wp.itemBuff("memo3_kind");
    String[] aaDrFlag = wp.itemBuff("dr_flag");
    String[] aaCrFlag = wp.itemBuff("cr_flag");
    String[] aaBrnRptFlag = wp.itemBuff("brn_rpt_flag");
    String[] aaOnlineUpdateFlag = wp.itemBuff("online_update_flag");
    String[] aaWriteOffFlag = wp.itemBuff("write_off_flag");
    String[] aaCountFlag = wp.itemBuff("count_flag");
    String[] aaNeuterFlag = wp.itemBuff("neuter_flag");
    String[] aaAssetPFlag = wp.itemBuff("asset_p_flag");
    String[] aaCrtDate = wp.itemBuff("crt_date");
    String[] aaCrtUser = wp.itemBuff("crt_user");

    wp.listCount[0] = aaAcNo.length;

    // check
    int rr = -1;
    int llOk = 0, llErr = 0;

    // -update-
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      mExAcNo = aaAcNo[rr];

      String mAcNo = "", mModSeqno = "";
      String lsSql = "select hex(rowid) as rowid, mod_seqno,ac_no from gen_acct_m where 1=1 ";
      lsSql += sqlCol(mExAcNo, "ac_no");
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        // update gen_acct_m
        mAcNo = sqlStr("ac_no");
        mModSeqno = sqlStr("mod_seqno");

        String usSql = "update gen_acct_m set "
            + " ac_full_name =:ac_full_name, ac_brief_name =:ac_brief_name, "
            + "memo3_flag =:memo3_flag,         memo3_kind =:memo3_kind,     dr_flag =:dr_flag, "
            + "cr_flag =:cr_flag,               brn_rpt_flag =:brn_rpt_flag, online_update_flag =:online_update_flag, "
            + "write_off_flag =:write_off_flag, count_flag =:count_flag,     neuter_flag =:neuter_flag, "
            + "asset_p_flag =:asset_p_flag,     crt_date =:crt_date,         crt_user =:crt_user, "
            + "mod_user =:mod_user, "
            + "mod_time = sysdate,              mod_pgm = 'genp0210',        mod_seqno = nvl(mod_seqno,0)+1 ";

        setString("ac_full_name", aaAcFullName[rr]);
        setString("ac_brief_name", aaAcBriefName[rr]);
        setString("memo3_flag", aaMemo3Flag[rr]);
        setString("memo3_kind", aaMemoKind[rr]);
        setString("dr_flag", aaDrFlag[rr]);
        setString("cr_flag", aaDrFlag[rr]);
        setString("brn_rpt_flag", aaBrnRptFlag[rr]);
        setString("online_update_flag", aaOnlineUpdateFlag[rr]);
        setString("write_off_flag", aaWriteOffFlag[rr]);
        setString("count_flag", aaCountFlag[rr]);
        setString("neuter_flag", aaNeuterFlag[rr]);
        setString("asset_p_flag", aaAssetPFlag[rr]);
        setString("crt_date", aaCrtDate[rr]);
        setString("crt_user", aaCrtUser[rr]);
        setString("mod_user", wp.loginUser);
        usSql += " where 1=1 ";
        if (StringUtils.isNotEmpty(mAcNo)) {
            usSql += " and ac_no = :ac_no ";
            setString("ac_no", mAcNo);
        }
        if (StringUtils.isNotEmpty(mModSeqno)) {
            usSql += " and mod_seqno = :mod_seqno ";
            setString("mod_seqno", mModSeqno);
        }
        sqlExec(usSql);

        if (sqlRowNum <= 0) {
          wp.colSet(rr, "ok_flag", "!");
          llErr++;
        } else {
          llOk++;
          wp.colSet(rr, "ok_flag", "V");
          if (deleteGenAcctMT(aaAcNo[rr]) != 1) {
            wp.colSet(rr, "ok_flag", "!");
            llErr++;
            llOk--;
            continue;
          }

          sqlCommit(1);
        }

      } else {
        // insert gen_acct_m
        String isSql = "insert into gen_acct_m "
            + "(ac_no, ac_full_name, ac_brief_name, memo3_flag, memo3_kind, "
            + "dr_flag, cr_flag, brn_rpt_flag, online_update_flag, write_off_flag, "
            + "count_flag, neuter_flag, asset_p_flag, crt_date, crt_user, "
            + "apr_date, apr_user, mod_user, mod_time, mod_pgm, mod_seqno) " + "values ("
            + ":ac_no, :ac_full_name, :ac_brief_name, :memo3_flag, :memo3_kind, "
            + ":dr_flag, :cr_flag, :brn_rpt_flag, :online_update_flag, :write_off_flag, "
            + ":count_flag, :neuter_flag, :asset_p_flag, :crt_date, :crt_user, "
            + ":apr_date, :apr_user, :mod_user, sysdate, 'genp0210', 1)";
        setString("ac_no", aaAcNo[rr]);
        setString("ac_full_name", aaAcFullName[rr]);
        setString("ac_brief_name", aaAcBriefName[rr]);
        setString("memo3_flag", aaMemo3Flag[rr]);
        setString("memo3_kind", aaMemoKind[rr]);
        setString("dr_flag", aaDrFlag[rr]);
        setString("cr_flag", aaDrFlag[rr]);
        setString("brn_rpt_flag", aaBrnRptFlag[rr]);
        setString("online_update_flag", aaOnlineUpdateFlag[rr]);
        setString("write_off_flag", aaWriteOffFlag[rr]);
        setString("count_flag", aaCountFlag[rr]);
        setString("neuter_flag", aaNeuterFlag[rr]);
        setString("asset_p_flag", aaAssetPFlag[rr]);
        setString("crt_date", aaCrtDate[rr]);
        setString("crt_user", aaCrtUser[rr]);
        setString("apr_date", getSysDate());
        setString("apr_user", wp.loginUser);
        setString("mod_user", wp.loginUser);
        sqlExec(isSql);
        if (sqlRowNum <= 0) {
          wp.colSet(rr, "ok_flag", "!");
          llErr++;
          continue;
        } else {
          wp.colSet(rr, "ok_flag", "V");
          llOk++;
          if (deleteGenAcctMT(aaAcNo[rr]) != 1) {
            wp.colSet(rr, "ok_flag", "!");
            llErr++;
            llOk--;
            continue;
          }
          sqlCommit(1);
        }
      }
    }

    alertMsg("放行處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr + ";");

  }

  public void processAjaxOption() throws Exception {

    wp.varRows = 1000;
    setSelectLimit(0);

    String lsSql = "select ac_no,ac_full_name "
            + " ,ac_no||'_'||ac_full_name as inter_desc "
            + " from gen_acct_m "
            + " where ac_no like :ac_no "
            + " order by ac_no ";
    setString("ac_no", wp.getValue("ex_ac_no", 0) + "%");
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "ac_no"));
    }

    return;
  }
  
}
