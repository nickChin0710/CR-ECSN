
 function submitControl(parmFunc)
 {
   if ( parmFunc == "D" )
      {
        if ( confirm("確定要刪除 !!") == false ) 
           { return false; }
      }

   actionCode = parmFunc;
   if ( parmFunc != "B" )
      { funCode   =  parmFunc; }

   hideData   = "";
   methodName = "actionFunction"; 
   newWindow  = "Y";
   if ( !validateInput() )
      { return  false; }
   createHideData();
   document.dataForm.HIDE.value  =  hideData;
   document.dataForm.action      = "MainControl";
   document.dataForm.submit();
   return false;
 }

 function refreshButton(parmLevel)
 {
   hideData = document.dataForm.HIDE.value;
   if ( hideData.length >= 43 )
      { splitHideData();  }
   document.dataForm.MESG_DATA.value = respMesg;
   respMesg="";
   return;
 }
