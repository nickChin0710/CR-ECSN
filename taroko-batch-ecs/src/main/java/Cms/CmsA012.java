package Cms;
/** 每日送全鋒文字檔產生處理程式
 * 2019-1015   JH    testing
 19-0222:    JH    bin:cms_roadmaster
 *  19-0110:    JH    FTP ok
 * 2018-0702	JH		test OK
 * 2020-0211	Pino    customized for TCB
 * 2020-0213	Pino    CheckRentCarNo
  *  109/12/04  V1.00.01    shiyuqi       updated for project coding standard   *
*  109/12/30  V1.00.03  yanghan       修改了部分无意义的變量名稱          * 
 * */

import com.CommCrd;
import com.CommFTP;
import com.CommFunction;
import com.BaseBatch;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;

public class CmsA012 extends BaseBatch {
private String progname = "每日送全鋒文字檔產生處理程式109/12/30  V1.00.03";
CommFunction comm = new CommFunction();
CommCrd comc = new CommCrd();
//CommRoutine comr = null;
//=============================================
hdata.CmsRoadmaster hRoad=new hdata.CmsRoadmaster();
hdata.CmsRoaddetail hRode=new hdata.CmsRoaddetail();
hdata.EcsRefIpAddr hEria=new hdata.EcsRefIpAddr();
Rds.RdsB020 rdsB020 = new Rds.RdsB020();
BufferedWriter file1 = null;
BufferedWriter file2 = null;

//-----------------------------
private long hCsumCsumCaradd = 0;
private long hCsumCsumCarstop = 0;
private long hCsumCsumPvadd = 0;
private long hCsumCsumPvstop = 0;
private long hCsumCarNofree = 0;
//private String h_card_id = "";
private String hCardIdPSeqno = "";
private String hIdnoChiName = "";
private String hIdnoIdNo = "";
private String hSysDate;
private String hFourMonths;
private String hSysYm;
private String isSendDate;
// ------------------------------------------------------------------------------
private String isFileName = "";
private int iiFileNumC = 0;
private String isSendfile = "";
private String isSendfileB = "";
private int iiFileNum = 0;
private int iiFileNumB = 0;
private int iiPvcard = 0;
private String isZipfile = "", isFtpfile ="";
private String isZipfileB = "", isFtpfileB ="";
private String isZipA = "";
private String isZipB = "";
private int file1cnt = 0;
private int file2cnt = 0;

//==============================================
private int tiMastCnt =-1;
private int tiCarStop =-1;
private int tiMastPv =-1;

int commit =1;

//=*****************************************************************************
public static void main(String[] args) {
	CmsA012 proc = new CmsA012();
	
//	proc.debug = true;
	//proc.ddd_sql(true);
	
	proc.mainProcess(args);
	proc.systemExit(0);
}

//==============================================================================
@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

	int liArg =args.length;
	if (liArg >1) {
		printf("Usage : CmsA012 [callbatch_seqno]");
		errExit(1);
	}

	dbConnect();

	if (liArg >0) {
		callBatchSeqno(args[liArg -1]);
	}
	callBatch(0, 0, 0);

	hSysDate =sysDate;
	hSysYm =commString.left(sysDate,6);
	hFourMonths =commString.left(commDate.dateAdd(hBusiDate, 0, -4, 0), 6);
	isSendDate =sysDate;

	isFileName = "roadcar";
	checkOpen();
	selectCmsRoaddetail();
	printf("->檔案TMSROAD8產生筆數:[%s]", file1cnt);
	printf("->檔案006NOREG產生筆數:[%s]", file2cnt);
    file1.close();
    file2.close();

	proStopQty();
	updateCmsCarsum();

//	printf("->檔案產生筆數:[%s]", totalCnt);

	ftpScript();
//
//	h_oold_ref_ip = h_eria.ref_ip;
//	h_oold_ref_name = h_eria.ref_name;
//	h_oold_user_id = h_eria.user_id;
//	h_oold_user_passwd = h_eria.user_hidewd;
//	h_oold_trans_type = h_eria.trans_type;
//	h_oold_remote_dir = h_eria.remote_dir;
//	h_oold_local_dir = h_eria.local_dir;
//	h_oold_port_no = h_eria.port_no;
//
	
