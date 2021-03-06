//incoming parameters - serviceName, amiID, serviceConfigBaseURL
def gitCredentials
def gitDeployURL
def parentstageName = "Acceptance"
def deployAmiTargetNode


node('master') {
	def masterWorkspace = pwd()
	getConfigFile(serviceConfigBaseURL,"serviceConfig.groovy")
	serviceConfigFile = new File("${masterWorkspace}/serviceConfig.groovy")
	def configObject = new ConfigSlurper().parse(serviceConfigFile.text)
	deployAmiTargetNode=configObject.deployAmiTargetNode
	gitDeployURL=configObject.gitDeployURL
	gitCredentials=configObject.gitCredentials
}

def subnetNums=[1,2]
for (subnetNum in subnetNums) {
	stage "${parentstageName}::DeploySubnet ${subnetNum}"
	subJob = build  job: '../subJobs/subnetDeploy',
            parameters: [
						[$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
				        [$class: 'StringParameterValue', name: 'gitDeployURL', value: gitDeployURL ],
						[$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
						[$class: 'StringParameterValue', name: 'amiID', value: amiID ],
						[$class: 'StringParameterValue', name: 'subnetNum', value: subnetNum.toString() ],
				        [$class: 'StringParameterValue', name: 'serviceConfigBaseURL', value: serviceConfigBaseURL],
				        [$class: 'StringParameterValue', name: 'targetNode', value: deployAmiTargetNode ],
            ];
}
stage "${parentstageName}::runFunctionalTest"


				
def getConfigFile(baseURL,fileName) {
    def workspace = pwd()
    def file = new File("${workspace}/${fileName}").newOutputStream()  
    file << new URL("${baseURL}/${fileName}").openStream()  
    file.close()
}