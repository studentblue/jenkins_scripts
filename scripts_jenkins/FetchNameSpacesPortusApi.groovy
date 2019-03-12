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

tokenKey = "TOKEN"
repoUrlKey = "REPO_URL"
userIdKey = "REPO_USER_ID"
namespacesApi = "/api/v1/namespaces?all=true"
separator = " @::@ "

def credentials = []

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

def getID(url, username, password, match, health = false, tags = false)
{
	def get = new URL(url).openConnection();

	get.setRequestProperty("Accept", "application/json")
	get.setRequestProperty("Portus-Auth", "${username}:${password}")
	
	def responseCode = get.getResponseCode();
	
	if (responseCode == 200) 
	{
		if( health )
			return true
		
		def response = new JsonSlurperClassic().parseText(get.getInputStream().getText());
	    
	    for(item in response)
	    {
			if( tags )
			{
				artifacts.add(item.name)
				
			}
			else
			{
				if( match.equals(item.name ))
				{
					return item.id
				}
			}
		}
		
		if( tags )
			return true
	}

	return false
	
}

def getNameSpaces(url, username, password, optionValue, optionDescription, defaultOn = true, emptyOn = true, )
{
	def namespaces = []
	
	if( defaultOn )
		namespaces.add("default")
	
	def get = new URL(url).openConnection();
	
	get.setRequestProperty("Accept", "application/json")
	get.setRequestProperty("Portus-Auth", "${username}:${password}")
	
	def responseCode = get.getResponseCode();
	
	if (responseCode == 200) 
	{
		def response = new JsonSlurperClassic().parseText(get.getInputStream().getText());
		
		for(item in response)
	    {
	    
			if( item.global == true)
				continue
			
			def desc = ""
			def team = ""
			

			if( item.team && item.team["name"] )
			{
				team = item.team["name"]
			}
			
			if( item.description )
			{
				desc = item.description.trim()
				if( desc )
				{
					if(desc.length() > 15)
						desc = item.description.substring(0,14) + " ..."
					else
						desc = item.description
				}
			}
			
			def option = item.name
			if( team)
				option += " Team: " + team + " "
			if(desc)
				option +=   "( " + desc + " )" 
			
			option += separator + item.id
			namespaces.add(option)
		}
	}
	
	if( emptyOn )
		namespaces.add("custom")

	return namespaces
	
}

try
{	
	credentials = getCredentials()
	

	if( ! credentials.size() == 3 )
		return ["Credentials Failed"]
	
	if( ! getID(credentials[repoUrlKey], credentials[userIdKey], credentials[tokenKey], "", true, false) )
		return ["Health Check Failed"]
	
	return getNameSpaces(credentials[repoUrlKey] + namespacesApi, credentials[userIdKey], credentials[tokenKey], optionValue, optionDescription)
}
catch (Exception e)
{
	return [e.getMessage()]
}