	/*暫時不傳ftp出去
	ftp_Proc(is_ftpfile);
	ftp_Proc(is_ftpfileB);
	*/
	
//	dir_read();

	sqlCommit(commit);
	endProgram();
}

//=============================================================================
void checkOpen() throws Exception {
	isSendfile = comc.getECSHOME() + "/media/cms/TMSROAD8.txt";
	isSendfileB = comc.getECSHOME() + "/media/cms/006NOREG.txt";
	isZipfile = comc.getECSHOME() + "/media/cms/TMSROAD8_" + isSendDate + ".zip";
	isZipfileB = comc.getECSHOME() + "/media/cms/006NOREG_" + isSendDate + ".zip";
	isFtpfile ="TMSROAD8_"+ isSendDate +".zip";
	isFtpfileB ="006NOREG_"+ isSendDate +".zip";
	isZipA = "TMSROAD8_" + isSendDate + ".zip";
	isZipB = "006NOREG_" + isSendDate + ".zip";
    
    isSendfile = Normalizer.normalize(isSendfile, java.text.Normalizer.Form.NFKD);

    file1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(isSendfile), "MS950"));
	
    isSendfileB = Normalizer.normalize(isSendfileB, java.text.Normalizer.Form.NFKD);

    file2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(isSendfileB), "MS950"));

//	ii_file_num = this.openOutputText(is_sendfile);
//	printf("-->open file-Name="+is_sendfile);
//	if (ii_file_num < 0) {
//		errmsg("沒有權限讀寫資料 失敗  %s", is_sendfile);
//		//h_call_error_desc = "在程式執行目錄下沒有權限讀寫資料";
//		err_exit(1);
//	}
//
//	ii_file_numB = this.openOutputText(is_sendfileB);
//	printf("-->open file-Name="+is_sendfileB);
//	if (ii_file_numB < 0) {
//		this.closeOutputText(ii_file_numB);
//		errmsg("沒有權限讀寫資料 失敗  %s", is_sendfileB);
////		h_call_error_desc = "在程式執行目錄下沒有權限讀寫資料";
//		this.err_exit(1);
//	}
}

