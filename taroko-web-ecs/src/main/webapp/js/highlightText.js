 var inobj = document.getElementsByTagName('input'); 
 for ( var j=0;	j	<	inobj.length;	j++	)
		 {
			 var inType	=	inobj[j].getAttribute("type");
			 if	(	inType !=	"text" )
			 		{	continue;	}
       inobj[j].onfocus = function() { 
       this.select(); 
       /*  if ( this.value == this.getAttribute('value') ) {  this.value=''; }  */
       }
    }