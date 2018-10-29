node {
    stage('Prepare'){
	// Start neccessary services to download required dependencies
   //       build job: 'StartServices'
	  build job: 'Sync-Repo'
    }
    stage('Check out') {
	    retry(3){
        	checkout scm
	    }
    }
    stage('Build') {
	// FIXME: Use full mvn patch due to mvn command not found issue - no idea why
    	if (env.BRANCH_NAME.findAll(/^[Release]+/)) {
    		sh '''
		    cd source
		    sudo /usr/local/bin/mvn clean verify -Pprod
	        '''
    	} else {
    		sh '''
		    cd source
		    sudo /usr/local/bin/mvn clean verify -Pdev
	        '''
    	}       
   }
    stage('Package') {
	script {
		//Retrieves version number from source    
		String workspace = pwd()
		def versionContent = readFile "${workspace}/source/com.kms.katalon/about.mappings"
		def versionNumber = (versionContent =~ /([0-9]+)[\.,]?([0-9]+)[\.,]?([0-9])/)
		String buildVersion = versionNumber[0][0]
	}
	   
        sh '''
            sudo ./package.sh ${JOB_BASE_NAME} ${buildVersion} ${BUILD_TIMESTAMP}
        '''

        if (env.BRANCH_NAME == 'release') {
                sh '''
                    sudo ./verify.sh ${JOB_BASE_NAME} ${buildVversion} ${BUILD_TIMESTAMP}
                '''
        }
    }
    stage('Notify') {
	emailext body: "Pipeline error: ${err}\nPlease go to ${BUILD_URL} and verify the build",
                recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']],
                subject: "'${JOB_NAME}' (${BUILD_NUMBER}) failed",
                to: 'datquach@kms-technology.com'    	
    }
}
