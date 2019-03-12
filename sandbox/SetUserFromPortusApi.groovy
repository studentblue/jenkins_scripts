import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty
import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*

import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl

import com.mig82.folders.properties.FolderProperties
import com.mig82.folders.properties.StringProperty

import groovy.json.JsonSlurperClassic

userProp = "PORTUS_USER"
userIdProp = "PORTUS_USER_ID"
adminToken = ""
adminTokenID = "portus-admin-token"
adminID = "admin"
url = "https://docker-registry-cpsiot-2018.pii.at/api/v1/users"
users = ""

def testFunction =
{
	String s1, String s2 ->
	println s1
	println s2
}

def getUsers =
{
	String url, String username, String token ->
		
		def get = new URL(url).openConnection();
		
		get.setRequestProperty("Accept", "application/json")
		get.setRequestProperty("Portus-Auth", "${username}:${token}")
		
		def responseCode = get.getResponseCode();
		
		if (responseCode == 200) 
		{
			def response = new JsonSlurperClassic().parseText(get.getInputStream().getText());
			
			users = response
		}
	
}

def getIDForUser =
{
	String username ->
		
		idOfUser = ""
		users.each
		{
			user ->
				if( username.equals(user["username"]) )
				{
					idOfUser = user["id"].toString()
					return true
				}
		}
		
		return idOfUser
}

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
							if( f1.getId().trim().equals(adminTokenID) )
							{
								
								adminToken = hudson.util.Secret.toString(f1.getSecret())
								return true
							}
					}
				}
			}
		}
}

getUsers(url, adminID, adminToken)

	
Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
{
	f ->
		if( f.getParent() instanceof hudson.model.Hudson )
		{	
			AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
			FolderProperties property = folderAbs.getProperties().get(FolderProperties.class)
			
			if( property )
			{
				
				userPropFound = false
				userName = ""
				
				userIdPropFound = false
				userIdPropRef = null
				
				testFunction( "Hello", "Dave" )
				
				property.getProperties().each
				{
					prop1 -> 
						
						if( prop1.key.equals(userProp) )
						{
							if( prop1.value )
							{
								userPropFound = true
								userName = prop1.value
							}
						}
						
						if( prop1.key.equals(userIdProp) )
						{
							
							userIdPropFound = true
							userIdPropRef = prop1
						}
				}
				
				if( userPropFound )
				{
					if( userIdPropFound )
					{
						if( userIdPropRef )
						{
							userValueId = getIDForUser(userName)
							if( userValueId )
							{
								userIdPropRef.setValue(userValueId)
								f.save()
							}
						}
					}
					else
					{
						userValueId = getIDForUser(userName)
						if( userValueId )
						{
							StringProperty[] test = [new StringProperty(userIdProp, userValueId)]
							property.setProperties(test)
							f.save()
						}
					}
				}
			}
		}
}

