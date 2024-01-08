
 top.refreshButton2('2');
 top.pageRows="999";

 function validateInput()
  {
    if ( !top.checkFormat() )
       { return false; }

   return true;
 }

 function wf_detl_2page(parm) {

	 	if (parm==="01") {
		top.respHtml  = "rskm1010_01_accttype";
	 }
	 else if (parm==="02") {
		top.respHtml  = "rskm1010_02_groupcode";
	 }
	 else if (parm=="03") {
		top.respHtml  = "rskm1010_03_classcode";
	 }
	 else if (parm=="04") {
		top.respHtml  = "rskm1010_04_pdrate";
	 }
	 else if (parm=="05") {
		top.respHtml  = "rskm1010_05_compbank";
	 }
	 else if (parm=="06") {
		top.respHtml  = "rskm1010_06_adjlmt";
	 }
	 else if (parm=="07") {
		top.respHtml  = "rskm1010_07_risk";
	 }
	 else if (parm=="08") {
		top.respHtml  = "rskm1010_08_block";
	 }
	 else if (parm=="09") {
		top.respHtml  = "rskm1010_09_inblock";
	 }
	 else if (parm=="10") {
		top.respHtml  = "rskm1010_10_exgroup";
	 }

    top.respLevel = 2;
	 document.dataForm.data_k2.value = parm;
    top.submitControl("S2");
    return true;
 }

 function wf_return_data() {
 	 top.upperLevel();
 	 top.submitControl("R");
 }
