/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  108/01/02  V1.01.00  Brian       program initial                           *
 *  108/08/12  V1.01.01  Brian       中文姓名增長為200 byte                          *
 *  109-12-15  V1.01.02  tanwei      updated for project coding standard       *
 *  112/08/25  V1.01.03  Wilson      行動電話空白要補0                              *
 *  112/09/05  V1.01.04  Wilson      地址空白要補NA                                *
 *  112/09/07  V1.01.05  Wilson      無值都改成填空白                                                                                      *
 *  112/09/14  V1.01.06  Wilson      調整為報送十天內的資料                                                                           *
 ******************************************************************************/

package Ich;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommCpi;

public class IchF006 extends AccessDAO {
    private String progname = "卡人資料檔(B06B)產生  112/09/14 V1.01.06";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommDate    commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommCpi        comcpi = new CommCpi();

    List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
    String rptId   = "";
    String rptName = "";
    int rptSeq = 0;

    int    debug = 0;
    String hTempUser = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hIcdrMediaCreateDate = "";
    String hIcdrMediaCreateTime = "";
    String hProcDate = "";
    String hIcdrRowid = "";
    String hTnlgFileName = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";
    String tmpstr1 = "";
    String hTfinFileIden = "B06B";


    String hIcdrIchCardNo     = "";
    String hIcdrIdno            = "";
    String hIcdrName            = "";
    String hIcdrBirthday        = "";
    String hIcdrAddr1          = "";
    String hIcdrAddr2          = "";
    String hIcdrMobPhone1     = "";
    String hIcdrMobPhone2     = "";
    String hIcdrPhone1         = "";
    String hIcdrPhone2         = "";
    String hIcdrEMailAddr1   = "";
    String hIcdrEMailAddr2   = "";
    String hIcdrOpCode         = "";
    String hIcdrOpKind         = "";
    String hIcdrSupFlag        = "";
    String hIcdrOldIchCardNo = "";
    String hIcdrCardLevel      = "";


    int forceFlag = 0;
    int totCnt    = 0;
    int hTnlgRecordCnt = 0;
    String hHash   = "";
    String allData = "";

    Buf1 detailSt = new Buf1();
    String out = "";
    //************************************************************************************
    public int mainProcess(String[] args)
    {
        try
        {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : IchF006 [notify_date] [force_flag (Y/N)]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }

            comcr.hCallRProgramCode = this.getClass().getName();
            String hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }


