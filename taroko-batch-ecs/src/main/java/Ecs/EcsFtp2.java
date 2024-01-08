/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/06/01  V1.00.01  Ryan                                                  *
* 112/07/11  V1.00.02  Ryan                增加rm的動作(刪除檔案的動作)                                          *
******************************************************************************/
package Ecs;

import com.*;
import java.nio.file.Paths;
import java.text.Normalizer;

@SuppressWarnings("unchecked")
public class EcsFtp2 extends AccessDAO {
	private String progname = "ECS FTP 搬檔處理共用程式 112/07/11  V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommFTP commFTP = null;
    CommRoutine comr = null;

    String fileDate = "";


    // ************************************************************************
    public static void main(String[] args) throws Exception {
        EcsFtp2 proc = new EcsFtp2();
        int retCode = proc.mainProcess(args);
        System.exit(retCode);
    }

    // ************************************************************************
    public int mainProcess(String[] args) {
        String moveType = "";
        String filePath1 = "";
        String filePath2 = "";

        try {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            
            if(args.length < 1 && !"cp".equalsIgnoreCase(moveType)
            		&&!"mv".equalsIgnoreCase(moveType)&&!"rm".equalsIgnoreCase(moveType)) {
            	showInputHint();
                return (1);
            }
            
            moveType = args[0];
            if("rm".equalsIgnoreCase(moveType) && args.length < 2) {
            	showInputHint();
                return (1);
            }

            if (!"rm".equalsIgnoreCase(moveType) && args.length != 3 ) {
                showInputHint();
                return (1);
            }
            
            if(args.length >= 2)
            	filePath1 = args[1]; 
            if(args.length == 3)
            	filePath2 = args[2]; 
            
//            if (!connectDataBase())
//                return (1);
            
            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            

            showLogMessage("I", "", String.format("輸入參數1 = [%s]",moveType));
            showLogMessage("I", "", String.format("輸入參數2 = [%s]",filePath1));
            showLogMessage("I", "", String.format("輸入參數3 = [%s]",filePath2));
 
            if("rm".equalsIgnoreCase(moveType)) {
            	for(int i = 1; i<args.length ;i++) {
            		deleteFolder(moveType,args[i]);
            	}
            	 return (0);
            }
            
            
            filePath1 = Normalizer.normalize(filePath1, java.text.Normalizer.Form.NFKD);
            filePath2 = Normalizer.normalize(filePath2, java.text.Normalizer.Form.NFKD);

            String homePath = Paths.get(comc.getECSHOME()).toString();

            if(filePath1.indexOf("media")>0) {
            	if(filePath1.indexOf(homePath)<0)
            		filePath1 =  homePath + filePath1;
            }
            if(filePath2.indexOf("media")>0) {
            	if(filePath2.indexOf(comc.getECSHOME())<0)
            		filePath2 =  homePath + filePath2;
            }
            
            moveFolder(moveType,filePath1,filePath2);
            return (0);
        }

        catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        } finally {
			finalProcess();
		}

    } // End of mainProcess


    // ************************************************************************

    public void moveFolder(String moveType,String fs , String ft) throws Exception {
    
            if ("cp".equalsIgnoreCase(moveType)) {
            	String cmdStr = String.format("cp -p %s %s ", fs, ft);

                showLogMessage("I", "", "備份遠端檔案: cp 檔案指令=" + cmdStr);

                if (comc.fileCopy(fs, ft) == false) {
                	showLogMessage("I", "", "ERROR : 檔案[" + fs + "]copy失敗!");
                    return;
                }
                showLogMessage("I", "", "備份遠端檔案[" + ft + "]完成.....");
			}
            
            if ("mv".equalsIgnoreCase(moveType)) {
            	String cmdStr = String.format("mv -i -f %s %s", fs, ft);

                showLogMessage("I", "", "搬移遠端檔案: mv 檔案指令=" + cmdStr);

                if (comc.fileMove(fs, ft) == false) {
                	showLogMessage("I", "", "ERROR : 檔案[" + fs + "]搬移失敗!");
                    return;
                }
                showLogMessage("I", "", "搬移遠端檔案[" + ft + "]完成.....");
			}
            
        }

    public void deleteFolder(String moveType,String fs) throws Exception {
    	fs = Normalizer.normalize(fs, java.text.Normalizer.Form.NFKD);

        String homePath = Paths.get(comc.getECSHOME()).toString();

        if(fs.indexOf("media")>0) {
        	if(fs.indexOf(homePath)<0)
        		fs =  homePath + fs;
        }
    	
        if ("rm".equalsIgnoreCase(moveType)) {
        	String cmdStr = String.format("rm -f %s ", fs);

            showLogMessage("I", "", "刪除遠端檔案: rm 檔案指令=" + cmdStr);
            
            if (comc.fileDelete(fs) == false) {
             	showLogMessage("I", "", "ERROR : 檔案[" + fs + "]刪除失敗!");
                return;
            }
            showLogMessage("I", "", "刪除遠端檔案[" + fs + "]完成.....");
		}
    }

	// ************************************************************************
	
	private void showInputHint() {
		showLogMessage("I", "", "請輸入參數，均為必填:");
		showLogMessage("I", "", "PARM 1 :   必填，mv為搬檔，cp為複製，rm為刪除");
		showLogMessage("I", "", "PARM 2 :   必填，來源路徑/檔名");
		showLogMessage("I", "", "PARM 3 :   必填，目的路徑/檔名，若為rm時，此欄位可以空白");
	}

} // End of class FetchSample

