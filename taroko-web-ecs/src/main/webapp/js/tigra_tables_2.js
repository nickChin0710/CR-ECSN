  
function tigra_tables ( 
		str_tableid, // table id (req.)
      num_pre_row, // how many rows pre-data (opt.)
		num_header_offset, // how many rows to skip before applying effects at the begining (opt.)
		num_footer_offset  // how many rows to skip at the bottom of the table (opt.)
	) { 

	// validate required parameters
	if (!str_tableid) return alert ("No table(s) ID specified in parameters");
	var obj_tables = (document.all ? document.all[str_tableid] : document.getElementById(str_tableid));
	if (!obj_tables) return alert ("Can't find table(s) with specified ID (" + str_tableid + ")");

	// set defaults for optional parameters
	var col_config = [];
	col_config.header_offset = (num_header_offset ? num_header_offset : 1);
	col_config.footer_offset = (num_footer_offset ? num_footer_offset : 0);
	col_config.odd_color = 'rgba(255,255,230,0.5)';
	col_config.even_color = 'rgba(255,255,179,0.5)';
	col_config.mover_color = 'rgba(255, 163, 102,0.5)';
	col_config.onclick_color = 'rgba(204,102,0,0.5)';
   col_config.num_prerow = (num_pre_row ? num_pre_row : 2);

	// init multiple tables with same ID
	if (obj_tables.length)
		for (var i = 0; i < obj_tables.length; i++)
			tt_init_table(obj_tables[i], col_config);
	// init single table
	else
		tt_init_table(obj_tables, col_config);
}

function tt_init_table (obj_table, col_config) {
	var col_lconfig = [],
		col_trs = obj_table.rows;
	if (!col_trs) return;
	for (var i = col_config.header_offset; i < col_trs.length - col_config.footer_offset; i++) {
      var li_data = parseInt((i - col_config.header_offset) / col_config.num_prerow);
		col_trs[i].config = col_config;
		col_trs[i].lconfig = col_lconfig;
		col_trs[i].set_color = tt_set_color;
		col_trs[i].onmouseover = tt_mover; 
		col_trs[i].onmouseout = tt_mout;
		col_trs[i].onmousedown = tt_onclick;
		col_trs[i].order = (li_data) % 2;
		col_trs[i].onmouseout();
	}
}

function tt_set_color(str_color) {
  this.style.background=str_color;
}

// event handlers
function tt_mover () {
	if (this.lconfig.clicked != this)
		this.set_color(this.config.mover_color);
}
function tt_mout () {
	if (this.lconfig.clicked != this)
		this.set_color(this.order ? this.config.odd_color : this.config.even_color);
}
function tt_onclick () {
	if (this.lconfig.clicked == this) {
		this.lconfig.clicked = null;
		this.onmouseover();
	}
	else {
		var last_clicked = this.lconfig.clicked;
		this.lconfig.clicked = this;
		if (last_clicked) last_clicked.onmouseout();
		this.set_color(this.config.onclick_color);
	}
}
