/******************************************************************************
*                                                                             *
*                               MODIFICATION LOG                              *
*                                                                             *
*    DATE     Version    AUTHOR              DESCRIPTION                      *
*  --------   -------------------  ------------------------------------------ *
*  103/02/24  V1.00.01   Alan      RECS-s1020315-032 Initial                  *
*  107/11/01  V1.00.01   Brian     Transfer to JAVA                           *
*  109/07/06  V1.00.02    Zuwei     coding standard, rename field method & format                   *
*  109/07/22  V1.00.03   Zuwei     coding standard, rename field method                   *
*  111/01/20  V1.00.04   Justin    fix Setting Manipulation
*******************************************************************************/
package com;

import java.sql.Connection;

public class CommMsgSendAttach extends AccessDAO {
  private final String PROGNAME = "comm_msg_func 傳送(含記錄檔)相關副程式 V1.00.01 103/02/24";

  CommCrd comc = new CommCrd();

  public String hPmsgSystemName = "";
  public String hPmsgPgmName = "";

  private String hPmsgSenderDeptNo = "";
  private String hPmsgGroupId = "";
  private String hPmsgSubject = "";
  private String hPmsgContents = "";
  private String hPmsgAttachFilename = "";

  public CommMsgSendAttach(Connection conn[], String[] dbAlias) throws Exception {
    // TODO Auto-generated constructor stub
    super.conn = conn;
    setDBalias(dbAlias);
    setSubParm(dbAlias);

    return;
  }

  /*****************************************************************************/
  public int commMsgSendAttach(String senderDeptNo, String groupId, String subject,
      String contents, String attachFilename) throws Exception {

    showLogMessage("I", "", String.format("\n%s", PROGNAME));
    initPtrMessageHst();

    /*** 主旨最長100字元、內文最長500字元、附件檔限一個 檔名最長100字元 暫不檢核 直接截斷 ***/
    if (hPmsgSystemName.equals(""))
      hPmsgSystemName = "ECS";
    if (hPmsgPgmName.equals(""))
      hPmsgPgmName = "MSG_FUNC";
    hPmsgSenderDeptNo = senderDeptNo;
    hPmsgGroupId = groupId;
    hPmsgSubject = subject;
    hPmsgContents = contents;
    hPmsgAttachFilename = attachFilename;

    if (hPmsgAttachFilename.equals("") == false) {
      /*** 傳送附件檔案到FTP ***/
      // ======================================================
      // FTP

      CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
      CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

      commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
      commFTP.hEflgSystemId = "msg_func"; /* 區分不同類的 FTP 檔案-大類 (必要) */
      commFTP.hEflgGroupId = "msg_func"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
      commFTP.hEflgSourceFrom = "電子郵件"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
      commFTP.hEriaLocalDir = String.format("%s/media/msg", comc.getECSHOME());
      commFTP.hEflgModPgm = this.getClass().getName();
      String hEflgRefIpCode = "MSG_FTP";

//      System.setProperty("user.dir", commFTP.hEriaLocalDir); // fix Setting Manipulation

      String procCode = String.format("put %s %s#%s#%s", hPmsgAttachFilename, hPmsgSystemName,
          hPmsgPgmName, hPmsgAttachFilename);
      showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

      int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

      if (errCode != 0) {
        showLogMessage("I", "",
            String.format("附件檔[%s]傳送失敗[%s][%s]", hPmsgAttachFilename, "1", "2"));
        return (1);
      }
      // ==================================================
    }
    /*** 寫入訊息發送交易到資料庫 ***/
    if (insertPtrMessageHst() == 0) {
      showLogMessage("I", "", String.format("訊息發送登錄完成"));
      return 0;
    } else {
      showLogMessage("I", "", String.format("訊息發送登錄失敗 error"));
      return (1);
    }
  }

  /*****************************************************************************/
  public int commMsgSend(String senderDeptNo, String groupId, String subject, String contents)
      throws Exception {
    return commMsgSendAttach(senderDeptNo, groupId, subject, contents, "");
  }

  /*****************************************************************************/
  void initPtrMessageHst() {
    hPmsgSenderDeptNo = "";
    hPmsgGroupId = "";
    hPmsgSubject = "";
    hPmsgContents = "";
    hPmsgAttachFilename = "";
  }

  /*****************************************************************************/
  int insertPtrMessageHst() {
    daoTable = "ptr_message_hst";

    setValue("system_name", hPmsgSystemName.equals("") ? "ECS" : hPmsgSystemName);
    setValue("pgm_name", hPmsgPgmName.equals("") ? "MSG_FUNC" : hPmsgPgmName);
    setValue("create_time", sysDate + sysTime);
    setValue("sender_dept_no", hPmsgSenderDeptNo);
    setValue("group_id", hPmsgGroupId);
    setValue("subject", hPmsgSubject);
    setValue("contents", hPmsgContents);
    setValue("attach_filename", hPmsgAttachFilename);
    try {
      insertTable();
      if (dupRecord.equals("Y"))
        return 1;
    } catch (Exception e) {
      return 1;
    }
    return 0;
  }
}