//==============================================================================
void selectCmsRoaddetail() throws Exception {
	String lsNewCar = "";
	String stringTmp="";
	String lsCard9 = "";
	String hStatus = "";
	
	hCsumCsumPvadd = 0;
	hCsumCsumCaradd = 0;
	hCsumCarNofree = 0;

	/*-get acct_type-*/
	sqlCmd = " select "
		+ " a.card_no , "
		+ " a.rd_type , "
		+ " a.rd_carno , "
		+ " a.rd_carmanname , "
		+ " a.rd_moddate , "
		+ " a.mod_pgm , "
		+ " a.rd_status , "
		+ " a.id_p_seqno , "
		+ " a.rds_pcard , "
		+ " b.chi_name , "
		+ " b.id_no "
		+ " from cms_roaddetail A "
		+ " join crd_idno B on a.id_p_seqno = b.id_p_seqno "
		+ " where 1=1 "
		+ " and a.rds_pcard in ('A','I','P','L')"
//k		+ " and a.rd_moddate = ? "
		;
//k	    setString(1, hSysDate);
	this.openCursor();

	while (fetchTable()) {
		hRode.cardNo = colSs("card_no");
		hRode.rdType = colSs("rd_type");
		hRode.rdCarno = colSs("rd_carno");
		hRode.rdCarmanname = colSs("rd_carmanname");
		hRode.rdModdate = colSs("rd_moddate");
		hRode.modPgm = colSs("mod_pgm");
		hRode.rdStatus = colSs("rd_status");
		hRode.idPSeqno = colSs("id_p_seqno");
		hRode.rdsPcard = colSs("rds_pcard");
		hIdnoChiName = colSs("chi_name");
		hIdnoIdNo = colSs("id_no");
		
		/*-initial Text-field-*/
		stringTmp = "";
		lsNewCar = "";
		lsCard9 = "";
		hStatus = "";

		lsCard9 = commString.right(hRode.cardNo,9); //卡號後9碼-
		lsNewCar = hRode.rdCarno;
		
		//-異動程式(mod_pgm)=cmsb012，是接收全鋒檔案
		if (eq(hRode.modPgm,"CmsB012")) {
			continue;
		}
		//-rm_status=4 未啟用
		if (eq(hRode.modPgm,"CrdD011") && eq(hRode.rdStatus,"4")) {
			continue;
		}
		//-check PV-card[自動登錄]--
		iiPvcard = 0;
		if (eq(hRode.rdType, "F")) {
			if (eq(hRode.rdsPcard, "A"))
				iiPvcard = 1;
		}
		//-自動登錄車號空白---
		if (iiPvcard == 1 && empty(hRode.rdCarno))
			lsNewCar = "AAA-AAAA";
		/*-人工登錄車號空白-*/
		if (iiPvcard == 0 && empty(hRode.rdCarno))
			continue;
		//-人工登錄車號與自費重複- 
		if (iiPvcard == 0 && eq(hRode.rdType, "F")) {
			int llCnt = 0;
			if (tiMastCnt <= 0) {
				sqlCmd = " select "
					+ " count(*) as mast_cnt "
					+ " from cms_roadmaster "
					+ " where rm_carno = ? "
					+ " and rm_type = 'E' "
					+ " and rm_status <> '0' ";
				tiMastCnt =ppStmtCrt("ti_mast_cnt","");
			}
			sqlSelect(tiMastCnt, new Object[] {
				hRode.rdCarno
			});

			if (sqlNrow < 0) {
				sqlerr("read cms_roadmaster error, [%s]", hRode.rdCarno);
				errExit(1);
			}
			llCnt = colInt("mast_cnt");
			if (llCnt > 0)
				continue;
		}
		
		totalCnt++;
		if (totalCnt % 3000 == 0)
			printf(" 讀取筆數 =[%s]", totalCnt);
		if(rdsB020.checkRentCarNo(hRode.rdCarno)==0) {
			hRode.rdsPcard ="L";
		}
		if(eq(hRode.rdStatus,"1")) {
			stringTmp= "2"+"N"+hRode.rdsPcard+ hIdnoIdNo.substring(0, 3)
			+"***"+ hIdnoIdNo.substring(6, 10)+lsCard9
			+fixLeft(hRode.rdCarno,8)+ hSysDate +fixLeft(hIdnoChiName,12);
			
			file1.write(stringTmp + "\n");
			file1cnt++;
		}
		if(eq(hRode.rdStatus,"0")) {
			stringTmp= "2"+"D"+hRode.rdsPcard+ hIdnoIdNo.substring(0, 3)
			+"***"+ hIdnoIdNo.substring(6, 10)+lsCard9
			+fixLeft(hRode.rdCarno,8)+ hSysDate +fixLeft(hIdnoChiName,12);
			
			file2.write(stringTmp + "\n");
			file2cnt++;
		}
		if(eq(hRode.rdStatus,"2")) {
			stringTmp= "2"+"R"+hRode.rdsPcard+ hIdnoIdNo.substring(0, 3)
			+"***"+ hIdnoIdNo.substring(6, 10)+lsCard9
			+fixLeft(hRode.rdCarno,8)+ hSysDate +fixLeft(hIdnoChiName,12);
			
			file2.write(stringTmp + "\n");
			file2cnt++;
		}
		if(eq(hRode.rdStatus,"3")) {
			stringTmp= "2"+"R"+hRode.rdsPcard+ hIdnoIdNo.substring(0, 3)
			+"***"+ hIdnoIdNo.substring(6, 10)+lsCard9
			+fixLeft(hRode.rdCarno,8)+ hSysDate +fixLeft(hIdnoChiName,12);
			
			file2.write(stringTmp + "\n");
			file2cnt++;
		}

		//-統計car-card- 
		if (iiPvcard == 1)
			hCsumCsumPvadd++;
		else {
			if (eq(hRode.rdType, "E"))
				hCsumCarNofree++;
			else
				hCsumCsumCaradd++;
		}
	} //=while
	
	this.closeCursor();
}

