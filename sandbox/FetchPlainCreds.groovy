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
							
							println f1.getId() + ": " +hudson.util.Secret.toString(f1.getSecret())
					}
				}
				
				creds = property.getCredentials(UsernamePasswordCredentialsImpl.class)
				
				if( creds )
				{
					creds.each
					{
						f1 ->
							
							//println f1.getId() + ": " +hudson.util.Secret.toString(f1.getSecret())
                      		
                      		println f1.getUsername() + ": " +hudson.util.Secret.toString(f1.getPassword())
                      		
                      		 	  	

					}
				}
			}
		}
}
