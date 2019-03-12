import jenkins.model.*
import com.cloudbees.plugins.credentials.Credentials

Set<Credentials> allCredentials = new HashSet<Credentials>();

List<String> artifacts = new ArrayList<String>()

def creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
      com.cloudbees.plugins.credentials.Credentials.class
);

allCredentials.addAll(creds)

artifacts.add("Point 1")

Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
{
	f -> creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.plugins.credentials.Credentials.class, f)
	allCredentials.addAll(creds)
}

for (c in allCredentials)
{
//	println(c.id + ": " + c.description)
	artifacts.add(c.id + ": " + c.description)
}

artifacts.add("Point 2")

return artifacts