//==============================================================================
void proStopQty() throws Exception {
	hCsumCsumCarstop = 0;
	hCsumCsumPvstop = 0;

	if (tiCarStop <=0) {
		sqlCmd = " select "
			+ " count(*) as aa_car_stop "
			+ " from cms_roadmaster A join crd_card B on A.card_no =B.card_no "
			+ " where A.rm_status = '0' "
			+ " and A.rm_reason = '2' "
			+ " and B.card_type not in "
			+ " (select card_type from ptr_card_type where rds_pcard ='A')";
		tiCarStop =ppStmtCrt("ti_car_stop","");
	}
	sqlSelect(tiCarStop);

	if (sqlNrow < 0) {
		sqlerr("cms_roadmaster.count error, [car]");
		errExit(1);
	}
	hCsumCsumCarstop = colInt("aa_car_stop");

	if (tiMastPv <=0) {
		sqlCmd = " select count(*) as aa_pv_stop "
			+ " from cms_roadmaster A join crd_card B on A.card_no =B.card_no "
			+ " where A.rm_status = '0' "
			+ " and A.rm_reason = '2' "
			+ " and B.card_type in "
			+ " (select card_type from ptr_card_type where rds_pcard = 'A')";
		tiMastPv =ppStmtCrt("ti_mast_pv","");
	}
	sqlSelect(tiMastPv);
	if (sqlNrow < 0) {
		sqlerr("cms_roadmaster.count error, [PV-card]");
		this.errExit(1);
	}

	hCsumCsumPvstop = colInt("aa_pv_stop");

}

//==============================================================================
void updateCmsCarsum() throws Exception {
	int llCnt = 0;

	sqlCmd = " select count(*) as aa_cnt "
		+ " from cms_carsum "
		+ " where send_date = ? ";
	sqlSelect("",new Object[] {
			isSendDate
	});
	if (sqlNrow >0)
		llCnt = colInt("aa_cnt");
	
	if (llCnt == 0) {
		sqlCmd = " insert into cms_carsum ("
			+ " send_date , "
			+ " car_add , "
			+ " car_stop , "
			+ " pvcard_add , "
			+ " pvcard_stop , "
			+ " car_nofree , "
			+ " mod_user, mod_time, mod_pgm, mod_seqno "
			+ " ) values ( "
			+ " ? , " //1
			+ " ? , " 
			+ " ? , "
			+ " ? , "
			+ " ? , " //5
			+ " ? , " //6
			+ commSqlStr.modxxxInsert(hModUser, hModPgm)
			+ " )";
		
		ppp(1, isSendDate);
		ppp(hCsumCsumCaradd);
		ppp(hCsumCsumCarstop);
		ppp(hCsumCsumPvadd);
		ppp(hCsumCsumPvstop);
		ppp(hCsumCarNofree);
		
		sqlExec("");
	}
	else {
		sqlCmd = " update cms_carsum set "
			+ " car_add = ? , "
			+ " car_stop = ? , "
			+ " pvcard_add = ? , "
			+ " pvcard_stop = ? , "
			+ " car_nofree = ? , "
			+commSqlStr.setModXxx(hModUser,hModPgm)
			+ " where send_date = ?  ";

		ppp(1, hCsumCsumCaradd);
		ppp(hCsumCsumCarstop);
		ppp(hCsumCsumPvadd);
		ppp(hCsumCsumPvstop);
		ppp(hCsumCarNofree);
		ppp(isSendDate);
		
		sqlExec("");
	}

	if (sqlNrow <= 0) {
		sqlerr("insert/update CMS_CARSUM error: date=[%s]", isSendDate);
		this.errExit(1);
	}
}

