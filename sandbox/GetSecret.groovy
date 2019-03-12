import jenkins.model.*
import com.cloudbees.plugins.credentials.Credentials
import org.jenkinsci.plugins.plaincredentials.StringCredentials

Set<Credentials> allCredentials = new HashSet<Credentials>();

List<String> artifacts = new ArrayList<String>()


/*
Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
{
	f -> creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.plugins.credentials.Credentials.class, f)
	allCredentials.addAll(creds)
}
*/
Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
{
	f -> creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(org.jenkinsci.plugins.plaincredentials.StringCredentials.class, f)
	allCredentials.addAll(creds)
}


for (c in allCredentials)
{
	artifacts.add(c.getClass())
	secret = c.getSecret()
	
	//artifacts.add(secret.getPlainText())
	
	//artifacts.add(secret.getClass())
	artifacts.add(c.id + ": " + secret)
}

return artifacts
