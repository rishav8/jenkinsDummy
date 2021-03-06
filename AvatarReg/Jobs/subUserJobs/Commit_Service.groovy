//incoming parameters
//serviceConfigBaseURL
//def serviceName = "avreg"
//def buildTrigger = "NIGHTLY"

def buildPublishTargetNode
def deployAmiTargetNode
def rootPomPath
def gitServiceURL
def gitCredentials
def gitLoadTestURL
def cookbookVersion
def parentstageName = "Commit"

node('master') {
	def masterWorkspace = pwd()
	sh """mkdir -p "${masterWorkspace}"
	"""
	getConfigFile(serviceConfigBaseURL,"serviceConfig.groovy")
	serviceConfigFile = new File("${masterWorkspace}/serviceConfig.groovy")
	def configObject = new ConfigSlurper().parse(serviceConfigFile.text)
	gitServiceURL=configObject.gitServiceURL
	gitDeployURL=configObject.gitDeployURL
	buildPublishTargetNode=configObject.buildPublishTargetNode
	deployAmiTargetNode=configObject.deployAmiTargetNode
	gitLoadTestURL=configObject.gitLoadTestURL
	gitCredentials=configObject.gitCredentials
	rootPomPath=configObject.serviceRootPomPath
	cookbookVersion=configObject.cookbookVersion
	configObject = null
}

stage "${parentstageName}::BuildAndPublish"

    subJob1 = build  job: '../subJobs/buildAndPublish',
                    parameters: [
                        [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
                        [$class: 'StringParameterValue', name: 'url', value: gitServiceURL ],
                        [$class: 'StringParameterValue', name: 'buildTrigger', value: buildTrigger ],
                        [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                        [$class: 'StringParameterValue', name: 'targetNode', value: buildPublishTargetNode],
						[$class: 'StringParameterValue', name: 'rootPomPath', value: rootPomPath],
                    ] ;
    
    // Returned Values
    def artifactVersion = subJob1.description.tokenize('#')[0].tokenize('=')[1]
    def commitID = subJob1.description.tokenize('#')[1].tokenize('=')[1]
	def timestamp = subJob1.description.tokenize('#')[2].tokenize('=')[1]

    
stage "${parentstageName}::Deploy"
    subJob2 = build  job: '../subJobs/deploy',
                    parameters: [
							[$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
							[$class: 'StringParameterValue', name: 'url', value: gitDeployURL ],
							[$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
							[$class: 'StringParameterValue', name: 'serviceConfigBaseURL', value: serviceConfigBaseURL ],
							[$class: 'StringParameterValue', name: 'artifactVersion', value: artifactVersion ],
							[$class: 'StringParameterValue', name: 'targetNode', value: deployAmiTargetNode ],
							[$class: 'StringParameterValue', name: 'cookbookVersion', value: cookbookVersion ],
                    ] ;
                    
    // Returned Values                
    def instanceID = subJob2.description.tokenize('#')[0].tokenize('=')[1]
    
    
    
stage "${parentstageName}::CreateAMI"
    subJob3 = build  job: '../subJobs/createAMI',
                     parameters: [
							[$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
							[$class: 'StringParameterValue', name: 'url', value: gitDeployURL ],
							[$class: 'StringParameterValue', name: 'artifactVersion', value: artifactVersion ],
							[$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
							[$class: 'StringParameterValue', name: 'serviceConfigBaseURL', value: serviceConfigBaseURL ],
							[$class: 'StringParameterValue', name: 'instanceID', value: instanceID ],
							[$class: 'StringParameterValue', name: 'commitID', value: commitID ],
							[$class: 'StringParameterValue', name: 'targetNode', value: deployAmiTargetNode ],
							[$class: 'StringParameterValue', name: 'cookbookVersion', value: cookbookVersion ],
							[$class: 'StringParameterValue', name: 'timestamp', value: timestamp ]
                    ] ;
					
    // Returned Values                
    def amiID = subJob3.description.tokenize('#')[0].tokenize('=')[1]
	
	
currentBuild.setDescription("#amiID="+amiID+"#commitID="+commitID)

def getConfigFile(baseURL,fileName) {
    def workspace = pwd()
	sh """mkdir -p "${workspace}"
	"""
    def file = new File("${workspace}/${fileName}").newOutputStream()  
    file << new URL("${baseURL}/${fileName}").openStream()  
    file.close()
} 