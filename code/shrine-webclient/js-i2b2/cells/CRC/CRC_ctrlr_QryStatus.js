/**
 * @projectDescription	The Asynchronous Query Status controller (GUI-only controller).
 * @inherits 	i2b2.CRC.ctrlr
 * @namespace	i2b2.CRC.ctrlr.QueryStatus
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.0
 * ----------------------------------------------------------------------------------------
 * updated 8-10-09: Initial Creation [Nick Benik]
 */

function cgmUtcDateParser(dateString){
//Date format:  2011-02-21T14:35:03.480-05:00
 try{
	  splitDateAndTime = dateString.split("T");
	  vrDate = splitDateAndTime[0].split("-");
	  vrTime = splitDateAndTime[1].split(":");
	
	
	  strYear 	= vrDate[0];
	  strMonth 	= vrDate[1] - 1;
	  strDay 	= vrDate[2];	
	
	
	/*
	alert("Year: "+ strYear);
	alert("Month: "+ strMonth);
	alert("Day: "+ strDay);*/
	  
	
	  strHours	= vrTime[0];
	  strMins	= vrTime[1];  
	  strSecs	= null;
	  strMills	= null;
	
	
	  vSecs 	= vrTime[2].split(".");
	  strSecs  	= vSecs[0];
	  
	  vMills 	= vSecs[1].split("-");
	  strMills	= vMills[0];
	
	
	/*
	alert("Hours: "+ strHours);
	alert("Minutes: "+ strMins);
	alert("Seconds: "+ strSecs);
	alert("MilliSeconds: "+ strMills);*/
	 return new Date(strYear, strMonth, strDay, strHours, strMins, strSecs, strMills);
	}
 catch(e){
	return null;
 }
}

i2b2.CRC.ctrlr.QueryStatus = function(dispDIV) { this.dispDIV = dispDIV; };

i2b2.CRC.ctrlr.QueryStatus._GetTitle = function(resultType, oRecord, oXML, node_len) {
	var title = "";

	var x_plusmn_pats = 0;
	var x_obs_factor   = 3;
	
	var t = i2b2.h.XPath(oXML,'self::query_result_instance/description')[0].firstChild.nodeValue;
	
	var QRS_Status = i2b2.h.XPath(oXML, 'descendant-or-self::query_status_type/name')[0].firstChild.nodeValue;
	var QRS_Description = i2b2.h.XPath(oXML, 'descendant-or-self::query_status_type/description')[0].firstChild.nodeValue;
	
	// use given title if it exist otherwise generate a title
	try {
		if((t != null) && (t !== undefined) &&
		   (t.toUpperCase() == "AGGREGATED")
		){
			t = t.toUpperCase();
		  x_plusmn_pats = ((1 * node_len) - 1) * x_obs_factor;
		}
		else{
			x_plusmn_pats = x_obs_factor;
		}

	} catch(e) {
		var t = null;
	}
	
	switch (resultType) {
		case "PATIENTSET":
			if (!t) { t = "Patient Set"; }
			break;
		case "PATIENT_COUNT_XML":
			if (!t) { t="Patient Count"; }
			break;
		}

	
	if((QRS_Status == "ERROR") || (QRS_Status == "UNAVAILABLE")){
		title = t + " - <span title='"+QRS_Description +"'><b><font color='#dd0000'>Issue</font></b></span>";
	}
	else if((QRS_Status == "PROCESSING")){
		title = t + " - <span><b><font color='#00dd00'>Still Processing Request</font></b></span>";
	}
	else{
		if (oRecord.size >= 10) {
			if (i2b2.PM.model.userRoles.contains("DATA_AGG")) {
				title = t+" - "+oRecord.size+" patients";
			} else {
				title = t+" - "+oRecord.size+"&nbsp;&plusmn;"+ x_plusmn_pats +" patients";
			}
		} 
		else {
			if (i2b2.PM.model.userRoles.contains("DATA_AGG")) {
				title = t+" - "+oRecord.size+" patients";
			} else {
				title = t+" - 10 patients or fewer";
			}
		}
	}
	return title;
};

