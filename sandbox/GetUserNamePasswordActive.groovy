import jenkins.model.*
import com.cloudbees.plugins.credentials.Credentials
import hudson.util.Secret

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

for (c in allCredentials)
{
	if( Test.equals(c.id) )
	{		
		artifacts.add(c.getUsername() + ":" + c.getPassword())
		return artifacts
	}
}

return ["No Credentials Found"]
