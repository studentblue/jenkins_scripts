import jenkins.model.*
import com.cloudbees.plugins.credentials.Credentials

Set<Credentials> allCredentials = new HashSet<Credentials>();

List<String> artifacts = new ArrayList<String>()

/*
def creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
      com.cloudbees.plugins.credentials.Credentials.class
);

allCredentials.addAll(creds)
*/

//artifacts.add("Point 1")


Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
{
	f -> creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.plugins.credentials.Credentials.class, f)
	allCredentials.addAll(creds)
//	artifacts.add("Folder Name: " + f.name)
}

a = 1
for (c in allCredentials)
{
//	println(c.id + ": " + c.description)
	//if( c.getScope() == CredentialsScope.USER )
	scope = c.getScope()
	if( scope == com.cloudbees.plugins.credentials.CredentialsScope.USER )
		continue
		//artifacts.add(a + ": User Scope found")
	if( scope == com.cloudbees.plugins.credentials.CredentialsScope.SYSTEM )
		continue
		//artifacts.add(a + ": System Scope found")
	if( scope == com.cloudbees.plugins.credentials.CredentialsScope.GLOBAL )
		continue
		//artifacts.add(a+ ": Global Scope found")
	
	
	artifacts.add(c.id + ": " + c.description)
	a++
}

//artifacts.add("Point 2")

return artifacts
