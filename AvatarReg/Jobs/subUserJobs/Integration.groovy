//incoming parameters - serviceName, amiID, serviceConfigBaseURL

def parentstageName = "Integration"
def deployAmiTargetNode
def gitCredentials
def gitDeployURL
def environment = "pie"

node('master') {
	def masterWorkspace = pwd()
	sh """mkdir -p "${masterWorkspace}"
	"""
	getConfigFile(serviceConfigBaseURL,"serviceConfig.groovy")
	serviceConfigFile = new File("${masterWorkspace}/serviceConfig.groovy")
	def configObject = new ConfigSlurper().parse(serviceConfigFile.text)
	deployAmiTargetNode=configObject.deployAmiTargetNode
	gitDeployURL=configObject.gitDeployURL
	gitCredentials=configObject.gitCredentials
}

def subnetNums=2
for (subnetNum=1;subnetNum<=subnetNums;subnetNum++) {
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
					[$class: 'StringParameterValue', name: 'environment', value: environment ],
            ];
}


def getConfigFile(baseURL,fileName) {
    def workspace = pwd()
	sh """mkdir -p "${workspace}"
	"""
    def file = new File("${workspace}/${fileName}").newOutputStream()  
    file << new URL("${baseURL}/${fileName}").openStream()  
    file.close()
}