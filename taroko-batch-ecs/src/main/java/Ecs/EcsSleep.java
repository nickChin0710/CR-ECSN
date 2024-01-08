/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/04/19  V1.01.01  JeffKung    program initial                           *
 ******************************************************************************/
package Ecs;

import java.util.concurrent.TimeUnit;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class EcsSleep extends AccessDAO {
	
	private String progname = "排程程式使用-Sleep時間控制程式 112/04/19 V1.00.01";
	
	CommCrd comc = new CommCrd();

	public int mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			
			if (args.length > 1 ) {
				comc.errExit("Usage : EcsSleep [Seconds]", "");
			}
			
			int sleepSeconds = 15;
			
			if (args.length == 1) {
				sleepSeconds = comc.str2int(args[0]);
			}
			
			showLogMessage("I", "", String.format("排程中斷時間,SLEEP[%d]秒",sleepSeconds));
			
			TimeUnit.SECONDS.sleep(sleepSeconds);

			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		EcsSleep proc = new EcsSleep();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
