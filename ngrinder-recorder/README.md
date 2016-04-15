nGrinder Recorder Chrome Extension
========

How to use
---------------------

     Setting
		- URL Filter : Use a regex pattern
		- Record ON/OFF
		- Clear button : Remove a all record list.
		- Generate Groovy/Jython button : Create automatically a script by list of recorded request.
	 Generate
		- Hide resource request/Show resource request
			: Hide/Show "js" or "css" or "image file"
		- Choose a request
			: If you click request raw, it request will be generated for script.
		- Delete None Scope button
			: If request list is too many, click this button after selecte the required request.
		- Create Script button
			: Do generate to the script by choosed request.
	 Use Script
		Upload the file to ngrinder script page(http://~/script/)
		- request.json : json of choosed request list
		- script file : Result script file.
		
How to deploy
---------------------
	 Create a package to .ctx
	 	Chrome developer guide : https://developer.chrome.com/extensions/packaging
	 	1. Move to the Extensions management page by this URL: chrome://extensions
	 	2. Enable a "Developer mode"
	 	3. Click the "Pack extension" button.
	 	4. Choose the "NGRINDER_PROJECT_HOME/ngrinder-recorder" folder in "Extension root directory" 
	 	5. Choose the "NGRINDER_PROJECT_HOME/ngrinder-recorder.pem" file in "private key file (optional)"
	 	6. Then you can gain "ngrinder-recorder.ctx" file.
	 	7. Move a "ngrinder-recorder.ctx" file to "NGRINDER_PROJECT_HOME/ngrinder-controller/src//main/webapp/crx/ngrinder-recorder-VERSION.ctx" 