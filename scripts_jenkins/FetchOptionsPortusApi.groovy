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

import groovy.json.JsonSlurperClassic
import static org.apache.commons.lang3.StringEscapeUtils.*

tokenKey = "TOKEN"
repoUrlKey = "REPO_URL"
userIdKey = "REPO_USER_ID"

def getCredentials(credId = "portus_api_token", repoId = "REPO_URL", userId = "PORTUS_USER")
{
	def credentials = [:]
	
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
	
	return credentials
}

def getHealth(url)
{
	def get = new URL(url+"/api/v1/health").openConnection()

	get.setRequestProperty("Accept", "application/json")
	
	def responseCode = get.getResponseCode();
	
	if (responseCode == 200) 
	{
		return true
	}
	else
		return false
	
}


def getOptions(url, user, token, defaultOn, customOn, mode)
{
	def options = []
	
	if( defaultOn)
	{
		options.add("default")
	}
	
	def get = new URL(url).openConnection();
	
	get.setRequestProperty("Accept", "application/json")
	get.setRequestProperty("Portus-Auth", "${user}:${token}")
	
	def responseCode = get.getResponseCode();
	
	if (responseCode == 200) 
	{
		def response = new JsonSlurperClassic().parseText(get.getInputStream().getText());
		
		
			for(item in response)
		    {
				if( mode.equals("namespace") )
				{
					if( item.global == true)
						continue
					
					
						def desc = " \"name1\": \"item.name\", \"visibility\": \"${item.visibility}\" "
						if( item.team && item.team["name"] )
						{
							desc = desc + ", \"team\": \"${item.team["name"]}\" "
						}
						
						if( item.description )
						{
							def sani = item.description.trim().replaceAll("\"", "'")
							if( sani.length() > 15 )
								desc = desc + ", \"desc\": \"${sani.substring(0,14)} ...\" "
							else
								desc = desc + ", \"desc\": \"${sani} ...\" "
						}
						desc = desc + " "
						options.add(desc.replaceAll("\"", "'"))
				}
				
				if( mode.equals("team") )
				{
					
					def desc = " \"name1\": \"${item.name}\" "
					
					if( item.description )
					{
						def sani = item.description.trim().replaceAll("\"", "'")
						
						if( sani.length() > 15 )
							desc += ", \"desc\": \"${sani.substring(0,14)} ... \" "
						else
							desc += ", \"desc\": \"${sani}\" "
					}
					
					desc += " "
					options.add(desc.replaceAll("\"", "'"))
					
				}
			}
		}
	
		if( customOn )
			options.add("custom")
		
		return options
}

def getOptionsTest()
{
	return "one=1, two=2"
}

try
{
	//optionValue=true
	//portusApi=/api/v1/teams
	//key=name
	
	//optionDescription=true
	//portusApi=/api/v1/teams
	
	//binding.hasVariable('superVariable')
	

	if ( ! binding.hasVariable("tokenKey") )
		def tokenKey = "TOKEN"
	
	if ( ! binding.hasVariable("repoUrlKey") )
		def repoUrlKey = "REPO_URL"
	
	if ( ! binding.hasVariable("userIdKey") )
		def userIdKey = "REPO_USER_ID"
	
	
	if( ! binding.hasVariable("portusApi") || ! portusApi.trim() )
		return ["Error: Portus Api not Found"]
	
	
	def credentials = getCredentials()
	
	if( ! credentials.size() == 3 )
		return ["Credentials Failed"]
	
	/*
	if( ! getHealth(credentials[repoUrlKey]) )
	{
		return ["Error: Portus not reachable"]
	}
	*/
	
	def defaultOn = false
	def customOn = false
	def mode = ""
	def url = ""
	def user = credentials[userIdKey]
	def token = credentials[tokenKey]
	
	if( portusApi.trim().equals("/api/v1/namespaces"))
	{
		defaultOn = true
		customOn = true
		mode = "namespace"
		portusApi = portusApi.trim() + "?all=true"
	}
	
	if( portusApi.trim().equals("/api/v1/teams") )
	{
		defaultOn = false
		customOn = true
		mode = "team"
		portusApi = portusApi.trim() + "?all=true"
	}
	
	url = credentials[repoUrlKey] + portusApi
	
	//return getOptions(url, user, token, defaultOn, customOn, mode )
	return getOptionsTest()
}
catch (Exception e)
{
	return [e.getMessage()]
}

/*
Namespace Sample
{
  "id": 10,
  "name": "cpsiot_build_linux_v7_arm",
  "created_at": "2019-02-16T01:01:26.000Z",
  "updated_at": "2019-02-16T01:01:26.000Z",
  "description": null,
  "team": {
    "id": 7,
    "name": "testa-team",
    "hidden": false
  },
  "visibility": "private",
  "global": false
}

Team Sample
{
  "id": 9,
  "name": "demo-team2",
  "created_at": "2019-02-15T13:19:41.000Z",
  "updated_at": "2019-02-15T13:19:41.000Z",
  "description": "Test Team 2",
  "hidden": false
}

*/
