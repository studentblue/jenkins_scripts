import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*
import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty
import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import com.mig82.folders.properties.FolderProperties

import groovy.json.*
import org.boon.Boon
 
def url = ""
def user = ""
def token = ""
def api = ""

def getCredentials(credId = "portus_api_token", repoId = "REPO_URL", userId = "PORTUS_USER")
{
	def credentials = [:]
	
	def tokenKey = "TOKEN"
	def repoUrlKey = "REPO_URL"
	def userIdKey = "REPO_USER_ID"
	
	Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
	{
		f ->
			if( f.getParent() instanceof hudson.model.Hudson )
			{
				AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
				FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)
				
				if( property )
				{
					creds = property.getCredentials(StringCredentialsImpl.class)
					
					//println creds
					
					if( creds )
					{
						creds.each
						{
							f1 ->
								if( f1.getId().trim().equals(credId) )
								{
									credentials.put(tokenKey, f1.getSecret())
									return true
								}
						}
					}
				}
			}
	}
	
	Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
	{
		f ->
			if( f.getParent() instanceof hudson.model.Hudson )
			{	
				AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
				FolderProperties property = folderAbs.getProperties().get(FolderProperties.class)
				
				if( property )
				{
					property.properties.each
					{
						f1 -> 
							if( f1.key.trim().equals(repoId) )
							{
								credentials.put(repoUrlKey, f1.value)
							}
							
							if( f1.key.trim().equals(userId) )
							{
								credentials.put(userIdKey, f1.value)
							}
					}
				}
			}
	}
	
	url = credentials[repoUrlKey]
	user = credentials[userIdKey]
	token = credentials[tokenKey]
	
}

def portusApiGET(api)
{
	def url2 = url + api
	def get = new URL(url2).openConnection();
	
	get.setRequestProperty("Accept", "application/json")
	get.setRequestProperty("Portus-Auth", "${user}:${token}")
	
	def responseCode = get.getResponseCode();
	
	if (responseCode == 200) 
	{
		def response = new JsonSlurperClassic().parseText(get.getInputStream().getText());
		return response
	}
	else
		return []
}

def generateJsonEditorOptions( namespaces = "", teams = "" )
{
	def jsonEditorOptions = [:]
	
	jsonEditorOptions.put("disable_edit_json", true)
	jsonEditorOptions.put("disable_properties", true)
	jsonEditorOptions.put("no_additional_properties", true)
	jsonEditorOptions.put("disable_collapse", true)
	jsonEditorOptions.put("disable_array_add", true)
	jsonEditorOptions.put("disable_array_delete", true)
	jsonEditorOptions.put("disable_array_reorder", true)
	//~ jsonEditorOptions.put("theme", "bootstrap2")
	jsonEditorOptions.put("theme", "bootstrap3")
	//~ jsonEditorOptions.put("iconlib", "fontawesome4")
	
	jsonEditorOptions.put("schema", [:])
	
	def schema = jsonEditorOptions["schema"]
	schema.put("type", "object")
	//~ schema.put("title", "Portus Namespaces")
	schema.put("properties", [:] )
	
	def properties = jsonEditorOptions["schema"]["properties"]
	//dockerhub repo name
	properties.put("DockerHubRepoName", [:] )
	
	def DockerHubRepoName = jsonEditorOptions["schema"]["properties"]["DockerHubRepoName"]
	
	DockerHubRepoName.put("title","Docker Hub Repo")
	DockerHubRepoName.put("type","string")
	DockerHubRepoName.put("propertyOrder", 1)
	
	//DockerHub_Repo_Tag
	properties.put("DockerHubRepoTag", [:] )
	
	def DockerHubRepoTag = jsonEditorOptions["schema"]["properties"]["DockerHubRepoTag"]
	
	DockerHubRepoTag.put("title","Docker Hub Repo Tag")
	DockerHubRepoTag.put("type","string")
	DockerHubRepoTag.put("propertyOrder", 2)
	
	//Portus_NameSpace Selector
	properties.put("PortusNameSpaceSelector", [:] )
	
	def PortusNameSpaceSelector = jsonEditorOptions["schema"]["properties"]["PortusNameSpaceSelector"]
	
	PortusNameSpaceSelector.put("PortusNameSpaceSelector", [:] )
	PortusNameSpaceSelector.put("title","Portus Name-Space")
	PortusNameSpaceSelector.put("type","string")
	PortusNameSpaceSelector.put("propertyOrder", 3)
	
	def values = getNameSpacesFromData( namespaces , "V")
	def options = getNameSpacesFromData( namespaces , "O")
	
	PortusNameSpaceSelector.put("enum", values)
	PortusNameSpaceSelector.put("options", [:])
	PortusNameSpaceSelector["options"].put("enum_titles", options)
	
	//PortusNameSpaceDescription
	properties.put("PortusNameSpaceDescription", [:] )
	
	def PortusSpaceDescription = jsonEditorOptions["schema"]["properties"]["PortusNameSpaceDescription"]
	PortusSpaceDescription.put("title","Custom Portus NameSpace")
	PortusSpaceDescription.put("type","string")
	PortusSpaceDescription.put("propertyOrder", 4)
	
	//PortusTeamSelector
	properties.put("PortusTeamSelector", [:] )
	
	def PortusTeamSelector = jsonEditorOptions["schema"]["properties"]["PortusTeamSelector"]
	
	PortusTeamSelector.put("title","Portus Team for Name Space")
	PortusTeamSelector.put("type","string")
	PortusTeamSelector.put("propertyOrder", 5)
	
	values = getTeamsFromData( teams , "V")
	options = getTeamsFromData( teams , "O")
	
	PortusTeamSelector.put("enum", values)
	PortusTeamSelector.put("options", [:])
	PortusTeamSelector["options"].put("enum_titles", options)
	
	//PortusTeamCustom
	properties.put("PortusTeamCustom", [:] )
	
	def PortusTeamCustom = jsonEditorOptions["schema"]["properties"]["PortusTeamCustom"]
	PortusTeamCustom.put("title","Custom Portus Team")
	PortusTeamCustom.put("type","string")
	PortusTeamCustom.put("propertyOrder", 6)
	
	//PortusImageName
	properties.put("PortusImageName", [:] )
	
	def PortusImageName = jsonEditorOptions["schema"]["properties"]["PortusImageName"]
	PortusImageName.put("title","Custom Portus Team")
	PortusImageName.put("type","string")
	PortusImageName.put("propertyOrder", 7)
	
	
	jsonEditorOptions.put("startval", [:] )
	
	jsonEditorOptions["startval"].put("DockerHubRepoName", "debian")
	jsonEditorOptions["startval"].put("DockerHubRepoTag", "latest")
	jsonEditorOptions["startval"].put("PortusNameSpaceSelector", getNameSpacesFromData( namespaces , "V1"))
	jsonEditorOptions["startval"].put("PortusNameSpaceDescription", "My Name Portus Name Space")
	jsonEditorOptions["startval"].put("PortusTeamSelector", getTeamsFromData( teams , "V1"))
	jsonEditorOptions["startval"].put("PortusTeamCustom", "My Portus Team")
	jsonEditorOptions["startval"].put("PortusImageName", "cpsiot-debian")
	
	return jsonEditorOptions
}