            hTnlgNotifyDate = "";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8)
                    hTnlgNotifyDate = args[0];
            }
            if (args.length == 2) {
                hTnlgNotifyDate = args[0];
                if (args[1].equals("Y"))
                    forceFlag = 1;
            }
            selectPtrBusinday();
            hProcDate = commDate.dateAdd(hTnlgNotifyDate,0,0,-10);

            tmpstr1 = String.format("BRQA_%3.3s_%8.8s_%4.4s",comc.ICH_BANK_ID3,hTnlgNotifyDate,hTfinFileIden);
                    
            showLogMessage("I", "", "Process date=["+forceFlag+"]"+hTnlgNotifyDate+","+ hProcDate +","+tmpstr1);

            hTnlgFileName = tmpstr1;

            if (forceFlag == 0) {
                if (selectIchNotifyLogA() != 0) {
                    String errMsg = String.format("select_ich_notify_log_a error !");
                    comcr.errRtn(errMsg, "",comcr.hCallBatchSeqno);
                }
            } else {
                updateIchB06bIdnoA();
            }

            fileOpen();

            selectIchB06bIdno();

            if(totCnt > 0)
            {
                hHash  = comc.encryptSHA(allData, "SHA-1", "big5");
                tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%537.537s%-40.40s\r\n",hTfinFileIden
                        ,"01", "0001", comc.ICH_BANK_ID3, String.format("%08d",totCnt) + "B", " ", hHash);

                lpar.set(0, comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", tmpstr1));
            }

            hTnlgRecordCnt = totCnt;

            fileClose();


            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)    comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        } catch (Exception ex)
        { expMethod = "mainProcess"; expHandle(ex); return exceptExit;
        }
    }
    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        
        hBusiBusinessDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_icdr_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_icdr_media_create_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "",comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate     = getValue("business_date");
            hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
            hIcdrMediaCreateDate = getValue("h_icdr_media_create_date");
            hIcdrMediaCreateTime = getValue("h_icdr_media_create_time");
        }

    }
    /***********************************************************************/
    int selectIchNotifyLogA() throws Exception {
        hTnlgMediaCreateDate = "";
        hTnlgFtpSendDate = "";

        sqlCmd  = "select media_create_date,";
        sqlCmd += " ftp_send_date ";
        sqlCmd += "  from ich_notify_log  ";
        sqlCmd += " where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return 0;
        }
        if (recordCnt > 0) {
            hTnlgMediaCreateDate = getValue("media_create_date");
            hTnlgFtpSendDate     = getValue("ftp_send_date");
        }

        if (hTnlgFtpSendDate.length() != 0) {
            String stderr = String.format("通知檔 [%s] 已FTP至ICH , 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        if (hTnlgMediaCreateDate.length() != 0) {
            String stderr = String.format("製卡回饋檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateIchB06bIdnoA() throws Exception {
        daoTable  = "ich_b06b_idno";
        updateSQL = "proc_flag       = 'N'";
        whereStr  = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", hTnlgFileName
                    , comcr.hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    void fileOpen() throws Exception
    {
        String temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        out = temstr1;


  /*selectSQL  = " count(*)   as all_cnt ";
  daoTable   = " ich_card_parm b, ich_b06b_idno a ";
  whereStr   = " where 1=1               ";
  whereStr  += "   and (a.proc_flag = 'N' or decode(a.proc_flag,'', 'N',a.proc_flag) = 'N') ";
  whereStr  += "   and b.card_code  = a.card_code ";
  int recCnt = selectTable();
  */

        hHash = "0000000000000000000000000000000000000000";
        tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%537.537s%-40.40s\r\n",hTfinFileIden,"01"
                ,"0001",comc.ICH_BANK_ID3, "00000000B"," ",hHash);

        lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", tmpstr1));
    }
    /***********************************************************************/
    void fileClose() throws Exception {
        comc.writeReport(out, lpar, "big5", false);
    }
    /***********************************************************************/
    void selectIchB06bIdno() throws Exception
    {
        sqlCmd = "select ";
        sqlCmd += "a.ich_card_no,";
        sqlCmd += "a.id_no,";
        sqlCmd += "a.name,";
        sqlCmd += "a.birthday,";
        sqlCmd += "a.addr_1,";
        sqlCmd += "a.addr_2,";
        sqlCmd += "substr(a.mob_phone_1,1,10) as mob_phone_1, ";
        sqlCmd += "substr(a.mob_phone_2,1,10) as mob_phone_2,";
        sqlCmd += "a.phone_1,";
        sqlCmd += "a.phone_2,";
        sqlCmd += "a.e_mail_addr_1,";
        sqlCmd += "a.e_mail_addr_2,";
        sqlCmd += "decode(a.op_code, 'A', 'I', a.op_code) op_code,";
        sqlCmd += "a.op_kind,";
        sqlCmd += "a.sup_flag,";
        sqlCmd += "decode(a.old_ich_card_no, '', '0000000000000000', a.old_ich_card_no) old_ich_card_no,";
        sqlCmd += "a.card_level,";
        sqlCmd += "a.rowid as rowid1 ";
        sqlCmd += "from  ich_b06b_idno a ";
        sqlCmd += "where 1=1 ";
//        sqlCmd += "  and (a.proc_flag = 'N' or decode(a.proc_flag,'', 'N',a.proc_flag) = 'N') ";
        sqlCmd += "  and a.sys_date between ? and ? ";
        sqlCmd += "  order by a.sys_date,a.sys_time ";
        setString(1, hProcDate);
        setString(2, hTnlgNotifyDate);
        
        openCursor();
        while (fetchTable()) {
            hIcdrIchCardNo = getValue("ich_card_no");
            hIcdrIdno = getValue("id_no");
            hIcdrName = getValue("name");
            hIcdrBirthday = getValue("birthday");
            hIcdrAddr1 = getValue("addr_1");
            hIcdrAddr2 = getValue("addr_2");
            hIcdrMobPhone1 = getValue("mob_phone_1");
            hIcdrMobPhone2 = getValue("mob_phone_2");
            hIcdrPhone1 = getValue("phone_1");
            hIcdrPhone2 = getValue("phone_2");
            hIcdrEMailAddr1 = getValue("e_mail_addr_1");
            hIcdrEMailAddr2 = getValue("e_mail_addr_2");
            hIcdrOpCode = getValue("op_code");
            hIcdrOpKind = getValue("op_kind");
            hIcdrSupFlag = getValue("sup_flag");
            hIcdrOldIchCardNo = getValue("old_ich_card_no");
            hIcdrCardLevel = getValue("card_level");
            hIcdrRowid = getValue("rowid1");


            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

            writeRtn();
            updateIchB06bIdno();

        }
        closeCursor();
    }
    /***********************************************************************/
    void writeRtn() throws Exception
    {

        String tmpstr = "";

        detailSt = new Buf1();

        detailSt.type = "D";

        tmpstr = String.format("%-16.16s", hIcdrIchCardNo);
        detailSt.ichCardNo = tmpstr;
        tmpstr = String.format("%-20.20s", hIcdrIdno);
        detailSt.idno = tmpstr;
        tmpstr = comcpi.commTransChinese(String.format("%-200.200s", hIcdrName));
        detailSt.name = tmpstr;
        tmpstr = String.format("%-8.8s", hIcdrBirthday);
        detailSt.birthday = tmpstr;
//        if(hIcdrAddr1.length() == 0)
//        	hIcdrAddr1 = "NA";
        tmpstr = comcpi.commTransChinese(String.format("%-80.80s", hIcdrAddr1));
        detailSt.addr1 = tmpstr;
//        if(hIcdrAddr2.length() == 0)
//        	hIcdrAddr2 = "NA";
        tmpstr = comcpi.commTransChinese(String.format("%-80.80s", hIcdrAddr2));
        detailSt.addr2 = tmpstr;        
        if(hIcdrMobPhone1.length() == 0)
        	hIcdrMobPhone1 = "0000000000";
        tmpstr = String.format("%-10.10s"  , hIcdrMobPhone1);
        detailSt.mobPhone1      = tmpstr;
        tmpstr = String.format("%-10.10s"  , hIcdrMobPhone2);
        detailSt.mobPhone2      = tmpstr;
        if(hIcdrPhone1.length() < 4)
            hIcdrPhone1 = "000-0";
        tmpstr = String.format("%-20.20s"  , hIcdrPhone1);
        detailSt.phone1      = tmpstr;
//        if(hIcdrPhone2.length() < 4)
//            hIcdrPhone2 = "NA";
        tmpstr = String.format("%-20.20s"  , hIcdrPhone2);
        detailSt.phone2      = tmpstr;
        if(hIcdrEMailAddr1.equals("NA"))
        	hIcdrEMailAddr1 = "";
        tmpstr = String.format("%-50.50s"  , hIcdrEMailAddr1);
        detailSt.eMailAddr1      = tmpstr;
        if(hIcdrEMailAddr2.equals("NA"))
        	hIcdrEMailAddr2 = "";
        tmpstr = String.format("%-50.50s"  , hIcdrEMailAddr2);
        detailSt.eMailAddr2      = tmpstr;
        tmpstr = String.format("%-1.1s"  , hIcdrOpCode);
        detailSt.opCode      = tmpstr;
        tmpstr = String.format("%-1.1s"  , hIcdrOpKind);
        detailSt.opKind      = tmpstr;
        tmpstr = String.format("%-1.1s"  , hIcdrSupFlag);
        detailSt.supFlag      = tmpstr;
        tmpstr = String.format("%-16.16s"  , hIcdrOldIchCardNo);
        detailSt.oldIchCardNo      = tmpstr;
        tmpstr = String.format("%-2.2s"  , hIcdrCardLevel);
        detailSt.cardLevel      = tmpstr;

        tmpstr = String.format("%-14.14s", sysDate+sysTime  );
        detailSt.sysDatetime  = tmpstr;

        detailSt.fillerEnd    = "\r\n";

        String buf = detailSt.allText();
        lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        allData += buf;

        if(debug==1)
            showLogMessage("I", "", "  Write =["+buf+"]"+ buf.getBytes("big5").length+","
                    + allData.getBytes("big5").length);

        return;
    }
    /***********************************************************************/
    void updateIchB06bIdno() throws Exception
    {
        daoTable   = "ich_b06b_idno";
        updateSQL  = " media_create_date = ?,";
        updateSQL += " media_create_time = ?,";
        updateSQL += " file_name         = ?,";
        updateSQL += " proc_flag         = 'Y',";
        updateSQL += " mod_pgm           = ?,";
        updateSQL += " mod_time          = sysdate";
        whereStr   = "where rowid        = ? ";
        setString(1, hIcdrMediaCreateDate);
        setString(2, hIcdrMediaCreateTime);
        setString(3, hTnlgFileName);
        setString(4, javaProgram);
        setRowId( 5, hIcdrRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", "",comcr.hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception
    {
        IchF006 proc = new IchF006();
        int  retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
    class Buf1 {
        String type;
        String ichCardNo;
        String idno;
        String name;
        String birthday;
        String addr1;
        String addr2;
        String mobPhone1;
        String mobPhone2;
        String phone1;
        String phone2;
        String eMailAddr1;
        String eMailAddr2;
        String opCode;
        String opKind;
        String supFlag;
        String oldIchCardNo;
        String cardLevel;
        String sysDatetime;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(type, 1);
            rtn += fixLeft(ichCardNo, 16);
            rtn += fixLeft(idno, 20);
            rtn += fixLeft(name, 200);
            rtn += fixLeft(birthday, 8);
            rtn += fixLeft(addr1, 80);
            rtn += fixLeft(addr2, 80);
            rtn += fixLeft(mobPhone1, 10);
            rtn += fixLeft(mobPhone2, 10);
            rtn += fixLeft(phone1, 20);
            rtn += fixLeft(phone2, 20);
            rtn += fixLeft(eMailAddr1, 50);
            rtn += fixLeft(eMailAddr2, 50);
            rtn += fixLeft(opCode, 1);
            rtn += fixLeft(opKind, 1);
            rtn += fixLeft(supFlag, 1);
            rtn += fixLeft(oldIchCardNo, 16);
            rtn += fixLeft(cardLevel, 2);
            rtn += fixLeft(sysDatetime, 14);
            rtn += fixLeft(fillerEnd, 2);
            return rtn;
        }

        String fixLeft(String str, int len) throws UnsupportedEncodingException {
            int size = (Math.floorDiv(len, 100) + 1) * 100;
            String spc = "";
            for (int i = 0; i < size; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = str + spc;
            byte[] bytes = str.getBytes("big5");
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, 0, vResult, 0, len);

            return new String(vResult, "big5");
        }
    }

}