i2b2.CRC.ctrlr.QueryStatus.prototype = function() {
	var private_singleton_isRunning = false;
	var private_startTime = false;
	var private_refreshInterrupt = false;

    /*
       justin: disable polling because we're moving to a more synchronous behavior

	function private_pollStatus() {
		var self = i2b2.CRC.ctrlr.currentQueryStatus;
		// this is a private function that is used by all QueryStatus object instances to check their status
		// callback processor to check the Query Instance
		var scopedCallbackQI = new i2b2_scopedCallback();
		scopedCallbackQI.scope = self;
		scopedCallbackQI.callback = function(results) {
			if (results.error) {
				alert(results.errorMsg);
				return;
			} else {
				// find our query instance
				var qi_list = results.refXML.getElementsByTagName('query_instance');
				var l = qi_list.length;
				for (var i=0; i<l; i++) {
					var temp = qi_list[i];
					var qi_id = i2b2.h.XPath(temp, 'descendant-or-self::query_instance_id')[0].firstChild.nodeValue;
					if (qi_id == this.QI.id) {
						// found the query instance, extract the info
						this.QI.status = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/name')[0].firstChild.nodeValue;
						this.QI.statusID = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/status_type_id')[0].firstChild.nodeValue;
						if (this.QI.status == "INCOMPLETE") {
							// another poll is required
							setTimeout("i2b2.CRC.ctrlr.currentQueryStatus.pollStatus()", this.polling_interval);
						} else {
							private_singleton_isRunning = false;
							// force a final redraw
							i2b2.CRC.ctrlr.currentQueryStatus.refreshStatus();
							// refresh the query history window
							i2b2.CRC.ctrlr.history.Refresh();
						}
						break;
					}
				}
			}
		}


		// callback processor to check the Query Result Set
		var scopedCallbackQRS = new i2b2_scopedCallback();
		scopedCallbackQRS.scope = self;
		scopedCallbackQRS.callback = function(results) {
			if (results.error) {
				alert(results.errorMsg);
				return;
			} else {
				// find our query instance
				var qrs_list = results.refXML.getElementsByTagName('query_result_instance');
				var l = qrs_list.length;
				for (var i=0; i<l; i++) {
					var temp = qrs_list[i];
					var qrs_id = i2b2.h.XPath(temp, 'descendant-or-self::result_instance_id')[0].firstChild.nodeValue;
					if (self.QRS.hasOwnProperty(qrs_id)) {
						var rec = self.QRS[qrs_id];
					} else {
						var rec = new Object();
						rec.QRS_ID = qrs_id;
						rec.size = i2b2.h.getXNodeVal(temp, 'set_size');
						rec.QRS_Type = i2b2.h.XPath(temp, 'descendant-or-self::query_result_type/name')[0].firstChild.nodeValue;
						rec.QRS_TypeID = i2b2.h.XPath(temp, 'descendant-or-self::query_result_type/result_type_id')[0].firstChild.nodeValue;
					}
					rec.QRS_Status = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/name')[0].firstChild.nodeValue;
					rec.QRS_Status_ID = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/status_type_id')[0].firstChild.nodeValue;
					// create execution time string
					var d = new Date();
					var t = Math.floor((d.getTime() - private_startTime)/100)/10;
					var exetime = t.toString();
					if (exetime.indexOf('.') < 0) {
						exetime += '.0';
					}
					// deal with time/status setting
					if (!rec.QRS_time) { rec.QRS_time = exetime; }
					if (rec.QRS_Status == "INCOMPLETE" || rec.QRS_Status == "WAITTOPROCESS" || rec.QRS_Status == "PROCESSING") {
						// increment the running time only for parts that are still pending/processing
						rec.QRS_time = exetime;
					}
					// set the proper title if it was not already set
					if (!rec.title) {
						rec.title = i2b2.CRC.ctrlr.QueryStatus._GetTitle(rec.QRS_Type, rec, temp, l);
					}
					self.QRS[qrs_id] = rec;
				}
				// see if we need to poll another time
				if (self.QI.status == "INCOMPLETE") {
					setTimeout("i2b2.CRC.ctrlr.currentQueryStatus.pollStatus()", self.polling_interval);
				} else {
					// refresh the query history window
					i2b2.CRC.ctrlr.history.Refresh();
				}
			}
			// force a redraw
			i2b2.CRC.ctrlr.currentQueryStatus.refreshStatus();
		}

		// fire off the ajax calls
		i2b2.CRC.ajax.getQueryInstanceList_fromQueryMasterId("CRC:QueryStatus", {qm_key_value: self.QM.id}, scopedCallbackQI);
		i2b2.CRC.ajax.getQueryResultInstanceList_fromQueryInstanceId("CRC:QueryStatus", {qi_key_value: self.QI.id}, scopedCallbackQRS);
	}
	*/

	function private_refresh_status() {
		var self = i2b2.CRC.ctrlr.currentQueryStatus;
		// this private function refreshes the display DIV
		if (private_singleton_isRunning) {
			var dispMsg = '<div style="clear:both;"><div style="float:left; font-weight:bold">Running Query: "'+self.QM.name+'"</div>';
			// display the current run duration
			var d = new Date();
			var t = Math.floor((d.getTime() - private_startTime)/100)/10;
			var s = t.toString();
			if (s.indexOf('.') < 0) {
				s += '.0';
			}
			dispMsg += '<div style="float:right">['+s+' secs]</div>';
		} else {
			var dispMsg = '<div style="clear:both;"><div style="float:left; font-weight:bold">Finished Query: "'+self.QM.name+'"</div>';
		}
		dispMsg += '</div>';
		for (var i=0; i < self.QRS.length; i++) {
			var rec = self.QRS[i];
			
			dispMsg += '<div style="margin-left:20px; clear:both; height:16px; line-height:16px; "><div style="float:left; height:16px; line-height:16px; ">'+rec.title+'</div>';
			if (rec.QRS_time) {
				var t = '<font color="';
				switch(rec.QRS_Status) {
					case "ERROR":
						t += '#dd0000"><b>ERROR</b>';
						break;
					case "COMPLETED":
					case "FINISHED":
						t += '#0000dd">'+rec.QRS_Status;
						i2b2.CRC.ctrlr.history.Refresh();
						//i2b2.CRC.view.history.refreshPrevQueries();
						break;
					case "UNAVAILABLE":
						t += '#dd0000"> UNAVAILABLE' 
						break;
					case "INCOMPLETE":
					case "WAITTOPROCESS":
					case "PROCESSING":
						t += '#00dd00"><b>'+rec.QRS_Status +"</b>";
						break;
				}
				t += '</font> ';
				dispMsg += '<div style="float:right; height:16px; line-height:16px; ">'+t+'['+rec.QRS_time+' secs]</div>';
			}
			dispMsg += '</div>';
		}
		self.dispDIV.innerHTML = dispMsg;
		//self.dispDIV.style.backgroundColor = '#F00';
		self.dispDIV.style.display = 'none';
		self.dispDIV.style.display = 'block';

		if (!private_singleton_isRunning && private_refreshInterrupt) {
			// make sure our refresh interrupt is turned off
			try {
				clearInterval(private_refreshInterrupt);
				private_refreshInterrupt = false;
			} catch (e) {}
		}
	}


	function private_startQuery() {
		var self = i2b2.CRC.ctrlr.currentQueryStatus;
		if (private_singleton_isRunning) { return false; }
		private_singleton_isRunning = true;
		self.dispDIV.innerHTML = '<b>Processing Query: "'+this.name+'"</b>';
		self.QM.name = this.name;
		self.QRS = [];

		// callback processor to run the query from definition
		this.callbackQueryDef = new i2b2_scopedCallback();
		this.callbackQueryDef.scope = this;
		this.callbackQueryDef.callback = function(results) {
			if (results.error) {
				alert(results.errorMsg);
				return;
			} else {
				//		"results" object contains the following attributes:
				//			refXML: xmlDomObject <--- for data processing
				//			msgRequest: xml (string)
				//			msgResponse: xml (string)
				//			error: boolean
				//			errorStatus: string [only with error=true]
				//			errorMsg: string [only with error=true]
				// save the query master
				var temp = results.refXML.getElementsByTagName('query_master')[0];
				self.QM.id = i2b2.h.getXNodeVal(temp, 'query_master_id');
				try{
					self.QM.name = i2b2.h.XPath(temp, 'descendant-or-self::name')[0].firstChild.nodeValue;
				}
				catch(e){
					//Not need for every request
				}

				// save the query instance
				var temp = results.refXML.getElementsByTagName('query_instance')[0];
				self.QI.id = i2b2.h.XPath(temp, 'descendant-or-self::query_instance_id')[0].firstChild.nodeValue;
				self.QI.status = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/name')[0].firstChild.nodeValue;
				self.QI.statusID = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/status_type_id')[0].firstChild.nodeValue;

				
				// we don't need to poll, all Result instances are listed in this message
				if ((self.QI.status == "COMPLETED" || self.QI.status == "ERROR")) {
					// create execution time string
					var d = new Date();
					var t = Math.floor((d.getTime() - private_startTime)/100)/10;
					var exetime = t.toString();
					if (exetime.indexOf('.') < 0) {
						exetime += '.0';
					}
					var qi_list = results.refXML.getElementsByTagName('query_result_instance');
					var l = qi_list.length;
					for (var i=0; i<l; i++) {

						var v_start_date = null;
						var v_end_date	 = null;
						var d_diff_dt	 = null;
						var str_diff_dt	 = null;
						
						try {
							var qi = qi_list[i];
							var temp = new Object();
							temp.size = i2b2.h.getXNodeVal(qi, 'set_size');
							
							try{
								temp.QI_ID = i2b2.h.getXNodeVal(qi, 'query_instance_id');
								temp.QRS_ID = i2b2.h.getXNodeVal(qi, 'result_instance_id');
							}
							catch(e){
								//Error Result
							}
							try{
								temp.QRS_Type = i2b2.h.XPath(qi, 'descendant-or-self::query_result_type/name')[0].firstChild.nodeValue;
								temp.QRS_TypeID = i2b2.h.XPath(qi, 'descendant-or-self::query_result_type/result_type_id')[0].firstChild.nodeValue;
								temp.QRS_Status_ID = i2b2.h.XPath(qi, 'descendant-or-self::query_status_type/status_type_id')[0].firstChild.nodeValue;
								
								try{
									v_start_date = i2b2.h.XPath(qi, 'descendant-or-self::query_result_instance/start_date')[0].firstChild.nodeValue;
									v_end_date 	 = i2b2.h.XPath(qi, 'descendant-or-self::query_result_instance/end_date')[0].firstChild.nodeValue;
								
									
									var	v_difference = 0;
									
									try{
										d_start_dt = 
											cgmUtcDateParser(v_start_date);
										d_end_dt = 
											cgmUtcDateParser(v_end_date);
										v_difference = (d_end_dt.getTime() - d_start_dt.getTime());
									}
									catch(dd){
										v_difference = (d.getTime() - private_startTime);
									}
									
									d_diff_dt = Math.floor(v_difference/100)/10;
									
									str_diff_dt = ""+ d_diff_dt;
									if (str_diff_dt.indexOf('.') < 0) {
										str_diff_dt += '.0';
									}
								}
								catch(tt){
									v_start_date = null;
									v_end_date = null;
								}
							}
							catch(e){
								//Not Required for all requests
							}
							temp.QRS_Status = i2b2.h.XPath(qi, 'descendant-or-self::query_status_type/name')[0].firstChild.nodeValue;
							temp.QRS_Description = i2b2.h.XPath(qi, 'descendant-or-self::query_status_type/description')[0].firstChild.nodeValue;
							
							if((v_start_date == null) &&
								(v_end_date == null)){
								temp.QRS_time = exetime;	
							}
							else{
								temp.QRS_time = str_diff_dt;
							}
							
							
							
							
							// set the proper title if it was not already set
							if (!temp.title) {
								temp.title = i2b2.CRC.ctrlr.QueryStatus._GetTitle(temp.QRS_Type, temp, qi, l);
							}
                            self.QRS.push(temp);
						} catch	(e) {}
					}
					private_singleton_isRunning = false;
				} else {
					// another poll is required

                    // Justin: disabling this for now as we are adjusting the contract such that
                    // the runQueryByQueryDef always returns you the final result and will not require polling
					// setTimeout("i2b2.CRC.ctrlr.currentQueryStatus.pollStatus()", this.polling_interval);
				}
			}
		}

		// switch to status tab
		i2b2.CRC.view.status.showDisplay();

		// timer and display refresh stuff
		private_startTime = new Date();
		private_refreshInterrupt = setInterval("i2b2.CRC.ctrlr.currentQueryStatus.refreshStatus()", 100);

		// AJAX call
		i2b2.CRC.ajax.runQueryInstance_fromQueryDefinition("CRC:QueryTool", this.params, this.callbackQueryDef);
	}
	return {
		name: "",
		polling_interval: 1000,
		QM: {id:false, status:""},
		QI: {id:false, status:""},
		QRS:[],
		displayDIV: false,
		running: false,
		started: false,
		startQuery: function(queryName, ajaxParams) {
			this.name = queryName;
			this.params = ajaxParams;
			private_startQuery.call(this);
		},
		isQueryRunning: function() {
			return private_singleton_isRunning;
		},
		refreshStatus: function() {
			private_refresh_status();
		},
		pollStatus: function() {
			private_pollStatus();
		}
	};
}();

i2b2.CRC.ctrlr.currentQueryStatus = false; 