//=============================================================================
void ftpScript() throws Exception {
	int liRC = 0;
	String lsPsswd = "", stringTmp="";
	//-ZIP-
	sqlCmd = " select "
		+ " wf_value2 as db_paswd"
		+ " from ptr_sys_parm "
		+ " where wf_parm = 'SYSPARM' "
		+ " and wf_key = 'ROADCAR_ZIP_PWD' "
		+commSqlStr.rownum(1)
		;
	sqlSelect();
	if (sqlNrow <= 0) {
		sqlerr("select ptr_sys_parm error [ROADCAR_ZIP_PWD]");
		this.errExit(1);
	}
	
	lsPsswd = colSs("db_paswd");
   //is_zipfile = Normalizer.normalize(is_zipfile, java.text.Normalizer.Form.NFKD);
   int liRc = comm.zipFile(isSendfile, isZipfile, lsPsswd);
   if(liRc != 0) {
      errmsg("無法壓縮檔案[%s]", isSendfile);
      errExit(1);
   }
   //--
   //is_zipfileB = Normalizer.normalize(is_zipfileB, java.text.Normalizer.Form.NFKD);
   liRc = comm.zipFile(isSendfileB, isZipfileB, lsPsswd);
   if(liRc != 0) {
      errmsg("無法壓縮檔案[%s]", isSendfile);
      errExit(1);
   }
}

//==============================================================================
void ftpProc(String aFile) throws Exception {
   // FTP

   CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());

   commFTP.hEflgTransSeqno = ecsModSeq(10);  //comr.getSeqno("ECS_MODSEQ"); /*串聯 log 檔所使用 鍵值(必要)*/
   commFTP.hEflgSystemId   = javaProgram; /* 區分不同類的 FTP 檔案-大類     (必要) */
   commFTP.hEflgGroupId    = "";          /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "";          /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEriaLocalDir   = "";  //get_ecsHome()+"/media/cms/";
   commFTP.hEflgModPgm     = hModPgm;
   String hEflgRefIpCode  = "ROAD_MEGAFTPSERVER";
   hEria.refIpCode ="ROAD_MEGAFTPSERVER";

   System.setProperty("user.dir", commFTP.hEriaLocalDir);

   //-a_file:檔名不含PATH-
   String procCode = String.format("put %s", aFile);
   printf("-->%s %s 開始上傳....",procCode,hEria.refIpCode);

   int liRc = commFTP.ftplogName(hEria.refIpCode, procCode);
  if (liRc != 0) {
     errmsg("FTP 1=[%s]傳檔錯誤 err_code[%d]", isZipfile,liRc);
     errExit();
  }
  // ==================================================
}
//void ftp_out() throws Exception {
//	int li_RC = 0, rtn = 0, li_cmdRC = 0;
//	String ss = "", ls_filename = "", temstr = "", tmpstr = "";
//
//	ss = get_ecsHome() + "/media/cms/" + is_file_name + "h";
//	ii_file_numC = this.openOutputText(ss, "MS950");
//
//	if (ii_file_numC < 0) {
//		printf("在程式執行目錄下沒有權限讀寫資料[%s]", ss);
//		this.closeOutputText(ii_file_numC);
//		err_exit(1);
//	}
//
//	li_RC = select_ecs_ref_ip_addr("ROAD_FTPCOMMON");
//	if (li_RC != 0)
//		return;
//
//	ls_filename = is_file_name + ".zip";
//	temstr = "open " + is_file_name;
//
//	this.writeTextFile(ii_file_numC, temstr);
//	temstr = "user " + h_eria.user_id + " " + h_eria.user_hidewd;
//	this.writeTextFile(ii_file_numC, temstr);
//	if (eq(h_eria.trans_type, "0"))
//		temstr = "ASCII";
//	else
//		temstr = "BINARY";
//	this.writeTextFile(ii_file_numC, temstr);
//	temstr = "cd " + h_eria.remote_dir;
//	this.writeTextFile(ii_file_numC, temstr);
//	temstr = "put " + ls_filename;
//	this.writeTextFile(ii_file_numC, temstr);
//	temstr = "bye";
//	this.writeTextFile(ii_file_numC, temstr);
//	this.closeOutputText(ii_file_numC);
//	tmpstr = "put " + is_file_name + ".sh";
//	// rtn = system_ftp(h_oold_ref_ip.arr , h_oold_user_id.arr,
//	// h_oold_user_passwd.arr, h_oold_trans_type.arr,
//	// h_oold_remote_dir.arr , h_oold_local_dir.arr,
//	// tmpstr);
//	if (rtn != 0)
//		printf("FTP [%s]傳檔錯誤 err_code[%s]", tmpstr, rtn);
//	/*-<<-*/
//
//}

