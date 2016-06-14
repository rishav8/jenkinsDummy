//incoming parameters - gitCredentials, serviceName, amiID, gitDeployURL, serviceConfigPath

def parentstageName = "Capacity"
def deployAmiTargetNode = 'AMIBuilder'


def subnetNums=[1,2]
for (subnetNum in subnetNums) {
	stage "${parentstageName}::DeploySubnet ${subnetNum}"
	subJob = build  job: '../../wppCommon/subJobs/subnetDeploy'/*,
            parameters: [
						[$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
				        [$class: 'StringParameterValue', name: 'gitDeployURL', value: gitDeployURL ],
						[$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
						[$class: 'StringParameterValue', name: 'parentstageName', value: parentstageName ],
						[$class: 'StringParameterValue', name: 'amiID', value: amiID ],
						[$class: 'StringParameterValue', name: 'subnetNum', value: subnetNum.toString() ],
				        [$class: 'StringParameterValue', name: 'serviceConfigPath', value: serviceConfigPath],
				        [$class: 'StringParameterValue', name: 'targetNode', value: deployAmiTargetNode ],
            ];*/
}

stage "${parentstageName}::runMicroLoadTest"
subJob = build  job: '../../wppCommon/subJobs/runMicroLoadTest'/*,
            parameters: [
                [$class: 'StringParameterValue', name: 'gitCredentials', value: gitCredentials ],
				[$class: 'StringParameterValue', name: 'gitDeployURL', value: gitDeployURL ],
                [$class: 'StringParameterValue', name: 'serviceName', value: serviceName ],
                [$class: 'StringParameterValue', name: 'parentstageName', value: parentstageName ],
                [$class: 'StringParameterValue', name: 'amiID', value: amiID ],
				[$class: 'StringParameterValue', name: 'serviceConfigPath', value: serviceConfigPath ],
				[$class: 'StringParameterValue', name: 'deployAmiTargetNode', value: deployAmiTargetNode ],
                ];*/