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

//"$schema":"http://json-schema.org/draft-04/schema",


folder = "haris"

def getCredentials(credId = "portus_api_token", repoId = "REPO_URL", userId = "PORTUS_USER")
{
	def credentials = [:]
	
	def folderProperties = [:]
	
	Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
	{
		f ->
			if( f.getParent() instanceof hudson.model.Hudson )
			{
				if( ! f.name.equals( folder ) )
				{
					//~ println f.name
					return
				}
				
				AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
				FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)
				
				if( property )
				{
					//~ creds = property.getCredentials(StringCredentialsImpl.class)
					creds = property.getCredentials(UsernamePasswordCredentialsImpl.class)
					
					
					//println creds
					
					//~ if( creds )
					//~ {
						//~ creds.each
						//~ {
							//~ f1 ->
								//~ if( f1.getId().trim().equals(credId) )
								//~ {
									//~ credentials.put(tokenKey, f1.getSecret())
									//~ return true
								//~ }
						//~ }
					//~ }
					
					if( creds )
					{
						creds.each
						{
							f1 ->
								//~ if( f1.getId().trim().equals(credId) )
								//~ {
									//~ println f1.getId()
									
									//~ println f1.getUsername() + ":" + hudson.util.Secret.toString(f1.getPassword())
									
									//~ println f1.getUsername().getClass()
									
									//~ println hudson.util.Secret.toString(f1.getPassword()).getClass()
									if( f1.getId().trim().equals(credId) )
										credentials.put(f1.getId(), ["user": f1.getUsername(), "pass": hudson.util.Secret.toString(f1.getPassword())])
									
									//~ return true
								//~ }
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
				
				if( ! f.name.equals( folder ) )
				{
					//~ println f.name
					return
				}
				
				AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
				FolderProperties property = folderAbs.getProperties().get(FolderProperties.class)
				
				if( property )
				{
					property.properties.each
					{
						f1 -> 
							//~ if( f1.key.trim().equals(repoId) )
							//~ {
								//~ credentials.put(repoUrlKey, f1.value)
							//~ }
							
							//~ if( f1.key.trim().equals(userId) )
							//~ {
								//~ credentials.put(userIdKey, f1.value)
							//~ }
							
							folderProperties.put( f1.getKey(), f1.getValue() )
					}
				}
			}
	}
	
	//~ url = credentials[repoUrlKey]
	//~ user = credentials[userIdKey]
	//~ token = credentials[tokenKey]
	
	return ["creds": credentials, "folder" : folderProperties]
	
}

def portusApiGET(url, api, user, token)
{
	def url2 = url + api
	def get = new URL(url2).openConnection();
	
	get.setRequestProperty("Accept", "application/json")
	get.setRequestProperty("Portus-Auth", "${user}:${token}")
	
  	//~ println user 
  
	def responseCode = get.getResponseCode();
	
	if (responseCode == 200) 
	{
		def response = new JsonSlurperClassic().parseText(get.getInputStream().getText());
		return response
	}
	else
		return []
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
	
	//~ options.add("Define own Team")
	//~ values.add("custom")
	
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
	
	//~ options.add("Define own NameSpace")
	//~ values.add("custom")
	
	if( mode.equals("V") )
		return values
	
	if( mode.equals("O") )
		return options
	
	return []	
}

def props = getCredentials()
def namespaces = portusApiGET(props.folder.REPO_URL,"/api/v1/namespaces?all=true", props.creds.portus_api_token.user, props.creds.portus_api_token.pass)
def teams = portusApiGET(props.folder.REPO_URL, "/api/v1/teams?all=true",props.creds.portus_api_token.user, props.creds.portus_api_token.pass)

println namespaces
println teams

