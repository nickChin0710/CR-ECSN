/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------  *
*  106/06/01  V1.00.00    SUP       program initial                          *
*  109/12/24  V1.00.01   shiyuqi    updated for project coding standard      *
*  112/12/03  V1.00.02   Wilson     crd_item_unit不判斷卡種                                                           *  
*  112/12/14  V1.00.03   Wilson     insert crd_whtrans增加覆核欄位                                         *
*****************************************************************************/

package Crd;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;

/*PP卡*/
public class CrdM005 extends AccessDAO {
    public final boolean debugMode = false;
    private String progname = "PP卡 系統自動扣除庫   112/12/14  V1.00.03 ";
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;

    String hTempUser = "";

    String hCallBatchSeqno = "";
    String hCallRProgramCode = "";

    String hBusinessDate = "";
    String hSystemDate = "";
    String hBatchno1 = "";
    String hBatchno2 = "";
    String hWareYear = "";
    int hWareMonth = 0;
    String hEmbpUnitCode = "";
    String hEmbpCardType = "";
    String hEmbpGroupCode = "";
    String hEmbpSourceCode = "";
    String hEmbpEmbossSource = "";
    String hEmbpPpCardNo = "";
    String unitCardItem  = "";
    String hEmbpRowid = "";
    String hEhtrEarehouseNo = "";
    String hWhtrCardItem = "";
    String hWhtrPlace = "";
    String hKeyCardtype = "";
    String hKeyUnitcode = "";
    String hWhtrWarehouseDate = "";
    String hWhtrCardType = "";
    String hWhtrGroupCode = "";
    String hWhtrUnitCode = "";
    String hWhtrTnsType = "";
    String hWhtrCreateDate = "";
    String hWhtrTransReason = "";
    String hWhtrCreateId = "";
    String hWhtrModUser = "";
    String hWhtrModPgm = "";
    String hWhtrModWs = "";
    String hWhtrModLog = "";
    String hWareWhYear = "";
    String hWareCardItem = "";
    String hWarePlace = "";
    int hWareOutQty01 = 0;
    int hWareOutQty02 = 0;
    int hWareOutQty03 = 0;
    int hWareOutQty04 = 0;
    int hWareOutQty05 = 0;
    int hWareOutQty06 = 0;
    int hWareOutQty07 = 0;
    int hWareOutQty08 = 0;
    int hWareOutQty09 = 0;
    int hWareOutQty10 = 0;
    int hWareOutQty11 = 0;
    int hWareOutQty12 = 0;
    String hWareModUser = "SYSTEM";
    String hWareModPgm = javaProgram;
    String hWareModWs = "R6";
    int hWhtrPrevTotal = 0;
    int hWhtrUseTotal = 0;
    int hWhtrModSeqno = 0;
    String hRealUnitcode = "";
    String hKeyGroupcode = "";
    String hkeyCardItem = "";

    int hCardQty = 0;
    int totCnt = 0;
    int liErr = 0;

    int tmpInt = 0;
    double hPreTotalBal  = 0;
    double hAllTotalBal  = 0;
    String hWhtrYear      = "";
    int    hWhtrMonth     = 0;
    int    seqNo           = 0;
    String hWareRowid     = "";
    String hWareLotNo    = "";
    double hWareItemAmt  = 0;
    double tmpQty          = 0;

    // ********************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : CrdM005 [callbatch_seqno]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }

            comcr.hCallRProgramCode = javaProgram;
            hTempUser = "";
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

            commonRtn();

            selectCrdEmbossPp();

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void commonRtn() throws Exception {
        sqlCmd = "select business_date ";
        sqlCmd += "  from ptr_businday ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusinessDate = getValue("business_date");

        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += "  from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
        }

        sqlCmd = "select to_char(to_number(to_char(sysdate,'yyyy')) - 1911)||'00001' h_batchno1, ";
        sqlCmd += " to_char(to_number(to_char(sysdate,'yyyy')) - 1911)||'99999' h_batchno2  ";
        sqlCmd += " from dual ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hBatchno1 = getValue("h_batchno1");
            hBatchno2 = getValue("h_batchno2");
        }

        sqlCmd = "select to_char  (sysdate,'yyyy')        h_ware_year,";
        sqlCmd += " to_number(to_char(sysdate,'mm')) h_ware_month ";
        sqlCmd += " from dual ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hWareYear  = getValue("h_ware_year");
            hWareMonth = getValueInt("h_ware_month");
            hWhtrYear  = hWareYear;
            hWhtrMonth = hWareMonth;
        }
    }
