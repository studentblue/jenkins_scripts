import jenkins.model.*
import com.cloudbees.plugins.credentials.Credentials
//import org.jenkinsci.plugins.plaincredentials.StringCredentials
//import com.cloudbees.plugins.credentials.common

Set<Credentials> allCredentials = new HashSet<Credentials>();

List<String> artifacts = new ArrayList<String>()

Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
{
	f -> creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials.class, f)
	allCredentials.addAll(creds)
}

artifacts.add(":selected")

for (c in allCredentials)
{
	artifacts.add(c.id)
}

return artifacts
