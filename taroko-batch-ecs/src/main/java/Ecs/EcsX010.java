/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/09/20  V1.00.01    JeffKung  program initial                           *
 ******************************************************************************/

package Ecs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class EcsX010 extends AccessDAO {

    private String progname = "ECS系統檔案壓縮清理處理程式  112/09/20 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            if(args.length == 0) {
            	showInputHint();
                return (1);
            }
            
            String moveType = args[0];
            if("gzip".equalsIgnoreCase(moveType) && args.length < 2) {
            	showInputHint();
                return (1);
            }
            
            if("housekeeping".equalsIgnoreCase(moveType) && args.length < 2) {
            	showInputHint();
                return (1);
            }

            if ("gunzip".equalsIgnoreCase(moveType) && args.length < 2 ) {
                showInputHint();
                return (1);
            }
            String fileFolder = "";
            int zipDays = -1; //保留天數default值為1天
            int keepDays = -7; //刪檔天數default值為7天
            if(args.length >= 2) {
            	fileFolder = args[1]; 
            }
            if(args.length >= 3) {
            	zipDays = Integer.parseInt(args[2]);
            	if (zipDays > 0) zipDays = zipDays*-1;
            }
            
            if(args.length == 4) {
            	keepDays = Integer.parseInt(args[3]);
            	if (keepDays > 0) keepDays = keepDays*-1;
            }

            showLogMessage("I", "", String.format("輸入參數1 = [%s]",moveType));
            showLogMessage("I", "", String.format("輸入參數2 = [%s]",fileFolder));
            showLogMessage("I", "", String.format("輸入參數3 = [%d]",zipDays));
            showLogMessage("I", "", String.format("輸入參數4 = [%d]",keepDays));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        	LocalDateTime dateTime = LocalDateTime.now();
        	LocalDateTime preZipDateTime = LocalDateTime.now().plusDays(zipDays);
        	LocalDateTime preKeepDateTime = LocalDateTime.now().plusDays(keepDays);
        	String strDateTime = dateTime.format(formatter);
        	String strPreZipDateTime = preZipDateTime.format(formatter);
        	String strPreKeepDateTime = preKeepDateTime.format(formatter);
        	
        	showLogMessage("I","","現在時間為 : [" + strDateTime + "]");
        	showLogMessage("I","","GZIP時間為 : [" + strPreZipDateTime + "]之前的檔案");
        	showLogMessage("I","","刪除檔案時間為 : [" + strPreKeepDateTime + "]之前的檔案");
            
        	File parmFolder = null;
        	
        	if("gzip".equalsIgnoreCase(moveType)) {
        		parmFolder = new File(fileFolder);
        		if (parmFolder.isDirectory()) {
        			gzipListFiles(fileFolder,strPreZipDateTime);
        		} else if (parmFolder.isFile()) {  		
        			if (parmFolder.getName().endsWith(".gz")==false) {
        	    		String inFileName = parmFolder.getAbsolutePath();
        	    		String gzFileName = parmFolder.getAbsolutePath()+".gz";
        	    		showLogMessage("I","","gzip file : [" + gzFileName + "......]"); 
        	    		if (compressGzipFile(inFileName, gzFileName)==0) {
        	    			showLogMessage("I","","delete file : [" + inFileName + "......]");
        	    			parmFolder.delete();
        	    		}
        			}
        		}
        	} else if("gunzip".equalsIgnoreCase(moveType)) {
        		parmFolder = new File(fileFolder);
        		if (parmFolder.isFile()) {  		
        			if (parmFolder.getName().endsWith(".gz")==true) {
        	    		String gzFileName = parmFolder.getAbsolutePath();
        	    		String outFileName = "";
        	    		int endPosition = gzFileName.indexOf(".gz");
        	    		if (endPosition!=-1) {
        	    			outFileName = comc.getSubString(gzFileName,0,endPosition);
        	    		} else {
        	    			;
        	    		}
        	    		showLogMessage("I","","gunzip file : [" + gzFileName + "......]");
        	    		if (decompressGzipFile(gzFileName,outFileName)==0) {
        	    			showLogMessage("I","","gunzip file : [" + outFileName + "]...成功");
        	    		}
        			}
        		} 
        	}else if("houseKeeping".equalsIgnoreCase(moveType)) {
        		parmFolder = new File(fileFolder);
        		if (parmFolder.isDirectory()) {
        			houseKeepingListFiles(fileFolder,strPreZipDateTime,strPreKeepDateTime);
        		} 
        	}
        			
            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

	/***********************************************************************/
	void gzipListFiles(String fileFolder, String strPreZipDateTime) throws Exception {

		File[] files = new File(fileFolder).listFiles();
		Calendar c = Calendar.getInstance();
		String fileDateTime = "";

		for (File file : files) {
			if (file.isFile()) {
				// results.add(file.getName());
				c.setTimeInMillis(file.lastModified());
				fileDateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(c.getTime());
				showLogMessage("I", "", "FileName is : [" + file.getName() + "]," + "上次修改時間為：[" + fileDateTime + "]"
						+ file.getAbsolutePath());
				if (fileDateTime.compareTo(strPreZipDateTime) < 0) {
					if (file.getName().endsWith(".gz") == false) {
						String inFileName = file.getAbsolutePath();
						String gzFileName = file.getAbsolutePath() + ".gz";
						showLogMessage("I", "", "gzip file : [" + gzFileName + "......]");
						if (compressGzipFile(inFileName, gzFileName) == 0) {
							showLogMessage("I", "", "delete file : [" + inFileName + "......]");
							file.delete();
						}
					}
				}
			}
		}
	}
      
    /***********************************************************************/
    void houseKeepingListFiles(String fileFolder,String strPreZipDateTime,String strPreKeepDateTime) throws Exception {
    	
    	File[] files = new File(fileFolder).listFiles();
    	//If this pathname does not denote a directory, then listFiles() returns null. 

    	Calendar c = Calendar.getInstance();
    	String fileDateTime = "";
    	
    	for (File file : files) {
    	    if (file.isFile()) {
    	        //results.add(file.getName());
    	        c.setTimeInMillis(file.lastModified());
    	        fileDateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(c.getTime());
        	    showLogMessage("I","","FileName is : [" + file.getName() + "],"
        	    		+ "上次修改時間為：[" + fileDateTime + "]" + file.getAbsolutePath());
        	    if (fileDateTime.compareTo(strPreKeepDateTime) < 0) {
        	    	showLogMessage("I","","delete file : [" + file.getName() + "......]");
    	    		file.delete();
        	    } else if (fileDateTime.compareTo(strPreZipDateTime) < 0) {
        	    	if (file.getName().endsWith(".gz")==false) {
        	    		String inFileName = file.getAbsolutePath();
        	    		String gzFileName = file.getAbsolutePath()+".gz";
        	    		showLogMessage("I","","gzip file : [" + gzFileName + "......]");
        	    		if (compressGzipFile(inFileName, gzFileName)==0) {
        	    			showLogMessage("I","","delete file : [" + inFileName + "......]");
            	    		file.delete();
        	    		}
        	    	}
        	    }
    	    }
    	}
    }
    
    int compressGzipFile(String file, String gzipFile) {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(gzipFile);
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
            int len;
            while((len=fis.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            //close resources
            gzipOS.close();
            fos.close();
            fis.close();
            return(0);
        } catch (IOException e) {
            e.printStackTrace();
            return(-1);
        }
    }
    
    int decompressGzipFile(String gzipFile, String newFile) {
        try {
            FileInputStream fis = new FileInputStream(gzipFile);
            GZIPInputStream gis = new GZIPInputStream(fis);
            FileOutputStream fos = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int len;
            while((len = gis.read(buffer)) != -1){
                fos.write(buffer, 0, len);
            }
            fos.close();
            gis.close();
            return(0);
        } catch (IOException e) {
            e.printStackTrace();
            return(-1);
        }
        
    }
    
    private void showInputHint() {
		showLogMessage("I", "", "請輸入參數，均為必填:");
		showLogMessage("I", "", "PARM 1 :   必填，gzip為壓縮，gunzip為解壓縮，housekeeping為檔案清除");
		showLogMessage("I", "", "PARM 2 :   必填，來源路徑/檔名");
		showLogMessage("I", "", "PARM 3 :  非必填，gzip保留天數,default為1天");
		showLogMessage("I", "", "PARM 4 :  非必填，刪檔保留天數,default為1天");
	}

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        EcsX010 proc = new EcsX010();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
