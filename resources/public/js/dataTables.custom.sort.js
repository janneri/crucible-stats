jQuery.extend( jQuery.fn.dataTableExt.oSort, {
    "date-fi-pre": function ( a ) {
        if ($.trim(a) != '') {
        	// 11.03.2010 04:00
            var splitted = $.trim(a).split(' ');
            var dateParts = splitted[0].split('.'); // DD.MM.YYYY
            var timeParts = splitted[1].split(':'); // hh:mm
            
            var x = new Date(dateParts[2], dateParts[1]-1, dateParts[0],
            		         timeParts[0], timeParts[1], 0);
        } else {
            var x = new Date(0,0,0,0,0,0);
        }
        return x;
    },
  
    "date-fi-asc": function ( a, b ) {
    	return ((a < b) ? -1 : ((a > b) ? 1 : 0));
    },
  
    "date-fi-desc": function ( a, b ) {
    	return ((a < b) ? 1 : ((a > b) ? -1 : 0));
    }
} );