/***********************************************************************/
void selectCrdEmbossPp() throws Exception 
{
        sqlCmd = "select ";
        sqlCmd += " decode(a.unit_code,'',a.group_code,a.unit_code) as unit_code , ";
        sqlCmd += " a.card_type, ";
        sqlCmd += " a.group_code, ";
        sqlCmd += " a.source_code, ";
        sqlCmd += " a.emboss_source, ";
        sqlCmd += " a.pp_card_no, ";
        sqlCmd += " b.card_item, ";
        sqlCmd += " a.rowid  as rowid ";
        sqlCmd += " from crd_emboss_pp a, crd_item_unit b ";
        sqlCmd += "where a.unit_code = b.unit_code ";
        sqlCmd += "  and decode(a.in_main_error,'','0',a.in_main_error) = '0' ";
        sqlCmd += "  and a.to_vendor_flag  = 'Y' ";
        sqlCmd += "  and a.wh_flag         = 'N' ";
        sqlCmd += "order by b.card_item ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hEmbpUnitCode     = getValue("unit_code", i);
            hEmbpCardType     = getValue("card_type", i);
            hEmbpGroupCode    = getValue("group_code", i);
            hEmbpSourceCode   = getValue("source_code", i);
            hEmbpEmbossSource = getValue("emboss_source", i);
            hEmbpPpCardNo    = getValue("pp_card_no", i);
            unitCardItem     = getValue("card_item", i);
            hEmbpRowid         = getValue("rowid", i);

            // pp 卡相同
            if(hEmbpUnitCode.length() == 0)
              {
               hEmbpUnitCode = hEmbpGroupCode;
              }
if(debug == 1)
   showLogMessage("I", "", "Read pp_card=["+hEmbpPpCardNo+"]"+totCnt+","+hEmbpUnitCode);
            
            liErr = 0;
            hRealUnitcode = hEmbpUnitCode;

            if(!hkeyCardItem.equals(unitCardItem)) {
                if (hCardQty > 0) {
                    crdWhtransInsert(1);
                    crdWarehouseUpdate(1);
                }
                hKeyUnitcode  = hRealUnitcode;
                hKeyCardtype  = hEmbpCardType;
                hKeyGroupcode = hEmbpGroupCode;
                hkeyCardItem  = unitCardItem;
                hCardQty = 0;
            }

            hCardQty++;
            totCnt++;
if(debug == 1)
   showLogMessage("I", "", "  card_qty =["+hCardQty+"]"+totCnt);

            daoTable  = "crd_emboss_pp";
            updateSQL = "wh_flag      = 'Y'";
            whereStr  = "where rowid  = ? ";
            setRowId(1, hEmbpRowid);

            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_emboss_pp not found!", "", comcr.hCallBatchSeqno);
            }
        }

    if(hCardQty > 0)
      {
       tmpInt = crdWhtransInsert(2);
       if(tmpInt == 0)   tmpInt = crdWarehouseUpdate(2);

       if (tmpInt == 0) { commitDataBase(); }
       else {
             rollbackDataBase();
             showLogMessage("I", "", "***  ROLLBACK 2****");
            }
      }
}
/************************************************************************/
/* Insert 卡片庫存異動檔 */
/************************************************************************/
int crdWhtransInsert(int idx) throws Exception 
{

        hEhtrEarehouseNo   = "";
        hWhtrCardItem      = "";
        hWhtrWarehouseDate = sysDate;
        hWhtrCardType      = hKeyCardtype;
        hWhtrGroupCode     = hKeyGroupcode;
        hWhtrUnitCode      = hKeyUnitcode;
        hWhtrTnsType       = "2";
        hWhtrPrevTotal     = 0;
        hWhtrUseTotal      = hCardQty;
        hWhtrCreateDate    = sysDate;
        hWhtrTransReason   = "1";
        hWhtrCreateId      = "SYSTEM";
        hWhtrModUser       = "SYSTEM";
        hWhtrModPgm        = javaProgram;
        hWhtrModWs         = "R6";
        hWhtrModSeqno      = 0;
        hWhtrModLog        = "1";

        /*-- get warehouse_no -------------------------------------------------*/
        sqlCmd = "select to_char(to_number(max(warehouse_no)) + 1) h_whtr_warehouse_no ";
        sqlCmd += " from crd_whtrans  ";
        sqlCmd += "where warehouse_no between ? and ? ";
        setString(1, hBatchno1);
        setString(2, hBatchno2);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hEhtrEarehouseNo = getValue("h_whtr_warehouse_no");
        }

        if (hEhtrEarehouseNo.length() == 0)
            hEhtrEarehouseNo = hBatchno1;

        /*-- get card_item ------------------------------------------------*/
        hWhtrPlace = "";
        sqlCmd = "select card_item, ";
        sqlCmd += "new_vendor, ";
        sqlCmd += "mku_vendor, ";
        sqlCmd += "chg_vendor  ";
        sqlCmd += " from crd_item_unit  ";
        sqlCmd += "where unit_code    = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hKeyUnitcode);
        if (debugMode) {
            setString(1, "PV");
            setString(2, "8799");
        }

        recordCnt = selectTable();

  if (debug == 1) showLogMessage("I", "", " select item_unit=" + recordCnt + ","+ hKeyCardtype+","+hKeyUnitcode + ", source=" + hEmbpEmbossSource);

        if (recordCnt > 0) {
            hWhtrCardItem = getValue("card_item");
            switch (hEmbpEmbossSource) {
            case "1":
                hWhtrPlace = getValue("new_vendor");
                break; // * 新製卡
            case "3":
            case "4":
                hWhtrPlace = getValue("chg_vendor");
                break; // * 續卡(換卡)
            default:
                hWhtrPlace = getValue("mku_vendor");
                break; // * 重製卡(補發卡)
            }
        }
        if (hWhtrCardItem.length() == 0) {
            String errMsg = String.format("Read crd_item_unit error [%s][%s]!! ", hKeyCardtype, hKeyUnitcode);
            comcr.errRtn(errMsg, "", comcr.hCallBatchSeqno);
        }

        crdWarehouseSelect(0);   // get prev_total