//int select_ecs_ref_ip_addr(String a_ref_ip) throws Exception {
//	int li_RC = 0;
//
//	/*-FTP ref_ID-*/
//
//	// li_RC=comm_oracle_ip(ss,ss1);
//
//	if (li_RC == 1) {
//		printf("Listener Online 未建 error");
//		this.err_exit(0);
//	}
//	else if (li_RC == 2) {
//		printf("Listener 所對應主機名稱未定義 error");
//		this.err_exit(0);
//	}
//
//	sqlCmd = " select *"
//		+ " from ecs_ref_ip_addr "
//		+ " where 1=1 "
//		+ " and ref_ip_code = ? ";
//	ppp(1,a_ref_ip);
//	sqlSelect();
//
//	if (sql_nrow < 0) {
//		sqlerr("select ecs_ref_ip_addr error[%s]", a_ref_ip);
//		this.err_exit(1);
//	}
//	if (sql_nrow == 0)
//		return 1;
//
//	h_eria.ref_ip = col_ss("ref_ip");
//	h_eria.ref_name = col_ss("ref_name");
//	h_eria.user_id = col_ss("user_id");
//	h_eria.user_hidewd = col_ss("user_hidewd");
//	h_eria.trans_type = col_ss("trans_type");
//	h_eria.remote_dir = col_ss("remote_dir");
//	h_eria.local_dir = col_ss("local_dir");
//	h_eria.port_no = col_ss("port_no");
//
//	return 0;
//}

// ------------------------------------------------------------------------------
void dirRead() {
	String filePath = "";
	filePath = this.getEcsHome() + "/media/cms/roadcarbk";
	// fd = open(file_path,O_RDONLY);
	// fchdir(fd);
	// close(fd);
	//
	// getcwd(file_path,sizeof(file_path));
	//
	// dir = opendir(file_path);
	// while ((ptr = readdir(dir)) != NULL)
	// {
	// if(strncmp(ptr->d_name,"roadcar",7) != 0 ) continue;
	//
	// printf("Now processing file [%s] ...",ptr->d_name);
	//
	// str2var(h_remove_file_name , ptr->d_name);
	// printf("四個月前月份[%s] ",h_four_months.arr);
	// printf("四個月前月份[%s] ",h_remove_file_name.arr+7,6);
	// if(strncmp(h_four_months.arr,h_remove_file_name.arr+7,6) > 0 )
	// {
	// sprintf(tmpstr,"rm %s/media/cms/roadcarbk/%s",
	// GetECSHOME(),ptr->d_name);
	// if (system_cmd(tmpstr,&int_cmd)!=0)
	// {
	// fprintf(stderr,"無法搬移檔案[%s]",tmpstr);
	// exit(1);
	// }
	// }
	// }
	// closedir(dir);
}
String fixLeft(String str, int len) throws UnsupportedEncodingException {
    String spc = "";
    for (int i = 0; i < 100; i++)
        spc += " ";
    if (str == null)
        str = "";
    str = str + spc;
    byte[] bytes = str.getBytes("MS950");
    byte[] vResult = new byte[len];
    System.arraycopy(bytes, 0, vResult, 0, len);
    return new String(vResult, "MS950");
}

}
