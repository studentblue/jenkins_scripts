import jenkins.model.*
import com.cloudbees.plugins.credentials.Credentials
import hudson.util.Secret
import groovy.json.JsonSlurper

try
{
	
	Set<Credentials> allCredentials = new HashSet<Credentials>();
	
	List<String> artifacts = new ArrayList<String>()
	
	if( !Test )
	{
		artifacts.add("Please Select Credentials:selected")
		return artifacts
	}
	
	Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
	{
		f -> creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials.class, f)
		allCredentials.addAll(creds)
	}
	
	userName = ""
	token = ""
	
	for (c in allCredentials)
	{
		if( Test.equals(c.id) )
		{		
			userName = c.getUsername()
			token = c.getPassword()
			break
		}
	}
	
	if( ! (userName && token) )
	{
		artifacts.add("Credentials not Found:selected")
		return artifacts
	}

	healthUrl = "https://docker-registry-cpsiot-2018.pii.at/api/v1/health"
	namespacesUrl = "https://docker-registry-cpsiot-2018.pii.at/api/v1/namespaces"
	def http = new URL(healthUrl).openConnection() as HttpURLConnection
	http.setRequestMethod('GET')
	//http.setDoOutput(true)
	http.setRequestProperty("Accept", "application/json")
	http.setRequestProperty("Portus-Auth", "${userName}:${token}")
	
	http.connect()
	
	responseCode = http.getResponseCode()
	
	if (responseCode == 200) 
	{
		//response = new JsonSlurper().parseText(http.inputStream.getText('UTF-8'))
		//artifacts.add(responseCode + " Health OK:selected")
		//return artifacts
		
		http.disconnect();
		
		http2 = new URL(namespacesUrl).openConnection() as HttpURLConnection
		http2.setRequestMethod('GET')
		http2.setRequestProperty("Accept", "application/json")
		http2.setRequestProperty("Portus-Auth", "${userName}:${token}")
		
		http2.connect()
		
		responseCode2 = http2.getResponseCode()
		
		if (responseCode2 == 200)
		{
		
			BufferedReader br = new BufferedReader(new InputStreamReader( ( http2.getInputStream() )))
			
			responseRaw = ""
			while ((output = br.readLine()) != null)
			{
				responseRaw += output
			}
			
			response = new JsonSlurper().parseText(responseRaw)
			//def artifactsJsonObject = jsonSlurper.parseText(artifactsObjectRaw)
		    //def dataArray = artifactsJsonObject
		    
		    for(item in response)
		    {
				artifacts.add(item.name)
			}
			
			return artifacts
		}
		else
		{
			artifacts.add( responseCode2 + " Connection Error:selected")
			return artifacts
		}
	}
	else 
	{
		//response = new JsonSlurper().parseText(http.errorStream.getText('UTF-8'))
		artifacts.add( responseCode + " Connection Error:selected")
		return artifacts
	}

}
catch (Exception e)
{
	// handle exception, e.g. Host unreachable, timeout etc.
	artifacts.add("An Error occured:selected")
	return artifacts
}
