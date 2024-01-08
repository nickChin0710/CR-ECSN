/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package busi.func;
/**PP-card新貴通公用程式
 * 2019-0610:  JH    p_seqno >>acno_p_seqno
 *  V.2018-0907.jh
 *
 * */

import busi.FuncBase;

public class UcPpcard extends FuncBase {

  public String idPseqno = "", idNo = "", idnoCode = ""; // is_id, is_id_code
  public String idnoName = "", idnoSex = "";
  public String mchtNo = "4889754660";
  public double imHolderAmt = 0;
  public double imGuestAmt = 0;

  public int readIdno(String aId, String aIdCode) {
    idNo = nvl(aId);
    idnoCode = nvl(aIdCode, "0");
    return readIdno();
  }

  public int cardPP() {
    msgOK();

    if (empty(idNo) && empty(idPseqno)) {
      errmsg("身分證ID 不可空白");
      return rc;
    }

    if (empty(idPseqno)) {
      if (readIdno(idNo, idnoCode) != 1)
        return -1;
    }

    return 1;
  }

  public int readIdno() {
    idPseqno = "";
    idnoName = "";
    idnoSex = "";

    if (empty(idNo)) {
      errmsg("身分證ID 不可空白");
      return rc;
    }

    strSql =
        "select id_p_seqno, chi_name, sex" + " from crd_idno" + " where id_no =? and id_no_code =?";
    setString2(1, idNo);
    setString(nvl(idnoCode, "0"));
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("身分證ID 不存在");
      return -1;
    }
    idPseqno = colStr("id_p_seqno");
    idnoName = colStr("chi_name");
    idnoSex = colStr("sex");
    return 1;
  }

  public int cardCount() {
    msgOK();
    if (empty(idNo) && empty(idPseqno)) {
      errmsg("身分證ID 不可空白");
      return -1;
    }
    if (empty(idPseqno)) {
      if (readIdno() != 1)
        return -1;
    }

    strSql =
        "select count(*) as xx_cnt" + " from crd_card_pp" + " where id_p_seqno =?"
            + " and current_code ='0'";
    setString2(1, idPseqno);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      return -1;
    }

    return colInt("xx_cnt");
  }

  public int applyCheck(String asId, String asIdCode) {
    msgOK();

    // -crd-card_pp-
    int liRc = cardCount();
    if (liRc < 0)
      return -1;
    if (liRc > 0) {
      errmsg("卡友已有 新貴通卡 不可再重複申請");
      return -1;
    }

    // -crd-ppcard_stop-
    int llCnt = 0;
    strSql =
        "select count(*) as xx_cnt" + " from crd_ppcard_stop" + " where id_p_seqno =?"
            + " and proc_flag <>'Y'";
    setString2(1, idPseqno);
    sqlSelect(strSql);
    if (sqlRowNum > 0 && colInt("xx_cnt") > 0) {
      errmsg("卡友有 [新貴通卡] 停用待處理中");
      return -1;
    }

    // -crd_ppcard_apply-
    strSql =
        "select count(*) as xx_cnt" + " from	crd_ppcard_apply" + " where id_p_seqno =?"
            + " and nvl(proc_flag,'x')<>'Y'";
    setString2(1, idPseqno);
    sqlSelect(strSql);
    if (sqlRowNum > 0 && colInt("xx_cnt") > 0) {
      errmsg("卡友已申請 [新貴通卡] 待處理中");
      return -1;
    }

    return 1;
  }

  public int getCardholder(String asPpCardNo) {
    msgOK();

    String lsPpcardNo = nvl(asPpCardNo);
    if (empty(lsPpcardNo)) {
      errmsg("新貴通卡號 不可空白");
      return 0;
    }

    idPseqno = "";
    strSql = "select id_p_seqno from crd_card_pp" + " where pp_card_no =?";
    setString2(1, lsPpcardNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("非本行之 新貴通卡");
      return -1;
    }

    idPseqno = colStr("id_p_seqno");

    strSql = " select id_no , id_no_code from crd_idno where id_p_seqno = ? ";
    setString2(1, idPseqno);
    sqlSelect(strSql);

    if (sqlRowNum <= 0) {
      errmsg("非本行之 新貴通卡");
      return -1;
    }

    idNo = colStr("id_no");
    idnoCode = colStr("id_no_code");


    return 1;
  }

  public int getFreeCnt(String asPpcardNo, String asVisitDate) {
    int liRcCnt = 0, liMax = 0, liFree = 0, liUse = 0;
    String lsPpcardNo = "", lsDate = "", lsYm = "";
    int liMax2 = 0, liFree2 = 0, liUse2 = 0;

    lsPpcardNo = nvl(asPpcardNo);
    lsDate = nvl(asVisitDate);
    if (empty(lsPpcardNo) || empty(lsDate))
      return 0;

    lsYm = commString.left(lsDate, 6);
    strSql =
        "SELECT max_cnt, free_cnt, use_free_cnt" + " FROM mkt_ppcard_use" + " WHERE pp_card_no =?" // :ls_ppcard_no
            + " and end_ym >=?" // :ls_ym
            + " and str_ym <=?" // :ls_ym
            + " order by cnt_cond desc";
    setString2(1, lsPpcardNo);
    setString(lsYm);
    setString(lsYm);
    sqlSelect(strSql);
    for (int ll = 0; ll < sqlRowNum; ll++) {
      liMax = colInt(ll, "max_cnt");
      liFree = colInt(ll, "free_cnt");
      liUse = colInt(ll, "use_free_cnt");
      if (liMax > liMax2)
        liMax2 = liMax;

      liFree2 += colInt("free_cnt");
      liUse2 += colInt("use_free_cnt");
    }

    // -over max-cnt-
    if (liUse2 >= liMax2)
      return 0;
    if (liMax2 >= liFree2) {
      liRcCnt = liFree2 - liUse2;
    } else {
      liRcCnt = liMax2 - liUse2;
    }
    if (liRcCnt < 0)
      return 0;

    return liRcCnt;
  }

  public int getVmjAmt(String asBinType) {
    // , ref decimal am_holder_amt, ref decimal am_guest_amt);string ls_vmj_code

    imHolderAmt = 0;
    imGuestAmt = 0;

    if (empty(asBinType))
      return 0;

    strSql = "select holder_amt, toget_amt" + " from mkt_ppcard_issue" + " where bin_type =?";
    setString2(1, asBinType);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("mkt_ppcard_issue error, " + this.sqlErrtext);
      return -1;
    }
    imHolderAmt = colNum("holder_amt");
    imGuestAmt = colNum("toget_amt");
    return 1;
  }

  public int freeCntSub(String asPpcardNo, String asVisitDate, int aiFree) {
    // -JH:100/1/25: add str_ym-
    String lsPpcardNo = "", lsYm = "";
    String lsRowid = "";
    int liCnt = 0, liRc = 1, liMax = 0, liFree = 0, liAdd = 0, liUse = 0;

    lsPpcardNo = nvl(asPpcardNo);
    lsYm = commString.left(asVisitDate, 6);
    liCnt = aiFree;

    if (empty(lsPpcardNo) || empty(lsYm) || liCnt == 0)
      return 0;

    strSql =
        "SELECT max_cnt, free_cnt, use_free_cnt" + ", hex(rowid) as rowid" + " FROM mkt_ppcard_use"
            + " WHERE pp_card_no =?" // :ls_ppcard_no
            + " and str_ym <=? and end_ym >=?" // :ls_ym, :ls_ym
            + " and use_free_cnt >0" + " order by end_ym asc, static_ym asc";
    setString2(1, lsPpcardNo);
    setString(lsYm);
    setString(lsYm);
    sqlSelect(strSql);
    if (sqlRowNum < 0) {
      errmsg("select mkt_ppcard_use error, kk[%s]", lsPpcardNo);
      return -1;
    }

    int llNrow = sqlRowNum;
    for (int ll = 0; ll < llNrow; ll++) {
      liMax = colInt(ll, "max_cnt");
      liFree = colInt(ll, "free_cnt");
      liUse = colInt(ll, "use_free_cnt");
      if (liUse >= liCnt) {
        liUse = liUse - liCnt;
        liCnt = 0;
      } else {
        liUse = 0;
        liCnt = liCnt - liUse;
      }
      strSql = "update mkt_ppcard_use set" + " use_free_cnt =?," // :li_cnt
          + " mod_time =sysdate" + " where rowid =?";
      setInt2(1, liUse);
      this.setRowId2(2, colStr(ll, "rowid"));
      sqlExec(strSql);
      if (sqlRowNum != 1) {
        errmsg("update MKT_PPCARD_USE error: " + sqlErrtext);
        return -1;
      }
      if (liCnt == 0)
        break;
    }
    // -自費次數 未全部還原-
    if (liCnt > 0)
      return 2;

    return 1;
  }

  public String getCardNo(String asPpcardNo) {
    String lsPpcardNo = "", lsCardNo = "", lsId = "", lsIdcode = "";
    idPseqno = "";
    idNo = "";
    idnoCode = "";

    lsPpcardNo = nvl(asPpcardNo);
    if (empty(lsPpcardNo)) {
      errmsg("PP 卡號 不可空白");
      return "";
    }

    strSql =
        "select A.card_no, A.id_p_seqno,C.id_no, C.id_no_code"
            + " from crd_card A join mkt_ppcard_apply B on A.card_type=B.card_type and A.group_code = B.group_code"
            + " join crd_idno C on C.id_p_seqno=A.id_p_seqno" + " where 1=1"
            + " and A.id_p_seqno in (select id_p_seqno from crd_card_pp where pp_card_no=?)"
            + " order by decode(A.current_code,'0',A.acct_type,A.oppost_date)" + commSqlStr.rownum(1);

    setString2(1, asPpcardNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("PP卡號 不存在, kk[%s]", lsPpcardNo);
      // errmsg("read crd_card error, "+sql_errtext);
      return "";
    }

    this.idPseqno = colStr("id_p_seqno");
    idNo = colStr("id_no");
    idnoCode = colStr("id_no_code");

    return colStr("card_no");
  }

  public int freeCntAdd(String asPpcardNo, String asVisitDate, int aiFree) {

    String lsPpcardNo = "", lsYm = "";
    int liCnt = 0, liRc = 1, liMax = 0, liFree = 0, liAdd = 0, liUse = 0;

    lsPpcardNo = nvl(asPpcardNo);
    lsYm = commString.left(asVisitDate, 6);
    liCnt = aiFree;

    if (empty(lsPpcardNo) || empty(lsYm) || liCnt == 0)
      return 0;

    strSql =
        "select max_cnt, free_cnt, use_free_cnt" + ", hex(rowid) as rowid" + " from mkt_ppcard_use"
            + " where pp_card_no =?" // :ls_ppcard_no
            + " and str_ym >=? and end_ym <=?" // :ls_ym,:ls_ym
            + " order by end_ym asc, static_ym asc";
    setString2(1, lsPpcardNo);
    setString(lsYm);
    setString(lsYm);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      return 1;
    }

    int llNrow = sqlRowNum;
    for (int ll = 0; ll <= llNrow; ll++) {
      liMax = colInt(ll, "max_cnt");
      liFree = colInt(ll, "free_cnt");
      liUse = colInt(ll, "use_free_cnt");
      if (liFree > liMax)
        liFree = liMax;
      if (liUse > liFree)
        continue;
      liAdd = liFree - liUse;
      if (liAdd > liCnt) {
        liAdd = liCnt;
        liCnt = 0;
      } else {
        liCnt = liCnt - liAdd;
      }

      strSql =
          "update mkt_ppcard_use set" + " use_free_cnt =use_free_cnt +?" + ", mod_time =sysdate"
              + " where rowid =?";
      setInt2(1, liAdd);
      setRowId2(2, colStr(ll, "rowid"));
      sqlExec(strSql);
      if (sqlRowNum != 1) {
        errmsg("update MKT_PPCARD_USE error: " + sqlErrtext);
        return -1;
      }

      if (liCnt == 0)
        break;
    }
    // -自費次數-
    // ai_cost =li_cnt
    return liRc;
  }

}