def getTeamsFromData( teams , mode)
{
	def values = []
	def options = []
	
	for(team in teams)
	{
		//Values
		values.add(team.name)
		
		//description / option
		def desc = " Name: "+team.name
		if( team.description )
		{
			def sani = team.description.trim()
			if( sani.length() > 15 )
				desc += " ( "+sani.substring(0,14)+" ... ) "
			else
				desc += " ( "+sani+" )"
		}
		options.add(desc)
		
		if( mode.equals("V1") )
			break
	}
	
	if( mode.equals("V1") )
	{
		if( values )
			return values[0]
		else
			return ""
	}
	
	options.add("Define own Team")
	values.add("custom")
	
	if( mode.equals("V") )
		return values
	
	if( mode.equals("O") )
		return options
	
	return []	
}

def getNameSpacesFromData( namespaces , mode)
{
	def values = []
	def options = []
	
	for(namespace in namespaces)
	{
		//println namespace["global"]
		if( namespace.global == true)
			continue
		
		//Values
		values.add(namespace.name)
		
		//description / option
		def desc = " Name: "+namespace.name
		
		if( namespace.description )
		{
			def sani = namespace.description.trim()
			if( sani.length() > 30 )
				desc += " ( "+sani.substring(0,29)+" ... ) "
			else
				desc += " ( "+sani+" )"
		}
		
		desc += ", Visibility: "+namespace.visibility
		
		if( namespace.team && namespace.team["name"] )
		{
			desc += ", Team: "+namespace.team["name"]
		}
		options.add(desc)
		
		if( mode.equals("V1") )
			break
	}
	
	if( mode.equals("V1") )
	{
		if( values )
			return values[0]
		else
			return ""
	}
	
	options.add("Define own NameSpace")
	values.add("custom")
	
	if( mode.equals("V") )
		return values
	
	if( mode.equals("O") )
		return options
	
	return []	
}

getCredentials()
def namespaces = portusApiGET("/api/v1/namespaces?all=true")
def teams = portusApiGET("/api/v1/teams?all=true")
def json = generateJsonEditorOptions( namespaces, teams )
//~ def json = generateJsonEditorOptions()

def test = Boon.toJson(json)

Boon.fromJson(test)

