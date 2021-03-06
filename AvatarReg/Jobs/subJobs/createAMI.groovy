import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

//incoming variables
//def url = "https://github.com/WPPg2/DevOps-Deployment"
//def gitCredentials = "8cf0000b-3991-4db0-a2d9-e157168d2cef"
//def serviceName="avreg"
//def commitID
//def instanceID="i-576d7efb"
//def serviceConfigPath = "/home/ec2-user/avregPipPilot/pipeline/services"
//def targetNode = 'AMIBuilder'
//def artifactVersion
//def timestamp

def amiName="${serviceName}_${artifactVersion}"
def amiID
def slaveWorkspaceDir
def masterWorkspace
def tempDir

node(targetNode)
{
	//get slave workspace directory
    slaveWorkspaceDir = pwd()
}

node('master')
{
	masterWorkspace = pwd()
	getConfigFile(serviceConfigBaseURL,"create_ami.json")
    //manipulating json file 1
    File inputFile = new File("${masterWorkspace}/create_ami.json")
    content = inputFile.text
    def slurped = new JsonSlurper().parseText(content)
    tempDir = slurped.log.temp_dir
	def amiTags = slurped.aws.ami.tags[0].value
	amiTags = amiTags.replaceAll(~/COOKBOOKVERSION/, "${cookbookVersion}")
	amiTags = amiTags.replaceAll(~/ARTIFACTVERSION/,"${artifactVersion}")
	amiTags = amiTags.replaceAll(~/TIMESTAMP/,"${timestamp}")
    def builder = new JsonBuilder(slurped)
    builder.content.aws.ami.tags[1].value="${commitID}"
    builder.content.aws.instance.id="${instanceID}"
    builder.content.aws.ami.name="${amiName}"
	builder.content.aws.ami.tags[0].value="${amiTags}"
    content = builder.toPrettyString()
    builder=null
}

node(targetNode)
{	
	//git checkout before writeFile
    git credentialsId: "$gitCredentials", url: "$url"   
    writeFile file: "create_ami.json", text: content
	
	//run create AMI from instance recipe
    sh """cd ${slaveWorkspaceDir}/chef-repos/aws-manager-repo/cookbooks
    sudo chef-client -z -j ${slaveWorkspaceDir}/create_ami.json -r 'recipe[ec2::createAMIFromInstance]' --log_level info"""
}

node('master')
{
	getConfigFile(serviceConfigBaseURL,"terminate_instance.json")
    //manipulating json file 2
    File inputFile2 = new File("${masterWorkspace}/terminate_instance.json")
    content = inputFile2.text
    def slurped2 = new JsonSlurper().parseText(content)
    def builder2 = new JsonBuilder(slurped2)
    builder2.content.aws.instance.id="${instanceID}"
    content = builder2.toPrettyString()
    builder2=null
}

node(targetNode)
{
    writeFile file: "terminate_instance.json", text: content
	
	//run terminate instance recipe
    sh """cd ${slaveWorkspaceDir}/chef-repos/aws-manager-repo/cookbooks
    sudo chef-client -z -j ${slaveWorkspaceDir}/terminate_instance.json -r 'recipe[ec2::terminateInstance]' --log_level info"""
	
	//get AMI ID from log file
    def content = readFile file: "${tempDir}/${serviceName}.log.serviceami"
    def slurped = new JsonSlurper().parseText(content)
    amiID = slurped.ImageId
    println "AMI ID is ${amiID}"
	
    def amiContent = "${serviceName}_ami_id=${amiID}"
    writeFile file: "${serviceName}.log.amiidprop", text: amiContent
}

//return values - amiID
currentBuild.setDescription("#amiID="+amiID)


def getConfigFile(baseURL,fileName) {
    def workspace = pwd()
    sh """mkdir -p "${workspace}"
	"""
    def file = new File("${workspace}/${fileName}").newOutputStream()  
    file << new URL("${baseURL}/${fileName}").openStream()  
    file.close()
}