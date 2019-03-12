import jenkins.model.*
import com.cloudbees.plugins.credentials.Credentials
import hudson.util.Secret
import groovy.json.JsonSlurper


artifacts = []
Set<Credentials> allCredentials = new HashSet<Credentials>()

def getID(url, username, password, match, health = false, tags = false)
{
	def http = new URL(url).openConnection() as HttpURLConnection
	http.setRequestMethod('GET')

	http.setRequestProperty("Accept", "application/json")
	http.setRequestProperty("Portus-Auth", "${userName}:${password}")
	
	http.connect()
	
	responseCode = http.getResponseCode()
	
	if (responseCode == 200) 
	{
		if( health )
			return true
		
		BufferedReader br = new BufferedReader(new InputStreamReader( ( http.getInputStream() )))
				
		responseRaw = ""
		while ((output = br.readLine()) != null)
		{
			responseRaw += output
		}
		
		response = new JsonSlurper().parseText(responseRaw)	
	    
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


try
{
	
	if( !binding.hasVariable('PortusCredsID') )
		return artifacts
	
	if( ! PortusCredsID )
		return artifacts
	
	if( !binding.hasVariable('repoName') )
		return artifacts
	
	if( !repoName )
		return artifacts
	
	if( !binding.hasVariable('nameSpace') )
		return artifacts
	
	if( !nameSpace )
		return artifacts
	
	PortusCredsIDTrimmed = PortusCredsID.trim()
	repoNameTrimmed = repoName.trim()
	nameSpaceTrimmed = nameSpace.trim()
	
	Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
	{
		f -> creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials.class, f)
		allCredentials.addAll(creds)
	}
	
	userName = ""
	token = ""
	nameSpaceID = ""
	repoID = ""
	
	for (c in allCredentials)
	{
		if( PortusCredsIDTrimmed.equals(c.id) )
		{		
			userName = c.getUsername()
			token = c.getPassword()
			break
		}
	}
	
	if( ! (userName && token) )
	{
		return artifacts
	}
	
	healthUrl = "https://docker-registry-cpsiot-2018.pii.at/api/v1/health"
	namespacesUrl = "https://docker-registry-cpsiot-2018.pii.at/api/v1/namespaces"
	
	if(! getID(healthUrl, userName, token, "", true) )
		return artifacts
	
	nameSpaceID = getID(namespacesUrl, userName, token, nameSpaceTrimmed)
	
	if( ! nameSpaceID )
		return artifacts
	
	repoUrl = "https://docker-registry-cpsiot-2018.pii.at/api/v1/namespaces/${nameSpaceID}/repositories"
	
	repoID = getID(repoUrl, userName, token, repoNameTrimmed)
	
	if( ! repoID )
		return artifacts
	
	tagsUrl = "https://docker-registry-cpsiot-2018.pii.at/api/v1/repositories/${repoID}/tags"
	
	tags = getID(tagsUrl, userName, token, "", false, true)
	
	if( tags )
		return artifacts
	else
		return []
}
catch (Exception e)
{
	// handle exception, e.g. Host unreachable, timeout etc.	
	return []
}
