{
  urlProxy: "/shrine-proxy/request",	
	urlFramework: "js-i2b2/",
	loginTimeout: 15, // in seconds
	//JIRA|SHRINE-519:Charles McGow
	username_label:"test username:", //Username Label
	password_label:"test password:", //Password Label
	//JIRA|SHRINE-519:Charles McGow
	// -------------------------------------------------------------------------------------------
	// THESE ARE ALL THE DOMAINS A USER CAN LOGIN TO
	lstDomains: [
		{ domain: "HarvardDemo",
		  name: "VM Java 1.4",
		  debug: true,
		  allowAnalysis: true,
		  urlCellPM: "http://services.i2b2.org/PM/rest/PMService/"
		},
		{ domain: "HarvardDemo",
		  name: "VM Java 1.4 [SHRINE]",
		  urlCellPM: "http://services.i2b2.org/PM/rest/PMService/",
		  allowAnalysis: false,
		  isSHRINE: true
		},
		{ domain: "demo",
		  name: "VM Java 1.3 RC4",
		  debug: true,
		  urlCellPM: "http://10.0.52.50:9090/axis2/rest/PMService/"
		},
		{ domain: "HMS_JAVA",
		  name: "Harvard Demo (Java 1.2)",
		  debug: true,
		  urlCellPM: "http://webservices.i2b2.org/PM/rest/PMService/"
		},
		{ domain: "demo",
		  name: "SHRINE CBMI DEMO",
		  debug: true,
		  urlCellPM: "http://cbmi-i2b2-dev:7070/axis2/rest/PMService/",
		  allowAnalysis: false,
		  isSHRINE: true
		},
        //carranet does not currenlty use the sheriff hence isSHrine is disabled
        { domain: "i2b2demo",
          name: "SHRINE CarraNet DEMO",
          debug: true,
          urlCellPM: "http://rc-carra-clone-2:7070/axis2/rest/PMService/",
          allowAnalysis: false,
          isSHRINE: false
        }
	]
	// -------------------------------------------------------------------------------------------
}
