/**
 *  ECS_ADDR_LABEL: 地址名條公用程式 V.2018-0807.00
 * 2018-0807:	JH		initial
 * 109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
 * 109-04-20  V1.00.01  Zuwei       code format                              *
 * 
 * */
package busi.ecs;

import busi.FuncBase;

public class EcsAddrLabel extends FuncBase {

  // public String crt_date="";
  // public String crt_user =""; // 建檔經辦
  public String crtPgm = ""; // 建檔程式
  public String crtDesc = ""; // 名條產生說明
  public String batchNo = ""; // 產生批號
  public String cardNo = ""; // 卡號
  public String idPseqno = ""; // 卡人序號
  public String idNo = ""; // 身分證號
  public String chiName = ""; // 姓名
  public String idnoSex = ""; // 稱謂
  public String addrZip = ""; // 地址郵遞區號
  public String addr1 = ""; // 地址1
  public String addr2 = ""; // 地址2
  public String addr3 = ""; // 地址3
  public String addr4 = ""; // 地址4
  public String addr14 = ""; // 地址14
  public String addr5 = ""; // 地址5
  public String remark1 = ""; // 備註1-左上
  public String remark2 = ""; // 備註2-右上
  public String remark3 = ""; // 備註3-左下
  public String remark4 = ""; // 備註4-右下

  int batchSeq = 0; // 批號序號

  public void setBatchSeq(int num) {
    batchSeq = num;
  }

  public void clear() {
    // crt_user ="";
    crtPgm = "";
    crtDesc = "";
    batchNo = "";
    // batch_seq =0;
    cardNo = "";
    idPseqno = "";
    idNo = "";
    chiName = "";
    idnoSex = "";
    addrZip = "";
    addr1 = "";
    addr2 = "";
    addr3 = "";
    addr4 = "";
    addr14 = "";
    addr5 = "";
    remark1 = "";
    remark2 = "";
    remark3 = "";
    remark4 = "";

  }

  public int dataInsert() {
    this.msgOK();
    batchSeq++;

    strSql =
        "insert into ecs_addr_label (" + " crt_date," + " crt_user," + " crt_pgm   ,"
            + " crt_desc  ," + " batch_no  ," + " batch_seq ," + " card_no   ," + " id_p_seqno,"
            + " id_no     ," + " chi_name  ," + " idno_sex  ," + " addr_zip  ," + " addr_1    ,"
            + " addr_2    ," + " addr_3    ," + " addr_4    ," + " addr_14   ," + " addr_5    ,"
            + " remark_1  ," + " remark_2  ," + " remark_3  ," + " remark_4  ,"
            + " mod_user, mod_time, mod_pgm, mod_seqno" + " ) values ("
            + commSqlStr.sysYYmd
            + ","
            + " :crt_user,"
            + " :crt_pgm   ,"
            + " :crt_desc  ,"
            + " :batch_no  ,"
            + " :batch_seq ,"
            + " :card_no   ,"
            + " :id_p_seqno,"
            + " :id_no     ,"
            + " :chi_name  ,"
            + " :idno_sex  ,"
            + " :addr_zip  ,"
            + " :addr_1    ,"
            + " :addr_2    ,"
            + " :addr_3    ,"
            + " :addr_4    ,"
            + " :addr_14   ,"
            + " :addr_5    ,"
            + " :remark_1  ,"
            + " :remark_2  ,"
            + " :remark_3  ,"
            + " :remark_4  ,"
            + " :mod_user,"
            + commSqlStr.sysdate
            + ", :mod_pgm, 1"
            + " )";

    setString2("crt_user", modUser);
    setString2("crt_pgm", crtPgm);
    setString2("crt_desc", crtDesc);
    setString2("batch_no", batchNo);
    setInt2("batch_seq", batchSeq);
    setString2("card_no", cardNo);
    setString2("id_p_seqno", idPseqno);
    setString2("id_no", idNo);
    setString2("chi_name", chiName);
    setString2("idno_sex", idnoSex);
    setString2("addr_zip", addrZip);
    setString2("addr_1", addr1);
    setString2("addr_2", addr2);
    setString2("addr_3", addr3);
    setString2("addr_4", addr4);
    setString2("addr_14", addr14);
    setString2("addr_5", addr5);
    setString2("remark_1", remark1);
    setString2("remark_2", remark2);
    setString2("remark_3", remark3);
    setString2("remark_4", remark4);
    setString2("mod_user", modUser);
    setString2("mod_pgm", modPgm);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert ECS_ADDR_LABEL error");
    }

    return rc;
  }

}