if(debug == 1)
   showLogMessage("I", "", " insert whtrans="+hAllTotalBal+","+hEhtrEarehouseNo + "," + hWhtrCardItem);

        /*-- Insert CRD_WHTRANS --*/
        daoTable = "crd_whtrans";
        setValue("warehouse_no"  , hEhtrEarehouseNo);
        setValue("card_item"     , hWhtrCardItem);
        setValue("warehouse_date", hWhtrWarehouseDate);
        setValue("card_type"     , hWhtrCardType);
//      setValue("group_code"    , h_whtr_group_code);
        setValue("unit_code"     , hWhtrUnitCode);
        setValue("tns_type"      , hWhtrTnsType);
        setValue("place"           , hWhtrPlace);
        setValueDouble("prev_total", hAllTotalBal);
        setValueInt("use_total" , hWhtrUseTotal);
        setValue("crt_date"     , hWhtrCreateDate);
        setValue("trans_reason" , hWhtrTransReason);
        setValue("apr_flag"     , "Y");
        setValue("apr_user"     , javaProgram);
        setValue("apr_date"     , sysDate);
        setValue("crt_user"     , hWhtrCreateId);
        setValue("mod_user"     , hWhtrModUser);
        setValue("mod_time"     , sysDate + sysTime);
        setValue("mod_pgm"      , hWhtrModPgm);
        setValue("mod_ws"       , hWhtrModWs);
        setValueInt("mod_seqno" , hWhtrModSeqno);
        setValue("mod_log"      , hWhtrModLog);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_whtrans duplicate!", "", comcr.hCallBatchSeqno);
        }

        return (0);
}
/*************************************************************************/
public int crdWarehouseSelect(int idx) throws Exception
{
  hAllTotalBal  = 0;
  selectSQL = "  sum(pre_total   "
            + "  + ( in_qty01+ in_qty02+ in_qty03+ in_qty04+ in_qty05+ in_qty06 + "
            + "      in_qty07+ in_qty08+ in_qty09+ in_qty10+ in_qty11+ in_qty12)  "
            + "  - (out_qty01+out_qty02+out_qty03+out_qty04+out_qty05+out_qty06 + "
            + "     out_qty07+out_qty08+out_qty09+out_qty10+out_qty11+out_qty12)) all_total_bal ";
  daoTable = "crd_warehouse ";
  whereStr = "where wh_year    =  ? "
           + "  and card_item  =  ? "
           + "  and place      =  ? ";

  setString(1, hWhtrYear);
  setString(2, hWhtrCardItem);
  setString(3, hWhtrPlace);

  tmpInt = selectTable();

  hAllTotalBal  = getValueDouble("all_total_bal");

if(debug == 1)
  showLogMessage("I", "", " select whtr=["+hWhtrCardItem+","+hWhtrPlace+"]"+hAllTotalBal);

  return (0);
}
/************************************************************************/
/* Update 卡片庫存檔 */
/***********************************************************************/
int crdWarehouseUpdate(int idx) throws Exception 
{

 if (debug == 1) showLogMessage("I", "", "  crd_warehouse_update [" + idx + "]" + hCardQty);

  double remainCardQty = hCardQty;

  crdWarehouseInit();
  seqNo  = 0;
/* brain */
  selectSQL = "out_qty01  , out_qty02   , out_qty03   , out_qty04   ,"
            + "out_qty05  , out_qty06   , out_qty07   , out_qty08   ,"
            + "out_qty09  , out_qty10   , out_qty11   , out_qty12   ,"
            + "  pre_total   "
            + "  + ( in_qty01+ in_qty02+ in_qty03+ in_qty04+ in_qty05+ in_qty06 + "
            + "      in_qty07+ in_qty08+ in_qty09+ in_qty10+ in_qty11+ in_qty12)  "
            + "  - (out_qty01+out_qty02+out_qty03+out_qty04+out_qty05+out_qty06 + "
            + "     out_qty07+out_qty08+out_qty09+out_qty10+out_qty11+out_qty12) h_pre_total_bal,"
            + "item_amt   , lot_no      , rowid   as rowid    ";
  daoTable = "crd_warehouse ";
  whereStr = "where wh_year    =  ? "
           + "  and card_item  =  ? "
           + "  and place      =  ? "
           + "order by lot_no ";

  setString(1, hWareWhYear);
  setString(2, hWareCardItem);
  setString(3, hWarePlace);
  int tmpInt  = selectTable();

  if(notFound.equals("Y")) {
     tmpInt = crdWarehouseInsert();
     return (1);
    }
if(debug==1)
   showLogMessage("I", "", " select ware_hou=" +hWhtrCardItem +","+hWhtrPlace+","+tmpInt);

  for(int i = 0; i < tmpInt ; i++) {
      hWareLotNo    = getValue("lot_no",i);
      hWareOutQty01 = getValueInt("out_qty01",i);
      hWareOutQty02 = getValueInt("out_qty02",i);
      hWareOutQty03 = getValueInt("out_qty03",i);
      hWareOutQty04 = getValueInt("out_qty04",i);
      hWareOutQty05 = getValueInt("out_qty05",i);
      hWareOutQty06 = getValueInt("out_qty06",i);
      hWareOutQty07 = getValueInt("out_qty07",i);
      hWareOutQty08 = getValueInt("out_qty08",i);
      hWareOutQty09 = getValueInt("out_qty09",i);
      hWareOutQty10 = getValueInt("out_qty10",i);
      hWareOutQty11 = getValueInt("out_qty11",i);
      hWareOutQty12 = getValueInt("out_qty12",i);
      hWareItemAmt  = getValueDouble("item_amt",i);
      hPreTotalBal  = getValueDouble("h_pre_total_bal",i);
      hWareRowid     = getValue("rowid",i);
if(debug == 1) showLogMessage("I", "", "    whtr lot=" + hWareLotNo + "," +i+","+ hPreTotalBal + " remain=" + remainCardQty);
      tmpQty = hPreTotalBal;  /* 該批號剩餘庫存 */
      if(i+1 == tmpInt) /* 最後一筆批號全加 */
        {
         tmpQty = remainCardQty;
        }
      else
        {
         if(hPreTotalBal < 1)      continue;    // 出庫
         if(remainCardQty < tmpQty) {
             tmpQty = remainCardQty;
             remainCardQty = 0;
         } else
             remainCardQty = remainCardQty - tmpQty;
        }

        /*-- add out_qty --*/
        switch (hWareMonth) {
        case 1:
            hWareOutQty01 += tmpQty;
            break;
        case 2:
            hWareOutQty02 += tmpQty;
            break;
        case 3:
            hWareOutQty03 += tmpQty;
            break;
        case 4:
            hWareOutQty04 += tmpQty;
            break;
        case 5:
            hWareOutQty05 += tmpQty;
            break;
        case 6:
            hWareOutQty06 += tmpQty;
            break;
        case 7:
            hWareOutQty07 += tmpQty;
            break;
        case 8:
            hWareOutQty08 += tmpQty;
            break;
        case 9:
            hWareOutQty09 += tmpQty;
            break;
        case 10:
            hWareOutQty10 += tmpQty;
            break;
        case 11:
            hWareOutQty11 += tmpQty;
            break;
        case 12:
            hWareOutQty12 += tmpQty;
            break;
        default:
            return (1);
        }

        /*-- Update CRD_WAREHOUSE --*/
        daoTable   = "crd_warehouse";
        updateSQL  = "out_qty01 = ?, ";
        updateSQL += "out_qty02 = ?, ";
        updateSQL += "out_qty03 = ?, ";
        updateSQL += "out_qty04 = ?, ";
        updateSQL += "out_qty05 = ?, ";
        updateSQL += "out_qty06 = ?, ";
        updateSQL += "out_qty07 = ?, ";
        updateSQL += "out_qty08 = ?, ";
        updateSQL += "out_qty09 = ?, ";
        updateSQL += "out_qty10 = ?, ";
        updateSQL += "out_qty11 = ?, ";
        updateSQL += "out_qty12 = ?, ";
        updateSQL += "mod_user  = ?, ";
        updateSQL += "mod_time  = sysdate, ";
        updateSQL += "mod_pgm   = ?, ";
        updateSQL += "mod_seqno = 0 ";
        whereStr   = "where rowid   = ? ";
        setInt(1, hWareOutQty01);
        setInt(2, hWareOutQty02);
        setInt(3, hWareOutQty03);
        setInt(4, hWareOutQty04);
        setInt(5, hWareOutQty05);
        setInt(6, hWareOutQty06);
        setInt(7, hWareOutQty07);
        setInt(8, hWareOutQty08);
        setInt(9, hWareOutQty09);
        setInt(10, hWareOutQty10);
        setInt(11, hWareOutQty11);
        setInt(12, hWareOutQty12);
        setString(13, hWareModUser);
        setString(14, hWareModPgm);
        setRowId( 15, hWareRowid);
        if (debugMode) {
            setString(15, "2009");
            setString(16, "2000古銅錢幣卡");
            setString(17, "1");
        }
        updateTable();
        if (notFound.equals("Y")) {
            showLogMessage("I", "", " not find crd_warehouse !!!" );
            return (1);
        }

      insertCrdWhtxDtl(hWareLotNo , tmpQty, hWareItemAmt);

      if(remainCardQty == 0) break;

    }

  return (0);
}
/****************************************************************/
public void insertCrdWhtxDtl(String iLotNo,double tmpQty,double iItemAmt) throws Exception
{
if(debug==1)
   showLogMessage("I", "", " insert dtl=" +tmpQty +","+hEhtrEarehouseNo+","+hPreTotalBal);

    seqNo++;
    setValue("warehouse_no"    , hEhtrEarehouseNo);
    setValueInt("seq_no"       , seqNo);
    setValue("warehouse_date"  , sysDate);
    setValue("card_item"       , hWhtrCardItem);
    setValue("lot_no"          , iLotNo );
//  setValue("card_type"       , h_key_cardtype);
//  setValue("group_code"      , h_key_groupcode);
//  setValue("unit_ocde"       , h_key_unitcode);
    setValue("tns_type"        , "2");
    setValue("place"           , hWhtrPlace);
    setValueDouble("item_amt"  , iItemAmt);
    setValueDouble("prev_total", hPreTotalBal);
    setValueDouble("use_total" , tmpQty);

    daoTable = "crd_whtx_dtl ";

    insertTable();
    // TODO Auto-generated method stub

}
/************************************************************************/
/* Insert 卡片庫存檔 */
/***********************************************************************/
int crdWarehouseInsert() throws Exception 
{
        daoTable = "crd_warehouse";
        setValue("wh_year"  , hWareWhYear);
        setValue("card_item", hWareCardItem);
        setValue("place"    , hWarePlace);
        setValue("lot_no"   , sysDate+"01");
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_warehouse duplicate!", "", comcr.hCallBatchSeqno);
        }
        return (0);
}
/*************************************************************************/
void crdWarehouseInit() 
{
        hWareWhYear   = hWareYear;
        hWareCardItem = hWhtrCardItem;
        hWarePlace     = hWhtrPlace;
        hWareOutQty01 = 0;
        hWareOutQty02 = 0;
        hWareOutQty03 = 0;
        hWareOutQty04 = 0;
        hWareOutQty05 = 0;
        hWareOutQty06 = 0;
        hWareOutQty07 = 0;
        hWareOutQty08 = 0;
        hWareOutQty09 = 0;
        hWareOutQty10 = 0;
        hWareOutQty11 = 0;
        hWareOutQty12 = 0;
        hWareModUser  = "SYSTEM";
        hWareModPgm   = javaProgram;
        hAllTotalBal  = 0;
        hWareLotNo    = "";
        hWareItemAmt  = 0;
        tmpQty          = 0;
}
/***********************************************************************/
public static void main(String[] args) throws Exception 
{
        CrdM005 proc = new CrdM005();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
}
/***********************************************************************/
}
