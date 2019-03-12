import jenkins.model.*
import com.cloudbees.plugins.credentials.Credentials
import org.jenkinsci.plugins.plaincredentials.StringCredentials
import hudson.util.Secret

Set<Credentials> allCredentials = new HashSet<Credentials>();

List<String> artifacts = new ArrayList<String>()

if( !Test )
{
	artifacts.add("Please Select Credentials")
	return artifacts
}	

Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
{
	f -> creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(org.jenkinsci.plugins.plaincredentials.StringCredentials.class, f)
	allCredentials.addAll(creds)
}


for (c in allCredentials)
{
	if( Test.equals(c.id) )
	{
		secret = c.getSecret()		
		//artifacts.add(secret)
		artifacts.add(secret.getPlainText())
		return artifacts
	}
}

return ["No Credentials Found"]
